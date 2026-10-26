package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for CampusConnect, holding structured local offline metadata
 * and extracted AI summaries for uploaded course documents.
 */
@Database(
  entities = [ProcessedDocumentEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun processedDocumentDao(): ProcessedDocumentDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(
      context: Context,
      scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "campus_connect_documents.db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch {
            populateInitialDocuments(database.processedDocumentDao())
          }
        }
      }

      suspend fun populateInitialDocuments(dao: ProcessedDocumentDao) {
        val seedDocs = listOf(
          ProcessedDocumentEntity(
            id = "doc-room-001",
            title = "Asymptotic Notation & Dynamic Programming",
            fileName = "CS210_Algorithms_Module4.pdf",
            fileSize = "2.4 MB",
            mimeType = "application/pdf",
            courseCode = "CS210",
            courseTitle = "Data Structures & Algorithms",
            university = "Catholic University of Mbeya (CUoM)",
            uploaderName = "Dr. Joseph Mushi",
            timestamp = System.currentTimeMillis() - 86400000L * 2, // 2 days ago
            extractedSummary = "Comprehensive analysis of Big-O, Omega, and Theta asymptotic bounds combined with optimal substructure dynamic programming memoization models.",
            keyTopics = "Asymptotic Analysis, Recurrence Relations, Memoization, Matrix Chain Multiplication, Bellman Equation",
            learningObjectives = "Analyze runtime complexities using Master Theorem; Formulate bottom-up dynamic programming tables; Identify optimal substructure in graph problems",
            studyTips = "Trace table indices for 1D vs 2D memoization grids; Practice drawing recursion trees for divide-and-conquer recurrences",
            wordCount = 4850,
            pageCount = 18,
            readingTimeMinutes = 18,
            extractionStatus = "COMPLETED",
            category = "Notes",
            tags = "Dynamic Programming, Big-O, Memoization"
          ),
          ProcessedDocumentEntity(
            id = "doc-room-002",
            title = "Relational Algebra & Normalization Forms",
            fileName = "IS201_Database_Normalization.pdf",
            fileSize = "1.9 MB",
            mimeType = "application/pdf",
            courseCode = "IS201",
            courseTitle = "Database Systems & Design",
            university = "Catholic University of Mbeya (CUoM)",
            uploaderName = "Prof. Emanuel K.",
            timestamp = System.currentTimeMillis() - 86400000L * 5, // 5 days ago
            extractedSummary = "In-depth decomposition of 1NF, 2NF, 3NF and BCNF relational anomalies, functional dependency closures, and lossless-join guarantees.",
            keyTopics = "Functional Dependencies, Minimal Cover, 3NF Synthesis, BCNF Decomposition, Lossless Joins",
            learningObjectives = "Identify transitive and partial dependencies; Decompose tables to eliminate update anomalies; Calculate attribute closures",
            studyTips = "Write out dependency arrows explicitly before decomposing tables into Third Normal Form",
            wordCount = 3920,
            pageCount = 14,
            readingTimeMinutes = 14,
            extractionStatus = "COMPLETED",
            category = "Syllabus",
            tags = "Relational Algebra, 3NF, BCNF, Functional Dependencies"
          ),
          ProcessedDocumentEntity(
            id = "doc-room-003",
            title = "TCP Congestion Control & Flow Mechanics",
            fileName = "NW302_Network_Protocols.pdf",
            fileSize = "3.1 MB",
            mimeType = "application/pdf",
            courseCode = "NW302",
            courseTitle = "Computer Networks",
            university = "Catholic University of Mbeya (CUoM)",
            uploaderName = "Dr. Sarah Temu",
            timestamp = System.currentTimeMillis() - 86400000L * 7, // 7 days ago
            extractedSummary = "Detailed exploration of Slow Start, AIMD (Additive Increase Multiplicative Decrease), fast retransmit, and packet window pacing over lossy channels.",
            keyTopics = "Slow Start Threshold, Sliding Window, Triple Duplicate ACKs, AIMD, ECN Signaling",
            learningObjectives = "Understand window scaling dynamics; Differentiate flow control from congestion control; Diagram three-way handshakes",
            studyTips = "Review the state transition diagram between Slow Start and Congestion Avoidance",
            wordCount = 5200,
            pageCount = 22,
            readingTimeMinutes = 20,
            extractionStatus = "COMPLETED",
            category = "Exam Revision",
            tags = "TCP, Sliding Window, Networking, Final Exam"
          )
        )
        dao.insertDocuments(seedDocs)
      }
    }
  }
}
