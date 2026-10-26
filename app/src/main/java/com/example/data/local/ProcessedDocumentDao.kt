package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for interacting with the processed_documents table in Room.
 */
@Dao
interface ProcessedDocumentDao {

  @Query("SELECT * FROM processed_documents ORDER BY timestamp DESC")
  fun getAllDocuments(): Flow<List<ProcessedDocumentEntity>>

  @Query("SELECT * FROM processed_documents WHERE id = :id")
  suspend fun getDocumentById(id: String): ProcessedDocumentEntity?

  @Query("SELECT * FROM processed_documents WHERE courseCode = :courseCode ORDER BY timestamp DESC")
  fun getDocumentsByCourse(courseCode: String): Flow<List<ProcessedDocumentEntity>>

  @Query("SELECT * FROM processed_documents WHERE category = :category ORDER BY timestamp DESC")
  fun getDocumentsByCategory(category: String): Flow<List<ProcessedDocumentEntity>>

  @Query("""
    SELECT * FROM processed_documents 
    WHERE title LIKE '%' || :query || '%' 
       OR extractedSummary LIKE '%' || :query || '%' 
       OR courseCode LIKE '%' || :query || '%' 
       OR fileName LIKE '%' || :query || '%'
       OR keyTopics LIKE '%' || :query || '%'
       OR category LIKE '%' || :query || '%'
       OR tags LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
  """)
  fun searchDocuments(query: String): Flow<List<ProcessedDocumentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: ProcessedDocumentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocuments(documents: List<ProcessedDocumentEntity>)

  @Update
  suspend fun updateDocument(document: ProcessedDocumentEntity)

  @Delete
  suspend fun deleteDocument(document: ProcessedDocumentEntity)

  @Query("DELETE FROM processed_documents WHERE id = :id")
  suspend fun deleteDocumentById(id: String)

  @Query("DELETE FROM processed_documents")
  suspend fun deleteAllDocuments()

  @Query("SELECT COUNT(*) FROM processed_documents")
  fun getDocumentCount(): Flow<Int>
}
