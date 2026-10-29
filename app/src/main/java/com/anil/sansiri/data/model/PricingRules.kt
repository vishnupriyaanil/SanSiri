package com.anil.sansiri.data.model

data class PricingRules(
    val id: String = "default_rules",
    val individualHouseRate: Double = 1500.0,
    val rentedHouseRate: Double = 2000.0,
    val apartmentRate: Double = 1200.0,
    val emptySiteRate: Double = 500.0,
    val selfOccupiedMaintenanceRate: Double = 1500.0,
    val rentedMaintenanceRate: Double = 2000.0,
    val emptySiteMaintenanceRate: Double = 500.0,
    val updatedAt: Long = System.currentTimeMillis()
)
