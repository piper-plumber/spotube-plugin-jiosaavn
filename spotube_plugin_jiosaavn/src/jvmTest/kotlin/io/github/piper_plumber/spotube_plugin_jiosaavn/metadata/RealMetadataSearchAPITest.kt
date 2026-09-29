package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSupportedSearchType
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealMetadataSearchAPITest {
    val client = RealMetadataSearchAPI(TestFixture.jiosaavn)

    @Test
    fun `test supportedSearchTypes contains expected types`() {
        val supportedTypes = client.supportedSearchTypes

        assertTrue(supportedTypes.contains(MetadataSupportedSearchType.TRACK))
        assertTrue(supportedTypes.contains(MetadataSupportedSearchType.ARTIST))
        assertTrue(supportedTypes.contains(MetadataSupportedSearchType.ALBUM))
        assertTrue(supportedTypes.contains(MetadataSupportedSearchType.PLAYLIST))
        assertTrue(supportedTypes.contains(MetadataSupportedSearchType.ALL))
    }

    private fun testTrack(track: MetadataSearchResult.Track) {
        assertTrue(IdTokenPair.isValidFormat(track.data.id), "Track ID should be in id@@token format")
        assertTrue(track.data.title.isNotBlank(), "Track title should not be blank")
        assertTrue(track.data.artists.isNotEmpty(), "Track should have at least one artist")
        track.data.artists.forEach { artist ->
            assertTrue(IdTokenPair.isValidFormat(artist.id), "Artist ID should be in id@@token format")
            assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
        }
        assertEquals(
            track.data.thumbnails?.isNotEmpty(),
            true,
            "Track should have at least one thumbnail"
        )
        assertNotNull(track.data.album, "Track should have an album") { album ->
            assertTrue(IdTokenPair.isValidFormat(album.id), "Album ID should be in id@@token format")
            assertTrue(album.title.isNotBlank(), "Album title should not be blank")
            assertTrue(album.thumbnails.isNotEmpty(), "Album should have at least one thumbnail")
            assertNotNull(album.artists, "Album should have artists") { artists ->
                assertTrue(artists.isNotEmpty(), "Album should have at least one artist")
                artists.forEach { artist ->
                    assertTrue(IdTokenPair.isValidFormat(artist.id), "Album artist ID should be in id@@token format")
                    assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
                }
            }
        }
    }

    private fun testArtist(artist: MetadataSearchResult.Artist) {
        assertTrue(artist.data.name.isNotBlank(), "Artist name should not be blank")
        assertTrue(IdTokenPair.isValidFormat(artist.data.id), "Artist ID should be in id@@token format")
        assertTrue(artist.data.thumbnails.isNotEmpty(), "Artist should have at least one thumbnail")
    }

    private fun testAlbum(album: MetadataSearchResult.Album) {
        assertTrue(IdTokenPair.isValidFormat(album.data.id), "Album ID should be in id@@token format")
        assertTrue(album.data.title.isNotBlank(), "Album title should not be blank")
        assertTrue(album.data.thumbnails.isNotEmpty(), "Album should have at least one thumbnail")
    }

    private fun testPlaylist(playlist: MetadataSearchResult.Playlist) {
        assertTrue(IdTokenPair.isValidFormat(playlist.data.id), "Playlist ID should be in id@@token format")
        assertTrue(playlist.data.title.isNotBlank(), "Playlist title should not be blank")
        assertTrue(
            playlist.data.thumbnails.isNotEmpty(),
            "Playlist should have at least one thumbnail"
        )
    }

    @Test
    fun `test search returns mixed results`() = runTest {
        val results = client.search("Arijit Singh")

        assertTrue(results.isNotEmpty(), "Search should return results")
        val hasTrack = results.any { it is MetadataSearchResult.Track }
        val hasArtist = results.any { it is MetadataSearchResult.Artist }
        val hasAlbum = results.any { it is MetadataSearchResult.Album }
        val hasPlaylist = results.any { it is MetadataSearchResult.Playlist }
        assertTrue(
            hasTrack || hasArtist || hasAlbum || hasPlaylist,
            "Search should return at least one type of result"
        )

        results.forEach {
            when (it) {
                is MetadataSearchResult.Track -> testTrack(it)
                is MetadataSearchResult.Artist -> testArtist(it)
                is MetadataSearchResult.Album -> testAlbum(it)
                is MetadataSearchResult.Playlist -> testPlaylist(it)
                else -> {}
            }
        }
    }

    @Test
    fun `test searchTracks returns tracks and paginates`() = runTest {
        val result = client.searchTracks("Kesariya", null)

        assertTrue(result.items.isNotEmpty(), "Search tracks should return results")
        result.items.forEach { trackResult ->
            testTrack(trackResult)
        }

        val totalCount = result.totalCount
        assertTrue(
            totalCount > result.items.size,
            "Total count should be greater than items returned for pagination"
        )

        val nextPagination = result.nextPagination
        assertNotNull(nextPagination, "Next pagination should not be null for paginated results")

        val nextResult = client.searchTracks("Kesariya", nextPagination)
        assertTrue(
            nextResult.items.isNotEmpty(),
            "Next page of search tracks should return results"
        )
        nextResult.items.forEach { trackResult ->
            testTrack(trackResult)
        }
    }

    @Test
    fun `test searchArtists returns artists and paginates`() = runTest {
        val result = client.searchArtists("Arijit Singh", null)

        assertTrue(result.items.isNotEmpty(), "Search artists should return results")
        result.items.forEach { artistResult ->
            testArtist(artistResult)
        }

        val totalCount = result.totalCount
        assertTrue(
            totalCount > result.items.size,
            "Total count should be greater than items returned for pagination"
        )
        val nextPagination = result.nextPagination
        assertNotNull(nextPagination, "Next pagination should not be null for paginated results")
        val nextResult = client.searchArtists("Arijit Singh", nextPagination)
        assertTrue(
            nextResult.items.isNotEmpty(),
            "Next page of search artists should return results"
        )
        nextResult.items.forEach { artistResult ->
            testArtist(artistResult)
        }
    }

    @Test
    fun `test searchAlbums returns albums and paginates`() = runTest {
        val result = client.searchAlbums("Aashiqui 2", null)

        assertTrue(result.items.isNotEmpty(), "Search albums should return results")
        result.items.forEach { albumResult ->
            testAlbum(albumResult)
        }
        val totalCount = result.totalCount
        assertTrue(
            totalCount > result.items.size,
            "Total count should be greater than items returned for pagination"
        )
        val nextPagination = result.nextPagination
        assertNotNull(nextPagination, "Next pagination should not be null for paginated results")
        val nextResult = client.searchAlbums("Aashiqui 2", nextPagination)
        assertTrue(
            nextResult.items.isNotEmpty(),
            "Next page of search albums should return results"
        )
        nextResult.items.forEach { albumResult ->
            testAlbum(albumResult)
        }
    }

    @Test
    fun `test searchPlaylists returns playlists and paginates`() = runTest {
        val result = client.searchPlaylists("Arijit Singh Song", null)

        assertTrue(result.items.isNotEmpty(), "Search playlists should return results")
        result.items.forEach { playlistResult ->
            testPlaylist(playlistResult)
        }

        val totalCount = result.totalCount
        assertTrue(
            totalCount > result.items.size,
            "Total count should be greater than items returned for pagination"
        )
        val nextPagination = result.nextPagination
        assertNotNull(nextPagination, "Next pagination should not be null for paginated results")

        val nextResult = client.searchPlaylists("Arijit Singh Song", nextPagination)
        assertTrue(
            nextResult.items.isNotEmpty(),
            "Next page of search playlists should return results"
        )
        nextResult.items.forEach { playlistResult ->
            testPlaylist(playlistResult)
        }
    }

    @Test
    fun `test searchUsers returns empty`() = runTest {
        val result = client.searchUsers("Test", null)

        assertTrue(result.items.isEmpty(), "Search users should return empty for JioSaavn")
    }
}
