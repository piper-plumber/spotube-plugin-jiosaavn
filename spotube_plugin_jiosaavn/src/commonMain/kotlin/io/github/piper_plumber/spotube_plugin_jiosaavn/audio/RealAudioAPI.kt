package io.github.piper_plumber.spotube_plugin_jiosaavn.audio

import dev.krtirtho.plugin_interfaces.host_apis.CryptoAPI
import dev.krtirtho.plugin_interfaces.host_apis.LegacyCipherAlgorithms
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioFormat
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioQuality
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioSource
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioStream
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song
import kotlin.io.encoding.Base64


class RealAudioAPI(private val client: Jiosaavn, private val crypto: CryptoAPI) : AudioAPI {
    companion object {
        //        private val jioSaavnIdRegex = """^[a-zA-Z0-9-]{8}$""".toRegex()
        private const val key = "38346591"
        private val bitrates = listOf(320, 160, 96, 48, 12)
//        private fun isJioSaavnId(id: String): Boolean = jioSaavnIdRegex.matches(id)
    }

    private fun buildUrlsFromBitrates(decryptedUrl: String): List<AudioStream> {
        return bitrates.map {
            AudioStream.Lossy(
                container = "mp4",
                codec = "aac",
                bitrate = it,
                url = decryptedUrl.replace("_96", "_${it}")
            )
        }
    }

    private suspend fun decryptMediaUrl(encryptedUrl: String): List<AudioStream> {
        val decodedUrlBytes = Base64.decode(encryptedUrl)
        val keyBytes = key.encodeToByteArray()
        val decryptedUrl =
            crypto.decryptLegacy(LegacyCipherAlgorithms.DES(), keyBytes, decodedUrlBytes)
                .decodeToString()

        return buildUrlsFromBitrates(decryptedUrl)
    }

    private suspend fun transform(
        track: MetadataTrack, song: Song
    ): AudioSource.Streamed {
        val primaryArtists =
            song.moreInfo.artistMap?.primaryArtists?.joinToString(", ") { it.name } ?: ""
        val featuredArtists =
            song.moreInfo.artistMap?.featuredArtists?.joinToString(", ") { it.name } ?: ""
        var confidence = 0f
        if (track.title.lowercase() in song.title.lowercase()) confidence += 0.3f
        if (track.artists.any {
                it.name.lowercase() in primaryArtists.lowercase() || it.name.lowercase() in featuredArtists.lowercase()
            }) confidence += 0.3f
        if ((track.album != null && track.album!!.title.lowercase() in (song.moreInfo.album
                ?: "").lowercase()) ||
            primaryArtists.lowercase() in (track.artists.firstOrNull()?.name?.lowercase()
                ?: "")
        ) confidence += 0.2f
        val duration = song.moreInfo.duration?.toIntOrNull()?.let { it * 1000 } ?: 0
        if (duration in (track.durationMs - 30_000)..(track.durationMs + 30_000)) confidence += 0.2f

        return AudioSource.Streamed(
            id = IdTokenPair.asString(song.id, song.permaUrl),
            title = song.title,
            artist = primaryArtists,
            album = null,
            thumbnails = listOf(
                Thumbnail(
                    url = song.image,
                    width = 300,
                    height = 300,
                )
            ),
            externalUri = "https://www.youtube.com/watch?v=${song.id}",
            confidence = confidence,
            streams = decryptMediaUrl(song.moreInfo.encryptedMediaUrl ?: "")
        )
    }

    private suspend fun getStreams(songId: String): List<AudioSource.Streamed> {
        val songResult = client.webapi.getSong(token = IdTokenPair.token(songId))
        val primaryArtists =
            songResult.moreInfo.artistMap?.primaryArtists?.joinToString(", ") { it.name } ?: ""
        val encryptedMediaUrl = songResult.moreInfo.encryptedMediaUrl ?: ""

        return listOf(
            AudioSource.Streamed(
                id = songResult.id,
                title = songResult.title,
                artist = primaryArtists,
                album = songResult.moreInfo.album ?: "",
                thumbnails = emptyList(),
                externalUri = songResult.permaUrl,
                confidence = 1.0f,
                streams = decryptMediaUrl(encryptedMediaUrl)
            )
        )
    }

    override val supportedQualities: List<AudioFormat> = listOf(
        AudioFormat(
            codec = "aac",
            container = "mp4",
            qualities = bitrates.map { bitrate -> AudioQuality.Lossy(bitrate = bitrate) }
        )
    )

    override suspend fun getStreamsByTrack(track: MetadataTrack): List<AudioSource> {
        val isJioSaavnId = IdTokenPair.isValidFormat(track.id)

        if (isJioSaavnId) {
            val streams = getStreams(track.id)
            if (streams.isNotEmpty()) return streams
        }

        val query = track.title + " - " + track.artists.joinToString(separator = ", ") { it.name }
        val searchResults = client.search.getResults(query, p = 0, n = 20)
        val songs = searchResults.results.map { transform(track, it) }

        return songs.sortedByDescending { it.confidence }.take(5)
    }

    override suspend fun getStreamsOfAudioSource(source: AudioSource.Basic): List<AudioSource.Streamed> {
        return getStreams(source.id)
    }
}
