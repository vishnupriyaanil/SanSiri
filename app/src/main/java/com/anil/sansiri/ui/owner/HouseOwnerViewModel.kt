package com.anil.sansiri.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.repository.SanSiriRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HouseOwnerUiState(
    val currentUser: User? = null,
    val houseDetail: HouseDetail? = null,
    val pricingRules: PricingRules = PricingRules(),
    val paymentHistory: List<MaintenancePayment> = emptyList(),
    val approvedHouses: List<HouseDetail> = emptyList(),
    val selectedTab: Int = 0,

    // Property / House registration form fields
    val propertyType: PropertyType = PropertyType.INDIVIDUAL_HOUSE,
    val siteNumber: String = "",
    val buildingName: String = "San Siri Residency",
    val buildingNumber: String = "",
    val floorNumber: String = "",
    val houseNumber: String = "",
    val numberOfRentedHousesInput: String = "0",
    val crossStreet: String = "",
    val occupancyType: OccupancyType = OccupancyType.SELF_OCCUPIED,
    val isSelfOccupied: Boolean = true,
    val isRented: Boolean = false,
    val numberOfRentedUnits: Int = 0,
    val isApartment: Boolean = false,
    val isSavingHouse: Boolean = false,
    val houseSaveSuccess: String? = null,
    val houseSaveError: String? = null,

    // Maintenance payment form fields
    val selectedMonthYear: String = "May 2025",
    val selectedPaymentMethod: String = "UPI",
    val transactionRef: String = "",
    val isSubmittingPayment: Boolean = false,
    val paymentSubmitSuccess: String? = null,
    val paymentSubmitError: String? = null
) {
    val currentCalculatedFee: Double
        get() {
            val detail = houseDetail ?: HouseDetail(
                propertyType = propertyType,
                numberOfRentedHouses = numberOfRentedHousesInput.toIntOrNull() ?: 0,
                occupancyType = occupancyType,
                isSelfOccupied = isSelfOccupied,
                isRented = isRented,
                numberOfRentedUnits = numberOfRentedUnits,
                isApartment = isApartment
            )
            if (detail.isApartment || detail.propertyType == PropertyType.APARTMENT) {
                return pricingRules.selfOccupiedMaintenanceRate
            }
            if (detail.occupancyType == OccupancyType.EMPTY_SITE || detail.propertyType == PropertyType.EMPTY_SITE) {
                return pricingRules.emptySiteMaintenanceRate
            }
            var totalFee = 0.0
            val isSelf = detail.isSelfOccupied || (detail.occupancyType == OccupancyType.SELF_OCCUPIED && !detail.isRented) || detail.propertyType == PropertyType.INDIVIDUAL_HOUSE
            val isRent = detail.isRented || detail.occupancyType == OccupancyType.RENTED || detail.propertyType == PropertyType.HOUSE_WITH_RENTED_HOMES
            if (isSelf) {
                totalFee += pricingRules.selfOccupiedMaintenanceRate
            }
            if (isRent) {
                val rentedCount = if (detail.numberOfRentedUnits > 0) detail.numberOfRentedUnits else maxOf(1, detail.numberOfRentedHouses)
                totalFee += (rentedCount * pricingRules.rentedMaintenanceRate)
            }
            if (!isSelf && !isRent) {
                return pricingRules.selfOccupiedMaintenanceRate
            }
            return totalFee
        }

    val registrationStatusMessage: String
        get() {
            val displayNum = houseDetail?.displayHouseOrSiteNumber ?: houseNumber.ifBlank { siteNumber }
            return when (houseDetail?.approvalStatus) {
                ApprovalStatus.PENDING -> "Your property registration ($displayNum) is pending Admin approval."
                ApprovalStatus.APPROVED -> "Property $displayNum verified and approved."
                ApprovalStatus.REJECTED -> "Your property registration ($displayNum) was rejected by Admin."
                null -> if (displayNum.isNotBlank()) "Your property details ($displayNum) are submitted." else "Please register your property details to access all features."
            }
        }
}

class HouseOwnerViewModel(private val repository: SanSiriRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseOwnerUiState())
    val uiState: StateFlow<HouseOwnerUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collectLatest { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    observeHouseDetail(user.uid)
                    observePayments(user.uid)
                }
            }
        }

        viewModelScope.launch {
            repository.getPricingRules().collectLatest { rules ->
                _uiState.update { it.copy(pricingRules = rules) }
            }
        }

        viewModelScope.launch {
            repository.getApprovedHouses().collectLatest { houses ->
                _uiState.update { it.copy(approvedHouses = houses) }
            }
        }
    }

    private fun observeHouseDetail(ownerUid: String) {
        viewModelScope.launch {
            repository.getHouseDetailByOwner(ownerUid).collectLatest { house ->
                _uiState.update { state ->
                    state.copy(
                        houseDetail = house,
                        propertyType = house?.propertyType ?: state.propertyType,
                        siteNumber = house?.siteNumber ?: state.siteNumber,
                        buildingName = house?.buildingName ?: state.buildingName,
                        buildingNumber = house?.buildingNumber ?: state.buildingNumber,
                        floorNumber = house?.floorNumber ?: state.floorNumber,
                        houseNumber = house?.houseNumber ?: state.houseNumber,
                        numberOfRentedHousesInput = house?.numberOfRentedHouses?.toString() ?: state.numberOfRentedHousesInput,
                        crossStreet = house?.crossStreet ?: state.crossStreet,
                        occupancyType = house?.occupancyType ?: state.occupancyType,
                        isSelfOccupied = house?.isSelfOccupied ?: state.isSelfOccupied,
                        isRented = house?.isRented ?: state.isRented,
                        numberOfRentedUnits = house?.numberOfRentedUnits ?: state.numberOfRentedUnits,
                        isApartment = house?.isApartment ?: state.isApartment
                    )
                }
            }
        }
    }

    private fun observePayments(ownerUid: String) {
        viewModelScope.launch {
            repository.getPaymentsByOwner(ownerUid).collectLatest { payments ->
                _uiState.update { it.copy(paymentHistory = payments) }
            }
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onPropertyTypeChanged(type: PropertyType) {
        _uiState.update { it.copy(propertyType = type, houseSaveError = null) }
    }

    fun onSiteNumberChanged(number: String) {
        _uiState.update { it.copy(siteNumber = number, houseSaveError = null) }
    }

    fun onBuildingNameChanged(name: String) {
        _uiState.update { it.copy(buildingName = name, houseSaveError = null) }
    }

    fun onBuildingNumberChanged(number: String) {
        _uiState.update { it.copy(buildingNumber = number, houseSaveError = null) }
    }

    fun onFloorNumberChanged(number: String) {
        _uiState.update { it.copy(floorNumber = number, houseSaveError = null) }
    }

    fun onHouseNumberChanged(number: String) {
        _uiState.update { it.copy(houseNumber = number, houseSaveError = null) }
    }

    fun onRentedHousesCountChanged(count: String) {
        _uiState.update { it.copy(numberOfRentedHousesInput = count, houseSaveError = null) }
    }

    fun onCrossStreetChanged(street: String) {
        _uiState.update { it.copy(crossStreet = street, houseSaveError = null) }
    }

    fun onOccupancyTypeChanged(type: OccupancyType) {
        _uiState.update { it.copy(occupancyType = type, houseSaveError = null) }
    }

    fun onOccupancySelectionChanged(
        isSelfOccupied: Boolean,
        isRented: Boolean,
        isApartment: Boolean,
        isEmptySite: Boolean,
        numberOfRentedUnits: Int,
        occupancyType: OccupancyType
    ) {
        val propType = when {
            isApartment -> PropertyType.APARTMENT
            isEmptySite -> PropertyType.EMPTY_SITE
            isRented -> PropertyType.HOUSE_WITH_RENTED_HOMES
            else -> PropertyType.INDIVIDUAL_HOUSE
        }
        _uiState.update {
            it.copy(
                isSelfOccupied = isSelfOccupied,
                isRented = isRented,
                isApartment = isApartment,
                numberOfRentedUnits = numberOfRentedUnits,
                numberOfRentedHousesInput = numberOfRentedUnits.toString(),
                occupancyType = occupancyType,
                propertyType = propType,
                houseSaveError = null
            )
        }
    }

    fun saveHouseDetail() {
        val currentState = _uiState.value
        val pType = currentState.propertyType
        val siteNum = currentState.siteNumber.trim()
        val bldgNum = currentState.buildingNumber.trim()
        val flrNum = currentState.floorNumber.trim()
        val houseNum = currentState.houseNumber.trim()
        val rentedCount = currentState.numberOfRentedHousesInput.toIntOrNull() ?: 0
        val street = currentState.crossStreet.trim()

        // Validation based on selected PropertyType
        when (pType) {
            PropertyType.INDIVIDUAL_HOUSE, PropertyType.EMPTY_SITE -> {
                if (siteNum.isBlank() && houseNum.isBlank()) {
                    _uiState.update { it.copy(houseSaveError = "Please enter Site or House Number") }
                    return
                }
            }
            PropertyType.HOUSE_WITH_RENTED_HOMES -> {
                if (siteNum.isBlank() && houseNum.isBlank()) {
                    _uiState.update { it.copy(houseSaveError = "Please enter Site or House Number") }
                    return
                }
                if (rentedCount <= 0) {
                    _uiState.update { it.copy(houseSaveError = "Please enter valid number of rented houses") }
                    return
                }
            }
            PropertyType.APARTMENT -> {
                if (houseNum.isBlank()) {
                    _uiState.update { it.copy(houseSaveError = "Please enter House/Flat Number") }
                    return
                }
            }
        }

        _uiState.update { it.copy(isSavingHouse = true, houseSaveError = null, houseSaveSuccess = null) }

        viewModelScope.launch {
            val user = currentState.currentUser
            val existing = currentState.houseDetail
            val updatedDetail = HouseDetail(
                id = existing?.id ?: "",
                ownerUid = user?.uid ?: "mock_owner_123",
                ownerName = user?.name ?: "House Owner",
                ownerPhone = user?.phoneNumber ?: "+919876543210",
                propertyType = pType,
                siteNumber = siteNum,
                buildingName = currentState.buildingName.ifBlank { "San Siri Residency" },
                buildingNumber = bldgNum,
                floorNumber = flrNum,
                houseNumber = houseNum,
                numberOfRentedHouses = if (currentState.isRented) currentState.numberOfRentedUnits else rentedCount,
                crossStreet = street,
                occupancyType = currentState.occupancyType,
                approvalStatus = existing?.approvalStatus ?: ApprovalStatus.PENDING,
                updatedAt = System.currentTimeMillis(),
                isSelfOccupied = currentState.isSelfOccupied,
                isRented = currentState.isRented,
                numberOfRentedUnits = currentState.numberOfRentedUnits,
                isApartment = currentState.isApartment
            )

            val result = repository.saveHouseDetail(updatedDetail)
            result.onSuccess { saved ->
                _uiState.update {
                    it.copy(
                        isSavingHouse = false,
                        houseDetail = saved,
                        houseSaveSuccess = "Property details saved successfully! Status: ${saved.approvalStatus.name}"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSavingHouse = false,
                        houseSaveError = error.localizedMessage ?: "Failed to save property details"
                    )
                }
            }
        }
    }

    fun onMonthYearChanged(monthYear: String) {
        _uiState.update { it.copy(selectedMonthYear = monthYear, paymentSubmitError = null) }
    }

    fun onPaymentMethodChanged(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method, paymentSubmitError = null) }
    }

    fun onTransactionRefChanged(ref: String) {
        _uiState.update { it.copy(transactionRef = ref, paymentSubmitError = null) }
    }

    fun submitPayment() {
        val currentState = _uiState.value
        val house = currentState.houseDetail
        val user = currentState.currentUser
        val amount = currentState.currentCalculatedFee
        val ref = currentState.transactionRef.trim()
        val houseNum = house?.displayHouseOrSiteNumber ?: currentState.houseNumber.ifBlank { currentState.siteNumber }

        if (houseNum.isBlank()) {
            _uiState.update { it.copy(paymentSubmitError = "Please save property details before submitting maintenance payment") }
            return
        }

        if (ref.isBlank()) {
            _uiState.update { it.copy(paymentSubmitError = "Please enter Transaction Reference ID / Note") }
            return
        }

        _uiState.update { it.copy(isSubmittingPayment = true, paymentSubmitError = null, paymentSubmitSuccess = null) }

        viewModelScope.launch {
            val payment = MaintenancePayment(
                id = "",
                houseId = house?.id ?: "house_${System.currentTimeMillis()}",
                ownerUid = user?.uid ?: "mock_owner_123",
                ownerName = user?.name ?: "House Owner",
                houseNumber = houseNum,
                monthYear = currentState.selectedMonthYear,
                amount = amount,
                paymentMethod = currentState.selectedPaymentMethod,
                transactionRef = ref,
                status = PaymentStatus.PENDING,
                submittedAt = System.currentTimeMillis()
            )

            val result = repository.submitPayment(payment)
            result.onSuccess { submitted ->
                _uiState.update {
                    it.copy(
                        isSubmittingPayment = false,
                        transactionRef = "",
                        paymentSubmitSuccess = "Payment of ₹${submitted.amount.toInt()} for ${submitted.monthYear} submitted successfully!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmittingPayment = false,
                        paymentSubmitError = error.localizedMessage ?: "Failed to submit payment"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                houseSaveSuccess = null,
                houseSaveError = null,
                paymentSubmitSuccess = null,
                paymentSubmitError = null
            )
        }
    }

    class Factory(private val repository: SanSiriRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HouseOwnerViewModel(repository) as T
        }
    }
}
