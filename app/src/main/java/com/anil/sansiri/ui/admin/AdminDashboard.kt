package com.anil.sansiri.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Badge
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.UserRole

sealed class AdminTabItem(
    val title: String,
    val icon: ImageVector,
    val tabIndex: Int
) {
    object PendingApprovals : AdminTabItem("Approvals", Icons.Default.HowToReg, 0)
    object PaymentVerification : AdminTabItem("Payments", Icons.AutoMirrored.Filled.ReceiptLong, 1)
    object PricingRules : AdminTabItem("Pricing Rates", Icons.Default.PriceChange, 2)
    object Directory : AdminTabItem("Directory", Icons.Default.People, 3)
    object ManageAdmins : AdminTabItem("Admins", Icons.Default.AdminPanelSettings, 4)

    companion object {
        val defaultItems = listOf(PendingApprovals, PaymentVerification, PricingRules, Directory)
        val superAdminItems = listOf(PendingApprovals, PaymentVerification, PricingRules, Directory, ManageAdmins)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: AdminViewModel,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onSwitchRoleClicked: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSuperAdmin = uiState.currentUser?.role == UserRole.SUPER_ADMIN
    val navItems = if (isSuperAdmin) AdminTabItem.superAdminItems else AdminTabItem.defaultItems
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val message = uiState.statusMessage ?: uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

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
                            val userName = uiState.currentUser?.name?.ifBlank { "Admin" } ?: "Admin"
                            val firstLetter = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"

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
                                UserRole.SUPER_ADMIN -> "Super Admin"
                                UserRole.ADMIN -> "Admin"
                                UserRole.USER, UserRole.HOUSE_OWNER -> "User / House Owner"
                                null -> "Admin"
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

                    navItems.forEachIndexed { index, tab ->
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
                            label = { Text("Switch to Resident Portal") },
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "San Siri Residency",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isSuperAdmin) "Super Admin" else "Admin",
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.height(100.dp)
            ) {
                navItems.forEach { tab ->
                    val isSelected = uiState.selectedTab == tab.tabIndex

                    val badgeCount = when (tab) {
                        AdminTabItem.PendingApprovals -> uiState.pendingHousesCount
                        AdminTabItem.PaymentVerification -> uiState.pendingPaymentsCount
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(tab.tabIndex) },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(text = badgeCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title, modifier = Modifier.size(32.dp))
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title, modifier = Modifier.size(32.dp))
                            }
                        },
                        label = null,
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> PendingApprovalsScreen(
                    pendingHouses = uiState.pendingHouses,
                    onApprove = { houseId -> viewModel.approveHouse(houseId) },
                    onReject = { houseId -> viewModel.rejectHouse(houseId) }
                )
                1 -> PaymentVerificationScreen(
                    pendingPayments = uiState.pendingPayments,
                    totalCollected = uiState.totalCollected,
                    pendingApprovalAmount = uiState.pendingApprovalAmount,
                    approvedPaymentsCount = uiState.approvedPaymentsCount,
                    onApprovePayment = { paymentId -> viewModel.approvePayment(paymentId) },
                    onRejectPayment = { paymentId -> viewModel.rejectPayment(paymentId) }
                )
                2 -> PricingRulesConfigScreen(
                    selfOccupiedRate = uiState.selfOccupiedRateInput,
                    rentedRate = uiState.rentedRateInput,
                    emptySiteRate = uiState.emptySiteRateInput,
                    pricingRules = uiState.pricingRules,
                    onSelfOccupiedRateChanged = { rate -> viewModel.onSelfOccupiedRateChanged(rate) },
                    onRentedRateChanged = { rate -> viewModel.onRentedRateChanged(rate) },
                    onEmptySiteRateChanged = { rate -> viewModel.onEmptySiteRateChanged(rate) },
                    onSaveRules = { viewModel.savePricingRules() },
                    statusMessage = uiState.statusMessage,
                    errorMessage = uiState.errorMessage
                )
                3 -> AdminDirectoryScreen(
                    approvedHouses = uiState.approvedHouses
                )
                4 -> ManageAdminsScreen(
                    currentUser = uiState.currentUser,
                    admins = uiState.adminsList,
                    allHouses = uiState.approvedHouses,
                    newUserName = uiState.newUserName,
                    newUserPhone = uiState.newUserPhone,
                    newUserFlatNumber = uiState.newUserFlatNumber,
                    newUserCrossStreet = uiState.newUserCrossStreet,
                    newUserOccupancyType = uiState.newUserOccupancyType,
                    newUserRole = uiState.newUserRole,
                    onNameChanged = { viewModel.onNewUserNameChanged(it) },
                    onPhoneChanged = { viewModel.onNewUserPhoneChanged(it) },
                    onFlatNumberChanged = { viewModel.onNewUserFlatNumberChanged(it) },
                    onCrossStreetChanged = { viewModel.onNewUserCrossStreetChanged(it) },
                    onOccupancyTypeChanged = { viewModel.onNewUserOccupancyTypeChanged(it) },
                    onRoleChanged = { viewModel.onNewUserRoleChanged(it) },
                    onAddUser = { viewModel.addNewUser() },
                    onToggleUserActiveStatus = { targetUid, isActive ->
                        viewModel.setUserActiveStatus(targetUid, isActive)
                    }
                )
            }
        }
    }
}
}
