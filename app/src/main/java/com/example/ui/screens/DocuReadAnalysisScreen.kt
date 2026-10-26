package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.DocuReadMetadataDoc
import com.example.data.model.AttachmentFile
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberLight
import com.example.viewmodel.CampusViewModel
import com.example.viewmodel.DocuReadUiState
import com.example.viewmodel.DocuReadViewModel

/**
 * Compose screen layout that allows users to initiate the document analysis process
 * and displays an interactive loading state during extraction.
 * Ingests and displays metadata stored in Firebase Firestore for the DocuRead app.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocuReadAnalysisScreen(
  campusViewModel: CampusViewModel,
  docuReadViewModel: DocuReadViewModel,
  onNavigateBack: () -> Unit,
  onPreviewDocument: ((AttachmentFile, String?, String?) -> Unit)? = null,
  onNavigateToProcessedDocs: (() -> Unit)? = null,
  initialAttachment: AttachmentFile? = null,
  initialCourseCode: String? = null,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val uiState by docuReadViewModel.uiState.collectAsState()
  val ingestedDocs by docuReadViewModel.allIngestedDocuments.collectAsState()
  val currentUser by campusViewModel.currentUser.collectAsState()
  val materials by campusViewModel.materials.collectAsState()

  // Selected document to analyze
  var selectedAttachment by remember {
    mutableStateOf(
      initialAttachment ?: materials.firstOrNull()?.attachment ?: AttachmentFile(
        fileName = "CS210_Algorithms_Module4.pdf",
        fileSize = "2.4 MB",
        mimeType = "application/pdf",
        previewText = "Comprehensive lecture notes on Asymptotic Notation and Dynamic Programming recurrence relations."
      )
    )
  }

  var selectedCourseCode by remember {
    mutableStateOf(initialCourseCode ?: materials.firstOrNull()?.courseCode ?: "CS210")
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Top Bar / Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(DeepBlue),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "DocuRead • AI Document Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
        Text(
          text = "Intelligent concept extraction, summarization & Firestore metadata storage",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFDCFCE7),
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Firestore Ready", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Quick navigation link to Room Processed Documents
    if (onNavigateToProcessedDocs != null) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToProcessedDocs() }
          .testTag("docuread_to_processed_docs_banner")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Browse Processed Summaries in Room Database",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Text(
            text = "View All →",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    // Dynamic UI based on Extraction State
    when (val state = uiState) {
      is DocuReadUiState.Idle -> {
        // Document Selection & Initiation Card
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .testTag("docuread_initiate_card")
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Text(
                  text = "Selected Document for Analysis:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = DeepBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(DeepBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (selectedAttachment.isImage) Icons.Default.Description else Icons.Default.PictureAsPdf,
                      contentDescription = null,
                      tint = DeepBlue,
                      modifier = Modifier.size(24.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = selectedAttachment.fileName,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${selectedAttachment.fileSize} • $selectedCourseCode • ${currentUser.university}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Initiate Analysis Button
                Button(
                  onClick = {
                    docuReadViewModel.initiateAnalysis(
                      attachment = selectedAttachment,
                      courseCode = selectedCourseCode,
                      courseTitle = "Academic Curriculum Resource",
                      university = currentUser.university,
                      uploaderName = currentUser.fullName
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_initiate_document_analysis")
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Initiate Document Analysis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                }
              }
            }
          }

          // Choose from other course materials
          item {
            Text(
              text = "Or Select from Coursework Materials:",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          items(materials) { mat ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (selectedAttachment.fileName == mat.attachment.fileName) DeepBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedAttachment = mat.attachment
                  selectedCourseCode = mat.courseCode
                }
                .testTag("select_material_${mat.id}")
            ) {
              Row(
                modifier = Modifier
                  .padding(14.dp)
                  .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.PictureAsPdf,
                  contentDescription = null,
                  tint = DeepBlue,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = mat.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                  Text(text = "${mat.courseCode} • ${mat.attachment.fileSize} • By ${mat.uploadedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (selectedAttachment.fileName == mat.attachment.fileName) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = DeepBlue, modifier = Modifier.size(20.dp))
                }
              }
            }
          }

          // Recently Ingested Documents from Firestore
          if (ingestedDocs.isNotEmpty()) {
            item {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Stored Firestore Ingestions (${ingestedDocs.size}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepBlue
              )
            }

            items(ingestedDocs) { docItem ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    // Open preview of previously analyzed document
                    val att = AttachmentFile(
                      fileName = docItem.fileName,
                      fileSize = docItem.fileSize,
                      previewText = docItem.summary
                    )
                    onPreviewDocument?.invoke(att, docItem.uploaderName, docItem.courseCode)
                  }
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = docItem.documentTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    StatusBadge(text = "Firestore Synced", backgroundColor = SuccessGreenLight, contentColor = SuccessGreen)
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(text = docItem.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = "${docItem.wordCount} words • ${docItem.pageCount} pages • ${docItem.courseCode}", style = MaterialTheme.typography.labelSmall, color = DeepBlue)
                }
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(30.dp))
          }
        }
      }

      is DocuReadUiState.Extracting -> {
        // LOADING STATE DURING EXTRACTION (Requested Feature)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("docuread_extraction_loading_container"),
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
              .fillMaxWidth(0.92f)
              .border(1.5.dp, DeepBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
          ) {
            Column(
              modifier = Modifier.padding(26.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(DeepBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(
                  progress = { state.progress },
                  color = DeepBlue,
                  strokeWidth = 4.dp,
                  modifier = Modifier.size(56.dp)
                )
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = DeepBlue,
                  modifier = Modifier.size(24.dp)
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = "DocuRead Analyzing Document...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepBlue
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = state.fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )

              Spacer(modifier = Modifier.height(16.dp))

              LinearProgressIndicator(
                progress = { state.progress },
                color = DeepBlue,
                trackColor = DeepBlue.copy(alpha = 0.15f),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .testTag("extraction_progress_bar")
              )

              Spacer(modifier = Modifier.height(14.dp))

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = DeepBlue,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = state.currentStep,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }

      is DocuReadUiState.Success -> {
        // Extraction Success & Ingested Metadata Display
        val doc = state.metadata
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .testTag("docuread_extraction_success_container"),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Ingestion Confirmation Banner
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = "Metadata Stored in Firebase Firestore",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                  )
                  Text(
                    text = "Collection: 'document_metadata' • ID: ${doc.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF166534).copy(alpha = 0.85f)
                  )
                }
              }
            }
          }

          // Document Key Stats Grid
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Word Count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text("${doc.wordCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
              }

              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Pages", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text("${doc.pageCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
              }

              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Read Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text("${doc.readingTimeMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
              }
            }
          }

          // Executive Summary Card with Copy & Share
          item {
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            var isSummaryCopied by remember { mutableStateOf(false) }

            val shareText = remember(doc) {
              """
              📘 CAMPUSCONNECT DOCUREAD ANALYSIS
              Title: ${doc.documentTitle}
              Course: ${doc.courseCode} - ${doc.courseTitle}
              File: ${doc.fileName} (${doc.fileSize})
              Uploaded by: ${doc.uploaderName} • ${doc.university}
              
              📄 EXECUTIVE SUMMARY:
              ${doc.summary}
              
              🎯 KEY CONCEPTS & SYLLABUS TOPICS:
              ${doc.keyTopics.joinToString(", ")}
              
              ✅ ACTIONABLE OBJECTIVES:
              ${doc.learningObjectives.joinToString("\n• ")}
              
              💡 EXAM & REVISION STRATEGY:
              ${doc.studyTips.joinToString("\n• ")}
              """.trimIndent()
            }

            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Executive Curriculum Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                  }

                  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                      onClick = {
                        clipboardManager.setText(AnnotatedString(doc.summary))
                        Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                        isSummaryCopied = true
                      },
                      modifier = Modifier.size(32.dp).testTag("docuread_copy_summary_icon_btn")
                    ) {
                      Icon(
                        imageVector = if (isSummaryCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Summary",
                        tint = if (isSummaryCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    IconButton(
                      onClick = {
                        val sendIntent = Intent().apply {
                          action = Intent.ACTION_SEND
                          putExtra(Intent.EXTRA_TEXT, shareText)
                          putExtra(Intent.EXTRA_SUBJECT, "DocuRead Analysis: ${doc.documentTitle}")
                          type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Document Analysis"))
                      },
                      modifier = Modifier.size(32.dp).testTag("docuread_share_summary_icon_btn")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = doc.summary,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  lineHeight = 22.sp
                )
              }
            }
          }

          // Key Topics Chips
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Core Concepts & Key Topics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  doc.keyTopics.forEach { topic ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = DeepBlue.copy(alpha = 0.1f)
                    ) {
                      Text(
                        text = topic,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          // Learning Objectives
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.School, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Actionable Learning Objectives", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                doc.learningObjectives.forEach { obj ->
                  Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = obj, style = MaterialTheme.typography.bodySmall)
                  }
                }
              }
            }
          }

          // Study & Exam Strategies
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Exam & Revision Strategy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                doc.studyTips.forEach { tip ->
                  Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }
              }
            }
          }

          // Actions
          item {
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            var isCopiedBottom by remember { mutableStateOf(false) }

            val fullAnalysisShareText = remember(doc) {
              """
              📘 CAMPUSCONNECT DOCUREAD ANALYSIS
              Title: ${doc.documentTitle}
              Course: ${doc.courseCode} - ${doc.courseTitle}
              File: ${doc.fileName} (${doc.fileSize})
              Uploaded by: ${doc.uploaderName} • ${doc.university}
              
              📄 EXECUTIVE SUMMARY:
              ${doc.summary}
              
              🎯 KEY CONCEPTS & SYLLABUS TOPICS:
              ${doc.keyTopics.joinToString(", ")}
              
              ✅ ACTIONABLE OBJECTIVES:
              ${doc.learningObjectives.joinToString("\n• ")}
              
              💡 EXAM & REVISION STRATEGY:
              ${doc.studyTips.joinToString("\n• ")}
              """.trimIndent()
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                androidx.compose.material3.FilledTonalButton(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(doc.summary))
                    Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                    isCopiedBottom = true
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("docuread_copy_summary_btn")
                ) {
                  Icon(
                    imageVector = if (isCopiedBottom) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (isCopiedBottom) "Copied!" else "Copy Summary")
                }

                androidx.compose.material3.OutlinedButton(
                  onClick = {
                    val sendIntent = Intent().apply {
                      action = Intent.ACTION_SEND
                      putExtra(Intent.EXTRA_TEXT, fullAnalysisShareText)
                      putExtra(Intent.EXTRA_SUBJECT, "DocuRead Analysis: ${doc.documentTitle}")
                      type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Document Analysis"))
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("docuread_share_btn")
                ) {
                  Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Share / Export")
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = { docuReadViewModel.reset() },
                  modifier = Modifier.weight(1f).testTag("btn_analyze_another_doc")
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Analyze Another")
                }

                Button(
                  onClick = {
                    val att = AttachmentFile(
                      fileName = doc.fileName,
                      fileSize = doc.fileSize,
                      previewText = doc.summary
                    )
                    onPreviewDocument?.invoke(att, doc.uploaderName, doc.courseCode)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.weight(1f).testTag("btn_read_full_doc")
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Read in Viewer")
                }
              }
            }
            Spacer(modifier = Modifier.height(30.dp))
          }
        }
      }

      is DocuReadUiState.Error -> {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { docuReadViewModel.reset() }) {
              Text("Retry")
            }
          }
        }
      }
    }
  }
}
