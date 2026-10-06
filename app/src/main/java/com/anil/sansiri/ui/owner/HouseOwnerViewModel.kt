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

    // House registration form fields
    val buildingName: String = "San Siri Residency",
    val houseNumber: String = "",
    val crossStreet: String = "",
    val occupancyType: OccupancyType = OccupancyType.SELF_OCCUPIED,
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
            val type = houseDetail?.occupancyType ?: occupancyType
            return if (type == OccupancyType.SELF_OCCUPIED) {
                pricingRules.selfOccupiedMaintenanceRate
            } else {
                pricingRules.rentedMaintenanceRate
            }
        }

    val registrationStatusMessage: String
        get() = when (houseDetail?.approvalStatus) {
            ApprovalStatus.PENDING -> "Your flat registration (${houseDetail.houseNumber}) is pending Admin approval."
            ApprovalStatus.APPROVED -> "Flat ${houseDetail.houseNumber} registered and approved."
            ApprovalStatus.REJECTED -> "Your flat registration (${houseDetail.houseNumber}) was rejected by Admin."
            null -> if (houseNumber.isNotBlank()) "Your flat details ($houseNumber) are not submitted yet." else "Please register your flat details to access all residency features."
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
                        buildingName = house?.buildingName ?: state.buildingName,
                        houseNumber = house?.houseNumber ?: state.houseNumber,
                        crossStreet = house?.crossStreet ?: state.crossStreet,
                        occupancyType = house?.occupancyType ?: state.occupancyType
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

    fun onBuildingNameChanged(name: String) {
        _uiState.update { it.copy(buildingName = name, houseSaveError = null) }
    }

    fun onHouseNumberChanged(number: String) {
        _uiState.update { it.copy(houseNumber = number, houseSaveError = null) }
    }

    fun onCrossStreetChanged(street: String) {
        _uiState.update { it.copy(crossStreet = street, houseSaveError = null) }
    }

    fun onOccupancyTypeChanged(type: OccupancyType) {
        _uiState.update { it.copy(occupancyType = type, houseSaveError = null) }
    }

    fun saveHouseDetail() {
        val currentState = _uiState.value
        val building = currentState.buildingName.trim()
        val houseNum = currentState.houseNumber.trim()
        val street = currentState.crossStreet.trim()

        if (houseNum.isBlank()) {
            _uiState.update { it.copy(houseSaveError = "Please enter house/flat number (e.g. A-101)") }
            return
        }

        _uiState.update { it.copy(isSavingHouse = true, houseSaveError = null, houseSaveSuccess = null) }

        viewModelScope.launch {
            val user = currentState.currentUser
            val existing = currentState.houseDetail
            val updatedDetail = HouseDetail(
                id = existing?.id ?: "",
                ownerUid = user?.uid ?: "mock_owner_123",
                ownerName = user?.name ?: "Anil Kumar",
                ownerPhone = user?.phoneNumber ?: "+919876543210",
                buildingName = if (building.isBlank()) "San Siri Residency" else building,
                houseNumber = houseNum,
                crossStreet = street,
                occupancyType = currentState.occupancyType,
                approvalStatus = existing?.approvalStatus ?: ApprovalStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )

            val result = repository.saveHouseDetail(updatedDetail)
            result.onSuccess { saved ->
                _uiState.update {
                    it.copy(
                        isSavingHouse = false,
                        houseDetail = saved,
                        houseSaveSuccess = "House details saved successfully. Registration status: ${saved.approvalStatus.name}"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSavingHouse = false,
                        houseSaveError = error.localizedMessage ?: "Failed to save house details"
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
        val houseNum = house?.houseNumber ?: currentState.houseNumber

        if (houseNum.isBlank()) {
            _uiState.update { it.copy(paymentSubmitError = "Please enter house details before submitting payment") }
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
                ownerName = user?.name ?: "Anil Kumar",
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

    fun calculateFeeForOccupancy(type: OccupancyType): Double {
        return repository.calculateMaintenanceFee(type, _uiState.value.pricingRules)
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
