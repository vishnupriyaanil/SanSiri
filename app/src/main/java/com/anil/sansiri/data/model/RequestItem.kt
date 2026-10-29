package com.anil.sansiri.data.model

data class RequestItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "General",
    val userUid: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val houseNumber: String = "",
    val status: ItemStatus = ItemStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
