package com.anil.sansiri.data.model

data class MonthlyHouseStatus(
    val houseId: String = "",
    val houseNumber: String = "",
    val ownerName: String = "",
    val monthYear: String = "",
    val isPaid: Boolean = false,
    val amount: Double = 0.0,
    val paymentDate: String? = null,
    val transactionRef: String? = null,
    val paymentStatus: PaymentStatus = if (isPaid) PaymentStatus.APPROVED else PaymentStatus.PENDING,
    val crossStreet: String = "",
    val paymentId: String? = null
)
