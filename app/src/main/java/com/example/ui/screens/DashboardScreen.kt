package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.RecordingRepository
import com.example.model.CloudSyncStatus
import com.example.model.RecordingSession
import com.example.model.RecorderState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    repository: RecordingRepository,
    onNavigateToPlayback: (RecordingSession) -> Unit,
    onNavigateToVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recorderState by repository.recorderState.collectAsState()
    val durationSeconds by repository.activeRecordingDuration.collectAsState()
    val recordingsList by repository.recordingsList.collectAsState()
    val config by repository.config.collectAsState()
    val cloudStats by repository.cloudStats.collectAsState()
    val liveFps by repository.liveFps.collectAsState()
    val liveCpuUsage by repository.liveCpuUsage.collectAsState()
    val liveAudioLevel by repository.liveAudioLevel.collectAsState()
    val liveTouchedPoints by repository.liveTouchedPoints.collectAsState()

    var showFloatingOverlay by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
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
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = ElectricPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "APP SCREEN RECORDING",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            text = "Screen Activity Monitor & Secure Cloud Vault",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Cloud Worker Badge
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(EmeraldSuccess, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Worker Cloud Stream",
                                color = CyanAccent,
                                fontSize = 10.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Hero Banner Asset Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///app/src/main/res/drawable/screen_rec_hero_1793869801044.jpg")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Hero Dashboard Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(DarkBackground.copy(alpha = 0.85f), Color.Transparent)
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Surface(
                                    color = ElectricPurple,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "REAL-TIME MONITORING ACTIVE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Capture & Playback Screen Activity",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Auto-tag touch events, audio peaks & upload encrypted to Cloud",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Main Recorder Control Panel
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (recorderState == RecorderState.RECORDING) CoralRecordingRed else DarkBorder,
                            RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Live Viewfinder / Status Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            when (recorderState) {
                                                RecorderState.RECORDING -> CoralRecordingRed
                                                RecorderState.PAUSED -> AmberWarning
                                                RecorderState.PROCESSING -> CyanAccent
                                                RecorderState.IDLE -> TextMuted
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (recorderState) {
                                        RecorderState.RECORDING -> "RECORDING SCREEN ACTIVITY"
                                        RecorderState.PAUSED -> "RECORDING PAUSED"
                                        RecorderState.PROCESSING -> "SAVING & ENCRYPTING..."
                                        RecorderState.IDLE -> "READY TO MONITOR"
                                    },
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            val mins = durationSeconds / 60
                            val secs = durationSeconds % 60
                            val timeText = String.format("%02d:%02d", mins, secs)

                            Surface(
                                color = if (recorderState == RecorderState.RECORDING) CoralRecordingRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = timeText,
                                    color = if (recorderState == RecorderState.RECORDING) CoralRecordingRed else CyanAccent,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio Waveform Visualizer
                        WaveformVisualizer(
                            isRecording = recorderState == RecorderState.RECORDING,
                            audioLevel = liveAudioLevel
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Record Button Trigger
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(
                                    when (recorderState) {
                                        RecorderState.RECORDING -> CoralRecordingRed
                                        RecorderState.PAUSED -> AmberWarning
                                        RecorderState.PROCESSING -> CyanAccent
                                        RecorderState.IDLE -> ElectricPurple
                                    }
                                )
                                .clickable {
                                    when (recorderState) {
                                        RecorderState.IDLE -> repository.startRecording()
                                        RecorderState.RECORDING -> repository.stopRecording()
                                        RecorderState.PAUSED -> repository.resumeRecording()
                                        RecorderState.PROCESSING -> {}
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (recorderState) {
                                    RecorderState.RECORDING -> Icons.Default.Stop
                                    RecorderState.PAUSED -> Icons.Default.PlayArrow
                                    RecorderState.PROCESSING -> Icons.Default.Sync
                                    RecorderState.IDLE -> Icons.Default.FiberManualRecord
                                },
                                contentDescription = "Record Trigger",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = when (recorderState) {
                                RecorderState.IDLE -> "Tap to start screen recording"
                                RecorderState.RECORDING -> "Tap to stop & save to Cloud Vault"
                                RecorderState.PAUSED -> "Tap to resume screen capture"
                                RecorderState.PROCESSING -> "Syncing to Cloudflare Worker..."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Controls Row (Mic Toggle, Overlay Toggle, Resolution selector)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = config.audioSource != "Mute",
                                onClick = {
                                    val nextAudio = if (config.audioSource == "Mute") "Combined System + Mic" else "Mute"
                                    repository.updateConfig(config.copy(audioSource = nextAudio))
                                },
                                label = { Text(if (config.audioSource == "Mute") "Muted" else "Mic Active", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (config.audioSource == "Mute") Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricPurple
                                )
                            )

                            FilterChip(
                                selected = showFloatingOverlay,
                                onClick = { showFloatingOverlay = !showFloatingOverlay },
                                label = { Text("Floating Overlay", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanAccent
                                )
                            )

                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text(config.resolution, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Hd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Real-time Telemetry Card
            item {
                LiveTelemetryCard(
                    fps = liveFps,
                    cpuUsage = liveCpuUsage,
                    touchedPoints = liveTouchedPoints,
                    resolution = "${config.resolution} @ ${config.fps}fps"
                )
            }

            // Cloud Storage Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToVault() }
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SECURE CLOUD VAULT",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = "${cloudStats.usedStorageGB} GB / ${cloudStats.totalStorageGB} GB",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Storage bar
                        val fraction = (cloudStats.usedStorageGB / cloudStats.totalStorageGB).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectricPurple,
                            trackColor = DarkSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔒 ${cloudStats.encryptionKeyStatus}",
                                color = EmeraldSuccess,
                                fontSize = 11.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "View Cloud Vault",
                                    color = CyanAccent,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Recordings Header & Carousel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SCREEN RECORDINGS",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                    TextButton(onClick = onNavigateToVault) {
                        Text("See All (${recordingsList.size})", color = CyanAccent, fontSize = 12.sp)
                    }
                }
            }

            item {
                if (recordingsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(DarkSurface, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No screen recordings saved yet.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recordingsList) { session ->
                            RecordingCardCompact(
                                session = session,
                                onClick = { onNavigateToPlayback(session) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Control Overlay widget preview anchored at bottom right
        if (showFloatingOverlay) {
            FloatingOverlayWidget(
                recorderState = recorderState,
                durationSeconds = durationSeconds,
                onStartRecording = { repository.startRecording() },
                onPauseRecording = { repository.pauseRecording() },
                onResumeRecording = { repository.resumeRecording() },
                onStopRecording = { repository.stopRecording() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun RecordingCardCompact(
    session: RecordingSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
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
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Duration badge bottom right
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = session.durationFormatted,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Cloud Sync Badge top left
                Surface(
                    color = when (session.cloudStatus) {
                        CloudSyncStatus.SYNCED -> EmeraldSuccess
                        CloudSyncStatus.UPLOADING -> CyanAccent
                        else -> TextMuted
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = session.cloudStatus.name,
                        color = Color.Black,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.title,
                color = TextPrimary,
                fontSize = 13.sp,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.appName,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "${session.fileSizeMB} MB",
                    color = CyanAccent,
                    fontSize = 10.sp
                )
            }
        }
    }
}
