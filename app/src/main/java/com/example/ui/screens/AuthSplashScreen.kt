package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Role
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.DeepBlueDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.CampusViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSplashScreen(
  viewModel: CampusViewModel,
  onAuthenticated: () -> Unit = {}
) {
  var isSplashVisible by remember { mutableStateOf(true) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register

  // Sign In Form States
  var signInIdentifier by remember { mutableStateOf("") }
  var signInPassword by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var keepSessionChecked by remember { mutableStateOf(true) }
  var signInErrorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  // Registration Form States
  var regFullName by remember { mutableStateOf("") }
  var regNumber by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regUniversity by remember { mutableStateOf("Catholic University of Mbeya (CUoM)") }
  var regRole by remember { mutableStateOf(Role.STUDENT) }
  var regProgramme by remember { mutableStateOf("BSc Computer Science") }
  var regErrorMessage by remember { mutableStateOf<String?>(null) }
  var isUniMenuExpanded by remember { mutableStateOf(false) }
  var isRoleMenuExpanded by remember { mutableStateOf(false) }

  val campuses by viewModel.campuses.collectAsState()
  val availableUniversities = remember(campuses) {
    (campuses.map { it.name } + listOf(
      "Catholic University of Mbeya (CUoM)",
      "University of Dar es Salaam (UDSM)",
      "Institute of Finance Management (IFM)"
    )).distinct()
  }

  // Smooth brief branded splash delay on cold start (800ms)
  LaunchedEffect(Unit) {
    delay(800)
    isSplashVisible = false
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            DeepBlueDark,
            DeepBlue,
            MaterialTheme.colorScheme.background
          )
        )
      )
  ) {
    if (isSplashVisible) {
      // Branded Splash Presentation
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.School,
            contentDescription = "CampusConnect Logo",
            tint = DeepBlue,
            modifier = Modifier.size(52.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "CampusConnect",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Text(
          text = "Unified Multi-Campus Academic Portal",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        CircularProgressIndicator(
          color = Color.White,
          strokeWidth = 3.dp,
          modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Verifying secure session...",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.7f)
        )
      }
    } else {
      // Main Authentication Screen Gate (Sign In / Register)
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Branding
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = null,
              tint = DeepBlue,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "CampusConnect",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Authentication & Access Control",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Auth Card
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_gate_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {

            // Tab Selector: Sign In vs Register
            TabRow(
              selectedTabIndex = selectedTab,
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = DeepBlue,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .testTag("auth_mode_tabs")
            ) {
              Tab(
                selected = selectedTab == 0,
                onClick = {
                  selectedTab = 0
                  signInErrorMessage = null
                },
                text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_sign_in")
              )
              Tab(
                selected = selectedTab == 1,
                onClick = {
                  selectedTab = 1
                  regErrorMessage = null
                },
                text = { Text("Register", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_register")
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
              // ------------------- SIGN IN FORM -------------------
              Text(
                text = "Sign in to your account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Use your Student/Staff ID or registered email address.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(16.dp))

              if (signInErrorMessage != null) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = DangerRed.copy(alpha = 0.12f),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.ErrorOutline,
                      contentDescription = null,
                      tint = DangerRed,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = signInErrorMessage ?: "",
                      style = MaterialTheme.typography.bodySmall,
                      color = DangerRed,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }

              OutlinedTextField(
                value = signInIdentifier,
                onValueChange = {
                  signInIdentifier = it
                  signInErrorMessage = null
                },
                label = { Text("Email or Student / Staff ID") },
                placeholder = { Text("e.g. eliamwenda01@gmail.com") },
                leadingIcon = {
                  Icon(Icons.Default.Person, contentDescription = null, tint = DeepBlue)
                },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_signin_identifier")
              )

              Spacer(modifier = Modifier.height(12.dp))

              OutlinedTextField(
                value = signInPassword,
                onValueChange = {
                  signInPassword = it
                  signInErrorMessage = null
                },
                label = { Text("Password") },
                placeholder = { Text("Enter account password") },
                leadingIcon = {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = DeepBlue)
                },
                trailingIcon = {
                  IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                      imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                      contentDescription = "Toggle password visibility"
                    )
                  }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                  if (signInIdentifier.isNotBlank()) {
                    isSubmitting = true
                    viewModel.setKeepSessionActive(keepSessionChecked)
                    val success = viewModel.login(signInIdentifier, signInPassword)
                    isSubmitting = false
                    if (success) {
                      onAuthenticated()
                    } else {
                      signInErrorMessage = "User account not found. Please register or verify credentials."
                    }
                  }
                }),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_signin_password")
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Keep Session Active Toggle (No Auto-Logout)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { keepSessionChecked = !keepSessionChecked }
                  .padding(vertical = 4.dp)
              ) {
                Checkbox(
                  checked = keepSessionChecked,
                  onCheckedChange = { keepSessionChecked = it },
                  colors = CheckboxDefaults.colors(checkedColor = DeepBlue),
                  modifier = Modifier.testTag("checkbox_keep_session")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = "Stay Signed In (No Auto-Logout)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Keep active session across app restarts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (signInIdentifier.isBlank()) {
                    signInErrorMessage = "Please enter your email or Registration ID."
                    return@Button
                  }
                  isSubmitting = true
                  viewModel.setKeepSessionActive(keepSessionChecked)
                  val success = viewModel.login(signInIdentifier, signInPassword)
                  isSubmitting = false
                  if (success) {
                    onAuthenticated()
                  } else {
                    signInErrorMessage = "Account not found for '$signInIdentifier'. Check details or register."
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("btn_submit_signin")
              ) {
                if (isSubmitting) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Sign In to Campus Portal", fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(20.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
              Spacer(modifier = Modifier.height(16.dp))

              // App Owner Master Key Direct Access
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF6B21A8).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B21A8).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6B21A8)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "App Owner Master Key",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B21A8)
                      )
                      Text(
                        text = "Elia Mwenda • eliamwenda01@gmail.com",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Text(
                    text = "Root governance authority to manage campuses, approve admins, and partition databases.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )

                  Spacer(modifier = Modifier.height(10.dp))

                  Button(
                    onClick = {
                      viewModel.setKeepSessionActive(keepSessionChecked)
                      viewModel.loginAsOwner()
                      onAuthenticated()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("btn_owner_quick_access")
                  ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Access as App Owner (Elia Mwenda)", fontWeight = FontWeight.Bold)
                  }
                }
              }

            } else {
              // ------------------- REGISTRATION FORM -------------------
              Text(
                text = "Register New Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Prioritizes your student or staff registration number.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(14.dp))

              if (regErrorMessage != null) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = DangerRed.copy(alpha = 0.12f),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.ErrorOutline,
                      contentDescription = null,
                      tint = DangerRed,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = regErrorMessage ?: "",
                      style = MaterialTheme.typography.bodySmall,
                      color = DangerRed,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }

              // University Selector
              ExposedDropdownMenuBox(
                expanded = isUniMenuExpanded,
                onExpandedChange = { isUniMenuExpanded = !isUniMenuExpanded }
              ) {
                OutlinedTextField(
                  value = regUniversity,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("University / Campus") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUniMenuExpanded) },
                  colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                  modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("input_reg_university")
                )
                ExposedDropdownMenu(
                  expanded = isUniMenuExpanded,
                  onDismissRequest = { isUniMenuExpanded = false }
                ) {
                  availableUniversities.forEach { uni ->
                    DropdownMenuItem(
                      text = { Text(uni) },
                      onClick = {
                        regUniversity = uni
                        isUniMenuExpanded = false
                      }
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = regNumber,
                onValueChange = {
                  regNumber = it
                  regErrorMessage = null
                },
                label = { Text("Registration / Staff ID") },
                placeholder = { Text("e.g. CUOM/BCS/2026/001") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_reg_number")
              )

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = regFullName,
                onValueChange = {
                  regFullName = it
                  regErrorMessage = null
                },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Asha Mwangi") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_reg_fullname")
              )

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = regEmail,
                onValueChange = {
                  regEmail = it
                  regErrorMessage = null
                },
                label = { Text("Email Address (Gmail / Any Email)") },
                placeholder = { Text("e.g. asha@gmail.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_reg_email")
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Role Selector
              ExposedDropdownMenuBox(
                expanded = isRoleMenuExpanded,
                onExpandedChange = { isRoleMenuExpanded = !isRoleMenuExpanded }
              ) {
                OutlinedTextField(
                  value = when (regRole) {
                    Role.STUDENT -> "Student"
                    Role.CLASS_REP -> "Class Representative"
                    Role.LECTURER -> "Lecturer"
                    Role.ADMIN -> "Campus Administrator"
                    Role.SUPER_ADMIN -> "Super Admin"
                  },
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("Account Role") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoleMenuExpanded) },
                  colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                  modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("input_reg_role")
                )
                ExposedDropdownMenu(
                  expanded = isRoleMenuExpanded,
                  onDismissRequest = { isRoleMenuExpanded = false }
                ) {
                  listOf(
                    Role.STUDENT to "Student",
                    Role.CLASS_REP to "Class Representative",
                    Role.LECTURER to "Lecturer",
                    Role.ADMIN to "Campus Administrator"
                  ).forEach { (r, label) ->
                    DropdownMenuItem(
                      text = { Text(label) },
                      onClick = {
                        regRole = r
                        isRoleMenuExpanded = false
                      }
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = regProgramme,
                onValueChange = { regProgramme = it },
                label = { Text("Programme / Department") },
                placeholder = { Text("e.g. BSc Computer Science") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_reg_programme")
              )

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Create Password") },
                placeholder = { Text("Enter account password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DeepBlue) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_reg_password")
              )

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (regFullName.isBlank() || regNumber.isBlank() || regEmail.isBlank()) {
                    regErrorMessage = "Please complete all required fields (Name, Registration ID, Email)."
                    return@Button
                  }
                  isSubmitting = true
                  viewModel.setKeepSessionActive(keepSessionChecked)
                  viewModel.registerUser(
                    fullName = regFullName.trim(),
                    registrationNumber = regNumber.trim(),
                    university = regUniversity,
                    email = regEmail.trim(),
                    role = regRole,
                    programme = regProgramme.trim().ifBlank { "Faculty Academic Studies" }
                  )
                  isSubmitting = false
                  onAuthenticated()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("btn_submit_registration")
              ) {
                if (isSubmitting) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Register & Enter Portal", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Notice Footer
        Text(
          text = "No unauthorized internal access • All academic records secured with role-based governance.",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.7f),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
