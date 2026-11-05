package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecorderState
import com.example.ui.theme.*

@Composable
fun FloatingOverlayWidget(
    recorderState: RecorderState,
    durationSeconds: Int,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Status Indicator Pulse
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when (recorderState) {
                            RecorderState.RECORDING -> CoralRecordingRed
                            RecorderState.PAUSED -> AmberWarning
                            RecorderState.PROCESSING -> CyanAccent
                            RecorderState.IDLE -> ElectricPurple
                        },
                        CircleShape
                    )
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (recorderState) {
                        RecorderState.RECORDING -> Icons.Default.Videocam
                        RecorderState.PAUSED -> Icons.Default.Pause
                        RecorderState.PROCESSING -> Icons.Default.Sync
                        RecorderState.IDLE -> Icons.Default.FiberManualRecord
                    },
                    contentDescription = "Status Icon",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    val mins = durationSeconds / 60
                    val secs = durationSeconds % 60
                    val timerStr = String.format("%02d:%02d", mins, secs)

                    Column {
                        Text(
                            text = if (recorderState == RecorderState.RECORDING) "REC $timerStr" else recorderState.name,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Cloud Sync Ready",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Controls
                    when (recorderState) {
                        RecorderState.IDLE -> {
                            IconButton(onClick = onStartRecording, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = EmeraldSuccess)
                            }
                        }
                        RecorderState.RECORDING -> {
                            IconButton(onClick = onPauseRecording, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = AmberWarning)
                            }
                            IconButton(onClick = onStopRecording, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = CoralRecordingRed)
                            }
                        }
                        RecorderState.PAUSED -> {
                            IconButton(onClick = onResumeRecording, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = EmeraldSuccess)
                            }
                            IconButton(onClick = onStopRecording, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = CoralRecordingRed)
                            }
                        }
                        RecorderState.PROCESSING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CyanAccent,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
