package com.anil.sansiri.data.model

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val isPreRegisteredByAdmin: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSuperAdmin: Boolean
        get() = role == UserRole.SUPER_ADMIN

    val isAdminOrSuperAdmin: Boolean
        get() = role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN

    val isUserOrOwner: Boolean
        get() = role == UserRole.USER || role == UserRole.HOUSE_OWNER
}
