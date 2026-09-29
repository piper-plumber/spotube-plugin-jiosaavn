package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealMetadataTrackAPITest {
    val client = RealMetadataTrackAPI(TestFixture.authenticatedJiosaavn)

    @Test
    fun `test getTrackInfo`() = runTest {
        val track = client.getTrack("By1SBCEdf1U")

        assertNotNull(track)
        assertTrue(track.id.isNotBlank(), "Track id should not be blank")
        assertTrue(IdTokenPair.isValidFormat(track.id), "Track id should be in id@@token format")
        assertTrue(track.title.isNotBlank(), "Track title should not be blank")
        assertTrue(track.durationMs > 0, "Track duration should be greater than 0")
        assertTrue(track.artists.isNotEmpty(), "Track should have at least one artist")
        assertNotNull(track.album, "Track album should not be null") {
            assertTrue(it.id.isNotBlank(), "Album id should not be blank")
            assertTrue(IdTokenPair.isValidFormat(it.id), "Album id should be in id@@token format")
            assertTrue(it.title.isNotBlank(), "Album title should not be blank")
            assertFalse(it.releaseDate.isNullOrBlank(), "Album release date should not be blank")
            assertTrue(it.artists.isNotEmpty(), "Album should have at least one artist")
        }
        track.artists.forEach {
            assertTrue(it.id.isNotBlank(), "Artist id should not be blank")
            assertTrue(IdTokenPair.isValidFormat(it.id), "Artist id should be in id@@token format")
            assertTrue(it.name.isNotBlank(), "Artist name should not be blank")
        }
    }

    @Test
    fun `test savedTracks`() = runTest {
        val result = client.savedTracks(null)

        assertTrue(result.items.isNotEmpty(), "Saved tracks should not be empty")
        result.items.forEach { track ->
            assertTrue(IdTokenPair.isValidFormat(track.id), "Track id should be in id@@token format")
            assertTrue(track.title.isNotBlank(), "Track title should not be blank")
            assertTrue(track.artists.isNotEmpty(), "Track should have at least one artist")
            assertNotNull(track.album, "Track album should not be null") {
                assertTrue(IdTokenPair.isValidFormat(it.id), "Album id should be in id@@token format")
                assertTrue(it.title.isNotBlank(), "Album title should not be blank")
                assertTrue(it.artists.isNotEmpty(), "Album should have at least one artist")
            }
            track.artists.forEach {
                assertTrue(it.name.isNotBlank(), "Artist name should not be blank")
                assertTrue(IdTokenPair.isValidFormat(it.id), "Artist id should be in id@@token format")
                assertTrue(it.thumbnails.isNotEmpty(), "Artist should have at least one thumbnail")
            }
        }
        assertNotNull(result.nextPagination, "Next pagination should not be null")
    }

    @Test
    fun `test isSavedTracks`() = runTest {
        val isSaved = client.isSavedTracks(listOf("By1SBCEdf1U"))

        assertTrue(isSaved.isNotEmpty(), "isSavedTracks result should not be empty")
    }
}
