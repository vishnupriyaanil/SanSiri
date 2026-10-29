package com.anil.sansiri.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anil.sansiri.data.model.Announcement
import com.anil.sansiri.data.model.AppNotification
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.ComplaintItem
import com.anil.sansiri.data.model.FinancialTransaction
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.ItemStatus
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.MonthlyHouseStatus
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.RequestComment
import com.anil.sansiri.data.model.RequestItem
import com.anil.sansiri.data.model.RequestStatus
import com.anil.sansiri.data.model.ResidentRequest
import com.anil.sansiri.data.model.SocietyContact
import com.anil.sansiri.data.model.TransactionType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.data.repository.SanSiriRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentUser: User? = null,
    val selectedNavRoute: String = "ANNOUNCEMENTS", // "ANNOUNCEMENTS", "REQUESTS", "PAYMENTS", "CONCERNS", "BALANCE_SHEET", "PEOPLES", "USER_MANAGEMENT"

    val announcements: List<Announcement> = emptyList(),
    val residentRequests: List<ResidentRequest> = emptyList(),
    val monthlyPaymentStatuses: List<MonthlyHouseStatus> = emptyList(),
    val unpaidMonths: List<String> = emptyList(),

    val financialTransactions: List<FinancialTransaction> = emptyList(),
    val crossRepresentatives: Map<String, String> = emptyMap(),
    val requestComments: List<RequestComment> = emptyList(),
    val societyContacts: List<SocietyContact> = emptyList(),

    val requests: List<RequestItem> = emptyList(),
    val complaints: List<ComplaintItem> = emptyList(),
    val pendingPayments: List<MaintenancePayment> = emptyList(),
    val myPayments: List<MaintenancePayment> = emptyList(),
    val allPayments: List<MaintenancePayment> = emptyList(),
    val myHouseDetail: HouseDetail? = null,
    val userHouses: List<HouseDetail> = emptyList(),
    val pendingHouses: List<HouseDetail> = emptyList(),
    val approvedHouses: List<HouseDetail> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val pricingRules: PricingRules = PricingRules(),

    // Pricing rates configuration inputs
    val selfOccupiedRateInput: String = "1500",
    val rentedRateInput: String = "2000",
    val emptySiteRateInput: String = "500",

    // UI Dialog & BottomSheet Controls
    val showCreateAnnouncementDialog: Boolean = false,
    val showCreateResidentRequestDialog: Boolean = false,
    val showCreateRequestDialog: Boolean = false,
    val showCreateConcernDialog: Boolean = false,
    val showCreateComplaintDialog: Boolean = false,
    val showPayMaintenanceDialog: Boolean = false,
    val showPropertyRegistrationDialog: Boolean = false,
    val showUpdatePropertyDialog: Boolean = false,
    val showUserManagementDialog: Boolean = false,
    val showPropertyApprovalsDialog: Boolean = false,
    val showPricingRulesDialog: Boolean = false,
    val showNotificationCenterDialog: Boolean = false,

    val notifications: List<AppNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,

    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class HomeViewModel(private val repository: SanSiriRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collectLatest { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    observeUserSpecificData(user.uid)
                }
            }
        }

        viewModelScope.launch {
            repository.getAnnouncements().collectLatest { list ->
                _uiState.update { it.copy(announcements = list) }
            }
        }

        viewModelScope.launch {
            repository.getResidentRequests().collectLatest { list ->
                _uiState.update { it.copy(residentRequests = list) }
            }
        }

        viewModelScope.launch {
            repository.get4MonthsPaymentStatus().collectLatest { list ->
                _uiState.update { it.copy(monthlyPaymentStatuses = list) }
            }
        }

        viewModelScope.launch {
            repository.getRequests().collectLatest { list ->
                _uiState.update { it.copy(requests = list) }
            }
        }

        viewModelScope.launch {
            repository.getComplaints().collectLatest { list ->
                _uiState.update { it.copy(complaints = list) }
            }
        }

        viewModelScope.launch {
            repository.getPendingPayments().collectLatest { list ->
                _uiState.update { it.copy(pendingPayments = list) }
            }
        }

        viewModelScope.launch {
            repository.getAllPayments().collectLatest { list ->
                _uiState.update { it.copy(allPayments = list) }
            }
        }

        viewModelScope.launch {
            repository.getPendingHouses().collectLatest { list ->
                _uiState.update { it.copy(pendingHouses = list) }
            }
        }

        viewModelScope.launch {
            repository.getApprovedHouses().collectLatest { list ->
                _uiState.update { it.copy(approvedHouses = list) }
            }
        }

        viewModelScope.launch {
            repository.getAllUsers().collectLatest { list ->
                _uiState.update { it.copy(allUsers = list) }
            }
        }

        viewModelScope.launch {
            repository.getPricingRules().collectLatest { rules ->
                _uiState.update { state ->
                    state.copy(
                        pricingRules = rules,
                        selfOccupiedRateInput = rules.selfOccupiedMaintenanceRate.toInt().toString(),
                        rentedRateInput = rules.rentedMaintenanceRate.toInt().toString(),
                        emptySiteRateInput = rules.emptySiteMaintenanceRate.toInt().toString()
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getFinancialTransactions().collectLatest { list ->
                _uiState.update { it.copy(financialTransactions = list) }
            }
        }

        viewModelScope.launch {
            repository.getCrossRepresentatives().collectLatest { map ->
                _uiState.update { it.copy(crossRepresentatives = map) }
            }
        }

        viewModelScope.launch {
            repository.getRequestComments().collectLatest { comments ->
                _uiState.update { it.copy(requestComments = comments) }
            }
        }

        viewModelScope.launch {
            repository.getSocietyContacts().collectLatest { list ->
                _uiState.update { it.copy(societyContacts = list) }
            }
        }

        viewModelScope.launch {
            repository.getNotificationsFlow().collectLatest { list ->
                val unread = list.count { !it.isRead }
                _uiState.update { it.copy(notifications = list, unreadNotificationCount = unread) }
            }
        }
    }

    private fun observeUserSpecificData(ownerUid: String) {
        viewModelScope.launch {
            repository.getHouseDetailByOwner(ownerUid).collectLatest { house ->
                _uiState.update { it.copy(myHouseDetail = house) }
            }
        }

        viewModelScope.launch {
            repository.getHousesByOwner(ownerUid).collectLatest { houses ->
                _uiState.update { it.copy(userHouses = houses) }
            }
        }

        viewModelScope.launch {
            repository.getPaymentsByOwner(ownerUid).collectLatest { list ->
                _uiState.update { it.copy(myPayments = list) }
            }
        }

        viewModelScope.launch {
            repository.getUnpaidMonthsForUser(ownerUid).collectLatest { unpaidList ->
                _uiState.update { it.copy(unpaidMonths = unpaidList) }
            }
        }
    }

    fun setSelectedNavRoute(route: String) {
        _uiState.update { it.copy(selectedNavRoute = route) }
    }

    // Dialog toggles
    fun setShowCreateAnnouncementDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateAnnouncementDialog = show) }
    }

    fun setShowCreateResidentRequestDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateResidentRequestDialog = show) }
    }

    fun setShowCreateRequestDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateRequestDialog = show) }
    }

    fun setShowCreateConcernDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateConcernDialog = show) }
    }

    fun setShowCreateComplaintDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateComplaintDialog = show) }
    }

    fun setShowPayMaintenanceDialog(show: Boolean) {
        _uiState.update { it.copy(showPayMaintenanceDialog = show) }
    }

    fun setShowPropertyRegistrationDialog(show: Boolean) {
        _uiState.update { it.copy(showPropertyRegistrationDialog = show) }
    }

    fun setShowUpdatePropertyDialog(show: Boolean) {
        _uiState.update { it.copy(showUpdatePropertyDialog = show) }
    }

    fun setShowUserManagementDialog(show: Boolean) {
        _uiState.update { it.copy(showUserManagementDialog = show) }
    }

    fun setShowPropertyApprovalsDialog(show: Boolean) {
        _uiState.update { it.copy(showPropertyApprovalsDialog = show) }
    }

    fun setShowPricingRulesDialog(show: Boolean) {
        _uiState.update { it.copy(showPricingRulesDialog = show) }
    }

    fun setShowNotificationCenterDialog(show: Boolean) {
        _uiState.update { it.copy(showNotificationCenterDialog = show) }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // Announcements
    fun createAnnouncement(title: String, content: String) {
        val user = _uiState.value.currentUser ?: return
        if (title.isBlank() || content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter title and content") }
            return
        }

        viewModelScope.launch {
            val announcement = Announcement(
                title = title.trim(),
                content = content.trim(),
                postedByName = user.name.ifBlank { "San Siri Admin" },
                authorUid = user.uid,
                authorRole = user.role,
                createdAt = System.currentTimeMillis()
            )
            repository.createAnnouncement(announcement)
            _uiState.update {
                it.copy(
                    showCreateAnnouncementDialog = false,
                    statusMessage = "Announcement posted successfully!"
                )
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            _uiState.update { it.copy(statusMessage = "Announcement deleted") }
        }
    }

    // Resident Requests / Concerns
    fun createResidentRequest(title: String, description: String, category: String = "REQUEST") {
        val user = _uiState.value.currentUser ?: return
        val house = _uiState.value.myHouseDetail
        if (title.isBlank() || description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in title and description") }
            return
        }

        viewModelScope.launch {
            val req = ResidentRequest(
                userUid = user.uid,
                userName = user.name.ifBlank { "Resident" },
                flatNumber = house?.displayHouseOrSiteNumber ?: "A-101",
                title = title.trim(),
                description = description.trim(),
                category = category,
                status = RequestStatus.OPEN,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.createResidentRequest(req)
            val msgLabel = if (category == "CONCERN") "Concern" else "Request"
            _uiState.update {
                it.copy(
                    showCreateResidentRequestDialog = false,
                    showCreateRequestDialog = false,
                    showCreateConcernDialog = false,
                    statusMessage = "$msgLabel raised successfully!"
                )
            }
        }
    }

    fun updateResidentRequestStatus(requestId: String, status: RequestStatus) {
        viewModelScope.launch {
            repository.updateResidentRequestStatus(requestId, status)
            _uiState.update { it.copy(statusMessage = "Request status updated to ${status.name}") }
        }
    }

    // Multi-Select Maintenance Payment
    fun submitMultiMonthPayment(selectedMonths: List<String>, transactionRef: String, totalAmount: Double) {
        val user = _uiState.value.currentUser ?: return
        val house = _uiState.value.myHouseDetail
        if (selectedMonths.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one month to pay") }
            return
        }
        if (transactionRef.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter Transaction Reference ID") }
            return
        }

        viewModelScope.launch {
            val houseId = house?.id ?: "house_101"
            repository.submitMultiMonthPayment(user.uid, houseId, selectedMonths, transactionRef.trim(), totalAmount)
            _uiState.update {
                it.copy(
                    selectedNavRoute = "HOME",
                    statusMessage = "Payment submitted successfully for ${selectedMonths.size} month(s)!"
                )
            }
        }
    }

    // User & Role Alignment
    fun updateUserRoleAndBuildingDetails(
        targetUid: String,
        name: String,
        houseNumber: String,
        cross: String,
        occupancyType: OccupancyType,
        role: UserRole
    ) {
        viewModelScope.launch {
            repository.updateUserRoleAndBuildingDetails(targetUid, name, houseNumber, cross, occupancyType, role)
            _uiState.update { it.copy(statusMessage = "User details and role updated successfully!") }
        }
    }

    // Legacy Requests & Complaints
    fun createRequest(title: String, description: String, category: String) {
        val user = _uiState.value.currentUser ?: return
        val house = _uiState.value.myHouseDetail
        if (title.isBlank() || description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter title and description") }
            return
        }

        viewModelScope.launch {
            val req = RequestItem(
                title = title.trim(),
                description = description.trim(),
                category = category.ifBlank { "General" },
                userUid = user.uid,
                userName = user.name.ifBlank { "Resident" },
                userPhone = user.phoneNumber,
                houseNumber = house?.displayHouseOrSiteNumber ?: "Unregistered",
                status = ItemStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
            repository.createRequest(req)
            _uiState.update {
                it.copy(
                    showCreateRequestDialog = false,
                    statusMessage = "Request raised successfully"
                )
            }
        }
    }

    fun updateRequestStatus(id: String, status: ItemStatus) {
        viewModelScope.launch {
            repository.updateRequestStatus(id, status)
            _uiState.update { it.copy(statusMessage = "Request status updated to ${status.name}") }
        }
    }

    fun createComplaint(title: String, description: String) {
        val user = _uiState.value.currentUser ?: return
        val house = _uiState.value.myHouseDetail
        if (title.isBlank() || description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter title and description") }
            return
        }

        viewModelScope.launch {
            val complaint = ComplaintItem(
                title = title.trim(),
                description = description.trim(),
                userUid = user.uid,
                userName = user.name.ifBlank { "Resident" },
                userPhone = user.phoneNumber,
                houseNumber = house?.displayHouseOrSiteNumber ?: "Unregistered",
                status = ItemStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
            repository.createComplaint(complaint)
            _uiState.update {
                it.copy(
                    showCreateComplaintDialog = false,
                    statusMessage = "Complaint raised successfully"
                )
            }
        }
    }

    fun updateComplaintStatus(id: String, status: ItemStatus) {
        viewModelScope.launch {
            repository.updateComplaintStatus(id, status)
            _uiState.update { it.copy(statusMessage = "Complaint status updated to ${status.name}") }
        }
    }

    fun submitPayment(monthYear: String, paymentMethod: String, transactionRef: String) {
        val user = _uiState.value.currentUser ?: return
        val house = _uiState.value.myHouseDetail
        val rules = _uiState.value.pricingRules
        val amount = repository.calculateMaintenanceFee(house, rules)

        if (transactionRef.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter Transaction Ref ID") }
            return
        }

        viewModelScope.launch {
            val payment = MaintenancePayment(
                houseId = house?.id ?: "house_${System.currentTimeMillis()}",
                ownerUid = user.uid,
                ownerName = user.name.ifBlank { "Resident" },
                houseNumber = house?.displayHouseOrSiteNumber ?: "General",
                monthYear = monthYear,
                amount = amount,
                paymentMethod = paymentMethod,
                transactionRef = transactionRef.trim(),
                status = PaymentStatus.PENDING,
                submittedAt = System.currentTimeMillis()
            )
            repository.submitPayment(payment)
            _uiState.update {
                it.copy(
                    showPayMaintenanceDialog = false,
                    statusMessage = "Maintenance payment of ₹${amount.toInt()} submitted for approval"
                )
            }
        }
    }

    fun approvePayment(paymentId: String) {
        viewModelScope.launch {
            repository.approvePayment(paymentId)
            _uiState.update { it.copy(statusMessage = "Payment approved") }
        }
    }

    fun approveMonthlyStatus(houseId: String, monthYear: String) {
        viewModelScope.launch {
            repository.approveMonthlyStatus(houseId, monthYear)
            _uiState.update { it.copy(statusMessage = "Payment status updated to APPROVED") }
        }
    }

    fun addPropertyForUser(
        ownerUid: String,
        houseNumber: String,
        crossStreet: String,
        occupancyType: OccupancyType,
        propertyType: PropertyType = PropertyType.INDIVIDUAL_HOUSE
    ) {
        viewModelScope.launch {
            repository.addPropertyForUser(ownerUid, houseNumber, crossStreet, occupancyType, propertyType)
            _uiState.update { it.copy(statusMessage = "Property added successfully") }
        }
    }

    fun rejectPayment(paymentId: String) {
        viewModelScope.launch {
            repository.rejectPayment(paymentId)
            _uiState.update { it.copy(statusMessage = "Payment rejected") }
        }
    }

    fun updateUserRole(targetUid: String, newRole: UserRole) {
        viewModelScope.launch {
            repository.updateUserRole(targetUid, newRole)
            _uiState.update { it.copy(statusMessage = "User role updated to ${newRole.name}") }
        }
    }

    fun addAdmin(phoneNumber: String, name: String) {
        if (phoneNumber.length < 10) {
            _uiState.update { it.copy(errorMessage = "Enter valid 10-digit phone number") }
            return
        }
        viewModelScope.launch {
            repository.addAdmin(phoneNumber, name)
            _uiState.update { it.copy(statusMessage = "Admin added successfully") }
        }
    }

    fun saveHouseDetail(
        propertyType: PropertyType,
        siteNumber: String,
        buildingName: String,
        buildingNumber: String,
        floorNumber: String,
        houseNumber: String,
        numberOfRentedHouses: Int,
        crossStreet: String,
        occupancyType: OccupancyType
    ) {
        val user = _uiState.value.currentUser ?: return
        val existing = _uiState.value.myHouseDetail

        viewModelScope.launch {
            val detail = HouseDetail(
                id = existing?.id ?: "",
                ownerUid = user.uid,
                ownerName = user.name.ifBlank { "Resident" },
                ownerPhone = user.phoneNumber,
                propertyType = propertyType,
                siteNumber = siteNumber.trim(),
                buildingName = buildingName.ifBlank { "San Siri Residency" },
                buildingNumber = buildingNumber.trim(),
                floorNumber = floorNumber.trim(),
                houseNumber = houseNumber.trim(),
                numberOfRentedHouses = numberOfRentedHouses,
                crossStreet = crossStreet.trim(),
                occupancyType = occupancyType,
                approvalStatus = existing?.approvalStatus ?: ApprovalStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveHouseDetail(detail)
            _uiState.update {
                it.copy(
                    showPropertyRegistrationDialog = false,
                    statusMessage = "Property details saved successfully!"
                )
            }
        }
    }

    fun saveHousesForUser(houses: List<HouseDetail>) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.saveHousesForUser(user.uid, houses)
            _uiState.update {
                it.copy(
                    showUpdatePropertyDialog = false,
                    showPropertyRegistrationDialog = false,
                    statusMessage = "All property details saved successfully!"
                )
            }
        }
    }

    fun approveHouse(houseId: String) {
        viewModelScope.launch {
            repository.updateHouseStatus(houseId, ApprovalStatus.APPROVED)
            _uiState.update { it.copy(statusMessage = "Property registration approved") }
        }
    }

    fun rejectHouse(houseId: String) {
        viewModelScope.launch {
            repository.updateHouseStatus(houseId, ApprovalStatus.REJECTED)
            _uiState.update { it.copy(statusMessage = "Property registration rejected") }
        }
    }

    fun onSelfOccupiedRateChanged(rate: String) {
        _uiState.update { it.copy(selfOccupiedRateInput = rate, errorMessage = null) }
    }

    fun onRentedRateChanged(rate: String) {
        _uiState.update { it.copy(rentedRateInput = rate, errorMessage = null) }
    }

    fun onEmptySiteRateChanged(rate: String) {
        _uiState.update { it.copy(emptySiteRateInput = rate, errorMessage = null) }
    }

    fun savePricingRules() {
        val selfRate = _uiState.value.selfOccupiedRateInput.toDoubleOrNull()
        val rentedRate = _uiState.value.rentedRateInput.toDoubleOrNull()
        val emptySiteRate = _uiState.value.emptySiteRateInput.toDoubleOrNull()

        if (selfRate == null || selfRate <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid rate for Self-Occupied house") }
            return
        }

        if (rentedRate == null || rentedRate <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid rate for Rented house") }
            return
        }

        if (emptySiteRate == null || emptySiteRate <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid rate for Empty Site") }
            return
        }

        viewModelScope.launch {
            val newRules = PricingRules(
                individualHouseRate = selfRate,
                rentedHouseRate = rentedRate,
                selfOccupiedMaintenanceRate = selfRate,
                rentedMaintenanceRate = rentedRate,
                emptySiteMaintenanceRate = emptySiteRate,
                emptySiteRate = emptySiteRate,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.updatePricingRules(newRules)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = "Pricing rules saved successfully (Self-Occupied: ₹${selfRate.toInt()}, Rented: ₹${rentedRate.toInt()}, Empty Site: ₹${emptySiteRate.toInt()})",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to save pricing rules"
                    )
                }
            }
        }
    }

    fun savePricingRules(individualRate: Double, rentedRate: Double, apartmentRate: Double, emptySiteRate: Double) {
        viewModelScope.launch {
            val newRules = PricingRules(
                individualHouseRate = individualRate,
                rentedHouseRate = rentedRate,
                apartmentRate = apartmentRate,
                emptySiteRate = emptySiteRate,
                selfOccupiedMaintenanceRate = individualRate,
                rentedMaintenanceRate = rentedRate,
                emptySiteMaintenanceRate = emptySiteRate,
                updatedAt = System.currentTimeMillis()
            )
            repository.updatePricingRules(newRules)
            _uiState.update {
                it.copy(
                    showPricingRulesDialog = false,
                    statusMessage = "Super Admin maintenance rates updated successfully"
                )
            }
        }
    }

    fun adminRegisterUser(
        name: String,
        phoneNumber: String,
        flatNumber: String,
        crossStreet: String,
        occupancyType: OccupancyType,
        role: UserRole
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter user name") }
            return
        }
        if (phoneNumber.length != 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit phone number") }
            return
        }

        viewModelScope.launch {
            val result = repository.adminRegisterUser(
                name = name,
                phoneNumber = phoneNumber,
                flatNumber = flatNumber,
                crossStreet = crossStreet,
                occupancyType = occupancyType,
                role = role
            )
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        statusMessage = "User '${user.name}' added successfully (${role.name})!",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to add user"
                    )
                }
            }
        }
    }

    fun toggleUserActiveStatus(targetUid: String, isActive: Boolean) {
        viewModelScope.launch {
            val result = repository.setUserActiveStatus(targetUid, isActive)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        statusMessage = if (isActive) "User account activated" else "User account deactivated",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Failed to update user active status"
                    )
                }
            }
        }
    }

    // Financial Transactions & Balance Sheet
    fun addFinancialTransaction(
        type: TransactionType,
        amount: Double,
        category: String,
        description: String,
        dateMillis: Long
    ) {
        val user = _uiState.value.currentUser ?: return
        if (amount <= 0 || description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter valid amount and description") }
            return
        }

        viewModelScope.launch {
            val transaction = FinancialTransaction(
                type = type,
                amount = amount,
                category = category,
                description = description.trim(),
                dateMillis = dateMillis,
                addedBy = user.name.ifBlank { "Admin" }
            )
            repository.addFinancialTransaction(transaction)
            val typeStr = if (type == TransactionType.INCOME) "Income" else "Expense"
            _uiState.update { it.copy(statusMessage = "$typeStr entry of ₹${amount.toInt()} added successfully") }
        }
    }

    // Cross Representative Assignment
    fun assignCrossRepresentative(cross: String, userUid: String) {
        viewModelScope.launch {
            repository.assignCrossRepresentative(cross, userUid)
            _uiState.update { it.copy(statusMessage = "Representative assigned for $cross") }
        }
    }

    // Interactive Request Comments
    fun addRequestComment(requestId: String, commentText: String) {
        val user = _uiState.value.currentUser ?: return
        if (commentText.isBlank()) return

        viewModelScope.launch {
            val nameCandidate = user.name.trim()
            val commenterName = if (nameCandidate.isNotBlank() && !nameCandidate.equals("Resident", ignoreCase = true)) {
                nameCandidate
            } else if (user.phoneNumber.isNotBlank()) {
                user.phoneNumber
            } else {
                "Resident"
            }

            val comment = RequestComment(
                requestId = requestId,
                userUid = user.uid,
                userName = commenterName,
                userRole = user.role,
                commentText = commentText.trim(),
                createdAt = System.currentTimeMillis()
            )
            repository.addRequestComment(comment)
            _uiState.update { it.copy(statusMessage = "Comment posted successfully!") }
        }
    }

    // Society Contacts
    fun addSocietyContact(category: String, contactPersonName: String, phoneNumber: String) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser
            val addedBy = user?.name?.ifBlank { "Admin" } ?: "Admin"
            val contact = SocietyContact(
                category = category,
                contactPersonName = contactPersonName,
                phoneNumber = phoneNumber,
                addedBy = addedBy
            )
            val result = repository.addSocietyContact(contact)
            if (result.isSuccess) {
                _uiState.update { it.copy(statusMessage = "Contact added successfully!") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to add contact") }
            }
        }
    }

    fun deleteSocietyContact(contactId: String) {
        viewModelScope.launch {
            val result = repository.deleteSocietyContact(contactId)
            if (result.isSuccess) {
                _uiState.update { it.copy(statusMessage = "Contact deleted successfully!") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete contact") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    class Factory(private val repository: SanSiriRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
