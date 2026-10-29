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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.ui.admin.AddNewUserDialog
import com.anil.sansiri.ui.admin.EditUserAndBuildingDetailsDialog

@Composable
fun PeoplesDirectoryScreen(
    houses: List<HouseDetail>,
    allUsers: List<User>,
    crossRepresentatives: Map<String, String>, // crossName -> userUid
    currentUser: User?,
    onAssignCrossRepresentative: (cross: String, userUid: String) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateUserRoleAndDetails: ((targetUid: String, name: String, houseNumber: String, cross: String, occupancyType: OccupancyType, role: UserRole) -> Unit)? = null
) {
    val context = LocalContext.current
    val isAdmin = currentUser?.isAdminOrSuperAdmin == true

    var searchQuery by remember { mutableStateOf("") }
    var selectedCrossFilter by remember { mutableStateOf("All Crosses") }
    var representativesOnly by remember { mutableStateOf(false) }
    var sortByCross by remember { mutableStateOf(true) } // true: Sort by Cross, false: Sort by Name (A-Z)
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    val isFiltered = selectedCrossFilter != "All Crosses" || representativesOnly || !sortByCross

    var selectedCrossForAssign by remember { mutableStateOf<String?>(null) }
    var userAndHouseToEdit by remember { mutableStateOf<Pair<User, HouseDetail?>?>(null) }

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

    // Function to check if a house's owner is representative of their cross
    val getRepUid = { cross: String ->
        val explicitRep = crossRepresentatives[cross]
        explicitRep ?: houses.firstOrNull {
            val c = it.crossStreet.trim().ifBlank { "1st Cross" }
            c.equals(cross, ignoreCase = true)
        }?.ownerUid
    }

    // Filter houses based on search query, selected cross filter, and representatives only
    val filteredHouses = remember(houses, searchQuery, selectedCrossFilter, representativesOnly, crossRepresentatives, allUsers) {
        houses.filter { house ->
            val hCross = house.crossStreet.trim().ifBlank { "1st Cross" }

            // 1. Search Query Filter
            val matchesSearch = searchQuery.isBlank() ||
                    house.ownerName.contains(searchQuery, ignoreCase = true) ||
                    house.houseNumber.contains(searchQuery, ignoreCase = true) ||
                    house.ownerPhone.contains(searchQuery, ignoreCase = true) ||
                    hCross.contains(searchQuery, ignoreCase = true)

            // 2. Cross Filter
            val matchesCross = selectedCrossFilter == "All Crosses" ||
                    hCross.equals(selectedCrossFilter, ignoreCase = true)

            // 3. Representatives Only Filter
            val isRep = house.ownerUid == getRepUid(hCross)
            val matchesRep = !representativesOnly || isRep

            matchesSearch && matchesCross && matchesRep
        }
    }

    Box(modifier = modifier.fillMaxSize()) box@ {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    border = BorderStroke(1.dp, Color(0xFF006A60).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Total Residents: ${filteredHouses.size}",
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
            OutlinedTextField(
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
                    val residentsInCross = filteredHouses.filter { house ->
                        val hCross = house.crossStreet.trim().ifBlank { "1st Cross" }
                        hCross.equals(cross, ignoreCase = true)
                    }.sortedBy { it.houseNumber }

                    val repUid = getRepUid(cross)
                    val representativeUser = repUid?.let { uid -> allUsers.find { it.uid == uid } }
                    val isRepInFiltered = representativeUser != null && filteredHouses.any { it.ownerUid == representativeUser.uid }

                    val shouldShow = residentsInCross.isNotEmpty() || isRepInFiltered

                    if (shouldShow) {
                        item(key = "header_$cross") {
                            CrossHeader(
                                crossName = cross,
                                isAdmin = isAdmin,
                                onAssignRepClicked = { selectedCrossForAssign = cross }
                            )
                        }

                        // Representative Highlighted Section if exists & matches filters
                        if (representativeUser != null && isRepInFiltered) {
                            val repHouse = houses.find { it.ownerUid == representativeUser.uid }
                            item(key = "rep_$cross") {
                                RepresentativeHighlightCard(
                                    crossName = cross,
                                    user = representativeUser,
                                    houseNumber = repHouse?.displayHouseOrSiteNumber ?: "Resident",
                                    isAdmin = isAdmin,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${representativeUser.phoneNumber}"))
                                        context.startActivity(intent)
                                    },
                                    onEdit = {
                                        userAndHouseToEdit = Pair(representativeUser, repHouse)
                                    }
                                )
                            }
                        }

                        val nonRepResidents = residentsInCross.filter { it.ownerUid != representativeUser?.uid }

                        if (nonRepResidents.isEmpty() && (!isRepInFiltered || representativesOnly)) {
                            item(key = "empty_$cross") {
                                Text(
                                    text = if (representativesOnly) "Representative listed above." else "No other registered residents in $cross yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        } else {
                            items(nonRepResidents, key = { "res_${it.id}" }) { house ->
                                val targetUser = allUsers.find { usr -> usr.uid == house.ownerUid }
                                    ?: User(uid = house.ownerUid, name = house.ownerName, phoneNumber = house.ownerPhone)
                                ResidentDirectoryCard(
                                    house = house,
                                    isRepresentative = false,
                                    isAdmin = isAdmin,
                                    onCall = {
                                        if (house.ownerPhone.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${house.ownerPhone}"))
                                            context.startActivity(intent)
                                        }
                                    },
                                    onEdit = {
                                        userAndHouseToEdit = Pair(targetUser, house)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Mode 2: Sorted by Name (A-Z)
                val sortedByNameHouses = filteredHouses.sortedBy { it.ownerName.ifBlank { "Resident" }.lowercase() }

                if (sortedByNameHouses.isEmpty()) {
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
                    items(sortedByNameHouses, key = { "az_${it.id}" }) { house ->
                        val hCross = house.crossStreet.trim().ifBlank { "1st Cross" }
                        val repUid = getRepUid(hCross)
                        val isRep = house.ownerUid == repUid
                        val targetUser = allUsers.find { usr -> usr.uid == house.ownerUid }
                            ?: User(uid = house.ownerUid, name = house.ownerName, phoneNumber = house.ownerPhone)

                        if (isRep) {
                            RepresentativeHighlightCard(
                                crossName = hCross,
                                user = targetUser,
                                houseNumber = house.displayHouseOrSiteNumber,
                                isAdmin = isAdmin,
                                onCall = {
                                    if (house.ownerPhone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${house.ownerPhone}"))
                                        context.startActivity(intent)
                                    }
                                },
                                onEdit = {
                                    userAndHouseToEdit = Pair(targetUser, house)
                                }
                            )
                        } else {
                            ResidentDirectoryCard(
                                house = house,
                                isRepresentative = false,
                                isAdmin = isAdmin,
                                onCall = {
                                    if (house.ownerPhone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${house.ownerPhone}"))
                                        context.startActivity(intent)
                                    }
                                },
                                onEdit = {
                                    userAndHouseToEdit = Pair(targetUser, house)
                                }
                            )
                        }
                    }
                }
            }
        } // Closes LazyColumn

        if (isAdmin) {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                shape = CircleShape,
                containerColor = Color(0xFF006A60),
                contentColor = Color.White,
                modifier = this@box.run { Modifier.align(Alignment.BottomEnd) }
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Resident"
                )
            }
        }
    } // Closes Box
    }

    // Assign Cross Representative Dialog for Admins & Super Admins
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

    // Edit User & House Details Dialog for Admins & Super Admins
    if (userAndHouseToEdit != null) {
        val (targetUser, targetHouse) = userAndHouseToEdit!!
        EditUserAndBuildingDetailsDialog(
            user = targetUser,
            house = targetHouse,
            isSuperAdmin = currentUser?.role == UserRole.SUPER_ADMIN,
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
            isSuperAdmin = currentUser?.role == UserRole.SUPER_ADMIN,
            onAddUser = { name, phone, flatNumber, crossStreet, occupancyType, selectedRole ->
                val targetUid = "user_dir_${System.currentTimeMillis()}"
                onUpdateUserRoleAndDetails?.invoke(
                    targetUid,
                    name,
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
private fun RepresentativeHighlightCard(
    crossName: String,
    user: User,
    houseNumber: String,
    isAdmin: Boolean = false,
    onCall: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(2.dp, Color(0xFFFFD54F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Golden Cross Representative Badge
                    Surface(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🌟", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cross Representative",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                        }
                    }

                    // Clear Cross Street Badge
                    Surface(
                        color = Color(0xFFE0F2F1),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF006A60).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "🚏 $crossName",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006A60),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.name.ifBlank { "Resident Representative" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Flat $houseNumber • ${user.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF424242)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin && onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Resident Details",
                            tint = Color(0xFF006A60)
                        )
                    }
                }
                IconButton(onClick = onCall) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Representative",
                        tint = Color(0xFF006A60)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResidentDirectoryCard(
    house: HouseDetail,
    isRepresentative: Boolean,
    isAdmin: Boolean = false,
    onCall: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val displayCross = house.crossStreet.trim().ifBlank { "1st Cross" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Flat ${house.displayHouseOrSiteNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Clear Cross Street Badge
                    Surface(
                        color = Color(0xFFE0F2F1),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF006A60).copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "🚏 $displayCross",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006A60),
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

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = house.ownerName.ifBlank { "Resident" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (house.ownerPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = house.ownerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin && onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Resident Details",
                            tint = Color(0xFF006A60)
                        )
                    }
                }
                if (house.ownerPhone.isNotBlank()) {
                    IconButton(onClick = onCall) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Resident",
                            tint = Color(0xFF006A60)
                        )
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
