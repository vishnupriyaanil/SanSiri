package com.anil.sansiri.data.model

data class MaintenancePayment(
    val id: String = "",
    val houseId: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val houseNumber: String = "",
    val monthYear: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val transactionRef: String = "",
    val status: PaymentStatus = PaymentStatus.PENDING,
    val submittedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null
)
