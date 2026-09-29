package io.github.piper_plumber.spotube_plugin_jiosaavn.audio

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.JVMCryptoAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

class RealAudioAPITest {
    val client = RealAudioAPI(TestFixture.jiosaavn, JVMCryptoAPI())

    @Test
    fun `test getAudioSources`() = runTest {
        val audioSources = client.getStreamsByTrack(
            MetadataTrack(
                id = "JkxStHZV@@OgMTYgB4bWU",
                title = "Level of Concern",
                durationMs = 220 * 1000,
                trackNumber = null,
                discNumber = null,
                artists = listOf(
                    MetadataArtist.Basic(
                        id = "720746@@Dx5Vty3hXGs_",
                        name = "Twenty One Pilots",
                        externalUri = "https://www.jiosaavn.com/artist/twenty-one-pilots-songs/Dx5Vty3hXGs_",
                        thumbnails = emptyList(),
                    )
                ),
                album = null,
                thumbnails = emptyList(),
                explicit = false,
                popularity = null,
                isrcCode = null,
                externalUri = "https://www.jiosaavn.com/song/level-of-concern/OgMTYgB4bWU"
            )
        )
        assertTrue(audioSources.isNotEmpty(), "Audio sources should not be empty")
        audioSources.forEach { source ->
            assertTrue(source.id.isNotBlank(), "Audio source ID should not be blank")
        }
    }
}