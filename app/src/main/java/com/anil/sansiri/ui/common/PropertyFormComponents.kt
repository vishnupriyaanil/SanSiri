package com.anil.sansiri.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.OccupancyType

val CROSS_STREET_OPTIONS = listOf(
    "1st Cross",
    "2nd Cross",
    "3rd Cross",
    "4th Cross",
    "5th Cross",
    "6th Cross",
    "7th Cross",
    "8th Cross",
    "9th Cross",
    "10th Cross"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossStreetDropdown(
    selectedCross: String,
    onCrossSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Cross Street / Location"
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = if (selectedCross.isNotBlank() && CROSS_STREET_OPTIONS.contains(selectedCross)) {
        selectedCross
    } else if (selectedCross.isNotBlank()) {
        selectedCross
    } else {
        "1st Cross"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CROSS_STREET_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onCrossSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OccupancySelectionSection(
    isSelfOccupied: Boolean,
    isRented: Boolean,
    isApartment: Boolean,
    isEmptySite: Boolean,
    numberOfRentedUnits: Int,
    onOccupancyChange: (
        isSelfOccupied: Boolean,
        isRented: Boolean,
        isApartment: Boolean,
        isEmptySite: Boolean,
        numberOfRentedUnits: Int,
        occupancyType: OccupancyType
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedRentedDropdown by remember { mutableStateOf(false) }
    val rentedUnitOptions = (1..10).map { it.toString() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Occupancy Selection & Building Rules:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Self Occupied Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Self Occupied",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Owner resides in the house",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSelfOccupied,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onOccupancyChange(
                                    true,
                                    isRented,
                                    false,
                                    false,
                                    if (isRented) maxOf(1, numberOfRentedUnits) else 0,
                                    if (isRented) OccupancyType.RENTED else OccupancyType.SELF_OCCUPIED
                                )
                            } else {
                                onOccupancyChange(
                                    false,
                                    false,
                                    isApartment,
                                    isEmptySite,
                                    0,
                                    if (isEmptySite) OccupancyType.EMPTY_SITE else OccupancyType.SELF_OCCUPIED
                                )
                            }
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // 2. Rented Switch (Can ONLY be activated when Self Occupied is ON!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rented",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelfOccupied) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Text(
                            text = if (isSelfOccupied) "Has portion/units rented out in building" else "Can ONLY be activated when Self Occupied is ON!",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelfOccupied) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isRented && isSelfOccupied,
                        enabled = isSelfOccupied,
                        onCheckedChange = { checked ->
                            val newRentedCount = if (numberOfRentedUnits <= 0) 1 else numberOfRentedUnits
                            if (checked) {
                                onOccupancyChange(
                                    true,
                                    true,
                                    false,
                                    false,
                                    newRentedCount,
                                    OccupancyType.RENTED
                                )
                            } else {
                                onOccupancyChange(
                                    true,
                                    false,
                                    false,
                                    false,
                                    0,
                                    OccupancyType.SELF_OCCUPIED
                                )
                            }
                        }
                    )
                }

                // If Rented is ON, show Number of Rented Homes Dropdown (1 to 10)
                if (isRented && isSelfOccupied) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedRentedDropdown,
                        onExpandedChange = { expandedRentedDropdown = !expandedRentedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${if (numberOfRentedUnits > 0) numberOfRentedUnits else 1} Rented Home(s)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Number of Rented Homes in Building") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRentedDropdown) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRentedDropdown,
                            onDismissRequest = { expandedRentedDropdown = false }
                        ) {
                            rentedUnitOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("$option Rented Home(s)") },
                                    onClick = {
                                        val count = option.toIntOrNull() ?: 1
                                        onOccupancyChange(
                                            true,
                                            true,
                                            false,
                                            false,
                                            count,
                                            OccupancyType.RENTED
                                        )
                                        expandedRentedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // 3. Apartment Switch (When activated, automatically turns OFF Self Occupied and Rented)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apartment",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically turns OFF Self Occupied and Rented",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isApartment,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onOccupancyChange(
                                    false,
                                    false,
                                    true,
                                    false,
                                    0,
                                    OccupancyType.SELF_OCCUPIED
                                )
                            } else {
                                onOccupancyChange(
                                    true,
                                    false,
                                    false,
                                    false,
                                    0,
                                    OccupancyType.SELF_OCCUPIED
                                )
                            }
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // 4. Empty Site Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Empty Site",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Vacant property without construction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEmptySite,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onOccupancyChange(
                                    false,
                                    false,
                                    false,
                                    true,
                                    0,
                                    OccupancyType.EMPTY_SITE
                                )
                            } else {
                                onOccupancyChange(
                                    true,
                                    false,
                                    false,
                                    false,
                                    0,
                                    OccupancyType.SELF_OCCUPIED
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
