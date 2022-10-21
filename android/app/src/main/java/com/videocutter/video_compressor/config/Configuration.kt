package com.videocutter.video_compressor.config

import com.videocutter.video_compressor.VideoQuality

data class Configuration(
    var quality: VideoQuality = VideoQuality.MEDIUM,
    var frameRate: Int? = null,
    var isMinBitrateCheckEnabled: Boolean = true,
    var videoBitrate: Int? = null,
    var disableAudio: Boolean = false,
    val keepOriginalResolution: Boolean = false,
    var videoHeight: Double? = null,
    var videoWidth: Double? = null
)
