package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProcessedDocumentEntity
import com.example.data.model.AttachmentFile
import com.example.viewmodel.CampusViewModel
import com.example.viewmodel.DocuReadViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable screen that displays a list of processed documents stored in Room Database,
 * featuring summary previews, keyword topics, reading statistics, and full analysis inspection.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProcessedDocumentsScreen(
  campusViewModel: CampusViewModel,
  docuReadViewModel: DocuReadViewModel,
  onNavigateBack: () -> Unit,
  onNavigateToAnalyze: () -> Unit,
  onPreviewDocument: ((AttachmentFile, String?, String?) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val documents by docuReadViewModel.filteredProcessedDocuments.collectAsState()
  val allDocs by docuReadViewModel.localProcessedDocuments.collectAsState()
  val searchQuery by docuReadViewModel.searchQuery.collectAsState()
  val selectedCourseFilter by docuReadViewModel.selectedCourseFilter.collectAsState()
  val selectedCategoryFilter by docuReadViewModel.selectedCategoryFilter.collectAsState()

  // Selected document for full analysis bottom sheet
  var inspectingDocument by remember { mutableStateOf<ProcessedDocumentEntity?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Extract distinct course codes for filter chips
  val courseOptions = remember(allDocs) {
    listOf("ALL") + allDocs.map { it.courseCode }.filter { it.isNotBlank() }.distinct().sorted()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Bar Header
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("processed_docs_back_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Processed Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Room SQLite Database • ${allDocs.size} documents saved",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Action: Analyze new document
        Button(
          onClick = onNavigateToAnalyze,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("processed_docs_analyze_new_btn")
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Analyze",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
          )
        }
      }
    }

    // Search and Filters Section
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { docuReadViewModel.setSearchQuery(it) },
        placeholder = {
          Text(
            "Search documents, summaries or topics...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.primary
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { docuReadViewModel.setSearchQuery("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("processed_docs_search_field")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Course Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(courseOptions) { course ->
          val isSelected = selectedCourseFilter.equals(course, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { docuReadViewModel.setCourseFilter(course) },
            label = {
              Text(
                text = if (course == "ALL") "All Subjects (${allDocs.size})" else course,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("course_chip_$course")
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Category Filter Chips
      val roomCategories = listOf("ALL", "Notes", "Syllabus", "Assignments", "Exam Revision")
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(roomCategories) { cat ->
          val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
          val count = if (cat == "ALL") allDocs.size else allDocs.count { it.category.equals(cat, ignoreCase = true) }
          FilterChip(
            selected = isSelected,
            onClick = { docuReadViewModel.setCategoryFilter(cat) },
            label = {
              Text(
                text = if (cat == "ALL") "All Types" else "$cat ($count)",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.testTag("room_category_chip_$cat")
          )
        }
      }
    }

    // Document List Content
    if (documents.isEmpty()) {
      // Empty state
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Description,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = if (searchQuery.isNotBlank()) "No Matching Documents" else "No Processed Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (searchQuery.isNotBlank())
              "Try searching with a different subject code, title or concept keyword."
            else
              "Uploaded materials analyzed with DocuRead AI are indexed here in local Room Database.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 18.sp
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onNavigateToAnalyze,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("empty_state_analyze_btn")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze New Document")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(documents, key = { it.id }) { doc ->
          ProcessedDocumentCard(
            doc = doc,
            onViewFullAnalysis = { inspectingDocument = doc },
            onReadDocument = {
              onPreviewDocument?.invoke(
                AttachmentFile(
                  fileName = doc.fileName,
                  fileSize = doc.fileSize,
                  mimeType = doc.mimeType,
                  previewText = doc.extractedSummary
                ),
                doc.uploaderName,
                doc.courseCode
              )
            },
            onDelete = { docuReadViewModel.deleteProcessedDocument(doc.id) }
          )
        }

        item {
          Spacer(modifier = Modifier.height(32.dp))
        }
      }
    }
  }

  // Full Analysis Modal Bottom Sheet
  inspectingDocument?.let { doc ->
    ModalBottomSheet(
      onDismissRequest = { inspectingDocument = null },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface,
      dragHandle = {
        Box(
          modifier = Modifier
            .padding(vertical = 10.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
        )
      }
    ) {
      FullAnalysisSheetContent(
        doc = doc,
        onDismiss = { inspectingDocument = null },
        onReadDocument = {
          inspectingDocument = null
          onPreviewDocument?.invoke(
            AttachmentFile(
              fileName = doc.fileName,
              fileSize = doc.fileSize,
              mimeType = doc.mimeType,
              previewText = doc.extractedSummary
            ),
            doc.uploaderName,
            doc.courseCode
          )
        }
      )
    }
  }
}

/**
 * Individual document card displaying metadata, summary preview, and action buttons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProcessedDocumentCard(
  doc: ProcessedDocumentEntity,
  onViewFullAnalysis: () -> Unit,
  onReadDocument: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val formattedDate = remember(doc.timestamp) {
    val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    formatter.format(Date(doc.timestamp))
  }

  val topicList = remember(doc.keyTopics) {
    doc.keyTopics.split(",").map { it.trim() }.filter { it.isNotBlank() }
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("processed_doc_card_${doc.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: File Type Badge + Course + Timestamp
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (doc.mimeType.contains("pdf")) Icons.Default.PictureAsPdf else Icons.Default.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
              Text(
                text = doc.courseCode,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            Text(
              text = "${doc.fileName} • ${doc.fileSize}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = doc.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("delete_doc_${doc.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete from Room",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Summary Preview Box (Highlighted Box)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
          .padding(12.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "EXTRACTED SUMMARY PREVIEW",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = 0.5.sp
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = doc.extractedSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
          )
        }
      }

      // Key Topics Chips
      if (topicList.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          topicList.take(3).forEach { topic ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
              Text(
                text = topic,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          if (topicList.size > 3) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
              Text(
                text = "+${topicList.size - 3} more",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(10.dp))

      // Footer: Stats & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Stats: Reading time, Pages
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${doc.readingTimeMinutes} min",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${doc.pageCount} pp",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onReadDocument,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("read_doc_${doc.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Visibility,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Read", style = MaterialTheme.typography.labelSmall)
          }

          FilledTonalButton(
            onClick = onViewFullAnalysis,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.testTag("view_analysis_${doc.id}")
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Full Analysis",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

/**
 * Full Analysis Bottom Sheet content displaying comprehensive extraction data.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullAnalysisSheetContent(
  doc: ProcessedDocumentEntity,
  onDismiss: () -> Unit,
  onReadDocument: () -> Unit
) {
  val formattedDate = remember(doc.timestamp) {
    val formatter = SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault())
    formatter.format(Date(doc.timestamp))
  }

  val topics = remember(doc.keyTopics) {
    doc.keyTopics.split(",").map { it.trim() }.filter { it.isNotBlank() }
  }

  val objectives = remember(doc.learningObjectives) {
    doc.learningObjectives.split(";").map { it.trim() }.filter { it.isNotBlank() }
  }

  val tips = remember(doc.studyTips) {
    doc.studyTips.split(";").map { it.trim() }.filter { it.isNotBlank() }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
      .verticalScroll(rememberScrollState())
      .testTag("full_analysis_sheet_content")
  ) {
    // Header Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
          Text(
            text = doc.courseCode,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = doc.courseTitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Quick Share and Copy icon buttons in header
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        var isCopiedHeader by remember { mutableStateOf(false) }

        val shareText = remember(doc) {
          """
          📘 CAMPUSCONNECT DOCUREAD ANALYSIS
          Title: ${doc.title}
          Course: ${doc.courseCode} - ${doc.courseTitle}
          File: ${doc.fileName} (${doc.fileSize})
          Uploaded by: ${doc.uploaderName} • ${doc.university}
          
          📄 EXECUTIVE SUMMARY:
          ${doc.extractedSummary}
          
          🎯 KEY SYLLABUS TOPICS:
          ${doc.keyTopics}
          
          ✅ LEARNING OBJECTIVES:
          ${doc.learningObjectives.replace(";", "\n• ")}
          
          💡 STUDY & EXAM REVISION TIPS:
          ${doc.studyTips.replace(";", "\n• ")}
          """.trimIndent()
        }

        IconButton(
          onClick = {
            clipboardManager.setText(AnnotatedString(doc.extractedSummary))
            Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
            isCopiedHeader = true
          },
          modifier = Modifier.testTag("header_copy_summary_btn")
        ) {
          Icon(
            imageVector = if (isCopiedHeader) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = "Copy Summary",
            tint = if (isCopiedHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, shareText)
              putExtra(Intent.EXTRA_SUBJECT, "Analysis: ${doc.title}")
              type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Document Analysis")
            context.startActivity(shareIntent)
          },
          modifier = Modifier.testTag("header_share_summary_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Title
    Text(
      text = doc.title,
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Timestamp & Uploader
    Text(
      text = "Extracted on $formattedDate • Uploaded by ${doc.uploaderName}",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Storage info pill
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Storage,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Persisted in Room Database (table: processed_documents) • Available Offline",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section 1: Executive Summary
    Text(
      text = "Executive Summary",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = doc.extractedSummary,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp,
        modifier = Modifier.padding(14.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section 2: Key Concepts & Syllabus Topics
    Text(
      text = "Key Syllabus Concepts",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      topics.forEach { topic ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = topic,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
    }

    // Section 3: Learning Objectives
    if (objectives.isNotEmpty()) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Learning Objectives",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        objectives.forEach { obj ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = obj,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 19.sp
            )
          }
        }
      }
    }

    // Section 4: Study Tips
    if (tips.isNotEmpty()) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Study & Exam Revision Tips",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tips.forEach { tip ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = tip,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 19.sp
            )
          }
        }
      }
    }

    // Export & Share Action Buttons Section
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isSummaryCopied by remember { mutableStateOf(false) }

    val formattedShareText = remember(doc) {
      """
      📘 CAMPUSCONNECT DOCUREAD ANALYSIS
      Title: ${doc.title}
      Course: ${doc.courseCode} - ${doc.courseTitle}
      File: ${doc.fileName} (${doc.fileSize})
      Uploaded by: ${doc.uploaderName} • ${doc.university}
      
      📄 EXECUTIVE SUMMARY:
      ${doc.extractedSummary}
      
      🎯 KEY SYLLABUS CONCEPTS:
      ${doc.keyTopics}
      
      ✅ LEARNING OBJECTIVES:
      ${doc.learningObjectives.replace(";", "\n• ")}
      
      💡 STUDY & EXAM REVISION TIPS:
      ${doc.studyTips.replace(";", "\n• ")}
      """.trimIndent()
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Dedicated Share & Export Action Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Button: Copy Summary to Clipboard
      FilledTonalButton(
        onClick = {
          clipboardManager.setText(AnnotatedString(doc.extractedSummary))
          Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
          isSummaryCopied = true
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("copy_summary_to_clipboard_btn")
      ) {
        Icon(
          imageVector = if (isSummaryCopied) Icons.Default.Check else Icons.Default.ContentCopy,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isSummaryCopied) "Copied!" else "Copy Summary",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Button: Share / Export Full Analysis
      OutlinedButton(
        onClick = {
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, formattedShareText)
            putExtra(Intent.EXTRA_SUBJECT, "Analysis: ${doc.title}")
            type = "text/plain"
          }
          val shareIntent = Intent.createChooser(sendIntent, "Share Document Analysis")
          context.startActivity(shareIntent)
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("share_export_analysis_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Share / Export",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    // Action button: Read in document viewer
    Spacer(modifier = Modifier.height(10.dp))
    Button(
      onClick = onReadDocument,
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
        .testTag("full_analysis_read_doc_btn")
    ) {
      Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Read Document in Viewer", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(32.dp))
  }
}
