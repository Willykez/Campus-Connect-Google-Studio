package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AttachmentFile
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.SuccessGreen

@Composable
fun DocumentViewerDialog(
  attachment: AttachmentFile,
  authorOrUploaderName: String? = null,
  courseCode: String? = null,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .fillMaxHeight(0.94f)
        .padding(8.dp)
        .testTag("built_in_document_viewer_modal")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Viewer Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (attachment.isImage) Color(0xFFE0E7FF) else Color(0xFFFEE2E2)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (attachment.isImage) Icons.Default.Image else Icons.Default.PictureAsPdf,
                contentDescription = "Document Icon",
                tint = if (attachment.isImage) DeepBlue else DangerRed,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
              Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
              Text(
                text = "${attachment.fileSize} • ${if (attachment.isImage) "Image Asset" else "PDF Document"}${if (courseCode != null) " • $courseCode" else ""}${if (authorOrUploaderName != null) " • By $authorOrUploaderName" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_document_viewer_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close Viewer")
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Document Body
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9))
            .padding(12.dp)
        ) {
          if (!attachment.isImage) {
            // Built-in Native PDF Preview Component using AndroidView and PdfRenderer
            PdfPreviewComponent(
              attachment = attachment,
              courseCode = courseCode,
              uploaderName = authorOrUploaderName,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            // Image Document Presentation
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "IMAGE ATTACHMENT VIEW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Verified,
                      contentDescription = "Verified Image",
                      tint = SuccessGreen,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Verified Submission Asset",
                      style = MaterialTheme.typography.labelSmall,
                      color = SuccessGreen,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (attachment.uriString != null) {
                  AsyncImage(
                    model = attachment.uriString,
                    contentDescription = attachment.fileName,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(360.dp)
                      .clip(RoundedCornerShape(8.dp))
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(300.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFFF8FAFC))
                      .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image preview",
                        tint = DeepBlue,
                        modifier = Modifier.size(64.dp)
                      )
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                        text = "Scanned Coursework Diagram",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                      )
                      Text(
                        text = "High resolution photo • Verified readable format",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                  text = attachment.previewText ?: "Scanned diagram showing finite state automata, transition tables, and computational proof for coursework validation.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFF334155),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Viewer Bottom Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CampusConnect Secure In-App Viewer",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            modifier = Modifier
              .height(36.dp)
              .testTag("btn_close_document_viewer")
          ) {
            Text("Close Document")
          }
        }
      }
    }
  }
}
