package com.anil.sansiri.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import com.anil.sansiri.data.model.FinancialTransaction
import com.anil.sansiri.data.model.TransactionType
import com.anil.sansiri.data.model.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM_RANGE
}

enum class TransactionTypeFilter {
    ALL,
    INCOME_ONLY,
    EXPENSES_ONLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSheetScreen(
    transactions: List<FinancialTransaction>,
    currentUser: User?,
    onAddTransaction: (type: TransactionType, amount: Double, category: String, description: String, dateMillis: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = currentUser?.isAdminOrSuperAdmin == true

    var selectedPeriod by remember { mutableStateOf(DateFilterPeriod.MONTHLY) }
    var typeFilter by remember { mutableStateOf(TransactionTypeFilter.ALL) }

    val defaultStartMonth = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    var startDateMillis by remember { mutableStateOf<Long?>(defaultStartMonth) }
    var endDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }

    var showCustomDateRangeDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val isFiltered = selectedPeriod != DateFilterPeriod.MONTHLY || typeFilter != TransactionTypeFilter.ALL

    val filteredByDate = remember(transactions, selectedPeriod, startDateMillis, endDateMillis) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        when (selectedPeriod) {
            DateFilterPeriod.WEEKLY -> {
                val weekAgo = now - 7 * 86400000L
                transactions.filter { it.dateMillis >= weekAgo }
            }
            DateFilterPeriod.MONTHLY -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfMonth = cal.timeInMillis
                transactions.filter { it.dateMillis >= startOfMonth }
            }
            DateFilterPeriod.YEARLY -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfYear = cal.timeInMillis
                transactions.filter { it.dateMillis >= startOfYear }
            }
            DateFilterPeriod.CUSTOM_RANGE -> {
                val start = startDateMillis ?: 0L
                val end = (endDateMillis ?: System.currentTimeMillis()) + 86400000L - 1
                transactions.filter { it.dateMillis in start..end }
            }
        }
    }

    val totalIncome = remember(filteredByDate) {
        filteredByDate.filter { it.isIncome }.sumOf { it.amount }
    }
    val totalExpenses = remember(filteredByDate) {
        filteredByDate.filter { it.isExpense }.sumOf { it.amount }
    }
    val netBalance = remember(totalIncome, totalExpenses) {
        totalIncome - totalExpenses
    }

    val finalFilteredTransactions = remember(filteredByDate, typeFilter) {
        when (typeFilter) {
            TransactionTypeFilter.ALL -> filteredByDate
            TransactionTypeFilter.INCOME_ONLY -> filteredByDate.filter { it.isIncome }
            TransactionTypeFilter.EXPENSES_ONLY -> filteredByDate.filter { it.isExpense }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Balance Sheet",
                            tint = Color(0xFF006A60),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Society Balance Sheet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
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
            }

            // Summary Cards Hero Section
            item {
                SummaryHeaderCard(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netBalance = netBalance
                )
            }

            // Compact Active Filter Summary Badge
            if (isFiltered) {
                item {
                    val periodLabel = when (selectedPeriod) {
                        DateFilterPeriod.WEEKLY -> "Weekly"
                        DateFilterPeriod.MONTHLY -> "Monthly"
                        DateFilterPeriod.YEARLY -> "Yearly"
                        DateFilterPeriod.CUSTOM_RANGE -> "Custom Range"
                    }
                    val typeLabel = when (typeFilter) {
                        TransactionTypeFilter.ALL -> "All Types"
                        TransactionTypeFilter.INCOME_ONLY -> "Income Only"
                        TransactionTypeFilter.EXPENSES_ONLY -> "Expenses Only"
                    }
                    val filterSummary = "Filter: $periodLabel • $typeLabel"

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = { showFilterDialog = true },
                                label = { Text(filterSummary) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Filter",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                selectedPeriod = DateFilterPeriod.MONTHLY
                                                typeFilter = TransactionTypeFilter.ALL
                                            }
                                    )
                                }
                            )
                        }

                        if (selectedPeriod == DateFilterPeriod.CUSTOM_RANGE && startDateMillis != null && endDateMillis != null) {
                            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val startStr = dateFormat.format(Date(startDateMillis!!))
                            val endStr = dateFormat.format(Date(endDateMillis!!))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { showCustomDateRangeDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Range: $startStr - $endStr (Tap to change)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transactions Title
            item {
                Text(
                    text = "Transactions (${finalFilteredTransactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (finalFilteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions found for selected filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(finalFilteredTransactions, key = { it.id }) { transaction ->
                    TransactionItemCard(transaction = transaction)
                }
            }
        }

        // Admin FAB: + Add Entry at bottom right in Deep Teal (#006A60)
        if (isAdmin) {
            ExtendedFloatingActionButton(
                onClick = { showAddTransactionDialog = true },
                containerColor = Color(0xFF006A60),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Entry") },
                text = { Text("Add Entry", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    // Custom Date Range Dialog
    if (showCustomDateRangeDialog) {
        CustomDateRangePickerDialog(
            initialStartDate = startDateMillis ?: System.currentTimeMillis(),
            initialEndDate = endDateMillis ?: System.currentTimeMillis(),
            onDateRangeSelected = { start, end ->
                startDateMillis = start
                endDateMillis = end
                selectedPeriod = DateFilterPeriod.CUSTOM_RANGE
                showCustomDateRangeDialog = false
            },
            onDismiss = { showCustomDateRangeDialog = false }
        )
    }

    // Add Income / Expense Dialog for Admins & Super Admins
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onAddTransaction = { type, amount, category, description, dateMillis ->
                onAddTransaction(type, amount, category, description, dateMillis)
                showAddTransactionDialog = false
            },
            onDismiss = { showAddTransactionDialog = false }
        )
    }

    if (showFilterDialog) {
        BalanceSheetFilterDialog(
            currentPeriod = selectedPeriod,
            currentTypeFilter = typeFilter,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            onDismissRequest = { showFilterDialog = false },
            onApply = { period, type ->
                selectedPeriod = period
                typeFilter = type
            },
            onOpenCustomDateRangePicker = {
                showCustomDateRangeDialog = true
            },
            onReset = {
                selectedPeriod = DateFilterPeriod.MONTHLY
                typeFilter = TransactionTypeFilter.ALL
            }
        )
    }
}

@Composable
private fun SummaryHeaderCard(
    totalIncome: Double,
    totalExpenses: Double,
    netBalance: Double
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Net Balance Hero Container (Deep Teal container #E0F2F1, Deep Teal text #004D40)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(netBalance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF004D40)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF004D40).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Net Balance",
                        tint = Color(0xFF004D40),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Total Income & Total Expenses Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Income Card (Light Green container #E8F5E9, Dark Green text #1B5E20)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF1B5E20).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Total Income",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currencyFormat.format(totalIncome),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            // Total Expenses Card (Light Red container #FFEBEE, Dark Red text #B71C1C)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFB71C1C).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFFB71C1C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currencyFormat.format(totalExpenses),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
        }
    }
}

private fun getCategoryEmoji(category: String, isIncome: Boolean): String {
    val catLower = category.lowercase()
    return when {
        catLower.contains("maintenance") -> "🛠️"
        catLower.contains("security") || catLower.contains("guard") -> "🛡️"
        catLower.contains("water") || catLower.contains("tanker") -> "💧"
        catLower.contains("electricity") || catLower.contains("bill") || catLower.contains("power") -> "⚡"
        catLower.contains("plumb") || catLower.contains("repair") || catLower.contains("fix") -> "🔧"
        catLower.contains("garden") || catLower.contains("plant") -> "🌿"
        catLower.contains("clean") || catLower.contains("sweep") -> "🧹"
        catLower.contains("hall") || catLower.contains("rental") -> "🏢"
        catLower.contains("donation") || catLower.contains("interest") -> "💰"
        isIncome -> "📈"
        else -> "📉"
    }
}

@Composable
private fun TransactionItemCard(transaction: FinancialTransaction) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(transaction.dateMillis))

    val isIncome = transaction.isIncome
    val accentColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val amountPrefix = if (isIncome) "+ " else "- "
    val emoji = getCategoryEmoji(transaction.category, isIncome)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left accent indicator bar (Green for Income, Red for Expense)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category badge with icon
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = emoji,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // Bold high-contrast Title (#1E293B)
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Subtitle (#64748B) and Category badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = transaction.category.ifBlank { if (isIncome) "Income" else "Expense" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // High-contrast Amount text: + ₹X,XXX in bold Green (#2E7D32) or - ₹X,XXX in bold Red (#C62828)
                Text(
                    text = amountPrefix + currencyFormat.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangePickerDialog(
    initialStartDate: Long,
    initialEndDate: Long,
    onDateRangeSelected: (startDateMillis: Long, endDateMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate,
        initialSelectedEndDateMillis = initialEndDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onDateRangeSelected(start, end)
                    } else if (start != null) {
                        onDateRangeSelected(start, start)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null
            ) {
                Text("Apply Range", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select Custom Date Range",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        )
    }
}

@Composable
private fun AddTransactionDialog(
    onAddTransaction: (type: TransactionType, amount: Double, category: String, description: String, dateMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categories = remember(type) {
        if (type == TransactionType.INCOME) {
            listOf("Maintenance Payment", "Hall Rental", "Donation", "Interest", "Miscellaneous Income")
        } else {
            listOf("Security Salary", "Water Tanker", "Electricity Bill", "Plumbing & Repairs", "Garden Maintenance", "Cleaning Supplies", "Miscellaneous Expense")
        }
    }

    var showCategoryMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Financial Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = TransactionType.INCOME }
                    ) {
                        RadioButton(
                            selected = type == TransactionType.INCOME,
                            onClick = { type = TransactionType.INCOME }
                        )
                        Text("Income (Green)", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = TransactionType.EXPENSE }
                    ) {
                        RadioButton(
                            selected = type == TransactionType.EXPENSE,
                            onClick = { type = TransactionType.EXPENSE }
                        )
                        Text("Expense (Red)", fontWeight = FontWeight.SemiBold, color = Color(0xFFC62828))
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 25000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        placeholder = { Text("Select or type category...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Select Category")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Monthly Security Salary, Water Tanker") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && description.isNotBlank()) {
                        onAddTransaction(
                            type,
                            amount,
                            category.ifBlank { if (type == TransactionType.INCOME) "Income" else "Expense" },
                            description.trim(),
                            System.currentTimeMillis()
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            ) {
                Text("Save Entry")
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
fun BalanceSheetFilterDialog(
    currentPeriod: DateFilterPeriod,
    currentTypeFilter: TransactionTypeFilter,
    startDateMillis: Long?,
    endDateMillis: Long?,
    onDismissRequest: () -> Unit,
    onApply: (period: DateFilterPeriod, typeFilter: TransactionTypeFilter) -> Unit,
    onOpenCustomDateRangePicker: () -> Unit,
    onReset: () -> Unit
) {
    var tempPeriod by remember { mutableStateOf(currentPeriod) }
    var tempType by remember { mutableStateOf(currentTypeFilter) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
        title = { Text("Filter Balance Sheet") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time Period
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time Period", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    DateFilterPeriod.entries.forEach { period ->
                        val label = when (period) {
                            DateFilterPeriod.WEEKLY -> "Weekly"
                            DateFilterPeriod.MONTHLY -> "Monthly"
                            DateFilterPeriod.YEARLY -> "Yearly"
                            DateFilterPeriod.CUSTOM_RANGE -> "Custom Range"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempPeriod = period
                                    if (period == DateFilterPeriod.CUSTOM_RANGE) {
                                        onOpenCustomDateRangePicker()
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = tempPeriod == period,
                                onClick = {
                                    tempPeriod = period
                                    if (period == DateFilterPeriod.CUSTOM_RANGE) {
                                        onOpenCustomDateRangePicker()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }

                    if (tempPeriod == DateFilterPeriod.CUSTOM_RANGE && startDateMillis != null && endDateMillis != null) {
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startStr = dateFormat.format(Date(startDateMillis))
                        val endStr = dateFormat.format(Date(endDateMillis))
                        TextButton(
                            onClick = onOpenCustomDateRangePicker,
                            modifier = Modifier.padding(start = 32.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Range: $startStr - $endStr")
                        }
                    }
                }

                // Transaction Type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transaction Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    TransactionTypeFilter.entries.forEach { type ->
                        val label = when (type) {
                            TransactionTypeFilter.ALL -> "All Transactions"
                            TransactionTypeFilter.INCOME_ONLY -> "Income Only"
                            TransactionTypeFilter.EXPENSES_ONLY -> "Expenses Only"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempType = type }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = tempType == type,
                                onClick = { tempType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(tempPeriod, tempType)
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

