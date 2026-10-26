package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository layer that abstracts access to local Room document metadata.
 */
class ProcessedDocumentRepository(
  private val dao: ProcessedDocumentDao
) {

  val allDocuments: Flow<List<ProcessedDocumentEntity>> = dao.getAllDocuments()
  val documentCount: Flow<Int> = dao.getDocumentCount()

  fun searchDocuments(query: String): Flow<List<ProcessedDocumentEntity>> {
    return if (query.isBlank()) {
      dao.getAllDocuments()
    } else {
      dao.searchDocuments(query.trim())
    }
  }

  fun getDocumentsByCourse(courseCode: String): Flow<List<ProcessedDocumentEntity>> {
    return if (courseCode.isBlank() || courseCode.equals("ALL", ignoreCase = true)) {
      dao.getAllDocuments()
    } else {
      dao.getDocumentsByCourse(courseCode)
    }
  }

  fun getDocumentsByCategory(category: String): Flow<List<ProcessedDocumentEntity>> {
    return if (category.isBlank() || category.equals("ALL", ignoreCase = true)) {
      dao.getAllDocuments()
    } else {
      dao.getDocumentsByCategory(category)
    }
  }

  suspend fun getDocumentById(id: String): ProcessedDocumentEntity? {
    return dao.getDocumentById(id)
  }

  suspend fun insertDocument(document: ProcessedDocumentEntity) {
    dao.insertDocument(document)
  }

  suspend fun insertDocuments(documents: List<ProcessedDocumentEntity>) {
    dao.insertDocuments(documents)
  }

  suspend fun deleteDocumentById(id: String) {
    dao.deleteDocumentById(id)
  }

  suspend fun deleteAll() {
    dao.deleteAllDocuments()
  }

  companion object {
    @Volatile
    private var INSTANCE: ProcessedDocumentRepository? = null

    fun getInstance(context: Context): ProcessedDocumentRepository {
      return INSTANCE ?: synchronized(this) {
        val database = AppDatabase.getDatabase(context)
        val instance = ProcessedDocumentRepository(database.processedDocumentDao())
        INSTANCE = instance
        instance
      }
    }
  }
}
