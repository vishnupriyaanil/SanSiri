package com.anil.sansiri.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.ui.admin.AddNewUserDialog
import com.anil.sansiri.ui.admin.AddPropertyDialog
import com.anil.sansiri.ui.admin.EditUserAndBuildingDetailsDialog

@Composable
fun PeoplesDirectoryScreen(
    houses: List<HouseDetail>,
    allUsers: List<User>,
    crossRepresentatives: Map<String, String>, // crossName -> userUid
    currentUser: User?,
    onAssignCrossRepresentative: (cross: String, userUid: String) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateUserRoleAndDetails: ((targetUid: String, name: String, houseNumber: String, cross: String, occupancyType: OccupancyType, role: UserRole) -> Unit)? = null,
    onAddNewUser: ((name: String, phone: String, flatNumber: String, crossStreet: String, occupancyType: OccupancyType, role: UserRole) -> Unit)? = null,
    onAddPropertyForUser: ((targetUid: String, houseNumber: String, crossStreet: String, occupancyType: OccupancyType, propertyType: PropertyType) -> Unit)? = null,
    onToggleUserActiveStatus: ((targetUid: String, isActive: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val isAdmin = currentUser?.isAdminOrSuperAdmin == true
    val isSuperAdmin = currentUser?.isSuperAdmin == true

    var searchQuery by remember { mutableStateOf("") }
    var selectedCrossFilter by remember { mutableStateOf("All Crosses") }
    var representativesOnly by remember { mutableStateOf(false) }
    var sortByCross by remember { mutableStateOf(true) } // true: Sort by Cross, false: Sort by Name (A-Z)
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    val isFiltered = selectedCrossFilter != "All Crosses" || representativesOnly || !sortByCross

    var selectedCrossForAssign by remember { mutableStateOf<String?>(null) }
    var userAndHouseToEdit by remember { mutableStateOf<Pair<User, HouseDetail?>?>(null) }
    var selectedUserForAddProperty by remember { mutableStateOf<User?>(null) }

    // Build standard 1st Cross ... 10th Cross + any additional cross in houses
    val standardCrosses = remember {
        listOf(
            "1st Cross", "2nd Cross", "3rd Cross", "4th Cross", "5th Cross",
            "6th Cross", "7th Cross", "8th Cross", "9th Cross", "10th Cross"
        )
    }

    val houseCrosses = remember(houses) {
        houses.map { it.crossStreet.trim() }.filter { it.isNotBlank() }.distinct()
    }

    val allCrosses = remember(standardCrosses, houseCrosses) {
        (standardCrosses + houseCrosses).distinct()
    }

    val filterCrossOptions = remember(allCrosses) {
        listOf("All Crosses") + allCrosses
    }

    // Function to check if a cross has a representative and return their UID
    val getRepUid = { cross: String ->
        crossRepresentatives[cross]
    }

    // Filter users based on search query, selected cross filter, and representatives only
    val filteredUsers = remember(allUsers, houses, searchQuery, selectedCrossFilter, representativesOnly, crossRepresentatives) {
        allUsers.filter { user ->
            val userHouses = houses.filter { it.ownerUid == user.uid }
            val primaryCross = userHouses.firstOrNull()?.crossStreet?.trim()?.ifBlank { "1st Cross" } ?: "1st Cross"
            val userCrosses = if (userHouses.isEmpty()) listOf(primaryCross) else userHouses.map { it.crossStreet.trim().ifBlank { "1st Cross" } }

            // 1. Search Query Filter
            val matchesSearch = searchQuery.isBlank() ||
                    user.name.contains(searchQuery, ignoreCase = true) ||
                    user.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                    userHouses.any { it.houseNumber.contains(searchQuery, ignoreCase = true) } ||
                    userCrosses.any { it.contains(searchQuery, ignoreCase = true) }

            // 2. Cross Filter
            val matchesCross = selectedCrossFilter == "All Crosses" ||
                    userCrosses.any { it.equals(selectedCrossFilter, ignoreCase = true) }

            // 3. Representatives Only Filter
            val isRep = userCrosses.any { c -> user.uid == getRepUid(c) }
            val matchesRep = !representativesOnly || isRep

            matchesSearch && matchesCross && matchesRep
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Title and Counter Badge directly below in a Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Peoples Directory",
                        tint = Color(0xFF006A60),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Peoples Directory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = Color(0xFF006A60).copy(alpha = 0.12f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF006A60).copy(alpha = 0.3f)),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Total Residents: ${filteredUsers.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006A60),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Search bar & Filter Icon Button with Marquee
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search resident by name, flat #, phone...",
                            modifier = Modifier.basicMarquee(),
                            maxLines = 1
                        )
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                IconButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Directory",
                        tint = if (isFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compact Active Filter Summary Badge / Chip
            if (isFiltered) {
                val activeFilterLabels = mutableListOf<String>()
                if (selectedCrossFilter != "All Crosses") activeFilterLabels.add(selectedCrossFilter)
                if (representativesOnly) activeFilterLabels.add("🌟 Reps Only")
                if (!sortByCross) activeFilterLabels.add("Name (A-Z)")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { showFilterDialog = true },
                        label = { Text("Filter: " + activeFilterLabels.joinToString(" • ")) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Filter",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        selectedCrossFilter = "All Crosses"
                                        representativesOnly = false
                                        sortByCross = true
                                    }
                            )
                        }
                    )
                }
            }

            if (showFilterDialog) {
                PeoplesFilterDialog(
                    currentCrossFilter = selectedCrossFilter,
                    currentRepresentativesOnly = representativesOnly,
                    currentSortByCross = sortByCross,
                    filterCrossOptions = filterCrossOptions,
                    onDismissRequest = { showFilterDialog = false },
                    onApply = { cross, repsOnly, sortCross ->
                        selectedCrossFilter = cross
                        representativesOnly = repsOnly
                        sortByCross = sortCross
                    },
                    onReset = {
                        selectedCrossFilter = "All Crosses"
                        representativesOnly = false
                        sortByCross = true
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (sortByCross) {
                    // Mode 1: Grouped & Sorted by Cross
                    val crossesToDisplay = if (selectedCrossFilter == "All Crosses") allCrosses else listOf(selectedCrossFilter)

                    crossesToDisplay.forEach { cross ->
                        val usersInCross = filteredUsers.filter { user ->
                            val userHouses = houses.filter { it.ownerUid == user.uid }
                            if (userHouses.isEmpty()) {
                                cross.equals("1st Cross", ignoreCase = true)
                            } else {
                                userHouses.any { it.crossStreet.trim().equals(cross, ignoreCase = true) }
                            }
                        }.sortedBy { it.name }

                        val shouldShow = usersInCross.isNotEmpty()

                        if (shouldShow) {
                            item(key = "header_$cross") {
                                CrossHeader(
                                    crossName = cross,
                                    isAdmin = isAdmin,
                                    onAssignRepClicked = { selectedCrossForAssign = cross }
                                )
                            }

                            items(usersInCross, key = { "res_${cross}_${it.uid}" }) { targetUser ->
                                val userHouses = houses.filter { it.ownerUid == targetUser.uid }
                                val pCross = userHouses.firstOrNull()?.crossStreet?.trim()?.ifBlank { cross } ?: cross
                                val isRep = targetUser.uid == getRepUid(cross)

                                ResidentItemCard(
                                    user = targetUser,
                                    userHouses = userHouses,
                                    isRepresentative = isRep,
                                    primaryCross = pCross,
                                    isAdmin = isAdmin,
                                    onCall = {
                                        if (targetUser.phoneNumber.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${targetUser.phoneNumber}"))
                                            context.startActivity(intent)
                                        }
                                    },
                                    onEdit = {
                                        userAndHouseToEdit = Pair(targetUser, userHouses.firstOrNull())
                                    },
                                    onAddProperty = {
                                        selectedUserForAddProperty = targetUser
                                    },
                                    onAssignRep = {
                                        selectedCrossForAssign = cross
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Mode 2: Sorted by Name (A-Z)
                    val sortedByNameUsers = filteredUsers.sortedBy { it.name.ifBlank { "Unnamed User" }.lowercase() }

                    if (sortedByNameUsers.isEmpty()) {
                        item(key = "empty_az") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No residents found matching your selected filter options.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(sortedByNameUsers, key = { "az_${it.uid}" }) { targetUser ->
                            val userHouses = houses.filter { it.ownerUid == targetUser.uid }
                            val pCross = userHouses.firstOrNull()?.crossStreet?.trim()?.ifBlank { "1st Cross" } ?: "1st Cross"
                            val isRep = getRepUid(pCross) == targetUser.uid

                            ResidentItemCard(
                                user = targetUser,
                                userHouses = userHouses,
                                isRepresentative = isRep,
                                primaryCross = pCross,
                                isAdmin = isAdmin,
                                onCall = {
                                    if (targetUser.phoneNumber.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${targetUser.phoneNumber}"))
                                        context.startActivity(intent)
                                    }
                                },
                                onEdit = {
                                    userAndHouseToEdit = Pair(targetUser, userHouses.firstOrNull())
                                },
                                onAddProperty = {
                                    selectedUserForAddProperty = targetUser
                                },
                                onAssignRep = {
                                    selectedCrossForAssign = pCross
                                }
                            )
                        }
                    }
                }
            } // Closes LazyColumn
        }

        // FAB for Admin & Super Admin to Add New User
        if (isAdmin) {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                shape = CircleShape,
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
    }

    // Dialogs
    if (selectedCrossForAssign != null) {
        val crossToAssign = selectedCrossForAssign!!
        AssignCrossRepDialog(
            crossName = crossToAssign,
            allUsers = allUsers,
            houses = houses,
            currentRepUid = crossRepresentatives[crossToAssign],
            onConfirmAssign = { userUid ->
                onAssignCrossRepresentative(crossToAssign, userUid)
                selectedCrossForAssign = null
            },
            onDismiss = { selectedCrossForAssign = null }
        )
    }

    if (userAndHouseToEdit != null) {
        val (targetUser, targetHouse) = userAndHouseToEdit!!
        EditUserAndBuildingDetailsDialog(
            user = targetUser,
            house = targetHouse,
            isSuperAdmin = isSuperAdmin,
            onSave = { name, houseNumber, cross, occupancyType, role ->
                onUpdateUserRoleAndDetails?.invoke(
                    targetUser.uid,
                    name,
                    houseNumber,
                    cross,
                    occupancyType,
                    role
                )
                userAndHouseToEdit = null
            },
            onDismiss = { userAndHouseToEdit = null }
        )
    }

    if (showAddUserDialog) {
        AddNewUserDialog(
            isSuperAdmin = isSuperAdmin,
            onAddUser = { name, phone, flatNumber, crossStreet, occupancyType, selectedRole ->
                onAddNewUser?.invoke(
                    name,
                    phone,
                    flatNumber,
                    crossStreet,
                    occupancyType,
                    selectedRole
                )
                showAddUserDialog = false
            },
            onDismiss = { showAddUserDialog = false }
        )
    }

    if (selectedUserForAddProperty != null) {
        val targetUser = selectedUserForAddProperty!!
        AddPropertyDialog(
            user = targetUser,
            onAddProperty = { houseNum, cross, occupancy, propType ->
                onAddPropertyForUser?.invoke(targetUser.uid, houseNum, cross, occupancy, propType)
                selectedUserForAddProperty = null
            },
            onDismiss = { selectedUserForAddProperty = null }
        )
    }
}

@Composable
private fun CrossHeader(
    crossName: String,
    isAdmin: Boolean,
    onAssignRepClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF006A60),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = crossName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006A60)
            )
        }

        if (isAdmin) {
            TextButton(
                onClick = onAssignRepClicked,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Assign Representative",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF006A60)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Assign Rep",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006A60)
                )
            }
        }
    }
}

@Composable
private fun ResidentItemCard(
    user: User,
    userHouses: List<HouseDetail>,
    isRepresentative: Boolean,
    primaryCross: String,
    isAdmin: Boolean,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onAddProperty: () -> Unit,
    onAssignRep: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Name, Role Badge, Active Badge, Rep Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = user.name.ifBlank { "Resident" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Role Badge
                        val (roleLabel, roleBg, roleText) = when (user.role) {
                            UserRole.SUPER_ADMIN -> Triple("SUPER ADMIN", Color(0xFFFFD54F), Color(0xFF1C1B1F))
                            UserRole.ADMIN -> Triple("ADMIN", Color(0xFFCEE8E2), Color(0xFF08201B))
                            UserRole.USER, UserRole.HOUSE_OWNER -> Triple("USER", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        }
                        Surface(color = roleBg, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = roleText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Active / Deactivated Badge
                        Surface(
                            color = if (user.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (user.isActive) "ACTIVE" else "INACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isRepresentative) {
                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🌟 Rep",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Phone & Cross
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.phoneNumber.ifBlank { "N/A" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = Color(0xFFE0F2F1),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF006A60).copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = "🚏 $primaryCross",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A60),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            // Properties / Houses owned by user
            if (userHouses.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "No property linked yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Properties (${userHouses.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006A60)
                    )
                    userHouses.forEach { house ->
                        val occLabel = when (house.occupancyType) {
                            OccupancyType.RENTED -> "Rented"
                            OccupancyType.EMPTY_SITE -> "Empty Site"
                            else -> "Self-Occupied"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF006A60)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Flat ${house.displayHouseOrSiteNumber} (${house.crossStreet}) • $occLabel",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Action Row for Admins & Super Admins
            if (isAdmin) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onAssignRep,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF006A60)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006A60))
                    ) {
                        Text("🌟 Assign Rep", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onAddProperty,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF006A60)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006A60))
                    ) {
                        Text("🏠 + Add Property", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF006A60)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006A60))
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Edit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    if (user.phoneNumber.isNotBlank()) {
                        OutlinedButton(
                            onClick = onCall,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF006A60)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006A60))
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF006A60), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF006A60))
                        }
                    }
                }
            } else {
                if (user.phoneNumber.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onCall,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF006A60)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006A60))
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF006A60))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", style = MaterialTheme.typography.labelSmall, color = Color(0xFF006A60), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignCrossRepDialog(
    crossName: String,
    allUsers: List<User>,
    houses: List<HouseDetail>,
    currentRepUid: String?,
    onConfirmAssign: (userUid: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUserUid by remember { mutableStateOf(currentRepUid ?: allUsers.firstOrNull()?.uid ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Assign Representative for $crossName",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select a resident to highlight as the Cross Representative for $crossName:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allUsers, key = { it.uid }) { user ->
                        val house = houses.find { it.ownerUid == user.uid }
                        val isSelected = selectedUserUid == user.uid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) Color(0xFFCEE8E2) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedUserUid = user.uid }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = user.name.ifBlank { "Resident" },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Flat ${house?.displayHouseOrSiteNumber ?: "N/A"} • ${user.phoneNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmAssign(selectedUserUid) },
                enabled = selectedUserUid.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A60))
            ) {
                Text("Confirm Assignment")
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
fun PeoplesFilterDialog(
    currentCrossFilter: String,
    currentRepresentativesOnly: Boolean,
    currentSortByCross: Boolean,
    filterCrossOptions: List<String>,
    onDismissRequest: () -> Unit,
    onApply: (cross: String, repsOnly: Boolean, sortByCross: Boolean) -> Unit,
    onReset: () -> Unit
) {
    var tempCross by remember { mutableStateOf(currentCrossFilter) }
    var tempRepsOnly by remember { mutableStateOf(currentRepresentativesOnly) }
    var tempSortByCross by remember { mutableStateOf(currentSortByCross) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
        title = { Text("Filter & Sort Directory") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cross Filter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cross Filter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    var crossDropdownExpanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { crossDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tempCross, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = crossDropdownExpanded,
                            onDismissRequest = { crossDropdownExpanded = false }
                        ) {
                            filterCrossOptions.forEach { crossOpt ->
                                DropdownMenuItem(
                                    text = { Text(crossOpt) },
                                    onClick = {
                                        tempCross = crossOpt
                                        crossDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Cross Representatives Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tempRepsOnly = !tempRepsOnly }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌟 Representatives Only", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = tempRepsOnly,
                        onCheckedChange = { tempRepsOnly = it }
                    )
                }

                // Sorting Option
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sorting", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSortByCross = true }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = tempSortByCross,
                            onClick = { tempSortByCross = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sort by Cross")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSortByCross = false }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = !tempSortByCross,
                            onClick = { tempSortByCross = false }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sort by Name (A-Z)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(tempCross, tempRepsOnly, tempSortByCross)
                    onDismissRequest()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onReset()
                    onDismissRequest()
                }
            ) {
                Text("Reset")
            }
        }
    )
}
