package com.example.model

enum class RecorderState {
    IDLE, RECORDING, PAUSED, PROCESSING
}

enum class CloudSyncStatus {
    LOCAL_ONLY, UPLOADING, SYNCED, ERROR
}

enum class EventType {
    APP_SWITCH, TOUCH_HEATMAP, AUDIO_PEAK, SYSTEM_ALERT, TEXT_INPUT
}

data class ActivityEvent(
    val id: String,
    val timestampMs: Long,
    val timeFormatted: String,
    val eventType: EventType,
    val description: String,
    val appName: String,
    val iconName: String = "ic_default"
)

data class ClickEventLog(
    val id: String,
    val timestampMs: Long,
    val timeFormatted: String,
    val xRatio: Float,
    val yRatio: Float,
    val pixelX: Int,
    val pixelY: Int,
    val gestureType: String = "SINGLE_TAP",
    val targetElementId: String = "btn_action_target",
    val appPackage: String = "com.aistudio.screenrecording",
    val appName: String = "App Screen Recording",
    val pressure: Float = 1.0f,
    val area: Float = 0.05f
) {
    fun toJsonString(): String {
        return """{
  "event_id": "$id",
  "timestamp_ms": $timestampMs,
  "time_formatted": "$timeFormatted",
  "coordinates": {
    "x_pixel": $pixelX,
    "y_pixel": $pixelY,
    "x_ratio": ${String.format("%.3f", xRatio)},
    "y_ratio": ${String.format("%.3f", yRatio)}
  },
  "gesture": "$gestureType",
  "target_component": "$targetElementId",
  "package_name": "$appPackage",
  "app_name": "$appName",
  "touch_pressure": $pressure,
  "touch_area": $area
}"""
    }
}

fun List<ClickEventLog>.toPrettyJsonString(): String {
    val itemsJson = joinToString(",\n") { "    " + it.toJsonString().replace("\n", "\n    ") }
    return """{
  "click_stream_export": {
    "total_click_events": ${size},
    "exported_at": "${System.currentTimeMillis()}",
    "events": [
$itemsJson
    ]
  }
}"""
}

data class RecordingSession(
    val id: String,
    val title: String,
    val timestamp: String,
    val durationSeconds: Int,
    val resolution: String = "1080p @ 60 FPS",
    val fileSizeMB: Float,
    val thumbnailPath: String = "",
    val cloudStatus: CloudSyncStatus = CloudSyncStatus.LOCAL_ONLY,
    val cloudShareUrl: String = "",
    val appName: String = "System Launcher",
    val tags: List<String> = listOf("Screen Activity", "Auto-Log"),
    val isEncrypted: Boolean = true,
    val events: List<ActivityEvent> = emptyList(),
    val clickHistory: List<ClickEventLog> = emptyList(),
    val bitrate: String = "8 Mbps",
    val audioSource: String = "Combined System + Mic"
) {
    val durationFormatted: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

data class RecordingConfig(
    val fps: Int = 60,
    val resolution: String = "1080p",
    val bitrateMbps: Int = 8,
    val audioSource: String = "Combined System + Mic",
    val showTouchVisualizer: Boolean = true,
    val floatingOverlay: Boolean = true,
    val autoCloudSync: Boolean = true,
    val encryptCloudStorage: Boolean = true,
    val cloudWorkerEndpoint: String = "https://apprecording.workers.dev/v1/vault"
)

data class CloudVaultStats(
    val totalStorageGB: Float = 15.0f,
    val usedStorageGB: Float = 4.8f,
    val totalRecordingsCount: Int = 12,
    val cloudSyncedCount: Int = 9,
    val encryptionKeyStatus: String = "AES-256 Cloud Encrypted",
    val workerStatus: String = "Connected (Cloudflare Worker v2.4)"
)
