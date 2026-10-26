package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttachmentFile
import com.example.data.model.ContentApprovalStatus
import com.example.data.model.Role
import com.example.data.model.StudyMaterial
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarmAmberLight
import com.example.viewmodel.CampusViewModel

@Composable
fun MaterialsScreen(
  viewModel: CampusViewModel,
  onOpenUploadDialog: () -> Unit,
  onPreviewDocument: (AttachmentFile, String?, String?) -> Unit,
  onAnalyzeWithDocuRead: ((AttachmentFile, String) -> Unit)? = null,
  onNavigateToProcessedDocs: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val materials by viewModel.materials.collectAsState()

  var selectedSubjectFilter by remember { mutableStateOf("ALL") }
  var selectedTypeFilter by remember { mutableStateOf("ALL") }
  var selectedTagFilter by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  // Track expanded state for each subject module (default all expanded for easy discovery)
  val expandedSubjects = remember { mutableStateMapOf<String, Boolean>() }

  // Extract distinct subjects
  val subjectCodes = remember(materials) {
    materials.map { it.courseCode }.distinct().sorted()
  }

  // Filter materials by subject, type/category, tag, and search query (filename, content keywords, etc.)
  val filteredMaterials = remember(materials, selectedSubjectFilter, selectedTypeFilter, selectedTagFilter, searchQuery) {
    materials.filter { mat ->
      val matchesSubject = when (selectedSubjectFilter) {
        "ALL" -> true
        "SAVED" -> mat.isDownloaded
        else -> mat.courseCode.equals(selectedSubjectFilter, ignoreCase = true)
      }
      val matchesType = when (selectedTypeFilter) {
        "ALL" -> true
        else -> mat.category.equals(selectedTypeFilter, ignoreCase = true)
      }
      val matchesTag = if (selectedTagFilter == null) true else {
        mat.tags.any { it.equals(selectedTagFilter, ignoreCase = true) }
      }
      val matchesSearch = if (searchQuery.isBlank()) true else {
        val q = searchQuery.trim()
        mat.title.contains(q, ignoreCase = true) ||
          mat.courseCode.contains(q, ignoreCase = true) ||
          mat.courseTitle.contains(q, ignoreCase = true) ||
          mat.uploadedBy.contains(q, ignoreCase = true) ||
          mat.attachment.fileName.contains(q, ignoreCase = true) ||
          (mat.attachment.previewText?.contains(q, ignoreCase = true) == true) ||
          mat.description.contains(q, ignoreCase = true) ||
          mat.category.contains(q, ignoreCase = true) ||
          mat.tags.any { it.contains(q, ignoreCase = true) }
      }
      matchesSubject && matchesType && matchesTag && matchesSearch
    }
  }

  // Group materials by courseCode
  val groupedBySubject = remember(filteredMaterials) {
    filteredMaterials.groupBy { it.courseCode }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = viewModel.getString("tab_materials"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Grouped by subject • Lecture notes & handouts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Mobile & Wi-Fi unrestricted download indicator
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFDCFCE7))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FileDownloadDone,
            contentDescription = "Unrestricted Downloads",
            modifier = Modifier.size(14.dp),
            tint = SuccessGreen
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Mobile Data & Wi-Fi",
            style = MaterialTheme.typography.labelSmall,
            color = SuccessGreen,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Search bar with clear button
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by filename, subject, keywords...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = DeepBlue, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("materials_search_input")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Subject Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp)
      ) {
        item {
          FilterChip(
            selected = selectedSubjectFilter == "ALL",
            onClick = { selectedSubjectFilter = "ALL" },
            label = { Text("All Subjects (${materials.size})", fontWeight = FontWeight.Medium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("filter_subject_all")
          )
        }

        if (onNavigateToProcessedDocs != null) {
          item {
            FilterChip(
              selected = false,
              onClick = { onNavigateToProcessedDocs() },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              label = {
                Text(
                  "AI Summaries (Room DB)",
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
              ),
              modifier = Modifier.testTag("filter_chip_room_summaries")
            )
          }
        }

        item {
          val savedCount = materials.count { it.isDownloaded }
          FilterChip(
            selected = selectedSubjectFilter == "SAVED",
            onClick = { selectedSubjectFilter = "SAVED" },
            label = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileDownloadDone, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Saved ($savedCount)")
              }
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SuccessGreenLight,
              selectedLabelColor = SuccessGreen
            ),
            modifier = Modifier.testTag("filter_subject_saved")
          )
        }

        items(subjectCodes) { code ->
          val count = materials.count { it.courseCode == code }
          FilterChip(
            selected = selectedSubjectFilter == code,
            onClick = { selectedSubjectFilter = code },
            label = { Text("$code ($count)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("filter_subject_$code")
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Category / Document Type Filter Chips
      val categories = listOf("ALL", "Notes", "Syllabus", "Assignments", "Exam Revision", "Reference")
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp)
      ) {
        items(categories) { cat ->
          val labelText = if (cat == "ALL") "All Types" else cat
          val count = if (cat == "ALL") materials.size else materials.count { it.category.equals(cat, ignoreCase = true) }
          FilterChip(
            selected = selectedTypeFilter == cat,
            onClick = { selectedTypeFilter = cat },
            label = { Text("$labelText ($count)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DeepBlue,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("filter_type_$cat")
          )
        }
      }

      // Active Tag Filter indicator chip
      if (selectedTagFilter != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 2.dp)
        ) {
          Text(
            text = "Active Tag:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedTagFilter = null }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("active_tag_filter_chip")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "#$selectedTagFilter",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear tag filter",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(12.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Materials List grouped cleanly by subject
      if (groupedBySubject.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = DeepBlue.copy(alpha = 0.4f),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No study materials found for this filter",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          groupedBySubject.forEach { (courseCode, subjectMaterials) ->
            item(key = "subject_header_$courseCode") {
              val isExpanded = expandedSubjects[courseCode] ?: true
              val courseTitle = subjectMaterials.firstOrNull()?.courseTitle ?: "Course Module"

              // Subject Category Header Accordion Card
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    expandedSubjects[courseCode] = !isExpanded
                  }
                  .testTag("subject_header_$courseCode")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepBlue.copy(alpha = 0.12f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = DeepBlue,
                        modifier = Modifier.size(20.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                      Text(
                        text = "$courseCode • $courseTitle",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                      )
                      Text(
                        text = "${subjectMaterials.size} learning ${if (subjectMaterials.size == 1) "document" else "documents"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  IconButton(
                    onClick = { expandedSubjects[courseCode] = !isExpanded },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                      contentDescription = if (isExpanded) "Collapse" else "Expand",
                      tint = DeepBlue
                    )
                  }
                }
              }
            }

            // Expanded Subject Items
            val isExpanded = expandedSubjects[courseCode] ?: true
            if (isExpanded) {
              items(subjectMaterials, key = { it.id }) { mat ->
                val canModerate = viewModel.canEditOrDelete(mat.uploaderId)

                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp)
                    .border(
                      width = 1.dp,
                      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                      shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("material_item_${mat.id}")
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(42.dp)
                          .clip(RoundedCornerShape(10.dp))
                          .background(if (mat.isDownloaded) SuccessGreenLight else DeepBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = if (mat.attachment.isImage) Icons.Default.Image else Icons.Default.PictureAsPdf,
                          contentDescription = "File Type",
                          tint = if (mat.isDownloaded) SuccessGreen else DeepBlue,
                          modifier = Modifier.size(22.dp)
                        )
                      }

                      Spacer(modifier = Modifier.width(12.dp))

                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = mat.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                          )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                          StatusBadge(
                            text = mat.category,
                            backgroundColor = when (mat.category) {
                              "Syllabus" -> Color(0xFFCCFBF1)
                              "Assignments" -> Color(0xFFF3E8FF)
                              "Exam Revision" -> Color(0xFFFEF3C7)
                              else -> DeepBlue.copy(alpha = 0.10f)
                            },
                            contentColor = when (mat.category) {
                              "Syllabus" -> Color(0xFF0F766E)
                              "Assignments" -> Color(0xFF6B21A8)
                              "Exam Revision" -> Color(0xFFB45309)
                              else -> DeepBlue
                            }
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = "${mat.attachment.fileSize} • By ${mat.uploadedBy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                          if (mat.isDownloaded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                              text = "Saved",
                              backgroundColor = SuccessGreenLight,
                              contentColor = SuccessGreen
                            )
                          }
                          if (mat.approvalStatus == ContentApprovalStatus.PENDING_REVIEW) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                              text = "Pending",
                              backgroundColor = WarmAmberLight,
                              contentColor = Color(0xFFB45309)
                            )
                          }
                        }
                      }

                      // Download / Offline Toggle Action
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (mat.isDownloaded) MaterialTheme.colorScheme.surfaceVariant else DeepBlue,
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .clickable { viewModel.toggleMaterialDownloaded(mat.id) }
                          .padding(horizontal = 10.dp, vertical = 6.dp)
                          .testTag("download_toggle_${mat.id}")
                      ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            imageVector = if (mat.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = "Download state",
                            tint = if (mat.isDownloaded) SuccessGreen else Color.White,
                            modifier = Modifier.size(14.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = if (mat.isDownloaded) "Saved" else "Save",
                            color = if (mat.isDownloaded) MaterialTheme.colorScheme.onSurface else Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                    }

                    // Filename indicator with attachment icon
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(top = 4.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Attachment,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = mat.attachment.fileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    // Tag Chips
                    if (mat.tags.isNotEmpty()) {
                      Spacer(modifier = Modifier.height(6.dp))
                      LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        items(mat.tags) { tag ->
                          val isTagSelected = selectedTagFilter == tag
                          Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isTagSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                              .clip(RoundedCornerShape(6.dp))
                              .clickable {
                                selectedTagFilter = if (isTagSelected) null else tag
                              }
                              .padding(horizontal = 6.dp, vertical = 2.dp)
                          ) {
                            Text(
                              text = "#$tag",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Normal,
                              color = if (isTagSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                          }
                        }
                      }
                    }

                    if (mat.description.isNotBlank()) {
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                        text = mat.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    // Content keyword match snippet
                    if (searchQuery.isNotBlank() && mat.attachment.previewText?.contains(searchQuery, ignoreCase = true) == true) {
                      Spacer(modifier = Modifier.height(6.dp))
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DeepBlue.copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = DeepBlue
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "Content match: ...${mat.attachment.previewText?.replace("\n", " ")?.take(100).orEmpty()}...",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepBlue
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Actions Row: Built-in Viewer & Moderation
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      // Read in Viewer & DocuRead Analysis Buttons
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = DeepBlue.copy(alpha = 0.08f),
                          modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPreviewDocument(mat.attachment, mat.uploadedBy, mat.courseCode) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("view_doc_btn_${mat.id}")
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                              imageVector = Icons.Default.Visibility,
                              contentDescription = "Read in Viewer",
                              tint = DeepBlue,
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = "Read",
                              color = DeepBlue,
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }

                        if (onAnalyzeWithDocuRead != null) {
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier
                              .clip(RoundedCornerShape(8.dp))
                              .clickable { onAnalyzeWithDocuRead.invoke(mat.attachment, mat.courseCode) }
                              .padding(horizontal = 10.dp, vertical = 6.dp)
                              .testTag("docuread_btn_${mat.id}")
                          ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "DocuRead Analysis",
                                tint = Color(0xFF6B21A8),
                                modifier = Modifier.size(14.dp)
                              )
                              Spacer(modifier = Modifier.width(4.dp))
                              Text(
                                text = "DocuRead",
                                color = Color(0xFF6B21A8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                              )
                            }
                          }
                        }
                      }

                      // Admin Moderation & Delete
                      if (canModerate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          if (user.role == Role.ADMIN || user.role == Role.SUPER_ADMIN) {
                            if (mat.approvalStatus != ContentApprovalStatus.APPROVED) {
                              IconButton(
                                onClick = { viewModel.setMaterialApproval(mat.id, ContentApprovalStatus.APPROVED) },
                                modifier = Modifier.size(32.dp).testTag("approve_mat_${mat.id}")
                              ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Approve", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                              }
                            }
                            if (mat.approvalStatus != ContentApprovalStatus.DISAPPROVED) {
                              IconButton(
                                onClick = { viewModel.setMaterialApproval(mat.id, ContentApprovalStatus.DISAPPROVED) },
                                modifier = Modifier.size(32.dp).testTag("disapprove_mat_${mat.id}")
                              ) {
                                Icon(Icons.Default.ThumbDown, contentDescription = "Disapprove", tint = DangerRed, modifier = Modifier.size(16.dp))
                              }
                            }
                          }

                          IconButton(
                            onClick = { viewModel.deleteMaterial(mat.id) },
                            modifier = Modifier.size(32.dp).testTag("delete_mat_${mat.id}")
                          ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(80.dp))
          }
        }
      }
    }

    // Upload Material FAB for Lecturers and Admins
    if (user.role == Role.LECTURER || user.role == Role.ADMIN || user.role == Role.SUPER_ADMIN) {
      FloatingActionButton(
        onClick = onOpenUploadDialog,
        containerColor = DeepBlue,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("fab_upload_material")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Upload Study Material"
        )
      }
    }
  }
}
