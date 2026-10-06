package com.anil.sansiri.data.model

enum class UserRole {
    HOUSE_OWNER,
    ADMIN,
    SUPER_ADMIN
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class OccupancyType {
    SELF_OCCUPIED,
    RENTED
}
