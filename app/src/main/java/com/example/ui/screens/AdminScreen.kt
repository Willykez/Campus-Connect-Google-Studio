@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.CampusCourse
import com.example.data.model.CampusEntity
import com.example.data.model.CampusProgramme
import com.example.data.model.CampusVenue
import com.example.data.model.Role
import com.example.data.model.ServerStatus
import com.example.data.model.UserProfile
import com.example.ui.components.AppBottomSheet
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberLight
import com.example.viewmodel.CampusViewModel
import com.example.viewmodel.ScreenTab

@Composable
fun AdminScreen(
  viewModel: CampusViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler { viewModel.setTab(ScreenTab.HOME) }

  val currentUser by viewModel.currentUser.collectAsState()
  val pendingUsers by viewModel.pendingUsers.collectAsState()
  val allRegisteredUsers by viewModel.allRegisteredUsers.collectAsState()
  val firestoreRoles by viewModel.firestoreRoles.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()
  val campuses by viewModel.campuses.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var suspendingUserId by remember { mutableStateOf<String?>(null) }
  var suspensionReasonText by remember { mutableStateOf("Violation of University Academic Regulations") }
  var changingRoleUser by remember { mutableStateOf<UserProfile?>(null) }
  var purgingCampusId by remember { mutableStateOf<String?>(null) }

  // Dialog & BottomSheet states for Super Admin & Campus Admin
  var showAddCampusDialog by remember { mutableStateOf(false) }
  var showAddCourseDialog by remember { mutableStateOf(false) }
  var showAddVenueDialog by remember { mutableStateOf(false) }
  var showAddProgrammeDialog by remember { mutableStateOf(false) }

  // Setup List Edit and Delete states
  var editingCourse by remember { mutableStateOf<CampusCourse?>(null) }
  var deletingCourse by remember { mutableStateOf<CampusCourse?>(null) }
  var editingProgramme by remember { mutableStateOf<CampusProgramme?>(null) }
  var deletingProgramme by remember { mutableStateOf<CampusProgramme?>(null) }
  var editingVenue by remember { mutableStateOf<CampusVenue?>(null) }
  var deletingVenue by remember { mutableStateOf<CampusVenue?>(null) }
  var editingCampus by remember { mutableStateOf<CampusEntity?>(null) }
  var deletingCampus by remember { mutableStateOf<CampusEntity?>(null) }

  // Access guard: Restricted exclusively to Campus Admin and Super Admin
  if (currentUser.role != Role.ADMIN && currentUser.role != Role.SUPER_ADMIN) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(24.dp)
        .testTag("admin_access_restricted_guard"),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(DangerRedLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Access Denied",
              tint = DangerRed,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Administrator Access Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DangerRed
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Your current account (${currentUser.fullName} • ${currentUser.role.name}) does not have administrative privileges. Campus management, user approvals, and database provisioning are strictly restricted to Administrators.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.setTab(ScreenTab.HOME) },
              modifier = Modifier.weight(1f).testTag("return_home_btn")
            ) {
              Text("Return Home")
            }

            Button(
              onClick = {
                val adminPersona = viewModel.allPersonas.firstOrNull { it.role == Role.ADMIN }
                if (adminPersona != null) {
                  viewModel.switchUser(adminPersona)
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
              modifier = Modifier.weight(1f).testTag("switch_to_admin_btn")
            ) {
              Text("Switch to Admin")
            }
          }
        }
      }
    }
    return
  }

  val isSuperAdmin = currentUser.role == Role.SUPER_ADMIN

  // Campus Admin is strictly restricted to their own campus:
  val campusPendingUsers = remember(pendingUsers, currentUser.university, isSuperAdmin) {
    if (isSuperAdmin) pendingUsers else pendingUsers.filter { it.university == currentUser.university }
  }

  val campusEnrolledUsers = remember(allRegisteredUsers, currentUser.university, isSuperAdmin) {
    if (isSuperAdmin) allRegisteredUsers else allRegisteredUsers.filter { it.university == currentUser.university }
  }

  val currentCampusEntity = remember(campuses, currentUser.university) {
    campuses.firstOrNull { it.name.equals(currentUser.university, ignoreCase = true) }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Header: Distinguishes Super Admin (App Manager) vs Campus Admin
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isSuperAdmin) "Global App Manager & DB Control" else "${currentUser.university.substringBefore("(").trim()} Admin",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
        Text(
          text = if (isSuperAdmin) "Multi-campus database provisioning & server health" else "Manage courses, rooms, approvals & enrolled students",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSuperAdmin) Color(0xFFF3E8FF) else Color(0xFFDCFCE7),
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isSuperAdmin) Icons.Default.Dns else Icons.Default.Security,
            contentDescription = null,
            tint = if (isSuperAdmin) Color(0xFF6B21A8) else Color(0xFF166534),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isSuperAdmin) "Super Admin" else "Campus Admin",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSuperAdmin) Color(0xFF6B21A8) else Color(0xFF166534),
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // TABS SPECIFIC TO ROLE
    if (isSuperAdmin) {
      // Super Admin Tabs: Campuses & Databases, Global Audit Log
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DeepBlue,
        edgePadding = 0.dp
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Campuses & DBs (${campuses.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
            }
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Global Audit Log", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
            }
          }
        )
      }
    } else {
      // Campus Admin Tabs: Approvals, Accounts, Courses/Programmes, Rooms/Venues
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DeepBlue,
        edgePadding = 0.dp
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              text = "Approvals (${campusPendingUsers.size})",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              text = "Enrolled (${campusEnrolledUsers.size})",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = {
            val coursesCount = currentCampusEntity?.courses?.size ?: 4
            Text(
              text = "Courses ($coursesCount)",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = {
            val venuesCount = currentCampusEntity?.venues?.size ?: 4
            Text(
              text = "Venues ($venuesCount)",
              fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // -------------------------------------------------------------
    // SUPER ADMIN VIEW
    // -------------------------------------------------------------
    if (isSuperAdmin) {
      when (selectedTab) {
        0 -> {
          // Campuses & Database Provisioning
          Column(modifier = Modifier.weight(1f)) {
            Button(
              onClick = { showAddCampusDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("btn_add_campus_db")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Add Campus & Provision Cloud Database", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(campuses) { campus ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .testTag("campus_card_${campus.id}")
                ) {
                  Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                          modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3E8FF)),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = campus.shortCode.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B21A8)
                          )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                          Text(
                            text = campus.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                          )
                          Text(
                            text = "DB: ${campus.dbName} • Region: ${campus.dbRegion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }

                      // Server Status Badge
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (campus.serverStatus) {
                          ServerStatus.ONLINE -> Color(0xFFDCFCE7)
                          ServerStatus.DEGRADED -> WarmAmberLight
                          ServerStatus.MAINTENANCE -> DeepBlue.copy(alpha = 0.12f)
                          ServerStatus.OFFLINE -> DangerRedLight
                        }
                      ) {
                        Text(
                          text = campus.serverStatus.name,
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = when (campus.serverStatus) {
                            ServerStatus.ONLINE -> SuccessGreen
                            ServerStatus.DEGRADED -> Color(0xFFB45309)
                            ServerStatus.MAINTENANCE -> DeepBlue
                            ServerStatus.OFFLINE -> DangerRed
                          },
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                      text = "Campus Admin: ${campus.adminName} (${campus.adminEmail})",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                      Text(
                        text = "Students: ${campus.totalStudents}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                      )
                      Text(
                        text = "Courses: ${campus.courses.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                      )
                      Text(
                        text = "Venues: ${campus.venues.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                      )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Super Admin Control Buttons
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        OutlinedButton(
                          onClick = {
                            val nextStatus = when (campus.serverStatus) {
                              ServerStatus.ONLINE -> ServerStatus.MAINTENANCE
                              ServerStatus.MAINTENANCE -> ServerStatus.ONLINE
                              else -> ServerStatus.ONLINE
                            }
                            viewModel.updateCampusServerStatus(campus.id, nextStatus)
                          },
                          modifier = Modifier.testTag("toggle_status_${campus.id}")
                        ) {
                          Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(13.dp))
                          Spacer(modifier = Modifier.width(3.dp))
                          Text(
                            text = if (campus.serverStatus == ServerStatus.ONLINE) "Maintenance" else "Online",
                            fontSize = 11.sp
                          )
                        }

                        OutlinedButton(
                          onClick = { purgingCampusId = campus.id },
                          colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                          modifier = Modifier.testTag("purge_data_${campus.id}")
                        ) {
                          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp), tint = DangerRed)
                          Spacer(modifier = Modifier.width(3.dp))
                          Text("Purge Data", fontSize = 11.sp, color = DangerRed)
                        }

                        IconButton(
                          onClick = { editingCampus = campus },
                          modifier = Modifier.size(32.dp).testTag("edit_campus_${campus.id}")
                        ) {
                          Icon(Icons.Default.Edit, contentDescription = "Edit Campus", tint = DeepBlue, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                          onClick = { deletingCampus = campus },
                          modifier = Modifier.size(32.dp).testTag("delete_campus_${campus.id}")
                        ) {
                          Icon(Icons.Default.Delete, contentDescription = "Delete Campus", tint = DangerRed, modifier = Modifier.size(16.dp))
                        }
                      }
                    }
                  }
                }
              }

              item {
                Spacer(modifier = Modifier.height(24.dp))
              }
            }
          }
        }
        1 -> {
          // Global Audit Log
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(auditLogs) { log ->
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = log.action,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = DeepBlue
                    )
                    Text(
                      text = log.timestamp,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(text = log.details, style = MaterialTheme.typography.bodyMedium)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = "Actor: ${log.actor}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }

            item {
              Spacer(modifier = Modifier.height(24.dp))
            }
          }
        }
      }
    } else {
      // -------------------------------------------------------------
      // CAMPUS ADMIN VIEW (Scoped strictly to currentUser.university)
      // -------------------------------------------------------------
      when (selectedTab) {
        0 -> {
          // Tab 0: Pending Approvals for THIS campus
          if (campusPendingUsers.isEmpty()) {
            Box(
              modifier = Modifier.weight(1f).fillMaxWidth(),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "No pending approvals for ${currentUser.university}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(campusPendingUsers) { userItem ->
                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.fillMaxWidth().testTag("pending_user_${userItem.id}")
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(text = userItem.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(text = userItem.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                          text = "Reg No: ${userItem.studentNumber ?: userItem.staffNumber ?: "Pending"} • ${userItem.programme}",
                          style = MaterialTheme.typography.labelSmall,
                          color = DeepBlue,
                          fontWeight = FontWeight.Medium
                        )
                      }
                      StatusBadge(text = userItem.role.name, backgroundColor = WarmAmberLight, contentColor = Color(0xFFB45309))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.End,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      OutlinedButton(
                        onClick = { viewModel.rejectUser(userItem.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        modifier = Modifier.testTag("reject_user_${userItem.id}")
                      ) {
                        Text("Reject")
                      }

                      Spacer(modifier = Modifier.width(8.dp))

                      Button(
                        onClick = { viewModel.approveUser(userItem.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.testTag("approve_user_${userItem.id}")
                      ) {
                        Text("Approve Account")
                      }
                    }
                  }
                }
              }

              item { Spacer(modifier = Modifier.height(24.dp)) }
            }
          }
        }

        1 -> {
          // Tab 1: Enrolled Accounts for THIS campus
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(campusEnrolledUsers) { userItem ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("enrolled_user_${userItem.id}")
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(text = userItem.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                      Text(
                        text = "${userItem.studentNumber ?: userItem.staffNumber ?: ""} • ${userItem.programme}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    StatusBadge(
                      text = userItem.status.name,
                      backgroundColor = if (userItem.status == AccountStatus.APPROVED) Color(0xFFDCFCE7) else DangerRedLight,
                      contentColor = if (userItem.status == AccountStatus.APPROVED) SuccessGreen else DangerRed
                    )
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (userItem.status == AccountStatus.APPROVED && userItem.role != Role.ADMIN) {
                      TextButton(
                        onClick = { suspendingUserId = userItem.id },
                        modifier = Modifier.testTag("suspend_btn_${userItem.id}")
                      ) {
                        Text("Suspend", color = DangerRed)
                      }
                    } else if (userItem.status == AccountStatus.SUSPENDED) {
                      TextButton(
                        onClick = { viewModel.reinstateUser(userItem.id) }
                      ) {
                        Text("Lift Suspension", color = SuccessGreen)
                      }
                    }
                  }
                }
              }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
          }
        }

        2 -> {
          // Tab 2: Programmes & Courses (Created by this Campus Admin!)
          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { showAddCourseDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("btn_create_course")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Course", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = { showAddProgrammeDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("btn_create_programme")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Programme", fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val courses = viewModel.getCoursesForUniversity(currentUser.university)
            val programmes = viewModel.getProgrammesForUniversity(currentUser.university)

            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Text("Verified Course Offerings (${courses.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeepBlue)
              }

              items(courses) { course ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.fillMaxWidth().testTag("course_card_${course.code}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = "${course.code}: ${course.title}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                      Text(text = "Instructor: ${course.lecturerName} • ${course.programmeName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        onClick = { editingCourse = course },
                        modifier = Modifier.size(32.dp).testTag("edit_course_${course.code}")
                      ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Course", tint = DeepBlue, modifier = Modifier.size(16.dp))
                      }
                      IconButton(
                        onClick = { deletingCourse = course },
                        modifier = Modifier.size(32.dp).testTag("delete_course_${course.code}")
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Course", tint = DangerRed, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }

              item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Academic Programmes (${programmes.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeepBlue)
              }

              items(programmes) { prog ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                  modifier = Modifier.fillMaxWidth().testTag("programme_card_${prog.code}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = "${prog.code} - ${prog.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                      Text(text = prog.department, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        onClick = { editingProgramme = prog },
                        modifier = Modifier.size(32.dp).testTag("edit_programme_${prog.code}")
                      ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Programme", tint = DeepBlue, modifier = Modifier.size(16.dp))
                      }
                      IconButton(
                        onClick = { deletingProgramme = prog },
                        modifier = Modifier.size(32.dp).testTag("delete_programme_${prog.code}")
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Programme", tint = DangerRed, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }

              item { Spacer(modifier = Modifier.height(24.dp)) }
            }
          }
        }

        3 -> {
          // Tab 3: Venues & Rooms for THIS campus
          Column(modifier = Modifier.weight(1f)) {
            Button(
              onClick = { showAddVenueDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("btn_create_venue")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add Campus Room / Venue")
            }

            Spacer(modifier = Modifier.height(10.dp))

            val venues = viewModel.getVenuesForUniversity(currentUser.university)

            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(venues) { venue ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.fillMaxWidth().testTag("venue_card_${venue.name}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = venue.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                      Text(text = "${venue.building} • Projector: ${if (venue.hasProjector) "Equipped" else "None"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                        Text("Cap: ${venue.capacity}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = SuccessGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                      }
                      Spacer(modifier = Modifier.width(4.dp))
                      IconButton(
                        onClick = { editingVenue = venue },
                        modifier = Modifier.size(32.dp).testTag("edit_venue_${venue.name}")
                      ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Venue", tint = DeepBlue, modifier = Modifier.size(16.dp))
                      }
                      IconButton(
                        onClick = { deletingVenue = venue },
                        modifier = Modifier.size(32.dp).testTag("delete_venue_${venue.name}")
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Venue", tint = DangerRed, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }

              item { Spacer(modifier = Modifier.height(24.dp)) }
            }
          }
        }
      }
    }
  }

  // -------------------------------------------------------------
  // MATERIAL 3 BOTTOM SHEETS (CRUD FOR ALL SETUP LISTS)
  // -------------------------------------------------------------

  // 1. Super Admin: Provision New Campus Bottom Sheet
  if (showAddCampusDialog) {
    var campusName by remember { mutableStateOf("") }
    var shortCode by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("af-south-1 (Johannesburg)") }

    AppBottomSheet(
      onDismissRequest = { showAddCampusDialog = false },
      modifier = Modifier.testTag("add_campus_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Provision New Campus Database", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { showAddCampusDialog = false }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = campusName,
        onValueChange = { campusName = it },
        label = { Text("University / Campus Name") },
        placeholder = { Text("e.g. St. Augustine University (SAUT)") },
        modifier = Modifier.fillMaxWidth().testTag("input_new_campus_name")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = shortCode,
        onValueChange = { shortCode = it },
        label = { Text("Short Code (e.g. SAUT)") },
        modifier = Modifier.fillMaxWidth().testTag("input_new_campus_code")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = adminName,
        onValueChange = { adminName = it },
        label = { Text("Appointed Campus Admin Name") },
        placeholder = { Text("e.g. Dr. Peter Mrema") },
        modifier = Modifier.fillMaxWidth().testTag("input_new_campus_admin_name")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = adminEmail,
        onValueChange = { adminEmail = it },
        label = { Text("Campus Admin Email") },
        placeholder = { Text("admin@saut.ac.tz") },
        modifier = Modifier.fillMaxWidth().testTag("input_new_campus_admin_email")
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { showAddCampusDialog = false }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (campusName.isNotBlank() && shortCode.isNotBlank() && adminEmail.isNotBlank()) {
              viewModel.addNewCampus(campusName, shortCode, adminEmail, adminName.ifBlank { "Registry Administrator" }, region)
              showAddCampusDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = campusName.isNotBlank() && shortCode.isNotBlank() && adminEmail.isNotBlank(),
          modifier = Modifier.testTag("btn_confirm_provision_campus")
        ) {
          Text("Provision Database")
        }
      }
    }
  }

  // 2. Super Admin: Edit Campus Bottom Sheet
  editingCampus?.let { campus ->
    var name by remember { mutableStateOf(campus.name) }
    var code by remember { mutableStateOf(campus.shortCode) }
    var email by remember { mutableStateOf(campus.adminEmail) }
    var region by remember { mutableStateOf(campus.dbRegion) }

    AppBottomSheet(
      onDismissRequest = { editingCampus = null },
      modifier = Modifier.testTag("edit_campus_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Edit Campus: ${campus.shortCode}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { editingCampus = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Campus Name") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = code,
        onValueChange = { code = it },
        label = { Text("Short Code") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Admin Email") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = region,
        onValueChange = { region = it },
        label = { Text("Database Region") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { editingCampus = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (name.isNotBlank() && code.isNotBlank()) {
              viewModel.editCampus(campus.id, name, code, email, region)
              editingCampus = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = name.isNotBlank() && code.isNotBlank()
        ) {
          Text("Save Changes")
        }
      }
    }
  }

  // 3. Super Admin: Delete Campus Bottom Sheet
  deletingCampus?.let { campus ->
    AppBottomSheet(
      onDismissRequest = { deletingCampus = null },
      modifier = Modifier.testTag("delete_campus_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Delete Campus: ${campus.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
        IconButton(onClick = { deletingCampus = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "This will permanently remove ${campus.name} (${campus.shortCode}), delete its database records, and invoke the Supabase Storage API to wipe all associated academic files.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { deletingCampus = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            viewModel.deleteCampus(campus.id)
            deletingCampus = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text("Confirm Permanent Delete")
        }
      }
    }
  }

  // 4. Campus Admin: Add Course Bottom Sheet
  if (showAddCourseDialog) {
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf(currentUser.fullName) }
    var programme by remember { mutableStateOf("BSc Computer Science") }

    AppBottomSheet(
      onDismissRequest = { showAddCourseDialog = false },
      modifier = Modifier.testTag("add_course_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Add Course Offering to ${currentUser.university}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { showAddCourseDialog = false }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = code,
        onValueChange = { code = it },
        label = { Text("Course Code (e.g. CS310)") },
        modifier = Modifier.fillMaxWidth().testTag("input_course_code")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Course Title") },
        placeholder = { Text("e.g. Artificial Intelligence") },
        modifier = Modifier.fillMaxWidth().testTag("input_course_title")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = lecturer,
        onValueChange = { lecturer = it },
        label = { Text("Assigned Lecturer") },
        modifier = Modifier.fillMaxWidth().testTag("input_course_lecturer")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = programme,
        onValueChange = { programme = it },
        label = { Text("Target Programme (e.g. BSc Computer Science)") },
        modifier = Modifier.fillMaxWidth().testTag("input_course_programme")
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { showAddCourseDialog = false }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (code.isNotBlank() && title.isNotBlank()) {
              viewModel.addCourseToCampus(currentUser.university, code, title, lecturer, programme)
              showAddCourseDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = code.isNotBlank() && title.isNotBlank(),
          modifier = Modifier.testTag("btn_confirm_add_course")
        ) {
          Text("Save Course")
        }
      }
    }
  }

  // 5. Campus Admin: Edit Course Bottom Sheet
  editingCourse?.let { course ->
    var code by remember { mutableStateOf(course.code) }
    var title by remember { mutableStateOf(course.title) }
    var lecturer by remember { mutableStateOf(course.lecturerName) }
    var programme by remember { mutableStateOf(course.programmeName) }

    AppBottomSheet(
      onDismissRequest = { editingCourse = null },
      modifier = Modifier.testTag("edit_course_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Edit Course: ${course.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { editingCourse = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = code,
        onValueChange = { code = it },
        label = { Text("Course Code") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Course Title") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = lecturer,
        onValueChange = { lecturer = it },
        label = { Text("Assigned Lecturer") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = programme,
        onValueChange = { programme = it },
        label = { Text("Associated Programme") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { editingCourse = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (code.isNotBlank() && title.isNotBlank()) {
              viewModel.editCourseInCampus(currentUser.university, course.code, code, title, lecturer, programme)
              editingCourse = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = code.isNotBlank() && title.isNotBlank()
        ) {
          Text("Update Course")
        }
      }
    }
  }

  // 6. Campus Admin: Delete Course Bottom Sheet
  deletingCourse?.let { course ->
    AppBottomSheet(
      onDismissRequest = { deletingCourse = null },
      modifier = Modifier.testTag("delete_course_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Delete Course: ${course.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
        IconButton(onClick = { deletingCourse = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Are you sure you want to remove '${course.title}' (${course.code}) from ${currentUser.university}?",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { deletingCourse = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            viewModel.deleteCourseFromCampus(currentUser.university, course.code)
            deletingCourse = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text("Delete Course")
        }
      }
    }
  }

  // 7. Campus Admin: Add Programme Bottom Sheet
  if (showAddProgrammeDialog) {
    var progCode by remember { mutableStateOf("") }
    var progName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Academic Faculty") }

    AppBottomSheet(
      onDismissRequest = { showAddProgrammeDialog = false },
      modifier = Modifier.testTag("add_programme_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Add Programme Offering", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { showAddProgrammeDialog = false }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = progCode,
        onValueChange = { progCode = it },
        label = { Text("Code (e.g. LLB)") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = progName,
        onValueChange = { progName = it },
        label = { Text("Programme Name") },
        placeholder = { Text("e.g. Bachelor of Laws") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = department,
        onValueChange = { department = it },
        label = { Text("Faculty / Department") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { showAddProgrammeDialog = false }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (progCode.isNotBlank() && progName.isNotBlank()) {
              viewModel.addProgrammeToCampus(currentUser.university, progCode, progName, department)
              showAddProgrammeDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = progCode.isNotBlank() && progName.isNotBlank()
        ) {
          Text("Save Programme")
        }
      }
    }
  }

  // 8. Campus Admin: Edit Programme Bottom Sheet
  editingProgramme?.let { prog ->
    var progCode by remember { mutableStateOf(prog.code) }
    var progName by remember { mutableStateOf(prog.name) }
    var department by remember { mutableStateOf(prog.department) }

    AppBottomSheet(
      onDismissRequest = { editingProgramme = null },
      modifier = Modifier.testTag("edit_programme_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Edit Programme: ${prog.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { editingProgramme = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = progCode,
        onValueChange = { progCode = it },
        label = { Text("Programme Code") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = progName,
        onValueChange = { progName = it },
        label = { Text("Programme Name") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = department,
        onValueChange = { department = it },
        label = { Text("Faculty / Department") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { editingProgramme = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (progCode.isNotBlank() && progName.isNotBlank()) {
              viewModel.editProgrammeInCampus(currentUser.university, prog.code, progCode, progName, department)
              editingProgramme = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = progCode.isNotBlank() && progName.isNotBlank()
        ) {
          Text("Update Programme")
        }
      }
    }
  }

  // 9. Campus Admin: Delete Programme Bottom Sheet
  deletingProgramme?.let { prog ->
    AppBottomSheet(
      onDismissRequest = { deletingProgramme = null },
      modifier = Modifier.testTag("delete_programme_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Delete Programme: ${prog.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
        IconButton(onClick = { deletingProgramme = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Are you sure you want to remove programme '${prog.name}' (${prog.code}) from ${currentUser.university}?",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { deletingProgramme = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            viewModel.deleteProgrammeFromCampus(currentUser.university, prog.code)
            deletingProgramme = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text("Delete Programme")
        }
      }
    }
  }

  // 10. Campus Admin: Add Venue Bottom Sheet
  if (showAddVenueDialog) {
    var venueName by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("60") }
    var building by remember { mutableStateOf("Main Campus Block") }
    var hasProjector by remember { mutableStateOf(true) }

    AppBottomSheet(
      onDismissRequest = { showAddVenueDialog = false },
      modifier = Modifier.testTag("add_venue_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Add Room / Venue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { showAddVenueDialog = false }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = venueName,
        onValueChange = { venueName = it },
        label = { Text("Venue Name (e.g. Room 302)") },
        modifier = Modifier.fillMaxWidth().testTag("input_venue_name")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = capacityStr,
        onValueChange = { capacityStr = it },
        label = { Text("Capacity") },
        modifier = Modifier.fillMaxWidth().testTag("input_venue_capacity")
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = building,
        onValueChange = { building = it },
        label = { Text("Building / Wing") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { showAddVenueDialog = false }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            val cap = capacityStr.toIntOrNull() ?: 50
            if (venueName.isNotBlank()) {
              viewModel.addVenueToCampus(currentUser.university, venueName, cap, building, hasProjector)
              showAddVenueDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = venueName.isNotBlank(),
          modifier = Modifier.testTag("btn_confirm_add_venue")
        ) {
          Text("Save Venue")
        }
      }
    }
  }

  // 11. Campus Admin: Edit Venue Bottom Sheet
  editingVenue?.let { venue ->
    var venueName by remember { mutableStateOf(venue.name) }
    var capacityStr by remember { mutableStateOf(venue.capacity.toString()) }
    var building by remember { mutableStateOf(venue.building) }
    var hasProjector by remember { mutableStateOf(venue.hasProjector) }

    AppBottomSheet(
      onDismissRequest = { editingVenue = null },
      modifier = Modifier.testTag("edit_venue_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Edit Venue: ${venue.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { editingVenue = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = venueName,
        onValueChange = { venueName = it },
        label = { Text("Venue Name") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = capacityStr,
        onValueChange = { capacityStr = it },
        label = { Text("Capacity") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = building,
        onValueChange = { building = it },
        label = { Text("Building") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { editingVenue = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            val cap = capacityStr.toIntOrNull() ?: 50
            if (venueName.isNotBlank()) {
              viewModel.editVenueInCampus(currentUser.university, venue.name, venueName, cap, building, hasProjector)
              editingVenue = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          enabled = venueName.isNotBlank()
        ) {
          Text("Update Venue")
        }
      }
    }
  }

  // 12. Campus Admin: Delete Venue Bottom Sheet
  deletingVenue?.let { venue ->
    AppBottomSheet(
      onDismissRequest = { deletingVenue = null },
      modifier = Modifier.testTag("delete_venue_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Delete Venue: ${venue.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
        IconButton(onClick = { deletingVenue = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Are you sure you want to remove '${venue.name}' (${venue.building}) from ${currentUser.university}?",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { deletingVenue = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            viewModel.deleteVenueFromCampus(currentUser.university, venue.name)
            deletingVenue = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text("Delete Venue")
        }
      }
    }
  }

  // 13. Super Admin: Purge Data & Supabase Storage Wipe Confirmation Bottom Sheet
  purgingCampusId?.let { cId ->
    val target = campuses.firstOrNull { it.id == cId }
    AppBottomSheet(
      onDismissRequest = { purgingCampusId = null },
      modifier = Modifier.testTag("purge_campus_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Purge Campus Records: ${target?.name ?: cId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
        IconButton(onClick = { purgingCampusId = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "This operation calls the Supabase Storage API to permanently delete all attached files (materials, submissions, notices) across buckets and executes the database purge RPC. Central audit ledger will record this wipe.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { purgingCampusId = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            viewModel.purgeCampusData(cId)
            purgingCampusId = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
          modifier = Modifier.testTag("confirm_purge_btn")
        ) {
          Text("Confirm Storage & DB Purge")
        }
      }
    }
  }

  // 14. Account Suspension Bottom Sheet
  suspendingUserId?.let { uId ->
    val targetUser = allRegisteredUsers.find { it.id == uId }
    AppBottomSheet(
      onDismissRequest = { suspendingUserId = null },
      modifier = Modifier.testTag("suspend_user_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Suspend Account: ${targetUser?.fullName ?: uId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { suspendingUserId = null }) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Please enter the formal institutional reason for account suspension.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = suspensionReasonText,
        onValueChange = { suspensionReasonText = it },
        label = { Text("Reason for Suspension") },
        modifier = Modifier.fillMaxWidth().testTag("suspension_reason_input")
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { suspendingUserId = null }) { Text("Cancel") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            viewModel.suspendUser(uId, suspensionReasonText)
            suspendingUserId = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
          modifier = Modifier.testTag("confirm_suspend_btn")
        ) {
          Text("Confirm Suspension")
        }
      }
    }
  }
}
