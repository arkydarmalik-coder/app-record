package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.RecordingRepository
import com.example.model.CloudSyncStatus
import com.example.model.RecordingSession
import com.example.ui.theme.*

@Composable
fun VaultScreen(
    repository: RecordingRepository,
    onNavigateToPlayback: (RecordingSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val recordingsList by repository.recordingsList.collectAsState()
    val cloudStats by repository.cloudStats.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SYNCED, LOCAL

    val filteredRecordings = remember(recordingsList, searchQuery, selectedFilter) {
        recordingsList.filter { session ->
            val matchesSearch = session.title.contains(searchQuery, ignoreCase = true) ||
                    session.appName.contains(searchQuery, ignoreCase = true) ||
                    session.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesFilter = when (selectedFilter) {
                "SYNCED" -> session.cloudStatus == CloudSyncStatus.SYNCED
                "LOCAL" -> session.cloudStatus == CloudSyncStatus.LOCAL_ONLY
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLOUD VAULT & LIBRARY",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            text = "AES-256 Cloud Encrypted Recording Storage",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            repository.syncAllToCloud()
                            Toast.makeText(context, "Syncing local recordings to Cloud Vault...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync All", fontSize = 11.sp)
                    }
                }
            }

            // Cloud Vault Storage Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("CLOUD STORAGE USAGE", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "${String.format("%.1f", cloudStats.usedStorageGB)} GB / ${cloudStats.totalStorageGB} GB",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("SYNCED CLOUD FILES", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "${cloudStats.cloudSyncedCount} of ${recordingsList.size}",
                                    color = CyanAccent,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val progress = (cloudStats.usedStorageGB / cloudStats.totalStorageGB).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyanAccent,
                            trackColor = DarkSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒 ${cloudStats.encryptionKeyStatus}", color = EmeraldSuccess, fontSize = 11.sp)
                            Text("Worker Cloud Storage: Active", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by app, title, or tag...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricPurple,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL" to "All Files", "SYNCED" to "Cloud Synced", "LOCAL" to "Local Only").forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanAccent
                                )
                            )
                        }
                    }
                }
            }

            // Recordings List
            if (filteredRecordings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(DarkSurface, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No recordings match your filter.", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredRecordings) { session ->
                    VaultRecordingCard(
                        session = session,
                        onPlay = {
                            repository.selectRecording(session)
                            onNavigateToPlayback(session)
                        },
                        onSync = {
                            repository.syncRecordingToCloud(session.id)
                            Toast.makeText(context, "Uploading to Cloud Vault...", Toast.LENGTH_SHORT).show()
                        },
                        onCopyLink = {
                            clipboardManager.setText(AnnotatedString(session.cloudShareUrl))
                            Toast.makeText(context, "Cloud link copied!", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            repository.deleteRecording(session.id)
                            Toast.makeText(context, "Deleted recording", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultRecordingCard(
    session: RecordingSession,
    onPlay: () -> Unit,
    onSync: () -> Unit,
    onCopyLink: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Preview
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (session.thumbnailPath.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file://${session.thumbnailPath}")
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = when (session.cloudStatus) {
                            CloudSyncStatus.SYNCED -> EmeraldSuccess.copy(alpha = 0.2f)
                            CloudSyncStatus.UPLOADING -> CyanAccent.copy(alpha = 0.2f)
                            else -> TextMuted.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = session.cloudStatus.name,
                            color = when (session.cloudStatus) {
                                CloudSyncStatus.SYNCED -> EmeraldSuccess
                                CloudSyncStatus.UPLOADING -> CyanAccent
                                else -> TextMuted
                            },
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${session.appName} • ${session.resolution}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = "${session.timestamp} • ${session.durationFormatted} • ${session.fileSizeMB} MB",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action icons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ElectricPurple.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onPlay() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspect", color = ElectricPurple, fontSize = 10.sp)
                        }
                    }

                    if (session.cloudStatus == CloudSyncStatus.LOCAL_ONLY) {
                        Surface(
                            color = CyanAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onSync() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync", color = CyanAccent, fontSize = 10.sp)
                            }
                        }
                    } else if (session.cloudStatus == CloudSyncStatus.SYNCED) {
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onCopyLink() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Link", color = EmeraldSuccess, fontSize = 10.sp)
                            }
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = CoralRecordingRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
