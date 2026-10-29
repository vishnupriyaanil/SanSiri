package com.anil.sansiri.data.model

data class HouseDetail(
    val id: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val propertyType: PropertyType = PropertyType.INDIVIDUAL_HOUSE,
    val siteNumber: String = "",
    val buildingName: String = "San Siri Residency",
    val buildingNumber: String = "",
    val floorNumber: String = "",
    val houseNumber: String = "",
    val numberOfRentedHouses: Int = 0,
    val crossStreet: String = "",
    val occupancyType: OccupancyType = OccupancyType.SELF_OCCUPIED,
    val approvalStatus: ApprovalStatus = ApprovalStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSelfOccupied: Boolean = true,
    val isRented: Boolean = false,
    val numberOfRentedUnits: Int = 0,
    val isApartment: Boolean = false,
    val isEmptySite: Boolean = false
) {
    val displayPropertyTitle: String
        get() = when (propertyType) {
            PropertyType.INDIVIDUAL_HOUSE -> "Individual House (Site #$siteNumber)"
            PropertyType.HOUSE_WITH_RENTED_HOMES -> "House with $numberOfRentedHouses Rented Homes (Site #$siteNumber)"
            PropertyType.APARTMENT -> "Apartment Bldg $buildingNumber, Flr $floorNumber, House #$houseNumber"
            PropertyType.EMPTY_SITE -> "Empty Site #$siteNumber"
        }

    val displayHouseOrSiteNumber: String
        get() = when (propertyType) {
            PropertyType.APARTMENT -> if (houseNumber.isNotBlank()) houseNumber else "Bldg $buildingNumber-$floorNumber"
            else -> if (siteNumber.isNotBlank()) "Site #$siteNumber" else houseNumber
        }
}
