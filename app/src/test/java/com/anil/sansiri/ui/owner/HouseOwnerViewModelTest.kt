package com.anil.sansiri.ui.owner

import com.anil.sansiri.FakeSanSiriRepository
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.PropertyType
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
    fun testMaintenanceFeeCalculation_IndividualVsRentedHouse() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        // Default propertyType is INDIVIDUAL_HOUSE -> rate should be 1500.0
        assertEquals(1500.0, viewModel.uiState.value.currentCalculatedFee, 0.01)

        // Switch to HOUSE_WITH_RENTED_HOMES with 1 rented unit (Self Occupied 1500 + 1 Rented 2000 = 3500.0)
        viewModel.onOccupancySelectionChanged(
            isSelfOccupied = true,
            isRented = true,
            isApartment = false,
            isEmptySite = false,
            numberOfRentedUnits = 1,
            occupancyType = com.anil.sansiri.data.model.OccupancyType.RENTED
        )
        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3500.0, viewModel.uiState.value.currentCalculatedFee, 0.01)
    }

    @Test
    fun testMaintenanceFeeCalculation_RepositoryHelper() = runTest {
        val rules = PricingRules(
            selfOccupiedMaintenanceRate = 1600.0,
            rentedMaintenanceRate = 2200.0,
            emptySiteMaintenanceRate = 500.0
        )

        val individualHouse = HouseDetail(isSelfOccupied = true, isRented = false)
        val rentedHouse = HouseDetail(isSelfOccupied = true, isRented = true, numberOfRentedUnits = 1)
        val apartmentHouse = HouseDetail(isApartment = true)
        val emptySite = HouseDetail(occupancyType = com.anil.sansiri.data.model.OccupancyType.EMPTY_SITE)

        val selfFee = fakeRepository.calculateMaintenanceFee(individualHouse, rules)
        val rentedFee = fakeRepository.calculateMaintenanceFee(rentedHouse, rules)
        val aptFee = fakeRepository.calculateMaintenanceFee(apartmentHouse, rules)
        val siteFee = fakeRepository.calculateMaintenanceFee(emptySite, rules)

        assertEquals(1600.0, selfFee, 0.01)
        assertEquals(3800.0, rentedFee, 0.01)
        assertEquals(1600.0, aptFee, 0.01)
        assertEquals(500.0, siteFee, 0.01)
    }

    @Test
    fun testDynamicFeeRecalculationWhenPricingRulesUpdate() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1500.0, viewModel.uiState.value.currentCalculatedFee, 0.01)

        // Super Admin updates pricing rules
        val updatedRules = PricingRules(
            selfOccupiedMaintenanceRate = 1800.0,
            rentedMaintenanceRate = 2400.0
        )
        fakeRepository.updatePricingRules(updatedRules)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1800.0, viewModel.uiState.value.currentCalculatedFee, 0.01)

        viewModel.onOccupancySelectionChanged(
            isSelfOccupied = true,
            isRented = true,
            isApartment = false,
            isEmptySite = false,
            numberOfRentedUnits = 1,
            occupancyType = com.anil.sansiri.data.model.OccupancyType.RENTED
        )
        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        // Self occupied 1800 + 1 * rented 2400 = 4200.0
        assertEquals(4200.0, viewModel.uiState.value.currentCalculatedFee, 0.01)
    }

    @Test
    fun testSaveHouseDetailFlow() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPropertyTypeChanged(PropertyType.APARTMENT)
        viewModel.onBuildingNumberChanged("A")
        viewModel.onFloorNumberChanged("2")
        viewModel.onHouseNumberChanged("A-101")
        viewModel.onCrossStreetChanged("3rd Cross")

        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.houseDetail)
        assertEquals("A-101", state.houseDetail?.houseNumber)
        assertEquals(PropertyType.APARTMENT, state.houseDetail?.propertyType)
        assertEquals("3rd Cross", state.houseDetail?.crossStreet)
        assertEquals(ApprovalStatus.PENDING, state.houseDetail?.approvalStatus)
        assertNotNull(state.houseSaveSuccess)
        assertNull(state.houseSaveError)
    }

    @Test
    fun testPaymentSubmissionFlow() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onOccupancySelectionChanged(
            isSelfOccupied = true,
            isRented = true,
            isApartment = false,
            isEmptySite = false,
            numberOfRentedUnits = 1,
            occupancyType = com.anil.sansiri.data.model.OccupancyType.RENTED
        )
        viewModel.onSiteNumberChanged("Site #105")
        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onMonthYearChanged("May 2025")
        viewModel.onPaymentMethodChanged("UPI")
        viewModel.onTransactionRefChanged("UPI987654321")

        viewModel.submitPayment()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.paymentSubmitSuccess)
        assertNull(state.paymentSubmitError)

        assertEquals(1, state.paymentHistory.size)
        val payment = state.paymentHistory.first()
        assertEquals("May 2025", payment.monthYear)
        assertEquals(3500.0, payment.amount, 0.01)
        assertEquals("UPI", payment.paymentMethod)
        assertEquals("UPI987654321", payment.transactionRef)
        assertEquals(PaymentStatus.PENDING, payment.status)
    }

    @Test
    fun testPaymentSubmissionFailsWithoutTransactionRef() = runTest {
        fakeRepository.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPropertyTypeChanged(PropertyType.INDIVIDUAL_HOUSE)
        viewModel.onSiteNumberChanged("Site #88")
        viewModel.saveHouseDetail()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTransactionRefChanged("")
        viewModel.submitPayment()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.paymentSubmitError)
        assertEquals(0, state.paymentHistory.size)
    }
}
