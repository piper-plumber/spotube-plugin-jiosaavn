package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Album
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Chart
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.ContentGetAlbumsResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.JiosaavnResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.ListeningHistory
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Playlist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.TopSearch
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.TopShow
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Trending

class ContentEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun getAlbums(n: Int = 50, p: Int = 1): List<Album> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "content.getAlbums",
                            "n" to n.toString(),
                            "p" to p.toString(),
                        )
                    )
                )
            }
        }

        return response.body<ContentGetAlbumsResponse>().data
    }

    suspend fun getCharts(): List<Chart> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "content.getCharts",
                        )
                    )
                )
            }
        }
        return response.body<List<Chart>>()
    }

    suspend fun getFeaturedPlaylists(
        n: Int = 50,
        p: Int = 1,
        language: String? = null,
    ): List<Playlist> {
        val params = mutableMapOf(
            "__call" to "content.getFeaturedPlaylists",
            "featured" to "true",
            "n" to n.toString(),
            "p" to p.toString(),
        )
        if (language != null) {
            params["language"] = language
        }

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(withDefaultQueryParams(params))
            }
        }
        return response.body<JiosaavnResponse<List<Playlist>>>().data ?: emptyList()
    }

    suspend fun getListeningHistory(
        n: Int = 40,
        lastPlayedTimestamp: String? = null,
    ): ListeningHistory {
        val params = mutableMapOf(
            "__call" to "content.getListeningHistory",
            "n" to n.toString(),
        )
        if (lastPlayedTimestamp != null) {
            params["last_played_timestamp"] = lastPlayedTimestamp
        }

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(withDefaultQueryParams(params))
            }
        }
        return response.body<ListeningHistory>()
    }

    suspend fun getTopSearches(): List<TopSearch> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "content.getTopSearches",
                        )
                    )
                )
            }
        }
        return response.body<List<TopSearch>>()
    }

    suspend fun getTopShows(n: Int = 20, p: Int = 1): List<TopShow> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "content.getTopShows",
                            "n" to n.toString(),
                            "p" to p.toString(),
                        )
                    )
                )
            }
        }
        return response.body<JiosaavnResponse<List<TopShow>>>().data ?: emptyList()
    }

    suspend fun getTrending(
        type: String = "playlist",
        language: String? = null,
    ): List<Trending> {
        val params = mutableMapOf(
            "__call" to "content.getTrending",
            "type" to type,
        )
        if (language != null) {
            params["language"] = language
        }

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(withDefaultQueryParams(params))
            }
        }
        return response.body<List<Trending>>()
    }
}
