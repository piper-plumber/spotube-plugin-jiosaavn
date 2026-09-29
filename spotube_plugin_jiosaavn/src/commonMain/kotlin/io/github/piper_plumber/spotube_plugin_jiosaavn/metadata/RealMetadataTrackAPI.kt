package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrackAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.RadioResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.RadioSongResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiAlbumResponse

class RealMetadataTrackAPI(
    private val jiosaavn: Jiosaavn
) : MetadataTrackAPI {

    override suspend fun getTrack(id: String): MetadataTrack {
        val response = jiosaavn.webapi.getSong(token = IdTokenPair.id(id))
        return response.toMetadataTrack()
    }

    override suspend fun savedTracks(pagination: PaginationStrategy?): PaginationResult<MetadataTrack> {
        val libraryIds = jiosaavn.library.getAll()
        val songIds = libraryIds.songs

        val limit = when (pagination) {
            is PaginationStrategy.Offset -> pagination.limit
            else -> 50
        }
        val offset = when (pagination) {
            is PaginationStrategy.Offset -> pagination.offset
            else -> 0
        }

        val pagedIds = songIds.drop(offset).take(limit)
        val batches = pagedIds.chunked(50)

        val allSongs = batches.flatMap { batch ->
            jiosaavn.library.getSongDetails(batch).songs
        }

        val tracks = allSongs.map { it.toMetadataTrack() }

        return PaginationResult(
            items = tracks,
            totalCount = songIds.size,
            nextPagination = if (tracks.size == limit) {
                PaginationStrategy.Offset(offset + limit, limit)
            } else null
        )
    }

    override suspend fun isSavedTracks(ids: List<String>): List<Boolean> {
        val libraryIds = jiosaavn.library.getAll()
        val savedSongIds = libraryIds.songs.toSet()
        return ids.map { IdTokenPair.id(it) in savedSongIds }
    }

    override suspend fun saveTracks(ids: List<String>) {
        for (id in ids) {
            jiosaavn.library.add(entityId = IdTokenPair.id(id), entityType = "song")
        }
    }

    override suspend fun removeSavedTracks(ids: List<String>) {
        for (id in ids) {
            jiosaavn.library.delete(entityId = IdTokenPair.id(id), entityType = "song")
        }
    }

    override suspend fun recommendationsBasedOnTracks(
        seedTrackIds: List<String>,
        limit: Int
    ): List<MetadataTrack> {
        val stationResponse = jiosaavn.webradio.createEntityStation(
            entityIds = seedTrackIds.map { IdTokenPair.id(it) },
            entityType = "queue"
        )
        require(stationResponse is RadioResponse.StationCreated) { "Failed to create radio station" }
        val stationId = stationResponse.stationId

        val tracks = mutableListOf<MetadataTrack>()
        var page = 1
        while (tracks.size < limit) {
            val remaining = limit - tracks.size
            val count = minOf(remaining, 5)
            val response = jiosaavn.webradio.getSong(
                stationId = stationId,
                count = count,
                next = page
            )
            require(response is RadioSongResponse.Success) { "Failed to get song from radio" }
            tracks.add(response.song.toMetadataTrack())
            page++
            if (count < 5) break
        }

        return tracks
    }

    private fun Song.toMetadataTrack(): MetadataTrack {
        val artists = this.moreInfo.artistMap?.primaryArtists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toMetadataArtistBasic() }
            ?: this.moreInfo.artistMap?.artists
                ?.takeIf { it.isNotEmpty() }
                ?.map { it.toMetadataArtistBasic() }
            ?: emptyList()

        val albumInfo = this.moreInfo.album?.let { album ->
            MetadataAlbum.Detailed(
                id = IdTokenPair.asString(this.moreInfo.albumId!!, this.moreInfo.albumUrl ?: ""),
                title = album,
                description = null,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                albumType = MetadataAlbumType.Album,
                artists = artists,
                releaseDate = this.moreInfo.releaseDate,
                genres = emptyList(),
                trackCount = 0,
                externalUri = this.moreInfo.albumUrl
            )
        }

        return MetadataTrack(
            id = IdTokenPair.asString(this.id, this.permaUrl),
            title = this.title,
            durationMs = this.moreInfo.duration?.toLongOrNull()?.times(1000) ?: 0L,
            trackNumber = null,
            discNumber = null,
            artists = artists,
            album = albumInfo,
            thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
            explicit = this.explicitContent == "1",
            popularity = null,
            isrcCode = null,
            externalUri = this.permaUrl
        )
    }
}

private fun Artist.toMetadataArtistBasic(): MetadataArtist.Basic {
    return MetadataArtist.Basic(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        name = this.name,
        thumbnails = listOfNotNull(this.image).map {
            Thumbnail(
                url = it,
                width = 500,
                height = 500
            )
        },
        externalUri = this.permaUrl
    )
}