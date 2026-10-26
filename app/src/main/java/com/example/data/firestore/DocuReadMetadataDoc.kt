package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing document extraction metadata ingested and stored
 * in Firebase Firestore under the 'document_metadata' / 'docuread_analyses' collection.
 */
@IgnoreExtraProperties
data class DocuReadMetadataDoc(
  @DocumentId val id: String = "",
  val documentTitle: String = "",
  val fileName: String = "",
  val fileSize: String = "",
  val mimeType: String = "application/pdf",
  val courseCode: String = "",
  val courseTitle: String = "",
  val university: String = "",
  val uploaderId: String = "",
  val uploaderName: String = "",
  val extractionStatus: String = "COMPLETED",
  val summary: String = "",
  val keyTopics: List<String> = emptyList(),
  val learningObjectives: List<String> = emptyList(),
  val studyTips: List<String> = emptyList(),
  val wordCount: Int = 0,
  val pageCount: Int = 1,
  val readingTimeMinutes: Int = 5,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val extractedAt: Timestamp? = null
)
