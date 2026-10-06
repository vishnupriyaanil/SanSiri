package com.anil.sansiri.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
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

data class AdminUiState(
    val currentUser: User? = null,
    val pendingHouses: List<HouseDetail> = emptyList(),
    val approvedHouses: List<HouseDetail> = emptyList(),
    val allHouses: List<HouseDetail> = emptyList(),
    val pendingPayments: List<MaintenancePayment> = emptyList(),
    val allPayments: List<MaintenancePayment> = emptyList(),
    val pricingRules: PricingRules = PricingRules(),

    // Pricing rule configuration inputs
    val selfOccupiedRateInput: String = "1500",
    val rentedRateInput: String = "2000",

    // Admin management inputs
    val adminsList: List<User> = emptyList(),
    val newAdminPhone: String = "",
    val newAdminName: String = "",

    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val totalCollected: Double
        get() = allPayments.sumOf { if (it.status == PaymentStatus.APPROVED) it.amount else 0.0 }

    val pendingPaymentsCount: Int
        get() = pendingPayments.size

    val pendingApprovalAmount: Double
        get() = pendingPayments.sumOf { it.amount }

    val approvedPaymentsCount: Int
        get() = allPayments.count { it.status == PaymentStatus.APPROVED }

    val totalApprovedHousesCount: Int
        get() = approvedHouses.size

    val pendingHousesCount: Int
        get() = pendingHouses.size
}

class AdminViewModel(private val repository: SanSiriRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collectLatest { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }

        viewModelScope.launch {
            repository.getPendingHouses().collectLatest { pending ->
                _uiState.update { it.copy(pendingHouses = pending) }
            }
        }

        viewModelScope.launch {
            repository.getApprovedHouses().collectLatest { approved ->
                _uiState.update { it.copy(approvedHouses = approved) }
            }
        }

        viewModelScope.launch {
            repository.getAllHouses().collectLatest { houses ->
                _uiState.update { it.copy(allHouses = houses) }
            }
        }

        viewModelScope.launch {
            repository.getPendingPayments().collectLatest { pending ->
                _uiState.update { it.copy(pendingPayments = pending) }
            }
        }

        viewModelScope.launch {
            repository.getAllPayments().collectLatest { payments ->
                _uiState.update { it.copy(allPayments = payments) }
            }
        }

        viewModelScope.launch {
            repository.getAdmins().collectLatest { admins ->
                _uiState.update { it.copy(adminsList = admins) }
            }
        }

        viewModelScope.launch {
            repository.getPricingRules().collectLatest { rules ->
                _uiState.update { state ->
                    state.copy(
                        pricingRules = rules,
                        selfOccupiedRateInput = rules.selfOccupiedMaintenanceRate.toInt().toString(),
                        rentedRateInput = rules.rentedMaintenanceRate.toInt().toString()
                    )
                }
            }
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun approveHouse(houseId: String) {
        viewModelScope.launch {
            val result = repository.updateHouseStatus(houseId, ApprovalStatus.APPROVED)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = "House registration approved successfully",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to approve house"
                    )
                }
            }
        }
    }

    fun rejectHouse(houseId: String) {
        viewModelScope.launch {
            val result = repository.updateHouseStatus(houseId, ApprovalStatus.REJECTED)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = "House registration rejected",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to reject house"
                    )
                }
            }
        }
    }

    fun onSelfOccupiedRateChanged(rate: String) {
        _uiState.update { it.copy(selfOccupiedRateInput = rate, errorMessage = null) }
    }

    fun onRentedRateChanged(rate: String) {
        _uiState.update { it.copy(rentedRateInput = rate, errorMessage = null) }
    }

    fun savePricingRules() {
        val selfRate = _uiState.value.selfOccupiedRateInput.toDoubleOrNull()
        val rentedRate = _uiState.value.rentedRateInput.toDoubleOrNull()

        if (selfRate == null || selfRate <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid rate for Self-Occupied house") }
            return
        }

        if (rentedRate == null || rentedRate <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid rate for Rented house") }
            return
        }

        viewModelScope.launch {
            val newRules = PricingRules(
                selfOccupiedMaintenanceRate = selfRate,
                rentedMaintenanceRate = rentedRate,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.updatePricingRules(newRules)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = "Pricing rules updated successfully (Self-Occupied: ₹${selfRate.toInt()}, Rented: ₹${rentedRate.toInt()})",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to update pricing rules"
                    )
                }
            }
        }
    }

    fun approvePayment(paymentId: String) {
        viewModelScope.launch {
            val result = repository.approvePayment(paymentId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = "Payment approved and marked as PAID",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to approve payment"
                    )
                }
            }
        }
    }

    fun rejectPayment(paymentId: String) {
        viewModelScope.launch {
            val result = repository.rejectPayment(paymentId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = "Payment rejected",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to reject payment"
                    )
                }
            }
        }
    }

    fun onNewAdminPhoneChanged(phone: String) {
        _uiState.update { it.copy(newAdminPhone = phone, errorMessage = null) }
    }

    fun onNewAdminNameChanged(name: String) {
        _uiState.update { it.copy(newAdminName = name, errorMessage = null) }
    }

    fun addAdmin() {
        val phone = _uiState.value.newAdminPhone.trim()
        val name = _uiState.value.newAdminName.trim()

        if (phone.length < 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit phone number") }
            return
        }

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter admin name") }
            return
        }

        viewModelScope.launch {
            val result = repository.addAdmin(phone, name)
            result.onSuccess { admin ->
                _uiState.update {
                    it.copy(
                        newAdminPhone = "",
                        newAdminName = "",
                        statusMessage = "Admin '${admin.name}' added successfully",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to add admin"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    class Factory(private val repository: SanSiriRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(repository) as T
        }
    }
}
