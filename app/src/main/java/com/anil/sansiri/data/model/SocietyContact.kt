package com.anil.sansiri.data.model

data class SocietyContact(
    val id: String = "",
    val category: String = "",
    val contactPersonName: String = "",
    val phoneNumber: String = "",
    val addedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
