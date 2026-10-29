package com.anil.sansiri.data.model

data class ResidentRequest(
    val id: String = "",
    val userUid: String = "",
    val userName: String = "",
    val flatNumber: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "REQUEST", // "REQUEST" or "CONCERN"
    val status: RequestStatus = RequestStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
