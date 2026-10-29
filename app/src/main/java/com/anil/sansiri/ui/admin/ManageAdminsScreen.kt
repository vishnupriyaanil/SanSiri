package com.anil.sansiri.ui.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import androidx.compose.material.icons.filled.Home

@Composable
fun ManageAdminsScreen(
    currentUser: User?,
    admins: List<User>,
    allHouses: List<HouseDetail> = emptyList(),
    newUserName: String = "",
    newUserPhone: String = "",
    newUserFlatNumber: String = "",
    newUserCrossStreet: String = "",
    newUserOccupancyType: OccupancyType = OccupancyType.SELF_OCCUPIED,
    newUserRole: UserRole = UserRole.USER,
    onNameChanged: (String) -> Unit = {},
    onPhoneChanged: (String) -> Unit = {},
    onFlatNumberChanged: (String) -> Unit = {},
    onCrossStreetChanged: (String) -> Unit = {},
    onOccupancyTypeChanged: (OccupancyType) -> Unit = {},
    onRoleChanged: (UserRole) -> Unit = {},
    onAddUser: () -> Unit = {},
    onAddPropertyForUser: ((targetUid: String, houseNumber: String, crossStreet: String, occupancyType: OccupancyType, propertyType: PropertyType) -> Unit)? = null,
    onToggleUserActiveStatus: ((targetUid: String, isActive: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserForAddProperty by remember { mutableStateOf<User?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Full Width Title & Tagline
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "User Directory & Account Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage user profiles, flat details & role permissions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Super Admin Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF004D40)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(0xFF00332C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Super Admin Control",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "SUPER ADMIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = currentUser?.name?.ifBlank { "San Siri Super Admin" } ?: "San Siri Super Admin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Phone: ${currentUser?.phoneNumber?.ifBlank { "+919964532102" } ?: "+919964532102"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Active Admins Section
            item {
                Text(
                    text = "Active Society Admins & Roles (${admins.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            }

            items(admins, key = { it.uid.ifBlank { it.phoneNumber } }) { admin ->
                val adminHouses = allHouses.filter { it.ownerUid == admin.uid }
                AdminUserCard(
                    admin = admin,
                    adminHouses = adminHouses,
                    onAddPropertyClicked = { selectedUserForAddProperty = admin },
                    onToggleActiveStatus = { isActive ->
                        onToggleUserActiveStatus?.invoke(admin.uid, isActive)
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddUserDialog = true },
            containerColor = Color(0xFF006A60),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Add User"
            )
        }
    }

    if (showAddUserDialog) {
        AddNewUserDialog(
            isSuperAdmin = currentUser?.isSuperAdmin == true,
            onAddUser = { name, phone, flat, cross, occupancy, role ->
                onNameChanged(name)
                onPhoneChanged(phone)
                onFlatNumberChanged(flat)
                onCrossStreetChanged(cross)
                onOccupancyTypeChanged(occupancy)
                onRoleChanged(role)
                onAddUser()
                showAddUserDialog = false
            },
            onDismiss = { showAddUserDialog = false }
        )
    }

    if (selectedUserForAddProperty != null) {
        val targetAdmin = selectedUserForAddProperty!!
        AddPropertyDialog(
            user = targetAdmin,
            onAddProperty = { houseNum, cross, occupancy, propType ->
                onAddPropertyForUser?.invoke(targetAdmin.uid, houseNum, cross, occupancy, propType)
                selectedUserForAddProperty = null
            },
            onDismiss = { selectedUserForAddProperty = null }
        )
    }
}

@Composable
fun AdminUserCard(
    admin: User,
    adminHouses: List<HouseDetail> = emptyList(),
    onAddPropertyClicked: (() -> Unit)? = null,
    onToggleActiveStatus: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSuper = admin.role == UserRole.SUPER_ADMIN

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isSuper) Icons.Default.Security else Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = if (isSuper) Color(0xFF006A60) else Color(0xFF4C635E),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = admin.name.ifBlank { "Admin" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = admin.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (adminHouses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        adminHouses.forEach { house ->
                            val occLabel = when (house.occupancyType) {
                                OccupancyType.RENTED -> "Rented"
                                OccupancyType.EMPTY_SITE -> "Empty Site"
                                else -> "Self-Occupied"
                            }
                            Text(
                                text = "Flat ${house.displayHouseOrSiteNumber} ($occLabel)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Temporary Password",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFD84315)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Temp Pass: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Anil@102",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current
                        Surface(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("Anil@102"))
                                Toast.makeText(context, "Password copied: Anil@102", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFBE9E7)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Password",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFFD84315)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Copy",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = if (isSuper) Color(0xFFFFD54F) else Color(0xFFCEE8E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isSuper) "SUPER_ADMIN" else "ADMIN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuper) Color(0xFF1C1B1F) else Color(0xFF08201B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = if (admin.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (admin.isActive) "ACTIVE" else "DEACTIVATED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (admin.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (onAddPropertyClicked != null) {
                    OutlinedButton(
                        onClick = onAddPropertyClicked,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF006A60)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006A60))
                    ) {
                        Text(
                            text = "+ Add Property",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (onToggleActiveStatus != null) {
                    OutlinedButton(
                        onClick = { onToggleActiveStatus(!admin.isActive) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (admin.isActive) Color(0xFFC62828) else Color(0xFF2E7D32)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (admin.isActive) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    ) {
                        Text(
                            text = if (admin.isActive) "Deactivate" else "Activate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
