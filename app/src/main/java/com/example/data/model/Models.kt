package com.example.data.model

enum class Role {
  STUDENT,
  CLASS_REP,
  LECTURER,
  ADMIN,
  SUPER_ADMIN
}

enum class AccountStatus {
  APPROVED,
  PENDING,
  REJECTED,
  SUSPENDED
}

enum class ContentApprovalStatus {
  APPROVED,
  PENDING_REVIEW,
  DISAPPROVED,
  DRAFT
}

enum class SessionKind {
  LECTURE,
  LAB,
  TUTORIAL
}

enum class SessionStatus {
  SCHEDULED,
  CHANGED,
  CANCELLED
}

enum class AudienceType {
  CAMPUS,
  PROGRAMME,
  COURSE
}

enum class AnnouncementPriority {
  NORMAL,
  URGENT,
  ACADEMIC
}

enum class NotificationType {
  TIMETABLE_CHANGE,
  ANNOUNCEMENT,
  DEADLINE_ALERT,
  APPROVAL,
  SUBMISSION_RECEIVED,
  GRADE_POSTED
}

enum class AppLanguage {
  ENGLISH,
  SWAHILI
}

data class RoleLimits(
  val announcementsLeft: Int,
  val announcementsLimit: Int,
  val urgentLeft: Int,
  val urgentLimit: Int,
  val materialsMaxMb: Int,
  val submissionsLeft: Int,
  val submissionsLimit: Int
)

data class AttachmentFile(
  val uriString: String? = null,
  val fileName: String,
  val fileSize: String,
  val mimeType: String = "application/pdf",
  val isImage: Boolean = false,
  val previewText: String? = null
)

data class UserProfile(
  val id: String,
  val fullName: String,
  val email: String,
  val role: Role,
  val status: AccountStatus,
  val university: String = "Catholic University of Mbeya (CUoM)",
  val studentNumber: String? = null,
  val staffNumber: String? = null,
  val programme: String? = null,
  val cohort: String? = null,
  val enrolledCourses: List<String> = emptyList(),
  val suspensionReason: String? = null,
  val avatarInitials: String = fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
)

data class ClassSession(
  val id: String,
  val courseCode: String,
  val courseTitle: String,
  val lecturerId: String,
  val lecturerName: String,
  val venue: String,
  val originalVenue: String? = null,
  val kind: SessionKind = SessionKind.LECTURE,
  val dayOfWeek: Int, // 1 = Mon, ..., 6 = Sat
  val startTime: String,
  val endTime: String,
  val status: SessionStatus = SessionStatus.SCHEDULED,
  val changeReason: String? = null,
  val programme: String = "BSc Computer Science",
  val cohort: String = "Year 2",
  val approvalStatus: ContentApprovalStatus = ContentApprovalStatus.APPROVED
)

data class Announcement(
  val id: String,
  val authorId: String,
  val authorName: String,
  val authorRole: String,
  val title: String,
  val body: String,
  val audienceType: AudienceType,
  val audienceName: String,
  val publishedAt: String,
  val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
  val readCount: Int = 0,
  val totalStudents: Int = 54,
  val isRead: Boolean = false,
  val isRetracted: Boolean = false,
  val retractedAt: String? = null,
  val retractedBy: String? = null,
  val remindedAt: String? = null,
  val postedOnBehalfOfLecturer: String? = null,
  val postedOnBehalfOfLecturerId: String? = null,
  val attachment: AttachmentFile? = null,
  val approvalStatus: ContentApprovalStatus = ContentApprovalStatus.APPROVED
)

data class StudyMaterial(
  val id: String,
  val courseCode: String,
  val courseTitle: String,
  val title: String,
  val uploaderId: String,
  val uploadedBy: String,
  val uploadedAt: String,
  val attachment: AttachmentFile,
  val isDownloaded: Boolean = false,
  val description: String = "",
  val category: String = "Notes", // e.g. "Notes", "Syllabus", "Assignments", "Exam Revision", "Lab Manual"
  val tags: List<String> = emptyList(),
  val programme: String = "BSc Computer Science",
  val approvalStatus: ContentApprovalStatus = ContentApprovalStatus.APPROVED
)

data class DeadlineItem(
  val id: String,
  val courseCode: String,
  val courseTitle: String,
  val lecturerId: String,
  val lecturerName: String,
  val title: String,
  val instructions: String,
  val dueDateTime: String,
  val urgencyText: String,
  val acceptedFormats: String = "PDF or Images (Max 15 MB)",
  val isCompleted: Boolean = false,
  val submittedCount: Int = 0,
  val totalStudents: Int = 54,
  val programme: String = "BSc Computer Science",
  val approvalStatus: ContentApprovalStatus = ContentApprovalStatus.APPROVED
)

data class StudentSubmission(
  val id: String,
  val deadlineId: String,
  val courseCode: String,
  val assignmentTitle: String,
  val studentId: String,
  val studentName: String,
  val studentNumber: String,
  val notes: String = "",
  val attachment: AttachmentFile,
  val submittedAt: String,
  val grade: String? = null,
  val feedback: String? = null,
  val approvalStatus: ContentApprovalStatus = ContentApprovalStatus.APPROVED
)

data class NotificationItem(
  val id: String,
  val title: String,
  val message: String,
  val type: NotificationType,
  val timestamp: String,
  val isRead: Boolean = false
)

data class AuditLog(
  val id: String,
  val actor: String,
  val action: String,
  val details: String,
  val timestamp: String
)

enum class ServerStatus {
  ONLINE,
  DEGRADED,
  MAINTENANCE,
  OFFLINE
}

data class CampusProgramme(
  val code: String,
  val name: String,
  val department: String = "Academic Faculty"
)

data class CampusCourse(
  val code: String,
  val title: String,
  val lecturerName: String = "Faculty Staff",
  val programmeName: String = "BSc Computer Science"
)

data class CampusVenue(
  val name: String,
  val capacity: Int,
  val building: String = "Main Campus",
  val hasProjector: Boolean = true
)

data class CampusEntity(
  val id: String,
  val name: String,
  val shortCode: String,
  val dbName: String,
  val dbRegion: String = "af-south-1 (Johannesburg)",
  val serverStatus: ServerStatus = ServerStatus.ONLINE,
  val adminEmail: String,
  val adminName: String,
  val programmes: List<CampusProgramme> = emptyList(),
  val courses: List<CampusCourse> = emptyList(),
  val venues: List<CampusVenue> = emptyList(),
  val totalStudents: Int = 120,
  val totalLecturers: Int = 14
)

