package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RealMetadataPlaylistAPITest {
    val client = RealMetadataPlaylistAPI(TestFixture.authenticatedJiosaavn)

    @Test
    fun `test getPlaylistInfo`() = runTest {
        val playlist = client.getPlaylist("1220823608")

        assertNotNull(playlist)
        assertTrue(playlist.id.isNotBlank(), "Playlist ID should not be blank")
        assertTrue(
            IdTokenPair.isValidFormat(playlist.id),
            "Playlist ID should be in the format of id@@token"
        )
        assertTrue(playlist.title.isNotBlank(), "Playlist title should not be blank")
        assertTrue(playlist.thumbnails.isNotEmpty(), "Playlist should have at least one thumbnail")
        assertTrue(playlist.trackCount >= 0, "Playlist track count should be non-negative")
        assertNotNull(playlist.owner, "Playlist owner should not be null") {
            assertTrue(it.id.isNotBlank(), "Playlist owner ID should not be blank")
            assertTrue(it.username.isNotBlank(), "Playlist owner name should not be blank")
        }
    }

    @Test
    fun `test savedPlaylist`() = runTest {
        val playlists = client.savedPlaylists(null)

        assertTrue(playlists.items.isNotEmpty(), "Saved playlists should not be empty")
        playlists.items.forEach {
            assertTrue(
                IdTokenPair.isValidFormat(it.id),
                "Playlist ID should be in the format of id@@token"
            )
            assertTrue(it.title.isNotBlank(), "Playlist title should not be blank")
            assertTrue(it.thumbnails.isNotEmpty(), "Playlist should have at least one thumbnail")
            assertFalse(it.externalUri.isNullOrBlank(), "Playlist must have a valid external URI")
            assertNotNull(it.owner, "Playlist owner should not be null") { owner ->
                assertTrue(owner.id.isNotBlank(), "Playlist owner ID should not be blank")
                assertTrue(owner.username.isNotBlank(), "Playlist owner name should not be blank")
            }
        }
    }

    // 1212002367 Returns 55 tracks so pagination should work for 50 items per page
    @Test
    fun `test getPlaylistTracks`() = runTest {
        val tracks =
            client.getPlaylistTracks("1212002367", null)

        assertTrue(tracks.items.isNotEmpty(), "Playlist tracks should not be empty")
        assertNotNull(
            tracks.nextPagination,
            "Next page URL should not be null for paginated results"
        )
        tracks.items.forEach {
            assertTrue(
                IdTokenPair.isValidFormat(it.id),
                "Track ID should be in the format of id@@token"
            )
            assertTrue(it.title.isNotBlank(), "Track title should not be blank")
            assertTrue(
                it.album?.thumbnails?.isNotEmpty() == true || it.thumbnails?.isNotEmpty() == true,
                "Track should have at least one thumbnail either from album or track"
            )
            assertNotNull(it.artists, "Track artists should not be null")
            assertTrue(it.artists.isNotEmpty(), "Track should have at least one artist")
            it.artists.forEach { artist ->
                assertTrue(
                    IdTokenPair.isValidFormat(artist.id),
                    "Artist ID should be in the format of id@@token"
                )
                assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
            }
        }
    }

    // 1212002367 Returns 55 tracks so pagination should work for 50 items per page
    @Test
    fun `test getPlaylistTracks with pagination`() = runTest {
        val tracks =
            client.getPlaylistTracks("1212002367", PaginationStrategy.Page(2, 50))

        assertTrue(tracks.items.isNotEmpty(), "Playlist tracks should not be empty")
        assertNull(
            tracks.nextPagination,
            "Next page URL should be null"
        )
        tracks.items.forEach {
            assertTrue(it.title.isNotBlank(), "Track title should not be blank")
            assertTrue(
                it.album?.thumbnails?.isNotEmpty() == true || it.thumbnails?.isNotEmpty() == true,
                "Track should have at least one thumbnail either from album or track"
            )
            assertNotNull(it.artists, "Track artists should not be null")
            assertTrue(it.artists.isNotEmpty(), "Track should have at least one artist")
            it.artists.forEach { artist ->
                assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
            }
        }
    }

    @Test
    fun `test isSavedPlaylists`() = runTest {
        val isSaved = client.isSavedPlaylists(listOf("1220823608"))

        assertTrue(isSaved.isNotEmpty(), "isSavedPlaylists result should not be empty")
    }
}
