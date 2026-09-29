package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryGetAllResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryMiniAlbum
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryMiniArtist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryMiniPlaylist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibrarySongDetails
import kotlin.time.Clock

class LibraryEndpoint(private val jiosaavn: Jiosaavn) {
    private var cachedLibraryIds: LibraryGetAllResponse? = null
    private var cacheTimestamp: Long = 0
    private val cacheTtlMs: Long = 60_000

    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

    suspend fun getAll(forceRefresh: Boolean = false): LibraryGetAllResponse {
        val now = currentTimeMillis()
        if (!forceRefresh && cachedLibraryIds != null && now - cacheTimestamp < cacheTtlMs) {
            return cachedLibraryIds!!
        }

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.getAll",
                        )
                    )
                )
            }
        }
        val result = response.body<LibraryGetAllResponse>()
        cachedLibraryIds = result
        cacheTimestamp = now
        return result
    }

    suspend fun add(entityId: String, entityType: String = "song"): LibraryResponse {
        val response = jiosaavn.client.post {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.add",
                            "entity_id" to entityId,
                            "entity_type" to entityType,
                        )
                    )
                )
            }
        }
        cachedLibraryIds = null
        return response.body<LibraryResponse.Success>()
    }

    suspend fun delete(entityId: String, entityType: String = "song"): LibraryResponse {
        val response = jiosaavn.client.post {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.delete",
                            "entity_id" to entityId,
                            "entity_type" to entityType,
                        )
                    )
                )
            }
        }
        cachedLibraryIds = null
        return response.body<LibraryResponse.Success>()
    }

    suspend fun getSongDetails(entityIds: List<String>, n: Int = 50): LibrarySongDetails {
        if (entityIds.isEmpty()) return LibrarySongDetails(emptyList())

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.getDetails",
                            "entity_ids" to entityIds.joinToString(","),
                            "entity_type" to "song",
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<LibrarySongDetails>()
    }

    suspend fun getAlbumDetails(entityIds: List<String>, n: Int = 50): List<LibraryMiniAlbum> {
        if (entityIds.isEmpty()) return emptyList()

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.getDetails",
                            "entity_ids" to entityIds.joinToString(","),
                            "entity_type" to "album",
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<List<LibraryMiniAlbum>>()
    }

    suspend fun getPlaylistDetails(entityIds: List<String>, n: Int = 50): List<LibraryMiniPlaylist> {
        if (entityIds.isEmpty()) return emptyList()

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.getDetails",
                            "entity_ids" to entityIds.joinToString(","),
                            "entity_type" to "playlist",
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<List<LibraryMiniPlaylist>>()
    }

    suspend fun getArtistDetails(entityIds: List<String>, n: Int = 50): List<LibraryMiniArtist> {
        if (entityIds.isEmpty()) return emptyList()

        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.getDetails",
                            "entity_ids" to entityIds.joinToString(","),
                            "entity_type" to "artist",
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<List<LibraryMiniArtist>>()
    }

    suspend fun getDetails(
        entityIds: List<String>,
        entityType: String = "song",
        n: Int = 50,
    ): LibrarySongDetails {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "library.getDetails",
                            "entity_ids" to entityIds.joinToString(","),
                            "entity_type" to entityType,
                            "n" to n.toString(),
                        )
                    )
                )
            }
        }
        return response.body<LibrarySongDetails>()
    }
}
