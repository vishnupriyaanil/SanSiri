package com.anil.sansiri.data.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class FinancialTransaction(
    val id: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val addedBy: String = ""
) {
    val isIncome: Boolean
        get() = type == TransactionType.INCOME

    val isExpense: Boolean
        get() = type == TransactionType.EXPENSE
}
