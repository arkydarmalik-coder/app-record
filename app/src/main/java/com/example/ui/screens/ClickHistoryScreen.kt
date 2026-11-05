package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingRepository
import com.example.model.ClickEventLog
import com.example.model.toPrettyJsonString
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickHistoryScreen(
    repository: RecordingRepository,
    modifier: Modifier = Modifier
) {
    val clickHistory by repository.clickHistoryList.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showJsonModal by remember { mutableStateOf(false) }
    var selectedClickForJson by remember { mutableStateOf<ClickEventLog?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredClicks = remember(clickHistory, searchQuery) {
        if (searchQuery.isEmpty()) clickHistory
        else clickHistory.filter {
            it.targetElementId.contains(searchQuery, ignoreCase = true) ||
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.gestureType.contains(searchQuery, ignoreCase = true)
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
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "USER CLICK SOURCE JSON",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            text = "Click History Logger & Telemetry JSON Stream",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            val jsonPayload = clickHistory.toPrettyJsonString()
                            clipboardManager.setText(AnnotatedString(jsonPayload))
                            Toast.makeText(context, "Full Click Stream JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON", color = Color.Black, fontSize = 11.sp)
                    }
                }
            }

            // Interactive Click Recorder Surface / Canvas
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(EmeraldSuccess, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TOUCH HEATMAP CANVAS", color = TextPrimary, fontSize = 12.sp, style = MaterialTheme.typography.titleSmall)
                            }
                            Text("Tap canvas to record click", color = CyanAccent, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Touch Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF030712))
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val xRatio = offset.x / size.width
                                        val yRatio = offset.y / size.height
                                        repository.logUserClick(xRatio, yRatio, "SINGLE_TAP", "touch_canvas_target")
                                        Toast
                                            .makeText(context, "Click recorded: (${(xRatio * 1080).toInt()}, ${(yRatio * 2160).toInt()})", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw simulated device frame
                                drawRoundRect(
                                    color = ElectricPurple.copy(alpha = 0.2f),
                                    topLeft = Offset(w * 0.1f, h * 0.1f),
                                    size = Size(w * 0.8f, h * 0.8f),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )

                                // Render recorded click touch dots
                                clickHistory.take(12).forEach { clk ->
                                    val cx = (w * 0.1f) + (clk.xRatio * w * 0.8f)
                                    val cy = (h * 0.1f) + (clk.yRatio * h * 0.8f)

                                    drawCircle(
                                        color = CyanAccent.copy(alpha = 0.4f),
                                        radius = 16f,
                                        center = Offset(cx, cy)
                                    )
                                    drawCircle(
                                        color = EmeraldSuccess,
                                        radius = 5f,
                                        center = Offset(cx, cy)
                                    )
                                }
                            }

                            Text(
                                text = "Interactive Canvas • Tap anywhere to generate JSON event",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Click Statistics Summary Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TOTAL CLICKS", color = TextMuted, fontSize = 10.sp)
                            Text("${clickHistory.size}", color = TextPrimary, fontSize = 20.sp, style = MaterialTheme.typography.titleLarge)
                            Text("Logged events", color = CyanAccent, fontSize = 10.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("JSON FORMAT", color = TextMuted, fontSize = 10.sp)
                            Text("Pristine", color = EmeraldSuccess, fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
                            Text("UTF-8 Schema", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = { showJsonModal = true },
                        modifier = Modifier.weight(1.2f).height(68.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("View Full JSON", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Search Filter & Clear Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLICK EVENT LOG HISTORY (${filteredClicks.size})",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelLarge
                    )

                    TextButton(onClick = { repository.clearClickHistory() }) {
                        Text("Clear Logs", color = CoralRecordingRed, fontSize = 11.sp)
                    }
                }
            }

            // List of Click Events
            if (filteredClicks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(DarkSurface, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No click events logged yet. Tap screen to generate.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredClicks) { clk ->
                    ClickEventRowItem(
                        clickEvent = clk,
                        onViewJson = {
                            selectedClickForJson = clk
                        },
                        onCopyJson = {
                            clipboardManager.setText(AnnotatedString(clk.toJsonString()))
                            Toast.makeText(context, "Click event JSON copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Full JSON Inspector Dialog Modal
        if (showJsonModal || selectedClickForJson != null) {
            AlertDialog(
                onDismissRequest = {
                    showJsonModal = false
                    selectedClickForJson = null
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SOURCE JSON USER CLICK", color = CyanAccent, fontSize = 14.sp)
                        IconButton(onClick = {
                            showJsonModal = false
                            selectedClickForJson = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }
                },
                text = {
                    val jsonText = selectedClickForJson?.toJsonString() ?: clickHistory.toPrettyJsonString()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF030712))
                            .padding(12.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = jsonText,
                                    color = EmeraldSuccess,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val jsonText = selectedClickForJson?.toJsonString() ?: clickHistory.toPrettyJsonString()
                            clipboardManager.setText(AnnotatedString(jsonText))
                            Toast.makeText(context, "JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showJsonModal = false
                            selectedClickForJson = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy JSON", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showJsonModal = false
                        selectedClickForJson = null
                    }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun ClickEventRowItem(
    clickEvent: ClickEventLog,
    onViewJson: () -> Unit,
    onCopyJson: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = clickEvent.targetElementId,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${clickEvent.appName} • ${clickEvent.gestureType}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(color = ElectricPurple.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "X:${clickEvent.pixelX} Y:${clickEvent.pixelY}",
                        color = ElectricPurple,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time: ${clickEvent.timeFormatted}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onViewJson() }
                    ) {
                        Text("View JSON", color = CyanAccent, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    Surface(
                        color = CyanAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onCopyJson() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy JSON", color = CyanAccent, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
