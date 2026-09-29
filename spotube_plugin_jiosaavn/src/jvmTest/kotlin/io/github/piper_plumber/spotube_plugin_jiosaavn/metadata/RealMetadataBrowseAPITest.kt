package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealMetadataBrowseAPITest {
    val client = RealMetadataBrowseAPI(TestFixture.jiosaavn)

    @Test
    fun `test featured returns first row items`() = runTest {
        val items = client.featured()

        assertTrue(items.isNotEmpty(), "Featured should return items from first row")
        val hasTrack = items.any { it is MetadataBrowseItem.Track }
        val hasArtist = items.any { it is MetadataBrowseItem.Artist }
        val hasAlbum = items.any { it is MetadataBrowseItem.Album }
        val hasPlaylist = items.any { it is MetadataBrowseItem.Playlist }
        assertTrue(
            hasTrack || hasArtist || hasAlbum || hasPlaylist,
            "Featured should return at least one type of item"
        )

        items.forEach { item ->
            when (item) {
                is MetadataBrowseItem.Track -> {
                    assertTrue(item.data.title.isNotBlank(), "Track title should not be blank")
                    assertTrue(
                        item.data.artists.isNotEmpty(),
                        "Track should have at least one artist"
                    )
                    item.data.artists.forEach { artist ->
                        assertTrue(artist.id.isNotBlank(), "Artist ID should not be blank")
                        assertTrue(
                            IdTokenPair.isValidFormat(artist.id),
                            "Artist ID should not be blank"
                        )
                        assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
                    }
                    assertNotNull(item.data.album, "Track should have an album") {
                        assertTrue(it.title.isNotBlank(), "Album title should not be blank")
                        assertTrue(it.artists.isNotEmpty(), "Album should have at least one artist")
                    }
                }

                is MetadataBrowseItem.Artist -> {
                    assertTrue(item.data.id.isNotBlank(), "Artist ID should not be blank")
                    assertTrue(
                        IdTokenPair.isValidFormat(item.data.id),
                        "Artist ID should not be blank"
                    )
                    assertTrue(item.data.name.isNotBlank(), "Artist name should not be blank")
                }

                is MetadataBrowseItem.Album -> {
                    assertTrue(item.data.id.isNotBlank(), "Album ID should not be blank")
                    assertTrue(
                        IdTokenPair.isValidFormat(item.data.id),
                        "Album ID should not be blank"
                    )
                    assertTrue(item.data.title.isNotBlank(), "Album title should not be blank")
                    assertTrue(
                        item.data.artists.isNotEmpty(),
                        "Album should have at least one artist"
                    )
                    item.data.artists.forEach { artist ->
                        assertTrue(artist.id.isNotBlank(), "Artist ID should not be blank")
                        assertTrue(
                            IdTokenPair.isValidFormat(artist.id),
                            "Artist ID should not be blank"
                        )
                        assertTrue(artist.name.isNotBlank(), "Artist name should not be blank")
                    }
                }

                is MetadataBrowseItem.Playlist -> {
                    assertTrue(item.data.id.isNotBlank(), "Playlist ID should not be blank")
                    assertTrue(
                        IdTokenPair.isValidFormat(item.data.id),
                        "Playlist ID should not be blank"
                    )
                    assertTrue(item.data.title.isNotBlank(), "Playlist title should not be blank")
                    assertNotNull(item.data.owner, "Playlist should have an owner") {
                        assertTrue(it.id.isNotBlank(), "Owner ID should not be blank")
                        assertTrue(it.username.isNotBlank(), "Owner name should not be blank")
                        assertFalse(
                            it.displayName.isNullOrBlank(),
                            "Owner display name should not be blank"
                        )
                    }
                }

                else -> {}
            }
        }
    }

    @Test
    fun `test list returns sections from second row onwards`() = runTest {
        val result = client.list(null)

        assertTrue(result.items.isNotEmpty(), "Browse list should return sections")
        result.items.forEach { section ->
            assertTrue(section.title.isNotBlank(), "Section title should not be blank")
        }
    }

    @Test
    fun `test sublist returns items for valid section`() = runTest {
        val sections = client.list(null)
        val firstSection = sections.items.find { it.moreLink != null }
        val result = client.sublist(firstSection!!.moreLink!!, null)

        assertTrue(result.items.isNotEmpty(), "Sublist should return items for valid section")
        result.items.forEach { item ->
            when (item) {
                is MetadataBrowseItem.Track -> assertTrue(item.data.title.isNotBlank())
                is MetadataBrowseItem.Artist -> assertTrue(item.data.name.isNotBlank())
                is MetadataBrowseItem.Album -> assertTrue(item.data.title.isNotBlank())
                is MetadataBrowseItem.Playlist -> assertTrue(item.data.title.isNotBlank())
                else -> {}
            }
        }
    }

    @Test
    fun `test sublist returns empty for invalid section`() = runTest {
        val result = client.sublist("nonexistent_section_xyz", null)

        assertTrue(result.items.isEmpty(), "Sublist should return empty list for invalid section")
    }
}
