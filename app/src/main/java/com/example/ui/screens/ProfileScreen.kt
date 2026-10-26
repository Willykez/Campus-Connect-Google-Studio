package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.AppLanguage
import com.example.data.model.Role
import com.example.data.model.UserProfile
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberLight
import com.example.viewmodel.AppThemeMode
import com.example.viewmodel.CampusViewModel

@Composable
fun ProfileScreen(
  viewModel: CampusViewModel,
  onOpenSignUpDialog: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val keepSessionActive by viewModel.keepSessionActive.collectAsState()
  val appLanguage by viewModel.appLanguage.collectAsState()
  val isOffline by viewModel.isOffline.collectAsState()
  val is24Hour by viewModel.is24HourFormat.collectAsState()
  val lastSynced by viewModel.lastSynced.collectAsState()
  val themeMode by viewModel.themeMode.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = viewModel.getString("tab_profile"),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Account credentials, sync status & localization",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // User Profile Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("profile_user_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DeepBlue),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = user.avatarInitials,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = user.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                  text = user.role.name,
                  backgroundColor = when (user.role) {
                    Role.STUDENT -> Color(0xFFE0E7FF)
                    Role.CLASS_REP -> WarmAmberLight
                    Role.LECTURER -> WarmAmberLight
                    Role.ADMIN -> Color(0xFFDCFCE7)
                    Role.SUPER_ADMIN -> Color(0xFFF3E8FF)
                  },
                  contentColor = when (user.role) {
                    Role.STUDENT -> DeepBlue
                    Role.CLASS_REP -> Color(0xFFB45309)
                    Role.LECTURER -> Color(0xFFB45309)
                    Role.ADMIN -> SuccessGreen
                    Role.SUPER_ADMIN -> Color(0xFF6B21A8)
                  }
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                  text = if (user.status == AccountStatus.APPROVED) "Verified" else "Pending Review",
                  backgroundColor = if (user.status == AccountStatus.APPROVED) Color(0xFFDCFCE7) else WarmAmberLight,
                  contentColor = if (user.status == AccountStatus.APPROVED) SuccessGreen else Color(0xFFB45309)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(14.dp))

          ProfileInfoRow("University / Campus", user.university)
          ProfileInfoRow("Registration Number (Priority #1)", user.studentNumber ?: user.staffNumber ?: "Not assigned")
          ProfileInfoRow("Programme / Dept", user.programme ?: "General Academic")
          ProfileInfoRow("Cohort / Designation", user.cohort ?: "Year 2")
        }
      }
    }

    // Session & Security Management Card (No Auto-Logout)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("session_management_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SuccessGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = SuccessGreen,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Session & Access Security",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Persistent authentication session",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFDCFCE7)
            ) {
              Text(
                text = "ACTIVE • NO TIMEOUT",
                style = MaterialTheme.typography.labelSmall,
                color = SuccessGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 9.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Your account session is active and preserved on this device. You will stay signed in across app restarts without automatic session expiry.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          // Stay Signed In toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Stay Signed In (No Auto-Logout)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Persist login credentials across app re-launches",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = keepSessionActive,
              onCheckedChange = { viewModel.setKeepSessionActive(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = DeepBlue),
              modifier = Modifier.testTag("toggle_keep_session_switch")
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Sign Out Action
          Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_profile_logout")
          ) {
            Icon(
              imageVector = Icons.Default.ExitToApp,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out / Lock Session", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Universal Multi-Campus Sign-Up Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth().testTag("profile_signup_callout_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Universal Multi-Campus Registration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepBlue
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Register with your student number first. Works with CUoM, UDSM, IFM, or any university, accepting Gmail and personal emails.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = onOpenSignUpDialog,
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_open_universal_signup")
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register New Student / Campus Account", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // App Owner Master Key & Live Governance
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("app_owner_key_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF6B21A8).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.VerifiedUser,
                  contentDescription = "Master Key",
                  tint = Color(0xFF6B21A8),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Master Root Authority",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Key to all users, campuses & approvals",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFF3E8FF)
            ) {
              Text(
                text = "OWNER KEY ACTIVE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B21A8),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 9.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "As the sole Master Owner, this account holds unilateral root access to register university campuses, approve campus administrators, partition cloud databases, and enforce system audit policies.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(14.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { viewModel.setTab(com.example.viewmodel.ScreenTab.ADMIN) },
              colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
              modifier = Modifier.weight(1f).testTag("owner_manage_campuses_btn")
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Campuses & Admins")
            }
            OutlinedButton(
              onClick = onOpenSignUpDialog,
              modifier = Modifier.weight(1f).testTag("owner_register_user_btn")
            ) {
              Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Register User")
            }
          }
        }
      }
    }

    // App Preferences (Language, Time, Offline)
    item {
      Text(
        text = "Preferences & Offline Sync",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Language Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Language, contentDescription = "Language", tint = DeepBlue)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Language / Lugha",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (appLanguage == AppLanguage.ENGLISH) "English (Active)" else "Kiswahili (Inayotumika)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = DeepBlue.copy(alpha = 0.12f),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleLanguage() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("toggle_language_btn")
            ) {
              Text(
                text = if (appLanguage == AppLanguage.ENGLISH) "Switch to Kiswahili" else "Badili kwa Kiingereza",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DeepBlue
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          // 24 Hour Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Time format", tint = DeepBlue)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Timezone & Format",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "East Africa Time (EAT, UTC+3) • 24-Hour",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Switch(
              checked = is24Hour,
              onCheckedChange = { viewModel.toggle24HourFormat() },
              colors = SwitchDefaults.colors(checkedThumbColor = DeepBlue),
              modifier = Modifier.testTag("toggle_24h_switch")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          // Offline Simulation Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.CloudOff, contentDescription = "Offline", tint = WarmAmber)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Simulate Offline Mode",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (isOffline) "Offline active (Loading from Room cache)" else "Online (Connected)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Switch(
              checked = isOffline,
              onCheckedChange = { viewModel.toggleOfflineSimulation() },
              colors = SwitchDefaults.colors(checkedThumbColor = WarmAmber),
              modifier = Modifier.testTag("toggle_offline_switch")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          // App Appearance Theme Options
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = when (themeMode) {
                  AppThemeMode.SYSTEM -> Icons.Default.BrightnessMedium
                  AppThemeMode.LIGHT -> Icons.Default.LightMode
                  AppThemeMode.DARK -> Icons.Default.DarkMode
                },
                contentDescription = "Theme",
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "App Theme / Appearance",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = when (themeMode) {
                    AppThemeMode.SYSTEM -> "System Adaptive (Auto switches with OS)"
                    AppThemeMode.LIGHT -> "Light Theme (Always bright)"
                    AppThemeMode.DARK -> "Dark Theme (Deep navy-slate)"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Option Segmented Selector
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              listOf(
                Triple("System", AppThemeMode.SYSTEM, Icons.Default.BrightnessMedium),
                Triple("Light", AppThemeMode.LIGHT, Icons.Default.LightMode),
                Triple("Dark", AppThemeMode.DARK, Icons.Default.DarkMode)
              ).forEach { (label, mode, icon) ->
                val isSelected = themeMode == mode
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                  shadowElevation = if (isSelected) 2.dp else 0.dp,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.setThemeMode(mode) }
                    .padding(vertical = 6.dp)
                    .testTag("theme_btn_${label.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = label,
                      tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Storage & Cache management
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Storage, contentDescription = "Storage", tint = DeepBlue)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Room Local Database",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Cached: 8 sessions, 3 announcements, 4 materials",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = { viewModel.clearLocalCache() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            modifier = Modifier.fillMaxWidth().testTag("clear_cache_btn")
          ) {
            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear cache", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Local Cache & Reset Tokens")
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun PersonaSelectorItem(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  tag: String
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) DeepBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) DeepBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .testTag(tag)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(14.dp)
          .clip(CircleShape)
          .background(if (isSelected) DeepBlue else Color.Transparent)
          .border(2.dp, if (isSelected) DeepBlue else MaterialTheme.colorScheme.outline, CircleShape)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = if (isSelected) DeepBlue else MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
