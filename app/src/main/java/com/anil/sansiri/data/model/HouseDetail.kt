package com.anil.sansiri.data.model

data class HouseDetail(
    val id: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val buildingName: String = "San Siri Residency",
    val houseNumber: String = "",
    val crossStreet: String = "",
    val occupancyType: OccupancyType = OccupancyType.SELF_OCCUPIED,
    val approvalStatus: ApprovalStatus = ApprovalStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
