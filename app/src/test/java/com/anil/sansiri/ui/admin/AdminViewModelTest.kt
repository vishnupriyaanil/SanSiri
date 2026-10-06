package com.anil.sansiri.ui.admin

import com.anil.sansiri.FakeSanSiriRepository
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSanSiriRepository
    private lateinit var viewModel: AdminViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSanSiriRepository()
        viewModel = AdminViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAdminHouseApprovalWorkflow() = runTest {
        val pendingHouse = HouseDetail(
            id = "house_pending_1",
            ownerUid = "owner_1",
            ownerName = "Anil Kumar",
            ownerPhone = "+919876543210",
            houseNumber = "A-101",
            crossStreet = "1st Cross",
            occupancyType = OccupancyType.SELF_OCCUPIED,
            approvalStatus = ApprovalStatus.PENDING
        )

        fakeRepository.saveHouseDetail(pendingHouse)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals(1, initialState.pendingHouses.size)
        assertEquals("A-101", initialState.pendingHouses.first().houseNumber)

        viewModel.approveHouse("house_pending_1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(0, updatedState.pendingHouses.size)
        assertEquals(1, updatedState.approvedHouses.size)
        assertEquals("A-101", updatedState.approvedHouses.first().houseNumber)
        assertEquals(ApprovalStatus.APPROVED, updatedState.approvedHouses.first().approvalStatus)
        assertNotNull(updatedState.statusMessage)
    }

    @Test
    fun testAdminHouseRejectionWorkflow() = runTest {
        val pendingHouse = HouseDetail(
            id = "house_pending_2",
            ownerUid = "owner_2",
            ownerName = "Rahul Sharma",
            ownerPhone = "+919876543211",
            houseNumber = "B-202",
            crossStreet = "2nd Cross",
            occupancyType = OccupancyType.RENTED,
            approvalStatus = ApprovalStatus.PENDING
        )

        fakeRepository.saveHouseDetail(pendingHouse)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingHouses.size)

        viewModel.rejectHouse("house_pending_2")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(0, updatedState.pendingHouses.size)
        assertEquals(0, updatedState.approvedHouses.size)
        assertNotNull(updatedState.statusMessage)

        val houseInRepo = fakeRepository.getAllHouses().first().find { it.id == "house_pending_2" }
        assertNotNull(houseInRepo)
        assertEquals(ApprovalStatus.REJECTED, houseInRepo?.approvalStatus)
    }

    @Test
    fun testPricingRulesUpdateWorkflow() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSelfOccupiedRateChanged("1800")
        viewModel.onRentedRateChanged("2500")

        viewModel.savePricingRules()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.statusMessage)
        assertNull(state.errorMessage)

        val updatedRules = fakeRepository.getPricingRules().first()
        assertEquals(1800.0, updatedRules.selfOccupiedMaintenanceRate, 0.01)
        assertEquals(2500.0, updatedRules.rentedMaintenanceRate, 0.01)
    }

    @Test
    fun testInvalidPricingRulesUpdateShowsError() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSelfOccupiedRateChanged("0")
        viewModel.onRentedRateChanged("-500")

        viewModel.savePricingRules()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)

        // Repository rules should remain unchanged
        val rules = fakeRepository.getPricingRules().first()
        assertEquals(1500.0, rules.selfOccupiedMaintenanceRate, 0.01)
    }

    @Test
    fun testPaymentApprovalWorkflow() = runTest {
        val payment = MaintenancePayment(
            id = "pay_pending_1",
            houseId = "house_1",
            ownerUid = "owner_1",
            ownerName = "Anil Kumar",
            houseNumber = "A-101",
            monthYear = "May 2025",
            amount = 1500.0,
            paymentMethod = "UPI",
            transactionRef = "UPI123456789",
            status = PaymentStatus.PENDING
        )

        fakeRepository.submitPayment(payment)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals(1, initialState.pendingPayments.size)
        assertEquals(1500.0, initialState.pendingApprovalAmount, 0.01)
        assertEquals(0.0, initialState.totalCollected, 0.01)
        assertEquals(0, initialState.approvedPaymentsCount)

        viewModel.approvePayment("pay_pending_1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(0, updatedState.pendingPayments.size)
        assertEquals(1500.0, updatedState.totalCollected, 0.01)
        assertEquals(1, updatedState.approvedPaymentsCount)
        assertNotNull(updatedState.statusMessage)
    }

    @Test
    fun testPaymentRejectionWorkflow() = runTest {
        val payment = MaintenancePayment(
            id = "pay_pending_2",
            houseId = "house_2",
            ownerUid = "owner_2",
            ownerName = "Suresh Raina",
            houseNumber = "C-303",
            monthYear = "May 2025",
            amount = 2000.0,
            paymentMethod = "Bank Transfer",
            transactionRef = "TXN998877",
            status = PaymentStatus.PENDING
        )

        fakeRepository.submitPayment(payment)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingPayments.size)

        viewModel.rejectPayment("pay_pending_2")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(0, updatedState.pendingPayments.size)
        assertEquals(0.0, updatedState.totalCollected, 0.01)
        assertNotNull(updatedState.statusMessage)

        val paymentInRepo = fakeRepository.getAllPayments().first().find { it.id == "pay_pending_2" }
        assertNotNull(paymentInRepo)
        assertEquals(PaymentStatus.REJECTED, paymentInRepo?.status)
    }

    @Test
    fun testTabSelectionAndClearMessages() = runTest {
        viewModel.setSelectedTab(2)
        assertEquals(2, viewModel.uiState.value.selectedTab)

        viewModel.onSelfOccupiedRateChanged("1700")
        viewModel.savePricingRules()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.statusMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.statusMessage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testSuperAdminAddAdminWorkflow() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial admins contains pre-populated Super Admin and Admin
        val initialAdmins = viewModel.uiState.value.adminsList
        assertTrue(initialAdmins.any { it.role == UserRole.SUPER_ADMIN })

        viewModel.onNewAdminPhoneChanged("9876543210")
        viewModel.onNewAdminNameChanged("Suresh Admin")
        viewModel.addAdmin()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.statusMessage)
        assertNull(state.errorMessage)
        assertEquals("", state.newAdminPhone)
        assertEquals("", state.newAdminName)

        val updatedAdmins = state.adminsList
        assertTrue(updatedAdmins.any { it.name == "Suresh Admin" && it.role == UserRole.ADMIN })
    }

    @Test
    fun testAddAdminFailsWithInvalidInput() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNewAdminPhoneChanged("12345")
        viewModel.onNewAdminNameChanged("Invalid")
        viewModel.addAdmin()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }
}
