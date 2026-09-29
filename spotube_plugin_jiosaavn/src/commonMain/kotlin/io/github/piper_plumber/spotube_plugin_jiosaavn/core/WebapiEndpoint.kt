package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.BrowseHoverDetails
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LaunchData
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiAlbumResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiArtistAlbumsResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiArtistResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiArtistSongsResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiPlaylistResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiShowResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiSongDetailsResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song

class WebapiEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun getLaunchData(): LaunchData {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "webapi.getLaunchData",
                        )
                    )
                )
            }
        }
        return response.body<LaunchData>()
    }

    private suspend inline fun <reified T : Any> get(
        token: String,
        type: String,
        page: Int = 1,
        nSong: Int? = null,
        nAlbum: Int? = null,
        n: Int? = null,
        subType: String? = null,
        more: Boolean? = null,
        category: String? = null,
        sortOrder: String? = null,
        includeMetaTags: Int = 0,
    ): T {
        val response = jiosaavn.client.get {
            url {
                url("/")
                parameters.putAll(
                    withDefaultQueryParams(
                        mutableMapOf(
                            "__call" to "webapi.get",
                            "token" to token,
                            "type" to type,
                            "p" to page.toString(),
                            "includeMetaTags" to includeMetaTags.toString(),
                        ).apply {
                            nSong?.let { put("n_song", it.toString()) }
                            nAlbum?.let { put("n_album", it.toString()) }
                            n?.let { put("n", it.toString()) }
                            subType?.let { put("sub_type", it) }
                            more?.let { put("more", it.toString()) }
                            category?.let { put("category", it) }
                            sortOrder?.let { put("sort_order", it) }
                        }
                    )
                )
            }
        }
        return response.body<T>()
    }

    suspend fun getArtist(
        token: String,
    ): WebapiArtistResponse = get(
        token = token,
        type = "artist",
    )

    suspend fun getArtistSongs(
        token: String,
        page: Int = 1,
        nSong: Int = 50,
    ): WebapiArtistSongsResponse = get(
        token = token,
        type = "artist",
        page = page,
        nSong = nSong,
        subType = "songs",
        more = true,
    )

    suspend fun getArtistAlbums(
        token: String,
        page: Int = 1,
        nAlbum: Int = 50,
    ): WebapiArtistAlbumsResponse = get(
        token = token,
        type = "artist",
        page = page,
        nAlbum = nAlbum,
        subType = "albums",
        more = true,
    )

    suspend fun getAlbum(
        token: String,
        page: Int = 1,
        n: Int = 50,
    ): WebapiAlbumResponse = get(
        token = token,
        type = "album",
        page = page,
        n = n,
    )

    suspend fun getPlaylist(
        token: String,
        page: Int = 1,
        limit: Int = 50,
    ): WebapiPlaylistResponse = get(
        token = token,
        type = "playlist",
        page = page,
        n = limit,
    )

    suspend fun getSongs(
        token: String,
    ): WebapiSongDetailsResponse = get(
        token = token,
        type = "song",
    )

    suspend fun getSong(
        token: String,
    ): Song = getSongs(token).songs.first()

    suspend fun getShow(
        token: String,
        page: Int = 1,
    ): WebapiShowResponse = get(
        token = token,
        type = "show",
        page = page,
    )

    suspend fun getBrowseHoverDetails(
        type: String,
        includeDetails: Boolean = true,
    ): BrowseHoverDetails {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "webapi.getBrowseHoverDetails",
                            "type" to type,
                            "include_details" to includeDetails.toString(),
                        )
                    )
                )
            }
        }
        return response.body<BrowseHoverDetails>()
    }
}
