package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.UserRoleDoc
import com.example.data.model.AccountStatus
import com.example.data.model.Announcement
import com.example.data.model.AnnouncementPriority
import com.example.data.model.AppLanguage
import com.example.data.model.AttachmentFile
import com.example.data.model.AudienceType
import com.example.data.model.CampusCourse
import com.example.data.model.CampusEntity
import com.example.data.model.CampusProgramme
import com.example.data.model.CampusVenue
import com.example.data.model.ClassSession
import com.example.data.model.ContentApprovalStatus
import com.example.data.model.DeadlineItem
import com.example.data.model.Role
import com.example.data.model.ServerStatus
import com.example.data.model.StudentSubmission
import com.example.data.model.StudyMaterial
import com.example.data.model.UserProfile
import com.example.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.flow.combine

enum class ScreenTab {
  HOME,
  TIMETABLE,
  MATERIALS,
  CALENDAR,
  SUBMISSIONS,
  PROFILE,
  ADMIN,
  NOTIFICATIONS,
  DOCUREAD,
  PROCESSED_DOCS
}

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

class CampusViewModel(
  private val repository: CampusRepository = CampusRepository()
) : ViewModel() {

  val currentUser = repository.currentUser
  val appLanguage = repository.appLanguage
  val isOffline = repository.isOffline
  val lastSynced = repository.lastSynced
  val is24HourFormat = repository.is24HourFormat

  // Scope & Programme Isolation: Every user only sees their own programme and course records
  val sessions: StateFlow<List<ClassSession>> = combine(repository.sessions, currentUser) { list, user ->
    repository.filterSessionsForUser(list, user)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val announcements: StateFlow<List<Announcement>> = combine(repository.announcements, currentUser) { list, user ->
    repository.filterAnnouncementsForUser(list, user)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val materials: StateFlow<List<StudyMaterial>> = combine(repository.materials, currentUser) { list, user ->
    repository.filterMaterialsForUser(list, user)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val deadlines: StateFlow<List<DeadlineItem>> = combine(repository.deadlines, currentUser) { list, user ->
    repository.filterDeadlinesForUser(list, user)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSessions = repository.sessions
  val allAnnouncements = repository.announcements
  val allMaterials = repository.materials
  val allDeadlines = repository.deadlines

  val submissions = repository.submissions
  val notifications = repository.notifications
  val pendingUsers = repository.pendingUsers
  val auditLogs = repository.auditLogs
  val campuses = repository.campuses

  val allPersonas = repository.allPersonas
  val allRegisteredUsers = repository.allRegisteredUsers
  val userLimits = repository.userLimits
  val firestoreRoles: StateFlow<List<UserRoleDoc>> = repository.firestoreRoles
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Authentication State & Session Persistence (No auto-logout)
  val isAuthenticated: StateFlow<Boolean> = repository.isAuthenticated
  val keepSessionActive: StateFlow<Boolean> = repository.keepSessionActive

  fun login(identifier: String, password: String): Boolean = repository.login(identifier, password)
  fun loginAsOwner(): Boolean = repository.loginAsOwner()
  fun logout() = repository.logout()
  fun setKeepSessionActive(keep: Boolean) = repository.setKeepSessionActive(keep)

  // Active Navigation Tab
  private val _currentTab = MutableStateFlow(ScreenTab.HOME)
  val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

  // App Theme Mode (Adaptive System, Light, Dark)
  private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
  }

  fun toggleTheme() {
    _themeMode.value = when (_themeMode.value) {
      AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
      AppThemeMode.LIGHT -> AppThemeMode.DARK
      AppThemeMode.DARK -> AppThemeMode.SYSTEM
    }
  }

  // Timetable State
  private val _selectedDay = MutableStateFlow(1)
  val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

  private val _isWeekView = MutableStateFlow(false)
  val isWeekView: StateFlow<Boolean> = _isWeekView.asStateFlow()

  // Filter for Announcements
  private val _announcementFilter = MutableStateFlow("ALL")
  val announcementFilter: StateFlow<String> = _announcementFilter.asStateFlow()

  // Filter for Submissions
  private val _submissionFilterCourse = MutableStateFlow("ALL")
  val submissionFilterCourse: StateFlow<String> = _submissionFilterCourse.asStateFlow()

  // Unread counts
  val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
    list.count { !it.isRead }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val unreadAnnouncementsCount: StateFlow<Int> = announcements.map { list ->
    list.count { !it.isRead }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Permissions
  fun canEditOrDelete(uploaderOrAuthorId: String, onBehalfOfLecturerId: String? = null): Boolean {
    return repository.canEditOrDelete(uploaderOrAuthorId, onBehalfOfLecturerId)
  }

  fun canApproveOrDisapprove(): Boolean {
    return repository.canApproveOrDisapprove()
  }

  // Navigation
  fun setTab(tab: ScreenTab) {
    _currentTab.value = tab
  }

  fun setSelectedDay(day: Int) {
    _selectedDay.value = day
  }

  fun toggleWeekView() {
    _isWeekView.value = !_isWeekView.value
  }

  fun setAnnouncementFilter(filter: String) {
    _announcementFilter.value = filter
  }

  fun setSubmissionFilterCourse(course: String) {
    _submissionFilterCourse.value = course
  }

  fun switchUser(user: UserProfile) {
    repository.switchUser(user)
  }

  fun toggleLanguage() {
    val newLang = if (appLanguage.value == AppLanguage.ENGLISH) AppLanguage.SWAHILI else AppLanguage.ENGLISH
    repository.setLanguage(newLang)
  }

  fun toggle24HourFormat() {
    repository.toggle24HourFormat()
  }

  fun toggleOfflineSimulation() {
    repository.toggleOfflineSimulation()
  }

  fun triggerSync() {
    repository.triggerSync()
  }

  fun markAnnouncementRead(id: String) {
    repository.markAnnouncementRead(id)
  }

  fun toggleDeadlineCompleted(id: String) {
    repository.toggleDeadlineCompleted(id)
  }

  fun toggleMaterialDownloaded(id: String) {
    repository.toggleMaterialDownloaded(id)
  }

  fun markAllNotificationsRead() {
    repository.markAllNotificationsRead()
  }

  // Student Work Submission
  fun submitStudentWork(
    deadlineId: String,
    courseCode: String,
    assignmentTitle: String,
    notes: String,
    attachment: AttachmentFile
  ) {
    repository.submitStudentWork(deadlineId, courseCode, assignmentTitle, notes, attachment)
  }

  fun gradeStudentWork(submissionId: String, grade: String, feedback: String) {
    repository.gradeStudentWork(submissionId, grade, feedback)
  }

  fun deleteStudentSubmission(submissionId: String) {
    repository.deleteStudentSubmission(submissionId)
  }

  // Announcements CRUD & Approvals
  fun publishAnnouncement(
    title: String,
    body: String,
    audienceType: AudienceType,
    audienceName: String,
    priority: AnnouncementPriority,
    attachment: AttachmentFile?,
    postedOnBehalfOfLecturer: String? = null,
    postedOnBehalfOfLecturerId: String? = null
  ) {
    repository.publishAnnouncement(title, body, audienceType, audienceName, priority, attachment, postedOnBehalfOfLecturer, postedOnBehalfOfLecturerId)
  }

  fun updateAnnouncement(id: String, newTitle: String, newBody: String, newPriority: AnnouncementPriority) {
    repository.updateAnnouncement(id, newTitle, newBody, newPriority)
  }

  fun deleteAnnouncement(id: String) {
    repository.deleteAnnouncement(id)
  }

  fun setAnnouncementApproval(id: String, status: ContentApprovalStatus) {
    repository.setAnnouncementApproval(id, status)
  }

  // Study Materials CRUD & Approvals
  fun uploadMaterial(
    title: String,
    courseCode: String,
    courseTitle: String,
    description: String,
    attachment: AttachmentFile,
    category: String = "Notes",
    tags: List<String> = emptyList()
  ) {
    repository.uploadMaterial(title, courseCode, courseTitle, description, attachment, category, tags)
  }

  fun updateMaterial(id: String, newTitle: String, newDescription: String) {
    repository.updateMaterial(id, newTitle, newDescription)
  }

  fun deleteMaterial(id: String) {
    repository.deleteMaterial(id)
  }

  fun setMaterialApproval(id: String, status: ContentApprovalStatus) {
    repository.setMaterialApproval(id, status)
  }

  // Sessions CRUD & Approvals
  fun updateSessionVenue(sessionId: String, newVenue: String, reason: String) {
    repository.updateSessionVenue(sessionId, newVenue, reason)
  }

  fun cancelSession(sessionId: String, reason: String) {
    repository.cancelSession(sessionId, reason)
  }

  fun restoreSession(sessionId: String) {
    repository.restoreSession(sessionId)
  }

  fun deleteSession(sessionId: String) {
    repository.deleteSession(sessionId)
  }

  fun setSessionApproval(sessionId: String, status: ContentApprovalStatus) {
    repository.setSessionApproval(sessionId, status)
  }

  // Deadlines CRUD & Approvals
  fun addDeadline(courseCode: String, courseTitle: String, title: String, instructions: String, dueDateTime: String, acceptedFormats: String) {
    repository.addDeadline(courseCode, courseTitle, title, instructions, dueDateTime, acceptedFormats)
  }

  fun updateDeadline(id: String, newTitle: String, newInstructions: String, newDueDateTime: String) {
    repository.updateDeadline(id, newTitle, newInstructions, newDueDateTime)
  }

  fun deleteDeadline(id: String) {
    repository.deleteDeadline(id)
  }

  fun setDeadlineApproval(id: String, status: ContentApprovalStatus) {
    repository.setDeadlineApproval(id, status)
  }

  // Admin user approvals & suspensions (Supabase v2 RPCs)
  fun approveUser(userId: String) {
    repository.approveUser(userId)
  }

  fun rejectUser(userId: String) {
    repository.rejectUser(userId)
  }

  fun suspendUser(userId: String, reason: String) {
    repository.suspendUser(userId, reason)
  }

  fun reinstateUser(userId: String) {
    repository.reinstateUser(userId)
  }

  fun assignUserRole(userId: String, newRole: Role) {
    repository.assignUserRole(userId, newRole)
  }

  fun registerUser(
    fullName: String,
    registrationNumber: String,
    university: String,
    email: String,
    role: Role,
    programme: String
  ): UserProfile {
    return repository.registerUser(fullName, registrationNumber, university, email, role, programme)
  }

  fun retractAnnouncement(id: String) {
    repository.retractAnnouncement(id)
  }

  fun remindUnread(id: String) {
    repository.remindUnread(id)
  }

  fun clearLocalCache() {
    repository.clearLocalCache()
  }

  // Multi-Campus dynamic querying
  fun getProgrammesForUniversity(uni: String) = repository.getProgrammesForUniversity(uni)
  fun getCoursesForUniversity(uni: String) = repository.getCoursesForUniversity(uni)
  fun getVenuesForUniversity(uni: String) = repository.getVenuesForUniversity(uni)

  // Super Admin actions
  fun addNewCampus(
    name: String,
    shortCode: String,
    adminEmail: String,
    adminName: String,
    dbRegion: String = "af-south-1 (Johannesburg)"
  ) = repository.addNewCampus(name, shortCode, adminEmail, adminName, dbRegion)

  fun editCampus(campusId: String, newName: String, newShortCode: String, adminEmail: String, region: String) =
    repository.editCampus(campusId, newName, newShortCode, adminEmail, region)

  fun deleteCampus(campusId: String) =
    repository.deleteCampus(campusId)

  fun updateCampusServerStatus(campusId: String, status: ServerStatus) =
    repository.updateCampusServerStatus(campusId, status)

  fun purgeCampusData(campusId: String) = repository.purgeCampusData(campusId)

  // Campus Admin actions: Course Management
  fun addCourseToCampus(uni: String, code: String, title: String, lecturer: String, programmeName: String = "BSc Computer Science") =
    repository.addCourseToCampus(uni, code, title, lecturer, programmeName)

  fun editCourseInCampus(uni: String, oldCode: String, newCode: String, title: String, lecturer: String, programmeName: String = "BSc Computer Science") =
    repository.editCourseInCampus(uni, oldCode, newCode, title, lecturer, programmeName)

  fun deleteCourseFromCampus(uni: String, code: String) =
    repository.deleteCourseFromCampus(uni, code)

  // Campus Admin actions: Venue Management
  fun addVenueToCampus(uni: String, name: String, capacity: Int, building: String, hasProjector: Boolean = true) =
    repository.addVenueToCampus(uni, name, capacity, building, hasProjector)

  fun editVenueInCampus(uni: String, oldName: String, newName: String, capacity: Int, building: String, hasProjector: Boolean) =
    repository.editVenueInCampus(uni, oldName, newName, capacity, building, hasProjector)

  fun deleteVenueFromCampus(uni: String, name: String) =
    repository.deleteVenueFromCampus(uni, name)

  // Campus Admin actions: Programme Management
  fun addProgrammeToCampus(uni: String, code: String, name: String, department: String) =
    repository.addProgrammeToCampus(uni, code, name, department)

  fun editProgrammeInCampus(uni: String, oldCode: String, newCode: String, name: String, department: String) =
    repository.editProgrammeInCampus(uni, oldCode, newCode, name, department)

  fun deleteProgrammeFromCampus(uni: String, code: String) =
    repository.deleteProgrammeFromCampus(uni, code)

  fun addTimetableSession(session: ClassSession) = repository.addTimetableSession(session)

  // Query lecturers available for CR to post on behalf of
  fun getLecturersForUniversity(uni: String): List<UserProfile> {
    return allRegisteredUsers.value.filter {
      it.role == Role.LECTURER && (it.university.equals(uni, ignoreCase = true) || it.university.contains("CUoM"))
    }
  }

  // Localized strings helper
  fun getString(key: String): String {
    val isSwahili = appLanguage.value == AppLanguage.ENGLISH.takeIf { false } != null || appLanguage.value == AppLanguage.SWAHILI
    return when (key) {
      "tab_home" -> if (isSwahili) "Nyumbani" else "Home"
      "tab_timetable" -> if (isSwahili) "Ratiba" else "Timetable"
      "tab_announcements" -> if (isSwahili) "Matangazo" else "Announcements"
      "tab_materials" -> if (isSwahili) "Vifaa" else "Materials"
      "tab_calendar" -> if (isSwahili) "Tarehe" else "Deadlines"
      "tab_submissions" -> if (isSwahili) "Kazi Zilizotumwa" else "Submissions"
      "tab_profile" -> if (isSwahili) "Wasifu" else "Profile"
      "tab_admin" -> if (isSwahili) "Utawala" else "Admin"
      "notifications" -> if (isSwahili) "Arifa" else "Notifications"
      "room_changed" -> if (isSwahili) "Chumba kimebadilishwa" else "Room changed"
      "cancelled" -> if (isSwahili) "Imeghairiwa" else "Cancelled"
      "scheduled" -> if (isSwahili) "Imepangwa" else "Scheduled"
      "greeting" -> if (isSwahili) "Habari" else "Hello"
      "today_classes" -> if (isSwahili) "Masomo ya Leo" else "Today's Classes"
      "latest_announcement" -> if (isSwahili) "Tangazo Lililo Angaziwa" else "Highlighted Announcement"
      "next_deadline" -> if (isSwahili) "Tarehe Inayofuata" else "Next Upcoming Deadline"
      "quick_actions" -> if (isSwahili) "Vitendo vya Haraka" else "Quick Actions"
      "my_assigned_courses" -> if (isSwahili) "Masomo Yangu Niliyopangiwa" else "My Assigned Courses"
      "pending_guest_notice" -> if (isSwahili) "Hali ya Mgeni: Unaweza kuona ratiba na matangazo yote bila kizuizi." else "Guest Access: You can view all timetables, announcements and materials freely."
      "offline_notice" -> if (isSwahili) "Hali ya Nje ya Mtandao • Room SQLite" else "Offline Mode • Room SQLite"
      else -> key
    }
  }
}
