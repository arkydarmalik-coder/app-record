package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingSession
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun VideoPlayerCanvas(
    session: RecordingSession,
    currentPlaybackMs: Long,
    isPlaying: Boolean,
    onPlaybackSeek: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    playbackSpeed: Float,
    onChangePlaybackSpeed: (Float) -> Unit,
    isTrimming: Boolean = false,
    trimStartMs: Long = 0L,
    trimEndMs: Long = 0L,
    onTrimRangeChange: (Long, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val totalMs = (session.durationSeconds * 1000L).coerceAtLeast(1000L)

    // Automatically advance playback slider when isPlaying is true
    LaunchedEffect(isPlaying, currentPlaybackMs, playbackSpeed) {
        if (isPlaying) {
            delay((100 / playbackSpeed).toLong())
            val nextMs = currentPlaybackMs + 100L
            if (nextMs >= totalMs) {
                onPlaybackSeek(0L)
                onTogglePlayPause()
            } else {
                onPlaybackSeek(nextMs)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        // Video Viewport Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF030712)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw video frame simulation grid & canvas graphics
                val progress = currentPlaybackMs.toFloat() / totalMs
                val gridColor = Color(0xFF1E293B)
                val lineCount = 8
                for (i in 1 until lineCount) {
                    val x = w * (i.toFloat() / lineCount)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    val y = h * (i.toFloat() / lineCount)
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }

                // Dynamic UI frame representation
                val frameX = (w * 0.15f) + (progress * w * 0.1f)
                val frameY = h * 0.2f
                val frameW = w * 0.7f
                val frameH = h * 0.6f

                // Draw phone mockup window
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                    ),
                    topLeft = Offset(frameX, frameY),
                    size = Size(frameW, frameH),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                drawRoundRect(
                    color = ElectricPurple.copy(alpha = 0.6f),
                    topLeft = Offset(frameX, frameY),
                    size = Size(frameW, frameH),
                    cornerRadius = CornerRadius(16f, 16f),
                    style = Stroke(width = 3f)
                )

                // Touch activity indicator pulse
                if (isPlaying) {
                    val pulseRadius = (progress * 50f) % 35f + 10f
                    val pulseX = frameX + (frameW * 0.5f) + (sin(progress * 10f) * frameW * 0.3f)
                    val pulseY = frameY + (frameH * 0.5f)
                    drawCircle(
                        color = CyanAccent.copy(alpha = 0.7f),
                        radius = pulseRadius,
                        center = Offset(pulseX, pulseY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(pulseX, pulseY)
                    )
                }
            }

            // Top Video Overlay Watermark & Quality badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isPlaying) CoralRecordingRed else CyanAccent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${session.appName} • ${session.resolution}",
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = ElectricPurple.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (session.isEncrypted) "AES-256 Cloud Stream" else "Local Playback",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Central Play / Pause tap overlay
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onTogglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Pause",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Bottom Timecode Display
            val currentSecs = (currentPlaybackMs / 1000L).toInt()
            val mins = currentSecs / 60
            val secs = currentSecs % 60
            val timeFormatted = String.format("%02d:%02d", mins, secs)

            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "$timeFormatted / ${session.durationFormatted}",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Scrub Bar & Controls
        if (isTrimming) {
            // Trim handle dual slider view
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TRIM RANGE", color = AmberWarning, fontSize = 11.sp)
                    val trimSecs = ((trimEndMs - trimStartMs) / 1000L).toInt()
                    Text("Selected: ${trimSecs}s", color = TextPrimary, fontSize = 11.sp)
                }

                Slider(
                    value = currentPlaybackMs.toFloat(),
                    onValueChange = { onPlaybackSeek(it.toLong()) },
                    valueRange = 0f..totalMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = AmberWarning,
                        activeTrackColor = AmberWarning,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )
            }
        } else {
            // Standard Seek Slider
            Slider(
                value = currentPlaybackMs.toFloat(),
                onValueChange = { onPlaybackSeek(it.toLong()) },
                valueRange = 0f..totalMs.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = ElectricPurple,
                    inactiveTrackColor = DarkSurfaceVariant
                )
            )
        }

        // Control Buttons Row (Skip, Play, Speed, Fullscreen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Toggles
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                    val isSelected = playbackSpeed == speed
                    Surface(
                        color = if (isSelected) CyanAccent else DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { onChangePlaybackSpeed(speed) }
                    ) {
                        Text(
                            text = "${speed}x",
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Transport Controls (Back 5s, Play/Pause, Forward 5s)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    onPlaybackSeek((currentPlaybackMs - 5000L).coerceAtLeast(0L))
                }) {
                    Icon(Icons.Default.Replay5, contentDescription = "-5s", tint = TextPrimary)
                }

                IconButton(
                    onClick = { onTogglePlayPause() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(ElectricPurple, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    onPlaybackSeek((currentPlaybackMs + 5000L).coerceAtMost(totalMs))
                }) {
                    Icon(Icons.Default.Forward5, contentDescription = "+5s", tint = TextPrimary)
                }
            }
        }
    }
}
