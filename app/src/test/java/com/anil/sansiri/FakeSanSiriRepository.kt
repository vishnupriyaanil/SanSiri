package com.anil.sansiri

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
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FakeSanSiriRepository : SanSiriRepository {

    private val superAdmin = User(
        uid = "super_admin_9964532102",
        phoneNumber = "+919964532102",
        name = "San Siri Super Admin",
        role = UserRole.SUPER_ADMIN
    )

    private val currentUserFlow = MutableStateFlow<User?>(null)
    private val usersFlow = MutableStateFlow<List<User>>(initialFakeUsers())
    private val housesFlow = MutableStateFlow<List<HouseDetail>>(initialFakeHouses())
    private val announcementsFlow = MutableStateFlow<List<Announcement>>(
        listOf(
            Announcement(
                id = "ann_1",
                title = "Annual Society Meeting on Sunday",
                content = "Meeting in Community Hall at 10 AM.",
                postedByName = "San Siri Super Admin",
                authorUid = "super_admin_9964532102",
                authorRole = UserRole.SUPER_ADMIN
            )
        )
    )
    private val residentRequestsFlow = MutableStateFlow<List<ResidentRequest>>(
        listOf(
            ResidentRequest(
                id = "req_1",
                userUid = "owner_123",
                userName = "Anil Kumar",
                flatNumber = "A-101",
                title = "Elevator noise on 3rd floor",
                description = "Squeaking noise in elevator",
                status = RequestStatus.OPEN
            )
        )
    )
    private val requestsFlow = MutableStateFlow<List<RequestItem>>(emptyList())
    private val complaintsFlow = MutableStateFlow<List<ComplaintItem>>(emptyList())
    private val pricingRulesFlow = MutableStateFlow(
        PricingRules(
            individualHouseRate = 1500.0,
            rentedHouseRate = 2000.0,
            emptySiteRate = 500.0,
            selfOccupiedMaintenanceRate = 1500.0,
            rentedMaintenanceRate = 2000.0,
            emptySiteMaintenanceRate = 500.0
        )
    )
    private val paymentsFlow = MutableStateFlow<List<MaintenancePayment>>(emptyList())
    private val monthlyStatusesFlow = MutableStateFlow<List<MonthlyHouseStatus>>(initialFakeMonthlyStatuses())
    private val customFinancialTransactionsFlow = MutableStateFlow<List<FinancialTransaction>>(
        listOf(
            FinancialTransaction(
                id = "ft_1",
                type = TransactionType.EXPENSE,
                amount = 25000.0,
                category = "Security Salary",
                description = "Monthly Security Salary",
                dateMillis = System.currentTimeMillis() - 86400000L * 2,
                addedBy = "San Siri Admin"
            )
        )
    )
    private val crossRepresentativesFlow = MutableStateFlow<Map<String, String>>(initialFakeCrossReps())
    private val requestCommentsFlow = MutableStateFlow<List<RequestComment>>(
        listOf(
            RequestComment(
                id = "comment_1",
                requestId = "req_1",
                userUid = "admin_123",
                userName = "San Siri Admin",
                userRole = UserRole.ADMIN,
                commentText = "Technician scheduled for inspection.",
                createdAt = System.currentTimeMillis() - 3600000L
            )
        )
    )
    private val societyContactsFlow = MutableStateFlow<List<SocietyContact>>(
        listOf(
            SocietyContact(
                id = "contact_1",
                category = "Electrical Issues",
                contactPersonName = "Ramesh (Electrician)",
                phoneNumber = "9876500001",
                addedBy = "San Siri Admin"
            ),
            SocietyContact(
                id = "contact_2",
                category = "Garbage Issues",
                contactPersonName = "Suresh (Garbage Supervisor)",
                phoneNumber = "9876500002",
                addedBy = "San Siri Admin"
            ),
            SocietyContact(
                id = "contact_3",
                category = "Plumbing Services",
                contactPersonName = "Mahesh (Plumber)",
                phoneNumber = "9876500003",
                addedBy = "San Siri Admin"
            ),
            SocietyContact(
                id = "contact_4",
                category = "Security Desk",
                contactPersonName = "Main Gate Guard",
                phoneNumber = "9876500004",
                addedBy = "San Siri Admin"
            )
        )
    )
    private val notificationsFlow = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = "notif_welcome",
                title = "Welcome to San Siri Residency",
                message = "Stay updated with community announcements, requests, and concerns here.",
                type = NotificationType.GENERAL,
                createdAt = System.currentTimeMillis() - 3600000,
                isRead = false
            ),
            AppNotification(
                id = "notif_announcement_1",
                title = "New Announcement: Annual General Body Meeting",
                message = "Annual General Body Meeting scheduled for Sunday at 10:00 AM in the Community Hall.",
                type = NotificationType.ANNOUNCEMENT,
                createdAt = System.currentTimeMillis() - 7200000,
                isRead = true
            )
        )
    )

    private fun getRecent4Months(): List<String> {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.US)
        val calendar = Calendar.getInstance()
        val months = mutableListOf<String>()
        for (i in 0..3) {
            months.add(formatter.format(calendar.time))
            calendar.add(Calendar.MONTH, -1)
        }
        return months
    }

    private fun initialFakeMonthlyStatuses(): List<MonthlyHouseStatus> {
        val recentMonths = getRecent4Months()
        val m0 = recentMonths[0] // Current Month (Tab 1)
        val m1 = recentMonths[1] // 1 Month Ago (Tab 2)
        val m2 = recentMonths[2] // 2 Months Ago (Tab 3)
        val m3 = recentMonths[3] // 3 Months Ago (Tab 4)

        val flats = listOf(
            Triple("house_101", "A-101", "Anil Kumar"),
            Triple("house_102", "A-102", "Priya Sharma"),
            Triple("house_201", "B-201", "Suresh Patel"),
            Triple("house_202", "B-202", "Ramesh V"),
            Triple("house_301", "C-301", "Kavitha Reddy"),
            Triple("house_302", "C-302", "Rajesh Rao")
        )

        val list = mutableListOf<MonthlyHouseStatus>()

        for ((houseId, houseNumber, ownerName) in flats) {
            val rate = when (houseNumber) {
                "A-102", "B-202" -> 2000.0
                "C-302" -> 1200.0
                else -> 1500.0
            }

            // Month 0 (Current Month): 3 Paid (A-101, B-201, C-302), 3 Unpaid (A-102, B-202, C-301)
            val m0Paid = houseNumber in listOf("A-101", "B-201", "C-302")
            val m0Date = when (houseNumber) {
                "A-101" -> "$m0 04"
                "B-201" -> "$m0 06"
                "C-302" -> "$m0 08"
                else -> null
            }
            val m0Ref = if (m0Paid) "TXN-M0-${houseNumber.replace("-", "")}" else null
            list.add(MonthlyHouseStatus(houseId, houseNumber, ownerName, m0, m0Paid, rate, m0Date, m0Ref))

            // Month 1 (1 Month Ago): 5 Paid, 1 Unpaid (B-202)
            val m1Paid = houseNumber != "B-202"
            val m1Date = when (houseNumber) {
                "A-101" -> "$m1 03"
                "A-102" -> "$m1 05"
                "B-201" -> "$m1 07"
                "C-301" -> "$m1 10"
                "C-302" -> "$m1 12"
                else -> null
            }
            val m1Ref = if (m1Paid) "TXN-M1-${houseNumber.replace("-", "")}" else null
            list.add(MonthlyHouseStatus(houseId, houseNumber, ownerName, m1, m1Paid, rate, m1Date, m1Ref))

            // Month 2 (2 Months Ago): All 6 Paid
            val m2Date = "$m2 05"
            val m2Ref = "TXN-M2-${houseNumber.replace("-", "")}"
            list.add(MonthlyHouseStatus(houseId, houseNumber, ownerName, m2, true, rate, m2Date, m2Ref))

            // Month 3 (3 Months Ago): All 6 Paid
            val m3Date = "$m3 05"
            val m3Ref = "TXN-M3-${houseNumber.replace("-", "")}"
            list.add(MonthlyHouseStatus(houseId, houseNumber, ownerName, m3, true, rate, m3Date, m3Ref))
        }

        return list
    }

    override fun signInWithPhone(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val token = try {
            val constructor = PhoneAuthProvider.ForceResendingToken::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (_: Exception) {
            @Suppress("UNCHECKED_CAST")
            null as PhoneAuthProvider.ForceResendingToken
        }
        callbacks.onCodeSent("mock_verification_id_123456", token)
    }

    override suspend fun signInWithPhoneAndPassword(phoneNumber: String, password: String): Result<User> {
        val cleanPhone = phoneNumber.trim()
        val foundUser = usersFlow.value.find {
            it.phoneNumber.replace("[^0-9]".toRegex(), "").endsWith(cleanPhone.replace("[^0-9]".toRegex(), ""))
        }
        val user = foundUser ?: if (phoneNumber.contains("9964532102") || phoneNumber.contains("9964532101")) {
            superAdmin
        } else {
            User(
                uid = "mock_uid_${System.currentTimeMillis()}",
                phoneNumber = phoneNumber,
                name = "Mock User",
                role = UserRole.USER
            )
        }

        if (!user.isActive) {
            return Result.failure(Exception("Your account has been deactivated. Please contact San Siri Residency Admin."))
        }

        if (password == "123456" || password == "Anil@102" || password == "12345") {
            currentUserFlow.value = user
            return Result.success(user)
        }
        return Result.failure(Exception("Invalid credentials"))
    }

    override fun sendOtpForRegistration(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        signInWithPhone(phoneNumber, activity, callbacks)
    }

    override suspend fun verifyOtpAndCreateUser(
        verificationId: String,
        code: String,
        phoneNumber: String,
        password: String,
        name: String
    ): Result<User> {
        if (code == "123456" || code == "Anil@102") {
            val user = User(
                uid = "mock_uid_${System.currentTimeMillis()}",
                phoneNumber = phoneNumber,
                name = name,
                role = UserRole.USER
            )
            currentUserFlow.value = user
            return Result.success(user)
        }
        return Result.failure(Exception("Invalid OTP"))
    }

    override suspend fun adminRegisterUser(
        name: String,
        phoneNumber: String,
        flatNumber: String,
        crossStreet: String,
        occupancyType: OccupancyType,
        role: UserRole
    ): Result<User> {
        val cleanPhone = if (phoneNumber.startsWith("+91")) phoneNumber else "+91$phoneNumber"
        val uid = "user_${System.currentTimeMillis()}"
        val user = User(
            uid = uid,
            phoneNumber = cleanPhone,
            name = name,
            role = role,
            isPreRegisteredByAdmin = true
        )
        val userList = usersFlow.value.toMutableList()
        userList.add(user)
        usersFlow.value = userList

        val house = HouseDetail(
            id = "house_${System.currentTimeMillis()}",
            ownerUid = uid,
            ownerName = name,
            ownerPhone = cleanPhone,
            houseNumber = flatNumber.ifBlank { "A-101" },
            crossStreet = crossStreet.ifBlank { "Main Road" },
            occupancyType = occupancyType,
            approvalStatus = ApprovalStatus.APPROVED,
            updatedAt = System.currentTimeMillis()
        )
        val houseList = housesFlow.value.toMutableList()
        houseList.add(house)
        housesFlow.value = houseList

        return Result.success(user)
    }

    override suspend fun adminRegisterResident(
        name: String,
        phoneNumber: String,
        occupancyType: OccupancyType
    ): Result<Unit> {
        val res = adminRegisterUser(
            name = name,
            phoneNumber = phoneNumber,
            flatNumber = "A-101",
            crossStreet = "Main Road",
            occupancyType = occupancyType,
            role = UserRole.USER
        )
        return if (res.isSuccess) Result.success(Unit) else Result.failure(res.exceptionOrNull() ?: Exception("Failed to register resident"))
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        val superAdminPhone = "+919964532102"
        val user = if (verificationId.contains("9964532102") || verificationId.contains("9964532101") || verificationId == "super_admin_verification") {
            usersFlow.value.find { it.phoneNumber == superAdminPhone } ?: superAdmin
        } else {
            usersFlow.value.find { it.uid == "user_123" } ?: User(
                uid = "user_123",
                phoneNumber = "+919876543210",
                name = "Demo User",
                role = UserRole.USER
            )
        }

        if (!user.isActive) {
            return Result.failure(Exception("Your account has been deactivated. Please contact San Siri Residency Admin."))
        }

        if (code == "Anil@102" || code == "123456" || code == "12345") {
            currentUserFlow.value = user
            return Result.success(user)
        }
        return Result.failure(Exception("Invalid OTP"))
    }

    override suspend fun setUserActiveStatus(userUid: String, isActive: Boolean): Result<Unit> {
        val list = usersFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.uid == userUid }
        if (idx >= 0) {
            val updated = list[idx].copy(isActive = isActive)
            list[idx] = updated
            usersFlow.value = list

            if (currentUserFlow.value?.uid == userUid) {
                if (!isActive) {
                    currentUserFlow.value = null
                } else {
                    currentUserFlow.value = updated
                }
            }
        }
        return Result.success(Unit)
    }

    override fun getCurrentUser(): User? = currentUserFlow.value

    override fun getCurrentUserFlow(): Flow<User?> = currentUserFlow.asStateFlow()

    override suspend fun logout() {
        currentUserFlow.value = null
    }

    override suspend fun updateUserRole(targetUid: String, newRole: UserRole): Result<Unit> {
        val list = usersFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.uid == targetUid }
        if (idx >= 0) {
            list[idx] = list[idx].copy(role = newRole)
            usersFlow.value = list
        }
        return Result.success(Unit)
    }

    override suspend fun updateUserRoleAndBuildingDetails(
        targetUid: String,
        name: String,
        houseNumber: String,
        cross: String,
        occupancyType: OccupancyType,
        role: UserRole
    ): Result<Unit> {
        val userList = usersFlow.value.toMutableList()
        val userIdx = userList.indexOfFirst { it.uid == targetUid }
        if (userIdx >= 0) {
            userList[userIdx] = userList[userIdx].copy(name = name, role = role)
            usersFlow.value = userList
        }

        val houseList = housesFlow.value.toMutableList()
        val houseIdx = houseList.indexOfFirst { it.ownerUid == targetUid }
        if (houseIdx >= 0) {
            houseList[houseIdx] = houseList[houseIdx].copy(
                ownerName = name,
                houseNumber = houseNumber,
                crossStreet = cross,
                occupancyType = occupancyType
            )
        } else {
            houseList.add(
                HouseDetail(
                    id = "house_${System.currentTimeMillis()}",
                    ownerUid = targetUid,
                    ownerName = name,
                    houseNumber = houseNumber,
                    crossStreet = cross,
                    occupancyType = occupancyType,
                    approvalStatus = ApprovalStatus.APPROVED
                )
            )
        }
        housesFlow.value = houseList
        return Result.success(Unit)
    }

    override fun getAllUsers(): Flow<List<User>> = usersFlow.asStateFlow()

    override suspend fun quickDemoLogin(
        role: UserRole,
        phoneNumber: String,
        name: String
    ): Result<User> {
        val uid = when (role) {
            UserRole.SUPER_ADMIN -> "super_admin_9964532102"
            UserRole.ADMIN -> "admin_123"
            else -> "owner_123"
        }
        val defaultPhone = when (role) {
            UserRole.SUPER_ADMIN -> "+919964532102"
            UserRole.ADMIN -> "+919876543210"
            else -> "+919876543210"
        }
        val defaultName = when (role) {
            UserRole.SUPER_ADMIN -> "San Siri Super Admin"
            UserRole.ADMIN -> "San Siri Admin"
            else -> "Anil Kumar"
        }
        val phoneToUse = if (phoneNumber.isNotBlank() && phoneNumber != "+919876543210") phoneNumber else defaultPhone
        val displayName = if (name.isNotBlank() && name != "Demo User") name else defaultName

        val user = User(
            uid = uid,
            phoneNumber = phoneToUse,
            name = displayName,
            role = role
        )
        currentUserFlow.value = user

        val list = usersFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.uid == uid || it.phoneNumber == phoneToUse }
        if (idx >= 0) list[idx] = user else list.add(user)
        usersFlow.value = list

        return Result.success(user)
    }

    override suspend fun addAdmin(phoneNumber: String, name: String): Result<User> {
        val formattedPhone = if (!phoneNumber.startsWith("+")) "+91${phoneNumber.trim()}" else phoneNumber.trim()
        val list = usersFlow.value.toMutableList()
        val cleanInputPhone = phoneNumber.replace("[^0-9]".toRegex(), "")
        val idx = list.indexOfFirst {
            it.phoneNumber.replace("[^0-9]".toRegex(), "").endsWith(cleanInputPhone)
        }

        val user = if (idx >= 0) {
            list[idx].copy(
                name = name.ifBlank { list[idx].name },
                role = UserRole.ADMIN
            )
        } else {
            User(
                uid = "admin_${System.currentTimeMillis()}",
                phoneNumber = formattedPhone,
                name = name.ifBlank { "Admin" },
                role = UserRole.ADMIN
            )
        }

        if (idx >= 0) list[idx] = user else list.add(user)
        usersFlow.value = list
        return Result.success(user)
    }

    override fun getAdmins(): Flow<List<User>> {
        return usersFlow.map { list ->
            list.filter { it.role == UserRole.ADMIN || it.role == UserRole.SUPER_ADMIN }
        }
    }

    override fun getAdminUsers(): Flow<List<User>> = getAdmins()

    override suspend fun saveHouseDetail(houseDetail: HouseDetail): Result<HouseDetail> {
        val id = if (houseDetail.id.isBlank()) "house_${System.currentTimeMillis()}" else houseDetail.id
        val updated = houseDetail.copy(id = id)
        val list = housesFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) list[idx] = updated else list.add(updated)
        housesFlow.value = list
        return Result.success(updated)
    }

    override suspend fun saveHousesForUser(ownerUid: String, houses: List<HouseDetail>): Result<List<HouseDetail>> {
        val user = usersFlow.value.find { it.uid == ownerUid }
        val newIds = houses.map { it.id }.filter { it.isNotBlank() }.toSet()

        val updatedAllHouses = housesFlow.value.toMutableList()
        updatedAllHouses.removeAll { it.ownerUid == ownerUid && it.id !in newIds }

        val savedList = mutableListOf<HouseDetail>()
        houses.forEachIndexed { index, h ->
            val id = if (h.id.isBlank()) "house_${System.currentTimeMillis()}_$index" else h.id
            val propType = when {
                h.occupancyType == OccupancyType.EMPTY_SITE || h.isEmptySite -> PropertyType.EMPTY_SITE
                h.isApartment -> PropertyType.APARTMENT
                h.isRented || h.occupancyType == OccupancyType.RENTED -> PropertyType.HOUSE_WITH_RENTED_HOMES
                else -> PropertyType.INDIVIDUAL_HOUSE
            }
            val updatedHouse = h.copy(
                id = id,
                ownerUid = ownerUid,
                ownerName = user?.name ?: h.ownerName.ifBlank { "Resident" },
                ownerPhone = user?.phoneNumber ?: h.ownerPhone,
                propertyType = propType,
                approvalStatus = if (h.approvalStatus == ApprovalStatus.PENDING) ApprovalStatus.APPROVED else h.approvalStatus,
                updatedAt = System.currentTimeMillis()
            )
            savedList.add(updatedHouse)
            val idx = updatedAllHouses.indexOfFirst { it.id == id }
            if (idx >= 0) {
                updatedAllHouses[idx] = updatedHouse
            } else {
                updatedAllHouses.add(updatedHouse)
            }
        }

        housesFlow.value = updatedAllHouses
        return Result.success(savedList)
    }

    override suspend fun deleteHouseDetail(houseId: String): Result<Unit> {
        val list = housesFlow.value.toMutableList()
        list.removeAll { it.id == houseId }
        housesFlow.value = list
        return Result.success(Unit)
    }

    override fun getHouseDetailByOwner(ownerUid: String): Flow<HouseDetail?> {
        return housesFlow.map { list -> list.firstOrNull { it.ownerUid == ownerUid } }
    }

    override fun getHousesByOwner(ownerUid: String): Flow<List<HouseDetail>> {
        return housesFlow.map { list -> list.filter { it.ownerUid == ownerUid } }
    }

    override suspend fun addPropertyForUser(
        ownerUid: String,
        houseNumber: String,
        crossStreet: String,
        occupancyType: OccupancyType,
        propertyType: PropertyType
    ): Result<HouseDetail> {
        val user = usersFlow.value.find { it.uid == ownerUid }
        val id = "house_${System.currentTimeMillis()}"
        val newHouse = HouseDetail(
            id = id,
            ownerUid = ownerUid,
            ownerName = user?.name ?: "Resident",
            ownerPhone = user?.phoneNumber ?: "",
            propertyType = propertyType,
            siteNumber = if (houseNumber.contains("Site")) houseNumber.replace("Site #", "").trim() else "",
            houseNumber = houseNumber,
            crossStreet = crossStreet,
            occupancyType = occupancyType,
            approvalStatus = ApprovalStatus.APPROVED
        )
        val list = housesFlow.value.toMutableList()
        list.add(newHouse)
        housesFlow.value = list
        return Result.success(newHouse)
    }

    override fun getApprovedHouses(): Flow<List<HouseDetail>> {
        return housesFlow.map { list -> list.filter { it.approvalStatus == ApprovalStatus.APPROVED } }
    }

    override fun getHouseDetails(): Flow<List<HouseDetail>> = getApprovedHouses()

    override fun getPendingHouses(): Flow<List<HouseDetail>> {
        return housesFlow.map { list -> list.filter { it.approvalStatus == ApprovalStatus.PENDING } }
    }

    override fun getAllHouses(): Flow<List<HouseDetail>> {
        return housesFlow.asStateFlow()
    }

    override suspend fun updateHouseStatus(houseId: String, status: ApprovalStatus): Result<Unit> {
        val list = housesFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == houseId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(approvalStatus = status)
            housesFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getAnnouncements(): Flow<List<Announcement>> = announcementsFlow.asStateFlow()

    override suspend fun createAnnouncement(announcement: Announcement): Result<Announcement> {
        val list = announcementsFlow.value.toMutableList()
        list.add(0, announcement)
        announcementsFlow.value = list

        publishNotification(
            title = "New Announcement: ${announcement.title}",
            message = announcement.content.ifBlank { announcement.title },
            type = NotificationType.ANNOUNCEMENT
        )

        return Result.success(announcement)
    }

    override suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        val list = announcementsFlow.value.toMutableList()
        list.removeAll { it.id == announcementId }
        announcementsFlow.value = list
        return Result.success(Unit)
    }

    override fun getResidentRequests(): Flow<List<ResidentRequest>> = residentRequestsFlow.asStateFlow()

    override suspend fun createResidentRequest(request: ResidentRequest): Result<ResidentRequest> {
        val list = residentRequestsFlow.value.toMutableList()
        list.add(0, request)
        residentRequestsFlow.value = list

        val isConcern = request.category.equals("CONCERN", ignoreCase = true)
        val userName = request.userName.ifBlank { "Resident" }
        val flatNumber = request.flatNumber.ifBlank { "N/A" }
        if (isConcern) {
            publishNotification(
                title = "New Concern: ${request.title}",
                message = "Raised by $userName ($flatNumber)",
                type = NotificationType.CONCERN
            )
        } else {
            publishNotification(
                title = "New Request: ${request.title}",
                message = "Requested by $userName ($flatNumber)",
                type = NotificationType.REQUEST
            )
        }

        return Result.success(request)
    }

    override suspend fun updateResidentRequestStatus(requestId: String, status: RequestStatus): Result<Unit> {
        val list = residentRequestsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == requestId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = status)
            residentRequestsFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getRequests(): Flow<List<RequestItem>> = requestsFlow.asStateFlow()

    override suspend fun createRequest(request: RequestItem): Result<RequestItem> {
        val list = requestsFlow.value.toMutableList()
        list.add(0, request)
        requestsFlow.value = list

        val userName = request.userName.ifBlank { "Resident" }
        val houseNumber = request.houseNumber.ifBlank { "N/A" }
        publishNotification(
            title = "New Request: ${request.title}",
            message = "Requested by $userName ($houseNumber)",
            type = NotificationType.REQUEST
        )

        return Result.success(request)
    }

    override suspend fun updateRequestStatus(requestId: String, status: ItemStatus): Result<Unit> {
        val list = requestsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == requestId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = status)
            requestsFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getComplaints(): Flow<List<ComplaintItem>> = complaintsFlow.asStateFlow()

    override suspend fun createComplaint(complaint: ComplaintItem): Result<ComplaintItem> {
        val list = complaintsFlow.value.toMutableList()
        list.add(0, complaint)
        complaintsFlow.value = list

        val userName = complaint.userName.ifBlank { "Resident" }
        val houseNumber = complaint.houseNumber.ifBlank { "N/A" }
        publishNotification(
            title = "New Concern: ${complaint.title}",
            message = "Raised by $userName ($houseNumber)",
            type = NotificationType.CONCERN
        )

        return Result.success(complaint)
    }

    override suspend fun updateComplaintStatus(
        complaintId: String,
        status: ItemStatus
    ): Result<Unit> {
        val list = complaintsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == complaintId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = status)
            complaintsFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getPricingRules(): Flow<PricingRules> = pricingRulesFlow.asStateFlow()

    override suspend fun updatePricingRules(rules: PricingRules): Result<Unit> {
        pricingRulesFlow.value = rules
        return Result.success(Unit)
    }

    override fun calculateMaintenanceFee(houseDetail: HouseDetail?, rules: PricingRules): Double {
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

    override fun calculateMaintenanceFee(occupancyType: OccupancyType, rules: PricingRules): Double {
        return when (occupancyType) {
            OccupancyType.SELF_OCCUPIED -> rules.selfOccupiedMaintenanceRate
            OccupancyType.RENTED -> rules.rentedMaintenanceRate
            OccupancyType.EMPTY_SITE -> rules.emptySiteMaintenanceRate
        }
    }

    override fun get4MonthsPaymentStatus(): Flow<List<MonthlyHouseStatus>> = monthlyStatusesFlow.asStateFlow()

    override suspend fun approveMonthlyStatus(houseId: String, monthYear: String): Result<Unit> {
        val list = monthlyStatusesFlow.value.toMutableList()
        val idx = list.indexOfFirst {
            (it.houseId == houseId || it.houseNumber == houseId) && it.monthYear == monthYear
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                isPaid = true,
                paymentStatus = PaymentStatus.APPROVED,
                paymentDate = "2025-01-01"
            )
            monthlyStatusesFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getUnpaidMonthsForUser(userUid: String): Flow<List<String>> {
        return monthlyStatusesFlow.map { list ->
            list.filter { !it.isPaid && (it.houseId == "house_101" || userUid.isNotBlank()) }.map { it.monthYear }.distinct()
        }
    }

    override suspend fun submitMultiMonthPayment(
        userUid: String,
        houseId: String,
        selectedMonths: List<String>,
        transactionRef: String,
        totalAmount: Double
    ): Result<Unit> {
        val list = monthlyStatusesFlow.value.toMutableList()
        for (month in selectedMonths) {
            for (i in list.indices) {
                if (list[i].monthYear == month) {
                    list[i] = list[i].copy(isPaid = true, paymentDate = "2025-05-15", transactionRef = transactionRef)
                }
            }
        }
        monthlyStatusesFlow.value = list
        return Result.success(Unit)
    }

    override suspend fun submitPayment(payment: MaintenancePayment): Result<MaintenancePayment> {
        val id = if (payment.id.isBlank()) "pay_${System.currentTimeMillis()}" else payment.id
        val updated = payment.copy(id = id)
        val list = paymentsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) list[idx] = updated else list.add(updated)
        paymentsFlow.value = list
        return Result.success(updated)
    }

    override fun getPaymentsByOwner(ownerUid: String): Flow<List<MaintenancePayment>> {
        return paymentsFlow.map { list -> list.filter { it.ownerUid == ownerUid } }
    }

    override fun getPendingPayments(): Flow<List<MaintenancePayment>> {
        return paymentsFlow.map { list -> list.filter { it.status == PaymentStatus.PENDING } }
    }

    override fun getAllPayments(): Flow<List<MaintenancePayment>> {
        return paymentsFlow.asStateFlow()
    }

    override suspend fun approvePayment(paymentId: String): Result<Unit> {
        val list = paymentsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == paymentId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = PaymentStatus.APPROVED)
            paymentsFlow.value = list
        }
        return Result.success(Unit)
    }

    override suspend fun rejectPayment(paymentId: String): Result<Unit> {
        val list = paymentsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == paymentId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = PaymentStatus.REJECTED)
            paymentsFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getFinancialTransactions(): Flow<List<FinancialTransaction>> {
        return combine(customFinancialTransactionsFlow, monthlyStatusesFlow) { custom, monthly ->
            val incomeFromPayments = monthly.filter { it.isPaid }.map { ms ->
                val dateMillis = try {
                    if (!ms.paymentDate.isNullOrBlank()) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ms.paymentDate)?.time ?: System.currentTimeMillis()
                    } else System.currentTimeMillis()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
                FinancialTransaction(
                    id = "maint_${ms.houseId}_${ms.monthYear.replace(" ", "_")}",
                    type = TransactionType.INCOME,
                    amount = ms.amount,
                    category = "Maintenance Payment",
                    description = "Maintenance Fee - ${ms.houseNumber} (${ms.monthYear})",
                    dateMillis = dateMillis,
                    addedBy = ms.ownerName
                )
            }
            (custom + incomeFromPayments).sortedByDescending { it.dateMillis }
        }
    }

    override suspend fun addFinancialTransaction(transaction: FinancialTransaction): Result<FinancialTransaction> {
        val id = if (transaction.id.isBlank()) "ft_${System.currentTimeMillis()}" else transaction.id
        val updated = transaction.copy(id = id)
        val list = customFinancialTransactionsFlow.value.toMutableList()
        list.add(0, updated)
        customFinancialTransactionsFlow.value = list
        return Result.success(updated)
    }

    override fun getCrossRepresentatives(): Flow<Map<String, String>> = crossRepresentativesFlow.asStateFlow()

    override suspend fun assignCrossRepresentative(cross: String, userUid: String): Result<Unit> {
        val map = crossRepresentativesFlow.value.toMutableMap()
        map[cross] = userUid
        crossRepresentativesFlow.value = map
        return Result.success(Unit)
    }

    override fun getRequestComments(): Flow<List<RequestComment>> = requestCommentsFlow.asStateFlow()

    override suspend fun addRequestComment(comment: RequestComment): Result<RequestComment> {
        val id = if (comment.id.isBlank()) "comment_${System.currentTimeMillis()}" else comment.id
        val resolvedUserName = if (comment.userName.isBlank() || comment.userName.equals("Resident", ignoreCase = true)) {
            val user = usersFlow.value.find { it.uid == comment.userUid }
            val nameCandidate = user?.name?.trim() ?: ""
            if (nameCandidate.isNotBlank() && !nameCandidate.equals("Resident", ignoreCase = true)) {
                nameCandidate
            } else if (!user?.phoneNumber.isNullOrBlank()) {
                user.phoneNumber
            } else {
                if (comment.userName.isNotBlank()) comment.userName else "Resident"
            }
        } else {
            comment.userName
        }
        val updated = comment.copy(id = id, userName = resolvedUserName)
        val list = requestCommentsFlow.value.toMutableList()
        list.add(updated)
        requestCommentsFlow.value = list

        val requestTitle = residentRequestsFlow.value.find { it.id == updated.requestId }?.title
            ?: requestsFlow.value.find { it.id == updated.requestId }?.title
            ?: "Request"
        val name = updated.userName.ifBlank { "Resident" }

        publishNotification(
            title = "New Comment on $requestTitle",
            message = "$name commented: ${updated.commentText}",
            type = NotificationType.COMMENT
        )

        return Result.success(updated)
    }

    override fun getSocietyContacts(): Flow<List<SocietyContact>> = societyContactsFlow.asStateFlow()

    override suspend fun addSocietyContact(contact: SocietyContact): Result<SocietyContact> {
        val id = if (contact.id.isBlank()) "contact_${System.currentTimeMillis()}" else contact.id
        val updated = contact.copy(id = id)
        val list = societyContactsFlow.value.toMutableList()
        list.add(0, updated)
        societyContactsFlow.value = list
        return Result.success(updated)
    }

    override suspend fun updateSocietyContact(contact: SocietyContact): Result<SocietyContact> {
        val list = societyContactsFlow.value.map { if (it.id == contact.id) contact else it }
        societyContactsFlow.value = list
        return Result.success(contact)
    }

    override suspend fun deleteSocietyContact(contactId: String): Result<Unit> {
        val list = societyContactsFlow.value.filterNot { it.id == contactId }
        societyContactsFlow.value = list
        return Result.success(Unit)
    }

    // Community Notifications
    override fun getNotificationsFlow(): Flow<List<AppNotification>> = notificationsFlow.asStateFlow()

    override suspend fun publishNotification(
        title: String,
        message: String,
        type: NotificationType,
        targetUid: String
    ): Result<AppNotification> {
        val id = "notif_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val notification = AppNotification(
            id = id,
            title = title,
            message = message,
            type = type,
            targetUid = targetUid,
            createdAt = System.currentTimeMillis(),
            isRead = false
        )
        val list = notificationsFlow.value.toMutableList()
        list.add(0, notification)
        notificationsFlow.value = list
        return Result.success(notification)
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        val list = notificationsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == notificationId }
        if (idx >= 0) {
            val updated = list[idx].copy(isRead = true)
            list[idx] = updated
            notificationsFlow.value = list
        }
        return Result.success(Unit)
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> {
        val list = notificationsFlow.value.map { it.copy(isRead = true) }
        notificationsFlow.value = list
        return Result.success(Unit)
    }

    override suspend fun clearNotifications(): Result<Unit> {
        notificationsFlow.value = emptyList()
        return Result.success(Unit)
    }

    private fun initialFakeCrossReps(): Map<String, String> {
        return mapOf(
            "1st Cross" to "owner_demo_123",
            "2nd Cross" to "owner_demo_456",
            "3rd Cross" to "owner_demo_789",
            "4th Cross" to "owner_demo_101",
            "5th Cross" to "owner_demo_202",
            "6th Cross" to "owner_demo_302",
            "7th Cross" to "owner_demo_401",
            "8th Cross" to "owner_demo_402",
            "9th Cross" to "owner_demo_501",
            "10th Cross" to "owner_demo_502"
        )
    }

    private fun initialFakeUsers(): List<User> {
        return listOf(
            superAdmin,
            User(
                uid = "admin_123",
                phoneNumber = "+919876543210",
                name = "San Siri Admin",
                role = UserRole.ADMIN
            ),
            User(
                uid = "owner_123",
                phoneNumber = "+919123456789",
                name = "Anil Kumar",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_123",
                phoneNumber = "+919123456789",
                name = "Ramesh Kumar",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_456",
                phoneNumber = "+919888877777",
                name = "Suresh Patel",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_789",
                phoneNumber = "+919777766666",
                name = "Anil Verma",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_101",
                phoneNumber = "+919666655555",
                name = "Priya Sharma",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_202",
                phoneNumber = "+919555544444",
                name = "Vijay Reddy",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_302",
                phoneNumber = "+919444433333",
                name = "Kavitha Nair",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_401",
                phoneNumber = "+919333322222",
                name = "Rajesh Gupta",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_402",
                phoneNumber = "+919222211111",
                name = "Deepa Singh",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_501",
                phoneNumber = "+919111100000",
                name = "Manjunath Rao",
                role = UserRole.USER
            ),
            User(
                uid = "owner_demo_502",
                phoneNumber = "+919000099999",
                name = "Sunita Joshi",
                role = UserRole.USER
            ),
            User(uid = "owner_demo_101_extra", phoneNumber = "+919811111111", name = "Arun Hegde", role = UserRole.USER),
            User(uid = "owner_demo_102_extra", phoneNumber = "+919822222222", name = "Bhavana K", role = UserRole.USER),
            User(uid = "owner_demo_103_extra", phoneNumber = "+919833333333", name = "Chetan Gowda", role = UserRole.USER),
            User(uid = "owner_demo_104_extra", phoneNumber = "+919844444444", name = "Divya Menon", role = UserRole.USER),
            User(uid = "owner_demo_105_extra", phoneNumber = "+919855555555", name = "Eshwar Prasad", role = UserRole.USER),
            User(uid = "owner_demo_106_extra", phoneNumber = "+919866666666", name = "Farhan Ali", role = UserRole.USER),
            User(uid = "owner_demo_107_extra", phoneNumber = "+919877777777", name = "Ganesh Bhat", role = UserRole.USER),
            User(uid = "owner_demo_108_extra", phoneNumber = "+919888888888", name = "Harini Rao", role = UserRole.USER),
            User(uid = "owner_demo_109_extra", phoneNumber = "+919899999999", name = "Irfan Khan", role = UserRole.USER),
            User(uid = "owner_demo_110_extra", phoneNumber = "+919800000000", name = "Jyothi Swaroop", role = UserRole.USER)
        )
    }

    private fun initialFakeHouses(): List<HouseDetail> {
        return listOf(
            HouseDetail(
                id = "house_101",
                ownerUid = "owner_demo_123",
                ownerName = "Ramesh Kumar",
                ownerPhone = "+919123456789",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block A",
                floorNumber = "1",
                houseNumber = "A-101",
                crossStreet = "1st Cross",
                occupancyType = OccupancyType.SELF_OCCUPIED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_102",
                ownerUid = "owner_demo_456",
                ownerName = "Suresh Patel",
                ownerPhone = "+919888877777",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block A",
                floorNumber = "1",
                houseNumber = "A-102",
                crossStreet = "2nd Cross",
                occupancyType = OccupancyType.RENTED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_201",
                ownerUid = "owner_demo_789",
                ownerName = "Anil Verma",
                ownerPhone = "+919777766666",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block B",
                floorNumber = "2",
                houseNumber = "B-201",
                crossStreet = "3rd Cross",
                occupancyType = OccupancyType.SELF_OCCUPIED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_202",
                ownerUid = "owner_demo_101",
                ownerName = "Priya Sharma",
                ownerPhone = "+919666655555",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block B",
                floorNumber = "2",
                houseNumber = "B-202",
                crossStreet = "4th Cross",
                occupancyType = OccupancyType.RENTED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_301",
                ownerUid = "owner_demo_202",
                ownerName = "Vijay Reddy",
                ownerPhone = "+919555544444",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block C",
                floorNumber = "3",
                houseNumber = "C-301",
                crossStreet = "5th Cross",
                occupancyType = OccupancyType.SELF_OCCUPIED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_302",
                ownerUid = "owner_demo_302",
                ownerName = "Kavitha Nair",
                ownerPhone = "+919444433333",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block C",
                floorNumber = "3",
                houseNumber = "C-302",
                crossStreet = "6th Cross",
                occupancyType = OccupancyType.SELF_OCCUPIED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_401",
                ownerUid = "owner_demo_401",
                ownerName = "Rajesh Gupta",
                ownerPhone = "+919333322222",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block D",
                floorNumber = "4",
                houseNumber = "D-401",
                crossStreet = "7th Cross",
                occupancyType = OccupancyType.SELF_OCCUPIED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_402",
                ownerUid = "owner_demo_402",
                ownerName = "Deepa Singh",
                ownerPhone = "+919222211111",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block D",
                floorNumber = "4",
                houseNumber = "D-402",
                crossStreet = "8th Cross",
                occupancyType = OccupancyType.RENTED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_501",
                ownerUid = "owner_demo_501",
                ownerName = "Manjunath Rao",
                ownerPhone = "+919111100000",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block E",
                floorNumber = "5",
                houseNumber = "E-501",
                crossStreet = "9th Cross",
                occupancyType = OccupancyType.SELF_OCCUPIED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(
                id = "house_502",
                ownerUid = "owner_demo_502",
                ownerName = "Sunita Joshi",
                ownerPhone = "+919000099999",
                propertyType = PropertyType.APARTMENT,
                buildingName = "San Siri Residency",
                buildingNumber = "Block E",
                floorNumber = "5",
                houseNumber = "E-502",
                crossStreet = "10th Cross",
                occupancyType = OccupancyType.RENTED,
                approvalStatus = ApprovalStatus.APPROVED
            ),
            HouseDetail(id = "house_101_b", ownerUid = "owner_demo_101_extra", ownerName = "Arun Hegde", ownerPhone = "+919811111111", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block A", floorNumber = "1", houseNumber = "A-101B", crossStreet = "1st Cross", occupancyType = OccupancyType.SELF_OCCUPIED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_102_b", ownerUid = "owner_demo_102_extra", ownerName = "Bhavana K", ownerPhone = "+919822222222", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block A", floorNumber = "1", houseNumber = "A-102B", crossStreet = "2nd Cross", occupancyType = OccupancyType.RENTED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_201_b", ownerUid = "owner_demo_103_extra", ownerName = "Chetan Gowda", ownerPhone = "+919833333333", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block B", floorNumber = "2", houseNumber = "B-201B", crossStreet = "3rd Cross", occupancyType = OccupancyType.SELF_OCCUPIED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_202_b", ownerUid = "owner_demo_104_extra", ownerName = "Divya Menon", ownerPhone = "+919844444444", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block B", floorNumber = "2", houseNumber = "B-202B", crossStreet = "4th Cross", occupancyType = OccupancyType.RENTED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_301_b", ownerUid = "owner_demo_105_extra", ownerName = "Eshwar Prasad", ownerPhone = "+919855555555", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block C", floorNumber = "3", houseNumber = "C-301B", crossStreet = "5th Cross", occupancyType = OccupancyType.SELF_OCCUPIED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_302_b", ownerUid = "owner_demo_106_extra", ownerName = "Farhan Ali", ownerPhone = "+919866666666", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block C", floorNumber = "3", houseNumber = "C-302B", crossStreet = "6th Cross", occupancyType = OccupancyType.SELF_OCCUPIED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_401_b", ownerUid = "owner_demo_107_extra", ownerName = "Ganesh Bhat", ownerPhone = "+919877777777", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block D", floorNumber = "4", houseNumber = "D-401B", crossStreet = "7th Cross", occupancyType = OccupancyType.SELF_OCCUPIED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_402_b", ownerUid = "owner_demo_108_extra", ownerName = "Harini Rao", ownerPhone = "+919888888888", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block D", floorNumber = "4", houseNumber = "D-402B", crossStreet = "8th Cross", occupancyType = OccupancyType.RENTED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_501_b", ownerUid = "owner_demo_109_extra", ownerName = "Irfan Khan", ownerPhone = "+919899999999", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block E", floorNumber = "5", houseNumber = "E-501B", crossStreet = "9th Cross", occupancyType = OccupancyType.SELF_OCCUPIED, approvalStatus = ApprovalStatus.APPROVED),
            HouseDetail(id = "house_502_b", ownerUid = "owner_demo_110_extra", ownerName = "Jyothi Swaroop", ownerPhone = "+919800000000", propertyType = PropertyType.APARTMENT, buildingName = "San Siri Residency", buildingNumber = "Block E", floorNumber = "5", houseNumber = "E-502B", crossStreet = "10th Cross", occupancyType = OccupancyType.RENTED, approvalStatus = ApprovalStatus.APPROVED)
        )
    }
}
