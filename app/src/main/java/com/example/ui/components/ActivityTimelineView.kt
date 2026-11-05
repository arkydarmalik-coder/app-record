package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivityEvent
import com.example.model.EventType
import com.example.ui.theme.*

@Composable
fun ActivityTimelineView(
    events: List<ActivityEvent>,
    currentPlaybackMs: Long,
    onEventClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCREEN ACTIVITY TIMELINE (${events.size} EVENTS)",
                fontSize = 12.sp,
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Tap event to seek",
                fontSize = 11.sp,
                color = CyanAccent,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(DarkSurface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No screen activity events logged yet.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(events) { event ->
                    val isCurrent = currentPlaybackMs >= event.timestampMs &&
                            currentPlaybackMs < (event.timestampMs + 10000L)
                    ActivityEventRow(
                        event = event,
                        isCurrent = isCurrent,
                        onClick = { onEventClicked(event.timestampMs) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityEventRow(
    event: ActivityEvent,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val (icon, color) = when (event.eventType) {
        EventType.APP_SWITCH -> Icons.Default.Apps to ElectricPurple
        EventType.TOUCH_HEATMAP -> Icons.Default.TouchApp to CyanAccent
        EventType.AUDIO_PEAK -> Icons.Default.GraphicEq to EmeraldSuccess
        EventType.SYSTEM_ALERT -> Icons.Default.NotificationsActive to CoralRecordingRed
        EventType.TEXT_INPUT -> Icons.Default.Keyboard to AmberWarning
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) ElectricPurple.copy(alpha = 0.2f) else DarkSurface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.description,
                color = TextPrimary,
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${event.appName} • ${event.eventType.name}",
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = event.timeFormatted,
                color = CyanAccent,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
