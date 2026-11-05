package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricPurple
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isRecording: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 24
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = width / (barCount * 1.5f)
        val gap = barWidth * 0.5f

        for (i in 0 until barCount) {
            val factor = if (isRecording) {
                val wave = (sin((i.toFloat() / barCount * 3.14f * 2) + phase) + 1f) / 2f
                (wave * audioLevel * 0.8f + 0.2f).coerceIn(0.15f, 1.0f)
            } else {
                0.2f
            }

            val currentBarHeight = height * factor
            val x = i * (barWidth + gap)
            val y = (height - currentBarHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(CyanAccent, ElectricPurple)
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
