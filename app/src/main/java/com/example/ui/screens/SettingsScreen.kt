package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingRepository
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: RecordingRepository,
    modifier: Modifier = Modifier
) {
    val config by repository.config.collectAsState()
    val cloudStats by repository.cloudStats.collectAsState()
    val context = LocalContext.current

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
            // Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECORDING & CLOUD CONFIG",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Quality & Capture Settings Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "VIDEO CAPTURE QUALITY",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Resolution Choice
                        Text("Resolution", color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("720p", "1080p", "4K").forEach { res ->
                                FilterChip(
                                    selected = config.resolution == res,
                                    onClick = { repository.updateConfig(config.copy(resolution = res)) },
                                    label = { Text(res, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricPurple.copy(alpha = 0.2f),
                                        selectedLabelColor = ElectricPurple
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // FPS Choice
                        Text("Frame Rate (FPS)", color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 120).forEach { fpsVal ->
                                FilterChip(
                                    selected = config.fps == fpsVal,
                                    onClick = { repository.updateConfig(config.copy(fps = fpsVal)) },
                                    label = { Text("$fpsVal FPS", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                        selectedLabelColor = CyanAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bitrate Choice
                        Text("Bitrate", color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(4, 8, 16, 24).forEach { mbps ->
                                FilterChip(
                                    selected = config.bitrateMbps == mbps,
                                    onClick = { repository.updateConfig(config.copy(bitrateMbps = mbps)) },
                                    label = { Text("$mbps Mbps", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Audio Source Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AUDIO CAPTURE SOURCE",
                            color = ElectricPurple,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        listOf("Combined System + Mic", "System Sound Only", "Microphone Only", "Mute").forEach { src ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { repository.updateConfig(config.copy(audioSource = src)) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(src, color = TextPrimary, fontSize = 13.sp)
                                RadioButton(
                                    selected = config.audioSource == src,
                                    onClick = { repository.updateConfig(config.copy(audioSource = src)) },
                                    colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple)
                                )
                            }
                        }
                    }
                }
            }

            // Cloud Vault & Security Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CLOUD STORAGE & SECURITY VAULT",
                            color = EmeraldSuccess,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Cloud Upload", color = TextPrimary, fontSize = 13.sp)
                                Text("Automatically sync recordings to Cloud Vault", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = config.autoCloudSync,
                                onCheckedChange = { repository.updateConfig(config.copy(autoCloudSync = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AES-256 Cloud Encryption", color = TextPrimary, fontSize = 13.sp)
                                Text("Client-side key before uploading to Cloud", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = config.encryptCloudStorage,
                                onCheckedChange = { repository.updateConfig(config.copy(encryptCloudStorage = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                        Text("Cloudflare Worker Stream Endpoint", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = config.cloudWorkerEndpoint,
                                color = CyanAccent,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Excluded Apps & Privacy Shield
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SENSITIVE APP PRIVACY SHIELD", color = AmberWarning, fontSize = 12.sp, style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Automatically pause screen recording when opening protected apps:", color = TextMuted, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        listOf("Banking & Financial Apps", "Password Managers", "Authenticator Apps").forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(app, color = TextPrimary, fontSize = 12.sp)
                                Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = AmberWarning))
                            }
                        }
                    }
                }
            }
        }
    }
}
