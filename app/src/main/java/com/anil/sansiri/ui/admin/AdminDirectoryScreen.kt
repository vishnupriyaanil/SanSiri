package com.anil.sansiri.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

enum class DirectoryOccupancyFilter {
    ALL,
    SELF_OCCUPIED,
    RENTED,
    EMPTY_SITE
}

@Composable
fun AdminDirectoryScreen(
    approvedHouses: List<HouseDetail>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var occupancyFilter by remember { mutableStateOf(DirectoryOccupancyFilter.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val isFiltered = occupancyFilter != DirectoryOccupancyFilter.ALL

    val filteredHouses = remember(approvedHouses, searchQuery, occupancyFilter) {
        approvedHouses.filter { house ->
            val matchesSearch = searchQuery.isBlank() ||
                    house.houseNumber.contains(searchQuery, ignoreCase = true) ||
                    house.ownerName.contains(searchQuery, ignoreCase = true) ||
                    house.crossStreet.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (occupancyFilter) {
                DirectoryOccupancyFilter.ALL -> true
                DirectoryOccupancyFilter.SELF_OCCUPIED -> house.occupancyType == OccupancyType.SELF_OCCUPIED
                DirectoryOccupancyFilter.RENTED -> house.occupancyType == OccupancyType.RENTED
                DirectoryOccupancyFilter.EMPTY_SITE -> house.occupancyType == OccupancyType.EMPTY_SITE
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search bar & Filter Icon Button Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by flat, owner or street...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
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

            // Compact Active Filter Summary Chip
            if (isFiltered) {
                val filterLabel = when (occupancyFilter) {
                    DirectoryOccupancyFilter.ALL -> "All"
                    DirectoryOccupancyFilter.SELF_OCCUPIED -> "Self-Occupied"
                    DirectoryOccupancyFilter.RENTED -> "Rented"
                    DirectoryOccupancyFilter.EMPTY_SITE -> "Empty Site"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { showFilterDialog = true },
                        label = { Text("Filter: $filterLabel") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Filter",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        occupancyFilter = DirectoryOccupancyFilter.ALL
                                    }
                            )
                        }
                    )
                }
            }
        }

        if (showFilterDialog) {
            AdminDirectoryFilterDialog(
                currentOccupancyFilter = occupancyFilter,
                approvedHouses = approvedHouses,
                onDismissRequest = { showFilterDialog = false },
                onApply = { filter ->
                    occupancyFilter = filter
                },
                onReset = {
                    occupancyFilter = DirectoryOccupancyFilter.ALL
                }
            )
        }

        if (filteredHouses.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HomeWork,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Approved Residences Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (approvedHouses.isEmpty()) "Approve house registrations to populate directory." else "Try adjusting your search or filters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Approved Directory (${filteredHouses.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40)
                    )
                }

                items(filteredHouses, key = { it.id }) { house ->
                    AdminResidentCard(house = house)
                }
            }
        }
    }
}

@Composable
fun AdminResidentCard(
    house: HouseDetail,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = null,
                        tint = Color(0xFF006A60),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Flat ${house.houseNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val occLabel = when (house.occupancyType) {
                    OccupancyType.SELF_OCCUPIED -> "Self-Occupied"
                    OccupancyType.RENTED -> "Rented"
                    OccupancyType.EMPTY_SITE -> "Empty Site"
                }
                val chipColor = when (house.occupancyType) {
                    OccupancyType.SELF_OCCUPIED -> Color(0xFFCEE8E2)
                    OccupancyType.RENTED -> Color(0xFFBEECE6)
                    OccupancyType.EMPTY_SITE -> Color(0xFFFEF3C7)
                }

                Surface(
                    color = chipColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = occLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF08201B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (house.ownerName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = house.ownerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (house.ownerPhone.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = house.ownerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (house.crossStreet.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = house.crossStreet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDirectoryFilterDialog(
    currentOccupancyFilter: DirectoryOccupancyFilter,
    approvedHouses: List<HouseDetail>,
    onDismissRequest: () -> Unit,
    onApply: (filter: DirectoryOccupancyFilter) -> Unit,
    onReset: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentOccupancyFilter) }

    val selfCount = remember(approvedHouses) { approvedHouses.count { it.occupancyType == OccupancyType.SELF_OCCUPIED } }
    val rentedCount = remember(approvedHouses) { approvedHouses.count { it.occupancyType == OccupancyType.RENTED } }
    val emptyCount = remember(approvedHouses) { approvedHouses.count { it.occupancyType == OccupancyType.EMPTY_SITE } }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
        title = { Text("Filter Directory") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Occupancy Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                val options = listOf(
                    DirectoryOccupancyFilter.ALL to "All (${approvedHouses.size})",
                    DirectoryOccupancyFilter.SELF_OCCUPIED to "Self-Occupied ($selfCount)",
                    DirectoryOccupancyFilter.RENTED to "Rented ($rentedCount)",
                    DirectoryOccupancyFilter.EMPTY_SITE to "Empty Site ($emptyCount)"
                )

                options.forEach { (filterOption, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempFilter = filterOption }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = tempFilter == filterOption,
                            onClick = { tempFilter = filterOption }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(tempFilter)
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

