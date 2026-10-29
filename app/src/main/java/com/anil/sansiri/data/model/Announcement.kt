package com.anil.sansiri.data.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val postedByName: String = "",
    val authorUid: String = "",
    val authorRole: UserRole = UserRole.ADMIN,
    val createdAt: Long = System.currentTimeMillis()
) {
    val authorName: String
        get() = postedByName.ifBlank { "San Siri Admin" }
}
