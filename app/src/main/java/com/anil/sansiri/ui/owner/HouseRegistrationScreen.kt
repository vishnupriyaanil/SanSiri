package com.anil.sansiri.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.OccupancyType

@Composable
fun HouseRegistrationScreen(
    uiState: HouseOwnerUiState,
    onBuildingNameChanged: (String) -> Unit,
    onHouseNumberChanged: (String) -> Unit,
    onCrossStreetChanged: (String) -> Unit,
    onOccupancyTypeChanged: (OccupancyType) -> Unit,
    onSaveClicked: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status indicator banner
        ApprovalStatusCard(
            status = uiState.houseDetail?.approvalStatus,
            houseNumber = uiState.houseDetail?.houseNumber ?: uiState.houseNumber
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

        // Main Registration Form Card
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
                    text = "House / Flat Registration Form",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Building Name Input
                OutlinedTextField(
                    value = uiState.buildingName,
                    onValueChange = onBuildingNameChanged,
                    label = { Text("Building Name") },
                    placeholder = { Text("San Siri Residency") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationCity, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // House/Flat Number Input
                OutlinedTextField(
                    value = uiState.houseNumber,
                    onValueChange = onHouseNumberChanged,
                    label = { Text("House / Flat Number *") },
                    placeholder = { Text("e.g. A-101, B-204") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.houseNumber.isBlank() && uiState.houseSaveError != null
                )

                // Cross / Street Details
                OutlinedTextField(
                    value = uiState.crossStreet,
                    onValueChange = onCrossStreetChanged,
                    label = { Text("Cross / Street Details") },
                    placeholder = { Text("e.g. 3rd Cross, Main Road") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Occupancy Type Selection
                Text(
                    text = "Occupancy Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val selfRate = uiState.pricingRules.selfOccupiedMaintenanceRate.toInt()
                val rentedRate = uiState.pricingRules.rentedMaintenanceRate.toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OccupancyOptionCard(
                        title = "Self-Occupied",
                        rateText = "₹$selfRate / mo",
                        isSelected = uiState.occupancyType == OccupancyType.SELF_OCCUPIED,
                        onSelect = { onOccupancyTypeChanged(OccupancyType.SELF_OCCUPIED) },
                        modifier = Modifier.weight(1f)
                    )

                    OccupancyOptionCard(
                        title = "Rented",
                        rateText = "₹$rentedRate / mo",
                        isSelected = uiState.occupancyType == OccupancyType.RENTED,
                        onSelect = { onOccupancyTypeChanged(OccupancyType.RENTED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = onSaveClicked,
                    enabled = !uiState.isSavingHouse && uiState.houseNumber.isNotBlank(),
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
                            text = if (uiState.houseDetail == null) "Submit Flat Registration" else "Update Registration Details",
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
    houseNumber: String,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, icon, statusText, noticeText) = when (status) {
        ApprovalStatus.PENDING -> StatusCardConfig(
            containerColor = Color(0xFFFFF8E1),
            contentColor = Color(0xFF8D6E63),
            icon = Icons.Default.Pending,
            statusText = "PENDING APPROVAL",
            noticeText = "Admin notice: Your flat registration details have been submitted and are pending review by the San Siri Residency admin. You can update your details if needed."
        )
        ApprovalStatus.APPROVED -> StatusCardConfig(
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF1B5E20),
            icon = Icons.Default.CheckCircle,
            statusText = "APPROVED",
            noticeText = "Admin notice: Your flat registration has been verified and approved by San Siri Residency admin."
        )
        ApprovalStatus.REJECTED -> StatusCardConfig(
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFB71C1C),
            icon = Icons.Default.Error,
            statusText = "REJECTED",
            noticeText = "Admin notice: Your flat registration request was rejected by Admin. Please double check flat number and details and re-submit."
        )
        null -> StatusCardConfig(
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF0D47A1),
            icon = Icons.Default.Info,
            statusText = "NOT REGISTERED",
            noticeText = "Admin notice: Please enter and submit your flat details below to register your residence with San Siri Residency."
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registration Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
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

            if (houseNumber.isNotBlank()) {
                Text(
                    text = "Registered Flat: $houseNumber",
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
