package com.anil.sansiri.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.ui.common.CrossStreetDropdown
import com.anil.sansiri.ui.common.OccupancySelectionSection

data class EditablePropertyState(
    val id: String = "",
    val buildingName: String = "San Siri Residency",
    val houseNumber: String = "",
    val crossStreet: String = "1st Cross",
    val isSelfOccupied: Boolean = true,
    val isRented: Boolean = false,
    val isApartment: Boolean = false,
    val isEmptySite: Boolean = false,
    val numberOfRentedUnits: Int = 0,
    val occupancyType: OccupancyType = OccupancyType.SELF_OCCUPIED,
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVED
)

fun HouseDetail.toEditablePropertyState(): EditablePropertyState {
    val selfOcc = isSelfOccupied || (occupancyType == OccupancyType.SELF_OCCUPIED && !isRented && !isApartment && occupancyType != OccupancyType.EMPTY_SITE) || propertyType == PropertyType.INDIVIDUAL_HOUSE
    val rented = isRented || occupancyType == OccupancyType.RENTED || propertyType == PropertyType.HOUSE_WITH_RENTED_HOMES
    val apt = isApartment || propertyType == PropertyType.APARTMENT
    val empty = isEmptySite || occupancyType == OccupancyType.EMPTY_SITE || propertyType == PropertyType.EMPTY_SITE

    return EditablePropertyState(
        id = id,
        buildingName = buildingName.ifBlank { "San Siri Residency" },
        houseNumber = if (houseNumber.isNotBlank()) houseNumber else if (siteNumber.isNotBlank()) "Site #$siteNumber" else "",
        crossStreet = if (crossStreet.isNotBlank()) crossStreet else "1st Cross",
        isSelfOccupied = selfOcc,
        isRented = rented,
        isApartment = apt,
        isEmptySite = empty,
        numberOfRentedUnits = if (numberOfRentedUnits > 0) numberOfRentedUnits else numberOfRentedHouses,
        occupancyType = occupancyType,
        approvalStatus = approvalStatus
    )
}

fun EditablePropertyState.toHouseDetail(ownerUid: String = "", ownerName: String = "", ownerPhone: String = ""): HouseDetail {
    val propType = when {
        isEmptySite -> PropertyType.EMPTY_SITE
        isApartment -> PropertyType.APARTMENT
        isRented -> PropertyType.HOUSE_WITH_RENTED_HOMES
        else -> PropertyType.INDIVIDUAL_HOUSE
    }
    return HouseDetail(
        id = id,
        ownerUid = ownerUid,
        ownerName = ownerName,
        ownerPhone = ownerPhone,
        propertyType = propType,
        siteNumber = if (houseNumber.contains("Site")) houseNumber.replace("Site #", "").trim() else "",
        buildingName = buildingName.ifBlank { "San Siri Residency" },
        buildingNumber = "",
        floorNumber = "",
        houseNumber = houseNumber.trim(),
        numberOfRentedHouses = if (isRented) maxOf(1, numberOfRentedUnits) else 0,
        numberOfRentedUnits = if (isRented) maxOf(1, numberOfRentedUnits) else 0,
        crossStreet = crossStreet.trim(),
        occupancyType = occupancyType,
        isSelfOccupied = isSelfOccupied,
        isRented = isRented,
        isApartment = isApartment,
        isEmptySite = isEmptySite,
        approvalStatus = approvalStatus,
        updatedAt = System.currentTimeMillis()
    )
}

@Composable
fun UpdatePropertyDialog(
    userHouses: List<HouseDetail> = emptyList(),
    myHouseDetail: HouseDetail? = null,
    onSaveProperties: (List<HouseDetail>) -> Unit,
    onDismiss: () -> Unit
) {
    var propertyList by remember(userHouses, myHouseDetail) {
        val initialList = if (userHouses.isNotEmpty()) {
            userHouses.map { it.toEditablePropertyState() }
        } else if (myHouseDetail != null) {
            listOf(myHouseDetail.toEditablePropertyState())
        } else {
            listOf(EditablePropertyState())
        }
        mutableStateOf(initialList)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HomeWork,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update Property Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Manage all your properties in San Siri Residency. You can edit existing properties or add multiple houses/flats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                propertyList.forEachIndexed { index, prop ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header Row for Property Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Property #${index + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (propertyList.size > 1) {
                                    IconButton(
                                        onClick = {
                                            propertyList = propertyList.toMutableList().apply { removeAt(index) }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Property",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            // 1. Building Name
                            OutlinedTextField(
                                value = prop.buildingName,
                                onValueChange = { newBldg ->
                                    propertyList = propertyList.toMutableList().also { list ->
                                        list[index] = list[index].copy(buildingName = newBldg)
                                    }
                                },
                                label = { Text("Building / Block Name *") },
                                placeholder = { Text("e.g. San Siri Residency") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 2. House / Flat Number
                            OutlinedTextField(
                                value = prop.houseNumber,
                                onValueChange = { newHouse ->
                                    propertyList = propertyList.toMutableList().also { list ->
                                        list[index] = list[index].copy(houseNumber = newHouse)
                                    }
                                },
                                label = { Text("House / Flat Number *") },
                                placeholder = { Text("e.g. A-101, B-201, Plot 12") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 3. Cross Street Dropdown
                            CrossStreetDropdown(
                                selectedCross = prop.crossStreet,
                                onCrossSelected = { newCross ->
                                    propertyList = propertyList.toMutableList().also { list ->
                                        list[index] = list[index].copy(crossStreet = newCross)
                                    }
                                }
                            )

                            // 4. Occupancy Selection Switches
                            OccupancySelectionSection(
                                isSelfOccupied = prop.isSelfOccupied,
                                isRented = prop.isRented,
                                isApartment = prop.isApartment,
                                isEmptySite = prop.isEmptySite,
                                numberOfRentedUnits = prop.numberOfRentedUnits,
                                onOccupancyChange = { isSelf, isRent, isApt, isEmpty, count, occ ->
                                    propertyList = propertyList.toMutableList().also { list ->
                                        list[index] = list[index].copy(
                                            isSelfOccupied = isSelf,
                                            isRented = isRent,
                                            isApartment = isApt,
                                            isEmptySite = isEmpty,
                                            numberOfRentedUnits = count,
                                            occupancyType = occ
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Prominent "+ Add Another Property" Button at the bottom of the list
                OutlinedButton(
                    onClick = {
                        propertyList = propertyList + EditablePropertyState(
                            id = "",
                            buildingName = "San Siri Residency",
                            houseNumber = "",
                            crossStreet = "1st Cross",
                            isSelfOccupied = true,
                            isRented = false,
                            isApartment = false,
                            isEmptySite = false,
                            numberOfRentedUnits = 0,
                            occupancyType = OccupancyType.SELF_OCCUPIED
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Add Another Property",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedHouses = propertyList.map { it.toHouseDetail() }
                    onSaveProperties(updatedHouses)
                },
                enabled = propertyList.isNotEmpty() && propertyList.all { it.houseNumber.isNotBlank() && it.buildingName.isNotBlank() },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Properties")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdatePropertyDialog(
    myHouseDetail: HouseDetail?,
    onSavePropertyDetails: (
        flatNumber: String,
        buildingName: String,
        crossStreet: String,
        occupancyType: OccupancyType
    ) -> Unit,
    onDismiss: () -> Unit
) {
    UpdatePropertyDialog(
        userHouses = emptyList(),
        myHouseDetail = myHouseDetail,
        onSaveProperties = { houses ->
            val first = houses.firstOrNull()
            if (first != null) {
                onSavePropertyDetails(
                    first.houseNumber,
                    first.buildingName,
                    first.crossStreet,
                    first.occupancyType
                )
            }
        },
        onDismiss = onDismiss
    )
}
