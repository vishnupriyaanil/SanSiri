package com.anil.sansiri.ui.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.ui.common.CrossStreetDropdown
import com.anil.sansiri.ui.common.OccupancySelectionSection
import com.anil.sansiri.ui.home.PeoplesDirectoryScreen

/**
 * @deprecated User Management has been merged into PeoplesDirectoryScreen.
 * This file contains shared admin dialogs (`AddNewUserDialog`, `AddPropertyDialog`, `EditUserAndBuildingDetailsDialog`)
 * and a deprecated wrapper.
 */
@Deprecated("User Management has been merged into PeoplesDirectoryScreen")
@Composable
fun UserManagementScreen(
    currentUser: User?,
    allUsers: List<User>,
    allHouses: List<HouseDetail>,
    onUpdateUserRoleAndDetails: (
        targetUid: String,
        name: String,
        houseNumber: String,
        cross: String,
        occupancyType: OccupancyType,
        role: UserRole
    ) -> Unit,
    onAddNewUser: ((name: String, phone: String, flatNumber: String, crossStreet: String, occupancyType: OccupancyType, role: UserRole) -> Unit)? = null,
    onAddPropertyForUser: ((targetUid: String, houseNumber: String, crossStreet: String, occupancyType: OccupancyType, propertyType: PropertyType) -> Unit)? = null,
    onToggleUserActiveStatus: ((targetUid: String, isActive: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PeoplesDirectoryScreen(
        houses = allHouses,
        allUsers = allUsers,
        crossRepresentatives = emptyMap(),
        currentUser = currentUser,
        onAssignCrossRepresentative = { _, _ -> },
        modifier = modifier,
        onUpdateUserRoleAndDetails = onUpdateUserRoleAndDetails,
        onAddNewUser = onAddNewUser,
        onAddPropertyForUser = onAddPropertyForUser,
        onToggleUserActiveStatus = onToggleUserActiveStatus
    )
}

@Composable
fun AddPropertyDialog(
    user: User,
    onAddProperty: (houseNumber: String, crossStreet: String, occupancyType: OccupancyType, propertyType: PropertyType) -> Unit,
    onDismiss: () -> Unit
) {
    var houseNumber by remember { mutableStateOf("") }
    var crossStreet by remember { mutableStateOf("1st Cross") }
    var occupancyType by remember { mutableStateOf(OccupancyType.SELF_OCCUPIED) }
    var propertyType by remember { mutableStateOf(PropertyType.INDIVIDUAL_HOUSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "+ Add Property/House for ${user.name.ifBlank { "User" }}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = houseNumber,
                    onValueChange = { houseNumber = it },
                    label = { Text("Flat / House / Site Number") },
                    placeholder = { Text("e.g. B-201 or Site #45") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                CrossStreetDropdown(
                    selectedCross = crossStreet,
                    onCrossSelected = { crossStreet = it }
                )

                Text(
                    text = "Occupancy Type:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OccupancyType.entries.forEach { type ->
                        val label = when (type) {
                            OccupancyType.SELF_OCCUPIED -> "Self Occupied"
                            OccupancyType.RENTED -> "Rented"
                            OccupancyType.EMPTY_SITE -> "Empty Site"
                        }
                        FilterChip(
                            selected = occupancyType == type,
                            onClick = { occupancyType = type },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (houseNumber.isNotBlank()) {
                        onAddProperty(houseNumber, crossStreet, occupancyType, propertyType)
                    }
                },
                enabled = houseNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A60))
            ) {
                Text("Add Property", fontWeight = FontWeight.Bold)
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
fun AddNewUserDialog(
    isSuperAdmin: Boolean,
    onAddUser: (name: String, phone: String, flatNumber: String, crossStreet: String, occupancyType: OccupancyType, role: UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var flatNumber by remember { mutableStateOf("") }
    var crossStreet by remember { mutableStateOf("1st Cross") }

    var isSelfOccupied by remember { mutableStateOf(true) }
    var isRented by remember { mutableStateOf(false) }
    var isApartment by remember { mutableStateOf(false) }
    var isEmptySite by remember { mutableStateOf(false) }
    var numberOfRentedUnits by remember { mutableStateOf(0) }
    var occupancyType by remember { mutableStateOf(OccupancyType.SELF_OCCUPIED) }

    var selectedRole by remember { mutableStateOf(UserRole.USER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("+ Add New User", fontWeight = FontWeight.Bold, color = Color(0xFF004D40)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Rajesh Kumar") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                    label = { Text("Phone Number (10 digits)") },
                    placeholder = { Text("e.g. 9876543210") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    supportingText = if (phone.isNotEmpty() && phone.length != 10) {
                        { Text("Must be 10 digits", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    isError = phone.isNotEmpty() && phone.length != 10,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = flatNumber,
                    onValueChange = { flatNumber = it },
                    label = { Text("Flat / House Number") },
                    placeholder = { Text("e.g. A-101 or B-202") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                CrossStreetDropdown(
                    selectedCross = crossStreet,
                    onCrossSelected = { crossStreet = it }
                )

                OccupancySelectionSection(
                    isSelfOccupied = isSelfOccupied,
                    isRented = isRented,
                    isApartment = isApartment,
                    isEmptySite = isEmptySite,
                    numberOfRentedUnits = numberOfRentedUnits,
                    onOccupancyChange = { isSelf, isRent, isApt, isEmpty, count, occ ->
                        isSelfOccupied = isSelf
                        isRented = isRent
                        isApartment = isApt
                        isEmptySite = isEmpty
                        numberOfRentedUnits = count
                        occupancyType = occ
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Role Alignment:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedRole == UserRole.USER,
                            onClick = { selectedRole = UserRole.USER },
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("USER", fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.ADMIN,
                            onClick = { selectedRole = UserRole.ADMIN },
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("ADMIN", fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (isSuperAdmin) {
                            FilterChip(
                                selected = selectedRole == UserRole.SUPER_ADMIN,
                                onClick = { selectedRole = UserRole.SUPER_ADMIN },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("SUPER_ADMIN", fontWeight = FontWeight.Bold)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddUser(name, phone, flatNumber, crossStreet, occupancyType, selectedRole) },
                enabled = name.isNotBlank() && phone.length == 10,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A60))
            ) {
                Text("Add User", fontWeight = FontWeight.Bold)
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
fun EditUserAndBuildingDetailsDialog(
    user: User,
    house: HouseDetail?,
    isSuperAdmin: Boolean,
    onSave: (name: String, houseNumber: String, cross: String, occupancyType: OccupancyType, role: UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var houseNumber by remember { mutableStateOf(house?.houseNumber ?: "A-101") }
    var crossStreet by remember { mutableStateOf(house?.crossStreet ?: "1st Cross") }

    var isSelfOccupied by remember { mutableStateOf(house?.isSelfOccupied ?: (house?.occupancyType == OccupancyType.SELF_OCCUPIED || house?.propertyType == PropertyType.INDIVIDUAL_HOUSE)) }
    var isRented by remember { mutableStateOf(house?.isRented ?: (house?.occupancyType == OccupancyType.RENTED || house?.propertyType == PropertyType.HOUSE_WITH_RENTED_HOMES)) }
    var isApartment by remember { mutableStateOf(house?.isApartment ?: (house?.propertyType == PropertyType.APARTMENT)) }
    var isEmptySite by remember { mutableStateOf(house?.occupancyType == OccupancyType.EMPTY_SITE || house?.propertyType == PropertyType.EMPTY_SITE) }
    var numberOfRentedUnits by remember { mutableStateOf(if (house?.numberOfRentedUnits ?: 0 > 0) house?.numberOfRentedUnits ?: 0 else house?.numberOfRentedHouses ?: 0) }
    var occupancyType by remember { mutableStateOf(house?.occupancyType ?: OccupancyType.SELF_OCCUPIED) }

    var selectedRole by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Resident & Role Details", fontWeight = FontWeight.Bold, color = Color(0xFF004D40)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = houseNumber,
                    onValueChange = { houseNumber = it },
                    label = { Text("Flat / House Number") },
                    placeholder = { Text("e.g. A-101, B-202") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                CrossStreetDropdown(
                    selectedCross = crossStreet,
                    onCrossSelected = { crossStreet = it }
                )

                OccupancySelectionSection(
                    isSelfOccupied = isSelfOccupied,
                    isRented = isRented,
                    isApartment = isApartment,
                    isEmptySite = isEmptySite,
                    numberOfRentedUnits = numberOfRentedUnits,
                    onOccupancyChange = { isSelf, isRent, isApt, isEmpty, count, occ ->
                        isSelfOccupied = isSelf
                        isRented = isRent
                        isApartment = isApt
                        isEmptySite = isEmpty
                        numberOfRentedUnits = count
                        occupancyType = occ
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Role Alignment:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedRole == UserRole.USER,
                            onClick = { selectedRole = UserRole.USER },
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("USER", fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.ADMIN,
                            onClick = { selectedRole = UserRole.ADMIN },
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("ADMIN", fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (isSuperAdmin) {
                            FilterChip(
                                selected = selectedRole == UserRole.SUPER_ADMIN,
                                onClick = { selectedRole = UserRole.SUPER_ADMIN },
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("SUPER_ADMIN", fontWeight = FontWeight.Bold)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, houseNumber, crossStreet, occupancyType, selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A60))
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
