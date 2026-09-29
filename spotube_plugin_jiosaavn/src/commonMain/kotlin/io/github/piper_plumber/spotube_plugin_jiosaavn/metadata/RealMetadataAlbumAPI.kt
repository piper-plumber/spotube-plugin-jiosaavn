package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryMiniAlbum
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiAlbumResponse

class RealMetadataAlbumAPI(
    private val jiosaavn: Jiosaavn
) : MetadataAlbumAPI {

    override suspend fun getAlbum(id: String): MetadataAlbum.Detailed {
        val response =
            jiosaavn.webapi.getAlbum(token = IdTokenPair.token(id))
        return response.toMetadataAlbumDetailed()
    }

    override suspend fun getTrackAlbum(track: MetadataTrack): MetadataAlbum.Detailed {
        val token = track.album?.id?.let { IdTokenPair.token(it) }
            ?: run {
                val songResponse = jiosaavn.webapi.getSong(token = IdTokenPair.token(track.id))
                songResponse.moreInfo.albumUrl?.takeIf { it.isNotBlank() }
                    ?.let { IdTokenPair.tokenFromUrl(it) }
            }

        requireNotNull(token) { "No album token found for track ${track.id}" }

        val album = jiosaavn.webapi.getAlbum(token = token)

        return album.toMetadataAlbumDetailed()
    }

    override suspend fun getAlbumTracks(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataTrack> {
        val response = jiosaavn.webapi.getAlbum(token = IdTokenPair.token(id))

        val tracks = response.list.map { song ->
            song.toMetadataTrack()
        }

        return PaginationResult(
            items = tracks,
            totalCount = tracks.size,
            nextPagination = null
        )
    }

    override suspend fun savedAlbums(pagination: PaginationStrategy?): PaginationResult<MetadataAlbum.Detailed> {
        val libraryIds = jiosaavn.library.getAll()
        val albumIds = libraryIds.albums

        val limit = when (pagination) {
            is PaginationStrategy.Offset -> pagination.limit
            else -> 50
        }
        val offset = when (pagination) {
            is PaginationStrategy.Offset -> pagination.offset
            else -> 0
        }

        val pagedIds = albumIds.drop(offset).take(limit)
        val batches = pagedIds.chunked(50)

        val allMiniAlbums = batches.flatMap { batch ->
            jiosaavn.library.getAlbumDetails(batch)
        }

        val albums = allMiniAlbums.map { it.toMetadataAlbumDetailed() }

        return PaginationResult(
            items = albums,
            totalCount = albumIds.size,
            nextPagination = if (albums.size == limit) {
                PaginationStrategy.Offset(offset + limit, limit)
            } else null
        )
    }

    override suspend fun isSavedAlbums(ids: List<String>): List<Boolean> {
        val libraryIds = jiosaavn.library.getAll()
        val savedAlbumIds = libraryIds.albums.toSet()
        return ids.map { IdTokenPair.id(it) in savedAlbumIds }
    }

    override suspend fun saveAlbums(ids: List<String>) {
        for (id in ids) {
            jiosaavn.library.add(entityId = IdTokenPair.id(id), entityType = "album")
        }
    }

    override suspend fun removeSavedAlbums(ids: List<String>) {
        for (id in ids) {
            jiosaavn.library.delete(entityId = IdTokenPair.id(id), entityType = "album")
        }
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

private fun LibraryMiniAlbum.toMetadataAlbumDetailed(): MetadataAlbum.Detailed {
    val artists = this.moreInfo?.artistMap?.primaryArtists
        ?.takeIf { it.isNotEmpty() }
        ?.map { it.toMetadataArtistBasic() }
        ?: this.moreInfo?.artistMap?.artists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toMetadataArtistBasic() }
        ?: emptyList()

    return MetadataAlbum.Detailed(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        description = null,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        albumType = MetadataAlbumType.Album,
        artists = artists,
        releaseDate = this.moreInfo?.year,
        genres = emptyList(),
        trackCount = 0,
        externalUri = this.permaUrl
    )
}

private fun WebapiAlbumResponse.toMetadataAlbumDetailed(): MetadataAlbum.Detailed {
    val artists = this.moreInfo.artistMap?.primaryArtists
        ?.takeIf { it.isNotEmpty() }
        ?.map { it.toMetadataArtistBasic() }
        ?: this.moreInfo.artistMap?.artists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toMetadataArtistBasic() }
        ?: emptyList()

    return MetadataAlbum.Detailed(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        description = null,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        albumType = MetadataAlbumType.Album,
        artists = artists,
        releaseDate = this.year,
        genres = emptyList(),
        trackCount = this.listCount.toIntOrNull() ?: 0,
        externalUri = this.permaUrl
    )
}

private fun io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song.toMetadataTrack(): MetadataTrack {
    val artists = this.moreInfo.artistMap?.primaryArtists
        ?.takeIf { it.isNotEmpty() }
        ?.map { it.toMetadataArtistBasic() }
        ?: this.moreInfo.artistMap?.artists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toMetadataArtistBasic() }
        ?: emptyList()

    return MetadataTrack(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        durationMs = this.moreInfo.duration?.toLongOrNull()?.times(1000) ?: 0L,
        trackNumber = null,
        discNumber = null,
        artists = artists,
        album = null,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        explicit = this.explicitContent == "1",
        popularity = null,
        isrcCode = null,
        externalUri = this.permaUrl
    )
}
