package com.anil.sansiri.ui.owner

import com.anil.sansiri.FakeSanSiriRepository
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HouseOwnerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSanSiriRepository
    private lateinit var viewModel: HouseOwnerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSanSiriRepository()
        viewModel = HouseOwnerViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testMaintenanceFeeCalculation_SelfOccupiedVsRented() = runTest {
        // Quick login to initialize user
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        // Default occupancy is SELF_OCCUPIED -> rate should be 1500.0
        viewModel.onOccupancyTypeChanged(OccupancyType.SELF_OCCUPIED)
        assertEquals(1500.0, viewModel.uiState.value.currentCalculatedFee, 0.01)

        // Switch occupancy to RENTED -> rate should be 2000.0
        viewModel.onOccupancyTypeChanged(OccupancyType.RENTED)
        assertEquals(2000.0, viewModel.uiState.value.currentCalculatedFee, 0.01)
    }

    @Test
    fun testMaintenanceFeeCalculation_RepositoryHelper() = runTest {
        val rules = PricingRules(
            selfOccupiedMaintenanceRate = 1600.0,
            rentedMaintenanceRate = 2200.0
        )

        val selfFee = fakeRepository.calculateMaintenanceFee(OccupancyType.SELF_OCCUPIED, rules)
        val rentedFee = fakeRepository.calculateMaintenanceFee(OccupancyType.RENTED, rules)

        assertEquals(1600.0, selfFee, 0.01)
        assertEquals(2200.0, rentedFee, 0.01)
    }

    @Test
    fun testDynamicFeeRecalculationWhenPricingRulesUpdate() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onOccupancyTypeChanged(OccupancyType.SELF_OCCUPIED)
        assertEquals(1500.0, viewModel.uiState.value.currentCalculatedFee, 0.01)

        // Admin updates pricing rules
        val updatedRules = PricingRules(
            selfOccupiedMaintenanceRate = 1800.0,
            rentedMaintenanceRate = 2400.0
        )
        fakeRepository.updatePricingRules(updatedRules)
        testDispatcher.scheduler.advanceUntilIdle()

        // Calculated fee in ViewModel should automatically update
        assertEquals(1800.0, viewModel.uiState.value.currentCalculatedFee, 0.01)

        // Switch to RENTED
        viewModel.onOccupancyTypeChanged(OccupancyType.RENTED)
        assertEquals(2400.0, viewModel.uiState.value.currentCalculatedFee, 0.01)
    }

    @Test
    fun testSaveHouseDetailFlow() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onBuildingNameChanged("San Siri Residency")
        viewModel.onHouseNumberChanged("A-101")
        viewModel.onCrossStreetChanged("3rd Cross")
        viewModel.onOccupancyTypeChanged(OccupancyType.SELF_OCCUPIED)

        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.houseDetail)
        assertEquals("A-101", state.houseDetail?.houseNumber)
        assertEquals("San Siri Residency", state.houseDetail?.buildingName)
        assertEquals("3rd Cross", state.houseDetail?.crossStreet)
        assertEquals(OccupancyType.SELF_OCCUPIED, state.houseDetail?.occupancyType)
        assertEquals(ApprovalStatus.PENDING, state.houseDetail?.approvalStatus)
        assertNotNull(state.houseSaveSuccess)
        assertNull(state.houseSaveError)
    }

    @Test
    fun testPaymentSubmissionFlow() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        // Register house first
        viewModel.onHouseNumberChanged("B-202")
        viewModel.onOccupancyTypeChanged(OccupancyType.RENTED)
        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        // Submit maintenance payment
        viewModel.onMonthYearChanged("May 2025")
        viewModel.onPaymentMethodChanged("UPI")
        viewModel.onTransactionRefChanged("UPI987654321")

        viewModel.submitPayment()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.paymentSubmitSuccess)
        assertNull(state.paymentSubmitError)

        // Check payment history
        assertEquals(1, state.paymentHistory.size)
        val payment = state.paymentHistory.first()
        assertEquals("May 2025", payment.monthYear)
        assertEquals(2000.0, payment.amount, 0.01)
        assertEquals("UPI", payment.paymentMethod)
        assertEquals("UPI987654321", payment.transactionRef)
        assertEquals(PaymentStatus.PENDING, payment.status)
    }

    @Test
    fun testPaymentSubmissionFailsWithoutTransactionRef() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onHouseNumberChanged("C-303")
        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        // Leave transaction reference blank
        viewModel.onTransactionRefChanged("")
        viewModel.submitPayment()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.paymentSubmitError)
        assertEquals(0, state.paymentHistory.size)
    }
}
