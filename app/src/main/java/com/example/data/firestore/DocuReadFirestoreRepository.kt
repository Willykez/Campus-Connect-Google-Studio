package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Firestore repository handling the ingestion and storage of document metadata
 * for DocuRead in Firebase Firestore.
 */
class DocuReadFirestoreRepository(
  private val context: Context? = null,
  private val firestoreDb: FirebaseFirestore? = null
) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val fallbackDocs = MutableStateFlow<List<DocuReadMetadataDoc>>(emptyList())

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
      Log.w("DocuReadFirestore", "Firestore not available, falling back to local memory store: ${e.message}")
      null
    }
  }

  init {
    // Seed initial sample documents
    val sampleDocs = listOf(
      DocuReadMetadataDoc(
        id = "doc-meta-001",
        documentTitle = "Asymptotic Notation & Dynamic Programming",
        fileName = "CS210_Algorithms_Module4.pdf",
        fileSize = "2.4 MB",
        mimeType = "application/pdf",
        courseCode = "CS210",
        courseTitle = "Data Structures & Algorithms",
        university = "Catholic University of Mbeya (CUoM)",
        uploaderId = "usr-lec-01",
        uploaderName = "Dr. Joseph Mushi",
        extractionStatus = "COMPLETED",
        summary = "Comprehensive lecture notes analyzing Big-O, Omega, and Theta bounds, followed by memoization tables for 0/1 Knapsack and Longest Common Subsequence.",
        keyTopics = listOf("Time Complexity", "Big-O Notation", "Memoization", "Bellman Equation", "Optimal Substructure"),
        learningObjectives = listOf(
          "Master asymptotic asymptotic proof techniques",
          "Convert recursive recurrence relations into bottom-up tables",
          "Analyze space-time trade-offs in subproblem caching"
        ),
        studyTips = listOf(
          "Draw the recursion tree before writing code",
          "Verify base cases to prevent stack overflow errors"
        ),
        wordCount = 4250,
        pageCount = 18,
        readingTimeMinutes = 15,
        createdAt = Timestamp.now(),
        extractedAt = Timestamp.now()
      ),
      DocuReadMetadataDoc(
        id = "doc-meta-002",
        documentTitle = "Relational Normalization: 1NF to BCNF",
        fileName = "CS220_Database_Normalization.pdf",
        fileSize = "1.8 MB",
        mimeType = "application/pdf",
        courseCode = "CS220",
        courseTitle = "Database Management Systems",
        university = "Catholic University of Mbeya (CUoM)",
        uploaderId = "usr-lec-01",
        uploaderName = "Dr. Joseph Mushi",
        extractionStatus = "COMPLETED",
        summary = "Detailed tutorial on functional dependencies, identifying candidate keys, and lossless-join decompositions into Third Normal Form and Boyce-Codd Normal Form.",
        keyTopics = listOf("Functional Dependencies", "1NF / 2NF / 3NF", "Boyce-Codd Normal Form", "Lossless Join Decomposition"),
        learningObjectives = listOf(
          "Identify transitive and partial functional dependencies",
          "Eliminate insertion, update, and deletion anomalies",
          "Prove dependency preservation during decomposition"
        ),
        studyTips = listOf(
          "Always test with Armstrong's Axioms for closure",
          "Check whether the LHS of every non-trivial FD is a superkey"
        ),
        wordCount = 3100,
        pageCount = 12,
        readingTimeMinutes = 11,
        createdAt = Timestamp.now(),
        extractedAt = Timestamp.now()
      )
    )
    fallbackDocs.value = sampleDocs
  }

  /**
   * Ingests and stores document metadata into the 'document_metadata' Firestore collection.
   */
  suspend fun ingestDocumentMetadata(doc: DocuReadMetadataDoc): Result<String> {
    val database = db
    val docId = if (doc.id.isBlank()) "doc-" + UUID.randomUUID().toString().take(8) else doc.id
    val readyDoc = doc.copy(
      id = docId,
      createdAt = doc.createdAt ?: Timestamp.now(),
      extractedAt = doc.extractedAt ?: Timestamp.now()
    )

    // Update in-memory fallback
    fallbackDocs.value = listOf(readyDoc) + fallbackDocs.value.filterNot { it.id == docId }

    return if (database != null) {
      try {
        database.collection("document_metadata")
          .document(docId)
          .set(readyDoc)
          .await()
        Result.success(docId)
      } catch (e: Exception) {
        Log.w("DocuReadFirestore", "Firestore set failed, using in-memory store: ${e.message}")
        Result.success(docId)
      }
    } else {
      Result.success(docId)
    }
  }

  /**
   * Observes all ingested document metadata from Firestore.
   */
  fun observeAllDocuments(): Flow<List<DocuReadMetadataDoc>> {
    val database = db
    return if (database != null) {
      database.collection("document_metadata")
        .snapshots()
        .map { snapshot ->
          val remote = snapshot.toObjects(DocuReadMetadataDoc::class.java)
          if (remote.isNotEmpty()) {
            remote
          } else {
            fallbackDocs.value
          }
        }
        .catch { e ->
          Log.w("DocuReadFirestore", "Snapshot listener error: ${e.message}")
          emit(fallbackDocs.value)
        }
    } else {
      fallbackDocs.asStateFlow()
    }
  }
}
