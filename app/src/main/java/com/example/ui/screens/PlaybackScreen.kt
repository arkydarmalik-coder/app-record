package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingRepository
import com.example.model.CloudSyncStatus
import com.example.model.RecordingSession
import com.example.ui.components.ActivityTimelineView
import com.example.ui.components.VideoPlayerCanvas
import com.example.ui.theme.*

@Composable
fun PlaybackScreen(
    repository: RecordingRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSession by repository.selectedRecording.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (selectedSession == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No recording selected", color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val session = selectedSession!!

    var currentPlaybackMs by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }

    // Trimmer State
    var isTrimmingMode by remember { mutableStateOf(false) }
    var trimStartMs by remember { mutableStateOf(0L) }
    var trimEndMs by remember { mutableStateOf(session.durationSeconds * 1000L) }

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
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "REAL-TIME PLAYBACK INSPECTOR",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = session.timestamp,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = {
                        repository.deleteRecording(session.id)
                        Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = CoralRecordingRed
                        )
                    }
                }
            }

            // Title & App Name header card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ElectricPurple.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = session.appName,
                                    color = ElectricPurple,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Surface(
                                color = when (session.cloudStatus) {
                                    CloudSyncStatus.SYNCED -> EmeraldSuccess.copy(alpha = 0.2f)
                                    CloudSyncStatus.UPLOADING -> CyanAccent.copy(alpha = 0.2f)
                                    else -> TextMuted.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (session.cloudStatus == CloudSyncStatus.SYNCED) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = if (session.cloudStatus == CloudSyncStatus.SYNCED) EmeraldSuccess else CyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = session.cloudStatus.name,
                                        color = if (session.cloudStatus == CloudSyncStatus.SYNCED) EmeraldSuccess else CyanAccent,
                                        fontSize = 10.sp,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = session.title,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${session.resolution} • ${session.durationFormatted} • ${session.fileSizeMB} MB",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // High-Performance Video Player Viewport
            item {
                VideoPlayerCanvas(
                    session = session,
                    currentPlaybackMs = currentPlaybackMs,
                    isPlaying = isPlaying,
                    onPlaybackSeek = { currentPlaybackMs = it },
                    onTogglePlayPause = { isPlaying = !isPlaying },
                    playbackSpeed = playbackSpeed,
                    onChangePlaybackSpeed = { playbackSpeed = it },
                    isTrimming = isTrimmingMode,
                    trimStartMs = trimStartMs,
                    trimEndMs = trimEndMs,
                    onTrimRangeChange = { start, end ->
                        trimStartMs = start
                        trimEndMs = end
                    }
                )
            }

            // Quick Action Buttons Row (Trim Clip, Cloud Sync, Share Link)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { isTrimmingMode = !isTrimmingMode },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTrimmingMode) AmberWarning else DarkSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = if (isTrimmingMode) Color.Black else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTrimmingMode) "Cancel Trim" else "Trim Clip",
                            color = if (isTrimmingMode) Color.Black else TextPrimary,
                            fontSize = 12.sp
                        )
                    }

                    if (session.cloudStatus == CloudSyncStatus.LOCAL_ONLY) {
                        Button(
                            onClick = {
                                repository.syncRecordingToCloud(session.id)
                                Toast.makeText(context, "Syncing to Cloud Vault...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloud Sync", fontSize = 12.sp)
                        }
                    } else if (session.cloudStatus == CloudSyncStatus.SYNCED) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(session.cloudShareUrl))
                                Toast.makeText(context, "Cloud stream link copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Link", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Apply Trim Action Banner if Trimming mode is active
            item {
                AnimatedVisibility(visible = isTrimmingMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberWarning, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("CUT & CROP RECORDING CLIP", color = AmberWarning, fontSize = 13.sp, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adjust timeline sliders to crop start and end timestamps.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    repository.trimRecording(session.id, trimStartMs, trimEndMs)
                                    isTrimmingMode = false
                                    Toast.makeText(context, "Clip trimmed successfully!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                            ) {
                                Text("Save Trimmed Clip", color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Cloud Storage Stream URL Card
            item {
                if (session.cloudStatus == CloudSyncStatus.SYNCED) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CLOUDFLARE WORKER PLAYBACK STREAM", color = CyanAccent, fontSize = 11.sp)
                                Surface(color = EmeraldSuccess.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text("AES-256 ENCRYPTED", color = EmeraldSuccess, fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = session.cloudShareUrl,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Activity Timeline Event Inspector
            item {
                ActivityTimelineView(
                    events = session.events,
                    currentPlaybackMs = currentPlaybackMs,
                    onEventClicked = { eventMs ->
                        currentPlaybackMs = eventMs
                        isPlaying = true
                        Toast.makeText(context, "Seeked to event timestamp", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
