package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import com.example.kdyaimap.ui.viewmodel.AdminViewModel
import com.example.kdyaimap.ui.viewmodel.AdminState
import com.example.kdyaimap.util.BackupWorker
import com.example.kdyaimap.util.DatabaseBackupHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = adminState) {
            is AdminState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AdminState.Success -> {
                TabbedAdminView(
                    state = state,
                    onApprove = { adminViewModel.approveEvent(it) },
                    onReject = { adminViewModel.rejectEvent(it) },
                    onRoleChange = { userId, role -> adminViewModel.updateUserRole(userId, role) }
                )
            }
            is AdminState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun TabbedAdminView(
    state: AdminState.Success,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onRoleChange: (Long, UserRole) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Pending Events", "User Management", "Database")
    val context = LocalContext.current

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> DashboardScreen(state = state)
            1 -> PendingEventsList(
                events = state.pendingEvents,
                onApprove = onApprove,
                onReject = onReject
            )
            2 -> UserManagementList(
                users = state.users,
                onRoleChange = onRoleChange
            )
            3 -> DatabaseBackupScreen(
                onBackup = {
                    val backupWorkRequest = OneTimeWorkRequestBuilder<BackupWorker>().build()
                    WorkManager.getInstance(context).enqueue(backupWorkRequest)
                },
                onRestore = {
                    val backupPath = context.getExternalFilesDir(null)?.absolutePath + "/campus_nav_db.bak"
                    DatabaseBackupHelper.restoreDatabase(context, backupPath)
                }
            )
        }
    }
}

@Composable
fun DashboardScreen(state: AdminState.Success) {
    val eventStatusCounts = state.pendingEvents.groupingBy { it.status }.eachCount()
    val eventTypeCounts = state.pendingEvents.groupingBy { it.eventType }.eachCount()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "活动统计概览",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // 状态统计卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "按状态分布",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                eventStatusCounts.forEach { (status, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = status.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // 类型统计卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "按类型分布",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                eventTypeCounts.forEach { (type, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // 总体统计
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "总体统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "待审核活动总数",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = state.pendingEvents.size.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "用户总数",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = state.users.size.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PendingEventsList(
    events: List<CampusEvent>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events, key = { it.id }) { event ->
            PendingEventCard(
                event = event,
                onApprove = { onApprove(event.id) },
                onReject = { onReject(event.id) }
            )
        }
    }
}

@Composable
fun UserManagementList(
    users: List<User>,
    onRoleChange: (Long, UserRole) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users, key = { it.id }) { user ->
            UserCard(
                user = user,
                onRoleChange = { newRole -> onRoleChange(user.id, newRole) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    user: User,
    onRoleChange: (UserRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = UserRole.values()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = user.username, style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = user.role.name,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        true
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.name) },
                            onClick = {
                                onRoleChange(role)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseBackupScreen(
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onBackup) {
            Text("Backup Database")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRestore) {
            Text("Restore Database")
        }
    }
}

@Composable
fun PendingEventCard(
    event: CampusEvent,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onReject, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove) {
                    Text("Approve")
                }
            }
        }
    }
}