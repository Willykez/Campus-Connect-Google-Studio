package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AccountStatus
import com.example.data.model.Role
import com.example.data.model.UserProfile

class SessionManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  companion object {
    private const val PREFS_NAME = "campus_connect_session_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_KEEP_ACTIVE = "key_keep_session_active"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_USER_ROLE = "key_user_role"
    private const val KEY_USER_STATUS = "key_user_status"
    private const val KEY_USER_UNIVERSITY = "key_user_university"
    private const val KEY_USER_STUDENT_NO = "key_user_student_no"
    private const val KEY_USER_STAFF_NO = "key_user_staff_no"
    private const val KEY_USER_PROGRAMME = "key_user_programme"
    private const val KEY_USER_COHORT = "key_user_cohort"
  }

  fun saveSession(user: UserProfile, keepActive: Boolean = true) {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, true)
      .putBoolean(KEY_KEEP_ACTIVE, keepActive)
      .putString(KEY_USER_ID, user.id)
      .putString(KEY_USER_NAME, user.fullName)
      .putString(KEY_USER_EMAIL, user.email)
      .putString(KEY_USER_ROLE, user.role.name)
      .putString(KEY_USER_STATUS, user.status.name)
      .putString(KEY_USER_UNIVERSITY, user.university)
      .putString(KEY_USER_STUDENT_NO, user.studentNumber)
      .putString(KEY_USER_STAFF_NO, user.staffNumber)
      .putString(KEY_USER_PROGRAMME, user.programme)
      .putString(KEY_USER_COHORT, user.cohort)
      .apply()
  }

  fun isSessionActive(): Boolean {
    val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    val keepActive = prefs.getBoolean(KEY_KEEP_ACTIVE, true)
    return isLoggedIn && keepActive
  }

  fun isKeepActiveEnabled(): Boolean {
    return prefs.getBoolean(KEY_KEEP_ACTIVE, true)
  }

  fun setKeepActive(keepActive: Boolean) {
    prefs.edit().putBoolean(KEY_KEEP_ACTIVE, keepActive).apply()
  }

  fun loadSession(): UserProfile? {
    if (!isSessionActive()) return null
    val id = prefs.getString(KEY_USER_ID, null) ?: return null
    val name = prefs.getString(KEY_USER_NAME, "User") ?: "User"
    val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    val roleStr = prefs.getString(KEY_USER_ROLE, Role.STUDENT.name) ?: Role.STUDENT.name
    val statusStr = prefs.getString(KEY_USER_STATUS, AccountStatus.APPROVED.name) ?: AccountStatus.APPROVED.name
    val university = prefs.getString(KEY_USER_UNIVERSITY, "Catholic University of Mbeya (CUoM)") ?: "Catholic University of Mbeya (CUoM)"
    val studentNo = prefs.getString(KEY_USER_STUDENT_NO, null)
    val staffNo = prefs.getString(KEY_USER_STAFF_NO, null)
    val programme = prefs.getString(KEY_USER_PROGRAMME, null)
    val cohort = prefs.getString(KEY_USER_COHORT, null)

    val role = try {
      Role.valueOf(roleStr)
    } catch (_: Exception) {
      Role.STUDENT
    }

    val status = try {
      AccountStatus.valueOf(statusStr)
    } catch (_: Exception) {
      AccountStatus.APPROVED
    }

    return UserProfile(
      id = id,
      fullName = name,
      email = email,
      role = role,
      status = status,
      university = university,
      studentNumber = studentNo,
      staffNumber = staffNo,
      programme = programme,
      cohort = cohort
    )
  }

  fun clearSession() {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, false)
      .remove(KEY_USER_ID)
      .remove(KEY_USER_NAME)
      .remove(KEY_USER_EMAIL)
      .remove(KEY_USER_ROLE)
      .remove(KEY_USER_STATUS)
      .remove(KEY_USER_UNIVERSITY)
      .remove(KEY_USER_STUDENT_NO)
      .remove(KEY_USER_STAFF_NO)
      .remove(KEY_USER_PROGRAMME)
      .remove(KEY_USER_COHORT)
      .apply()
  }
}
