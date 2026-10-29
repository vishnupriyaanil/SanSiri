package com.anil.sansiri.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anil.sansiri.data.model.Announcement
import com.anil.sansiri.data.model.RequestStatus
import com.anil.sansiri.data.model.ResidentRequest
import com.anil.sansiri.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onPostAnnouncementClicked: () -> Unit,
    onDeleteAnnouncement: (String) -> Unit,
    onRaiseConcernClicked: () -> Unit,
    onUpdateConcernStatus: (String, RequestStatus) -> Unit,
    onNavigateToPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = uiState.currentUser
    val isAdmin = user?.isAdminOrSuperAdmin == true
    val pendingMonthsCount = uiState.unpaidMonths.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Maintenance Summary Quick Indicator Card
        item {
            MaintenanceSummaryCard(
                pendingMonthsCount = pendingMonthsCount,
                unpaidMonths = uiState.unpaidMonths,
                houseNumber = uiState.myHouseDetail?.displayHouseOrSiteNumber ?: "A-101",
                onNavigateToPay = onNavigateToPay
            )
        }

        // 2. Announcements Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Announcements",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Community Announcements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isAdmin) {
                    Button(
                        onClick = onPostAnnouncementClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006A60),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Post",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post Announcement")
                    }
                }
            }
        }

        if (uiState.announcements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No community announcements yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(uiState.announcements, key = { it.id }) { announcement ->
                AnnouncementItemCard(
                    announcement = announcement,
                    isAdmin = isAdmin,
                    onDelete = { onDeleteAnnouncement(announcement.id) }
                )
            }
        }

        // 3. Requests & Concerns Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Concerns",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Resident Concerns & Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRaiseConcernClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006A60),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Raise Concern",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Raise Concern")
                }
            }
        }

        if (uiState.residentRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No resident concerns or requests reported.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(uiState.residentRequests, key = { it.id }) { request ->
                ResidentRequestItemCard(
                    request = request,
                    isAdmin = isAdmin,
                    onUpdateStatus = { newStatus -> onUpdateConcernStatus(request.id, newStatus) }
                )
            }
        }
    }
}

@Composable
private fun MaintenanceSummaryCard(
    pendingMonthsCount: Int,
    unpaidMonths: List<String>,
    houseNumber: String,
    onNavigateToPay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pendingMonthsCount > 0) Color(0xFFFFEBEE) else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(1.dp, if (pendingMonthsCount > 0) Color(0xFFEF9A9A) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (pendingMonthsCount > 0) Color(0xFFFFCDD2) else Color(0xFFCEE8E2),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (pendingMonthsCount > 0) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (pendingMonthsCount > 0) Color(0xFFC62828) else Color(0xFF006A60)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Maintenance Status ($houseNumber)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (pendingMonthsCount > 0) "$pendingMonthsCount Pending Month(s)" else "All Maintenance Dues Paid!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (pendingMonthsCount > 0) Color(0xFFC62828) else Color(0xFF006A60)
                        )
                    }
                }

                if (pendingMonthsCount > 0) {
                    Button(
                        onClick = onNavigateToPay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC62828),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Pay", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Now")
                    }
                }
            }

            if (pendingMonthsCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pending: ${unpaidMonths.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun AnnouncementItemCard(
    announcement: Announcement,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Posted by ${announcement.postedByName} • ${formatTimestamp(announcement.createdAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAdmin) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ResidentRequestItemCard(
    request: ResidentRequest,
    isAdmin: Boolean,
    onUpdateStatus: (RequestStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val (statusColor, statusBg, statusIcon) = when (request.status) {
        RequestStatus.OPEN -> Triple(Color(0xFFC62828), Color(0xFFFFEBEE), Icons.Default.Error)
        RequestStatus.IN_PROGRESS -> Triple(Color(0xFFE65100), Color(0xFFFFF3E0), Icons.Default.HourglassTop)
        RequestStatus.RESOLVED -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = request.status.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Flat ${request.flatNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006A60)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = request.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Raised by ${request.userName} • ${formatTimestamp(request.createdAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAdmin) {
                    Box {
                        OutlinedButton(
                            onClick = { showMenu = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change Status")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("OPEN") },
                                onClick = {
                                    showMenu = false
                                    onUpdateStatus(RequestStatus.OPEN)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("IN_PROGRESS") },
                                onClick = {
                                    showMenu = false
                                    onUpdateStatus(RequestStatus.IN_PROGRESS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("RESOLVED") },
                                onClick = {
                                    showMenu = false
                                    onUpdateStatus(RequestStatus.RESOLVED)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Recently"
    }
}

@Composable
fun CreateResidentRequestDialog(
    onRaiseRequest: (title: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise Resident Concern", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Summary") },
                    placeholder = { Text("e.g. Elevator noise, Water leak") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRaiseRequest(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Submit Concern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
