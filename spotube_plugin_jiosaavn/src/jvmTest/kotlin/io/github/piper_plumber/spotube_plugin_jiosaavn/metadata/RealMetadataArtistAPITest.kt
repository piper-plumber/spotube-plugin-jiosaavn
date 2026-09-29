package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealMetadataArtistAPITest {
    val client = RealMetadataArtistAPI(TestFixture.authenticatedJiosaavn)

    @Test
    fun `test getArtistInfo`() = runTest {
        val artist = client.getArtist("720746@@Dx5Vty3hXGs_")

        assertNotNull(artist)
        assertTrue(artist.id.isNotBlank(), "Artist id should not be blank")
        assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
        assertTrue(artist.thumbnails.isNotEmpty(), "Artist should have at least one thumbnail")
    }

    @Test
    fun `test getArtistTop10Tracks`() = runTest {
        val tracks = client.getArtistTop10Tracks("720746@@Dx5Vty3hXGs_")

        assertTrue(tracks.size <= 10, "Artist top tracks should not be more than 10")
        tracks.forEach {
            assertTrue(IdTokenPair.isValidFormat(it.id), "Track id should be in id@@token format")
            assertTrue(it.title.isNotBlank(), "Track title should not be blank")
            assertTrue(it.artists.isNotEmpty(), "Track artists should not be empty")
            it.artists.forEach { artist ->
                assertTrue(IdTokenPair.isValidFormat(artist.id), "Artist id should be in id@@token format")
                assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
            }
        }
    }

    @Test
    fun `test getArtistAlbums`() = runTest {
        val albums = client.getArtistAlbums("720746@@Dx5Vty3hXGs_", null)

        albums.items.forEach {
            assertTrue(IdTokenPair.isValidFormat(it.id), "Album id should be in id@@token format")
            assertTrue(it.title.isNotBlank(), "Album title should not be blank")
            assertTrue(it.thumbnails.isNotEmpty(), "Album should have at least one thumbnail")
            assertTrue(it.artists.isNotEmpty(), "Album artists should not be empty")
            assertEquals(
                it.releaseDate?.isNotBlank(),
                true,
                "Album release date should not be blank"
            )
            assertEquals(it.albumType, MetadataAlbumType.Album, "Album type should be Album")
            it.artists.forEach { artist ->
                assertTrue(IdTokenPair.isValidFormat(artist.id), "Artist id should be in id@@token format")
                assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
            }
        }
    }

    @Test
    fun `test savedArtists`() = runTest {
        val artists = client.savedArtists(null)

        artists.items.forEach {
            assertTrue(it.name.isNotBlank(), "Artist name should not be blank")
            assertTrue(it.thumbnails.isNotEmpty(), "Artist should have at least one thumbnail")
            assertTrue(IdTokenPair.isValidFormat(it.id), "Artist ID should be in id@@token format")
        }
        assertNotNull(artists.nextPagination, "Next pagination should not be null for saved artists")
    }

    @Test
    fun `test isSavedArtists`() = runTest {
        val isSaved = client.isSavedArtists(listOf("485944@@rhIHJvwRgOU_", "459320@@LlRWpHzy3Hk_"))

        assertTrue(isSaved.isNotEmpty(), "isSavedArtists result should not be empty")
    }
}
