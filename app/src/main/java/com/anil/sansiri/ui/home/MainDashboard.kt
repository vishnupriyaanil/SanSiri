package com.anil.sansiri.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.PriceChange
import com.anil.sansiri.data.model.PropertyType
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.ui.admin.PricingRulesConfigScreen
import com.anil.sansiri.ui.home.tabs.AnnouncementsTab
import com.anil.sansiri.ui.home.tabs.ConcernsTab
import com.anil.sansiri.ui.home.tabs.RequestsTab
import com.anil.sansiri.ui.owner.HouseOwnerViewModel
import kotlinx.coroutines.launch

sealed class NavRouteItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavRouteItem("HOME", "Home", Icons.Default.Home)
    object Announcements : NavRouteItem("ANNOUNCEMENTS", "Announcements", Icons.Default.Campaign)
    object Requests : NavRouteItem("REQUESTS", "Requests", Icons.AutoMirrored.Filled.Assignment)
    object Payments : NavRouteItem("PAYMENTS", "Payments", Icons.Default.Payments)
    object Concerns : NavRouteItem("CONCERNS", "Concerns", Icons.Default.Feedback)
    object BalanceSheet : NavRouteItem("BALANCE_SHEET", "Balance Sheet", Icons.Default.ReceiptLong)
    object Peoples : NavRouteItem("PEOPLES", "Peoples Directory", Icons.Default.Groups)
    object Contacts : NavRouteItem("CONTACTS", "Contacts", Icons.Default.ContactPhone)
    object PricingRates : NavRouteItem("PRICING_RATES", "Pricing Rates", Icons.Default.PriceChange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: HomeViewModel,
    houseOwnerViewModel: HouseOwnerViewModel,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onSwitchRoleClicked: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val user = uiState.currentUser
    val isAdmin = user?.isAdminOrSuperAdmin == true

    val unpaidMonthsCount = uiState.unpaidMonths.size

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val msg = uiState.statusMessage ?: uiState.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
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
                    // Side Drawer Profile Header - Deep Teal with Gold/Amber badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF006A60))
                            .padding(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            val userName = user?.name?.ifBlank { "Resident" } ?: "Resident"
                            val firstLetter = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "R"

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
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    modifier = Modifier.size(16.dp),
                                    tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = user?.phoneNumber ?: "N/A",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Role clearly displayed in Profile Header with Gold/Amber Badge
                            val roleBadgeText = when (user?.role) {
                                UserRole.SUPER_ADMIN -> "Super Admin"
                                UserRole.ADMIN -> "Admin"
                                UserRole.USER, UserRole.HOUSE_OWNER -> "User"
                                null -> "User"
                            }
                            Surface(
                                color = androidx.compose.ui.graphics.Color(0xFFFFD54F),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = roleBadgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Left Navigation Drawer Links
                    val isHomeSelected = uiState.selectedNavRoute in listOf(
                        NavRouteItem.Home.route,
                        NavRouteItem.Announcements.route,
                        NavRouteItem.Requests.route,
                        NavRouteItem.Payments.route,
                        NavRouteItem.Concerns.route
                    )
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        selected = isHomeSelected,
                        onClick = {
                            viewModel.setSelectedNavRoute(NavRouteItem.Announcements.route)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Balance Sheet Drawer Item (Visible to Everyone!)
                    NavigationDrawerItem(
                        label = { Text("Balance Sheet") },
                        icon = { Icon(NavRouteItem.BalanceSheet.icon, contentDescription = null) },
                        selected = uiState.selectedNavRoute == NavRouteItem.BalanceSheet.route,
                        onClick = {
                            viewModel.setSelectedNavRoute(NavRouteItem.BalanceSheet.route)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Peoples Directory Drawer Item (Visible to Everyone!)
                    NavigationDrawerItem(
                        label = { Text("Peoples") },
                        icon = { Icon(NavRouteItem.Peoples.icon, contentDescription = null) },
                        selected = uiState.selectedNavRoute == NavRouteItem.Peoples.route,
                        onClick = {
                            viewModel.setSelectedNavRoute(NavRouteItem.Peoples.route)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Contacts Drawer Item (Visible to Everyone!)
                    NavigationDrawerItem(
                        label = { Text("Contacts") },
                        icon = { Icon(NavRouteItem.Contacts.icon, contentDescription = null) },
                        selected = uiState.selectedNavRoute == NavRouteItem.Contacts.route,
                        onClick = {
                            viewModel.setSelectedNavRoute(NavRouteItem.Contacts.route)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Update Property Details Item in Left Drawer
                    NavigationDrawerItem(
                        label = { Text("Update Property Details") },
                        icon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.setShowUpdatePropertyDialog(true)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    if (isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("Pricing Rates") },
                            icon = { Icon(NavRouteItem.PricingRates.icon, contentDescription = null) },
                            selected = uiState.selectedNavRoute == NavRouteItem.PricingRates.route,
                            onClick = {
                                viewModel.setSelectedNavRoute(NavRouteItem.PricingRates.route)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    if (onSwitchRoleClicked != null) {
                        NavigationDrawerItem(
                            label = { Text("Switch Portal") },
                            icon = { Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Switch Portal") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onSwitchRoleClicked()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout Item at the bottom of the Left Navigation Drawer
                    NavigationDrawerItem(
                        label = { Text("Logout", fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
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
                // Clean TopAppBar in Deep Teal (#004D40) with White Text
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color.White)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = if (uiState.selectedNavRoute == NavRouteItem.Payments.route) "Maintenance" else "San Siri Residency",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val roleSubtitle = when (user?.role) {
                                UserRole.SUPER_ADMIN -> "Super Admin"
                                UserRole.ADMIN -> "Admin"
                                UserRole.HOUSE_OWNER, UserRole.USER -> "House Owner"
                                null -> "House Owner"
                            }
                            Text(
                                text = roleSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.setShowNotificationCenterDialog(true) }) {
                            if (uiState.unreadNotificationCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(
                                                text = if (uiState.unreadNotificationCount > 99) "99+" else uiState.unreadNotificationCount.toString()
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
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
                val isMainHomeRoute = uiState.selectedNavRoute in listOf(
                    NavRouteItem.Home.route,
                    NavRouteItem.Announcements.route,
                    NavRouteItem.Requests.route,
                    NavRouteItem.Payments.route,
                    NavRouteItem.Concerns.route,
                    "HOME"
                )
                if (isMainHomeRoute) {
                    // 4 Main Navigation Tabs Bottom Bar (Increased height: 100.dp)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.height(100.dp)
                    ) {
                        // 1st Tab: Announcements
                        NavigationBarItem(
                            selected = uiState.selectedNavRoute == NavRouteItem.Announcements.route || uiState.selectedNavRoute == NavRouteItem.Home.route || uiState.selectedNavRoute == "HOME",
                            onClick = { viewModel.setSelectedNavRoute(NavRouteItem.Announcements.route) },
                            icon = { Icon(imageVector = NavRouteItem.Announcements.icon, contentDescription = NavRouteItem.Announcements.title, modifier = Modifier.size(24.dp)) },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        // 2nd Tab: Requests
                        NavigationBarItem(
                            selected = uiState.selectedNavRoute == NavRouteItem.Requests.route,
                            onClick = { viewModel.setSelectedNavRoute(NavRouteItem.Requests.route) },
                            icon = { Icon(imageVector = NavRouteItem.Requests.icon, contentDescription = NavRouteItem.Requests.title, modifier = Modifier.size(24.dp)) },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        // 3rd Tab: Payments
                        NavigationBarItem(
                            selected = uiState.selectedNavRoute == NavRouteItem.Payments.route,
                            onClick = { viewModel.setSelectedNavRoute(NavRouteItem.Payments.route) },
                            icon = {
                                if (unpaidMonthsCount > 0) {
                                    BadgedBox(
                                        badge = { Badge { Text(unpaidMonthsCount.toString()) } }
                                    ) {
                                        Icon(imageVector = NavRouteItem.Payments.icon, contentDescription = NavRouteItem.Payments.title, modifier = Modifier.size(24.dp))
                                    }
                                } else {
                                    Icon(imageVector = NavRouteItem.Payments.icon, contentDescription = NavRouteItem.Payments.title, modifier = Modifier.size(24.dp))
                                }
                            },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        // 4th Tab: Concerns
                        NavigationBarItem(
                            selected = uiState.selectedNavRoute == NavRouteItem.Concerns.route,
                            onClick = { viewModel.setSelectedNavRoute(NavRouteItem.Concerns.route) },
                            icon = { Icon(imageVector = NavRouteItem.Concerns.icon, contentDescription = NavRouteItem.Concerns.title, modifier = Modifier.size(24.dp)) },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
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
                // Smooth Animated Tab Transitions
                AnimatedContent(
                    targetState = uiState.selectedNavRoute,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "TabSwitchAnimation"
                ) { targetRoute ->
                    when (targetRoute) {
                        NavRouteItem.Home.route, "HOME", NavRouteItem.Announcements.route -> AnnouncementsTab(
                            announcements = uiState.announcements,
                            currentUser = user,
                            onPostAnnouncementClicked = { viewModel.setShowCreateAnnouncementDialog(true) },
                            onDeleteAnnouncementClicked = { id -> viewModel.deleteAnnouncement(id) }
                        )

                        NavRouteItem.Requests.route -> RequestsTab(
                            requests = uiState.residentRequests,
                            requestComments = uiState.requestComments,
                            currentUser = user,
                            onNewRequestClicked = { viewModel.setShowCreateRequestDialog(true) },
                            onUpdateStatusClicked = { id, status -> viewModel.updateResidentRequestStatus(id, status) },
                            onAddCommentClicked = { id, commentText -> viewModel.addRequestComment(id, commentText) }
                        )

                        NavRouteItem.Payments.route -> PaymentStatus4MonthsScreen(
                            monthlyStatuses = uiState.monthlyPaymentStatuses,
                            unpaidMonths = uiState.unpaidMonths,
                            myHouseDetail = uiState.myHouseDetail,
                            userHouses = uiState.userHouses,
                            pricingRules = uiState.pricingRules,
                            isAdmin = user?.role == UserRole.SUPER_ADMIN || user?.role == UserRole.ADMIN,
                            onApprovePayment = { houseId, monthYear ->
                                viewModel.approveMonthlyStatus(houseId, monthYear)
                            },
                            onSubmitPayment = { selectedMonths, transactionRef, totalAmount ->
                                viewModel.submitMultiMonthPayment(selectedMonths, transactionRef, totalAmount)
                            }
                        )

                        NavRouteItem.Concerns.route -> ConcernsTab(
                            concerns = uiState.residentRequests,
                            currentUser = user,
                            onRaiseConcernClicked = { viewModel.setShowCreateConcernDialog(true) },
                            onUpdateStatusClicked = { id, status -> viewModel.updateResidentRequestStatus(id, status) }
                        )

                        NavRouteItem.BalanceSheet.route -> BalanceSheetScreen(
                            transactions = uiState.financialTransactions,
                            currentUser = user,
                            onAddTransaction = { type, amount, category, description, dateMillis ->
                                viewModel.addFinancialTransaction(type, amount, category, description, dateMillis)
                            }
                        )

                        NavRouteItem.Peoples.route -> PeoplesDirectoryScreen(
                            houses = uiState.approvedHouses,
                            allUsers = uiState.allUsers,
                            crossRepresentatives = uiState.crossRepresentatives,
                            currentUser = user,
                            onAssignCrossRepresentative = { cross, userUid ->
                                viewModel.assignCrossRepresentative(cross, userUid)
                            },
                            onUpdateUserRoleAndDetails = { targetUid, name, houseNum, cross, occupancy, role ->
                                viewModel.updateUserRoleAndBuildingDetails(targetUid, name, houseNum, cross, occupancy, role)
                            },
                            onAddNewUser = { name, phone, flat, cross, occupancy, role ->
                                viewModel.adminRegisterUser(name, phone, flat, cross, occupancy, role)
                            },
                            onAddPropertyForUser = { targetUid, houseNum, cross, occupancy, propType ->
                                viewModel.addPropertyForUser(targetUid, houseNum, cross, occupancy, propType)
                            }
                        )

                        NavRouteItem.Contacts.route -> SocietyContactsScreen(
                            contacts = uiState.societyContacts,
                            currentUser = user,
                            onAddContact = { category, name, phone ->
                                viewModel.addSocietyContact(category, name, phone)
                            },
                            onUpdateContact = { contact ->
                                viewModel.updateSocietyContact(contact)
                            },
                            onDeleteContact = { contactId ->
                                viewModel.deleteSocietyContact(contactId)
                            }
                        )

                        NavRouteItem.PricingRates.route -> PricingRulesConfigScreen(
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

                        else -> AnnouncementsTab(
                            announcements = uiState.announcements,
                            currentUser = user,
                            onPostAnnouncementClicked = { viewModel.setShowCreateAnnouncementDialog(true) },
                            onDeleteAnnouncementClicked = { id -> viewModel.deleteAnnouncement(id) }
                        )
                    }
                }

                // Action Dialogs with Popup Animations
                AnimatedVisibility(
                    visible = uiState.showCreateAnnouncementDialog,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    CreateAnnouncementDialog(
                        onPostAnnouncement = { title, content -> viewModel.createAnnouncement(title, content) },
                        onDismiss = { viewModel.setShowCreateAnnouncementDialog(false) }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showCreateRequestDialog || uiState.showCreateResidentRequestDialog,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    CreateRequestDialog(
                        onRaiseRequest = { title, desc, _ ->
                            viewModel.createResidentRequest(title, desc, category = "REQUEST")
                        },
                        onDismiss = {
                            viewModel.setShowCreateRequestDialog(false)
                            viewModel.setShowCreateResidentRequestDialog(false)
                        }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showCreateConcernDialog,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    CreateComplaintDialog(
                        onRaiseComplaint = { title, desc ->
                            viewModel.createResidentRequest(title, desc, category = "CONCERN")
                        },
                        onDismiss = { viewModel.setShowCreateConcernDialog(false) }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showUpdatePropertyDialog,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    UpdatePropertyDialog(
                        userHouses = uiState.userHouses,
                        myHouseDetail = uiState.myHouseDetail,
                        onSaveProperties = { updatedHouses ->
                            viewModel.saveHousesForUser(updatedHouses)
                            viewModel.setShowUpdatePropertyDialog(false)
                        },
                        onDismiss = { viewModel.setShowUpdatePropertyDialog(false) }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showNotificationCenterDialog,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    NotificationCenterDialog(
                        notifications = uiState.notifications,
                        onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                        onClearAll = { viewModel.clearNotifications() },
                        onDismiss = { viewModel.setShowNotificationCenterDialog(false) }
                    )
                }
            }
        }
    }
}
