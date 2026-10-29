package com.anil.sansiri.data.repository

import android.app.Activity
import com.anil.sansiri.data.model.Announcement
import com.anil.sansiri.data.model.AppNotification
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.ComplaintItem
import com.anil.sansiri.data.model.FinancialTransaction
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.ItemStatus
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.MonthlyHouseStatus
import com.anil.sansiri.data.model.NotificationType
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.RequestComment
import com.anil.sansiri.data.model.RequestItem
import com.anil.sansiri.data.model.RequestStatus
import com.anil.sansiri.data.model.ResidentRequest
import com.anil.sansiri.data.model.SocietyContact
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface SanSiriRepository {
    // Authentication & User Management
    fun signInWithPhone(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    
    suspend fun signInWithPhoneAndPassword(phoneNumber: String, password: String): Result<User>
    
    fun sendOtpForRegistration(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )
    
    suspend fun verifyOtpAndCreateUser(
        verificationId: String,
        code: String,
        phoneNumber: String,
        password: String,
        name: String
    ): Result<User>
    
    suspend fun adminRegisterResident(
        name: String,
        phoneNumber: String,
        occupancyType: OccupancyType
    ): Result<Unit>

    suspend fun adminRegisterUser(
        name: String,
        phoneNumber: String,
        flatNumber: String,
        crossStreet: String,
        occupancyType: OccupancyType,
        role: UserRole
    ): Result<User>
    
    fun getCurrentUser(): User?
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun logout()
    suspend fun updateUserRole(targetUid: String, newRole: UserRole): Result<Unit>
    
    suspend fun updateUserRoleAndBuildingDetails(
        targetUid: String,
        name: String,
        houseNumber: String,
        cross: String,
        occupancyType: OccupancyType,
        role: UserRole
    ): Result<Unit>

    suspend fun setUserActiveStatus(userUid: String, isActive: Boolean): Result<Unit>

    fun getAllUsers(): Flow<List<User>>
    fun getAdmins(): Flow<List<User>>
    fun getAdminUsers(): Flow<List<User>> = getAdmins()
    suspend fun addAdmin(phoneNumber: String, name: String): Result<User>
    suspend fun quickDemoLogin(
        role: UserRole = UserRole.USER,
        phoneNumber: String = "+919876543210",
        name: String = "Demo User"
    ): Result<User>

    // House & Property details CRUD
    suspend fun saveHouseDetail(houseDetail: HouseDetail): Result<HouseDetail>
    suspend fun saveHousesForUser(ownerUid: String, houses: List<HouseDetail>): Result<List<HouseDetail>>
    suspend fun deleteHouseDetail(houseId: String): Result<Unit>
    fun getHouseDetailByOwner(ownerUid: String): Flow<HouseDetail?>
    fun getHousesByOwner(ownerUid: String): Flow<List<HouseDetail>>
    suspend fun addPropertyForUser(
        ownerUid: String,
        houseNumber: String,
        crossStreet: String,
        occupancyType: OccupancyType,
        propertyType: PropertyType = PropertyType.INDIVIDUAL_HOUSE
    ): Result<HouseDetail>
    fun getApprovedHouses(): Flow<List<HouseDetail>>
    fun getPendingHouses(): Flow<List<HouseDetail>>
    fun getAllHouses(): Flow<List<HouseDetail>>
    suspend fun updateHouseStatus(houseId: String, status: ApprovalStatus): Result<Unit>

    // Announcements
    fun getAnnouncements(): Flow<List<Announcement>>
    suspend fun createAnnouncement(announcement: Announcement): Result<Announcement>
    suspend fun deleteAnnouncement(announcementId: String): Result<Unit>

    // Resident Requests & Concerns
    fun getResidentRequests(): Flow<List<ResidentRequest>>
    suspend fun createResidentRequest(request: ResidentRequest): Result<ResidentRequest>
    suspend fun updateResidentRequestStatus(requestId: String, status: RequestStatus): Result<Unit>

    // Requests (Legacy / General)
    fun getRequests(): Flow<List<RequestItem>>
    suspend fun createRequest(request: RequestItem): Result<RequestItem>
    suspend fun updateRequestStatus(requestId: String, status: ItemStatus): Result<Unit>

    // Complaints
    fun getComplaints(): Flow<List<ComplaintItem>>
    suspend fun createComplaint(complaint: ComplaintItem): Result<ComplaintItem>
    suspend fun updateComplaintStatus(complaintId: String, status: ItemStatus): Result<Unit>

    // Pricing rules & Auto Maintenance Fee Calculation set by Super Admin
    fun getPricingRules(): Flow<PricingRules>
    suspend fun updatePricingRules(rules: PricingRules): Result<Unit>

    fun calculateMaintenanceFee(houseDetail: HouseDetail?, rules: PricingRules): Double {
        if (houseDetail == null) return rules.selfOccupiedMaintenanceRate

        if (houseDetail.isApartment || houseDetail.propertyType == PropertyType.APARTMENT) {
            return rules.selfOccupiedMaintenanceRate
        }

        if (houseDetail.occupancyType == OccupancyType.EMPTY_SITE || houseDetail.propertyType == PropertyType.EMPTY_SITE) {
            return rules.emptySiteMaintenanceRate
        }

        var totalFee = 0.0
        val isSelf = houseDetail.isSelfOccupied || (houseDetail.occupancyType == OccupancyType.SELF_OCCUPIED && !houseDetail.isRented) || houseDetail.propertyType == PropertyType.INDIVIDUAL_HOUSE
        val isRent = houseDetail.isRented || houseDetail.occupancyType == OccupancyType.RENTED || houseDetail.propertyType == PropertyType.HOUSE_WITH_RENTED_HOMES

        if (isSelf) {
            totalFee += rules.selfOccupiedMaintenanceRate
        }

        if (isRent) {
            val rentedCount = if (houseDetail.numberOfRentedUnits > 0) {
                houseDetail.numberOfRentedUnits
            } else {
                maxOf(1, houseDetail.numberOfRentedHouses)
            }
            totalFee += (rentedCount * rules.rentedMaintenanceRate)
        }

        if (!isSelf && !isRent) {
            return rules.selfOccupiedMaintenanceRate
        }

        return totalFee
    }

    fun calculateMaintenanceFee(occupancyType: OccupancyType, rules: PricingRules): Double {
        return when (occupancyType) {
            OccupancyType.SELF_OCCUPIED -> rules.selfOccupiedMaintenanceRate
            OccupancyType.RENTED -> rules.rentedMaintenanceRate
            OccupancyType.EMPTY_SITE -> rules.emptySiteMaintenanceRate
        }
    }

    // Payments & 4-Month Status
    fun get4MonthsPaymentStatus(): Flow<List<MonthlyHouseStatus>>
    fun getUnpaidMonthsForUser(userUid: String): Flow<List<String>>
    suspend fun approveMonthlyStatus(houseId: String, monthYear: String): Result<Unit>
    suspend fun submitMultiMonthPayment(
        userUid: String,
        houseId: String,
        selectedMonths: List<String>,
        transactionRef: String,
        totalAmount: Double
    ): Result<Unit>

    suspend fun submitPayment(payment: MaintenancePayment): Result<MaintenancePayment>
    fun getPaymentsByOwner(ownerUid: String): Flow<List<MaintenancePayment>>
    fun getPendingPayments(): Flow<List<MaintenancePayment>>
    fun getAllPayments(): Flow<List<MaintenancePayment>>
    suspend fun approvePayment(paymentId: String): Result<Unit>
    suspend fun rejectPayment(paymentId: String): Result<Unit>

    // Financial Transactions & Balance Sheet
    fun getFinancialTransactions(): Flow<List<FinancialTransaction>>
    suspend fun addFinancialTransaction(transaction: FinancialTransaction): Result<FinancialTransaction>

    // Cross Representatives
    fun getCrossRepresentatives(): Flow<Map<String, String>>
    suspend fun assignCrossRepresentative(cross: String, userUid: String): Result<Unit>

    // Interactive Request Comments
    fun getRequestComments(): Flow<List<RequestComment>>
    suspend fun addRequestComment(comment: RequestComment): Result<RequestComment>

    // Society Contacts
    fun getSocietyContacts(): Flow<List<SocietyContact>>
    suspend fun addSocietyContact(contact: SocietyContact): Result<SocietyContact>
    suspend fun updateSocietyContact(contact: SocietyContact): Result<SocietyContact>
    suspend fun deleteSocietyContact(contactId: String): Result<Unit>

    // Community Notifications
    fun getNotificationsFlow(): Flow<List<AppNotification>>
    suspend fun publishNotification(
        title: String,
        message: String,
        type: NotificationType,
        targetUid: String = "ALL"
    ): Result<AppNotification>
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit>
    suspend fun markAllNotificationsAsRead(): Result<Unit>
    suspend fun clearNotifications(): Result<Unit>
}
