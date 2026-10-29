package com.anil.sansiri.data.repository

import android.app.Activity
import android.content.Context
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
import com.anil.sansiri.util.NotificationHelper
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val crossNames = listOf(
    "1st Cross", "2nd Cross", "3rd Cross", "4th Cross", "5th Cross",
    "6th Cross", "7th Cross", "8th Cross", "9th Cross", "10th Cross"
)

private val demoFirstNames = listOf("Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
    "Ananya", "Diya", "Saanvi", "Aadhya", "Pari", "Anika", "Navya", "Riya", "Meera", "Kavya",
    "Ramesh", "Suresh", "Mahesh", "Rajesh", "Priya", "Sunita", "Deepa", "Manjunath", "Vijay", "Kavitha")
private val demoLastNames = listOf("Kumar", "Sharma", "Patel", "Verma", "Reddy", "Rao", "Gupta", "Singh", "Joshi", "Nair", "Pillai", "Iyer")

private fun generate10UsersAndHouses(): Pair<List<User>, List<HouseDetail>> {
    val users = mutableListOf<User>()
    val houses = mutableListOf<HouseDetail>()

    users.add(User(uid = "super_admin_9964532102", phoneNumber = "+919964532102", name = "San Siri Super Admin", role = UserRole.SUPER_ADMIN))
    users.add(User(uid = "admin_demo_123", phoneNumber = "+919876543210", name = "San Siri Admin", role = UserRole.ADMIN))
    users.add(User(uid = "owner_123", phoneNumber = "+919123456789", name = "Anil Kumar", role = UserRole.USER))

    val blockLetters = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")

    for (cIdx in crossNames.indices) {
        val cross = crossNames[cIdx]
        val block = blockLetters[cIdx % blockLetters.size]
        for (h in 1..10) {
            val idx = cIdx * 10 + h
            val uid = "owner_cross_${cIdx + 1}_$h"
            val houseId = "house_cross_${cIdx + 1}_$h"
            val houseNum = "$block-${(cIdx + 1) * 100 + h}"
            val name = "${demoFirstNames[(idx * 3) % demoFirstNames.size]} ${demoLastNames[(idx * 7) % demoLastNames.size]}"
            val phone = "+9198${String.format(Locale.US, "%08d", 7654000 + idx)}"

            users.add(
                User(
                    uid = uid,
                    phoneNumber = phone,
                    name = name,
                    role = UserRole.USER,
                    isPreRegisteredByAdmin = true
                )
            )

            houses.add(
                HouseDetail(
                    id = houseId,
                    ownerUid = uid,
                    ownerName = name,
                    ownerPhone = phone,
                    propertyType = if (h % 3 == 0) PropertyType.INDIVIDUAL_HOUSE else PropertyType.APARTMENT,
                    buildingName = "San Siri Residency",
                    buildingNumber = "Block $block",
                    floorNumber = ((h % 5) + 1).toString(),
                    houseNumber = houseNum,
                    crossStreet = cross,
                    occupancyType = if (h % 2 == 0) OccupancyType.RENTED else OccupancyType.SELF_OCCUPIED,
                    approvalStatus = ApprovalStatus.APPROVED,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    return Pair(users, houses)
}

private fun initialDemoUsers(): List<User> {
    return generate10UsersAndHouses().first
}

private fun initialDemoHouses(): List<HouseDetail> {
    return generate10UsersAndHouses().second
}

class SanSiriRepositoryImpl private constructor(private val context: Context) : SanSiriRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance() else null
        } catch (_: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseFirestore.getInstance() else null
        } catch (_: Exception) {
            null
        }
    }

    // In-memory state flows initialized with realistic San Siri Residency demo data
    private val currentUserFlow = MutableStateFlow<User?>(null)
    private val usersFlow = MutableStateFlow<List<User>>(initialDemoUsers())
    private val housesFlow = MutableStateFlow<List<HouseDetail>>(initialDemoHouses())
    private val announcementsFlow = MutableStateFlow<List<Announcement>>(initialDemoAnnouncements())
    private val residentRequestsFlow = MutableStateFlow<List<ResidentRequest>>(initialDemoResidentRequests())
    private val requestsFlow = MutableStateFlow<List<RequestItem>>(emptyList())
    private val complaintsFlow = MutableStateFlow<List<ComplaintItem>>(emptyList())
    private val paymentsFlow = MutableStateFlow<List<MaintenancePayment>>(emptyList())
    private val monthlyStatusesFlow = MutableStateFlow<List<MonthlyHouseStatus>>(initialDemoMonthlyStatuses())
    private val pricingRulesFlow = MutableStateFlow(
        PricingRules(
            id = "default_rules",
            individualHouseRate = 1500.0,
            rentedHouseRate = 2000.0,
            apartmentRate = 1200.0,
            emptySiteRate = 500.0,
            selfOccupiedMaintenanceRate = 1500.0,
            rentedMaintenanceRate = 2000.0,
            emptySiteMaintenanceRate = 500.0,
            updatedAt = System.currentTimeMillis()
        )
    )
    private val customFinancialTransactionsFlow = MutableStateFlow<List<FinancialTransaction>>(initialDemoCustomTransactions())
    private val crossRepresentativesFlow = MutableStateFlow<Map<String, String>>(initialDemoCrossReps())
    private val requestCommentsFlow = MutableStateFlow<List<RequestComment>>(initialDemoRequestComments())
    private val societyContactsFlow = MutableStateFlow<List<SocietyContact>>(initialDemoSocietyContacts())
    private val notificationsFlow = MutableStateFlow<List<AppNotification>>(initialDemoNotifications())

    private fun saveUserToPrefs(user: User) {
        try {
            val prefs = context.getSharedPreferences("sansiri_auth_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("cached_uid", user.uid)
                .putString("cached_phone", user.phoneNumber)
                .putString("cached_name", user.name)
                .putString("cached_role", user.role.name)
                .putBoolean("cached_is_active", user.isActive)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("SanSiriRepo", "Failed to save user to prefs", e)
        }
    }

    private fun getUserFromPrefs(): User? {
        return try {
            val prefs = context.getSharedPreferences("sansiri_auth_prefs", Context.MODE_PRIVATE)
            val uid = prefs.getString("cached_uid", null) ?: return null
            val phone = prefs.getString("cached_phone", "") ?: ""
            val name = prefs.getString("cached_name", "") ?: ""
            val roleStr = prefs.getString("cached_role", UserRole.USER.name) ?: UserRole.USER.name
            val isActive = prefs.getBoolean("cached_is_active", true)
            val role = try {
                UserRole.valueOf(roleStr)
            } catch (_: Exception) {
                UserRole.USER
            }
            User(
                uid = uid,
                phoneNumber = phone,
                name = name,
                role = role,
                isActive = isActive
            )
        } catch (e: Exception) {
            android.util.Log.e("SanSiriRepo", "Failed to read user from prefs", e)
            null
        }
    }

    private fun clearUserFromPrefs() {
        try {
            val prefs = context.getSharedPreferences("sansiri_auth_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            android.util.Log.e("SanSiriRepo", "Failed to clear user from prefs", e)
        }
    }

    private fun restoreSession() {
        val cachedUser = getUserFromPrefs()
        if (cachedUser != null) {
            if (!cachedUser.isActive) {
                currentUserFlow.value = null
                clearUserFromPrefs()
            } else {
                currentUserFlow.value = cachedUser
            }
        }

        val firebaseUser = auth?.currentUser
        val targetUid = firebaseUser?.uid ?: cachedUser?.uid

        if (targetUid != null) {
            firestore?.collection("users")?.document(targetUid)?.get()
                ?.addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            if (!user.isActive) {
                                currentUserFlow.value = null
                                clearUserFromPrefs()
                            } else {
                                currentUserFlow.value = user
                                saveUserToPrefs(user)
                            }
                        }
                    }
                }
        }
    }

    init {
        restoreSession()
        setupFirestoreListeners()
        seedDummyDataIfNeeded()
    }

    private fun setupFirestoreListeners() {
        val db = firestore ?: return

        db.collection("users").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc -> doc.toObject(User::class.java) }
                usersFlow.value = list

                val current = currentUserFlow.value
                if (current != null) {
                    val updatedCurrent = list.find { it.uid == current.uid }
                    if (updatedCurrent != null) {
                        if (!updatedCurrent.isActive) {
                            currentUserFlow.value = null
                            clearUserFromPrefs()
                        } else {
                            currentUserFlow.value = updatedCurrent
                            saveUserToPrefs(updatedCurrent)
                        }
                    }
                }
            }
        }

        db.collection("house_details").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc -> doc.toObject(HouseDetail::class.java) }
                if (list.isNotEmpty()) {
                    housesFlow.value = list
                }
            }
        }

        db.collection("houses").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc -> doc.toObject(HouseDetail::class.java) }
                if (list.isNotEmpty() || housesFlow.value.isEmpty()) {
                    housesFlow.value = list
                }
            }
        }

        db.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc -> doc.toObject(Announcement::class.java) }
                    announcementsFlow.value = list
                }
            }

        db.collection("resident_requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc -> doc.toObject(ResidentRequest::class.java) }
                    residentRequestsFlow.value = list
                }
            }

        db.collection("monthly_statuses").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc -> doc.toObject(MonthlyHouseStatus::class.java) }
                monthlyStatusesFlow.value = list
            }
        }

        db.collection("pricing").document("default_rules").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val rules = snapshot.toObject(PricingRules::class.java)
                if (rules != null) {
                    pricingRulesFlow.value = rules
                }
            }
        }

        db.collection("society_contacts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc -> doc.toObject(SocietyContact::class.java) }
                    societyContactsFlow.value = list
                }
            }

        db.collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc -> doc.toObject(AppNotification::class.java) }
                    notificationsFlow.value = list
                }
            }
    }

    private fun seedDummyDataIfNeeded() {
        val db = firestore ?: return
        repositoryScope.launch {
            try {
                db.collection("pricing").document("default_rules").get().addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        db.collection("pricing").document("default_rules").set(pricingRulesFlow.value)
                    }
                }

                db.collection("house_details").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.size() < 50) {
                        val (users, houses) = generate10UsersAndHouses()
                        val batch = db.batch()
                        users.forEach { user ->
                            batch.set(db.collection("users").document(user.uid), user)
                        }
                        houses.forEach { house ->
                            batch.set(db.collection("house_details").document(house.id), house)
                            batch.set(db.collection("houses").document(house.id), house)
                        }
                        batch.commit()
                    }
                }

                db.collection("houses").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.size() < 50) {
                        val (users, houses) = generate10UsersAndHouses()
                        val batch = db.batch()
                        users.forEach { user ->
                            batch.set(db.collection("users").document(user.uid), user)
                        }
                        houses.forEach { house ->
                            batch.set(db.collection("house_details").document(house.id), house)
                            batch.set(db.collection("houses").document(house.id), house)
                        }
                        batch.commit()
                    }
                }

                db.collection("users").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (user in usersFlow.value) {
                            db.collection("users").document(user.uid).set(user)
                        }
                    }
                }

                db.collection("announcements").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (ann in announcementsFlow.value) {
                            db.collection("announcements").document(ann.id).set(ann)
                        }
                    }
                }

                db.collection("resident_requests").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (req in residentRequestsFlow.value) {
                            db.collection("resident_requests").document(req.id).set(req)
                        }
                    }
                }

                db.collection("monthly_statuses").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (ms in monthlyStatusesFlow.value) {
                            val docId = "${ms.houseId}_${ms.monthYear.replace(" ", "_")}"
                            db.collection("monthly_statuses").document(docId).set(ms)
                        }
                    }
                }

                db.collection("society_contacts").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (contact in societyContactsFlow.value) {
                            db.collection("society_contacts").document(contact.id).set(contact)
                        }
                    }
                }

                db.collection("notifications").get().addOnSuccessListener { snapshot ->
                    if (snapshot == null || snapshot.isEmpty) {
                        for (notif in notificationsFlow.value) {
                            db.collection("notifications").document(notif.id).set(notif)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SanSiriRepo", "Seeding failed", e)
            }
        }
    }

    // Helper functions for initial demo data
    companion object {
        @Volatile
        private var INSTANCE: SanSiriRepositoryImpl? = null

        fun getInstance(context: Context): SanSiriRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SanSiriRepositoryImpl(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun initialDemoCustomTransactions(): List<FinancialTransaction> {
            val now = System.currentTimeMillis()
            return listOf(
                FinancialTransaction(
                    id = "ft_1",
                    type = TransactionType.EXPENSE,
                    amount = 25000.0,
                    category = "Security Salary",
                    description = "Monthly Security Guards Salary (2 Guards)",
                    dateMillis = now - 86400000L * 3,
                    addedBy = "San Siri Admin"
                ),
                FinancialTransaction(
                    id = "ft_2",
                    type = TransactionType.EXPENSE,
                    amount = 4500.0,
                    category = "Water Tanker",
                    description = "Emergency Water Tanker Supply (3 Loads)",
                    dateMillis = now - 86400000L * 6,
                    addedBy = "San Siri Admin"
                ),
                FinancialTransaction(
                    id = "ft_3",
                    type = TransactionType.INCOME,
                    amount = 5000.0,
                    category = "Hall Booking",
                    description = "Community Hall Party Event Booking",
                    dateMillis = now - 86400000L * 8,
                    addedBy = "San Siri Admin"
                ),
                FinancialTransaction(
                    id = "ft_4",
                    type = TransactionType.EXPENSE,
                    amount = 3200.0,
                    category = "Maintenance",
                    description = "Overhead Tank Chemical Cleaning",
                    dateMillis = now - 86400000L * 12,
                    addedBy = "San Siri Admin"
                )
            )
        }

        private fun initialDemoCrossReps(): Map<String, String> {
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

        private fun initialDemoRequestComments(): List<RequestComment> {
            val now = System.currentTimeMillis()
            return listOf(
                RequestComment(
                    id = "comment_1",
                    requestId = "req_1",
                    userUid = "admin_demo_123",
                    userName = "San Siri Admin",
                    userRole = UserRole.ADMIN,
                    commentText = "Elevator technician has been scheduled for inspection tomorrow at 11 AM.",
                    createdAt = now - 86400000L * 1
                ),
                RequestComment(
                    id = "comment_2",
                    requestId = "req_1",
                    userUid = "owner_demo_123",
                    userName = "Anil Kumar",
                    userRole = UserRole.USER,
                    commentText = "Thank you Admin! Please ask them to check the motor noise as well.",
                    createdAt = now - 3600000L * 18
                ),
                RequestComment(
                    id = "comment_3",
                    requestId = "req_2",
                    userUid = "admin_demo_123",
                    userName = "San Siri Admin",
                    userRole = UserRole.ADMIN,
                    commentText = "Plumber assigned to fix parking slot B-12 pipe leakage.",
                    createdAt = now - 3600000L * 3
                )
            )
        }

        private fun initialDemoNotifications(): List<AppNotification> {
            val now = System.currentTimeMillis()
            return listOf(
                AppNotification(
                    id = "notif_welcome",
                    title = "Welcome to San Siri Residency",
                    message = "Stay updated with community announcements, requests, and concerns here.",
                    type = NotificationType.GENERAL,
                    createdAt = now - 3600000,
                    isRead = false
                ),
                AppNotification(
                    id = "notif_announcement_1",
                    title = "New Announcement: Annual General Body Meeting",
                    message = "Annual General Body Meeting scheduled for Sunday at 10:00 AM in the Community Hall.",
                    type = NotificationType.ANNOUNCEMENT,
                    createdAt = now - 7200000,
                    isRead = true
                ),
                AppNotification(
                    id = "notif_request_1",
                    title = "New Request: Street Light Repair",
                    message = "Requested by Anil Kumar (101)",
                    type = NotificationType.REQUEST,
                    createdAt = now - 14400000,
                    isRead = true
                )
            )
        }

        private fun initialDemoSocietyContacts(): List<SocietyContact> {
            val now = System.currentTimeMillis()
            return listOf(
                SocietyContact(
                    id = "contact_1",
                    category = "Electrical Issues",
                    contactPersonName = "Ramesh (Electrician)",
                    phoneNumber = "9876500001",
                    addedBy = "San Siri Admin",
                    createdAt = now - 86400000L * 4
                ),
                SocietyContact(
                    id = "contact_2",
                    category = "Garbage Issues",
                    contactPersonName = "Suresh (Garbage Supervisor)",
                    phoneNumber = "9876500002",
                    addedBy = "San Siri Admin",
                    createdAt = now - 86400000L * 3
                ),
                SocietyContact(
                    id = "contact_3",
                    category = "Plumbing Services",
                    contactPersonName = "Mahesh (Plumber)",
                    phoneNumber = "9876500003",
                    addedBy = "San Siri Admin",
                    createdAt = now - 86400000L * 2
                ),
                SocietyContact(
                    id = "contact_4",
                    category = "Security Desk",
                    contactPersonName = "Main Gate Guard",
                    phoneNumber = "9876500004",
                    addedBy = "San Siri Admin",
                    createdAt = now - 86400000L * 1
                )
            )
        }

        private fun initialDemoAnnouncements(): List<Announcement> {
            val now = System.currentTimeMillis()
            return listOf(
                Announcement(
                    id = "ann_1",
                    title = "Annual Society Meeting on Sunday",
                    content = "Dear Residents, the Annual General Body Meeting for San Siri Residency will be held this Sunday at 10:00 AM in the Community Hall. Attendance requested.",
                    postedByName = "San Siri Super Admin",
                    authorUid = "super_admin_9964532102",
                    authorRole = UserRole.SUPER_ADMIN,
                    createdAt = now - 86400000L * 2
                ),
                Announcement(
                    id = "ann_2",
                    title = "Water Tank Cleaning on 15th",
                    content = "Overhead water tanks will undergo periodic cleaning on Friday the 15th between 9 AM and 2 PM. Please store sufficient water.",
                    postedByName = "San Siri Admin",
                    authorUid = "admin_demo_123",
                    authorRole = UserRole.ADMIN,
                    createdAt = now - 86400000L * 1
                ),
                Announcement(
                    id = "ann_3",
                    title = "Garbage Segregation Guidelines",
                    content = "Please ensure mandatory wet and dry waste segregation in green and blue bins placed on each floor.",
                    postedByName = "San Siri Admin",
                    authorUid = "admin_demo_123",
                    authorRole = UserRole.ADMIN,
                    createdAt = now - 3600000L * 5
                )
            )
        }

        private fun initialDemoResidentRequests(): List<ResidentRequest> {
            val now = System.currentTimeMillis()
            return listOf(
                ResidentRequest(
                    id = "req_1",
                    userUid = "owner_demo_123",
                    userName = "Anil Kumar",
                    flatNumber = "A-101",
                    title = "Elevator service on 3rd floor",
                    description = "Elevator makes squeaking noise when arriving at 3rd floor. Please schedule maintenance.",
                    category = "REQUEST",
                    status = RequestStatus.OPEN,
                    createdAt = now - 86400000L * 2,
                    updatedAt = now - 86400000L * 2
                ),
                ResidentRequest(
                    id = "req_2",
                    userUid = "owner_demo_456",
                    userName = "Priya Sharma",
                    flatNumber = "A-102",
                    title = "Parking slot B-12 pipe repair",
                    description = "Water leakage from overhead pipe near parking slot B-12.",
                    category = "REQUEST",
                    status = RequestStatus.IN_PROGRESS,
                    createdAt = now - 86400000L * 1,
                    updatedAt = now - 3600000L * 4
                ),
                ResidentRequest(
                    id = "req_3",
                    userUid = "owner_demo_789",
                    userName = "Suresh Patel",
                    flatNumber = "B-201",
                    title = "NOC for Broadband Cable",
                    description = "Requesting NOC to install fiber internet line.",
                    category = "REQUEST",
                    status = RequestStatus.RESOLVED,
                    createdAt = now - 86400000L * 3,
                    updatedAt = now - 86400000L * 1
                ),
                ResidentRequest(
                    id = "req_4",
                    userUid = "owner_demo_123",
                    userName = "Anil Kumar",
                    flatNumber = "A-101",
                    title = "Late night music disturbance in Block B",
                    description = "Excessive volume past 11 PM on weekends. Request security enforcement.",
                    category = "CONCERN",
                    status = RequestStatus.OPEN,
                    createdAt = now - 86400000L * 2,
                    updatedAt = now - 86400000L * 2
                ),
                ResidentRequest(
                    id = "req_5",
                    userUid = "owner_demo_456",
                    userName = "Priya Sharma",
                    flatNumber = "A-102",
                    title = "Garbage collection timing delay",
                    description = "Garbage collection truck delayed on Sundays causing odour in corridor.",
                    category = "CONCERN",
                    status = RequestStatus.IN_PROGRESS,
                    createdAt = now - 86400000L * 1,
                    updatedAt = now - 3600000L * 2
                ),
                ResidentRequest(
                    id = "req_6",
                    userUid = "owner_demo_789",
                    userName = "Suresh Patel",
                    flatNumber = "B-201",
                    title = "Stray dogs near children play area",
                    description = "Request gate security to ensure main gate remains closed.",
                    category = "CONCERN",
                    status = RequestStatus.RESOLVED,
                    createdAt = now - 86400000L * 4,
                    updatedAt = now - 86400000L * 2
                )
            )
        }

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

        private fun initialDemoMonthlyStatuses(): List<MonthlyHouseStatus> {
            val recentMonths = getRecent4Months()
            val houses = initialDemoHouses()
            val list = mutableListOf<MonthlyHouseStatus>()

            for (house in houses) {
                val rate = when (house.occupancyType) {
                    OccupancyType.RENTED -> 2000.0
                    OccupancyType.EMPTY_SITE -> 500.0
                    else -> 1500.0
                }

                for (mIdx in recentMonths.indices) {
                    val month = recentMonths[mIdx]
                    val hash = (house.id.hashCode() + mIdx * 19).let { if (it < 0) -it else it }
                    val mod = hash % 3

                    val isPaid = mod == 0
                    val isPending = mod == 1
                    val paymentStatus = when {
                        isPaid -> PaymentStatus.APPROVED
                        isPending -> PaymentStatus.PENDING
                        else -> PaymentStatus.PENDING
                    }
                    val txnRef = if (isPaid) "TXN-M$mIdx-${house.houseNumber.replace("-", "")}" else if (isPending) "UPI/2025/${hash % 9000 + 1000}" else null
                    val payDate = if (isPaid || isPending) "$month ${(hash % 25) + 1}" else null

                    list.add(
                        MonthlyHouseStatus(
                            houseId = house.id,
                            houseNumber = house.houseNumber,
                            ownerName = house.ownerName,
                            monthYear = month,
                            isPaid = isPaid,
                            amount = rate,
                            paymentDate = payDate,
                            transactionRef = txnRef,
                            paymentStatus = paymentStatus,
                            crossStreet = house.crossStreet,
                            paymentId = if (isPaid || isPending) "pay_${house.id}_m$mIdx" else null
                        )
                    )
                }
            }
            return list
        }
    }

    // --- Authentication & User Management ---
    override fun signInWithPhone(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val authObj = auth
        if (authObj != null) {
            val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(authObj)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            val token = try {
                val constructor = PhoneAuthProvider.ForceResendingToken::class.java.getDeclaredConstructor()
                constructor.isAccessible = true
                constructor.newInstance()
            } catch (_: Exception) {
                @Suppress("UNCHECKED_CAST")
                null as PhoneAuthProvider.ForceResendingToken
            }
            callbacks.onCodeSent("demo_verification_id_12345", token)
        }
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        val superAdminPhone = "+919964532102"
        if (code == "Anil@102" || code == "123456" || code == "12345") {
            val user = usersFlow.value.find { it.phoneNumber == superAdminPhone } ?: User(
                uid = "super_admin_9964532102",
                phoneNumber = superAdminPhone,
                name = "San Siri Super Admin",
                role = UserRole.SUPER_ADMIN
            )
            if (!user.isActive) {
                return Result.failure(Exception("Your account has been deactivated. Please contact San Siri Residency Admin."))
            }
            currentUserFlow.value = user
            saveUserToPrefs(user)
            return Result.success(user)
        }
        return Result.failure(Exception("Invalid OTP code"))
    }

    override suspend fun signInWithPhoneAndPassword(phoneNumber: String, password: String): Result<User> {
        val cleanPhone = phoneNumber.trim()
        val foundUser = usersFlow.value.find {
            it.phoneNumber.replace("[^0-9]".toRegex(), "").endsWith(cleanPhone.replace("[^0-9]".toRegex(), ""))
        }
        val user = foundUser ?: User(
            uid = "user_${System.currentTimeMillis()}",
            phoneNumber = cleanPhone,
            name = "Resident User",
            role = UserRole.USER
        )
        if (!user.isActive) {
            return Result.failure(Exception("Your account has been deactivated. Please contact San Siri Residency Admin."))
        }
        currentUserFlow.value = user
        saveUserToPrefs(user)
        return Result.success(user)
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
        val user = User(
            uid = "user_${System.currentTimeMillis()}",
            phoneNumber = phoneNumber,
            name = name,
            role = UserRole.USER
        )
        currentUserFlow.value = user
        saveUserToPrefs(user)

        val list = usersFlow.value.toMutableList()
        list.add(user)
        usersFlow.value = list
        firestore?.collection("users")?.document(user.uid)?.set(user)

        return Result.success(user)
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
        firestore?.collection("users")?.document(user.uid)?.set(user)

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
        firestore?.collection("houses")?.document(house.id)?.set(house)

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

    override fun getCurrentUser(): User? = currentUserFlow.value

    override fun getCurrentUserFlow(): Flow<User?> = currentUserFlow.asStateFlow()

    override suspend fun logout() {
        auth?.signOut()
        currentUserFlow.value = null
        clearUserFromPrefs()
    }

    override suspend fun updateUserRole(targetUid: String, newRole: UserRole): Result<Unit> {
        val list = usersFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.uid == targetUid }
        if (idx >= 0) {
            val updated = list[idx].copy(role = newRole)
            list[idx] = updated
            usersFlow.value = list

            if (currentUserFlow.value?.uid == targetUid) {
                currentUserFlow.value = updated
                saveUserToPrefs(updated)
            }
            firestore?.collection("users")?.document(targetUid)?.update("role", newRole.name)
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
        // 1. Update user name and role
        val userList = usersFlow.value.toMutableList()
        val userIdx = userList.indexOfFirst { it.uid == targetUid }
        if (userIdx >= 0) {
            val updatedUser = userList[userIdx].copy(
                name = name.ifBlank { userList[userIdx].name },
                role = role
            )
            userList[userIdx] = updatedUser
            usersFlow.value = userList
            if (currentUserFlow.value?.uid == targetUid) {
                currentUserFlow.value = updatedUser
                saveUserToPrefs(updatedUser)
            }
            firestore?.collection("users")?.document(targetUid)?.set(updatedUser)
        }

        // 2. Update house detail for user
        val houseList = housesFlow.value.toMutableList()
        val houseIdx = houseList.indexOfFirst { it.ownerUid == targetUid }
        val updatedHouse = if (houseIdx >= 0) {
            houseList[houseIdx].copy(
                ownerName = name.ifBlank { houseList[houseIdx].ownerName },
                houseNumber = houseNumber.ifBlank { houseList[houseIdx].houseNumber },
                crossStreet = cross.ifBlank { houseList[houseIdx].crossStreet },
                occupancyType = occupancyType
            )
        } else {
            HouseDetail(
                id = "house_${System.currentTimeMillis()}",
                ownerUid = targetUid,
                ownerName = name,
                houseNumber = houseNumber,
                crossStreet = cross,
                occupancyType = occupancyType,
                approvalStatus = ApprovalStatus.APPROVED
            )
        }

        if (houseIdx >= 0) houseList[houseIdx] = updatedHouse else houseList.add(updatedHouse)
        housesFlow.value = houseList
        firestore?.collection("houses")?.document(updatedHouse.id)?.set(updatedHouse)

        return Result.success(Unit)
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
                    clearUserFromPrefs()
                } else {
                    currentUserFlow.value = updated
                    saveUserToPrefs(updated)
                }
            }
            firestore?.collection("users")?.document(userUid)?.update("isActive", isActive)
        }
        return Result.success(Unit)
    }

    override fun getAllUsers(): Flow<List<User>> = usersFlow.asStateFlow()

    override fun getAdmins(): Flow<List<User>> {
        return usersFlow.map { list ->
            list.filter { it.role == UserRole.ADMIN || it.role == UserRole.SUPER_ADMIN }
        }
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
                name = name.ifBlank { "San Siri Admin" },
                role = UserRole.ADMIN
            )
        }

        if (idx >= 0) list[idx] = user else list.add(user)
        usersFlow.value = list
        firestore?.collection("users")?.document(user.uid)?.set(user)
        return Result.success(user)
    }

    override suspend fun quickDemoLogin(
        role: UserRole,
        phoneNumber: String,
        name: String
    ): Result<User> {
        val user = when (role) {
            UserRole.SUPER_ADMIN -> usersFlow.value.find { it.role == UserRole.SUPER_ADMIN }
                ?: User(uid = "super_admin_9964532102", phoneNumber = "+919964532102", name = "San Siri Super Admin", role = UserRole.SUPER_ADMIN)
            UserRole.ADMIN -> usersFlow.value.find { it.role == UserRole.ADMIN }
                ?: User(uid = "admin_demo_123", phoneNumber = "+919876543210", name = "San Siri Admin", role = UserRole.ADMIN)
            else -> usersFlow.value.find { it.uid == "owner_demo_123" }
                ?: User(uid = "owner_demo_123", phoneNumber = "+919123456789", name = "Anil Kumar", role = UserRole.USER)
        }

        currentUserFlow.value = user
        saveUserToPrefs(user)
        return Result.success(user)
    }

    // --- House & Property details ---
    override suspend fun saveHouseDetail(houseDetail: HouseDetail): Result<HouseDetail> {
        val id = if (houseDetail.id.isBlank()) "house_${System.currentTimeMillis()}" else houseDetail.id
        val updated = houseDetail.copy(id = id)
        val list = housesFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) list[idx] = updated else list.add(updated)
        housesFlow.value = list
        firestore?.collection("houses")?.document(id)?.set(updated)
        return Result.success(updated)
    }

    override suspend fun saveHousesForUser(ownerUid: String, houses: List<HouseDetail>): Result<List<HouseDetail>> {
        val user = usersFlow.value.find { it.uid == ownerUid }
        val existingHouses = housesFlow.value.filter { it.ownerUid == ownerUid }
        val existingIds = existingHouses.map { it.id }.filter { it.isNotBlank() }.toSet()
        val newIds = houses.map { it.id }.filter { it.isNotBlank() }.toSet()

        // Delete removed houses from Firestore
        val removedIds = existingIds - newIds
        removedIds.forEach { houseId ->
            firestore?.collection("houses")?.document(houseId)?.delete()
        }

        val updatedAllHouses = housesFlow.value.toMutableList()
        updatedAllHouses.removeAll { it.ownerUid == ownerUid }

        val currentMonthly = monthlyStatusesFlow.value.toMutableList()
        val recentMonths = getRecent4Months()

        val savedList = mutableListOf<HouseDetail>()

        houses.forEachIndexed { index, h ->
            val isNew = h.id.isBlank() || h.id !in existingIds
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
            updatedAllHouses.add(updatedHouse)
            firestore?.collection("houses")?.document(id)?.set(updatedHouse)

            if (isNew) {
                val rate = when (updatedHouse.occupancyType) {
                    OccupancyType.RENTED -> 2000.0
                    OccupancyType.EMPTY_SITE -> 500.0
                    else -> 1500.0
                }
                recentMonths.forEach { m ->
                    if (currentMonthly.none { it.houseId == id && it.monthYear == m }) {
                        currentMonthly.add(
                            MonthlyHouseStatus(
                                houseId = id,
                                houseNumber = updatedHouse.houseNumber,
                                ownerName = updatedHouse.ownerName,
                                monthYear = m,
                                isPaid = false,
                                paymentStatus = PaymentStatus.PENDING,
                                crossStreet = updatedHouse.crossStreet,
                                amount = rate,
                                transactionRef = null
                            )
                        )
                    }
                }
            }
        }

        housesFlow.value = updatedAllHouses
        monthlyStatusesFlow.value = currentMonthly

        return Result.success(savedList)
    }

    override suspend fun deleteHouseDetail(houseId: String): Result<Unit> {
        val list = housesFlow.value.toMutableList()
        list.removeAll { it.id == houseId }
        housesFlow.value = list
        firestore?.collection("houses")?.document(houseId)?.delete()
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
        firestore?.collection("houses")?.document(id)?.set(newHouse)

        val currentMonthly = monthlyStatusesFlow.value.toMutableList()
        val recentMonths = getRecent4Months()
        val rate = when (occupancyType) {
            OccupancyType.RENTED -> 2000.0
            OccupancyType.EMPTY_SITE -> 500.0
            else -> 1500.0
        }
        recentMonths.forEach { m ->
            currentMonthly.add(
                MonthlyHouseStatus(
                    houseId = id,
                    houseNumber = houseNumber,
                    ownerName = user?.name ?: "Resident",
                    monthYear = m,
                    isPaid = false,
                    paymentStatus = PaymentStatus.PENDING,
                    crossStreet = crossStreet,
                    amount = rate,
                    transactionRef = null
                )
            )
        }
        monthlyStatusesFlow.value = currentMonthly

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
            firestore?.collection("houses")?.document(houseId)?.update("approvalStatus", status.name)
        }
        return Result.success(Unit)
    }

    // --- Announcements ---
    override fun getAnnouncements(): Flow<List<Announcement>> = announcementsFlow.asStateFlow()

    override suspend fun createAnnouncement(announcement: Announcement): Result<Announcement> {
        val id = if (announcement.id.isBlank()) "ann_${System.currentTimeMillis()}" else announcement.id
        val updated = announcement.copy(id = id)
        val list = announcementsFlow.value.toMutableList()
        list.add(0, updated)
        announcementsFlow.value = list
        firestore?.collection("announcements")?.document(id)?.set(updated)

        publishNotification(
            title = "New Announcement: ${updated.title}",
            message = updated.content.ifBlank { updated.title },
            type = NotificationType.ANNOUNCEMENT
        )

        return Result.success(updated)
    }

    override suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        val list = announcementsFlow.value.toMutableList()
        list.removeAll { it.id == announcementId }
        announcementsFlow.value = list
        firestore?.collection("announcements")?.document(announcementId)?.delete()
        return Result.success(Unit)
    }

    // --- Resident Requests & Concerns ---
    override fun getResidentRequests(): Flow<List<ResidentRequest>> = residentRequestsFlow.asStateFlow()

    override suspend fun createResidentRequest(request: ResidentRequest): Result<ResidentRequest> {
        val id = if (request.id.isBlank()) "req_${System.currentTimeMillis()}" else request.id
        val updated = request.copy(id = id)
        val list = residentRequestsFlow.value.toMutableList()
        list.add(0, updated)
        residentRequestsFlow.value = list
        firestore?.collection("resident_requests")?.document(id)?.set(updated)

        val isConcern = updated.category.equals("CONCERN", ignoreCase = true)
        val userName = updated.userName.ifBlank { "Resident" }
        val flatNumber = updated.flatNumber.ifBlank { "N/A" }
        if (isConcern) {
            publishNotification(
                title = "New Concern: ${updated.title}",
                message = "Raised by $userName ($flatNumber)",
                type = NotificationType.CONCERN
            )
        } else {
            publishNotification(
                title = "New Request: ${updated.title}",
                message = "Requested by $userName ($flatNumber)",
                type = NotificationType.REQUEST
            )
        }

        return Result.success(updated)
    }

    override suspend fun updateResidentRequestStatus(requestId: String, status: RequestStatus): Result<Unit> {
        val list = residentRequestsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == requestId }
        if (idx >= 0) {
            val updated = list[idx].copy(status = status, updatedAt = System.currentTimeMillis())
            list[idx] = updated
            residentRequestsFlow.value = list
            firestore?.collection("resident_requests")?.document(requestId)?.set(updated)
        }
        return Result.success(Unit)
    }

    // Legacy Requests & Complaints
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

    override suspend fun updateComplaintStatus(complaintId: String, status: ItemStatus): Result<Unit> {
        val list = complaintsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == complaintId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = status)
            complaintsFlow.value = list
        }
        return Result.success(Unit)
    }

    // --- Pricing Rules ---
    override fun getPricingRules(): Flow<PricingRules> = pricingRulesFlow.asStateFlow()

    override suspend fun updatePricingRules(rules: PricingRules): Result<Unit> {
        pricingRulesFlow.value = rules
        firestore?.collection("pricing")?.document("default_rules")?.set(rules)
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

    // --- Payments & 4-Month Payment Status ---
    override fun get4MonthsPaymentStatus(): Flow<List<MonthlyHouseStatus>> = monthlyStatusesFlow.asStateFlow()

    override suspend fun approveMonthlyStatus(houseId: String, monthYear: String): Result<Unit> {
        val list = monthlyStatusesFlow.value.toMutableList()
        val idx = list.indexOfFirst {
            (it.houseId == houseId || it.houseNumber == houseId) && it.monthYear == monthYear
        }
        if (idx >= 0) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            list[idx] = list[idx].copy(
                isPaid = true,
                paymentStatus = PaymentStatus.APPROVED,
                paymentDate = todayStr
            )
            monthlyStatusesFlow.value = list
            val docId = "${list[idx].houseId}_${monthYear.replace(" ", "_")}"
            firestore?.collection("monthly_statuses")?.document(docId)?.set(list[idx])
        }
        return Result.success(Unit)
    }

    override fun getUnpaidMonthsForUser(userUid: String): Flow<List<String>> {
        return housesFlow.map { houses ->
            val userHouse = houses.find { it.ownerUid == userUid }
            val houseNumber = userHouse?.displayHouseOrSiteNumber ?: "A-101"
            val allStatuses = monthlyStatusesFlow.value

            val userStatuses = allStatuses.filter {
                it.houseId == userHouse?.id || it.houseNumber == houseNumber || (userHouse == null && it.houseNumber == "A-101")
            }

            userStatuses.filter { !it.isPaid }.map { it.monthYear }
        }
    }

    override suspend fun submitMultiMonthPayment(
        userUid: String,
        houseId: String,
        selectedMonths: List<String>,
        transactionRef: String,
        totalAmount: Double
    ): Result<Unit> {
        val userHouse = housesFlow.value.find { it.ownerUid == userUid || it.id == houseId }
        val houseNumber = userHouse?.displayHouseOrSiteNumber ?: "A-101"
        val ownerName = userHouse?.ownerName ?: currentUserFlow.value?.name ?: "Resident"
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val list = monthlyStatusesFlow.value.toMutableList()
        for (month in selectedMonths) {
            val idx = list.indexOfFirst {
                (it.houseId == houseId || it.houseNumber == houseNumber) && it.monthYear == month
            }
            if (idx >= 0) {
                list[idx] = list[idx].copy(
                    isPaid = true,
                    paymentDate = todayStr
                )
                val docId = "${list[idx].houseId}_${month.replace(" ", "_")}"
                firestore?.collection("monthly_statuses")?.document(docId)?.set(list[idx])
            } else {
                val newStatus = MonthlyHouseStatus(
                    houseId = houseId.ifBlank { "house_101" },
                    houseNumber = houseNumber,
                    ownerName = ownerName,
                    monthYear = month,
                    isPaid = true,
                    amount = if (selectedMonths.isNotEmpty()) totalAmount / selectedMonths.size else 1500.0,
                    paymentDate = todayStr
                )
                list.add(newStatus)
                val docId = "${newStatus.houseId}_${month.replace(" ", "_")}"
                firestore?.collection("monthly_statuses")?.document(docId)?.set(newStatus)
            }
        }
        monthlyStatusesFlow.value = list

        // Also add a MaintenancePayment record
        val payment = MaintenancePayment(
            id = "pay_${System.currentTimeMillis()}",
            houseId = houseId.ifBlank { userHouse?.id ?: "house_101" },
            ownerUid = userUid,
            ownerName = ownerName,
            houseNumber = houseNumber,
            monthYear = selectedMonths.joinToString(", "),
            amount = totalAmount,
            paymentMethod = "UPI / Online",
            transactionRef = transactionRef,
            status = PaymentStatus.APPROVED,
            submittedAt = System.currentTimeMillis(),
            approvedAt = System.currentTimeMillis()
        )
        submitPayment(payment)

        return Result.success(Unit)
    }

    override suspend fun submitPayment(payment: MaintenancePayment): Result<MaintenancePayment> {
        val id = if (payment.id.isBlank()) "pay_${System.currentTimeMillis()}" else payment.id
        val updated = payment.copy(id = id)
        val list = paymentsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) list[idx] = updated else list.add(updated)
        paymentsFlow.value = list
        firestore?.collection("payments")?.document(id)?.set(updated)
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
            firestore?.collection("payments")?.document(paymentId)?.update("status", PaymentStatus.APPROVED.name)
        }
        return Result.success(Unit)
    }

    override suspend fun rejectPayment(paymentId: String): Result<Unit> {
        val list = paymentsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == paymentId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = PaymentStatus.REJECTED)
            paymentsFlow.value = list
            firestore?.collection("payments")?.document(paymentId)?.update("status", PaymentStatus.REJECTED.name)
        }
        return Result.success(Unit)
    }

    // --- Financial Transactions & Balance Sheet ---
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
        firestore?.collection("financial_transactions")?.document(id)?.set(updated)
        return Result.success(updated)
    }

    // --- Cross Representatives ---
    override fun getCrossRepresentatives(): Flow<Map<String, String>> = crossRepresentativesFlow.asStateFlow()

    override suspend fun assignCrossRepresentative(cross: String, userUid: String): Result<Unit> {
        val map = crossRepresentativesFlow.value.toMutableMap()
        map[cross] = userUid
        crossRepresentativesFlow.value = map
        firestore?.collection("cross_representatives")?.document(cross.replace(" ", "_"))?.set(mapOf("cross" to cross, "userUid" to userUid))
        return Result.success(Unit)
    }

    // --- Interactive Request Comments ---
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
        firestore?.collection("request_comments")?.document(id)?.set(updated)

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

    // --- Society Contacts ---
    override fun getSocietyContacts(): Flow<List<SocietyContact>> = societyContactsFlow.asStateFlow()

    override suspend fun addSocietyContact(contact: SocietyContact): Result<SocietyContact> {
        return try {
            val id = if (contact.id.isBlank()) "contact_${System.currentTimeMillis()}" else contact.id
            val updated = contact.copy(id = id)
            val list = societyContactsFlow.value.toMutableList()
            list.add(0, updated)
            societyContactsFlow.value = list
            firestore?.collection("society_contacts")?.document(id)?.set(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSocietyContact(contact: SocietyContact): Result<SocietyContact> {
        return try {
            val list = societyContactsFlow.value.map { if (it.id == contact.id) contact else it }
            societyContactsFlow.value = list
            firestore?.collection("society_contacts")?.document(contact.id)?.set(contact)
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSocietyContact(contactId: String): Result<Unit> {
        return try {
            val list = societyContactsFlow.value.filterNot { it.id == contactId }
            societyContactsFlow.value = list
            firestore?.collection("society_contacts")?.document(contactId)?.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Community Notifications ---
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
        firestore?.collection("notifications")?.document(id)?.set(notification)

        NotificationHelper.showSystemNotification(context, title, message)

        return Result.success(notification)
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        val list = notificationsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == notificationId }
        if (idx >= 0) {
            val updated = list[idx].copy(isRead = true)
            list[idx] = updated
            notificationsFlow.value = list
            firestore?.collection("notifications")?.document(notificationId)?.update("isRead", true)
        }
        return Result.success(Unit)
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> {
        val list = notificationsFlow.value.map { it.copy(isRead = true) }
        notificationsFlow.value = list
        firestore?.collection("notifications")?.get()?.addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.update("isRead", true)
            }
        }
        return Result.success(Unit)
    }

    override suspend fun clearNotifications(): Result<Unit> {
        notificationsFlow.value = emptyList()
        firestore?.collection("notifications")?.get()?.addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.delete()
            }
        }
        return Result.success(Unit)
    }
}
