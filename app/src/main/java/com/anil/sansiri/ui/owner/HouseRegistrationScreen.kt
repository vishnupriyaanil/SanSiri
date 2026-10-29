package com.anil.sansiri.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MapsHomeWork
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.ui.common.CrossStreetDropdown
import com.anil.sansiri.ui.common.OccupancySelectionSection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HouseRegistrationScreen(
    uiState: HouseOwnerUiState,
    onPropertyTypeChanged: (PropertyType) -> Unit,
    onSiteNumberChanged: (String) -> Unit,
    onBuildingNameChanged: (String) -> Unit,
    onBuildingNumberChanged: (String) -> Unit,
    onFloorNumberChanged: (String) -> Unit,
    onHouseNumberChanged: (String) -> Unit,
    onRentedHousesCountChanged: (String) -> Unit,
    onCrossStreetChanged: (String) -> Unit,
    onOccupancyTypeChanged: (OccupancyType) -> Unit,
    onOccupancySelectionChanged: ((Boolean, Boolean, Boolean, Boolean, Int, OccupancyType) -> Unit)? = null,
    onSaveClicked: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status indicator banner
        ApprovalStatusCard(
            status = uiState.houseDetail?.approvalStatus,
            displayTitle = uiState.houseDetail?.displayPropertyTitle ?: uiState.registrationStatusMessage
        )

        // Success or error messages
        if (uiState.houseSaveSuccess != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.houseSaveSuccess,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (uiState.houseSaveError != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.houseSaveError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Main Property Registration Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Property Details Registration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Select Property Type
                Text(
                    text = "Select Property Type *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.propertyType == PropertyType.INDIVIDUAL_HOUSE,
                        onClick = { onPropertyTypeChanged(PropertyType.INDIVIDUAL_HOUSE) },
                        label = { Text("Individual House") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )

                    FilterChip(
                        selected = uiState.propertyType == PropertyType.HOUSE_WITH_RENTED_HOMES,
                        onClick = { onPropertyTypeChanged(PropertyType.HOUSE_WITH_RENTED_HOMES) },
                        label = { Text("House with Rented Homes") },
                        leadingIcon = { Icon(Icons.Default.MapsHomeWork, contentDescription = null) }
                    )

                    FilterChip(
                        selected = uiState.propertyType == PropertyType.APARTMENT,
                        onClick = { onPropertyTypeChanged(PropertyType.APARTMENT) },
                        label = { Text("Apartment") },
                        leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) }
                    )

                    FilterChip(
                        selected = uiState.propertyType == PropertyType.EMPTY_SITE,
                        onClick = { onPropertyTypeChanged(PropertyType.EMPTY_SITE) },
                        label = { Text("Empty Site") },
                        leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic Inputs based on selected Property Type
                when (uiState.propertyType) {
                    PropertyType.INDIVIDUAL_HOUSE, PropertyType.EMPTY_SITE -> {
                        OutlinedTextField(
                            value = uiState.siteNumber,
                            onValueChange = onSiteNumberChanged,
                            label = { Text("Site Number *") },
                            placeholder = { Text("e.g. Site #102") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Numbers, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    PropertyType.HOUSE_WITH_RENTED_HOMES -> {
                        OutlinedTextField(
                            value = uiState.siteNumber,
                            onValueChange = onSiteNumberChanged,
                            label = { Text("Site Number *") },
                            placeholder = { Text("e.g. Site #105") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Numbers, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.numberOfRentedHousesInput,
                            onValueChange = onRentedHousesCountChanged,
                            label = { Text("Number of Rented Houses *") },
                            placeholder = { Text("e.g. 2 or 3") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    PropertyType.APARTMENT -> {
                        OutlinedTextField(
                            value = uiState.buildingName,
                            onValueChange = onBuildingNameChanged,
                            label = { Text("Building Name / Complex") },
                            placeholder = { Text("San Siri Residency") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.LocationCity, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.buildingNumber,
                                onValueChange = onBuildingNumberChanged,
                                label = { Text("Bldg No *") },
                                placeholder = { Text("A") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Apartment, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = uiState.floorNumber,
                                onValueChange = onFloorNumberChanged,
                                label = { Text("Floor No *") },
                                placeholder = { Text("2") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Stairs, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = uiState.houseNumber,
                            onValueChange = onHouseNumberChanged,
                            label = { Text("House / Flat Number *") },
                            placeholder = { Text("e.g. 204 or Flat A-204") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Cross / Street Details Dropdown
                CrossStreetDropdown(
                    selectedCross = uiState.crossStreet,
                    onCrossSelected = onCrossStreetChanged
                )

                // Occupancy Selection & Rules
                OccupancySelectionSection(
                    isSelfOccupied = uiState.isSelfOccupied,
                    isRented = uiState.isRented,
                    isApartment = uiState.isApartment,
                    isEmptySite = uiState.occupancyType == OccupancyType.EMPTY_SITE || uiState.propertyType == PropertyType.EMPTY_SITE,
                    numberOfRentedUnits = uiState.numberOfRentedUnits,
                    onOccupancyChange = { isSelf, isRent, isApt, isEmpty, count, occ ->
                        onOccupancySelectionChanged?.invoke(isSelf, isRent, isApt, isEmpty, count, occ)
                            ?: onOccupancyTypeChanged(occ)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = onSaveClicked,
                    enabled = !uiState.isSavingHouse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSavingHouse) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.houseDetail == null) "Submit Property Registration" else "Update Property Details",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalStatusCard(
    status: ApprovalStatus?,
    displayTitle: String,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, icon, statusText, noticeText) = when (status) {
        ApprovalStatus.PENDING -> StatusCardConfig(
            containerColor = Color(0xFFFFF8E1),
            contentColor = Color(0xFF8D6E63),
            icon = Icons.Default.Pending,
            statusText = "PENDING APPROVAL",
            noticeText = "Admin notice: Your property details have been submitted and are pending review by San Siri Residency admin."
        )
        ApprovalStatus.APPROVED -> StatusCardConfig(
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF1B5E20),
            icon = Icons.Default.CheckCircle,
            statusText = "APPROVED",
            noticeText = "Admin notice: Your property registration has been verified and approved."
        )
        ApprovalStatus.REJECTED -> StatusCardConfig(
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFB71C1C),
            icon = Icons.Default.Error,
            statusText = "REJECTED",
            noticeText = "Admin notice: Your property request was rejected by Admin. Please re-check details and update."
        )
        null -> StatusCardConfig(
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF0D47A1),
            icon = Icons.Default.Info,
            statusText = "NOT REGISTERED",
            noticeText = "Admin notice: Please select and enter your property details below to register with San Siri Residency."
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Property Registration Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = contentColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (displayTitle.isNotBlank()) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }

            Text(
                text = noticeText,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
        }
    }
}

private data class StatusCardConfig(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val statusText: String,
    val noticeText: String
)

@Composable
fun OccupancyOptionCard(
    title: String,
    rateText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface

    OutlinedCard(
        modifier = modifier
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rateText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
