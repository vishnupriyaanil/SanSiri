package com.anil.sansiri.data.model

data class PricingRules(
    val id: String = "default_rules",
    val selfOccupiedMaintenanceRate: Double = 1500.0,
    val rentedMaintenanceRate: Double = 2000.0,
    val updatedAt: Long = System.currentTimeMillis()
)
