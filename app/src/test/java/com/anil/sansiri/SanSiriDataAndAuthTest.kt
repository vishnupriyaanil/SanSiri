package com.anil.sansiri

import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.data.repository.SanSiriRepository
import com.anil.sansiri.ui.auth.AuthViewModel
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
class SanSiriDataAndAuthTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSanSiriRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSanSiriRepository()
        authViewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testQuickDemoLogin_HouseOwner() = runTest {
        authViewModel.onRoleSelected(UserRole.HOUSE_OWNER)
        authViewModel.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.HOUSE_OWNER, state.currentUser?.role)
    }

    @Test
    fun testQuickDemoLogin_Admin() = runTest {
        authViewModel.onRoleSelected(UserRole.ADMIN)
        authViewModel.quickDemoLogin(UserRole.ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.ADMIN, state.currentUser?.role)
    }

    @Test
    fun testQuickDemoLogin_SuperAdmin() = runTest {
        authViewModel.quickDemoLogin(UserRole.SUPER_ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
        assertEquals("+919964532102", state.currentUser?.phoneNumber)
        assertEquals("San Siri Super Admin", state.currentUser?.name)
    }

    @Test
    fun testSuperAdminLoginWithCredentials() = runTest {
        authViewModel.onPhoneNumberChanged("9964532102")
        authViewModel.onOtpCodeChanged("12345")
        authViewModel.verifyOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
        assertEquals("+919964532102", state.currentUser?.phoneNumber)
    }

    @Test
    fun testAddAdminAndGetAdmins() = runTest {
        val result = fakeRepository.addAdmin("9876512340", "Rajesh Admin")
        assertTrue(result.isSuccess)

        val admins = fakeRepository.getAdmins().first()
        assertTrue(admins.any { it.phoneNumber.contains("9876512340") && it.role == UserRole.ADMIN })
    }

    @Test
    fun testHouseDetailCRUD() = runTest {
        val house = HouseDetail(
            id = "test_h1",
            ownerUid = "owner_123",
            ownerName = "John Doe",
            ownerPhone = "+919999988888",
            houseNumber = "C-301",
            crossStreet = "5th Cross",
            occupancyType = OccupancyType.SELF_OCCUPIED,
            approvalStatus = ApprovalStatus.PENDING
        )

        val saveResult = fakeRepository.saveHouseDetail(house)
        assertTrue(saveResult.isSuccess)

        val retrievedHouse = fakeRepository.getHouseDetailByOwner("owner_123").first()
        assertNotNull(retrievedHouse)
        assertEquals("C-301", retrievedHouse?.houseNumber)

        val updateResult = fakeRepository.updateHouseStatus("test_h1", ApprovalStatus.APPROVED)
        assertTrue(updateResult.isSuccess)

        val approvedHouses = fakeRepository.getApprovedHouses().first()
        assertTrue(approvedHouses.any { it.id == "test_h1" })
    }

    @Test
    fun testPricingRulesAndUpdate() = runTest {
        val initialRules = fakeRepository.getPricingRules().first()
        assertEquals(1500.0, initialRules.selfOccupiedMaintenanceRate, 0.01)

        val newRules = PricingRules(
            selfOccupiedMaintenanceRate = 1800.0,
            rentedMaintenanceRate = 2200.0
        )
        fakeRepository.updatePricingRules(newRules)

        val updated = fakeRepository.getPricingRules().first()
        assertEquals(1800.0, updated.selfOccupiedMaintenanceRate, 0.01)
        assertEquals(2200.0, updated.rentedMaintenanceRate, 0.01)
    }

    @Test
    fun testMaintenancePaymentsAndApproval() = runTest {
        val payment = MaintenancePayment(
            id = "pay_test_1",
            houseId = "test_h1",
            ownerUid = "owner_123",
            ownerName = "John Doe",
            houseNumber = "C-301",
            monthYear = "2025-06",
            amount = 1800.0,
            paymentMethod = "UPI",
            transactionRef = "UPI000111222",
            status = PaymentStatus.PENDING
        )

        val submitResult = fakeRepository.submitPayment(payment)
        assertTrue(submitResult.isSuccess)

        val pendingList = fakeRepository.getPendingPayments().first()
        assertTrue(pendingList.any { it.id == "pay_test_1" })

        val approveResult = fakeRepository.approvePayment("pay_test_1")
        assertTrue(approveResult.isSuccess)

        val updatedOwnerPayments = fakeRepository.getPaymentsByOwner("owner_123").first()
        val approvedPayment = updatedOwnerPayments.find { it.id == "pay_test_1" }
        assertNotNull(approvedPayment)
        assertEquals(PaymentStatus.APPROVED, approvedPayment?.status)
    }

    @Test
    fun testLogout() = runTest {
        authViewModel.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(authViewModel.uiState.value.currentUser)

        authViewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(authViewModel.uiState.value.currentUser)
    }
}
