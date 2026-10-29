package com.anil.sansiri.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MonthlyHouseStatus
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MaintenanceStatusFilter(val label: String) {
    ALL("All"),
    PAID("Paid"),
    PENDING("Pending Approval"),
    UNPAID("Unpaid")
}

enum class MaintenanceSortOption(val label: String) {
    BY_STATUS("Sort by Status"),
    BY_NAME("Sort by Name"),
    BY_HOUSE_NUMBER("Sort by House Number")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentStatus4MonthsScreen(
    monthlyStatuses: List<MonthlyHouseStatus>,
    modifier: Modifier = Modifier,
    unpaidMonths: List<String> = emptyList(),
    myHouseDetail: HouseDetail? = null,
    userHouses: List<HouseDetail> = emptyList(),
    pricingRules: PricingRules = PricingRules(),
    isAdmin: Boolean = false,
    onApprovePayment: ((houseId: String, monthYear: String) -> Unit)? = null,
    onSubmitPayment: ((selectedMonths: List<String>, transactionRef: String, totalAmount: Double) -> Unit)? = null
) {
    var subTabSelected by remember { mutableIntStateOf(0) } // 0 = 4-Month Society Overview, 1 = Pay Maintenance

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = subTabSelected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = subTabSelected == 0,
                onClick = { subTabSelected = 0 },
                text = { Text("4-Month Society Overview", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTabSelected == 1,
                onClick = { subTabSelected = 1 },
                text = {
                    Text(
                        text = if (unpaidMonths.isNotEmpty()) "Pay Dues (${unpaidMonths.size} Pending)" else "Pay Maintenance",
                        fontWeight = FontWeight.Bold,
                        color = if (unpaidMonths.isNotEmpty()) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        if (subTabSelected == 0) {
            FourMonthsStatusView(
                monthlyStatuses = monthlyStatuses,
                isAdmin = isAdmin,
                onApprovePayment = onApprovePayment
            )
        } else {
            PayMaintenanceSection(
                unpaidMonths = unpaidMonths,
                myHouseDetail = myHouseDetail,
                userHouses = userHouses,
                pricingRules = pricingRules,
                onSubmitPayment = onSubmitPayment ?: { _, _, _ -> }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FourMonthsStatusView(
    monthlyStatuses: List<MonthlyHouseStatus>,
    isAdmin: Boolean = false,
    onApprovePayment: ((houseId: String, monthYear: String) -> Unit)? = null
) {
    val months = remember {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.US)
        val cal = Calendar.getInstance()
        val list = mutableListOf<String>()
        for (i in 0..3) {
            list.add(formatter.format(cal.time))
            cal.add(Calendar.MONTH, -1)
        }
        list
    }
    var selectedMonthIndex by remember { mutableIntStateOf(0) }
    val selectedMonth = months.getOrElse(selectedMonthIndex) { months[0] }

    var selectedStatusFilter by remember { mutableStateOf(MaintenanceStatusFilter.ALL) }
    var selectedCrossFilter by remember { mutableStateOf("All Crosses") }
    var selectedSortOption by remember { mutableStateOf(MaintenanceSortOption.BY_STATUS) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val isFiltered = selectedStatusFilter != MaintenanceStatusFilter.ALL ||
            selectedCrossFilter != "All Crosses" ||
            selectedSortOption != MaintenanceSortOption.BY_STATUS

    val crossOptions = remember {
        listOf("All Crosses") + (1..10).map { "${it}st Cross".replace("1st Cross", "1st Cross").replace("2st Cross", "2nd Cross").replace("3st Cross", "3rd Cross").replace("4st Cross", "4th Cross").replace("5st Cross", "5th Cross").replace("6st Cross", "6th Cross").replace("7st Cross", "7th Cross").replace("8st Cross", "8th Cross").replace("9st Cross", "9th Cross").replace("10st Cross", "10th Cross") }
    }

    val monthFilteredList = monthlyStatuses.filter { item ->
        item.monthYear.equals(selectedMonth, ignoreCase = true) ||
        (item.monthYear.take(3).equals(selectedMonth.take(3), ignoreCase = true) &&
         item.monthYear.takeLast(4) == selectedMonth.takeLast(4))
    }

    val statusFilteredList = monthFilteredList.filter { item ->
        val isPaid = item.isPaid || item.paymentStatus == PaymentStatus.APPROVED
        val isPending = !isPaid && (!item.transactionRef.isNullOrBlank() || item.paymentStatus == PaymentStatus.PENDING)
        val isUnpaid = !isPaid && !isPending

        when (selectedStatusFilter) {
            MaintenanceStatusFilter.ALL -> true
            MaintenanceStatusFilter.PAID -> isPaid
            MaintenanceStatusFilter.PENDING -> isPending
            MaintenanceStatusFilter.UNPAID -> isUnpaid
        }
    }

    val crossFilteredList = statusFilteredList.filter { item ->
        if (selectedCrossFilter == "All Crosses") true
        else {
            val itemCross = if (item.crossStreet.isNotBlank()) item.crossStreet else when (item.houseNumber.take(1)) {
                "A" -> "1st Cross"
                "B" -> "2nd Cross"
                "C" -> "3rd Cross"
                else -> "1st Cross"
            }
            itemCross.equals(selectedCrossFilter, ignoreCase = true) || itemCross.contains(selectedCrossFilter, ignoreCase = true)
        }
    }

    val finalSortedList = when (selectedSortOption) {
        MaintenanceSortOption.BY_STATUS -> crossFilteredList.sortedWith(
            compareBy<MonthlyHouseStatus> { item ->
                val isPaid = item.isPaid || item.paymentStatus == PaymentStatus.APPROVED
                val isPending = !isPaid && (!item.transactionRef.isNullOrBlank() || item.paymentStatus == PaymentStatus.PENDING)
                when {
                    isPending -> 0
                    !isPaid -> 1
                    else -> 2
                }
            }.thenBy { it.houseNumber }
        )
        MaintenanceSortOption.BY_NAME -> crossFilteredList.sortedBy { it.ownerName.lowercase() }
        MaintenanceSortOption.BY_HOUSE_NUMBER -> crossFilteredList.sortedBy { it.houseNumber.lowercase() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "4-Month Payment Status View",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { showFilterDialog = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (isFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SecondaryTabRow(
            selectedTabIndex = selectedMonthIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            months.forEachIndexed { index, monthLabel ->
                Tab(
                    selected = selectedMonthIndex == index,
                    onClick = { selectedMonthIndex = index },
                    text = {
                        Text(
                            text = monthLabel,
                            fontWeight = if (selectedMonthIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isFiltered) {
            val activeLabels = mutableListOf<String>()
            if (selectedStatusFilter != MaintenanceStatusFilter.ALL) activeLabels.add(selectedStatusFilter.label)
            if (selectedCrossFilter != "All Crosses") activeLabels.add(selectedCrossFilter)
            if (selectedSortOption != MaintenanceSortOption.BY_STATUS) activeLabels.add(selectedSortOption.label)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = true,
                    onClick = { showFilterDialog = true },
                    label = { Text("Filter: " + activeLabels.joinToString(" • ")) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Filter",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    selectedStatusFilter = MaintenanceStatusFilter.ALL
                                    selectedCrossFilter = "All Crosses"
                                    selectedSortOption = MaintenanceSortOption.BY_STATUS
                                }
                        )
                    }
                )
            }
        }

        if (showFilterDialog) {
            PaymentStatusFilterDialog(
                currentStatusFilter = selectedStatusFilter,
                currentCrossFilter = selectedCrossFilter,
                currentSortOption = selectedSortOption,
                crossOptions = crossOptions,
                onDismissRequest = { showFilterDialog = false },
                onApply = { status, cross, sort ->
                    selectedStatusFilter = status
                    selectedCrossFilter = cross
                    selectedSortOption = sort
                },
                onReset = {
                    selectedStatusFilter = MaintenanceStatusFilter.ALL
                    selectedCrossFilter = "All Crosses"
                    selectedSortOption = MaintenanceSortOption.BY_STATUS
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (finalSortedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching payment records found for $selectedMonth",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(finalSortedList, key = { "${it.houseId}_${it.houseNumber}_${it.monthYear}" }) { item ->
                    MonthlyPaymentCard(
                        item = item,
                        isAdmin = isAdmin,
                        onApprove = {
                            onApprovePayment?.invoke(item.houseId, item.monthYear)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyPaymentCard(
    item: MonthlyHouseStatus,
    isAdmin: Boolean = false,
    onApprove: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    val isPaid = item.isPaid || item.paymentStatus == PaymentStatus.APPROVED
    val isPending = !isPaid && (!item.transactionRef.isNullOrBlank() || item.paymentStatus == PaymentStatus.PENDING)

    val backgroundColor = when {
        isPaid -> Color(0xFFE8F5E9)      // Soft Green (#E8F5E9)
        isPending -> Color(0xFFFFF8E1)   // Soft Amber (#FFF8E1)
        else -> Color(0xFFFFEBEE)        // Soft Red (#FFEBEE)
    }

    val textColor = when {
        isPaid -> Color(0xFF2E7D32)
        isPending -> Color(0xFFB78103)
        else -> Color(0xFFC62828)
    }

    val borderColor = when {
        isPaid -> Color(0xFFA5D6A7)
        isPending -> Color(0xFFFFE082)
        else -> Color(0xFFEF9A9A)
    }

    val badgeText = when {
        isPaid -> "PAID / APPROVED"
        isPending -> "PENDING APPROVAL"
        else -> "UNPAID"
    }

    val badgeBgColor = when {
        isPaid -> Color(0xFF2E7D32)
        isPending -> Color(0xFFF57F17)
        else -> Color(0xFFC62828)
    }

    val statusIcon = when {
        isPaid -> Icons.Default.CheckCircle
        isPending -> Icons.Default.Schedule
        else -> Icons.Default.Error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = when {
                                    isPaid -> Color(0xFFC8E6C9)
                                    isPending -> Color(0xFFFFECB3)
                                    else -> Color(0xFFFFCDD2)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Flat ${item.houseNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.ownerName.ifBlank { "Resident" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        }
                        if (isPaid && !item.paymentDate.isNullOrBlank()) {
                            Text(
                                text = "Paid on ${item.paymentDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        } else if (isPending && !item.transactionRef.isNullOrBlank()) {
                            Text(
                                text = "Ref: ${item.transactionRef}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${item.amount.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = badgeBgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick Approval Action Button for Admins if Pending Approval
            if (isPending && isAdmin && onApprove != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Approve",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Approve",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Billing Month:",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                        Text(
                            text = item.monthYear,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    if (!item.transactionRef.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Transaction Ref:",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                            Text(
                                text = item.transactionRef,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayMaintenanceSection(
    unpaidMonths: List<String>,
    myHouseDetail: HouseDetail?,
    userHouses: List<HouseDetail> = emptyList(),
    pricingRules: PricingRules,
    onSubmitPayment: (selectedMonths: List<String>, transactionRef: String, totalAmount: Double) -> Unit
) {
    val allOwnedHouses = remember(myHouseDetail, userHouses) {
        if (userHouses.isNotEmpty()) userHouses else listOfNotNull(myHouseDetail)
    }
    var selectedFlatIndex by remember { mutableIntStateOf(0) }
    val activeHouse = allOwnedHouses.getOrElse(selectedFlatIndex) { myHouseDetail }

    val ratePerMonth = when (activeHouse?.occupancyType) {
        OccupancyType.RENTED -> pricingRules.rentedMaintenanceRate
        OccupancyType.EMPTY_SITE -> pricingRules.emptySiteMaintenanceRate
        else -> pricingRules.selfOccupiedMaintenanceRate
    }

    val selectedMap = remember(unpaidMonths) {
        mutableStateMapOf<String, Boolean>().apply {
            unpaidMonths.forEach { month -> put(month, true) }
        }
    }

    var transactionRef by remember { mutableStateOf("") }

    val selectedMonths = unpaidMonths.filter { selectedMap[it] == true }
    val totalAmount = selectedMonths.size * ratePerMonth

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flat Selector if user owns multiple properties
        if (allOwnedHouses.size > 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Select Property/Flat to Pay For:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allOwnedHouses.forEachIndexed { idx, house ->
                                val isSelected = idx == selectedFlatIndex
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFlatIndex = idx },
                                    label = {
                                        Text("Flat ${house.displayHouseOrSiteNumber.ifBlank { "Unit #${idx + 1}" }}")
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pay Society Maintenance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val occText = when (activeHouse?.occupancyType) {
                                OccupancyType.RENTED -> "Rented Rate"
                                OccupancyType.EMPTY_SITE -> "Empty Site Rate"
                                else -> "Self-Occupied Rate"
                            }
                            Text(
                                text = "Flat ${activeHouse?.displayHouseOrSiteNumber ?: "A-101"} • $occText (₹${ratePerMonth.toInt()}/mo)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Months Count:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            color = if (unpaidMonths.isNotEmpty()) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${unpaidMonths.size} Pending",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (unpaidMonths.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "No Pending Dues!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "You have cleared all maintenance payments for San Siri Residency.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Select Pending Months to Pay:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        unpaidMonths.forEach { month ->
                            val isChecked = selectedMap[month] ?: false
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMap[month] = !isChecked }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { selectedMap[month] = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = month,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Maintenance Fee",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "₹${ratePerMonth.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selected Count:",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${selectedMonths.size} month(s)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rate per month:",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "₹${ratePerMonth.toInt()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL AMOUNT:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "₹${totalAmount.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Transaction Reference / UPI ID") },
                    placeholder = { Text("e.g. UPI/2025/987654321") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = {
                        onSubmitPayment(selectedMonths, transactionRef, totalAmount)
                    },
                    enabled = selectedMonths.isNotEmpty() && transactionRef.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Payment (₹${totalAmount.toInt()})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentStatusFilterDialog(
    currentStatusFilter: MaintenanceStatusFilter,
    currentCrossFilter: String,
    currentSortOption: MaintenanceSortOption,
    crossOptions: List<String>,
    onDismissRequest: () -> Unit,
    onApply: (status: MaintenanceStatusFilter, cross: String, sort: MaintenanceSortOption) -> Unit,
    onReset: () -> Unit
) {
    var tempStatus by remember { mutableStateOf(currentStatusFilter) }
    var tempCross by remember { mutableStateOf(currentCrossFilter) }
    var tempSort by remember { mutableStateOf(currentSortOption) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
        title = { Text("Filter & Sort Payment Status") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Filter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Status Filter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaintenanceStatusFilter.entries.forEach { status ->
                            FilterChip(
                                selected = tempStatus == status,
                                onClick = { tempStatus = status },
                                label = { Text(status.label) }
                            )
                        }
                    }
                }

                // Cross Filter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cross / Street", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                            crossOptions.forEach { cross ->
                                DropdownMenuItem(
                                    text = { Text(cross) },
                                    onClick = {
                                        tempCross = cross
                                        crossDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Sort Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sorting", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    MaintenanceSortOption.entries.forEach { sort ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempSort = sort }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = tempSort == sort,
                                onClick = { tempSort = sort }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sort.label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(tempStatus, tempCross, tempSort)
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

