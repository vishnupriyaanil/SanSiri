package com.anil.sansiri

import android.app.Activity
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.data.repository.SanSiriRepository
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeSanSiriRepository : SanSiriRepository {

    private val superAdmin = User(
        uid = "super_admin_9964532102",
        phoneNumber = "+919964532102",
        name = "San Siri Super Admin",
        role = UserRole.SUPER_ADMIN
    )

    private val currentUserFlow = MutableStateFlow<User?>(null)
    private val usersFlow = MutableStateFlow<List<User>>(
        listOf(
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
                role = UserRole.HOUSE_OWNER
            )
        )
    )
    private val housesFlow = MutableStateFlow<List<HouseDetail>>(emptyList())
    private val pricingRulesFlow = MutableStateFlow(
        PricingRules(
            selfOccupiedMaintenanceRate = 1500.0,
            rentedMaintenanceRate = 2000.0
        )
    )
    private val paymentsFlow = MutableStateFlow<List<MaintenancePayment>>(emptyList())

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

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        if ((verificationId.contains("9964532102") || verificationId == "super_admin_verification") && (code == "12345" || code == "123456")) {
            currentUserFlow.value = superAdmin
            return Result.success(superAdmin)
        }
        val user = User(
            uid = "user_123",
            phoneNumber = "+919876543210",
            name = "Demo User",
            role = UserRole.HOUSE_OWNER
        )
        currentUserFlow.value = user
        return Result.success(user)
    }

    override fun getCurrentUser(): User? = currentUserFlow.value

    override fun getCurrentUserFlow(): Flow<User?> = currentUserFlow.asStateFlow()

    override suspend fun logout() {
        currentUserFlow.value = null
    }

    override suspend fun switchUserRole(newRole: UserRole): Result<User> {
        val current = currentUserFlow.value ?: User(
            uid = when (newRole) {
                UserRole.SUPER_ADMIN -> "super_admin_9964532102"
                UserRole.ADMIN -> "admin_123"
                UserRole.HOUSE_OWNER -> "owner_123"
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
        return Result.success(updated)
    }

    override suspend fun quickDemoLogin(
        role: UserRole,
        phoneNumber: String,
        name: String
    ): Result<User> {
        val uid = when (role) {
            UserRole.SUPER_ADMIN -> "super_admin_9964532102"
            UserRole.ADMIN -> "admin_123"
            UserRole.HOUSE_OWNER -> "owner_123"
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
                name = if (name.isNotBlank()) name else list[idx].name,
                role = UserRole.ADMIN
            )
        } else {
            User(
                uid = "admin_${System.currentTimeMillis()}",
                phoneNumber = formattedPhone,
                name = if (name.isNotBlank()) name else "Admin",
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
        val list = housesFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == houseId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(approvalStatus = status)
            housesFlow.value = list
        }
        return Result.success(Unit)
    }

    override fun getPricingRules(): Flow<PricingRules> = pricingRulesFlow.asStateFlow()

    override suspend fun updatePricingRules(rules: PricingRules): Result<Unit> {
        pricingRulesFlow.value = rules
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
}
