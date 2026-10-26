package com.example.data.repository

import com.example.data.model.AccountStatus
import com.example.data.model.Announcement
import com.example.data.model.AnnouncementPriority
import com.example.data.model.AppLanguage
import com.example.data.model.AttachmentFile
import com.example.data.model.AudienceType
import com.example.data.model.AuditLog
import com.example.data.model.CampusCourse
import com.example.data.model.CampusEntity
import com.example.data.model.CampusProgramme
import com.example.data.model.CampusVenue
import com.example.data.model.ClassSession
import com.example.data.model.ContentApprovalStatus
import com.example.data.model.DeadlineItem
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.Role
import com.example.data.model.RoleLimits
import com.example.data.model.ServerStatus
import com.example.data.model.SessionKind
import com.example.data.model.SessionStatus
import com.example.data.model.StudentSubmission
import com.example.data.model.StudyMaterial
import com.example.data.model.UserProfile
import com.example.data.firestore.UserRoleDoc
import com.example.data.firestore.UserRoleFirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.content.Context
import com.example.data.local.SessionManager

class CampusRepository(
  val context: Context? = null,
  val seedDemoData: Boolean = false
) {
  val sessionManager: SessionManager? = context?.let { SessionManager(it) }
  private val restoredUser: UserProfile? = if (seedDemoData) null else sessionManager?.loadSession()

  // Multiple demo personas to support multi-admin, multi-uploader, and unapproved users
  val studentAsha = UserProfile(
    id = "usr-stu-01",
    fullName = "Asha Mwangi",
    email = "a.mwangi@gmail.com",
    role = Role.STUDENT,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    studentNumber = "CUOM/BCS/2022/084",
    programme = "BSc Computer Science",
    cohort = "Year 2 • Semester 1"
  )

  val studentKelvin = UserProfile(
    id = "usr-stu-02",
    fullName = "Kelvin Tarimo",
    email = "k.tarimo@gmail.com",
    role = Role.STUDENT,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    studentNumber = "CUOM/BCS/2022/091",
    programme = "BSc Computer Science",
    cohort = "Year 2 • Semester 1"
  )

  // Unapproved user: can see all data, but their submissions/uploads start as pending review
  val unapprovedStudentBaraka = UserProfile(
    id = "usr-pen-01",
    fullName = "Baraka Juma (Pending)",
    email = "b.juma@gmail.com",
    role = Role.STUDENT,
    status = AccountStatus.PENDING,
    university = "Catholic University of Mbeya (CUoM)",
    studentNumber = "CUOM/BIS/2024/011",
    programme = "BSc Information Systems",
    cohort = "Year 1 • Unverified"
  )

  // Suspended account demo (matches v2 schema account_status 'suspended')
  val suspendedStudentDaudi = UserProfile(
    id = "usr-sus-01",
    fullName = "Daudi Kibona (Suspended)",
    email = "d.kibona@gmail.com",
    role = Role.STUDENT,
    status = AccountStatus.SUSPENDED,
    university = "Catholic University of Mbeya (CUoM)",
    studentNumber = "CUOM/BCS/2023/044",
    programme = "BSc Computer Science",
    cohort = "Year 2",
    suspensionReason = "Pending academic disciplinary committee review regarding examination clearance."
  )

  val lecturerMushi = UserProfile(
    id = "usr-lec-01",
    fullName = "Dr. Joseph Mushi",
    email = "j.mushi@cuom.ac.tz",
    role = Role.LECTURER,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    staffNumber = "CUOM-STF-041",
    programme = "Department of Computer Science",
    cohort = "Senior Lecturer"
  )

  val lecturerGrace = UserProfile(
    id = "usr-lec-02",
    fullName = "Prof. Grace Temu",
    email = "g.temu@udsm.ac.tz",
    role = Role.LECTURER,
    status = AccountStatus.APPROVED,
    university = "University of Dar es Salaam (UDSM)",
    staffNumber = "UDSM-STF-019",
    programme = "Mathematics & Logic Department",
    cohort = "Associate Professor"
  )

  // Admin 1: CUoM Admin
  val adminEmanuel = UserProfile(
    id = "usr-adm-01",
    fullName = "Emanuel Mwenda (CUoM Admin)",
    email = "admin.registry@cuom.ac.tz",
    role = Role.ADMIN,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    staffNumber = "CUOM-ADM-001",
    programme = "Academic Registry Office",
    cohort = "Chief Administrator"
  )

  // Admin 2: IFM Dean Admin
  val adminSarah = UserProfile(
    id = "usr-adm-02",
    fullName = "Dr. Sarah Kimaro (IFM Admin)",
    email = "s.kimaro@student.ifm.ac.tz",
    role = Role.ADMIN,
    status = AccountStatus.APPROVED,
    university = "Institute of Finance Management (IFM)",
    staffNumber = "IFM-ADM-008",
    programme = "Dean of ICT & Engineering",
    cohort = "Faculty Administrator"
  )

  // Super Admin: Overall App Admin Manager (Multi-Campus Manager)
  val superAdminTariq = UserProfile(
    id = "usr-mgr-01",
    fullName = "Eng. Tariq Bakari (App Manager)",
    email = "manager@campusconnect.app",
    role = Role.SUPER_ADMIN,
    status = AccountStatus.APPROVED,
    university = "CampusConnect Global Hub",
    staffNumber = "SYS-MGR-001",
    programme = "Global Multi-Campus Operations",
    cohort = "System Architect"
  )

  val crJuma = UserProfile(
    id = "usr-cr-01",
    fullName = "Juma Bakari (Class Rep)",
    email = "j.bakari@cuom.ac.tz",
    role = Role.CLASS_REP,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    studentNumber = "CUOM/BCS/2022/005",
    programme = "BSc Computer Science",
    cohort = "Year 2 • Class Representative",
    enrolledCourses = listOf("CS210", "MT101", "CS212", "IS202")
  )

  // Human Resources Student (Isolated from Computer Science student Asha)
  val studentNeema = UserProfile(
    id = "usr-stu-hr",
    fullName = "Neema Makundi (HR Student)",
    email = "n.makundi@cuom.ac.tz",
    role = Role.STUDENT,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    studentNumber = "CUOM/BHRM/2022/012",
    programme = "Bachelor of Human Resource Management",
    cohort = "Year 2 • Semester 1",
    enrolledCourses = listOf("HR201", "HR202", "HR205")
  )

  val lecturerNehemia = UserProfile(
    id = "usr-lec-hr",
    fullName = "Dr. Nehemia Moshi",
    email = "n.moshi@cuom.ac.tz",
    role = Role.LECTURER,
    status = AccountStatus.APPROVED,
    university = "Catholic University of Mbeya (CUoM)",
    staffNumber = "CUOM-STF-088",
    programme = "Department of Human Resource Management",
    cohort = "Senior Lecturer • Head of HR"
  )

  // Master App Owner & Root Key Authority (Owner of the entire system)
  val appOwner = UserProfile(
    id = "usr-owner-01",
    fullName = "Elia Mwenda",
    email = "eliamwenda01@gmail.com",
    role = Role.SUPER_ADMIN,
    status = AccountStatus.APPROVED,
    university = "CampusConnect Global Hub",
    staffNumber = "SYS-OWNER-001",
    programme = "Master App Administration & Global Governance",
    cohort = "Master App Owner"
  )

  val allPersonas = if (seedDemoData) {
    listOf(appOwner, studentAsha, crJuma, studentNeema, studentKelvin, unapprovedStudentBaraka, suspendedStudentDaudi, lecturerMushi, lecturerNehemia, lecturerGrace, adminEmanuel, adminSarah, superAdminTariq)
  } else {
    restoredUser?.let { listOf(appOwner, it).distinctBy { u -> u.id } } ?: listOf(appOwner)
  }

  private val _currentUser = MutableStateFlow<UserProfile>(
    when {
      seedDemoData -> studentAsha
      restoredUser != null -> restoredUser
      else -> appOwner
    }
  )
  val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

  // Authentication State: persistent session across restarts, no auto-logout
  private val _isAuthenticated = MutableStateFlow<Boolean>(seedDemoData || restoredUser != null)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  private val _keepSessionActive = MutableStateFlow<Boolean>(sessionManager?.isKeepActiveEnabled() ?: true)
  val keepSessionActive: StateFlow<Boolean> = _keepSessionActive.asStateFlow()

  private val repoScope = CoroutineScope(Dispatchers.IO)
  val userRoleFirestoreRepo = UserRoleFirestoreRepository()
  val firestoreRoles: Flow<List<UserRoleDoc>> = userRoleFirestoreRepo.observeAllRoles()
  val supabaseBackend = com.example.data.remote.SupabaseBackendService()

  private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
  val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

  private val _isOffline = MutableStateFlow(false)
  val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

  private val _lastSynced = MutableStateFlow("Today, 13:08 EAT")
  val lastSynced: StateFlow<String> = _lastSynced.asStateFlow()

  private val _is24HourFormat = MutableStateFlow(true)
  val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

  private fun computeRoleLimits(user: UserProfile): RoleLimits {
    return when (user.role) {
      Role.STUDENT -> RoleLimits(
        announcementsLeft = 0,
        announcementsLimit = 0,
        urgentLeft = 0,
        urgentLimit = 0,
        materialsMaxMb = 15,
        submissionsLeft = 20,
        submissionsLimit = 20
      )
      Role.CLASS_REP -> RoleLimits(
        announcementsLeft = 5,
        announcementsLimit = 5,
        urgentLeft = 1,
        urgentLimit = 1,
        materialsMaxMb = 25,
        submissionsLeft = 20,
        submissionsLimit = 20
      )
      Role.LECTURER -> RoleLimits(
        announcementsLeft = 9,
        announcementsLimit = 10,
        urgentLeft = 2,
        urgentLimit = 2,
        materialsMaxMb = 50,
        submissionsLeft = 0,
        submissionsLimit = 0
      )
      Role.ADMIN -> RoleLimits(
        announcementsLeft = 100,
        announcementsLimit = 100,
        urgentLeft = 50,
        urgentLimit = 50,
        materialsMaxMb = 100,
        submissionsLeft = 50,
        submissionsLimit = 50
      )
      Role.SUPER_ADMIN -> RoleLimits(
        announcementsLeft = 999,
        announcementsLimit = 999,
        urgentLeft = 999,
        urgentLimit = 999,
        materialsMaxMb = 500,
        submissionsLeft = 999,
        submissionsLimit = 999
      )
    }
  }

  private val _userLimits = MutableStateFlow(computeRoleLimits(_currentUser.value))
  val userLimits: StateFlow<RoleLimits> = _userLimits.asStateFlow()

  // Initial Class Sessions with SessionKind (lecture, lab, tutorial)
  private val initialSessionsList = listOf(
      ClassSession(
        id = "sess-01",
        courseCode = "CS210",
        courseTitle = "Data Structures & Algorithms",
        lecturerId = "usr-lec-01",
        lecturerName = "Dr. Joseph Mushi",
        venue = "LTB Room 4",
        kind = SessionKind.LECTURE,
        dayOfWeek = 1,
        startTime = "08:00",
        endTime = "10:00",
        status = SessionStatus.SCHEDULED,
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-02",
        courseCode = "MT101",
        courseTitle = "Discrete Mathematics & Logic",
        lecturerId = "usr-lec-02",
        lecturerName = "Prof. Grace Temu",
        venue = "Science Complex Hall B",
        kind = SessionKind.LECTURE,
        dayOfWeek = 1,
        startTime = "11:00",
        endTime = "13:00",
        status = SessionStatus.SCHEDULED,
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-03",
        courseCode = "CS212",
        courseTitle = "Computer Architecture & Org.",
        lecturerId = "usr-lec-01",
        lecturerName = "Dr. Salim Bakari",
        venue = "Computer Lab 3 (Moved from LTB 2)",
        originalVenue = "LTB Room 2",
        kind = SessionKind.LECTURE,
        dayOfWeek = 2,
        startTime = "09:00",
        endTime = "11:00",
        status = SessionStatus.CHANGED,
        changeReason = "Projector lamp failure in LTB 2. Relocated to Computer Lab 3.",
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-04",
        courseCode = "IS202",
        courseTitle = "Relational Database Management Systems",
        lecturerId = "usr-lec-01",
        lecturerName = "Eng. Daniel Kweka",
        venue = "LTB Room 1",
        kind = SessionKind.LECTURE,
        dayOfWeek = 2,
        startTime = "14:00",
        endTime = "16:00",
        status = SessionStatus.SCHEDULED,
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-05",
        courseCode = "CS215",
        courseTitle = "Object-Oriented Software Engineering",
        lecturerId = "usr-lec-01",
        lecturerName = "Dr. Joseph Mushi",
        venue = "Main Hall South",
        kind = SessionKind.LECTURE,
        dayOfWeek = 3,
        startTime = "08:00",
        endTime = "10:00",
        status = SessionStatus.SCHEDULED,
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-06",
        courseCode = "CS210-L",
        courseTitle = "CS210 Lab: Tree Traversal & Heaps",
        lecturerId = "usr-lec-01",
        lecturerName = "Dr. Joseph Mushi",
        venue = "Advanced Software Lab",
        kind = SessionKind.LAB,
        dayOfWeek = 4,
        startTime = "10:00",
        endTime = "12:00",
        status = SessionStatus.SCHEDULED,
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-07",
        courseCode = "GS101",
        courseTitle = "Development Perspectives & Ethics",
        lecturerId = "usr-lec-02",
        lecturerName = "Dr. Mariam Ndosi",
        venue = "LTB Room 3",
        kind = SessionKind.TUTORIAL,
        dayOfWeek = 4,
        startTime = "14:00",
        endTime = "16:00",
        status = SessionStatus.CANCELLED,
        changeReason = "Lecturer attending Faculty Academic Board Meeting.",
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-hr-01",
        courseCode = "HR201",
        courseTitle = "Human Resource Management Principles",
        lecturerId = "usr-lec-hr",
        lecturerName = "Dr. Nehemia Moshi",
        venue = "Lecture Theatre A",
        kind = SessionKind.LECTURE,
        dayOfWeek = 1,
        startTime = "09:00",
        endTime = "11:00",
        status = SessionStatus.SCHEDULED,
        programme = "Bachelor of Human Resource Management",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      ClassSession(
        id = "sess-hr-02",
        courseCode = "HR202",
        courseTitle = "Organizational Behavior & Labor Law",
        lecturerId = "usr-lec-hr",
        lecturerName = "Dr. Nehemia Moshi",
        venue = "Room 104",
        kind = SessionKind.LECTURE,
        dayOfWeek = 3,
        startTime = "11:00",
        endTime = "13:00",
        status = SessionStatus.SCHEDULED,
        programme = "Bachelor of Human Resource Management",
        approvalStatus = ContentApprovalStatus.APPROVED
      )
    )
  private val _sessions = MutableStateFlow<List<ClassSession>>(if (seedDemoData) initialSessionsList else emptyList())
  val sessions: StateFlow<List<ClassSession>> = _sessions.asStateFlow()

  // Announcements
  private val initialAnnouncementsList = listOf(
      Announcement(
        id = "ann-cr-01",
        authorId = "usr-cr-01",
        authorName = "Juma Bakari",
        authorRole = "Class Representative (CR)",
        title = "Extra Revision Session on Graphs & Trees (On Behalf of Lecturer)",
        body = "On behalf of Dr. Joseph Mushi, an extra tutorial and question review session will take place on Saturday at 10:00 AM in LTB 4. Please prepare your questions from Tutorial Sheet 3.",
        audienceType = AudienceType.COURSE,
        audienceName = "CS210 - Algorithms",
        publishedAt = "Today, 11:30 EAT",
        priority = AnnouncementPriority.NORMAL,
        readCount = 42,
        totalStudents = 54,
        isRead = false,
        postedOnBehalfOfLecturer = "Dr. Joseph Mushi",
        postedOnBehalfOfLecturerId = "usr-lec-01",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      Announcement(
        id = "ann-hr-01",
        authorId = "usr-lec-hr",
        authorName = "Dr. Nehemia Moshi",
        authorRole = "Head of Department • Human Resources",
        title = "HRM Annual Professional Internship Placement Briefing",
        body = "All 2nd year Human Resource Management students are invited to the mandatory professional internship briefing this Thursday at 14:00 in Room 104. Bring your updated CV drafts.",
        audienceType = AudienceType.PROGRAMME,
        audienceName = "Bachelor of Human Resource Management",
        publishedAt = "Yesterday, 16:00 EAT",
        priority = AnnouncementPriority.NORMAL,
        readCount = 38,
        totalStudents = 45,
        isRead = false,
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      Announcement(
        id = "ann-01",
        authorId = "usr-lec-01",
        authorName = "Dr. Joseph Mushi",
        authorRole = "Senior Lecturer • Head of CS",
        title = "CS210 Midterm Continuous Assessment Test (CAT) Rescheduled",
        body = "Please note that the Midterm CAT originally slated for this Thursday has been moved to Monday 26 Oct 2026 at 08:00 in Main Examination Hall. The test will cover Binary Search Trees, AVL Trees, and Graph Traversal algorithms. Bring your official student IDs.",
        audienceType = AudienceType.COURSE,
        audienceName = "CS210 - Algorithms",
        publishedAt = "Today, 10:45 EAT",
        priority = AnnouncementPriority.URGENT,
        readCount = 48,
        totalStudents = 54,
        isRead = false,
        attachment = AttachmentFile(
          fileName = "CAT_Revision_Topics_2026.pdf",
          fileSize = "1.2 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "TOPICS INCLUDED FOR CS210 MIDTERM:\n1. Binary Trees: Height balance, recursive depth, Morris traversal\n2. AVL Trees: Single & double rotations, balance factors\n3. Graph Algorithms: Adjacency list representation, Breadth-First Search, Depth-First Search, topological sorting\n4. Space and Time Complexity calculations (Big-O analysis)."
        ),
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      Announcement(
        id = "ann-02",
        authorId = "usr-adm-01",
        authorName = "Academic Registry Office",
        authorRole = "Central Administration",
        title = "Deadline for Semester 1 Course Registration and Verification",
        body = "All undergraduate students are reminded that online course confirmation closes this Friday at 23:59 EAT. Ensure your fees clearance is updated with the bursar before submitting your registration slip to the department coordinator.",
        audienceType = AudienceType.CAMPUS,
        audienceName = "All Students • Campus-wide",
        publishedAt = "Yesterday, 14:20 EAT",
        priority = AnnouncementPriority.ACADEMIC,
        readCount = 312,
        totalStudents = 450,
        isRead = true,
        attachment = AttachmentFile(
          fileName = "Registration_Notice_Schedule.pdf",
          fileSize = "850 KB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "UNIVERSITY OF CAMPUSCONNECT TANZANIA\nOFFICE OF THE ACADEMIC REGISTRAR\n\nCIRCULAR: 2026/S1-04\nSubject: Semester 1 Student Clearance Verification\n1. Payment validation deadline: 24 Oct 2026.\n2. In-person biometric check: Student Center Wing B."
        ),
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      Announcement(
        id = "ann-03",
        authorId = "usr-lec-01",
        authorName = "Eng. Daniel Kweka",
        authorRole = "Lecturer • Database Systems",
        title = "PostgreSQL Lab Server Maintenance This Wednesday Evening",
        body = "The departmental database server (db.campus.ac.tz) will undergo storage expansion and PostgreSQL 16 upgrade on Wednesday between 18:00 and 22:00 EAT. Please commit and export your sql dumps before maintenance begins.",
        audienceType = AudienceType.PROGRAMME,
        audienceName = "BSc Computer Science & IS",
        publishedAt = "18 Oct 2026, 09:15 EAT",
        priority = AnnouncementPriority.NORMAL,
        readCount = 76,
        totalStudents = 95,
        isRead = true,
        attachment = AttachmentFile(
          fileName = "db_backup_instructions.pdf",
          fileSize = "480 KB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "POSTGRESQL BACKUP GUIDELINES:\npg_dump -U student -h db.campus.ac.tz -d my_db > backup_oct2026.sql\nVerify file size and store on local machine."
        ),
        approvalStatus = ContentApprovalStatus.APPROVED
      )
    )
  private val _announcements = MutableStateFlow<List<Announcement>>(if (seedDemoData) initialAnnouncementsList else emptyList())
  val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

  // Study Materials
  private val initialMaterialsList = listOf(
      StudyMaterial(
        id = "mat-01",
        courseCode = "CS210",
        courseTitle = "Data Structures & Algorithms",
        title = "Module 4: Balanced Search Trees & Red-Black Trees",
        uploaderId = "usr-lec-01",
        uploadedBy = "Dr. Joseph Mushi",
        uploadedAt = "18 Oct 2026",
        attachment = AttachmentFile(
          fileName = "Module_4_RedBlackTrees.pdf",
          fileSize = "2.8 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "MODULE 4: RED-BLACK BALANCED TREES\nLecturer: Dr. Joseph Mushi\n\nProperties:\n1. Every node is either red or black.\n2. Root is always black.\n3. Every leaf (NIL) is black.\n4. If a node is red, both children are black.\n5. For each node, all simple paths from the node to descendant leaves contain the same number of black nodes."
        ),
        isDownloaded = true,
        description = "Comprehensive lecture slides and pseudocode implementations for Red-Black Tree rotations and node deletion.",
        category = "Notes",
        tags = listOf("Trees", "Algorithms", "RedBlack", "Lecture 4"),
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      StudyMaterial(
        id = "mat-02",
        courseCode = "CS210",
        courseTitle = "Data Structures & Algorithms",
        title = "Tutorial Sheet 3: Graph Algorithms & Dijkstra",
        uploaderId = "usr-lec-01",
        uploadedBy = "Dr. Joseph Mushi",
        uploadedAt = "16 Oct 2026",
        attachment = AttachmentFile(
          fileName = "Tutorial_Sheet_03_Graphs.pdf",
          fileSize = "1.1 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "TUTORIAL SHEET 3\nQ1: Trace Dijkstra's algorithm on the given graph starting from node A.\nQ2: Prove whether Prim's algorithm works correctly with negative edge weights."
        ),
        isDownloaded = false,
        description = "Practice problems on shortest path algorithms and minimum spanning trees.",
        category = "Assignments",
        tags = listOf("Tutorial", "Dijkstra", "Graphs", "Problem Set"),
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      StudyMaterial(
        id = "mat-03",
        courseCode = "IS202",
        courseTitle = "Database Systems",
        title = "Relational Algebra & Advanced SQL Queries Handout",
        uploaderId = "usr-lec-01",
        uploadedBy = "Eng. Daniel Kweka",
        uploadedAt = "14 Oct 2026",
        attachment = AttachmentFile(
          fileName = "Relational_Algebra_Handout.pdf",
          fileSize = "3.4 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "RELATIONAL ALGEBRA HANDOUT\nCovers: Selection, Projection, Cross-Product, Theta Join, Outer Joins, and Aggregation."
        ),
        isDownloaded = true,
        description = "Covers joins, aggregation grouping, window functions, and indexing strategies in PostgreSQL.",
        category = "Syllabus",
        tags = listOf("Relational Algebra", "SQL", "Database", "Handout"),
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      StudyMaterial(
        id = "mat-04",
        courseCode = "MT101",
        courseTitle = "Discrete Mathematics",
        title = "Set Theory, Relations & Combinatorics Summary Notes",
        uploaderId = "usr-lec-02",
        uploadedBy = "Prof. Grace Temu",
        uploadedAt = "12 Oct 2026",
        attachment = AttachmentFile(
          fileName = "MT101_SetTheory_Summary.pdf",
          fileSize = "4.2 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "DISCRETE MATHEMATICS & LOGIC\nProf. Grace Temu\nSection 1: Propositional Logic and Truth Tables.\nSection 2: Pigeonhole Principle and Permutations."
        ),
        isDownloaded = false,
        description = "Lecture summaries and past question walkthroughs for the midterm revision.",
        category = "Exam Revision",
        tags = listOf("Set Theory", "Midterm", "Logic", "Summary"),
        programme = "BSc Computer Science",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      StudyMaterial(
        id = "mat-hr-01",
        courseCode = "HR201",
        courseTitle = "Human Resource Management",
        title = "Strategic Human Resource Planning & Labor Relations 2026",
        uploaderId = "usr-lec-hr",
        uploadedBy = "Dr. Nehemia Moshi",
        uploadedAt = "19 Oct 2026",
        attachment = AttachmentFile(
          fileName = "HR201_Strategic_Planning.pdf",
          fileSize = "2.1 MB",
          previewText = "STRATEGIC HUMAN RESOURCE MANAGEMENT\nModule 1: Workforce Planning & Talent Forecasting\nModule 2: Labor Law Compliance in Tanzania\nModule 3: Performance Management Frameworks."
        ),
        description = "Core textbook summary and lecture slides for Strategic HR Planning.",
        category = "Notes",
        tags = listOf("HR", "Strategic Planning", "Labor Law"),
        programme = "Bachelor of Human Resource Management",
        approvalStatus = ContentApprovalStatus.APPROVED
      )
    )
  private val _materials = MutableStateFlow<List<StudyMaterial>>(if (seedDemoData) initialMaterialsList else emptyList())
  val materials: StateFlow<List<StudyMaterial>> = _materials.asStateFlow()

  // Deadlines & Academic Calendar
  private val initialDeadlinesList = listOf(
      DeadlineItem(
        id = "dl-hr-01",
        courseCode = "HR201",
        courseTitle = "Human Resource Management",
        lecturerId = "usr-lec-hr",
        lecturerName = "Dr. Nehemia Moshi",
        title = "Case Study: Collective Bargaining & Dispute Resolution",
        instructions = "Analyze the provided collective bargaining agreement case and draft an arbitration proposal (Max 6 pages).",
        dueDateTime = "Monday, 27 Oct 2026 • 17:00 EAT",
        urgencyText = "Next week",
        acceptedFormats = "PDF only (Max 15 MB)",
        isCompleted = false,
        submittedCount = 12,
        totalStudents = 45,
        programme = "Bachelor of Human Resource Management",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      DeadlineItem(
        id = "dl-01",
        courseCode = "CS210",
        courseTitle = "Data Structures & Algorithms",
        lecturerId = "usr-lec-01",
        lecturerName = "Dr. Joseph Mushi",
        title = "Programming Assignment 2: Graph Cycle Detection",
        instructions = "Implement Dijkstra and Tarjan's algorithm in Kotlin, Java, or Python. Submit your clean source code PDF report or screenshot test run.",
        dueDateTime = "Tomorrow, 22 Oct 2026 • 23:59 EAT",
        urgencyText = "Due in 24 hours",
        acceptedFormats = "PDF or Images (Max 15 MB)",
        isCompleted = false,
        submittedCount = 38,
        totalStudents = 54,
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      DeadlineItem(
        id = "dl-02",
        courseCode = "IS202",
        courseTitle = "Relational Databases",
        lecturerId = "usr-lec-01",
        lecturerName = "Eng. Daniel Kweka",
        title = "Database Schema Design Proposal",
        instructions = "Submit ER diagrams (image or PDF), 3NF normalization justification, and PostgreSQL DDL scripts for your university hostel allocation system.",
        dueDateTime = "Friday, 24 Oct 2026 • 17:00 EAT",
        urgencyText = "Due in 3 days",
        acceptedFormats = "PDF, PNG or JPG (Max 20 MB)",
        isCompleted = true,
        submittedCount = 51,
        totalStudents = 54,
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      DeadlineItem(
        id = "dl-03",
        courseCode = "ACADEMIC",
        courseTitle = "University Registry",
        lecturerId = "usr-adm-01",
        lecturerName = "Emanuel Mwenda",
        title = "Semester 1 Course Registration Slip Verification",
        instructions = "Upload a clear scanned photo or PDF of your fee clearance slip with official registry stamps.",
        dueDateTime = "Friday, 24 Oct 2026 • 16:00 EAT",
        urgencyText = "Due in 3 days",
        acceptedFormats = "PDF or Image photo (Max 10 MB)",
        isCompleted = false,
        submittedCount = 320,
        totalStudents = 450,
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      DeadlineItem(
        id = "dl-04",
        courseCode = "CS215",
        courseTitle = "Software Engineering",
        lecturerId = "usr-lec-01",
        lecturerName = "Dr. Joseph Mushi",
        title = "Sprint 1 User Stories & Architecture Specification",
        instructions = "Submit software architecture document following IEEE 830 format and Figma diagram exports.",
        dueDateTime = "Monday, 27 Oct 2026 • 12:00 EAT",
        urgencyText = "Next week",
        acceptedFormats = "PDF only (Max 25 MB)",
        isCompleted = false,
        submittedCount = 14,
        totalStudents = 54,
        approvalStatus = ContentApprovalStatus.APPROVED
      )
    )
  private val _deadlines = MutableStateFlow<List<DeadlineItem>>(if (seedDemoData) initialDeadlinesList else emptyList())
  val deadlines: StateFlow<List<DeadlineItem>> = _deadlines.asStateFlow()

  // Student Submissions for assignments
  private val initialSubmissionsList = listOf(
      StudentSubmission(
        id = "sub-01",
        deadlineId = "dl-01",
        courseCode = "CS210",
        assignmentTitle = "Programming Assignment 2: Graph Cycle Detection",
        studentId = "usr-stu-01",
        studentName = "Asha Mwangi",
        studentNumber = "T22-03-08492",
        notes = "Tarjan's algorithm implemented with recursive DFS. Included test cases with cyclic and acyclic graphs.",
        attachment = AttachmentFile(
          fileName = "CS210_AshaMwangi_Tarjan_Report.pdf",
          fileSize = "1.8 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "CS210 ASSIGNMENT 2: TARJAN'S SCC & CYCLE DETECTION\nStudent: Asha Mwangi (T22-03-08492)\n\nImplementation:\nClass GraphCycleDetector(val vertices: Int) {\n  private val adj = Array(vertices) { mutableListOf<Int>() }\n  fun hasCycle(): Boolean { ... }\n}\nComplexity: O(V + E) verified with JUnit tests."
        ),
        submittedAt = "20 Oct 2026, 16:45 EAT",
        grade = "92/100",
        feedback = "Excellent work Asha! The Tarjan SCC stack implementation is mathematically sound and unit tests covered edge cases.",
        approvalStatus = ContentApprovalStatus.APPROVED
      ),
      StudentSubmission(
        id = "sub-02",
        deadlineId = "dl-01",
        courseCode = "CS210",
        assignmentTitle = "Programming Assignment 2: Graph Cycle Detection",
        studentId = "usr-stu-02",
        studentName = "Kelvin Tarimo",
        studentNumber = "T22-03-09120",
        notes = "Submission contains Dijkstra implementation and cycle check.",
        attachment = AttachmentFile(
          fileName = "Tarimo_CS210_Solution.pdf",
          fileSize = "2.1 MB",
          mimeType = "application/pdf",
          isImage = false,
          previewText = "CS210 ASSIGNMENT SUBMISSION\nStudent: Kelvin Tarimo\nAlgorithms: Dijkstra and Cycle Detection."
        ),
        submittedAt = "20 Oct 2026, 18:20 EAT",
        grade = null,
        feedback = null,
        approvalStatus = ContentApprovalStatus.PENDING_REVIEW
      )
    )
  private val _submissions = MutableStateFlow<List<StudentSubmission>>(if (seedDemoData) initialSubmissionsList else emptyList())
  val submissions: StateFlow<List<StudentSubmission>> = _submissions.asStateFlow()

  // Notifications
  private val initialNotificationsList = listOf(
      NotificationItem(
        id = "notif-01",
        title = "Venue Changed for CS212",
        message = "Computer Architecture on Tuesday moved from LTB 2 to Computer Lab 3.",
        type = NotificationType.TIMETABLE_CHANGE,
        timestamp = "Today, 11:30 EAT",
        isRead = false
      ),
      NotificationItem(
        id = "notif-02",
        title = "Urgent: CS210 Midterm CAT Moved",
        message = "Dr. Mushi posted an update regarding the rescheduled examination hall date.",
        type = NotificationType.ANNOUNCEMENT,
        timestamp = "Today, 10:45 EAT",
        isRead = false
      ),
      NotificationItem(
        id = "notif-03",
        title = "Assignment Graded: CS210",
        message = "Dr. Joseph Mushi graded your Assignment 2: 92/100.",
        type = NotificationType.GRADE_POSTED,
        timestamp = "Today, 17:00 EAT",
        isRead = false
      ),
      NotificationItem(
        id = "notif-04",
        title = "Deadline Tomorrow",
        message = "CS210 Programming Assignment 2 is due tomorrow at 23:59 EAT.",
        type = NotificationType.DEADLINE_ALERT,
        timestamp = "Today, 08:00 EAT",
        isRead = true
      )
    )
  private val _notifications = MutableStateFlow<List<NotificationItem>>(if (seedDemoData) initialNotificationsList else emptyList())
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  // Pending user accounts for Administrator review
  private val _pendingUsers = MutableStateFlow<List<UserProfile>>(emptyList())
  val pendingUsers: StateFlow<List<UserProfile>> = _pendingUsers.asStateFlow()

  // Audit Logs
  private val initialAuditLogsList = listOf(
      AuditLog(
        id = "log-01",
        actor = "Elia Mwenda (App Owner)",
        action = "ROOT_INITIALIZATION",
        details = "Owner initialized master root key and campus access controls.",
        timestamp = "21 Oct 2026, 08:00 EAT"
      ),
      AuditLog(
        id = "log-02",
        actor = "Dr. Joseph Mushi (Lecturer)",
        action = "ASSIGNMENT_GRADED",
        details = "Graded submission for Asha Mwangi: 92/100",
        timestamp = "20 Oct 2026, 17:00 EAT"
      )
    )
  private val _auditLogs = MutableStateFlow<List<AuditLog>>(
    if (seedDemoData) initialAuditLogsList else listOf(
      AuditLog(
        id = "log-01",
        actor = "Elia Mwenda (App Owner)",
        action = "ROOT_KEY_ACTIVE",
        details = "Master key enabled. Ready for live database synchronization.",
        timestamp = "Active"
      )
    )
  )
  val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

  // Multi-Campus Entities with Program Offerings, Courses, and Venues
  private val _campuses = MutableStateFlow<List<CampusEntity>>(
    listOf(
      CampusEntity(
        id = "cuom",
        name = "Catholic University of Mbeya (CUoM)",
        shortCode = "CUoM",
        dbName = "db_cuom_prod",
        dbRegion = "af-south-1 (Johannesburg)",
        serverStatus = ServerStatus.ONLINE,
        adminEmail = "admin.registry@cuom.ac.tz",
        adminName = "Emanuel Mwenda",
        programmes = listOf(
          CampusProgramme("BCS", "BSc Computer Science", "Faculty of Science & Computing"),
          CampusProgramme("BHRM", "Bachelor of Human Resource Management", "Faculty of Business & Humanities"),
          CampusProgramme("BIS", "BSc Information Systems", "Faculty of Science & Computing"),
          CampusProgramme("BBA", "Bachelor of Business Administration", "Faculty of Business"),
          CampusProgramme("LLB", "Bachelor of Laws", "Faculty of Law")
        ),
        courses = listOf(
          CampusCourse("CS210", "Data Structures & Algorithms", "Dr. Joseph Mushi", "BSc Computer Science"),
          CampusCourse("CS220", "Database Management Systems", "Dr. Joseph Mushi", "BSc Computer Science"),
          CampusCourse("HR201", "Human Resource Management Principles", "Dr. Nehemia Moshi", "Bachelor of Human Resource Management"),
          CampusCourse("HR202", "Organizational Behavior & Labor Law", "Dr. Nehemia Moshi", "Bachelor of Human Resource Management"),
          CampusCourse("IS101", "Systems Analysis & Design", "Faculty Staff", "BSc Information Systems"),
          CampusCourse("LAW102", "Constitutional & Administrative Law", "Faculty Staff", "Bachelor of Laws")
        ),
        venues = listOf(
          CampusVenue("LTB Room 4", 120, "Main Lecture Complex"),
          CampusVenue("Lab 3", 60, "Science & Computing Centre"),
          CampusVenue("Lecture Theatre A", 200, "Main Auditorium"),
          CampusVenue("Room 104", 45, "Academic Wing B")
        ),
        totalStudents = 142,
        totalLecturers = 18
      ),
      CampusEntity(
        id = "udsm",
        name = "University of Dar es Salaam (UDSM)",
        shortCode = "UDSM",
        dbName = "db_udsm_prod",
        dbRegion = "af-south-1 (Johannesburg)",
        serverStatus = ServerStatus.ONLINE,
        adminEmail = "registry@udsm.ac.tz",
        adminName = "Registry Office",
        programmes = listOf(
          CampusProgramme("CS", "BSc Computer Science", "CoICT"),
          CampusProgramme("TE", "BSc Telecommunications Engineering", "CoICT"),
          CampusProgramme("EC", "BA Economics", "CoSS"),
          CampusProgramme("MD", "Doctor of Medicine", "MUHAS/UDSM")
        ),
        courses = listOf(
          CampusCourse("CS174", "Programming in C/C++", "Prof. Grace Temu"),
          CampusCourse("EE201", "Signals and Systems", "Faculty Staff"),
          CampusCourse("EC101", "Principles of Microeconomics", "Faculty Staff")
        ),
        venues = listOf(
          CampusVenue("Yombo 4", 300, "Yombo Complex"),
          CampusVenue("Nkrumah Hall", 500, "Main Campus"),
          CampusVenue("CoICT Lab 2", 80, "Kijitonyama Campus")
        ),
        totalStudents = 380,
        totalLecturers = 42
      ),
      CampusEntity(
        id = "ifm",
        name = "Institute of Finance Management (IFM)",
        shortCode = "IFM",
        dbName = "db_ifm_prod",
        dbRegion = "af-south-1 (Johannesburg)",
        serverStatus = ServerStatus.ONLINE,
        adminEmail = "s.kimaro@student.ifm.ac.tz",
        adminName = "Dr. Sarah Kimaro",
        programmes = listOf(
          CampusProgramme("BIT", "BSc Information Technology", "Faculty of Computing"),
          CampusProgramme("CSF", "BSc Computer Security & Forensics", "Faculty of Computing"),
          CampusProgramme("ACT", "BSc Actuarial Science", "Faculty of Insurance"),
          CampusProgramme("BF", "BBA Banking & Finance", "Faculty of Finance")
        ),
        courses = listOf(
          CampusCourse("IT201", "Web Technologies & REST APIs", "Dr. Sarah Kimaro"),
          CampusCourse("CSF301", "Network Forensics & Security", "Faculty Staff"),
          CampusCourse("ACT102", "Probability and Risk Modeling", "Faculty Staff")
        ),
        venues = listOf(
          CampusVenue("Block C Hall 1", 150, "Block C"),
          CampusVenue("ICT Lab 1", 60, "Computing Centre"),
          CampusVenue("Main Auditorium", 350, "Administration Block")
        ),
        totalStudents = 210,
        totalLecturers = 25
      ),
      CampusEntity(
        id = "cbe",
        name = "College of Business Education (CBE)",
        shortCode = "CBE",
        dbName = "db_cbe_prod",
        dbRegion = "af-south-1 (Johannesburg)",
        serverStatus = ServerStatus.ONLINE,
        adminEmail = "admin@cbe.ac.tz",
        adminName = "Academic Dean",
        programmes = listOf(
          CampusProgramme("BBA", "Bachelor of Business Administration", "Faculty of Commerce"),
          CampusProgramme("BITM", "BSc Information Technology in Management", "Faculty of ICT")
        ),
        courses = listOf(
          CampusCourse("MGT101", "Principles of Modern Management", "Faculty Staff"),
          CampusCourse("ITM202", "Enterprise Resource Planning", "Faculty Staff")
        ),
        venues = listOf(
          CampusVenue("CBE Hall A", 200, "Main Block"),
          CampusVenue("Lab 1", 50, "ICT Building")
        ),
        totalStudents = 160,
        totalLecturers = 16
      )
    )
  )
  val campuses: StateFlow<List<CampusEntity>> = _campuses.asStateFlow()

  // Permission Checks:
  // Super Admin & Campus Admin have elevated permissions.
  // Super Admin can edit/delete anything globally.
  // Campus Admin has full rights for their campus data.
  // Uploader can edit or delete their own items.
  fun canEditOrDelete(uploaderOrAuthorId: String, onBehalfOfLecturerId: String? = null): Boolean {
    val user = _currentUser.value
    return user.role == Role.SUPER_ADMIN || user.role == Role.ADMIN || user.id == uploaderOrAuthorId || (onBehalfOfLecturerId != null && user.id == onBehalfOfLecturerId)
  }

  fun canApproveOrDisapprove(): Boolean {
    val user = _currentUser.value
    return user.role == Role.SUPER_ADMIN || user.role == Role.ADMIN
  }

  // Multi-Campus dynamic querying
  fun getProgrammesForUniversity(uniName: String): List<CampusProgramme> {
    val campus = _campuses.value.firstOrNull { it.name.equals(uniName, ignoreCase = true) || it.shortCode.equals(uniName, ignoreCase = true) }
    return campus?.programmes ?: listOf(
      CampusProgramme("GEN", "General Academic Studies", "Academic Registry"),
      CampusProgramme("BCS", "BSc Computer Science", "Computing")
    )
  }

  fun getCoursesForUniversity(uniName: String): List<CampusCourse> {
    val campus = _campuses.value.firstOrNull { it.name.equals(uniName, ignoreCase = true) || it.shortCode.equals(uniName, ignoreCase = true) }
    return campus?.courses ?: listOf(
      CampusCourse("CS210", "Data Structures & Algorithms", "Faculty Staff"),
      CampusCourse("CS220", "Database Management Systems", "Faculty Staff")
    )
  }

  fun getVenuesForUniversity(uniName: String): List<CampusVenue> {
    val campus = _campuses.value.firstOrNull { it.name.equals(uniName, ignoreCase = true) || it.shortCode.equals(uniName, ignoreCase = true) }
    return campus?.venues ?: listOf(
      CampusVenue("LTB Room 4", 120, "Main Block"),
      CampusVenue("Lab 3", 60, "ICT Complex"),
      CampusVenue("Lecture Theatre A", 200, "Auditorium")
    )
  }

  // Super Admin Action: Provision Database and Register New Campus
  fun addNewCampus(
    name: String,
    shortCode: String,
    adminEmail: String,
    adminName: String,
    dbRegion: String = "af-south-1 (Johannesburg)"
  ): CampusEntity {
    val cleanId = shortCode.lowercase().filter { it.isLetterOrDigit() }
    val newCampus = CampusEntity(
      id = cleanId,
      name = name.trim(),
      shortCode = shortCode.trim(),
      dbName = "db_${cleanId}_prod",
      dbRegion = dbRegion,
      serverStatus = ServerStatus.ONLINE,
      adminEmail = adminEmail.trim(),
      adminName = adminName.trim(),
      programmes = listOf(
        CampusProgramme("BCS", "BSc Computer Science", "Faculty of Computing"),
        CampusProgramme("BBA", "Bachelor of Business Administration", "Faculty of Business")
      ),
      courses = listOf(
        CampusCourse("CS101", "Introduction to Computing", adminName),
        CampusCourse("MGT101", "Principles of Management", "Faculty Staff")
      ),
      venues = listOf(
        CampusVenue("Hall 1", 150, "Administration Block"),
        CampusVenue("Lab 1", 50, "Science Wing")
      ),
      totalStudents = 1,
      totalLecturers = 1
    )

    _campuses.value = _campuses.value + newCampus

    // Create appointed admin persona for that campus
    val newCampusAdmin = UserProfile(
      id = "usr-adm-" + cleanId,
      fullName = "$adminName ($shortCode Admin)",
      email = adminEmail.trim(),
      role = Role.ADMIN,
      status = AccountStatus.APPROVED,
      university = name.trim(),
      staffNumber = "$shortCode-ADM-001",
      programme = "Campus Administration",
      cohort = "Chief Administrator"
    )
    _allRegisteredUsers.value = _allRegisteredUsers.value + newCampusAdmin

    recordAudit(
      action = "CAMPUS_DATABASE_PROVISIONED",
      details = "Super Admin provisioned database 'db_${cleanId}_prod' for $name ($shortCode) with admin $adminEmail"
    )

    return newCampus
  }

  // Super Admin Action: Server / Database Health Management
  fun updateCampusServerStatus(campusId: String, status: ServerStatus) {
    _campuses.value = _campuses.value.map {
      if (it.id == campusId) it.copy(serverStatus = status) else it
    }
    recordAudit(
      action = "CAMPUS_STATUS_UPDATED",
      details = "Super Admin updated database status for campus $campusId to ${status.name}"
    )
  }

  // Super Admin Action: Purge or Delete Campus Data with real Supabase Storage & DB wipe
  fun purgeCampusData(campusId: String) {
    val target = _campuses.value.firstOrNull { it.id == campusId } ?: return

    // Collect all attachment file paths belonging to this campus before removing rows
    val filesToDelete = mutableListOf<String>()
    _materials.value.filter { it.uploadedBy.contains(target.shortCode) || it.courseCode.startsWith(target.shortCode) }.forEach {
      filesToDelete.add("materials/${it.courseCode}/${it.uploaderId}/${it.attachment.fileName}")
    }
    _announcements.value.filter { it.audienceName.contains(target.name) || it.authorName.contains(target.shortCode) }.forEach { ann ->
      ann.attachment?.let { att -> filesToDelete.add("announcements/${ann.authorId}/${att.fileName}") }
    }
    _submissions.value.filter { it.studentName.contains(target.shortCode) }.forEach { sub ->
      filesToDelete.add("submissions/${sub.studentId}/${sub.deadlineId}/${sub.attachment.fileName}")
    }

    // Call real Supabase Storage API and Database RPC in background coroutine
    repoScope.launch {
      val wipeResult = supabaseBackend.purgeCampusStorageAndData(target.shortCode, filesToDelete)
      recordAudit(
        action = "SUPABASE_STORAGE_PURGE",
        details = "Permanently wiped ${wipeResult.deletedFilesCount} files from Supabase Storage buckets for ${target.name}."
      )
    }

    _sessions.value = _sessions.value.filterNot { it.programme.contains(target.shortCode) }
    _materials.value = _materials.value.filterNot { it.uploadedBy.contains(target.shortCode) }
    _allRegisteredUsers.value = _allRegisteredUsers.value.filterNot { it.university == target.name }
    _pendingUsers.value = _pendingUsers.value.filterNot { it.university == target.name }

    recordAudit(
      action = "CAMPUS_DATA_PURGED",
      details = "Super Admin purged database records and storage files for ${target.name}"
    )
  }

  // Campus Admin Action: Add Course to their Campus
  fun addCourseToCampus(universityName: String, code: String, title: String, lecturer: String, programmeName: String = "BSc Computer Science") {
    val newCourse = CampusCourse(code.trim().uppercase(), title.trim(), lecturer.trim(), programmeName.trim())
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        campus.copy(courses = campus.courses + newCourse)
      } else campus
    }
    recordAudit(
      action = "COURSE_CREATED",
      details = "Added course ${newCourse.code}: ${newCourse.title} to $universityName"
    )
  }

  fun editCourseInCampus(universityName: String, oldCode: String, newCode: String, newTitle: String, newLecturer: String, programmeName: String = "BSc Computer Science") {
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        val updatedCourses = campus.courses.map { c ->
          if (c.code.equals(oldCode, ignoreCase = true)) {
            CampusCourse(newCode.trim().uppercase(), newTitle.trim(), newLecturer.trim(), programmeName.trim())
          } else c
        }
        campus.copy(courses = updatedCourses)
      } else campus
    }
    recordAudit(
      action = "COURSE_EDITED",
      details = "Updated course $oldCode to $newCode ($newTitle) in $universityName"
    )
  }

  fun deleteCourseFromCampus(universityName: String, code: String) {
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        campus.copy(courses = campus.courses.filterNot { it.code.equals(code, ignoreCase = true) })
      } else campus
    }
    recordAudit(
      action = "COURSE_DELETED",
      details = "Deleted course $code from $universityName"
    )
  }

  // Campus Admin Action: Add Venue to their Campus
  fun addVenueToCampus(universityName: String, name: String, capacity: Int, building: String, hasProjector: Boolean = true) {
    val newVenue = CampusVenue(name.trim(), capacity, building.trim(), hasProjector)
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        campus.copy(venues = campus.venues + newVenue)
      } else campus
    }
    recordAudit(
      action = "VENUE_CREATED",
      details = "Added venue $name (Cap $capacity) at $building to $universityName"
    )
  }

  fun editVenueInCampus(universityName: String, oldName: String, newName: String, newCapacity: Int, newBuilding: String, hasProjector: Boolean) {
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        val updatedVenues = campus.venues.map { v ->
          if (v.name.equals(oldName, ignoreCase = true)) {
            CampusVenue(newName.trim(), newCapacity, newBuilding.trim(), hasProjector)
          } else v
        }
        campus.copy(venues = updatedVenues)
      } else campus
    }
    recordAudit(
      action = "VENUE_EDITED",
      details = "Updated venue $oldName to $newName (Cap $newCapacity) in $universityName"
    )
  }

  fun deleteVenueFromCampus(universityName: String, name: String) {
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        campus.copy(venues = campus.venues.filterNot { it.name.equals(name, ignoreCase = true) })
      } else campus
    }
    recordAudit(
      action = "VENUE_DELETED",
      details = "Deleted venue $name from $universityName"
    )
  }

  // Campus Admin Action: Add Programme to their Campus
  fun addProgrammeToCampus(universityName: String, code: String, name: String, department: String) {
    val newProg = CampusProgramme(code.trim().uppercase(), name.trim(), department.trim())
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        campus.copy(programmes = campus.programmes + newProg)
      } else campus
    }
    recordAudit(
      action = "PROGRAMME_CREATED",
      details = "Added programme ${newProg.code}: ${newProg.name} to $universityName"
    )
  }

  fun editProgrammeInCampus(universityName: String, oldCode: String, newCode: String, newName: String, newDepartment: String) {
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        val updatedProgrammes = campus.programmes.map { p ->
          if (p.code.equals(oldCode, ignoreCase = true)) {
            CampusProgramme(newCode.trim().uppercase(), newName.trim(), newDepartment.trim())
          } else p
        }
        campus.copy(programmes = updatedProgrammes)
      } else campus
    }
    recordAudit(
      action = "PROGRAMME_EDITED",
      details = "Updated programme $oldCode to $newCode ($newName) in $universityName"
    )
  }

  fun deleteProgrammeFromCampus(universityName: String, code: String) {
    _campuses.value = _campuses.value.map { campus ->
      if (campus.name.equals(universityName, ignoreCase = true)) {
        campus.copy(programmes = campus.programmes.filterNot { it.code.equals(code, ignoreCase = true) })
      } else campus
    }
    recordAudit(
      action = "PROGRAMME_DELETED",
      details = "Deleted programme $code from $universityName"
    )
  }

  // Super Admin: Campus Management
  fun editCampus(campusId: String, newName: String, newShortCode: String, newAdminEmail: String, newRegion: String) {
    _campuses.value = _campuses.value.map { c ->
      if (c.id == campusId) {
        c.copy(name = newName.trim(), shortCode = newShortCode.trim(), adminEmail = newAdminEmail.trim(), dbRegion = newRegion.trim())
      } else c
    }
    recordAudit(
      action = "CAMPUS_EDITED",
      details = "Super Admin edited configuration for campus $campusId ($newName)"
    )
  }

  fun deleteCampus(campusId: String) {
    val target = _campuses.value.firstOrNull { it.id == campusId } ?: return
    purgeCampusData(campusId)
    _campuses.value = _campuses.value.filterNot { it.id == campusId }
    recordAudit(
      action = "CAMPUS_DELETED",
      details = "Super Admin deleted campus ${target.name} (${target.shortCode})"
    )
  }

  // Create Timetable Session with structured subject, venue, and lecturer
  fun addTimetableSession(session: ClassSession) {
    _sessions.value = listOf(session) + _sessions.value
    recordAudit(
      action = "SESSION_SCHEDULED",
      details = "Scheduled ${session.courseCode} in ${session.venue} by ${session.lecturerName}"
    )
  }

  // -------------------------------------------------------------
  // STRICT SCOPE & PROGRAMME ISOLATION FILTERING
  // Ensures CS students do not see HR students' details/docs/classes, and vice versa!
  // -------------------------------------------------------------
  fun filterSessionsForUser(list: List<ClassSession>, user: UserProfile): List<ClassSession> {
    if (user.role == Role.SUPER_ADMIN) return list
    if (user.role == Role.ADMIN) return list
    if (user.role == Role.LECTURER) {
      return list.filter { s ->
        s.lecturerId == user.id || s.lecturerName.contains(user.fullName, ignoreCase = true) ||
          (user.programme != null && s.programme.contains(user.programme.take(10), ignoreCase = true))
      }
    }
    val userProg = user.programme.orEmpty().trim()
    return list.filter { session ->
      if (userProg.isBlank()) true
      else session.programme.equals(userProg, ignoreCase = true) ||
           (user.enrolledCourses.isNotEmpty() && user.enrolledCourses.any { it.equals(session.courseCode, ignoreCase = true) })
    }
  }

  fun filterMaterialsForUser(list: List<StudyMaterial>, user: UserProfile): List<StudyMaterial> {
    if (user.role == Role.SUPER_ADMIN) return list
    if (user.role == Role.ADMIN) return list
    if (user.role == Role.LECTURER) {
      return list.filter { m ->
        m.uploaderId == user.id || m.uploadedBy.contains(user.fullName, ignoreCase = true) ||
          (user.programme != null && m.programme.contains(user.programme.take(10), ignoreCase = true))
      }
    }
    val userProg = user.programme.orEmpty().trim()
    return list.filter { mat ->
      if (userProg.isBlank()) true
      else mat.programme.equals(userProg, ignoreCase = true) ||
           (user.enrolledCourses.isNotEmpty() && user.enrolledCourses.any { it.equals(mat.courseCode, ignoreCase = true) })
    }
  }

  fun filterDeadlinesForUser(list: List<DeadlineItem>, user: UserProfile): List<DeadlineItem> {
    if (user.role == Role.SUPER_ADMIN) return list
    if (user.role == Role.ADMIN) return list
    if (user.role == Role.LECTURER) {
      return list.filter { d ->
        d.lecturerId == user.id || d.lecturerName.contains(user.fullName, ignoreCase = true) ||
          (user.programme != null && d.programme.contains(user.programme.take(10), ignoreCase = true))
      }
    }
    val userProg = user.programme.orEmpty().trim()
    return list.filter { dl ->
      if (dl.courseCode == "ACADEMIC") true
      else if (userProg.isBlank()) true
      else dl.programme.equals(userProg, ignoreCase = true) ||
           (user.enrolledCourses.isNotEmpty() && user.enrolledCourses.any { it.equals(dl.courseCode, ignoreCase = true) })
    }
  }

  fun filterAnnouncementsForUser(list: List<Announcement>, user: UserProfile): List<Announcement> {
    if (user.role == Role.SUPER_ADMIN) return list
    if (user.role == Role.ADMIN) return list
    if (user.role == Role.LECTURER) return list

    val userProg = user.programme.orEmpty().trim()
    return list.filter { ann ->
      when (ann.audienceType) {
        AudienceType.CAMPUS -> true
        AudienceType.PROGRAMME -> {
          if (userProg.isBlank()) true
          else ann.audienceName.contains(userProg, ignoreCase = true) ||
               userProg.contains(ann.audienceName, ignoreCase = true) ||
               (userProg.contains("Computer", ignoreCase = true) && ann.audienceName.contains("CS", ignoreCase = true)) ||
               (userProg.contains("Human", ignoreCase = true) && ann.audienceName.contains("HR", ignoreCase = true))
        }
        AudienceType.COURSE -> {
          if (user.enrolledCourses.isNotEmpty() && user.enrolledCourses.any { ann.audienceName.contains(it, ignoreCase = true) }) {
            true
          } else if (userProg.contains("Computer", ignoreCase = true) && (ann.audienceName.contains("CS", ignoreCase = true) || ann.audienceName.contains("MT", ignoreCase = true) || ann.audienceName.contains("IS", ignoreCase = true))) {
            true
          } else if (userProg.contains("Human", ignoreCase = true) && ann.audienceName.contains("HR", ignoreCase = true)) {
            true
          } else {
            false
          }
        }
      }
    }
  }

  private val _allRegisteredUsers = MutableStateFlow<List<UserProfile>>(allPersonas)
  val allRegisteredUsers: StateFlow<List<UserProfile>> = _allRegisteredUsers.asStateFlow()

  // Actions
  fun switchUser(user: UserProfile) {
    _currentUser.value = user
    _userLimits.value = computeRoleLimits(user)
    if (_isAuthenticated.value) {
      sessionManager?.saveSession(user, _keepSessionActive.value)
    }
  }

  fun login(identifier: String, password: String): Boolean {
    val cleanId = identifier.trim()
    if (cleanId.isBlank()) return false

    // App Owner direct credentials
    if (cleanId.equals("eliamwenda01@gmail.com", ignoreCase = true) || cleanId.equals("SYS-OWNER-001", ignoreCase = true)) {
      _currentUser.value = appOwner
      _userLimits.value = computeRoleLimits(appOwner)
      _isAuthenticated.value = true
      sessionManager?.saveSession(appOwner, _keepSessionActive.value)
      recordAudit("AUTH_LOGIN", "Master App Owner Elia Mwenda logged in successfully.")
      return true
    }

    // Check registered users by email or registration / staff number
    val foundUser = _allRegisteredUsers.value.find { u ->
      u.email.equals(cleanId, ignoreCase = true) ||
      (u.studentNumber != null && u.studentNumber.equals(cleanId, ignoreCase = true)) ||
      (u.staffNumber != null && u.staffNumber.equals(cleanId, ignoreCase = true))
    }

    if (foundUser != null) {
      _currentUser.value = foundUser
      _userLimits.value = computeRoleLimits(foundUser)
      _isAuthenticated.value = true
      sessionManager?.saveSession(foundUser, _keepSessionActive.value)
      recordAudit("AUTH_LOGIN", "User ${foundUser.fullName} (${foundUser.university}) logged in.")
      return true
    }

    return false
  }

  fun loginAsOwner(): Boolean {
    _currentUser.value = appOwner
    _userLimits.value = computeRoleLimits(appOwner)
    _isAuthenticated.value = true
    sessionManager?.saveSession(appOwner, _keepSessionActive.value)
    recordAudit("AUTH_LOGIN_OWNER", "Owner direct root key authenticated for Elia Mwenda.")
    return true
  }

  fun logout() {
    _isAuthenticated.value = false
    sessionManager?.clearSession()
    recordAudit("AUTH_LOGOUT", "User ${_currentUser.value.fullName} signed out.")
  }

  fun setKeepSessionActive(keep: Boolean) {
    _keepSessionActive.value = keep
    sessionManager?.setKeepActive(keep)
    if (keep && _isAuthenticated.value) {
      sessionManager?.saveSession(_currentUser.value, keepActive = true)
    }
  }

  fun setLanguage(language: AppLanguage) {
    _appLanguage.value = language
  }

  fun toggle24HourFormat() {
    _is24HourFormat.value = !_is24HourFormat.value
  }

  fun toggleOfflineSimulation() {
    _isOffline.value = !_isOffline.value
    if (!_isOffline.value) {
      val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
      _lastSynced.value = "Today, $now EAT"
    }
  }

  fun triggerSync() {
    val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    _lastSynced.value = "Today, $now EAT"
  }

  fun markAnnouncementRead(id: String) {
    _announcements.value = _announcements.value.map {
      if (it.id == id && !it.isRead) {
        it.copy(isRead = true, readCount = it.readCount + 1)
      } else it
    }
  }

  fun toggleDeadlineCompleted(id: String) {
    _deadlines.value = _deadlines.value.map {
      if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
    }
  }

  fun toggleMaterialDownloaded(id: String) {
    _materials.value = _materials.value.map {
      if (it.id == id) it.copy(isDownloaded = !it.isDownloaded) else it
    }
  }

  fun markAllNotificationsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }

  // Student Work: Submit Assignment
  fun submitStudentWork(
    deadlineId: String,
    courseCode: String,
    assignmentTitle: String,
    notes: String,
    attachment: AttachmentFile
  ) {
    val student = _currentUser.value
    val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()) + " EAT"
    val newSub = StudentSubmission(
      id = "sub-" + UUID.randomUUID().toString().take(6),
      deadlineId = deadlineId,
      courseCode = courseCode,
      assignmentTitle = assignmentTitle,
      studentId = student.id,
      studentName = student.fullName,
      studentNumber = student.studentNumber ?: "STUDENT-${student.id.takeLast(4)}",
      notes = notes,
      attachment = attachment,
      submittedAt = now,
      approvalStatus = ContentApprovalStatus.PENDING_REVIEW
    )
    _submissions.value = listOf(newSub) + _submissions.value

    // Increment submitted count on deadline
    _deadlines.value = _deadlines.value.map { dl ->
      if (dl.id == deadlineId) dl.copy(submittedCount = dl.submittedCount + 1, isCompleted = true) else dl
    }

    recordAudit(
      action = "WORK_SUBMITTED",
      details = "${student.fullName} submitted work for $courseCode ($assignmentTitle) [${attachment.fileName}]"
    )
  }

  // Lecturer/Admin: Grade Student Work
  fun gradeStudentWork(submissionId: String, grade: String, feedback: String) {
    val actor = _currentUser.value
    _submissions.value = _submissions.value.map { sub ->
      if (sub.id == submissionId) {
        sub.copy(grade = grade, feedback = feedback, approvalStatus = ContentApprovalStatus.APPROVED)
      } else sub
    }

    val sub = _submissions.value.find { it.id == submissionId }
    if (sub != null) {
      val notif = NotificationItem(
        id = "notif-" + UUID.randomUUID().toString().take(6),
        title = "Assignment Graded: ${sub.courseCode}",
        message = "${actor.fullName} posted your grade ($grade) for ${sub.assignmentTitle}",
        type = NotificationType.GRADE_POSTED,
        timestamp = "Just now",
        isRead = false
      )
      _notifications.value = listOf(notif) + _notifications.value
    }

    recordAudit(
      action = "WORK_GRADED",
      details = "Graded submission $submissionId with grade: $grade"
    )
  }

  // Delete Student Submission (uploader or admin)
  fun deleteStudentSubmission(submissionId: String) {
    val sub = _submissions.value.find { it.id == submissionId } ?: return
    if (!canEditOrDelete(sub.studentId)) return
    _submissions.value = _submissions.value.filter { it.id != submissionId }
    recordAudit(
      action = "SUBMISSION_DELETED",
      details = "Deleted submission ${sub.assignmentTitle} by ${sub.studentName}"
    )
  }

  // Admin, Lecturer & Class Representative: Announcements CRUD
  fun publishAnnouncement(
    title: String,
    body: String,
    audienceType: AudienceType,
    audienceName: String,
    priority: AnnouncementPriority,
    attachment: AttachmentFile? = null,
    postedOnBehalfOfLecturer: String? = null,
    postedOnBehalfOfLecturerId: String? = null
  ) {
    val author = _currentUser.value
    val isCR = author.role == Role.CLASS_REP
    val authorRole = when (author.role) {
      Role.CLASS_REP -> "Class Representative (CR)"
      Role.LECTURER -> author.cohort ?: "Faculty Member"
      Role.ADMIN -> "Central Administration"
      Role.SUPER_ADMIN -> "Global Administrator"
      else -> "Student"
    }
    val initialStatus = if (author.role == Role.ADMIN || author.role == Role.LECTURER || isCR) ContentApprovalStatus.APPROVED else ContentApprovalStatus.PENDING_REVIEW

    val resolvedLecturerId = postedOnBehalfOfLecturerId ?: if (postedOnBehalfOfLecturer != null) {
      if (postedOnBehalfOfLecturer.contains("Mushi", ignoreCase = true)) "usr-lec-01"
      else if (postedOnBehalfOfLecturer.contains("Temu", ignoreCase = true)) "usr-lec-02"
      else if (postedOnBehalfOfLecturer.contains("Moshi", ignoreCase = true)) "usr-lec-hr"
      else "usr-lec-01"
    } else null

    val newAnn = Announcement(
      id = "ann-" + UUID.randomUUID().toString().take(6),
      authorId = author.id,
      authorName = author.fullName,
      authorRole = authorRole,
      title = title,
      body = body,
      audienceType = audienceType,
      audienceName = audienceName,
      publishedAt = "Just now",
      priority = priority,
      readCount = 0,
      totalStudents = 54,
      isRead = true,
      postedOnBehalfOfLecturer = postedOnBehalfOfLecturer,
      postedOnBehalfOfLecturerId = resolvedLecturerId,
      attachment = attachment,
      approvalStatus = initialStatus
    )
    _announcements.value = listOf(newAnn) + _announcements.value

    val notif = NotificationItem(
      id = "notif-" + UUID.randomUUID().toString().take(6),
      title = "New Announcement: $title",
      message = "${author.fullName} published an announcement for $audienceName.",
      type = NotificationType.ANNOUNCEMENT,
      timestamp = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value

    recordAudit(
      action = "ANNOUNCEMENT_POSTED",
      details = "Published announcement '$title' targeting $audienceName"
    )
  }

  fun updateAnnouncement(id: String, newTitle: String, newBody: String, newPriority: AnnouncementPriority) {
    val ann = _announcements.value.find { it.id == id } ?: return
    if (!canEditOrDelete(ann.authorId, ann.postedOnBehalfOfLecturerId)) return

    _announcements.value = _announcements.value.map {
      if (it.id == id) it.copy(title = newTitle, body = newBody, priority = newPriority) else it
    }
    recordAudit(
      action = "ANNOUNCEMENT_EDITED",
      details = "Edited announcement '$newTitle'"
    )
  }

  fun deleteAnnouncement(id: String) {
    val ann = _announcements.value.find { it.id == id } ?: return
    if (!canEditOrDelete(ann.authorId, ann.postedOnBehalfOfLecturerId)) return

    _announcements.value = _announcements.value.filter { it.id != id }
    recordAudit(
      action = "ANNOUNCEMENT_DELETED",
      details = "Deleted announcement '${ann.title}'"
    )
  }

  fun setAnnouncementApproval(id: String, status: ContentApprovalStatus) {
    if (!canApproveOrDisapprove()) return
    _announcements.value = _announcements.value.map {
      if (it.id == id) it.copy(approvalStatus = status) else it
    }
    recordAudit(
      action = "CONTENT_APPROVAL_CHANGED",
      details = "Admin set announcement $id status to ${status.name}"
    )
  }

  // Supabase v2 schema RPC: retract_announcement
  fun retractAnnouncement(id: String) {
    val ann = _announcements.value.find { it.id == id } ?: return
    if (!canEditOrDelete(ann.authorId, ann.postedOnBehalfOfLecturerId)) return

    val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()) + " EAT"
    _announcements.value = _announcements.value.map {
      if (it.id == id) it.copy(
        isRetracted = true,
        retractedAt = now,
        retractedBy = _currentUser.value.fullName
      ) else it
    }
    recordAudit(
      action = "ANNOUNCEMENT_RETRACTED",
      details = "Retracted / withdrew academic announcement '${ann.title}'"
    )
  }

  // Supabase v2 schema RPC: remind_unread
  fun remindUnread(id: String) {
    val ann = _announcements.value.find { it.id == id } ?: return
    if (!canEditOrDelete(ann.authorId, ann.postedOnBehalfOfLecturerId)) return

    val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()) + " EAT"
    _announcements.value = _announcements.value.map {
      if (it.id == id) it.copy(remindedAt = now) else it
    }

    val unreadCount = (ann.totalStudents - ann.readCount).coerceAtLeast(1)
    val notif = NotificationItem(
      id = "notif-" + UUID.randomUUID().toString().take(6),
      title = "Urgent Notice Reminder: ${ann.title}",
      message = "Reminder sent to $unreadCount unread students for '${ann.title}'.",
      type = NotificationType.DEADLINE_ALERT,
      timestamp = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value

    recordAudit(
      action = "ANNOUNCEMENT_REMINDER_SENT",
      details = "Dispatched notification reminder for '${ann.title}' to $unreadCount unread students"
    )
  }

  // Admin & Uploader: Study Materials CRUD
  fun uploadMaterial(
    title: String,
    courseCode: String,
    courseTitle: String,
    description: String,
    attachment: AttachmentFile,
    category: String = "Notes",
    tags: List<String> = emptyList()
  ) {
    val author = _currentUser.value
    val newMat = StudyMaterial(
      id = "mat-" + UUID.randomUUID().toString().take(6),
      courseCode = courseCode,
      courseTitle = courseTitle,
      title = title,
      uploaderId = author.id,
      uploadedBy = author.fullName,
      uploadedAt = "Today",
      attachment = attachment,
      isDownloaded = true,
      description = description,
      category = category,
      tags = tags,
      approvalStatus = ContentApprovalStatus.APPROVED
    )
    _materials.value = listOf(newMat) + _materials.value

    recordAudit(
      action = "MATERIAL_UPLOAD",
      details = "Uploaded '$title' for $courseCode (${attachment.fileName})"
    )
  }

  fun updateMaterial(id: String, newTitle: String, newDescription: String) {
    val mat = _materials.value.find { it.id == id } ?: return
    if (!canEditOrDelete(mat.uploaderId)) return

    _materials.value = _materials.value.map {
      if (it.id == id) it.copy(title = newTitle, description = newDescription) else it
    }
    recordAudit(
      action = "MATERIAL_EDITED",
      details = "Edited study material '$newTitle'"
    )
  }

  fun deleteMaterial(id: String) {
    val mat = _materials.value.find { it.id == id } ?: return
    if (!canEditOrDelete(mat.uploaderId)) return

    _materials.value = _materials.value.filter { it.id != id }
    recordAudit(
      action = "MATERIAL_DELETED",
      details = "Deleted study material '${mat.title}'"
    )
  }

  fun setMaterialApproval(id: String, status: ContentApprovalStatus) {
    if (!canApproveOrDisapprove()) return
    _materials.value = _materials.value.map {
      if (it.id == id) it.copy(approvalStatus = status) else it
    }
    recordAudit(
      action = "MATERIAL_APPROVAL_CHANGED",
      details = "Admin set material $id status to ${status.name}"
    )
  }

  // Admin & Lecturer: Sessions CRUD
  fun updateSessionVenue(sessionId: String, newVenue: String, reason: String) {
    val sess = _sessions.value.find { it.id == sessionId } ?: return
    if (!canEditOrDelete(sess.lecturerId)) return

    var courseCode = ""
    _sessions.value = _sessions.value.map { s ->
      if (s.id == sessionId) {
        courseCode = s.courseCode
        s.copy(
          venue = newVenue,
          originalVenue = s.originalVenue ?: s.venue,
          status = SessionStatus.CHANGED,
          changeReason = reason
        )
      } else s
    }

    val notif = NotificationItem(
      id = "notif-" + UUID.randomUUID().toString().take(6),
      title = "Room Changed: $courseCode",
      message = "Relocated to $newVenue. Note: $reason",
      type = NotificationType.TIMETABLE_CHANGE,
      timestamp = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value

    recordAudit(
      action = "VENUE_CHANGE",
      details = "Updated $courseCode venue to $newVenue ($reason)"
    )
  }

  fun cancelSession(sessionId: String, reason: String) {
    val sess = _sessions.value.find { it.id == sessionId } ?: return
    if (!canEditOrDelete(sess.lecturerId)) return

    var courseCode = ""
    _sessions.value = _sessions.value.map { s ->
      if (s.id == sessionId) {
        courseCode = s.courseCode
        s.copy(
          status = SessionStatus.CANCELLED,
          changeReason = reason
        )
      } else s
    }

    val notif = NotificationItem(
      id = "notif-" + UUID.randomUUID().toString().take(6),
      title = "Lecture Cancelled: $courseCode",
      message = "Session was marked cancelled: $reason",
      type = NotificationType.TIMETABLE_CHANGE,
      timestamp = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value

    recordAudit(
      action = "SESSION_CANCELLED",
      details = "Cancelled $courseCode session ($reason)"
    )
  }

  fun restoreSession(sessionId: String) {
    val sess = _sessions.value.find { it.id == sessionId } ?: return
    if (!canEditOrDelete(sess.lecturerId)) return

    _sessions.value = _sessions.value.map { s ->
      if (s.id == sessionId) {
        s.copy(
          status = SessionStatus.SCHEDULED,
          changeReason = null,
          venue = s.originalVenue ?: s.venue
        )
      } else s
    }
  }

  fun deleteSession(sessionId: String) {
    val sess = _sessions.value.find { it.id == sessionId } ?: return
    if (!canEditOrDelete(sess.lecturerId)) return

    _sessions.value = _sessions.value.filter { it.id != sessionId }
    recordAudit(
      action = "SESSION_DELETED",
      details = "Deleted timetable session ${sess.courseCode} (${sess.venue})"
    )
  }

  fun setSessionApproval(sessionId: String, status: ContentApprovalStatus) {
    if (!canApproveOrDisapprove()) return
    _sessions.value = _sessions.value.map {
      if (it.id == sessionId) it.copy(approvalStatus = status) else it
    }
    recordAudit(
      action = "SESSION_APPROVAL_CHANGED",
      details = "Admin set session $sessionId status to ${status.name}"
    )
  }

  // Admin & Lecturer: Deadlines CRUD
  fun addDeadline(
    courseCode: String,
    courseTitle: String,
    title: String,
    instructions: String,
    dueDateTime: String,
    acceptedFormats: String
  ) {
    val author = _currentUser.value
    val newDl = DeadlineItem(
      id = "dl-" + UUID.randomUUID().toString().take(6),
      courseCode = courseCode,
      courseTitle = courseTitle,
      lecturerId = author.id,
      lecturerName = author.fullName,
      title = title,
      instructions = instructions,
      dueDateTime = dueDateTime,
      urgencyText = "Upcoming",
      acceptedFormats = acceptedFormats.ifBlank { "PDF or Images (Max 15 MB)" },
      isCompleted = false,
      submittedCount = 0,
      totalStudents = 54,
      approvalStatus = ContentApprovalStatus.APPROVED
    )
    _deadlines.value = listOf(newDl) + _deadlines.value

    val notif = NotificationItem(
      id = "notif-" + UUID.randomUUID().toString().take(6),
      title = "New Deadline: $courseCode",
      message = "$title due on $dueDateTime",
      type = NotificationType.DEADLINE_ALERT,
      timestamp = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value

    recordAudit(
      action = "DEADLINE_CREATED",
      details = "Created deadline '$title' for $courseCode due $dueDateTime"
    )
  }

  fun updateDeadline(id: String, newTitle: String, newInstructions: String, newDueDateTime: String) {
    val dl = _deadlines.value.find { it.id == id } ?: return
    if (!canEditOrDelete(dl.lecturerId)) return

    _deadlines.value = _deadlines.value.map {
      if (it.id == id) it.copy(title = newTitle, instructions = newInstructions, dueDateTime = newDueDateTime) else it
    }
    recordAudit(
      action = "DEADLINE_EDITED",
      details = "Edited deadline '$newTitle'"
    )
  }

  fun deleteDeadline(id: String) {
    val dl = _deadlines.value.find { it.id == id } ?: return
    if (!canEditOrDelete(dl.lecturerId)) return

    _deadlines.value = _deadlines.value.filter { it.id != id }
    recordAudit(
      action = "DEADLINE_DELETED",
      details = "Deleted deadline '${dl.title}'"
    )
  }

  fun setDeadlineApproval(id: String, status: ContentApprovalStatus) {
    if (!canApproveOrDisapprove()) return
    _deadlines.value = _deadlines.value.map {
      if (it.id == id) it.copy(approvalStatus = status) else it
    }
    recordAudit(
      action = "DEADLINE_APPROVAL_CHANGED",
      details = "Admin set deadline $id status to ${status.name}"
    )
  }

  // Admin Actions: Approve or Reject Pending User
  fun approveUser(userId: String) {
    if (!canApproveOrDisapprove()) return
    val user = _pendingUsers.value.find { it.id == userId } ?: return
    _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
    recordAudit(
      action = "ACCOUNT_APPROVED",
      details = "Approved ${user.role.name.lowercase().replaceFirstChar { it.uppercase() }} account for ${user.fullName}"
    )
  }

  fun rejectUser(userId: String) {
    if (!canApproveOrDisapprove()) return
    val user = _pendingUsers.value.find { it.id == userId } ?: return
    _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
    recordAudit(
      action = "ACCOUNT_REJECTED",
      details = "Rejected account registration for ${user.fullName}"
    )
  }

  // Supabase v2 schema RPC: suspend_account
  fun suspendUser(userId: String, reason: String) {
    if (!canApproveOrDisapprove()) return
    _allRegisteredUsers.value = _allRegisteredUsers.value.map { u ->
      if (u.id == userId) u.copy(status = AccountStatus.SUSPENDED, suspensionReason = reason) else u
    }
    // Also update if pending
    _pendingUsers.value = _pendingUsers.value.map { u ->
      if (u.id == userId) u.copy(status = AccountStatus.SUSPENDED, suspensionReason = reason) else u
    }
    recordAudit(
      action = "ACCOUNT_SUSPENDED",
      details = "Admin suspended account $userId. Reason: $reason"
    )
  }

  fun reinstateUser(userId: String) {
    if (!canApproveOrDisapprove()) return
    _allRegisteredUsers.value = _allRegisteredUsers.value.map { u ->
      if (u.id == userId) u.copy(status = AccountStatus.APPROVED, suspensionReason = null) else u
    }
    recordAudit(
      action = "ACCOUNT_REINSTATED",
      details = "Admin reinstated account $userId to Approved status"
    )
  }

  // Firestore user_roles collection role modification (Admin-only)
  fun assignUserRole(userId: String, newRole: Role) {
    if (!canApproveOrDisapprove()) return
    repoScope.launch {
      userRoleFirestoreRepo.updateUserRole(userId, newRole, _currentUser.value.id)
    }
    _allRegisteredUsers.value = _allRegisteredUsers.value.map { u ->
      if (u.id == userId) u.copy(role = newRole) else u
    }
    if (_currentUser.value.id == userId) {
      val updated = _currentUser.value.copy(role = newRole)
      _currentUser.value = updated
      _userLimits.value = computeRoleLimits(updated)
    }
    recordAudit(
      action = "FIRESTORE_ROLE_UPDATED",
      details = "Admin changed role for user $userId to ${newRole.name} in Firestore collection 'user_roles'"
    )
  }

  // Universal Multi-Campus Sign-Up / Registration
  fun registerUser(
    fullName: String,
    registrationNumber: String,
    university: String,
    email: String,
    role: Role,
    programme: String
  ): UserProfile {
    val newId = "usr-" + role.name.lowercase().take(3) + "-" + UUID.randomUUID().toString().take(6)
    val newUser = UserProfile(
      id = newId,
      fullName = fullName.trim(),
      email = email.trim(),
      role = role,
      status = if (role == Role.STUDENT) AccountStatus.APPROVED else AccountStatus.PENDING,
      university = university,
      studentNumber = if (role == Role.STUDENT) registrationNumber.trim() else null,
      staffNumber = if (role != Role.STUDENT) registrationNumber.trim() else null,
      programme = programme.trim().ifBlank { "Faculty Academic Studies" },
      cohort = if (role == Role.STUDENT) "Registered 2026" else "Faculty Staff"
    )

    _allRegisteredUsers.value = listOf(newUser) + _allRegisteredUsers.value

    if (newUser.status == AccountStatus.PENDING) {
      _pendingUsers.value = listOf(newUser) + _pendingUsers.value
    }

    // Save to Firestore user_roles collection
    repoScope.launch {
      val doc = UserRoleDoc(
        id = newId,
        userId = newId,
        email = newUser.email,
        fullName = newUser.fullName,
        role = newUser.role.name,
        accountStatus = newUser.status.name,
        university = newUser.university,
        studentOrStaffNumber = registrationNumber.trim(),
        department = newUser.programme ?: "",
        assignedBy = "self-registration"
      )
      userRoleFirestoreRepo.registerUserRole(doc)
    }

    // Automatically switch active session to newly created user
    switchUser(newUser)
    _isAuthenticated.value = true
    sessionManager?.saveSession(newUser, _keepSessionActive.value)

    val notif = NotificationItem(
      id = "notif-" + UUID.randomUUID().toString().take(6),
      title = "Account Registered",
      message = "Welcome, ${newUser.fullName}! Your account at $university has been initialized.",
      type = NotificationType.APPROVAL,
      timestamp = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value

    recordAudit(
      action = "USER_REGISTERED",
      details = "Registered user ${newUser.fullName} ($university • $registrationNumber • ${role.name})"
    )

    return newUser
  }

  fun seedDemoFixtures() {
    _sessions.value = initialSessionsList
    _announcements.value = initialAnnouncementsList
    _materials.value = initialMaterialsList
    _deadlines.value = initialDeadlinesList
    _submissions.value = initialSubmissionsList
    _notifications.value = initialNotificationsList
    _allRegisteredUsers.value = listOf(appOwner, studentAsha, crJuma, studentNeema, studentKelvin, unapprovedStudentBaraka, suspendedStudentDaudi, lecturerMushi, lecturerNehemia, lecturerGrace, adminEmanuel, adminSarah, superAdminTariq)
    _currentUser.value = studentAsha
    _isAuthenticated.value = true
  }

  fun clearLocalCache() {
    _announcements.value = _announcements.value.map { it.copy(isRead = false) }
    _materials.value = _materials.value.map { it.copy(isDownloaded = false) }
    _deadlines.value = _deadlines.value.map { it.copy(isCompleted = false) }
    _notifications.value = emptyList()
    _lastSynced.value = "Cache cleared"
  }

  private fun recordAudit(action: String, details: String) {
    val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()) + " EAT"
    val actor = _currentUser.value.fullName + " (${_currentUser.value.role.name})"
    val log = AuditLog(
      id = "log-" + UUID.randomUUID().toString().take(6),
      actor = actor,
      action = action,
      details = details,
      timestamp = now
    )
    _auditLogs.value = listOf(log) + _auditLogs.value
  }
}
