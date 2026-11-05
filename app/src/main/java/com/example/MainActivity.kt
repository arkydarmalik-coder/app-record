package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingRepository
import com.example.model.RecordingSession
import com.example.ui.screens.ClickHistoryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlaybackScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.AppScreenRecordingTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.TextMuted

enum class AppTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Record", Icons.Default.Videocam),
    CLICK_JSON("Click JSON", Icons.Default.Code),
    VAULT("Vault", Icons.Default.Cloud),
    PLAYBACK("Playback", Icons.Default.PlayCircle),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val repository = RecordingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppScreenRecordingTheme {
                MainContent(repository)
            }
        }
    }
}

@Composable
private fun MainContent(repository: RecordingRepository) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }

    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        currentTab = AppTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selected) ElectricPurple else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                color = if (selected) ElectricPurple else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ElectricPurple.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    repository = repository,
                    onNavigateToPlayback = { session ->
                        repository.selectRecording(session)
                        currentTab = AppTab.PLAYBACK
                    },
                    onNavigateToVault = {
                        currentTab = AppTab.VAULT
                    }
                )

                AppTab.CLICK_JSON -> ClickHistoryScreen(
                    repository = repository
                )

                AppTab.VAULT -> VaultScreen(
                    repository = repository,
                    onNavigateToPlayback = { session ->
                        repository.selectRecording(session)
                        currentTab = AppTab.PLAYBACK
                    }
                )

                AppTab.PLAYBACK -> PlaybackScreen(
                    repository = repository,
                    onBack = { currentTab = AppTab.DASHBOARD }
                )

                AppTab.SETTINGS -> SettingsScreen(
                    repository = repository
                )
            }
        }
    }
}
