package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchAlbumResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchArtistResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchMoreResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchPlaylistResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SearchEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun getResults(q: String, p: Int = 0, n: Int = 20): SearchResponse<Song> {
        val response = jiosaavn.client.get {
            url {
                url("/")
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "search.getResults",
                            "q" to q,
                            "p" to p.toString(),
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<SearchResponse<Song>>()
    }

    suspend fun getAlbumResults(q: String, p: Int = 1, n: Int = 20): SearchResponse<SearchAlbumResult> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "search.getAlbumResults",
                            "q" to q,
                            "p" to p.toString(),
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<SearchResponse<SearchAlbumResult>>()
    }

    suspend fun getArtistResults(q: String, p: Int = 1, n: Int = 20): SearchResponse<SearchArtistResult> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "search.getArtistResults",
                            "q" to q,
                            "p" to p.toString(),
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<SearchResponse<SearchArtistResult>>()
    }

    suspend fun getPlaylistResults(q: String, p: Int = 1, n: Int = 20): SearchResponse<SearchPlaylistResult> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "search.getPlaylistResults",
                            "q" to q,
                            "p" to p.toString(),
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<SearchResponse<SearchPlaylistResult>>()
    }

    suspend fun getMoreResults(
        q: String,
        type: String,
        p: Int = 1,
        n: Int = 20,
    ): SearchResponse<SearchMoreResult> {
        val paramsJson = Json.encodeToString(mapOf("type" to type))
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "search.getMoreResults",
                            "q" to q,
                            "query" to q,
                            "params" to paramsJson,
                            "p" to p.toString(),
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<SearchResponse<SearchMoreResult>>()
    }
}
