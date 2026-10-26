package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.ProcessedDocumentEntity
import com.example.data.local.ProcessedDocumentRepository
import com.example.data.model.AttachmentFile
import com.example.data.model.Role
import com.example.data.model.SessionStatus
import com.example.data.repository.CampusRepository
import com.example.viewmodel.AppThemeMode
import com.example.viewmodel.CampusViewModel
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CampusConnect", appName)
  }

  @Test
  fun `verify repository initial state and role switching`() {
    val repo = CampusRepository(seedDemoData = true)
    assertEquals(Role.STUDENT, repo.currentUser.value.role)
    assertEquals("Asha Mwangi", repo.currentUser.value.fullName)

    repo.switchUser(repo.lecturerMushi)
    assertEquals(Role.LECTURER, repo.currentUser.value.role)
    assertEquals("Dr. Joseph Mushi", repo.currentUser.value.fullName)

    repo.switchUser(repo.adminEmanuel)
    assertEquals(Role.ADMIN, repo.currentUser.value.role)
    assertTrue(repo.canApproveOrDisapprove())
  }

  @Test
  fun `verify student coursework submission and lecturer grading`() {
    val repo = CampusRepository(seedDemoData = true)
    val initialSubmissionsCount = repo.submissions.value.size

    val sampleDoc = AttachmentFile(
      fileName = "Asha_CS210_Solution.pdf",
      fileSize = "1.5 MB",
      mimeType = "application/pdf"
    )

    repo.submitStudentWork(
      deadlineId = "dl-01",
      courseCode = "CS210",
      assignmentTitle = "Programming Assignment 2",
      notes = "Solved with DFS",
      attachment = sampleDoc
    )

    assertEquals(initialSubmissionsCount + 1, repo.submissions.value.size)
    val latestSub = repo.submissions.value.first()
    assertEquals("Asha_CS210_Solution.pdf", latestSub.attachment.fileName)

    // Lecturer grades the submission
    repo.switchUser(repo.lecturerMushi)
    repo.gradeStudentWork(latestSub.id, "98/100", "Flawless proof and clean architecture.")

    val gradedSub = repo.submissions.value.find { it.id == latestSub.id }
    assertEquals("98/100", gradedSub?.grade)
    assertEquals("Flawless proof and clean architecture.", gradedSub?.feedback)
  }

  @Test
  fun `verify admin has full access to edit and delete all items while non-owners cannot`() {
    val repo = CampusRepository(seedDemoData = true)

    // Regular student Asha cannot delete Dr. Mushi's course material
    repo.switchUser(repo.studentAsha)
    assertFalse(repo.canEditOrDelete("usr-lec-01"))

    // Admin Emanuel can delete or edit anything
    repo.switchUser(repo.adminEmanuel)
    assertTrue(repo.canEditOrDelete("usr-lec-01"))
    assertTrue(repo.canEditOrDelete("usr-stu-01"))
    assertTrue(repo.canApproveOrDisapprove())

    // Admin Sarah also has full admin access
    repo.switchUser(repo.adminSarah)
    assertTrue(repo.canEditOrDelete("usr-lec-01"))
    assertTrue(repo.canApproveOrDisapprove())
  }

  @Test
  fun `verify unapproved guest user can read all academic data without restriction`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.unapprovedStudentBaraka)

    // Unapproved student has full reading access to sessions, announcements, and materials
    assertTrue(repo.sessions.value.isNotEmpty())
    assertTrue(repo.announcements.value.isNotEmpty())
    assertTrue(repo.materials.value.isNotEmpty())
    assertTrue(repo.deadlines.value.isNotEmpty())
  }

  @Test
  fun `verify supabase v2 session kind classification`() {
    val repo = CampusRepository(seedDemoData = true)
    val lectureSession = repo.sessions.value.find { it.courseCode == "CS210" }
    val labSession = repo.sessions.value.find { it.courseCode == "CS210-L" }
    val tutorialSession = repo.sessions.value.find { it.courseCode == "GS101" }

    assertNotNull(lectureSession)
    assertEquals(com.example.data.model.SessionKind.LECTURE, lectureSession?.kind)

    assertNotNull(labSession)
    assertEquals(com.example.data.model.SessionKind.LAB, labSession?.kind)

    assertNotNull(tutorialSession)
    assertEquals(com.example.data.model.SessionKind.TUTORIAL, tutorialSession?.kind)
  }

  @Test
  fun `verify supabase v2 retract announcement and remind unread`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.lecturerMushi)

    val targetAnn = repo.announcements.value.first()
    assertFalse(targetAnn.isRetracted)

    // Retract announcement (Supabase v2 retract_announcement RPC)
    repo.retractAnnouncement(targetAnn.id)
    val retracted = repo.announcements.value.find { it.id == targetAnn.id }
    assertTrue(retracted?.isRetracted == true)
    assertEquals("Dr. Joseph Mushi", retracted?.retractedBy)
    assertNotNull(retracted?.retractedAt)

    // Remind unread students (Supabase v2 remind_unread RPC)
    repo.remindUnread(targetAnn.id)
    val reminded = repo.announcements.value.find { it.id == targetAnn.id }
    assertNotNull(reminded?.remindedAt)
    assertTrue(repo.notifications.value.any { it.title.contains("Notice Reminder") })
  }

  @Test
  fun `verify supabase v2 account suspension and reinstatement`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.adminEmanuel)

    // Suspend user (Supabase v2 suspend_account RPC)
    repo.suspendUser("usr-stu-02", "Investigation regarding examination conduct.")
    val suspended = repo.allRegisteredUsers.value.find { it.id == "usr-stu-02" }
    assertEquals(com.example.data.model.AccountStatus.SUSPENDED, suspended?.status)
    assertEquals("Investigation regarding examination conduct.", suspended?.suspensionReason)

    // Reinstate user
    repo.reinstateUser("usr-stu-02")
    val reinstated = repo.allRegisteredUsers.value.find { it.id == "usr-stu-02" }
    assertEquals(com.example.data.model.AccountStatus.APPROVED, reinstated?.status)
    assertEquals(null, reinstated?.suspensionReason)
  }

  @Test
  fun `verify supabase v2 role daily quota limits`() {
    val repo = CampusRepository(seedDemoData = true)

    // Student limits
    repo.switchUser(repo.studentAsha)
    val studentLimits = repo.userLimits.value
    assertEquals(0, studentLimits.announcementsLimit)
    assertEquals(20, studentLimits.submissionsLimit)

    // Lecturer limits
    repo.switchUser(repo.lecturerMushi)
    val lecturerLimits = repo.userLimits.value
    assertEquals(10, lecturerLimits.announcementsLimit)
    assertEquals(2, lecturerLimits.urgentLimit)

    // Admin limits
    repo.switchUser(repo.adminEmanuel)
    val adminLimits = repo.userLimits.value
    assertEquals(100, adminLimits.announcementsLimit)
    assertEquals(50, adminLimits.urgentLimit)
  }

  @Test
  fun `verify firestore user role assignment and admin only approval constraint`() {
    val repo = CampusRepository(seedDemoData = true)

    // Non-admins cannot approve or disapprove documents
    repo.switchUser(repo.studentAsha)
    assertFalse(repo.canApproveOrDisapprove())

    repo.switchUser(repo.lecturerMushi)
    assertFalse(repo.canApproveOrDisapprove())

    // Only Admin can approve or disapprove documents
    repo.switchUser(repo.adminEmanuel)
    assertTrue(repo.canApproveOrDisapprove())

    // Admin updates a user's role in the Firestore collection
    repo.assignUserRole("usr-stu-02", Role.LECTURER)
    val updatedUser = repo.allRegisteredUsers.value.find { it.id == "usr-stu-02" }
    assertEquals(Role.LECTURER, updatedUser?.role)

    // Audit log records the Firestore role update
    assertTrue(repo.auditLogs.value.any { it.action == "FIRESTORE_ROLE_UPDATED" })
  }

  @Test
  fun `verify multi campus registration prioritizes registration number and allows open emails`() {
    val repo = CampusRepository(seedDemoData = true)

    // Student registers with CUoM registration number and personal Gmail
    val registered = repo.registerUser(
      fullName = "Bakari Mkapa",
      registrationNumber = "CUOM/BCS/2024/099",
      university = "Catholic University of Mbeya (CUoM)",
      email = "bmkapa99@gmail.com",
      role = Role.STUDENT,
      programme = "BSc Computer Science"
    )

    assertEquals("Bakari Mkapa", registered.fullName)
    assertEquals("CUOM/BCS/2024/099", registered.studentNumber)
    assertEquals("Catholic University of Mbeya (CUoM)", registered.university)
    assertEquals("bmkapa99@gmail.com", registered.email)
    assertEquals(Role.STUDENT, registered.role)

    // Current user automatically becomes the registered user
    assertEquals(registered.id, repo.currentUser.value.id)

    // Registration is recorded in audit log
    assertTrue(repo.auditLogs.value.any { it.action == "USER_REGISTERED" })
  }

  @Test
  fun `verify super admin app manager permissions and campus database provisioning`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.superAdminTariq)

    assertEquals(Role.SUPER_ADMIN, repo.currentUser.value.role)
    assertTrue(repo.canEditOrDelete("usr-lec-01"))
    assertTrue(repo.canApproveOrDisapprove())

    // Super Admin provisions new campus database for SAUT
    val newCampus = repo.addNewCampus(
      name = "St. Augustine University of Tanzania (SAUT)",
      shortCode = "SAUT",
      adminEmail = "admin@saut.ac.tz",
      adminName = "Dr. Peter Mrema"
    )

    assertEquals("db_saut_prod", newCampus.dbName)
    assertTrue(repo.campuses.value.any { it.id == "saut" })
    assertTrue(repo.allRegisteredUsers.value.any { it.email == "admin@saut.ac.tz" })

    // Super Admin updates database server status
    repo.updateCampusServerStatus("saut", com.example.data.model.ServerStatus.MAINTENANCE)
    val updatedCampus = repo.campuses.value.find { it.id == "saut" }
    assertEquals(com.example.data.model.ServerStatus.MAINTENANCE, updatedCampus?.serverStatus)

    // Audit log records provisioning
    assertTrue(repo.auditLogs.value.any { it.action == "CAMPUS_DATABASE_PROVISIONED" })
  }

  @Test
  fun `verify campus admin course creation and structured session scheduling`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.adminEmanuel)

    // Emanuel adds new course CS310 to CUoM
    repo.addCourseToCampus(
      universityName = "Catholic University of Mbeya (CUoM)",
      code = "CS310",
      title = "Artificial Intelligence & Heuristics",
      lecturer = "Dr. Joseph Mushi"
    )

    val cuomCourses = repo.getCoursesForUniversity("Catholic University of Mbeya (CUoM)")
    assertTrue(cuomCourses.any { it.code == "CS310" })

    // Add room venue
    repo.addVenueToCampus(
      universityName = "Catholic University of Mbeya (CUoM)",
      name = "Room 305",
      capacity = 70,
      building = "ICT Wing"
    )
    val cuomVenues = repo.getVenuesForUniversity("Catholic University of Mbeya (CUoM)")
    assertTrue(cuomVenues.any { it.name == "Room 305" })

    // Schedule new class session picking structured data
    val newSession = com.example.data.model.ClassSession(
      id = "sess-ai-01",
      courseCode = "CS310",
      courseTitle = "Artificial Intelligence & Heuristics",
      lecturerId = "usr-lec-01",
      lecturerName = "Dr. Joseph Mushi",
      venue = "Room 305",
      dayOfWeek = 2,
      startTime = "10:00",
      endTime = "12:00"
    )
    repo.addTimetableSession(newSession)

    assertTrue(repo.sessions.value.any { it.id == "sess-ai-01" })
    assertTrue(repo.auditLogs.value.any { it.action == "SESSION_SCHEDULED" })
  }

  @Test
  fun `verify docuread document analysis and firestore metadata ingestion`() = kotlinx.coroutines.runBlocking {
    val firestoreRepo = com.example.data.firestore.DocuReadFirestoreRepository()

    // Verify initial sample documents exist in the repository
    val initialDocs = firestoreRepo.observeAllDocuments()
    val initialList = initialDocs.first()
    assertTrue("Should have initial seeded sample documents", initialList.isNotEmpty())

    // Ingest a new document metadata into Firestore repository
    val customDoc = com.example.data.firestore.DocuReadMetadataDoc(
      id = "doc-test-99",
      documentTitle = "Distributed Systems & Consensus Protocols",
      fileName = "CS410_Raft_Paxos_Notes.pdf",
      fileSize = "3.8 MB",
      mimeType = "application/pdf",
      courseCode = "CS410",
      courseTitle = "Distributed Systems",
      university = "Catholic University of Mbeya (CUoM)",
      uploaderId = "usr-lec-01",
      uploaderName = "Dr. Joseph Mushi",
      extractionStatus = "COMPLETED",
      summary = "Deep dive into leader election, log replication, and Byzantine Fault Tolerance in distributed clusters.",
      keyTopics = listOf("Raft Consensus", "Paxos Algorithm", "Split-brain Resolution", "Vector Clocks"),
      learningObjectives = listOf("Implement Raft leader heartbeat", "Handle network partitions reliably"),
      studyTips = listOf("Sketch election state transitions on paper", "Review term number monotonicity"),
      wordCount = 6800,
      pageCount = 28,
      readingTimeMinutes = 25
    )

    val ingestResult = firestoreRepo.ingestDocumentMetadata(customDoc)
    assertTrue("Ingestion result should succeed", ingestResult.isSuccess)

    // Verify observable stream contains the newly ingested document
    val updatedList = firestoreRepo.observeAllDocuments().first()
    val foundDoc = updatedList.find { it.id == "doc-test-99" }
    assertNotNull("Newly ingested document must be retrievable from Firestore stream", foundDoc)
    assertEquals("CS410_Raft_Paxos_Notes.pdf", foundDoc?.fileName)
    assertEquals(4, foundDoc?.keyTopics?.size)
    assertEquals(28, foundDoc?.pageCount)

    // Verify DocuReadViewModel state progression and analysis trigger
    val viewModel = com.example.viewmodel.DocuReadViewModel(firestoreRepo)
    val testAttachment = AttachmentFile(
      fileName = "CS410_Raft_Paxos_Notes.pdf",
      fileSize = "3.8 MB",
      mimeType = "application/pdf",
      previewText = "Comprehensive notes on Distributed Systems Consensus."
    )

    assertEquals(com.example.viewmodel.DocuReadUiState.Idle, viewModel.uiState.value)

    viewModel.initiateAnalysis(
      attachment = testAttachment,
      courseCode = "CS410",
      courseTitle = "Distributed Systems",
      university = "Catholic University of Mbeya (CUoM)",
      uploaderName = "Dr. Joseph Mushi"
    )

    // Extraction begins with Extracting UI state
    val currentState = viewModel.uiState.value
    assertTrue(
      "UI State should be Extracting or Success",
      currentState is com.example.viewmodel.DocuReadUiState.Extracting ||
          currentState is com.example.viewmodel.DocuReadUiState.Success
    )
  }

  @Test
  fun `verify room database schema and processed document repository operations`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = inMemoryDb.processedDocumentDao()
    val repo = ProcessedDocumentRepository(dao)

    // Initially empty
    val initialList = repo.allDocuments.first()
    assertTrue(initialList.isEmpty())

    // Insert processed document
    val testEntity = ProcessedDocumentEntity(
      id = "doc-room-test-1",
      title = "Asymptotic Complexity & Dynamic Programming",
      fileName = "Algorithms_Lec_04.pdf",
      fileSize = "2.8 MB",
      mimeType = "application/pdf",
      courseCode = "CS210",
      courseTitle = "Data Structures & Algorithms",
      university = "Catholic University of Mbeya (CUoM)",
      uploaderName = "Dr. Joseph Mushi",
      timestamp = System.currentTimeMillis(),
      extractedSummary = "Analyzes asymptotic notation (Big-O, Omega, Theta) alongside DP optimal substructure models.",
      keyTopics = "Asymptotic Notation, Recurrence Relations, Memoization, Matrix Chain",
      learningObjectives = "Analyze runtime using Master Theorem; Construct dynamic programming recurrence equations",
      studyTips = "Review divide and conquer recurrence trees; Practice memoization table construction",
      wordCount = 4200,
      pageCount = 16,
      readingTimeMinutes = 15,
      extractionStatus = "COMPLETED"
    )

    repo.insertDocument(testEntity)

    // Verify insertion and Flow reactivity
    val updatedList = repo.allDocuments.first()
    assertEquals(1, updatedList.size)
    assertEquals("Asymptotic Complexity & Dynamic Programming", updatedList.first().title)
    assertEquals("CS210", updatedList.first().courseCode)
    assertEquals("Algorithms_Lec_04.pdf", updatedList.first().fileName)
    assertTrue(updatedList.first().extractedSummary.contains("Big-O"))

    // Verify course-specific filter query
    val cs210Docs = repo.getDocumentsByCourse("CS210").first()
    assertEquals(1, cs210Docs.size)

    val is201Docs = repo.getDocumentsByCourse("IS201").first()
    assertTrue(is201Docs.isEmpty())

    // Verify search query matching title or summary
    val searchResults = repo.searchDocuments("Asymptotic").first()
    assertEquals(1, searchResults.size)

    val noMatchResults = repo.searchDocuments("QuantumComputing").first()
    assertTrue(noMatchResults.isEmpty())

    // Verify deletion
    repo.deleteDocumentById("doc-room-test-1")
    val afterDeleteList = repo.allDocuments.first()
    assertTrue(afterDeleteList.isEmpty())

    inMemoryDb.close()
  }

  @Test
  fun `verify campus viewmodel adaptive theme mode and toggling`() {
    val viewModel = CampusViewModel()

    // Default is SYSTEM adaptive
    assertEquals(AppThemeMode.SYSTEM, viewModel.themeMode.value)

    // Toggle: SYSTEM -> LIGHT
    viewModel.toggleTheme()
    assertEquals(AppThemeMode.LIGHT, viewModel.themeMode.value)

    // Toggle: LIGHT -> DARK
    viewModel.toggleTheme()
    assertEquals(AppThemeMode.DARK, viewModel.themeMode.value)

    // Toggle: DARK -> SYSTEM
    viewModel.toggleTheme()
    assertEquals(AppThemeMode.SYSTEM, viewModel.themeMode.value)

    // Explicit setter
    viewModel.setThemeMode(AppThemeMode.DARK)
    assertEquals(AppThemeMode.DARK, viewModel.themeMode.value)
  }

  @Test
  fun `verify CS student cannot see HR student materials deadlines or sessions`() {
    val repo = CampusRepository(seedDemoData = true)
    
    // Switch to Computer Science student (Asha)
    repo.switchUser(repo.studentAsha)
    val csUser = repo.currentUser.value
    assertEquals("BSc Computer Science", csUser.programme)

    val csSessions = repo.filterSessionsForUser(repo.sessions.value, csUser)
    val csMaterials = repo.filterMaterialsForUser(repo.materials.value, csUser)
    val csDeadlines = repo.filterDeadlinesForUser(repo.deadlines.value, csUser)

    // CS student MUST NOT see any HR courses or materials
    assertTrue(csSessions.none { it.courseCode.startsWith("HR") })
    assertTrue(csMaterials.none { it.courseCode.startsWith("HR") })
    assertTrue(csDeadlines.none { it.courseCode.startsWith("HR") })

    // Switch to HR student (Neema)
    repo.switchUser(repo.studentNeema)
    val hrUser = repo.currentUser.value
    assertEquals("Bachelor of Human Resource Management", hrUser.programme)

    val hrSessions = repo.filterSessionsForUser(repo.sessions.value, hrUser)
    val hrMaterials = repo.filterMaterialsForUser(repo.materials.value, hrUser)
    val hrDeadlines = repo.filterDeadlinesForUser(repo.deadlines.value, hrUser)

    // HR student sees HR materials and sessions
    assertTrue(hrSessions.any { it.courseCode.startsWith("HR") })
    assertTrue(hrMaterials.any { it.courseCode.startsWith("HR") })
    assertTrue(hrDeadlines.any { it.courseCode.startsWith("HR") })

    // HR student MUST NOT see CS courses
    assertTrue(hrSessions.none { it.courseCode.startsWith("CS") })
    assertTrue(hrMaterials.none { it.courseCode.startsWith("CS") })
  }

  @Test
  fun `verify Class Representative posts notice on behalf of responsible lecturer`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.crJuma)
    val crUser = repo.currentUser.value
    assertEquals(com.example.data.model.Role.CLASS_REP, crUser.role)

    val initialCount = repo.announcements.value.size
    repo.publishAnnouncement(
      title = "CR Notice: Assignment Extended",
      body = "Lecturer Dr. Mushi has instructed that assignment 2 is extended by 2 days.",
      audienceType = com.example.data.model.AudienceType.COURSE,
      audienceName = "CS210 - Algorithms",
      priority = com.example.data.model.AnnouncementPriority.URGENT,
      postedOnBehalfOfLecturer = "Dr. Joseph Mushi"
    )

    assertEquals(initialCount + 1, repo.announcements.value.size)
    val newAnn = repo.announcements.value.first()
    assertEquals("Dr. Joseph Mushi", newAnn.postedOnBehalfOfLecturer)
    assertEquals("usr-lec-01", newAnn.postedOnBehalfOfLecturerId)
  }

  @Test
  fun `verify setup course and venue edit and delete`() {
    val repo = CampusRepository(seedDemoData = true)
    repo.switchUser(repo.adminEmanuel)

    val uniName = repo.adminEmanuel.university

    // Edit Course
    repo.editCourseInCampus(
      universityName = uniName,
      oldCode = "CS210",
      newCode = "CS210",
      newTitle = "Advanced Data Structures & Algorithms",
      newLecturer = "Dr. Joseph Mushi",
      programmeName = "BSc Computer Science"
    )
    val campus = repo.campuses.value.find { it.name.equals(uniName, ignoreCase = true) }
    val updatedCourse = campus?.courses?.find { it.code == "CS210" }
    assertEquals("Advanced Data Structures & Algorithms", updatedCourse?.title)

    // Edit Venue
    repo.editVenueInCampus(
      universityName = uniName,
      oldName = "Lab 3",
      newName = "Advanced AI Lab 3",
      newCapacity = 75,
      newBuilding = "ICT Complex Floor 2",
      hasProjector = true
    )
    val campusAfterVenue = repo.campuses.value.find { it.name.equals(uniName, ignoreCase = true) }
    val updatedVenue = campusAfterVenue?.venues?.find { it.name == "Advanced AI Lab 3" }
    assertNotNull(updatedVenue)
    assertEquals(75, updatedVenue?.capacity)
  }

  @Test
  fun `verify clean production repository has only owner account and zero placeholder items`() {
    val cleanRepo = CampusRepository() // default seedDemoData = false
    assertEquals(1, cleanRepo.allPersonas.size)
    assertEquals("eliamwenda01@gmail.com", cleanRepo.currentUser.value.email)
    assertEquals("Elia Mwenda", cleanRepo.currentUser.value.fullName)
    assertEquals(Role.SUPER_ADMIN, cleanRepo.currentUser.value.role)
    assertFalse(cleanRepo.isAuthenticated.value)
    assertTrue(cleanRepo.sessions.value.isEmpty())
    assertTrue(cleanRepo.announcements.value.isEmpty())
    assertTrue(cleanRepo.materials.value.isEmpty())
    assertTrue(cleanRepo.deadlines.value.isEmpty())
    assertTrue(cleanRepo.submissions.value.isEmpty())
    assertTrue(cleanRepo.notifications.value.isEmpty())
  }

  @Test
  fun `verify authentication gate login owner direct access and logout`() {
    val repo = CampusRepository()
    // Not authenticated initially
    assertFalse(repo.isAuthenticated.value)

    // Non-existent user fails
    val failed = repo.login("unknown@student.ac.tz", "pass")
    assertFalse(failed)
    assertFalse(repo.isAuthenticated.value)

    // Login as Owner Elia Mwenda
    val ownerLogin = repo.login("eliamwenda01@gmail.com", "secure123")
    assertTrue(ownerLogin)
    assertTrue(repo.isAuthenticated.value)
    assertEquals("Elia Mwenda", repo.currentUser.value.fullName)
    assertEquals(Role.SUPER_ADMIN, repo.currentUser.value.role)

    // Logout
    repo.logout()
    assertFalse(repo.isAuthenticated.value)

    // Login via direct owner root key
    repo.loginAsOwner()
    assertTrue(repo.isAuthenticated.value)
    assertEquals("eliamwenda01@gmail.com", repo.currentUser.value.email)
  }

  @Test
  fun `verify registration automatically authenticates user and maintains persistent session`() {
    val repo = CampusRepository()
    assertFalse(repo.isAuthenticated.value)

    val registered = repo.registerUser(
      fullName = "Baraka Juma",
      registrationNumber = "CUOM/BCS/2026/999",
      university = "Catholic University of Mbeya (CUoM)",
      email = "baraka.juma@gmail.com",
      role = Role.STUDENT,
      programme = "BSc Computer Science"
    )

    // Immediately authenticated upon registration with no auto-logout
    assertTrue(repo.isAuthenticated.value)
    assertEquals("Baraka Juma", repo.currentUser.value.fullName)
    assertEquals(Role.STUDENT, repo.currentUser.value.role)
    assertTrue(repo.keepSessionActive.value)
  }

  @Test
  fun `verify session manager persists and restores user credentials`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.data.local.SessionManager(context)

    // Clear any prior test state
    sessionManager.clearSession()
    assertFalse(sessionManager.isSessionActive())

    val user = com.example.data.model.UserProfile(
      id = "usr-test-01",
      fullName = "Neema Temu",
      email = "neema.temu@udsm.ac.tz",
      role = Role.LECTURER,
      status = com.example.data.model.AccountStatus.APPROVED,
      university = "University of Dar es Salaam (UDSM)",
      staffNumber = "UDSM-STF-042",
      programme = "Department of Computer Science",
      cohort = "Senior Lecturer"
    )

    sessionManager.saveSession(user, keepActive = true)
    assertTrue(sessionManager.isSessionActive())

    val loaded = sessionManager.loadSession()
    assertNotNull(loaded)
    assertEquals("Neema Temu", loaded?.fullName)
    assertEquals("neema.temu@udsm.ac.tz", loaded?.email)
    assertEquals(Role.LECTURER, loaded?.role)
    assertEquals("UDSM-STF-042", loaded?.staffNumber)

    sessionManager.clearSession()
    assertFalse(sessionManager.isSessionActive())
  }
}



