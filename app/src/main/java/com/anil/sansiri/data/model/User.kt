package com.anil.sansiri.data.model

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val role: UserRole = UserRole.HOUSE_OWNER,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSuperAdmin: Boolean
        get() = role == UserRole.SUPER_ADMIN

    val isAdminOrSuperAdmin: Boolean
        get() = role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN
}
