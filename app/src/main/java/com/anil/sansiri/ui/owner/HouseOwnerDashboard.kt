package com.anil.sansiri.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Badge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.ApprovalStatus

data class OwnerNavTab(
    val title: String,
    val icon: ImageVector
)

val ownerNavTabs = listOf(
    OwnerNavTab("My Flat", Icons.Default.HomeWork),
    OwnerNavTab("Pay Fee", Icons.Default.Payments),
    OwnerNavTab("History", Icons.AutoMirrored.Filled.ReceiptLong),
    OwnerNavTab("Directory", Icons.Default.People)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseOwnerDashboard(
    viewModel: HouseOwnerViewModel,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onSwitchRoleClicked: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF006A60))
                            .padding(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            val userName = uiState.currentUser?.name?.ifBlank { "House Owner" } ?: "House Owner"
                            val firstLetter = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "H"

                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                color = Color(0xFFFFD54F)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = firstLetter,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1B1F)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.currentUser?.phoneNumber ?: "N/A",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val roleTag = when (uiState.currentUser?.role) {
                                com.anil.sansiri.data.model.UserRole.SUPER_ADMIN -> "Super Admin"
                                com.anil.sansiri.data.model.UserRole.ADMIN -> "Admin"
                                com.anil.sansiri.data.model.UserRole.USER, com.anil.sansiri.data.model.UserRole.HOUSE_OWNER -> "User / House Owner"
                                null -> "User / House Owner"
                            }
                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = roleTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    Spacer(modifier = Modifier.height(8.dp))

                    ownerNavTabs.forEachIndexed { index, tab ->
                        NavigationDrawerItem(
                            label = { Text(tab.title) },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                            selected = uiState.selectedTab == index,
                            onClick = {
                                viewModel.setSelectedTab(index)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (onSwitchRoleClicked != null) {
                        NavigationDrawerItem(
                            label = { Text("Switch to Admin Portal") },
                            icon = { Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Switch Portal") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onSwitchRoleClicked()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogoutClicked()
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "San Siri Residency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "House Owner",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        if (onSwitchRoleClicked != null) {
                            IconButton(onClick = onSwitchRoleClicked) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Role",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF004D40),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
        },
            bottomBar = {
                NavigationBar {
                    ownerNavTabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.setSelectedTab(index) },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                            label = null,
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            // Registration status notification banner at the top of the dashboard
            RegistrationNotificationBanner(
                status = uiState.houseDetail?.approvalStatus,
                houseNumber = uiState.houseDetail?.displayHouseOrSiteNumber ?: uiState.houseNumber
            )

            // Screen Content based on selected tab
            when (uiState.selectedTab) {
                0 -> HouseRegistrationScreen(
                    uiState = uiState,
                    onPropertyTypeChanged = viewModel::onPropertyTypeChanged,
                    onSiteNumberChanged = viewModel::onSiteNumberChanged,
                    onBuildingNameChanged = viewModel::onBuildingNameChanged,
                    onBuildingNumberChanged = viewModel::onBuildingNumberChanged,
                    onFloorNumberChanged = viewModel::onFloorNumberChanged,
                    onHouseNumberChanged = viewModel::onHouseNumberChanged,
                    onRentedHousesCountChanged = viewModel::onRentedHousesCountChanged,
                    onCrossStreetChanged = viewModel::onCrossStreetChanged,
                    onOccupancyTypeChanged = viewModel::onOccupancyTypeChanged,
                    onOccupancySelectionChanged = viewModel::onOccupancySelectionChanged,
                    onSaveClicked = viewModel::saveHouseDetail,
                    onClearMessages = viewModel::clearMessages
                )
                1 -> MaintenancePayScreen(
                    uiState = uiState,
                    onMonthYearChanged = viewModel::onMonthYearChanged,
                    onPaymentMethodChanged = viewModel::onPaymentMethodChanged,
                    onTransactionRefChanged = viewModel::onTransactionRefChanged,
                    onSubmitPayment = viewModel::submitPayment,
                    onClearMessages = viewModel::clearMessages
                )
                2 -> PaymentHistoryScreen(
                    payments = uiState.paymentHistory
                )
                3 -> ResidencyDirectoryScreen(
                    houses = uiState.approvedHouses
                )
            }
        }
    }
}
}

@Composable
fun RegistrationNotificationBanner(
    status: ApprovalStatus?,
    houseNumber: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor, message) = when (status) {
        ApprovalStatus.PENDING -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFF8D6E63),
            "Status: Your property registration ($houseNumber) is pending Admin approval."
        )
        ApprovalStatus.APPROVED -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            "Status: Property $houseNumber is verified and approved."
        )
        ApprovalStatus.REJECTED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFB71C1C),
            "Status: Property registration ($houseNumber) was rejected by Admin."
        )
        null -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF0D47A1),
            "Status: Property details not registered yet. Please register below."
        )
    }

    Surface(
        color = bgColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = fgColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}
