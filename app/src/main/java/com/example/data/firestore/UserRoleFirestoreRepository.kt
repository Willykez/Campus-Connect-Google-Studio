package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AccountStatus
import com.example.data.model.Role
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class UserRoleDoc(
  @DocumentId val id: String = "",
  val userId: String = "",
  val email: String = "",
  val fullName: String = "",
  val role: String = "STUDENT",
  val accountStatus: String = "APPROVED",
  val university: String = "Catholic University of Mbeya (CUoM)",
  val studentOrStaffNumber: String = "",
  val department: String = "",
  val assignedBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toDomainRole(): Role = try {
    Role.valueOf(role.uppercase())
  } catch (_: Exception) {
    Role.STUDENT
  }

  fun toDomainAccountStatus(): AccountStatus = try {
    AccountStatus.valueOf(accountStatus.uppercase())
  } catch (_: Exception) {
    AccountStatus.APPROVED
  }
}

class UserRoleFirestoreRepository(
  private val context: Context? = null,
  private val firestoreDb: FirebaseFirestore? = null
) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val fallbackRoles = MutableStateFlow<List<UserRoleDoc>>(emptyList())

  private val db: FirebaseFirestore? by lazy {
    if (firestoreDb != null) return@lazy firestoreDb
    try {
      val dbIdRes = context?.resources?.getIdentifier("firestore_database_id", "string", context.packageName) ?: 0
      if (dbIdRes != 0) {
        val dbId = context?.getString(dbIdRes)
        if (!dbId.isNullOrBlank()) {
          FirebaseFirestore.getInstance(dbId)
        } else {
          FirebaseFirestore.getInstance()
        }
      } else {
        FirebaseFirestore.getInstance()
      }
    } catch (e: Exception) {
      Log.w("UserRoleFirestore", "Firestore not initialized, falling back to local memory store: ${e.message}")
      null
    }
  }

  init {
    // Seed in-memory initial cache
    val defaultDocs = listOf(
      UserRoleDoc(
        id = "usr-stu-01",
        userId = "usr-stu-01",
        email = "a.mwangi@gmail.com",
        fullName = "Asha Mwangi",
        role = "STUDENT",
        accountStatus = "APPROVED",
        university = "Catholic University of Mbeya (CUoM)",
        studentOrStaffNumber = "CUOM/BCS/2022/084",
        department = "Department of Computer Science"
      ),
      UserRoleDoc(
        id = "usr-stu-02",
        userId = "usr-stu-02",
        email = "k.tarimo@gmail.com",
        fullName = "Kelvin Tarimo",
        role = "STUDENT",
        accountStatus = "APPROVED",
        university = "Catholic University of Mbeya (CUoM)",
        studentOrStaffNumber = "CUOM/BCS/2022/091",
        department = "Department of Computer Science"
      ),
      UserRoleDoc(
        id = "usr-pen-01",
        userId = "usr-pen-01",
        email = "b.juma@gmail.com",
        fullName = "Baraka Juma (Pending)",
        role = "STUDENT",
        accountStatus = "PENDING",
        university = "Catholic University of Mbeya (CUoM)",
        studentOrStaffNumber = "CUOM/BIS/2024/011",
        department = "Department of Information Systems"
      ),
      UserRoleDoc(
        id = "usr-sus-01",
        userId = "usr-sus-01",
        email = "d.kibona@gmail.com",
        fullName = "Daudi Kibona (Suspended)",
        role = "STUDENT",
        accountStatus = "SUSPENDED",
        university = "Catholic University of Mbeya (CUoM)",
        studentOrStaffNumber = "CUOM/BCS/2023/044",
        department = "Department of Computer Science"
      ),
      UserRoleDoc(
        id = "usr-lec-01",
        userId = "usr-lec-01",
        email = "j.mushi@cuom.ac.tz",
        fullName = "Dr. Joseph Mushi",
        role = "LECTURER",
        accountStatus = "APPROVED",
        university = "Catholic University of Mbeya (CUoM)",
        studentOrStaffNumber = "CUOM-STF-041",
        department = "Department of Computer Science"
      ),
      UserRoleDoc(
        id = "usr-lec-02",
        userId = "usr-lec-02",
        email = "g.temu@udsm.ac.tz",
        fullName = "Prof. Grace Temu",
        role = "LECTURER",
        accountStatus = "APPROVED",
        university = "University of Dar es Salaam (UDSM)",
        studentOrStaffNumber = "UDSM-STF-019",
        department = "Department of Mathematics & Logic"
      ),
      UserRoleDoc(
        id = "usr-adm-01",
        userId = "usr-adm-01",
        email = "admin.registry@cuom.ac.tz",
        fullName = "Emanuel Mwenda (CUoM Admin)",
        role = "ADMIN",
        accountStatus = "APPROVED",
        university = "Catholic University of Mbeya (CUoM)",
        studentOrStaffNumber = "CUOM-ADM-001",
        department = "Academic Registry Office"
      ),
      UserRoleDoc(
        id = "usr-adm-02",
        userId = "usr-adm-02",
        email = "s.kimaro@student.ifm.ac.tz",
        fullName = "Dr. Sarah Kimaro (IFM Admin)",
        role = "ADMIN",
        accountStatus = "APPROVED",
        university = "Institute of Finance Management (IFM)",
        studentOrStaffNumber = "IFM-ADM-008",
        department = "Dean of ICT & Engineering"
      )
    )
    fallbackRoles.value = defaultDocs
  }

  fun observeAllRoles(): Flow<List<UserRoleDoc>> {
    val database = db
    return if (database != null) {
      database.collection("user_roles")
        .snapshots()
        .map { snapshot ->
          val list = snapshot.toObjects(UserRoleDoc::class.java)
          if (list.isNotEmpty()) list else fallbackRoles.value
        }
        .catch { error ->
          Log.w("UserRoleFirestore", "Error observing Firestore user_roles, using memory store: ${error.message}")
          emit(fallbackRoles.value)
        }
    } else {
      fallbackRoles.asStateFlow()
    }
  }

  fun observeUserRole(userId: String): Flow<UserRoleDoc?> {
    val database = db
    return if (database != null) {
      database.collection("user_roles").document(userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObject(UserRoleDoc::class.java)
            ?: fallbackRoles.value.find { it.userId == userId || it.id == userId }
        }
        .catch { error ->
          Log.w("UserRoleFirestore", "Error observing user role for $userId: ${error.message}")
          emit(fallbackRoles.value.find { it.userId == userId || it.id == userId })
        }
    } else {
      fallbackRoles.map { list -> list.find { it.userId == userId || it.id == userId } }
    }
  }

  suspend fun updateUserRole(
    targetUserId: String,
    newRole: Role,
    assignedByAdminId: String
  ): Result<Unit> {
    return try {
      val database = db
      val updatedList = fallbackRoles.value.map { doc ->
        if (doc.userId == targetUserId || doc.id == targetUserId) {
          doc.copy(role = newRole.name, assignedBy = assignedByAdminId)
        } else doc
      }
      fallbackRoles.value = updatedList

      if (database != null) {
        val docRef = database.collection("user_roles").document(targetUserId)
        docRef.update(
          mapOf(
            "role" to newRole.name,
            "assignedBy" to assignedByAdminId,
            "updatedAt" to Timestamp.now()
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("UserRoleFirestore", "Failed to update role in Firestore: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun updateAccountStatus(
    targetUserId: String,
    newStatus: AccountStatus
  ): Result<Unit> {
    return try {
      val database = db
      val updatedList = fallbackRoles.value.map { doc ->
        if (doc.userId == targetUserId || doc.id == targetUserId) {
          doc.copy(accountStatus = newStatus.name)
        } else doc
      }
      fallbackRoles.value = updatedList

      if (database != null) {
        val docRef = database.collection("user_roles").document(targetUserId)
        docRef.update(
          mapOf(
            "accountStatus" to newStatus.name,
            "updatedAt" to Timestamp.now()
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("UserRoleFirestore", "Failed to update status in Firestore: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun registerUserRole(doc: UserRoleDoc): Result<Unit> {
    return try {
      val existing = fallbackRoles.value.filter { it.userId != doc.userId && it.id != doc.id }
      fallbackRoles.value = listOf(doc) + existing

      val database = db
      if (database != null) {
        val docId = doc.userId.ifBlank { doc.id }
        database.collection("user_roles").document(docId).set(doc).await()
      }
      Log.i("UserRoleFirestore", "User registered successfully with university ${doc.university}: ${doc.fullName}")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("UserRoleFirestore", "Failed to register user in Firestore: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun seedUsersIfEmpty(users: List<UserProfile>) {
    try {
      val database = db ?: return
      val snapshot = database.collection("user_roles").limit(1).get().await()
      if (snapshot.isEmpty) {
        for (user in users) {
          val doc = UserRoleDoc(
            id = user.id,
            userId = user.id,
            email = user.email,
            fullName = user.fullName,
            role = user.role.name,
            accountStatus = user.status.name,
            university = user.university,
            studentOrStaffNumber = user.studentNumber ?: user.staffNumber ?: "",
            department = user.programme ?: ""
          )
          database.collection("user_roles").document(user.id).set(doc).await()
        }
        Log.i("UserRoleFirestore", "Successfully seeded ${users.size} default roles into Firestore collection 'user_roles'")
      }
    } catch (e: Exception) {
      Log.w("UserRoleFirestore", "Firestore seeding skipped or not needed: ${e.message}")
    }
  }
}
