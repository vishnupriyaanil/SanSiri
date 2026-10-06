package com.anil.sansiri.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "San Siri Residency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.currentUser?.name ?: "House Owner Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "OWNER",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (onSwitchRoleClicked != null) {
                        IconButton(onClick = onSwitchRoleClicked) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    IconButton(onClick = onLogoutClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                        label = { Text(tab.title) }
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
                houseNumber = uiState.houseDetail?.houseNumber ?: uiState.houseNumber
            )

            // Screen Content based on selected tab
            when (uiState.selectedTab) {
                0 -> HouseRegistrationScreen(
                    uiState = uiState,
                    onBuildingNameChanged = viewModel::onBuildingNameChanged,
                    onHouseNumberChanged = viewModel::onHouseNumberChanged,
                    onCrossStreetChanged = viewModel::onCrossStreetChanged,
                    onOccupancyTypeChanged = viewModel::onOccupancyTypeChanged,
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
            "Status: Your flat registration ($houseNumber) is pending Admin approval."
        )
        ApprovalStatus.APPROVED -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            "Status: Flat $houseNumber is verified and approved."
        )
        ApprovalStatus.REJECTED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFB71C1C),
            "Status: Flat registration ($houseNumber) was rejected by Admin."
        )
        null -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF0D47A1),
            "Status: Flat details not registered yet. Please register below."
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
