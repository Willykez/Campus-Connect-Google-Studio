@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CampusEntity
import com.example.data.model.Role
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue

val DEFAULT_UNIVERSITIES = listOf(
  "Catholic University of Mbeya (CUoM)",
  "University of Dar es Salaam (UDSM)",
  "Institute of Finance Management (IFM)",
  "College of Business Education (CBE)",
  "Other Institution"
)

/**
 * Universal Multi-Campus Sign-Up Dialog.
 * Prioritizes student registration numbers, supports any university/campus,
 * dynamically populates programme offerings based on the chosen campus,
 * and allows free-format email addresses (Gmail, campus, or personal).
 */
@Composable
fun SignUpDialog(
  onDismiss: () -> Unit,
  campuses: List<CampusEntity> = emptyList(),
  onRegister: (
    fullName: String,
    registrationNumber: String,
    university: String,
    email: String,
    role: Role,
    programme: String
  ) -> Unit
) {
  BackHandler { onDismiss() }

  val universitiesList = remember(campuses) {
    if (campuses.isNotEmpty()) {
      campuses.map { it.name } + listOf("Other Institution")
    } else {
      DEFAULT_UNIVERSITIES
    }
  }

  var selectedUniversity by remember { mutableStateOf(universitiesList.firstOrNull() ?: DEFAULT_UNIVERSITIES[0]) }
  var isUniDropdownExpanded by remember { mutableStateOf(false) }

  // Dynamic courses and programmes based on selected campus
  val availableProgrammes = remember(selectedUniversity, campuses) {
    val campus = campuses.firstOrNull { it.name.equals(selectedUniversity, ignoreCase = true) }
    if (campus != null && campus.programmes.isNotEmpty()) {
      campus.programmes.map { "${it.code} - ${it.name}" } + listOf("Other / Custom Programme")
    } else when {
      selectedUniversity.contains("CUoM", ignoreCase = true) -> listOf(
        "BCS - BSc Computer Science",
        "BHRM - Bachelor of Human Resource Management",
        "BIS - BSc Information Systems",
        "BBA - Bachelor of Business Administration",
        "LLB - Bachelor of Laws",
        "Other / Custom Programme"
      )
      selectedUniversity.contains("UDSM", ignoreCase = true) -> listOf(
        "CS - BSc Computer Science",
        "TE - BSc Telecommunications Engineering",
        "EC - BA Economics",
        "MD - Doctor of Medicine",
        "Other / Custom Programme"
      )
      selectedUniversity.contains("IFM", ignoreCase = true) -> listOf(
        "BIT - BSc Information Technology",
        "CSF - BSc Computer Security & Forensics",
        "ACT - BSc Actuarial Science",
        "BF - BBA Banking & Finance",
        "Other / Custom Programme"
      )
      else -> listOf(
        "BSc Computer Science",
        "Bachelor of Business Administration",
        "General Academic Studies",
        "Other / Custom Programme"
      )
    }
  }

  var registrationNumber by remember { mutableStateOf("") }
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(Role.STUDENT) }
  var selectedProgramme by remember { mutableStateOf(availableProgrammes.firstOrNull() ?: "BSc Computer Science") }
  var customProgramme by remember { mutableStateOf("") }
  var isProgrammeDropdownExpanded by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Update selected programme when university changes
  LaunchedEffect(availableProgrammes) {
    if (selectedProgramme !in availableProgrammes && selectedProgramme != "Other / Custom Programme") {
      selectedProgramme = availableProgrammes.firstOrNull() ?: "General Academic Studies"
    }
  }

  AppBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("signup_registration_modal")
  ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeepBlue.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = DeepBlue,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Universal Campus Sign-Up",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Student Registration First • All Campuses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_signup_dialog_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(14.dp))

        // 1. SELECT UNIVERSITY FIRST (Drives course population)
        Text(
          text = "1. Select University / Campus *",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = DeepBlue
        )
        Text(
          text = "Choose your institution. Available courses will adapt automatically.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = selectedUniversity,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
              IconButton(onClick = { isUniDropdownExpanded = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select University")
              }
            },
            leadingIcon = {
              Icon(Icons.Default.School, contentDescription = null, tint = DeepBlue)
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { isUniDropdownExpanded = true }
              .testTag("input_university_selector")
          )

          DropdownMenu(
            expanded = isUniDropdownExpanded,
            onDismissRequest = { isUniDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
          ) {
            universitiesList.forEach { uni ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = uni,
                    fontWeight = if (uni == selectedUniversity) FontWeight.Bold else FontWeight.Normal,
                    color = if (uni == selectedUniversity) DeepBlue else MaterialTheme.colorScheme.onSurface
                  )
                },
                onClick = {
                  selectedUniversity = uni
                  isUniDropdownExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Student / Staff Registration Number
        Text(
          text = "Registration ID *",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = DeepBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = registrationNumber,
          onValueChange = {
            registrationNumber = it
            errorMessage = null
          },
          placeholder = { Text("e.g. CUOM/BCS/2026/001") },
          leadingIcon = {
            Icon(Icons.Default.Badge, contentDescription = null, tint = DeepBlue)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_registration_number")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Full Name
        Text(
          text = "Full Name *",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = fullName,
          onValueChange = {
            fullName = it
            errorMessage = null
          },
          placeholder = { Text("e.g. Asha Mwangi") },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = DeepBlue)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_full_name")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Email Address
        Text(
          text = "Email Address *",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = email,
          onValueChange = {
            email = it
            errorMessage = null
          },
          placeholder = { Text("e.g. student@gmail.com") },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = DeepBlue)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_email")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Account Role
        Text(
          text = "Account Role *",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(Role.STUDENT, Role.CLASS_REP, Role.LECTURER, Role.ADMIN).forEach { role ->
            FilterChip(
              selected = selectedRole == role,
              onClick = { selectedRole = role },
              label = {
                Text(
                  text = when (role) {
                    Role.STUDENT -> "Student"
                    Role.CLASS_REP -> "Class Rep"
                    Role.LECTURER -> "Lecturer"
                    Role.ADMIN -> "Campus Admin"
                    Role.SUPER_ADMIN -> "App Manager"
                  },
                  fontWeight = FontWeight.Medium
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DeepBlue.copy(alpha = 0.15f),
                selectedLabelColor = DeepBlue
              ),
              modifier = Modifier.testTag("chip_role_${role.name.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Programme / Department
        Text(
          text = "Course / Programme *",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = DeepBlue
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = selectedProgramme,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
              IconButton(onClick = { isProgrammeDropdownExpanded = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Course")
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { isProgrammeDropdownExpanded = true }
              .testTag("input_programme")
          )

          DropdownMenu(
            expanded = isProgrammeDropdownExpanded,
            onDismissRequest = { isProgrammeDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
          ) {
            availableProgrammes.forEach { prog ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = prog,
                    fontWeight = if (prog == selectedProgramme) FontWeight.Bold else FontWeight.Normal,
                    color = if (prog == selectedProgramme) DeepBlue else MaterialTheme.colorScheme.onSurface
                  )
                },
                onClick = {
                  selectedProgramme = prog
                  isProgrammeDropdownExpanded = false
                }
              )
            }
          }
        }

        if (selectedProgramme == "Other / Custom Programme") {
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = customProgramme,
            onValueChange = { customProgramme = it },
            placeholder = { Text("Programme title") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_custom_programme")
          )
        }

        // Error Feedback
        errorMessage?.let { err ->
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = err,
            style = MaterialTheme.typography.bodySmall,
            color = DangerRed,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("btn_cancel_signup")
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              val reg = registrationNumber.trim()
              val name = fullName.trim()
              val mail = email.trim()
              val finalProg = if (selectedProgramme == "Other / Custom Programme") {
                customProgramme.trim().ifBlank { "General Academic Studies" }
              } else {
                selectedProgramme
              }

              if (reg.isBlank()) {
                errorMessage = "Please enter your Student / Staff Registration Number."
                return@Button
              }
              if (name.isBlank()) {
                errorMessage = "Please enter your full name."
                return@Button
              }
              if (!mail.contains("@") || !mail.contains(".") || mail.length < 5 || mail.contains(" ")) {
                errorMessage = "Please enter a valid email address (e.g. user@gmail.com or campus email)."
                return@Button
              }

              onRegister(name, reg, selectedUniversity, mail, selectedRole, finalProg)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            modifier = Modifier
              .weight(1f)
              .testTag("btn_submit_signup")
          ) {
            Text("Register Account")
          }
        }
  }
}
