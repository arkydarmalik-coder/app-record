package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LiveTelemetryCard(
    fps: Int,
    cpuUsage: Int,
    touchedPoints: Int,
    resolution: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(EmeraldSuccess, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REAL-TIME HARDWARE MONITOR",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = resolution,
                    fontSize = 12.sp,
                    color = CyanAccent,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryItem(
                    icon = Icons.Default.Speed,
                    label = "FRAME RATE",
                    value = "$fps FPS",
                    color = ElectricPurple,
                    modifier = Modifier.weight(1f)
                )
                TelemetryItem(
                    icon = Icons.Default.Memory,
                    label = "CPU USAGE",
                    value = "$cpuUsage %",
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                TelemetryItem(
                    icon = Icons.Default.TouchApp,
                    label = "TOUCH EVENTS",
                    value = "$touchedPoints pts",
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
