package io.github.piper_plumber.spotube_plugin_jiosaavn.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MoreInfoDecodingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun launchDataHistoryWithEmptyMoreInfoArray() {
        val input = """
            {
              "history": [
                {
                  "id": "903409848",
                  "title": "Sid Sriram - Love Songs - Telugu",
                  "subtitle": "",
                  "type": "playlist",
                  "image": "https://c.saavncdn.com/editorial/x.jpg",
                  "perma_url": "https://www.jiosaavn.com/featured/x/sbJbMSD745tFo9wdEAzFBA__",
                  "more_info": [],
                  "explicit_content": "0",
                  "mini_obj": true
                }
              ]
            }
        """.trimIndent()

        val data = json.decodeFromString<LaunchData>(input)
        val item = assertIs<LaunchPlaylist>(data.history!!.single())
        assertEquals("903409848", item.id)
        assertEquals(PlaylistMoreInfo(), item.moreInfo)
    }

    @Test
    fun playlistDetailsMoreInfoEmptyArray() {
        val info = json.decodeFromString<PlaylistDetailsMoreInfo>("[]")
        assertNull(info.songCount)
    }

    @Test
    fun moreInfoObjectStillDecodes() {
        val info = json.decodeFromString<SongMoreInfo>("""{"album":"X","duration":"200"}""")
        assertEquals("X", info.album)
        assertEquals("200", info.duration)
    }
}
