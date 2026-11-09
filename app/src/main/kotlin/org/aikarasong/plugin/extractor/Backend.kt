package org.aikarasong.plugin.extractor

import kotlinx.serialization.Serializable

/** What the plug-in can do for the main app. One implementation talks to YouTube ([NewPipeBackend]). */
interface Backend {
    fun searchPage(
        query: String,
        cursor: String?,
    ): PageDto

    fun resolveStream(videoId: String): StreamDto

    fun details(videoId: String): DetailsDto

    fun playlist(playlistId: String): List<VideoDto>
}

@Serializable
data class VideoDto(
    val id: String,
    val title: String,
    val channel: String,
    val durationSec: Int,
    val thumb: String? = null,
    val views: Long = -1,
)

/** [next] is a token the plug-in understands (null at the end of the list). */
@Serializable
data class PageDto(val videos: List<VideoDto>, val next: String? = null)

@Serializable
data class StreamDto(val videoUrl: String, val audioUrl: String? = null)

@Serializable
data class FormatDto(val ext: String, val url: String)

@Serializable
data class TrackDto(val lang: String, val formats: List<FormatDto>)

/** [autoLangs] are the languages of the auto-generated captions, [allLangs] those of every caption track. */
@Serializable
data class DetailsDto(
    val tracks: List<TrackDto>,
    val description: String,
    val autoLangs: List<String>,
    val allLangs: List<String>,
)
