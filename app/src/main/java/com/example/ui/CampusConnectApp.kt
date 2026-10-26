package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProcessedDocumentRepository
import com.example.data.model.Announcement
import com.example.data.model.AttachmentFile
import com.example.data.model.DeadlineItem
import com.example.data.model.Role
import com.example.data.model.StudentSubmission
import com.example.data.model.StudyMaterial
import com.example.ui.components.AddDeadlineDialog
import com.example.ui.components.AnnouncementDetailDialog
import com.example.ui.components.CancelSessionDialog
import com.example.ui.components.ChangeVenueDialog
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.EditAnnouncementDialog
import com.example.ui.components.GradeSubmissionDialog
import com.example.ui.components.PublishAnnouncementDialog
import com.example.ui.components.SignUpDialog
import com.example.ui.components.SubmitWorkDialog
import com.example.ui.components.UploadMaterialDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.DeadlinesScreen
import androidx.compose.material.icons.filled.ExitToApp
import com.example.data.repository.CampusRepository
import com.example.ui.screens.AuthSplashScreen
import com.example.ui.screens.DocuReadAnalysisScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MaterialsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProcessedDocumentsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SubmissionsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.CampusConnectTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberLight
import com.example.viewmodel.AppThemeMode
import com.example.viewmodel.CampusViewModel
import com.example.viewmodel.DocuReadViewModel
import com.example.viewmodel.ScreenTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusConnectApp(
  context: android.content.Context = LocalContext.current,
  viewModel: CampusViewModel = remember {
    CampusViewModel(repository = CampusRepository(context = context.applicationContext))
  }
) {
  val isAuthenticated by viewModel.isAuthenticated.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val isOffline by viewModel.isOffline.collectAsState()
  val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
  val pendingUsers by viewModel.pendingUsers.collectAsState()
  val submissions by viewModel.submissions.collectAsState()

  val sessions by viewModel.sessions.collectAsState()
  val announcements by viewModel.announcements.collectAsState()
  val userLimits by viewModel.userLimits.collectAsState()

  // Dialog & Viewer states
  var selectedAnnouncementId by remember { mutableStateOf<String?>(null) }
  var showPublishDialog by remember { mutableStateOf(false) }
  var showUploadDialog by remember { mutableStateOf(false) }
  var showAddDeadlineDialog by remember { mutableStateOf(false) }
  var venueChangeSessionId by remember { mutableStateOf<String?>(null) }
  var cancelSessionId by remember { mutableStateOf<String?>(null) }
  var showSignUpDialog by remember { mutableStateOf(false) }
  var showTopBarMenu by remember { mutableStateOf(false) }

  var editingAnnouncement by remember { mutableStateOf<Announcement?>(null) }
  var submitWorkDeadline by remember { mutableStateOf<DeadlineItem?>(null) }
  var gradingSubmission by remember { mutableStateOf<StudentSubmission?>(null) }

  // Built-in Document Viewer state
  var viewingDocument by remember { mutableStateOf<AttachmentFile?>(null) }
  var viewingDocAuthor by remember { mutableStateOf<String?>(null) }
  var viewingDocCourse by remember { mutableStateOf<String?>(null) }

  // Room Persistence & DocuRead Document Analysis state & ViewModel
  val context = LocalContext.current
  val localDocRepo = remember(context) { ProcessedDocumentRepository.getInstance(context) }
  val docuReadViewModel: DocuReadViewModel = remember(localDocRepo) {
    DocuReadViewModel(localRepo = localDocRepo)
  }
  var docuReadInitialDoc by remember { mutableStateOf<AttachmentFile?>(null) }
  var docuReadInitialCourse by remember { mutableStateOf<String?>(null) }

  // Universal Back Press Handling - intercepts back press when modals are open
  // or switches back to Home tab, preventing unintended app closures.
  val isAnyModalOpen = viewingDocument != null ||
      selectedAnnouncementId != null ||
      showPublishDialog ||
      showUploadDialog ||
      showAddDeadlineDialog ||
      venueChangeSessionId != null ||
      cancelSessionId != null ||
      editingAnnouncement != null ||
      submitWorkDeadline != null ||
      gradingSubmission != null ||
      showSignUpDialog

  BackHandler(enabled = isAnyModalOpen || currentTab != ScreenTab.HOME) {
    when {
      viewingDocument != null -> {
        viewingDocument = null
        viewingDocAuthor = null
        viewingDocCourse = null
      }
      selectedAnnouncementId != null -> selectedAnnouncementId = null
      showPublishDialog -> showPublishDialog = false
      showUploadDialog -> showUploadDialog = false
      showAddDeadlineDialog -> showAddDeadlineDialog = false
      venueChangeSessionId != null -> venueChangeSessionId = null
      cancelSessionId != null -> cancelSessionId = null
      editingAnnouncement != null -> editingAnnouncement = null
      submitWorkDeadline != null -> submitWorkDeadline = null
      gradingSubmission != null -> gradingSubmission = null
      showSignUpDialog -> showSignUpDialog = false
      currentTab != ScreenTab.HOME -> viewModel.setTab(ScreenTab.HOME)
    }
  }

  val themeMode by viewModel.themeMode.collectAsState()
  val isSystemDark = isSystemInDarkTheme()
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemDark
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
  }

  CampusConnectTheme(darkTheme = isDark) {
    if (!isAuthenticated) {
      AuthSplashScreen(
        viewModel = viewModel,
        onAuthenticated = {
          // User successfully authenticated
        }
      )
    } else {
      Scaffold(
        modifier = Modifier
          .fillMaxSize()
          .testTag("campus_connect_main_scaffold"),
      topBar = {
        TopAppBar(
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(DeepBlue),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = "Logo",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "CampusConnect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  // Role Badge
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(
                        when (currentUser.role) {
                          Role.SUPER_ADMIN -> Color(0xFFF3E8FF)
                          Role.ADMIN -> Color(0xFFDCFCE7)
                          Role.LECTURER -> WarmAmber.copy(alpha = 0.18f)
                          Role.CLASS_REP -> WarmAmberLight
                          Role.STUDENT -> DeepBlue.copy(alpha = 0.12f)
                        }
                      )
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = when (currentUser.role) {
                        Role.SUPER_ADMIN -> "APP MANAGER"
                        Role.CLASS_REP -> "CLASS REP"
                        else -> currentUser.role.name
                      },
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = when (currentUser.role) {
                        Role.SUPER_ADMIN -> Color(0xFF6B21A8)
                        Role.ADMIN -> Color(0xFF166534)
                        Role.LECTURER -> Color(0xFFB45309)
                        Role.CLASS_REP -> Color(0xFFB45309)
                        Role.STUDENT -> DeepBlue
                      },
                      fontSize = 9.sp
                    )
                  }
                }
                Text(
                  text = if (isOffline) "Offline (Room Cache)" else "${currentUser.studentNumber ?: currentUser.staffNumber ?: currentUser.fullName.split(" ").firstOrNull() ?: ""} • ${currentUser.university}",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isOffline) WarmAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
              }
            }
          },
          actions = {
            // Unread Notifications with badge
            IconButton(
              onClick = { viewModel.setTab(ScreenTab.NOTIFICATIONS) },
              modifier = Modifier.testTag("top_bar_notif_btn")
            ) {
              BadgedBox(
                badge = {
                  if (unreadNotifs > 0) {
                    Badge(containerColor = WarmAmber, contentColor = Color.White) {
                      Text("$unreadNotifs")
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Notifications,
                  contentDescription = "Notifications",
                  tint = if (currentTab == ScreenTab.NOTIFICATIONS) DeepBlue else MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Clean Overflow / More Menu
            Box {
              IconButton(
                onClick = { showTopBarMenu = true },
                modifier = Modifier.testTag("top_bar_more_menu_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "Menu",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }

              DropdownMenu(
                expanded = showTopBarMenu,
                onDismissRequest = { showTopBarMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text(if (isDark) "Light Mode" else "Dark Mode") },
                  leadingIcon = {
                    Icon(
                      imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                      contentDescription = null
                    )
                  },
                  onClick = {
                    viewModel.toggleTheme()
                    showTopBarMenu = false
                  }
                )

                if (currentUser.role == Role.SUPER_ADMIN || currentUser.role == Role.ADMIN) {
                  DropdownMenuItem(
                    text = { Text("Admin & Campuses Console") },
                    leadingIcon = {
                      Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DeepBlue)
                    },
                    onClick = {
                      viewModel.setTab(ScreenTab.ADMIN)
                      showTopBarMenu = false
                    }
                  )
                }

                DropdownMenuItem(
                  text = { Text("DocuRead AI Assistant") },
                  leadingIcon = {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DeepBlue)
                  },
                  onClick = {
                    viewModel.setTab(ScreenTab.DOCUREAD)
                    showTopBarMenu = false
                  }
                )

                DropdownMenuItem(
                  text = { Text("Coursework Submissions") },
                  leadingIcon = {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                  },
                  onClick = {
                    viewModel.setTab(ScreenTab.SUBMISSIONS)
                    showTopBarMenu = false
                  }
                )

                DropdownMenuItem(
                  text = { Text("Saved Document Summaries") },
                  leadingIcon = {
                    Icon(Icons.Default.Storage, contentDescription = null)
                  },
                  onClick = {
                    viewModel.setTab(ScreenTab.PROCESSED_DOCS)
                    showTopBarMenu = false
                  }
                )

                HorizontalDivider()

                DropdownMenuItem(
                  text = { Text("User Registration") },
                  leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                  },
                  onClick = {
                    showSignUpDialog = true
                    showTopBarMenu = false
                  }
                )

                DropdownMenuItem(
                  text = { Text("Sign Out / Lock Session", color = DangerRed) },
                  leadingIcon = {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DangerRed)
                  },
                  onClick = {
                    showTopBarMenu = false
                    viewModel.logout()
                  }
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        )
      },
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = currentTab == ScreenTab.HOME,
            onClick = { viewModel.setTab(ScreenTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(viewModel.getString("tab_home")) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = DeepBlue.copy(alpha = 0.15f)),
            modifier = Modifier.testTag("nav_item_home")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.TIMETABLE,
            onClick = { viewModel.setTab(ScreenTab.TIMETABLE) },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Timetable") },
            label = { Text(viewModel.getString("tab_timetable")) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = DeepBlue.copy(alpha = 0.15f)),
            modifier = Modifier.testTag("nav_item_timetable")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.MATERIALS,
            onClick = { viewModel.setTab(ScreenTab.MATERIALS) },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Materials") },
            label = { Text(viewModel.getString("tab_materials")) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = DeepBlue.copy(alpha = 0.15f)),
            modifier = Modifier.testTag("nav_item_materials")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.CALENDAR,
            onClick = { viewModel.setTab(ScreenTab.CALENDAR) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Deadlines") },
            label = { Text(viewModel.getString("tab_calendar")) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = DeepBlue.copy(alpha = 0.15f)),
            modifier = Modifier.testTag("nav_item_calendar")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.PROFILE,
            onClick = { viewModel.setTab(ScreenTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text(viewModel.getString("tab_profile")) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = DeepBlue.copy(alpha = 0.15f)),
            modifier = Modifier.testTag("nav_item_profile")
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentTab) {
          ScreenTab.HOME -> HomeScreen(
            viewModel = viewModel,
            onNavigateToNotifications = { viewModel.setTab(ScreenTab.NOTIFICATIONS) },
            onOpenAnnouncementDetail = { selectedAnnouncementId = it },
            onOpenPublishDialog = { showPublishDialog = true },
            onOpenChangeVenueDialog = { venueChangeSessionId = it },
            onOpenUploadDialog = { showUploadDialog = true },
            onOpenAddDeadlineDialog = { showAddDeadlineDialog = true }
          )

          ScreenTab.TIMETABLE -> TimetableScreen(
            viewModel = viewModel,
            onOpenChangeVenueDialog = { venueChangeSessionId = it },
            onOpenCancelDialog = { cancelSessionId = it }
          )

          ScreenTab.MATERIALS -> MaterialsScreen(
            viewModel = viewModel,
            onOpenUploadDialog = { showUploadDialog = true },
            onPreviewDocument = { file, author, course ->
              viewingDocument = file
              viewingDocAuthor = author
              viewingDocCourse = course
            },
            onAnalyzeWithDocuRead = { file, course ->
              docuReadInitialDoc = file
              docuReadInitialCourse = course
              viewModel.setTab(ScreenTab.DOCUREAD)
            },
            onNavigateToProcessedDocs = { viewModel.setTab(ScreenTab.PROCESSED_DOCS) }
          )

          ScreenTab.CALENDAR -> DeadlinesScreen(
            viewModel = viewModel,
            onOpenAddDeadlineDialog = { showAddDeadlineDialog = true },
            onSubmitWorkClick = { dl -> submitWorkDeadline = dl }
          )

          ScreenTab.SUBMISSIONS -> SubmissionsScreen(
            viewModel = viewModel,
            onPreviewDocument = { file ->
              viewingDocument = file
              viewingDocAuthor = null
              viewingDocCourse = null
            },
            onGradeSubmission = { sub -> gradingSubmission = sub }
          )

          ScreenTab.PROFILE -> ProfileScreen(
            viewModel = viewModel,
            onOpenSignUpDialog = { showSignUpDialog = true }
          )

          ScreenTab.ADMIN -> AdminScreen(
            viewModel = viewModel
          )

          ScreenTab.NOTIFICATIONS -> NotificationsScreen(
            viewModel = viewModel
          )

          ScreenTab.DOCUREAD -> DocuReadAnalysisScreen(
            campusViewModel = viewModel,
            docuReadViewModel = docuReadViewModel,
            onNavigateBack = { viewModel.setTab(ScreenTab.MATERIALS) },
            onPreviewDocument = { file, author, course ->
              viewingDocument = file
              viewingDocAuthor = author
              viewingDocCourse = course
            },
            onNavigateToProcessedDocs = { viewModel.setTab(ScreenTab.PROCESSED_DOCS) },
            initialAttachment = docuReadInitialDoc,
            initialCourseCode = docuReadInitialCourse
          )

          ScreenTab.PROCESSED_DOCS -> ProcessedDocumentsScreen(
            campusViewModel = viewModel,
            docuReadViewModel = docuReadViewModel,
            onNavigateBack = { viewModel.setTab(ScreenTab.MATERIALS) },
            onNavigateToAnalyze = { viewModel.setTab(ScreenTab.DOCUREAD) },
            onPreviewDocument = { file, author, course ->
              viewingDocument = file
              viewingDocAuthor = author
              viewingDocCourse = course
            }
          )
        }
      }

      // Announcement Detail Dialog
      selectedAnnouncementId?.let { id ->
        val announcement = announcements.find { it.id == id }
        if (announcement != null) {
          AnnouncementDetailDialog(
            announcement = announcement,
            currentUserRole = currentUser.role,
            canModerate = viewModel.canEditOrDelete(announcement.authorId),
            onDismiss = { selectedAnnouncementId = null },
            onPreviewAttachment = { file ->
              viewingDocument = file
              viewingDocAuthor = announcement.authorName
              viewingDocCourse = announcement.audienceName
            },
            onEditClick = {
              selectedAnnouncementId = null
              editingAnnouncement = announcement
            },
            onDeleteClick = {
              viewModel.deleteAnnouncement(announcement.id)
              selectedAnnouncementId = null
            },
            onToggleApproval = { status ->
              viewModel.setAnnouncementApproval(announcement.id, status)
            },
            onRetractClick = {
              viewModel.retractAnnouncement(announcement.id)
            },
            onRemindUnreadClick = {
              viewModel.remindUnread(announcement.id)
            }
          )
        }
      }

      // Publish Announcement Dialog
      if (showPublishDialog) {
        val availableLecs = viewModel.getLecturersForUniversity(currentUser.university)
        PublishAnnouncementDialog(
          onDismiss = { showPublishDialog = false },
          roleLimits = userLimits,
          currentUserRole = currentUser.role,
          availableLecturers = availableLecs,
          onPublish = { title, body, audienceType, audienceName, priority, attachment, onBehalfOf ->
            viewModel.publishAnnouncement(title, body, audienceType, audienceName, priority, attachment, onBehalfOf)
          },
          onPreviewAttachment = { file ->
            viewingDocument = file
          }
        )
      }

      // Edit Announcement Dialog
      editingAnnouncement?.let { ann ->
        EditAnnouncementDialog(
          announcement = ann,
          onDismiss = { editingAnnouncement = null },
          onSave = { newTitle, newBody, newPriority ->
            viewModel.updateAnnouncement(ann.id, newTitle, newBody, newPriority)
          }
        )
      }

      // Upload Study Material Dialog
      if (showUploadDialog) {
        UploadMaterialDialog(
          onDismiss = { showUploadDialog = false },
          onUpload = { title, code, courseTitle, desc, attachment, category, tags ->
            viewModel.uploadMaterial(title, code, courseTitle, desc, attachment, category, tags)
          },
          onPreviewAttachment = { file ->
            viewingDocument = file
          }
        )
      }

      // Add Deadline Dialog
      if (showAddDeadlineDialog) {
        AddDeadlineDialog(
          onDismiss = { showAddDeadlineDialog = false },
          onAdd = { code, courseTitle, title, instructions, dueDateTime, formats ->
            viewModel.addDeadline(code, courseTitle, title, instructions, dueDateTime, formats)
          }
        )
      }

      // Student Coursework Submit Dialog
      submitWorkDeadline?.let { dl ->
        SubmitWorkDialog(
          deadline = dl,
          onDismiss = { submitWorkDeadline = null },
          onSubmit = { notes, file ->
            viewModel.submitStudentWork(
              deadlineId = dl.id,
              courseCode = dl.courseCode,
              assignmentTitle = dl.title,
              notes = notes,
              attachment = file
            )
          },
          onPreviewFile = { file ->
            viewingDocument = file
            viewingDocAuthor = currentUser.fullName
            viewingDocCourse = dl.courseCode
          }
        )
      }

      // Lecturer / Admin Grade Submission Dialog
      gradingSubmission?.let { sub ->
        GradeSubmissionDialog(
          submission = sub,
          onDismiss = { gradingSubmission = null },
          onPreviewDocument = { file ->
            viewingDocument = file
            viewingDocAuthor = sub.studentName
            viewingDocCourse = sub.courseCode
          },
          onGradeSubmitted = { grade, feedback ->
            viewModel.gradeStudentWork(sub.id, grade, feedback)
          }
        )
      }

      // Change Venue Dialog
      venueChangeSessionId?.let { sId ->
        val session = sessions.find { it.id == sId }
        if (session != null) {
          ChangeVenueDialog(
            session = session,
            onDismiss = { venueChangeSessionId = null },
            onConfirm = { newVenue, reason ->
              viewModel.updateSessionVenue(sId, newVenue, reason)
            }
          )
        }
      }

      // Cancel Session Dialog
      cancelSessionId?.let { sId ->
        val session = sessions.find { it.id == sId }
        if (session != null) {
          CancelSessionDialog(
            session = session,
            onDismiss = { cancelSessionId = null },
            onConfirm = { reason ->
              viewModel.cancelSession(sId, reason)
            }
          )
        }
      }

      // Built-in Document Viewer Dialog
      viewingDocument?.let { doc ->
        DocumentViewerDialog(
          attachment = doc,
          authorOrUploaderName = viewingDocAuthor,
          courseCode = viewingDocCourse,
          onDismiss = {
            viewingDocument = null
            viewingDocAuthor = null
            viewingDocCourse = null
          }
        )
      }

      // Universal Multi-Campus Sign-Up Dialog
      if (showSignUpDialog) {
        val campusesList by viewModel.campuses.collectAsState()
        SignUpDialog(
          onDismiss = { showSignUpDialog = false },
          campuses = campusesList,
          onRegister = { name, reg, uni, mail, role, prog ->
            viewModel.registerUser(name, reg, uni, mail, role, prog)
          }
        )
      }
    }
  }
}
}
