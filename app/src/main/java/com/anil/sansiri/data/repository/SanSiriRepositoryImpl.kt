package com.anil.sansiri.data.repository

import android.app.Activity
import android.content.Context
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SanSiriRepositoryImpl private constructor(private val context: Context) : SanSiriRepository {

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

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty() && auth != null
        } catch (_: Exception) {
            false
        }

    // In-memory state flows for offline / fallback / quick responsive UI
    private val currentUserFlow = MutableStateFlow<User?>(null)

    private val superAdminUser = User(
        uid = "super_admin_9964532102",
        phoneNumber = "+919964532102",
        name = "San Siri Super Admin",
        role = UserRole.SUPER_ADMIN
    )

    private val initialUsers = listOf(
        superAdminUser,
        User(
            uid = "mock_admin_123",
            phoneNumber = "+919876543210",
            name = "San Siri Admin",
            role = UserRole.ADMIN
        ),
        User(
            uid = "mock_owner_123",
            phoneNumber = "+919123456789",
            name = "Anil Kumar",
            role = UserRole.HOUSE_OWNER
        )
    )

    private val usersFlow = MutableStateFlow<List<User>>(initialUsers)

    private val initialHouses = listOf(
        HouseDetail(
            id = "house_101",
            ownerUid = "mock_owner_123",
            ownerName = "Anil Kumar",
            ownerPhone = "+919123456789",
            buildingName = "San Siri Residency",
            houseNumber = "A-102",
            crossStreet = "3rd Cross",
            occupancyType = OccupancyType.SELF_OCCUPIED,
            approvalStatus = ApprovalStatus.APPROVED
        ),
        HouseDetail(
            id = "house_102",
            ownerUid = "mock_owner_456",
            ownerName = "Ramesh Sharma",
            ownerPhone = "+919888877777",
            buildingName = "San Siri Residency",
            houseNumber = "B-204",
            crossStreet = "4th Cross",
            occupancyType = OccupancyType.RENTED,
            approvalStatus = ApprovalStatus.PENDING
        )
    )

    private val housesFlow = MutableStateFlow<List<HouseDetail>>(initialHouses)

    private val pricingRulesFlow = MutableStateFlow(
        PricingRules(
            id = "default_rules",
            selfOccupiedMaintenanceRate = 1500.0,
            rentedMaintenanceRate = 2000.0,
            updatedAt = System.currentTimeMillis()
        )
    )

    private val initialPayments = listOf(
        MaintenancePayment(
            id = "pay_001",
            houseId = "house_101",
            ownerUid = "mock_owner_123",
            ownerName = "Anil Kumar",
            houseNumber = "A-102",
            monthYear = "2025-05",
            amount = 1500.0,
            paymentMethod = "UPI",
            transactionRef = "UPI987654321",
            status = PaymentStatus.APPROVED,
            submittedAt = System.currentTimeMillis() - 86400000L * 5,
            approvedAt = System.currentTimeMillis() - 86400000L * 4
        ),
        MaintenancePayment(
            id = "pay_002",
            houseId = "house_102",
            ownerUid = "mock_owner_456",
            ownerName = "Ramesh Sharma",
            houseNumber = "B-204",
            monthYear = "2025-05",
            amount = 2000.0,
            paymentMethod = "GPay",
            transactionRef = "GP123456789",
            status = PaymentStatus.PENDING,
            submittedAt = System.currentTimeMillis() - 86400000L * 1,
            approvedAt = null
        )
    )

    private val paymentsFlow = MutableStateFlow<List<MaintenancePayment>>(initialPayments)

    override fun signInWithPhone(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val cleanPhone = phoneNumber.replace("[^0-9]".toRegex(), "")
        if (isFirebaseAvailable && auth != null && !cleanPhone.endsWith("9964532102")) {
            try {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60L,
                    TimeUnit.SECONDS,
                    activity,
                    callbacks
                )
                return
            } catch (_: Exception) {
                // Fallback to mock
            }
        }
        // Fallback trigger for mock / debug environments
        val mockToken = try {
            val constructor = PhoneAuthProvider.ForceResendingToken::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (_: Exception) {
            @Suppress("UNCHECKED_CAST")
            null as PhoneAuthProvider.ForceResendingToken
        }
        val verId = if (cleanPhone.endsWith("9964532102")) "super_admin_verification_9964532102" else "mock_verification_id_123456"
        callbacks.onCodeSent(verId, mockToken)
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        // Super Admin verification check for code 12345 or 123456
        if ((code == "12345" || code == "123456") && (verificationId.contains("9964532102") || verificationId == "super_admin_verification_9964532102")) {
            currentUserFlow.value = superAdminUser
            return Result.success(superAdminUser)
        }

        if (isFirebaseAvailable && auth != null && verificationId != "mock_verification_id_123456") {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                val userResult = suspendCoroutine<User?> { cont ->
                    auth!!.signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            val fbUser = authResult.user
                            if (fbUser != null) {
                                val user = User(
                                    uid = fbUser.uid,
                                    phoneNumber = fbUser.phoneNumber ?: "",
                                    name = fbUser.displayName ?: "User",
                                    role = UserRole.HOUSE_OWNER
                                )
                                cont.resume(user)
                            } else {
                                cont.resume(null)
                            }
                        }
                        .addOnFailureListener {
                            cont.resume(null)
                        }
                }
                if (userResult != null) {
                    currentUserFlow.value = userResult
                    try {
                        firestore?.collection("users")?.document(userResult.uid)?.set(userResult)
                    } catch (_: Exception) {}
                    return Result.success(userResult)
                }
            } catch (e: Exception) {
                if (code != "123456" && code != "12345") {
                    return Result.failure(e)
                }
            }
        }

        // Mock verification fallback
        if (code == "123456" || code == "12345" || verificationId == "mock_verification_id_123456" || !isFirebaseAvailable) {
            val user = User(
                uid = "mock_user_${System.currentTimeMillis()}",
                phoneNumber = "+919876543210",
                name = "Demo User",
                role = UserRole.HOUSE_OWNER
            )
            currentUserFlow.value = user
            return Result.success(user)
        }

        return Result.failure(Exception("Invalid verification code"))
    }

    override fun getCurrentUser(): User? = currentUserFlow.value

    override fun getCurrentUserFlow(): Flow<User?> = currentUserFlow.asStateFlow()

    override suspend fun logout() {
        currentUserFlow.value = null
        if (isFirebaseAvailable) {
            try {
                auth?.signOut()
            } catch (_: Exception) {}
        }
    }

    override suspend fun switchUserRole(newRole: UserRole): Result<User> {
        val current = currentUserFlow.value ?: User(
            uid = when (newRole) {
                UserRole.SUPER_ADMIN -> "super_admin_9964532102"
                UserRole.ADMIN -> "mock_admin_123"
                UserRole.HOUSE_OWNER -> "mock_owner_123"
            },
            phoneNumber = when (newRole) {
                UserRole.SUPER_ADMIN -> "+919964532102"
                UserRole.ADMIN -> "+919876543210"
                UserRole.HOUSE_OWNER -> "+919876543210"
            },
            name = when (newRole) {
                UserRole.SUPER_ADMIN -> "San Siri Super Admin"
                UserRole.ADMIN -> "San Siri Admin"
                UserRole.HOUSE_OWNER -> "Anil Kumar"
            },
            role = newRole
        )
        val updated = current.copy(role = newRole)
        currentUserFlow.value = updated
        if (isFirebaseAvailable) {
            try {
                firestore?.collection("users")?.document(updated.uid)?.set(updated)
            } catch (_: Exception) {}
        }
        return Result.success(updated)
    }

    override suspend fun quickDemoLogin(
        role: UserRole,
        phoneNumber: String,
        name: String
    ): Result<User> {
        val uid = when (role) {
            UserRole.SUPER_ADMIN -> "super_admin_9964532102"
            UserRole.ADMIN -> "mock_admin_123"
            UserRole.HOUSE_OWNER -> "mock_owner_123"
        }
        val defaultPhone = when (role) {
            UserRole.SUPER_ADMIN -> "+919964532102"
            UserRole.ADMIN -> "+919876543210"
            UserRole.HOUSE_OWNER -> "+919876543210"
        }
        val defaultName = when (role) {
            UserRole.SUPER_ADMIN -> "San Siri Super Admin"
            UserRole.ADMIN -> "San Siri Admin"
            UserRole.HOUSE_OWNER -> "Anil Kumar"
        }
        val phoneToUse = if (phoneNumber.isNotBlank() && phoneNumber != "+919876543210") phoneNumber else defaultPhone
        val displayName = if (name.isNotBlank() && name != "Demo User") name else defaultName

        val user = User(
            uid = uid,
            phoneNumber = phoneToUse,
            name = displayName,
            role = role,
            createdAt = System.currentTimeMillis()
        )
        currentUserFlow.value = user

        val currentUsers = usersFlow.value.toMutableList()
        val idx = currentUsers.indexOfFirst { it.uid == uid || it.phoneNumber == phoneToUse }
        if (idx >= 0) {
            currentUsers[idx] = user
        } else {
            currentUsers.add(user)
        }
        usersFlow.value = currentUsers

        if (isFirebaseAvailable) {
            try {
                firestore?.collection("users")?.document(uid)?.set(user)
            } catch (_: Exception) {}
        }
        return Result.success(user)
    }

    override suspend fun addAdmin(phoneNumber: String, name: String): Result<User> {
        val formattedPhone = if (!phoneNumber.startsWith("+")) "+91${phoneNumber.trim()}" else phoneNumber.trim()
        val existingList = usersFlow.value.toMutableList()

        val cleanInputPhone = phoneNumber.replace("[^0-9]".toRegex(), "")
        val existingIndex = existingList.indexOfFirst {
            it.phoneNumber.replace("[^0-9]".toRegex(), "").endsWith(cleanInputPhone)
        }

        val user = if (existingIndex >= 0) {
            val existing = existingList[existingIndex]
            existing.copy(
                name = if (name.isNotBlank()) name else existing.name,
                role = UserRole.ADMIN
            )
        } else {
            User(
                uid = "admin_${System.currentTimeMillis()}",
                phoneNumber = formattedPhone,
                name = if (name.isNotBlank()) name else "Admin",
                role = UserRole.ADMIN,
                createdAt = System.currentTimeMillis()
            )
        }

        if (existingIndex >= 0) {
            existingList[existingIndex] = user
        } else {
            existingList.add(user)
        }
        usersFlow.value = existingList

        if (isFirebaseAvailable) {
            try {
                firestore?.collection("users")?.document(user.uid)?.set(user)
            } catch (_: Exception) {}
        }
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
        val updated = houseDetail.copy(id = id, updatedAt = System.currentTimeMillis())

        val currentList = housesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            currentList[index] = updated
        } else {
            currentList.add(updated)
        }
        housesFlow.value = currentList

        if (isFirebaseAvailable) {
            try {
                firestore?.collection("houses")?.document(id)?.set(updated)
            } catch (_: Exception) {}
        }
        return Result.success(updated)
    }

    override fun getHouseDetailByOwner(ownerUid: String): Flow<HouseDetail?> {
        return housesFlow.map { list -> list.firstOrNull { it.ownerUid == ownerUid } }
    }

    override fun getApprovedHouses(): Flow<List<HouseDetail>> {
        return housesFlow.map { list -> list.filter { it.approvalStatus == ApprovalStatus.APPROVED } }
    }

    override fun getPendingHouses(): Flow<List<HouseDetail>> {
        return housesFlow.map { list -> list.filter { it.approvalStatus == ApprovalStatus.PENDING } }
    }

    override fun getAllHouses(): Flow<List<HouseDetail>> {
        return housesFlow.asStateFlow()
    }

    override suspend fun updateHouseStatus(houseId: String, status: ApprovalStatus): Result<Unit> {
        val currentList = housesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == houseId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(
                approvalStatus = status,
                updatedAt = System.currentTimeMillis()
            )
            housesFlow.value = currentList
        }
        if (isFirebaseAvailable) {
            try {
                firestore?.collection("houses")?.document(houseId)?.update(
                    "approvalStatus", status.name,
                    "updatedAt", System.currentTimeMillis()
                )
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override fun getPricingRules(): Flow<PricingRules> {
        return pricingRulesFlow.asStateFlow()
    }

    override suspend fun updatePricingRules(rules: PricingRules): Result<Unit> {
        val updated = rules.copy(updatedAt = System.currentTimeMillis())
        pricingRulesFlow.value = updated
        if (isFirebaseAvailable) {
            try {
                firestore?.collection("pricing")?.document("default_rules")?.set(updated)
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun submitPayment(payment: MaintenancePayment): Result<MaintenancePayment> {
        val id = if (payment.id.isBlank()) "pay_${System.currentTimeMillis()}" else payment.id
        val newPayment = payment.copy(id = id, submittedAt = System.currentTimeMillis())

        val currentList = paymentsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            currentList[index] = newPayment
        } else {
            currentList.add(0, newPayment)
        }
        paymentsFlow.value = currentList

        if (isFirebaseAvailable) {
            try {
                firestore?.collection("payments")?.document(id)?.set(newPayment)
            } catch (_: Exception) {}
        }
        return Result.success(newPayment)
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
        val currentList = paymentsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == paymentId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(
                status = PaymentStatus.APPROVED,
                approvedAt = System.currentTimeMillis()
            )
            paymentsFlow.value = currentList
        }
        if (isFirebaseAvailable) {
            try {
                firestore?.collection("payments")?.document(paymentId)?.update(
                    "status", PaymentStatus.APPROVED.name,
                    "approvedAt", System.currentTimeMillis()
                )
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun rejectPayment(paymentId: String): Result<Unit> {
        val currentList = paymentsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == paymentId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(
                status = PaymentStatus.REJECTED
            )
            paymentsFlow.value = currentList
        }
        if (isFirebaseAvailable) {
            try {
                firestore?.collection("payments")?.document(paymentId)?.update(
                    "status", PaymentStatus.REJECTED.name
                )
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    companion object {
        @Volatile
        private var INSTANCE: SanSiriRepositoryImpl? = null

        fun getInstance(context: Context): SanSiriRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SanSiriRepositoryImpl(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
