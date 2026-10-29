package com.anil.sansiri.data.model

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    USER,
    HOUSE_OWNER;

    val isUserOrOwner: Boolean
        get() = this == USER || this == HOUSE_OWNER
}

enum class PropertyType {
    INDIVIDUAL_HOUSE,
    HOUSE_WITH_RENTED_HOMES,
    APARTMENT,
    EMPTY_SITE
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
    RENTED,
    EMPTY_SITE
}

enum class ItemStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED
}

enum class RequestStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED
}
