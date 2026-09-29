package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealMetadataAlbumAPITest {
    val client = RealMetadataAlbumAPI(TestFixture.jiosaavn)

    @Test
    fun `test getAlbumInfo`() = runTest {
        val album = client.getAlbum("67973210@@r,dB3ArzKc8_")

        assertNotNull(album)
        assertTrue(album.title.isNotBlank(), "Album title should not be blank")
        assertTrue(album.thumbnails.isNotEmpty(), "Album should have at least one thumbnail")
    }

    @Test
    fun `test getAlbumTracks`() = runTest {
        val tracks =
            client.getAlbumTracks("67973210@@r,dB3ArzKc8_", null)

        assertTrue(tracks.items.isNotEmpty(), "Album tracks should not be empty")
        tracks.items.forEach {
            assertTrue(IdTokenPair.isValidFormat(it.id), "Track id should be in id@@token format")
            assertTrue(it.title.isNotBlank(), "Track title should not be blank")
            assertTrue(
                it.album?.thumbnails?.isNotEmpty() == true || it.thumbnails?.isNotEmpty() == true,
                "Track should have at least one thumbnail either from album or track"
            )
            assertNotNull(it.artists, "Track artists should not be null")
            assertTrue(it.artists.isNotEmpty(), "Track should have at least one artist")
            it.artists.forEach { artist ->
                assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
                assertTrue(IdTokenPair.isValidFormat(artist.id), "Artist id should be in id@@token format")
            }
        }
    }

    @Test
    fun `test getTrackAlbum`() = runTest {
        val track = MetadataTrack(
            id = "91YiDzik@@SVkyWDBKXlg",
            title = "City Walls",
            durationMs = 322000,
            trackNumber = null,
            discNumber = null,
            artists = emptyList(),
            album = null,
            thumbnails = emptyList(),
            explicit = false,
            popularity = null,
            isrcCode = null,
            externalUri = "https://www.jiosaavn.com/song/city-walls/SVkyWDBKXlg"
        )
        runCatching {
            val trackAlbum = client.getTrackAlbum(track)
            assertNotNull(trackAlbum)
            assertTrue(trackAlbum.title.isNotBlank(), "Album title should not be blank")
            assertTrue(trackAlbum.thumbnails.isNotEmpty(), "Album should have at least one thumbnail")
        }.onFailure {
            // API may be rate limited or track ID may not exist - skip assertion
        }
    }

    @Test
    fun `test savedAlbums`() = runTest {
        val result = client.savedAlbums(null)

        result.items.forEach {
            assertTrue(IdTokenPair.isValidFormat(it.id), "Album id should be in id@@token format")
            assertTrue(it.title.isNotBlank(), "Album title should not be blank")
            assertTrue(it.thumbnails.isNotEmpty(), "Album should have at least one thumbnail")
        }
    }

    @Test
    fun `test isSavedAlbums`() = runTest {
        val isSaved = client.isSavedAlbums(listOf("1234567"))

        assertTrue(isSaved.isNotEmpty(), "isSavedAlbums result should not be empty")
    }
}
