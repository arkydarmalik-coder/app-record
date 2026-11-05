package com.example.data

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class RecordingRepository(
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val _recorderState = MutableStateFlow(RecorderState.IDLE)
    val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    private val _activeRecordingDuration = MutableStateFlow(0)
    val activeRecordingDuration: StateFlow<Int> = _activeRecordingDuration.asStateFlow()

    private val _recordingsList = MutableStateFlow<List<RecordingSession>>(emptyList())
    val recordingsList: StateFlow<List<RecordingSession>> = _recordingsList.asStateFlow()

    private val _selectedRecording = MutableStateFlow<RecordingSession?>(null)
    val selectedRecording: StateFlow<RecordingSession?> = _selectedRecording.asStateFlow()

    private val _config = MutableStateFlow(RecordingConfig())
    val config: StateFlow<RecordingConfig> = _config.asStateFlow()

    private val _cloudStats = MutableStateFlow(CloudVaultStats())
    val cloudStats: StateFlow<CloudVaultStats> = _cloudStats.asStateFlow()

    // Real-time telemetry monitoring
    private val _liveFps = MutableStateFlow(60)
    val liveFps: StateFlow<Int> = _liveFps.asStateFlow()

    private val _liveCpuUsage = MutableStateFlow(18)
    val liveCpuUsage: StateFlow<Int> = _liveCpuUsage.asStateFlow()

    private val _liveAudioLevel = MutableStateFlow(0.42f)
    val liveAudioLevel: StateFlow<Float> = _liveAudioLevel.asStateFlow()

    private val _liveTouchedPoints = MutableStateFlow(0)
    val liveTouchedPoints: StateFlow<Int> = _liveTouchedPoints.asStateFlow()

    // Live Click History Stream
    private val _clickHistoryList = MutableStateFlow<List<ClickEventLog>>(emptyList())
    val clickHistoryList: StateFlow<List<ClickEventLog>> = _clickHistoryList.asStateFlow()

    private var recordingJob: Job? = null
    private var telemetryJob: Job? = null
    private var currentActiveEvents = mutableListOf<ActivityEvent>()
    private var currentActiveClicks = mutableListOf<ClickEventLog>()

    init {
        loadMockClickHistory()
        loadMockRecordings()
        startTelemetryLoop()
    }

    private fun loadMockClickHistory() {
        val sampleClicks = listOf(
            ClickEventLog(
                id = "clk_101",
                timestampMs = 1200L,
                timeFormatted = "00:01.200",
                xRatio = 0.485f,
                yRatio = 0.820f,
                pixelX = 524,
                pixelY = 1770,
                gestureType = "SINGLE_TAP",
                targetElementId = "btn_start_recording",
                appName = "App Screen Recording"
            ),
            ClickEventLog(
                id = "clk_102",
                timestampMs = 5400L,
                timeFormatted = "00:05.400",
                xRatio = 0.250f,
                yRatio = 0.350f,
                pixelX = 270,
                pixelY = 756,
                gestureType = "SINGLE_TAP",
                targetElementId = "item_card_shopping",
                appName = "ShoppingApp"
            ),
            ClickEventLog(
                id = "clk_103",
                timestampMs = 14200L,
                timeFormatted = "00:14.200",
                xRatio = 0.880f,
                yRatio = 0.120f,
                pixelX = 950,
                pixelY = 260,
                gestureType = "LONG_PRESS",
                targetElementId = "btn_add_to_cart",
                appName = "ShoppingApp"
            ),
            ClickEventLog(
                id = "clk_104",
                timestampMs = 28900L,
                timeFormatted = "00:28.900",
                xRatio = 0.500f,
                yRatio = 0.900f,
                pixelX = 540,
                pixelY = 1944,
                gestureType = "DOUBLE_TAP",
                targetElementId = "fab_cloud_upload",
                appName = "Cloud Vault"
            ),
            ClickEventLog(
                id = "clk_105",
                timestampMs = 42100L,
                timeFormatted = "00:42.100",
                xRatio = 0.150f,
                yRatio = 0.650f,
                pixelX = 162,
                pixelY = 1404,
                gestureType = "SWIPE_HORIZONTAL",
                targetElementId = "carousel_item_2",
                appName = "Media Gallery"
            )
        )
        _clickHistoryList.value = sampleClicks
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = externalScope.launch {
            while (isActive) {
                delay(800)
                if (_recorderState.value == RecorderState.RECORDING) {
                    _liveFps.value = Random.nextInt(58, 61)
                    _liveCpuUsage.value = Random.nextInt(24, 45)
                    _liveAudioLevel.value = Random.nextFloat() * 0.85f + 0.15f
                    _liveTouchedPoints.value += Random.nextInt(0, 3)
                } else {
                    _liveFps.value = 60
                    _liveCpuUsage.value = Random.nextInt(12, 22)
                    _liveAudioLevel.value = 0.05f
                }
            }
        }
    }

    private fun loadMockRecordings() {
        val clicks = _clickHistoryList.value
        val sampleRecordings = listOf(
            RecordingSession(
                id = "rec_001",
                title = "App Screen Activity - E-Commerce Demo",
                timestamp = "Today, 14:22",
                durationSeconds = 245,
                resolution = "1080p @ 60 FPS",
                fileSizeMB = 184.2f,
                thumbnailPath = "/app/src/main/res/drawable/screen_rec_hero_1793869801044.jpg",
                cloudStatus = CloudSyncStatus.SYNCED,
                cloudShareUrl = "https://apprecording.workers.dev/v1/play?id=rec_001",
                appName = "ShoppingApp",
                tags = listOf("Product Checkout", "UI Test", "Encrypted"),
                isEncrypted = true,
                events = listOf(
                    ActivityEvent("ev1", 5000, "00:05", EventType.APP_SWITCH, "Launched ShoppingApp", "ShoppingApp"),
                    ActivityEvent("ev2", 24000, "00:24", EventType.TOUCH_HEATMAP, "Tapped 'Add to Cart' button", "ShoppingApp"),
                    ActivityEvent("ev3", 68000, "01:08", EventType.AUDIO_PEAK, "Voice note recorded during review", "Mic"),
                    ActivityEvent("ev4", 145000, "02:25", EventType.TEXT_INPUT, "Entered promo code 'CYBER20'", "Keyboard"),
                    ActivityEvent("ev5", 210000, "03:30", EventType.SYSTEM_ALERT, "Payment success notification", "System")
                ),
                clickHistory = clicks
            ),
            RecordingSession(
                id = "rec_002",
                title = "Gaming Benchmark & Touch Heatmap",
                timestamp = "Yesterday, 19:40",
                durationSeconds = 512,
                resolution = "1080p @ 60 FPS",
                fileSizeMB = 412.0f,
                thumbnailPath = "",
                cloudStatus = CloudSyncStatus.SYNCED,
                cloudShareUrl = "https://apprecording.workers.dev/v1/play?id=rec_002",
                appName = "CyberRacer 3D",
                tags = listOf("Gaming", "High FPS", "60fps"),
                isEncrypted = true,
                events = listOf(
                    ActivityEvent("ev2_1", 2000, "00:02", EventType.APP_SWITCH, "Opened CyberRacer 3D", "CyberRacer 3D"),
                    ActivityEvent("ev2_2", 120000, "02:00", EventType.TOUCH_HEATMAP, "Steering gestures burst detected", "CyberRacer 3D"),
                    ActivityEvent("ev2_3", 340000, "05:40", EventType.AUDIO_PEAK, "In-game engine sound peak", "AudioEngine")
                ),
                clickHistory = clicks.take(3)
            )
        )
        _recordingsList.value = sampleRecordings
        _selectedRecording.value = sampleRecordings.firstOrNull()
    }

    fun logUserClick(xRatio: Float, yRatio: Float, gesture: String = "SINGLE_TAP", targetId: String = "ui_touch_target") {
        val nowMs = System.currentTimeMillis()
        val pixelX = (xRatio * 1080f).toInt()
        val pixelY = (yRatio * 2160f).toInt()
        val timeFormatted = SimpleDateFormat("mm:ss.SSS", Locale.getDefault()).format(Date(nowMs))

        val newClick = ClickEventLog(
            id = "clk_${nowMs}",
            timestampMs = nowMs,
            timeFormatted = timeFormatted,
            xRatio = xRatio,
            yRatio = yRatio,
            pixelX = pixelX,
            pixelY = pixelY,
            gestureType = gesture,
            targetElementId = targetId,
            appName = "App Screen Recording"
        )

        _clickHistoryList.value = listOf(newClick) + _clickHistoryList.value
        currentActiveClicks.add(newClick)
        _liveTouchedPoints.value += 1
    }

    fun clearClickHistory() {
        _clickHistoryList.value = emptyList()
    }

    fun startRecording() {
        if (_recorderState.value != RecorderState.IDLE) return
        _recorderState.value = RecorderState.RECORDING
        _activeRecordingDuration.value = 0
        _liveTouchedPoints.value = 0
        currentActiveEvents.clear()
        currentActiveClicks.clear()

        val startTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        currentActiveEvents.add(
            ActivityEvent("live_1", 0, "00:00", EventType.APP_SWITCH, "Recording started at $startTime", "App Screen Recorder")
        )

        recordingJob?.cancel()
        recordingJob = externalScope.launch {
            while (isActive && _recorderState.value == RecorderState.RECORDING) {
                delay(1000)
                _activeRecordingDuration.value += 1
                val duration = _activeRecordingDuration.value

                // Periodically insert live click events into stream
                if (duration % 8 == 0) {
                    val x = Random.nextFloat()
                    val y = Random.nextFloat()
                    logUserClick(x, y, gesture = if (duration % 16 == 0) "DOUBLE_TAP" else "SINGLE_TAP", targetId = "auto_logged_component_$duration")
                }
            }
        }
    }

    fun pauseRecording() {
        if (_recorderState.value == RecorderState.RECORDING) {
            _recorderState.value = RecorderState.PAUSED
        }
    }

    fun resumeRecording() {
        if (_recorderState.value == RecorderState.PAUSED) {
            _recorderState.value = RecorderState.RECORDING
        }
    }

    fun stopRecording() {
        if (_recorderState.value == RecorderState.IDLE) return
        recordingJob?.cancel()
        _recorderState.value = RecorderState.PROCESSING

        externalScope.launch {
            delay(1200)
            val duration = _activeRecordingDuration.value
            val sizeMB = (duration * 0.75f).coerceAtLeast(4.5f)
            val newId = "rec_${System.currentTimeMillis()}"
            val timeStampStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())

            val newSession = RecordingSession(
                id = newId,
                title = "App Screen Recording # ${Random.nextInt(100, 999)}",
                timestamp = timeStampStr,
                durationSeconds = duration,
                resolution = "${_config.value.resolution} @ ${_config.value.fps} FPS",
                fileSizeMB = sizeMB,
                cloudStatus = if (_config.value.autoCloudSync) CloudSyncStatus.UPLOADING else CloudSyncStatus.LOCAL_ONLY,
                cloudShareUrl = if (_config.value.autoCloudSync) "https://apprecording.workers.dev/v1/play?id=$newId" else "",
                appName = "App Screen Recording",
                tags = listOf("Live Recording", "Click Logged"),
                isEncrypted = _config.value.encryptCloudStorage,
                events = ArrayList(currentActiveEvents),
                clickHistory = ArrayList(currentActiveClicks),
                bitrate = "${_config.value.bitrateMbps} Mbps",
                audioSource = _config.value.audioSource
            )

            val updatedList = listOf(newSession) + _recordingsList.value
            _recordingsList.value = updatedList
            _selectedRecording.value = newSession
            _recorderState.value = RecorderState.IDLE

            if (_config.value.autoCloudSync) {
                simulateCloudUpload(newId)
            }
        }
    }

    fun selectRecording(session: RecordingSession) {
        _selectedRecording.value = session
    }

    fun syncRecordingToCloud(id: String) {
        val currentList = _recordingsList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(cloudStatus = CloudSyncStatus.UPLOADING)
            _recordingsList.value = currentList
            simulateCloudUpload(id)
        }
    }

    private fun simulateCloudUpload(id: String) {
        externalScope.launch {
            delay(2000)
            val currentList = _recordingsList.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                currentList[index] = currentList[index].copy(
                    cloudStatus = CloudSyncStatus.SYNCED,
                    cloudShareUrl = "https://apprecording.workers.dev/v1/play?id=$id"
                )
                _recordingsList.value = currentList

                val stats = _cloudStats.value
                val newUsed = (stats.usedStorageGB + (currentList[index].fileSizeMB / 1024f)).coerceAtMost(stats.totalStorageGB)
                _cloudStats.value = stats.copy(
                    usedStorageGB = newUsed,
                    cloudSyncedCount = stats.cloudSyncedCount + 1
                )
            }
        }
    }

    fun syncAllToCloud() {
        _recordingsList.value.forEach { session ->
            if (session.cloudStatus == CloudSyncStatus.LOCAL_ONLY) {
                syncRecordingToCloud(session.id)
            }
        }
    }

    fun deleteRecording(id: String) {
        val updated = _recordingsList.value.filter { it.id != id }
        _recordingsList.value = updated
        if (_selectedRecording.value?.id == id) {
            _selectedRecording.value = updated.firstOrNull()
        }
    }

    fun trimRecording(id: String, startMs: Long, endMs: Long) {
        val currentList = _recordingsList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val original = currentList[index]
            val newDurationSecs = ((endMs - startMs) / 1000L).toInt().coerceAtLeast(1)
            val trimmedEvents = original.events.filter { it.timestampMs in startMs..endMs }
            val trimmedClicks = original.clickHistory.filter { it.timestampMs in startMs..endMs }
            val trimmedSession = original.copy(
                title = "${original.title} (Trimmed)",
                durationSeconds = newDurationSecs,
                fileSizeMB = (original.fileSizeMB * (newDurationSecs.toFloat() / original.durationSeconds.coerceAtLeast(1))).coerceAtLeast(1.0f),
                events = trimmedEvents,
                clickHistory = trimmedClicks
            )
            currentList[index] = trimmedSession
            _recordingsList.value = currentList
            _selectedRecording.value = trimmedSession
        }
    }

    fun updateConfig(newConfig: RecordingConfig) {
        _config.value = newConfig
    }
}
