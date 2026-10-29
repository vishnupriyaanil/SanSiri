package com.anil.sansiri.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole

// 1. User Role Management Dialog (Super Admin can create/assign Super Admins from available users list)
@Composable
fun UserRoleManagementDialog(
    currentUser: User?,
    usersList: List<User>,
    onUpdateRole: (String, UserRole) -> Unit,
    onAddAdmin: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newAdminPhone by remember { mutableStateOf("") }
    var newAdminName by remember { mutableStateOf("") }
    val isSuperAdmin = currentUser?.role == UserRole.SUPER_ADMIN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSuperAdmin) "Super Admin User Management" else "Admin User Directory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign roles to registered users in the database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Add Admin input
                OutlinedTextField(
                    value = newAdminPhone,
                    onValueChange = { newValue ->
                        newAdminPhone = newValue.filter { it.isDigit() }.take(10)
                    },
                    label = { Text("Phone Number") },
                    placeholder = { Text("10-digit number") },
                    supportingText = if (newAdminPhone.isNotEmpty() && newAdminPhone.length != 10) {
                        { Text("Phone number must be exactly 10 digits", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    isError = newAdminPhone.isNotEmpty() && newAdminPhone.length != 10,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newAdminName,
                    onValueChange = { newAdminName = it },
                    label = { Text("Name") },
                    placeholder = { Text("Rajesh Kumar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onAddAdmin(newAdminPhone, newAdminName)
                        newAdminPhone = ""
                        newAdminName = ""
                    },
                    enabled = newAdminPhone.length == 10 && newAdminName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add / Upgrade Admin Role")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registered User List (${usersList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usersList, key = { it.uid.ifBlank { it.phoneNumber } }) { user ->
                        UserRoleRow(
                            targetUser = user,
                            currentUser = currentUser,
                            onUpdateRole = onUpdateRole
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun UserRoleRow(
    targetUser: User,
    currentUser: User?,
    onUpdateRole: (String, UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuperAdmin = currentUser?.role == UserRole.SUPER_ADMIN

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = targetUser.name.ifBlank { "User" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = targetUser.phoneNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = when (targetUser.role) {
                        UserRole.SUPER_ADMIN -> MaterialTheme.colorScheme.primary
                        UserRole.ADMIN -> MaterialTheme.colorScheme.secondary
                        UserRole.USER, UserRole.HOUSE_OWNER -> MaterialTheme.colorScheme.tertiary
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = targetUser.role.name,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Role Change Options
            if (isSuperAdmin || (currentUser?.role == UserRole.ADMIN && targetUser.role != UserRole.SUPER_ADMIN)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = targetUser.role == UserRole.HOUSE_OWNER || targetUser.role == UserRole.USER,
                        onClick = { onUpdateRole(targetUser.uid, UserRole.USER) },
                        label = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("USER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = targetUser.role == UserRole.ADMIN,
                        onClick = { onUpdateRole(targetUser.uid, UserRole.ADMIN) },
                        label = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (isSuperAdmin) {
                        FilterChip(
                            selected = targetUser.role == UserRole.SUPER_ADMIN,
                            onClick = { onUpdateRole(targetUser.uid, UserRole.SUPER_ADMIN) },
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("SUPER ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// 2. Post Announcement Dialog
@Composable
fun CreateAnnouncementDialog(
    onPostAnnouncement: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Society Announcement", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title *") },
                    placeholder = { Text("e.g. Annual Society Meeting") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Announcement Details *") },
                    placeholder = { Text("Enter full details here...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPostAnnouncement(title, content) },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Publish Announcement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 3. Create Request Dialog
@Composable
fun CreateRequestDialog(
    onRaiseRequest: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("NOC / Document") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise Society Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Request Title *") },
                    placeholder = { Text("e.g. Need Gate Pass / NOC") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Maintenance / Gate Pass / Parking") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Request Details *") },
                    placeholder = { Text("Explain request details...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRaiseRequest(title, description, category) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 4. Create Complaint Dialog
@Composable
fun CreateComplaintDialog(
    onRaiseComplaint: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise Society Complaint", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Complaint Title *") },
                    placeholder = { Text("e.g. Water Leakage / Elevator Noise") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Complaint Details *") },
                    placeholder = { Text("Describe the issue in detail...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRaiseComplaint(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Submit Complaint")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 5. Pay Maintenance Dialog
@Composable
fun PayMaintenanceDialog(
    pricingRules: PricingRules,
    myHouseDetail: HouseDetail?,
    onSubmitPayment: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var monthYear by remember { mutableStateOf("May 2025") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var transactionRef by remember { mutableStateOf("") }

    val calculatedAmount = when (myHouseDetail?.propertyType) {
        PropertyType.INDIVIDUAL_HOUSE -> pricingRules.individualHouseRate
        PropertyType.HOUSE_WITH_RENTED_HOMES -> pricingRules.rentedHouseRate * maxOf(1, myHouseDetail.numberOfRentedHouses)
        PropertyType.APARTMENT -> pricingRules.apartmentRate
        PropertyType.EMPTY_SITE -> pricingRules.emptySiteRate
        null -> pricingRules.individualHouseRate
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay Maintenance Fee", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Auto-Calculated Maintenance Amount (${myHouseDetail?.propertyType?.name ?: "Standard"}):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "₹${calculatedAmount.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Month & Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method (UPI / GPay / NetBanking)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Transaction Reference ID / Note *") },
                    placeholder = { Text("e.g. UPI1234567890") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitPayment(monthYear, paymentMethod, transactionRef) },
                enabled = transactionRef.isNotBlank()
            ) {
                Text("Submit Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 6. Property Approvals Dialog
@Composable
fun PropertyApprovalsDialog(
    pendingHouses: List<HouseDetail>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Property Registration Approvals (${pendingHouses.size})", fontWeight = FontWeight.Bold) },
        text = {
            if (pendingHouses.isEmpty()) {
                Text("No pending property registrations to review.")
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pendingHouses, key = { it.id }) { house ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = house.displayPropertyTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Owner: ${house.ownerName} (${house.ownerPhone})",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onReject(house.id) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Reject") }

                                    Button(
                                        onClick = { onApprove(house.id) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Approve") }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// 7. Pricing Rules Config Dialog (Super Admin / Admin sets maintenance rates for each property type)
@Composable
fun PricingRulesDialog(
    pricingRules: PricingRules,
    onSavePricingRules: (Double, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var individualRate by remember { mutableStateOf(pricingRules.individualHouseRate.toInt().toString()) }
    var rentedRate by remember { mutableStateOf(pricingRules.rentedHouseRate.toInt().toString()) }
    var apartmentRate by remember { mutableStateOf(pricingRules.apartmentRate.toInt().toString()) }
    var emptySiteRate by remember { mutableStateOf(pricingRules.emptySiteRate.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Super Admin: Set Maintenance Rates", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Set monthly maintenance fees for each property type. The app will auto-calculate fees accordingly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = individualRate,
                    onValueChange = { individualRate = it },
                    label = { Text("Individual House Rate (₹/mo)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rentedRate,
                    onValueChange = { rentedRate = it },
                    label = { Text("Rented House Rate per Unit (₹/mo)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apartmentRate,
                    onValueChange = { apartmentRate = it },
                    label = { Text("Apartment Unit Rate (₹/mo)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emptySiteRate,
                    onValueChange = { emptySiteRate = it },
                    label = { Text("Empty Site Rate (₹/mo)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ind = individualRate.toDoubleOrNull() ?: 1500.0
                    val rent = rentedRate.toDoubleOrNull() ?: 2000.0
                    val apt = apartmentRate.toDoubleOrNull() ?: 1200.0
                    val site = emptySiteRate.toDoubleOrNull() ?: 500.0
                    onSavePricingRules(ind, rent, apt, site)
                }
            ) {
                Text("Save Rates")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
