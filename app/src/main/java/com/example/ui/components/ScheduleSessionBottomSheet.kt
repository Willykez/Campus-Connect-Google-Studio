package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.model.CampusCourse
import com.example.data.model.CampusVenue
import com.example.data.model.ClassSession
import com.example.data.model.ContentApprovalStatus
import com.example.data.model.SessionKind
import com.example.data.model.SessionStatus
import com.example.data.model.UserProfile
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen
import java.util.UUID

/**
 * Clean Material 3 Modal Bottom Sheet for scheduling a new timetable session.
 * Allows lecturers or class leaders to pick from campus subject availability,
 * room/venue availability, time slots, and responsible lecturers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSessionBottomSheet(
  onDismiss: () -> Unit,
  availableCourses: List<CampusCourse>,
  availableVenues: List<CampusVenue>,
  facultyMembers: List<UserProfile>,
  currentUser: UserProfile,
  onScheduleSession: (ClassSession) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  BackHandler { onDismiss() }

  val defaultCourses = if (availableCourses.isNotEmpty()) availableCourses else listOf(
    CampusCourse("CS210", "Data Structures & Algorithms", "Dr. Joseph Mushi"),
    CampusCourse("CS220", "Database Management Systems", "Dr. Joseph Mushi"),
    CampusCourse("IS101", "Systems Analysis & Design", "Faculty Staff")
  )

  val defaultVenues = if (availableVenues.isNotEmpty()) availableVenues else listOf(
    CampusVenue("LTB Room 4", 120, "Main Lecture Complex"),
    CampusVenue("Lab 3", 60, "Science & Computing Centre"),
    CampusVenue("Lecture Theatre A", 200, "Main Auditorium"),
    CampusVenue("Room 104", 45, "Academic Wing B")
  )

  var selectedCourse by remember { mutableStateOf(defaultCourses.first()) }
  var isCourseDropdownOpen by remember { mutableStateOf(false) }

  var selectedVenue by remember { mutableStateOf(defaultVenues.first()) }
  var isVenueDropdownOpen by remember { mutableStateOf(false) }

  var selectedLecturer by remember {
    mutableStateOf(
      facultyMembers.firstOrNull { it.id == currentUser.id }?.fullName
        ?: facultyMembers.firstOrNull()?.fullName
        ?: currentUser.fullName
    )
  }
  var isLecturerDropdownOpen by remember { mutableStateOf(false) }

  var selectedDay by remember { mutableIntStateOf(1) } // 1=Mon ... 6=Sat
  var selectedKind by remember { mutableStateOf(SessionKind.LECTURE) }

  val timeSlots = listOf(
    "08:00" to "10:00",
    "10:00" to "12:00",
    "13:00" to "15:00",
    "15:00" to "17:00",
    "17:00" to "19:00"
  )
  var selectedTimeSlotIndex by remember { mutableIntStateOf(0) }

  val dayLabels = listOf(
    1 to "Mon",
    2 to "Tue",
    3 to "Wed",
    4 to "Thu",
    5 to "Fri",
    6 to "Sat"
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("schedule_session_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Schedule Class Session",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Pick subject, room, time and faculty lecturer",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close Sheet")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
      Spacer(modifier = Modifier.height(14.dp))

      // 1. Subject / Course Availability Picker
      Text(
        text = "1. Subject / Course Offering *",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = DeepBlue
      )
      Spacer(modifier = Modifier.height(6.dp))

      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = "${selectedCourse.code}: ${selectedCourse.title}",
          onValueChange = {},
          readOnly = true,
          trailingIcon = {
            IconButton(onClick = { isCourseDropdownOpen = true }) {
              Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Course")
            }
          },
          leadingIcon = {
            Icon(Icons.Default.School, contentDescription = null, tint = DeepBlue)
          },
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isCourseDropdownOpen = true }
            .testTag("picker_session_course")
        )

        DropdownMenu(
          expanded = isCourseDropdownOpen,
          onDismissRequest = { isCourseDropdownOpen = false },
          modifier = Modifier.fillMaxWidth(0.9f)
        ) {
          defaultCourses.forEach { course ->
            DropdownMenuItem(
              text = {
                Column {
                  Text(
                    text = "${course.code} • ${course.title}",
                    fontWeight = if (course == selectedCourse) FontWeight.Bold else FontWeight.Normal,
                    color = if (course == selectedCourse) DeepBlue else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Instructor: ${course.lecturerName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              },
              onClick = {
                selectedCourse = course
                selectedLecturer = course.lecturerName
                isCourseDropdownOpen = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Room / Venue Availability Picker
      Text(
        text = "2. Venue / Room Availability *",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = DeepBlue
      )
      Spacer(modifier = Modifier.height(6.dp))

      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = "${selectedVenue.name} (Cap ${selectedVenue.capacity} • ${selectedVenue.building})",
          onValueChange = {},
          readOnly = true,
          trailingIcon = {
            IconButton(onClick = { isVenueDropdownOpen = true }) {
              Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Venue")
            }
          },
          leadingIcon = {
            Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = DeepBlue)
          },
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isVenueDropdownOpen = true }
            .testTag("picker_session_venue")
        )

        DropdownMenu(
          expanded = isVenueDropdownOpen,
          onDismissRequest = { isVenueDropdownOpen = false },
          modifier = Modifier.fillMaxWidth(0.9f)
        ) {
          defaultVenues.forEach { venue ->
            DropdownMenuItem(
              text = {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = venue.name,
                      fontWeight = if (venue == selectedVenue) FontWeight.Bold else FontWeight.Normal,
                      color = if (venue == selectedVenue) DeepBlue else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${venue.building} • Projector: ${if (venue.hasProjector) "Yes" else "No"}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.padding(start = 8.dp)
                  ) {
                    Text(
                      text = "Cap: ${venue.capacity}",
                      style = MaterialTheme.typography.labelSmall,
                      color = SuccessGreen,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              },
              onClick = {
                selectedVenue = venue
                isVenueDropdownOpen = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Responsible Lecturer Picker
      Text(
        text = "3. Responsible Lecturer / Instructor *",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))

      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = selectedLecturer,
          onValueChange = {},
          readOnly = true,
          trailingIcon = {
            IconButton(onClick = { isLecturerDropdownOpen = true }) {
              Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Lecturer")
            }
          },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = DeepBlue)
          },
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isLecturerDropdownOpen = true }
            .testTag("picker_session_lecturer")
        )

        DropdownMenu(
          expanded = isLecturerDropdownOpen,
          onDismissRequest = { isLecturerDropdownOpen = false },
          modifier = Modifier.fillMaxWidth(0.9f)
        ) {
          val lecturers = (facultyMembers.filter { it.role == com.example.data.model.Role.LECTURER } + listOf(currentUser)).distinctBy { it.fullName }
          lecturers.forEach { lec ->
            DropdownMenuItem(
              text = {
                Text(
                  text = lec.fullName,
                  fontWeight = if (lec.fullName == selectedLecturer) FontWeight.Bold else FontWeight.Normal,
                  color = if (lec.fullName == selectedLecturer) DeepBlue else MaterialTheme.colorScheme.onSurface
                )
              },
              onClick = {
                selectedLecturer = lec.fullName
                isLecturerDropdownOpen = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Session Type (Lecture / Lab / Tutorial)
      Text(
        text = "4. Session Kind *",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SessionKind.values().forEach { kind ->
          FilterChip(
            selected = selectedKind == kind,
            onClick = { selectedKind = kind },
            label = { Text(kind.name) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DeepBlue.copy(alpha = 0.15f),
              selectedLabelColor = DeepBlue
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Day of Week
      Text(
        text = "5. Day of Week *",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        dayLabels.forEach { (num, label) ->
          FilterChip(
            selected = selectedDay == num,
            onClick = { selectedDay = num },
            label = { Text(label, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DeepBlue,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 6. Time Slot
      Text(
        text = "6. Time Slot (East Africa Time) *",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        timeSlots.forEachIndexed { index, (start, end) ->
          FilterChip(
            selected = selectedTimeSlotIndex == index,
            onClick = { selectedTimeSlotIndex = index },
            label = { Text("$start - $end", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DeepBlue.copy(alpha = 0.15f),
              selectedLabelColor = DeepBlue
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f)
        ) {
          Text("Cancel")
        }

        Button(
          onClick = {
            val slot = timeSlots[selectedTimeSlotIndex]
            val newSession = ClassSession(
              id = "sess-" + UUID.randomUUID().toString().take(6),
              courseCode = selectedCourse.code,
              courseTitle = selectedCourse.title,
              lecturerId = currentUser.id,
              lecturerName = selectedLecturer,
              venue = selectedVenue.name,
              kind = selectedKind,
              dayOfWeek = selectedDay,
              startTime = slot.first,
              endTime = slot.second,
              status = SessionStatus.SCHEDULED,
              programme = currentUser.programme ?: "BSc Computer Science",
              cohort = "Semester 1",
              approvalStatus = ContentApprovalStatus.APPROVED
            )
            onScheduleSession(newSession)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
          modifier = Modifier
            .weight(1f)
            .testTag("submit_schedule_session_btn")
        ) {
          Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save & Add to Timetable")
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
