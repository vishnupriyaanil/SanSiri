package com.anil.sansiri.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.SocietyContact
import com.anil.sansiri.data.model.User

@Composable
fun SocietyContactsScreen(
    contacts: List<SocietyContact>,
    currentUser: User?,
    onAddContact: (category: String, name: String, phone: String) -> Unit,
    onUpdateContact: (contact: SocietyContact) -> Unit,
    onDeleteContact: (contactId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdmin = currentUser?.isAdminOrSuperAdmin == true
    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<SocietyContact?>(null) }
    var contactToDelete by remember { mutableStateOf<SocietyContact?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (contacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = "Contacts",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Society Contacts Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap '+ Add Contact' below to add important contacts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Society Emergency Contacts (${contacts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(contacts, key = { it.id }) { contact ->
                    SocietyContactCard(
                        contact = contact,
                        isAdmin = isAdmin,
                        onCall = {
                            val phone = contact.phoneNumber.trim()
                            if (phone.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                        },
                        onEdit = { contactToEdit = contact },
                        onDelete = { contactToDelete = contact }
                    )
                }
            }
        }

        if (isAdmin) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                containerColor = Color(0xFF006A60),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    }

    if (showAddDialog) {
        AddSocietyContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { category, name, phone ->
                onAddContact(category, name, phone)
                showAddDialog = false
            }
        )
    }

    if (contactToEdit != null) {
        EditSocietyContactDialog(
            contact = contactToEdit!!,
            onDismiss = { contactToEdit = null },
            onConfirm = { updatedContact ->
                onUpdateContact(updatedContact)
                contactToEdit = null
            }
        )
    }

    if (contactToDelete != null) {
        val contact = contactToDelete!!
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            title = { Text("Delete Contact", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${contact.contactPersonName}' (${contact.category})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteContact(contact.id)
                        contactToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getCategoryEmoji(category: String): String {
    val lower = category.lowercase()
    return when {
        lower.contains("electric") || lower.contains("power") -> "⚡"
        lower.contains("garbage") || lower.contains("waste") || lower.contains("clean") || lower.contains("sanitation") -> "🗑️"
        lower.contains("plumb") || lower.contains("water") || lower.contains("pipe") -> "🔧"
        lower.contains("secur") || lower.contains("guard") || lower.contains("gate") -> "🛡️"
        lower.contains("lift") || lower.contains("elevator") -> "🛗"
        lower.contains("internet") || lower.contains("wifi") || lower.contains("cable") -> "📶"
        lower.contains("emergency") || lower.contains("police") || lower.contains("fire") || lower.contains("hospital") -> "🚨"
        else -> "📞"
    }
}

private fun formatPhoneNumber(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.length == 10 -> "+91 $digits"
        digits.length == 12 && digits.startsWith("91") -> "+91 ${digits.substring(2)}"
        else -> if (phone.startsWith("+")) phone else "+91 $phone"
    }
}

@Composable
private fun SocietyContactCard(
    contact: SocietyContact,
    isAdmin: Boolean,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Accent Header Bar with Category Name & Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF006A60))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getCategoryEmoji(contact.category),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = contact.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (isAdmin) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Contact",
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Contact",
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Card Body with Contact Person Name, Formatted Phone, and Call Now Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = contact.contactPersonName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatPhoneNumber(contact.phoneNumber),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Prominent "Call Now" Button
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006A60),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Now",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSocietyContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, name: String, phone: String) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var contactPersonName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Society Contact",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        errorMessage = null
                    },
                    label = { Text("Category Name (e.g., Electrical Issues)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactPersonName,
                    onValueChange = {
                        contactPersonName = it
                        errorMessage = null
                    },
                    label = { Text("Contact Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        phoneNumber = input.filter { it.isDigit() }.take(10)
                        errorMessage = null
                    },
                    label = { Text("Phone Number (10 Digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedCategory = category.trim()
                    val trimmedName = contactPersonName.trim()
                    val trimmedPhone = phoneNumber.trim()

                    if (trimmedCategory.isEmpty()) {
                        errorMessage = "Please enter category name"
                        return@Button
                    }
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Please enter contact person name"
                        return@Button
                    }
                    if (trimmedPhone.length != 10) {
                        errorMessage = "Phone number must be exactly 10 digits"
                        return@Button
                    }

                    onConfirm(trimmedCategory, trimmedName, trimmedPhone)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006A60),
                    contentColor = Color.White
                )
            ) {
                Text("Add Contact")
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
private fun EditSocietyContactDialog(
    contact: SocietyContact,
    onDismiss: () -> Unit,
    onConfirm: (updatedContact: SocietyContact) -> Unit
) {
    var category by remember(contact) { mutableStateOf(contact.category) }
    var contactPersonName by remember(contact) { mutableStateOf(contact.contactPersonName) }
    var phoneNumber by remember(contact) { mutableStateOf(contact.phoneNumber.filter { it.isDigit() }.takeLast(10)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Society Contact",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        errorMessage = null
                    },
                    label = { Text("Category Name (e.g., Electrical Issues)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactPersonName,
                    onValueChange = {
                        contactPersonName = it
                        errorMessage = null
                    },
                    label = { Text("Contact Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        phoneNumber = input.filter { it.isDigit() }.take(10)
                        errorMessage = null
                    },
                    label = { Text("Phone Number (10 Digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedCategory = category.trim()
                    val trimmedName = contactPersonName.trim()
                    val trimmedPhone = phoneNumber.trim()

                    if (trimmedCategory.isEmpty()) {
                        errorMessage = "Please enter category name"
                        return@Button
                    }
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Please enter contact person name"
                        return@Button
                    }
                    if (trimmedPhone.length != 10) {
                        errorMessage = "Phone number must be exactly 10 digits"
                        return@Button
                    }

                    val updatedContact = contact.copy(
                        category = trimmedCategory,
                        contactPersonName = trimmedName,
                        phoneNumber = trimmedPhone
                    )
                    onConfirm(updatedContact)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006A60),
                    contentColor = Color.White
                )
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
