@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Announcement
import com.example.data.model.AnnouncementPriority
import com.example.data.model.AttachmentFile
import com.example.data.model.AudienceType
import com.example.data.model.ClassSession
import com.example.data.model.ContentApprovalStatus
import com.example.data.model.DeadlineItem
import com.example.data.model.Role
import com.example.data.model.RoleLimits
import com.example.data.model.StudyMaterial
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberLight

@Composable
fun AnnouncementDetailDialog(
  announcement: Announcement,
  currentUserRole: Role,
  canModerate: Boolean,
  onDismiss: () -> Unit,
  onPreviewAttachment: (AttachmentFile) -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onToggleApproval: (ContentApprovalStatus) -> Unit,
  onRetractClick: (() -> Unit)? = null,
  onRemindUnreadClick: (() -> Unit)? = null
) {
  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("announcement_detail_modal")
  ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(
              text = announcement.audienceName,
              backgroundColor = DeepBlue.copy(alpha = 0.12f),
              contentColor = DeepBlue
            )

            if (announcement.isRetracted) {
              Spacer(modifier = Modifier.width(6.dp))
              StatusBadge(
                text = "WITHDRAWN",
                backgroundColor = DangerRedLight,
                contentColor = DangerRed
              )
            } else if (announcement.approvalStatus == ContentApprovalStatus.PENDING_REVIEW) {
              Spacer(modifier = Modifier.width(6.dp))
              StatusBadge(
                text = "Pending Approval",
                backgroundColor = WarmAmberLight,
                contentColor = Color(0xFFB45309)
              )
            } else if (announcement.approvalStatus == ContentApprovalStatus.DISAPPROVED) {
              Spacer(modifier = Modifier.width(6.dp))
              StatusBadge(
                text = "Disapproved",
                backgroundColor = DangerRedLight,
                contentColor = DangerRed
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        if (announcement.isRetracted) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DangerRedLight,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.EventBusy, contentDescription = "Retracted", tint = DangerRed)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Official Notice Retracted / Withdrawn",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = DangerRed
                )
                Text(
                  text = "Withdrawn on ${announcement.retractedAt ?: "recently"} by ${announcement.retractedBy ?: "author"}. Students are advised to disregard earlier instructions.",
                  style = MaterialTheme.typography.bodySmall,
                  color = DangerRed
                )
              }
            }
          }
        }

        if (announcement.remindedAt != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "📢 Notice reminder dispatched on: ${announcement.remindedAt}",
            style = MaterialTheme.typography.labelSmall,
            color = DeepBlue,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = announcement.title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Published by ${announcement.authorName} (${announcement.authorRole}) • ${announcement.publishedAt}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (announcement.postedOnBehalfOfLecturer != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DeepBlue.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.School, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Authorized Notice: Posted by Class Rep on behalf of ${announcement.postedOnBehalfOfLecturer}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DeepBlue
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = announcement.body,
          style = MaterialTheme.typography.bodyLarge,
          lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )

        // Attachment Card with Built-in Document Preview
        if (announcement.attachment != null) {
          Spacer(modifier = Modifier.height(16.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Attachment,
                  contentDescription = "Attachment",
                  tint = DeepBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = announcement.attachment.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${announcement.attachment.fileSize} • Tap to view document",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Button(
                onClick = { onPreviewAttachment(announcement.attachment) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                modifier = Modifier.testTag("btn_view_attached_doc")
              ) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Preview", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Doc")
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Read confirmation stats (Supabase v2 announcement_reads)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFDCFCE7))
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Read Confirmation (v2 RPC):",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = SuccessGreen
            )
            Text(
              text = "${announcement.readCount} of ${announcement.totalStudents} enrolled students read",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = SuccessGreen
            )
          }

          if (canModerate && !announcement.isRetracted && onRemindUnreadClick != null && (announcement.totalStudents - announcement.readCount) > 0) {
            Button(
              onClick = onRemindUnreadClick,
              colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
              modifier = Modifier.testTag("btn_remind_unread")
            ) {
              Icon(Icons.Default.Send, contentDescription = "Remind", modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Remind Unread (${announcement.totalStudents - announcement.readCount})", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        // Admin & Uploader Management Row
        if (canModerate) {
          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Admin Approval Toggles & Retract Button
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              if (currentUserRole == Role.ADMIN) {
                if (announcement.approvalStatus != ContentApprovalStatus.APPROVED) {
                  Button(
                    onClick = { onToggleApproval(ContentApprovalStatus.APPROVED) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("btn_approve_announcement")
                  ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Approve", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", style = MaterialTheme.typography.labelSmall)
                  }
                }

                if (announcement.approvalStatus != ContentApprovalStatus.DISAPPROVED) {
                  OutlinedButton(
                    onClick = { onToggleApproval(ContentApprovalStatus.DISAPPROVED) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    modifier = Modifier.testTag("btn_disapprove_announcement")
                  ) {
                    Icon(Icons.Default.ThumbDown, contentDescription = "Disapprove", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disapprove", style = MaterialTheme.typography.labelSmall)
                  }
                }
              }

              // Retract notice button (Supabase v2 retract_announcement RPC)
              if (!announcement.isRetracted && onRetractClick != null) {
                OutlinedButton(
                  onClick = onRetractClick,
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                  modifier = Modifier.testTag("btn_retract_announcement")
                ) {
                  Icon(Icons.Default.EventBusy, contentDescription = "Retract", modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Retract Notice", style = MaterialTheme.typography.labelSmall)
                }
              }
            }

            // Edit & Delete actions for Author or Admin
            Row {
              IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_announcement_btn")) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DeepBlue)
              }
              IconButton(onClick = onDeleteClick, modifier = Modifier.testTag("delete_announcement_btn")) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
              }
            }
          }
        }
  }
}

@Composable
fun PublishAnnouncementDialog(
  onDismiss: () -> Unit,
  onPublish: (title: String, body: String, audienceType: AudienceType, audienceName: String, priority: AnnouncementPriority, attachment: AttachmentFile?, postedOnBehalfOfLecturer: String?) -> Unit,
  onPreviewAttachment: (AttachmentFile) -> Unit,
  roleLimits: RoleLimits? = null,
  currentUserRole: Role = Role.STUDENT,
  availableLecturers: List<com.example.data.model.UserProfile> = emptyList()
) {
  var title by remember { mutableStateOf("") }
  var body by remember { mutableStateOf("") }
  var selectedAudience by remember { mutableStateOf(AudienceType.COURSE) }
  var audienceName by remember { mutableStateOf("CS210 - Algorithms") }
  var isUrgent by remember { mutableStateOf(false) }
  var selectedAttachment by remember { mutableStateOf<AttachmentFile?>(null) }
  val defaultLec = availableLecturers.firstOrNull()?.fullName ?: "Dr. Joseph Mushi"
  var selectedLecturer by remember { mutableStateOf(defaultLec) }

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("publish_announcement_modal")
  ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (currentUserRole == Role.CLASS_REP) "Publish Notice (on behalf of Lecturer)" else "Publish Official Notice",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        if (roleLimits != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DeepBlue.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Daily Quota (v2 Limits):",
                style = MaterialTheme.typography.labelSmall,
                color = DeepBlue,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${roleLimits.announcementsLeft}/${roleLimits.announcementsLimit} notices • ${roleLimits.urgentLeft}/${roleLimits.urgentLimit} urgent",
                style = MaterialTheme.typography.labelSmall,
                color = DeepBlue,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Class Representative (CR) Mandatory Delegation Picker
        if (currentUserRole == Role.CLASS_REP) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = WarmAmberLight,
            modifier = Modifier.fillMaxWidth().testTag("cr_on_behalf_picker_card")
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Responsible Lecturer (CR Requirement):",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFB45309)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Class reps post strictly on behalf of the faculty lecturer who sent the work/notice.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF78350F)
              )
              Spacer(modifier = Modifier.height(8.dp))

              val lecturerOptions = if (availableLecturers.isNotEmpty()) {
                availableLecturers.map { it.fullName }
              } else {
                listOf("Dr. Joseph Mushi", "Prof. Grace Temu", "Dr. Nehemia Moshi", "Eng. Daniel Kweka")
              }

              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(lecturerOptions) { lecName ->
                  FilterChip(
                    selected = selectedLecturer == lecName,
                    onClick = { selectedLecturer = lecName },
                    label = { Text(lecName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = DeepBlue,
                      selectedLabelColor = Color.White
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = selectedLecturer,
                onValueChange = { selectedLecturer = it },
                label = { Text("Appointed Lecturer Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("cr_lecturer_input")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Select Audience:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AudienceChip(
            label = "CS210 Class",
            isSelected = selectedAudience == AudienceType.COURSE,
            onClick = {
              selectedAudience = AudienceType.COURSE
              audienceName = "CS210 - Algorithms"
            }
          )
          AudienceChip(
            label = "BSc CS Dept",
            isSelected = selectedAudience == AudienceType.PROGRAMME,
            onClick = {
              selectedAudience = AudienceType.PROGRAMME
              audienceName = "BSc Computer Science"
            }
          )
          AudienceChip(
            label = "Campus-wide",
            isSelected = selectedAudience == AudienceType.CAMPUS,
            onClick = {
              selectedAudience = AudienceType.CAMPUS
              audienceName = "All Campus Students"
            }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Announcement Title") },
          placeholder = { Text("e.g. Midterm Test Rescheduled") },
          modifier = Modifier.fillMaxWidth().testTag("announcement_title_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = body,
          onValueChange = { body = it },
          label = { Text("Message Body") },
          placeholder = { Text("Type official announcement message...") },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .testTag("announcement_body_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Integrated File Picker for Announcement Attachment
        Text(
          text = "Attachment (Optional):",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        CampusFilePicker(
          selectedFile = selectedAttachment,
          onFileSelected = { selectedAttachment = it },
          onClearFile = { selectedAttachment = null },
          onPreviewFile = onPreviewAttachment,
          title = "Select Circular or Circular Scan",
          allowedTypesDescription = "PDF, JPG, PNG attachments supported"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { isUrgent = !isUrgent }
            .padding(vertical = 4.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isUrgent) DangerRed else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUrgent) DangerRed else MaterialTheme.colorScheme.outline)
          ) {}
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Mark as URGENT notice", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val canSubmit = title.isNotBlank() && body.isNotBlank() && (currentUserRole != Role.CLASS_REP || selectedLecturer.isNotBlank())

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (canSubmit) {
                val priority = if (isUrgent) AnnouncementPriority.URGENT else AnnouncementPriority.NORMAL
                val onBehalf = if (currentUserRole == Role.CLASS_REP) selectedLecturer.trim() else null
                onPublish(title, body, selectedAudience, audienceName, priority, selectedAttachment, onBehalf)
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            enabled = canSubmit,
            modifier = Modifier.testTag("submit_publish_btn")
          ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Publish", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Publish Now")
          }
        }
  }
}

@Composable
fun EditAnnouncementDialog(
  announcement: Announcement,
  onDismiss: () -> Unit,
  onSave: (newTitle: String, newBody: String, newPriority: AnnouncementPriority) -> Unit
) {
  var title by remember { mutableStateOf(announcement.title) }
  var body by remember { mutableStateOf(announcement.body) }
  var isUrgent by remember { mutableStateOf(announcement.priority == AnnouncementPriority.URGENT) }

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("edit_announcement_modal")
  ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Edit Announcement",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = body,
          onValueChange = { body = it },
          label = { Text("Message Body") },
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (title.isNotBlank() && body.isNotBlank()) {
                val priority = if (isUrgent) AnnouncementPriority.URGENT else AnnouncementPriority.NORMAL
                onSave(title, body, priority)
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
          ) {
            Text("Save Changes")
          }
        }
  }
}

@Composable
fun AudienceChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) DeepBlue else MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
  }
}

@Composable
fun ChangeVenueDialog(
  session: ClassSession,
  onDismiss: () -> Unit,
  onConfirm: (newVenue: String, reason: String) -> Unit
) {
  var newVenue by remember { mutableStateOf("") }
  var reason by remember { mutableStateOf("") }

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("change_venue_modal")
  ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Relocate Lecture Venue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "${session.courseCode}: ${session.courseTitle}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = DeepBlue
        )
        Text(
          text = "Current Venue: ${session.venue}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = newVenue,
          onValueChange = { newVenue = it },
          label = { Text("New Venue / Hall") },
          placeholder = { Text("e.g. Computer Lab 4") },
          modifier = Modifier.fillMaxWidth().testTag("new_venue_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Reason for Change") },
          placeholder = { Text("e.g. Projector maintenance in LTB") },
          modifier = Modifier.fillMaxWidth().testTag("venue_reason_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (newVenue.isNotBlank()) {
                onConfirm(newVenue, reason.ifBlank { "Relocated by lecturer" })
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = WarmAmber),
            enabled = newVenue.isNotBlank(),
            modifier = Modifier.testTag("confirm_venue_change_btn")
          ) {
            Icon(imageVector = Icons.Default.EditLocation, contentDescription = "Update", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Update & Notify Class", color = Color.White)
          }
        }
  }
}

@Composable
fun CancelSessionDialog(
  session: ClassSession,
  onDismiss: () -> Unit,
  onConfirm: (reason: String) -> Unit
) {
  var reason by remember { mutableStateOf("") }

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("cancel_session_modal")
  ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Cancel Lecture Session",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DangerRed
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Are you sure you want to cancel ${session.courseCode}?",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Enrolled students will receive a push notification immediately.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Reason for Cancellation") },
          placeholder = { Text("e.g. Faculty meeting, makeup on Saturday") },
          modifier = Modifier.fillMaxWidth().testTag("cancel_reason_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Keep Scheduled")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onConfirm(reason.ifBlank { "Class cancelled by lecturer" })
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            modifier = Modifier.testTag("confirm_cancel_session_btn")
          ) {
            Icon(imageVector = Icons.Default.EventBusy, contentDescription = "Cancel Class", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Confirm Cancellation")
          }
        }
  }
}

@Composable
fun UploadMaterialDialog(
  onDismiss: () -> Unit,
  onUpload: (title: String, courseCode: String, courseTitle: String, description: String, attachment: AttachmentFile, category: String, tags: List<String>) -> Unit,
  onPreviewAttachment: (AttachmentFile) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var courseCode by remember { mutableStateOf("CS210") }
  var description by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Notes") }
  val selectedTags = remember { mutableStateListOf("MIDTERM", "LECTURE") }
  var customTagInput by remember { mutableStateOf("") }
  var selectedFile by remember { mutableStateOf<AttachmentFile?>(null) }

  val availableCategories = listOf("Notes", "Syllabus", "Assignments", "Exam Revision", "Reference", "Lab Manual")
  val sampleQuickTags = listOf("MIDTERM", "FINAL EXAM", "FORMULAS", "LECTURE", "LAB", "ASSIGNMENT", "WEEK 1-4")

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("upload_material_modal")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Upload Course Material",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      IconButton(onClick = onDismiss) {
        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = courseCode,
      onValueChange = { courseCode = it },
      label = { Text("Course Code") },
      modifier = Modifier.fillMaxWidth().testTag("material_course_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = title,
      onValueChange = { title = it },
      label = { Text("Document / Handout Title") },
      placeholder = { Text("e.g. Chapter 4: Dynamic Programming Notes") },
      modifier = Modifier.fillMaxWidth().testTag("material_title_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Document Category Selector
    Text(
      text = "Document Category / Type:",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(availableCategories) { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { selectedCategory = cat },
          label = { Text(cat) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DeepBlue,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("category_chip_$cat")
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Tags & Keywords Selector
    Text(
      text = "Tags & Organization Keywords:",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(sampleQuickTags) { tag ->
        val isSelected = selectedTags.contains(tag)
        FilterChip(
          selected = isSelected,
          onClick = {
            if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
          },
          label = { Text("#$tag") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          modifier = Modifier.testTag("tag_chip_$tag")
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = customTagInput,
      onValueChange = { customTagInput = it },
      label = { Text("Custom tags (comma separated)") },
      placeholder = { Text("e.g. Algorithms, Graph Theory, Formulas") },
      modifier = Modifier.fillMaxWidth().testTag("material_tags_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("Short Description") },
      placeholder = { Text("Includes pseudocode and practice examples") },
      modifier = Modifier.fillMaxWidth().testTag("material_desc_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Integrated File Picker
    Text(
      text = "Select Document File (PDF or Image):",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    CampusFilePicker(
      selectedFile = selectedFile,
      onFileSelected = { selectedFile = it },
      onClearFile = { selectedFile = null },
      onPreviewFile = onPreviewAttachment,
      title = "Select Lecture Slide or Notes",
      allowedTypesDescription = "PDF, JPG, PNG files (Max 25 MB)"
    )

    Spacer(modifier = Modifier.height(18.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = {
          val file = selectedFile ?: AttachmentFile(
            fileName = "${title.ifBlank { "Notes" }}.pdf",
            fileSize = "2.1 MB",
            mimeType = "application/pdf",
            previewText = "LECTURE NOTES FOR $courseCode\nCategory: $selectedCategory\nTitle: $title\n\n$description"
          )
          if (title.isNotBlank() && courseCode.isNotBlank()) {
            val courseTitle = if (courseCode.contains("CS210")) "Data Structures & Algorithms" else "Computer Science"
            val allTags = (selectedTags + customTagInput.split(",").map { it.trim().uppercase() }.filter { it.isNotBlank() }).distinct()
            onUpload(title, courseCode, courseTitle, description, file, selectedCategory, allTags)
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
        enabled = title.isNotBlank() && courseCode.isNotBlank(),
        modifier = Modifier.testTag("submit_upload_btn")
      ) {
        Icon(imageVector = Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save & Publish")
      }
    }
  }
}

@Composable
fun AddDeadlineDialog(
  onDismiss: () -> Unit,
  onAdd: (courseCode: String, courseTitle: String, title: String, instructions: String, dueDateTime: String, acceptedFormats: String) -> Unit
) {
  var courseCode by remember { mutableStateOf("CS210") }
  var title by remember { mutableStateOf("") }
  var instructions by remember { mutableStateOf("") }
  var dueDateTime by remember { mutableStateOf("Friday, 24 Oct 2026 • 23:59 EAT") }
  var acceptedFormats by remember { mutableStateOf("PDF or Images (Max 15 MB)") }

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_deadline_modal")
  ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Create Academic Deadline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = courseCode,
          onValueChange = { courseCode = it },
          label = { Text("Course Code") },
          modifier = Modifier.fillMaxWidth().testTag("deadline_course_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Task / Assignment Title") },
          placeholder = { Text("e.g. Programming Assignment 3") },
          modifier = Modifier.fillMaxWidth().testTag("deadline_title_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = dueDateTime,
          onValueChange = { dueDateTime = it },
          label = { Text("Due Date & Time (EAT)") },
          modifier = Modifier.fillMaxWidth().testTag("deadline_due_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = acceptedFormats,
          onValueChange = { acceptedFormats = it },
          label = { Text("Accepted Formats") },
          placeholder = { Text("e.g. PDF (Max 15 MB)") },
          modifier = Modifier.fillMaxWidth().testTag("deadline_formats_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = instructions,
          onValueChange = { instructions = it },
          label = { Text("Submission Instructions") },
          placeholder = { Text("Submit zip or PDF report with test cases") },
          modifier = Modifier.fillMaxWidth().testTag("deadline_instructions_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (title.isNotBlank()) {
                val courseTitle = if (courseCode.contains("CS210")) "Data Structures & Algorithms" else "Academic Module"
                onAdd(courseCode, courseTitle, title, instructions, dueDateTime, acceptedFormats)
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            enabled = title.isNotBlank(),
            modifier = Modifier.testTag("submit_deadline_btn")
          ) {
            Text("Create Deadline")
          }
        }
  }
}
