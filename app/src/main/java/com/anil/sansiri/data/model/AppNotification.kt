package com.anil.sansiri.data.model

enum class NotificationType {
    COMMENT,
    REQUEST,
    CONCERN,
    ANNOUNCEMENT,
    PAYMENT,
    GENERAL
}

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val targetUid: String = "ALL",
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
