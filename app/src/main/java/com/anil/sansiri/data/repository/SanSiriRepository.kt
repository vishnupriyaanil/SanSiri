package com.anil.sansiri.data.repository

import android.app.Activity
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface SanSiriRepository {
    // Authentication
    fun signInWithPhone(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    fun getCurrentUser(): User?
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun logout()
    suspend fun switchUserRole(newRole: UserRole): Result<User>
    suspend fun quickDemoLogin(
        role: UserRole,
        phoneNumber: String = "+919876543210",
        name: String = "Demo User"
    ): Result<User>

    // Admin & Super Admin Management
    suspend fun addAdmin(phoneNumber: String, name: String): Result<User>
    fun getAdmins(): Flow<List<User>>
    fun getAdminUsers(): Flow<List<User>> = getAdmins()

    // House details CRUD
    suspend fun saveHouseDetail(houseDetail: HouseDetail): Result<HouseDetail>
    fun getHouseDetailByOwner(ownerUid: String): Flow<HouseDetail?>
    fun getApprovedHouses(): Flow<List<HouseDetail>>
    fun getPendingHouses(): Flow<List<HouseDetail>>
    fun getAllHouses(): Flow<List<HouseDetail>>
    suspend fun updateHouseStatus(houseId: String, status: ApprovalStatus): Result<Unit>

    // Pricing rules
    fun getPricingRules(): Flow<PricingRules>
    suspend fun updatePricingRules(rules: PricingRules): Result<Unit>

    // Maintenance Fee Helper
    fun calculateMaintenanceFee(occupancyType: OccupancyType, rules: PricingRules): Double {
        return when (occupancyType) {
            OccupancyType.SELF_OCCUPIED -> rules.selfOccupiedMaintenanceRate
            OccupancyType.RENTED -> rules.rentedMaintenanceRate
        }
    }

    // Payments
    suspend fun submitPayment(payment: MaintenancePayment): Result<MaintenancePayment>
    fun getPaymentsByOwner(ownerUid: String): Flow<List<MaintenancePayment>>
    fun getPendingPayments(): Flow<List<MaintenancePayment>>
    fun getAllPayments(): Flow<List<MaintenancePayment>>
    suspend fun approvePayment(paymentId: String): Result<Unit>
    suspend fun rejectPayment(paymentId: String): Result<Unit>
}
