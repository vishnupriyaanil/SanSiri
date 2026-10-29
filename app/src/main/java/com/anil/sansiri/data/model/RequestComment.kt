package com.anil.sansiri.data.model

data class RequestComment(
    val id: String = "",
    val requestId: String = "",
    val userUid: String = "",
    val userName: String = "",
    val userRole: UserRole = UserRole.USER,
    val commentText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
