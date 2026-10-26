package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing metadata for uploaded and processed documents,
 * including titles, timestamps, extracted summaries, and syllabus analytics.
 */
@Entity(tableName = "processed_documents")
data class ProcessedDocumentEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val fileName: String,
  val fileSize: String,
  val mimeType: String = "application/pdf",
  val courseCode: String,
  val courseTitle: String,
  val university: String,
  val uploaderName: String,
  val timestamp: Long = System.currentTimeMillis(),
  val extractedSummary: String,
  val keyTopics: String, // Comma-separated list of topics
  val learningObjectives: String, // Semicolon or newline separated
  val studyTips: String, // Semicolon or newline separated
  val wordCount: Int = 0,
  val pageCount: Int = 1,
  val readingTimeMinutes: Int = 5,
  val extractionStatus: String = "COMPLETED",
  val category: String = "Notes", // e.g. "Notes", "Syllabus", "Assignments", "Exam Revision", "Lab Manual"
  val tags: String = "" // Comma-separated list of tags
)
