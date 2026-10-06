package com.anil.sansiri.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    object PricingRules : AdminTabItem("Pricing", Icons.Default.Settings, 2)
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

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val message = uiState.statusMessage ?: uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "San Siri Admin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isSuperAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isSuperAdmin) "SUPER ADMIN" else "ADMIN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuperAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (onSwitchRoleClicked != null) {
                        IconButton(onClick = onSwitchRoleClicked) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onLogoutClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = { Text(tab.title) }
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
                    pricingRules = uiState.pricingRules,
                    onSelfOccupiedRateChanged = { rate -> viewModel.onSelfOccupiedRateChanged(rate) },
                    onRentedRateChanged = { rate -> viewModel.onRentedRateChanged(rate) },
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
                    newAdminPhone = uiState.newAdminPhone,
                    newAdminName = uiState.newAdminName,
                    onPhoneChanged = { viewModel.onNewAdminPhoneChanged(it) },
                    onNameChanged = { viewModel.onNewAdminNameChanged(it) },
                    onAddAdmin = { viewModel.addAdmin() }
                )
            }
        }
    }
}
