package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.PlaylistDetails
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.PlaylistDetailsResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.PlaylistStatusResponse

class PlaylistEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun getDetails(listid: String, n: Int = 50, p: Int = 1): PlaylistDetails {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.getDetails",
                            "listid" to listid,
                            "api_version" to "4",
                            "n" to n.toString(),
                            "p" to p.toString(),
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetails>()
    }

    suspend fun create(
        listname: String,
        contents: String = "",
        share: Boolean = true,
    ): PlaylistDetailsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.create",
                            "listname" to listname,
                            "contents" to contents,
                            "share" to share.toString(),
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetailsResponse>()
    }

    suspend fun add(
        listid: String,
        contents: String,
    ): PlaylistDetailsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.add",
                            "listid" to listid,
                            "contents" to contents,
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetailsResponse>()
    }

    suspend fun remove(
        listid: String,
        pid: String,
    ): PlaylistDetailsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.remove",
                            "listid" to listid,
                            "pid" to pid,
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetailsResponse>()
    }

    suspend fun delete(listid: String): PlaylistStatusResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.delete",
                            "listid" to listid,
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistStatusResponse>()
    }

    suspend fun rename(
        listid: String,
        listname: String,
    ): PlaylistDetailsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.rename",
                            "listid" to listid,
                            "listname" to listname,
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetailsResponse>()
    }

    suspend fun makePrivate(listid: String): PlaylistDetailsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.makePrivate",
                            "listid" to listid,
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetailsResponse>()
    }

    suspend fun makePublic(listid: String): PlaylistDetailsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "playlist.makePublic",
                            "listid" to listid,
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<PlaylistDetailsResponse>()
    }
}
