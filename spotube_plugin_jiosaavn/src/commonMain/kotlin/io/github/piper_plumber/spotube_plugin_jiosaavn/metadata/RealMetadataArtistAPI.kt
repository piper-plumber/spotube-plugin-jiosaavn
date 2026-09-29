package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistOverview
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Album
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryMiniArtist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiAlbumResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.WebapiArtistResponse

class RealMetadataArtistAPI(
    private val jiosaavn: Jiosaavn
) : MetadataArtistAPI {

    override suspend fun getArtist(id: String): MetadataArtist.Detailed {
        val response = jiosaavn.webapi.getArtist(token = IdTokenPair.token(id))
        return response.toMetadataArtist()
    }

    override suspend fun artistOverview(id: String): MetadataArtistOverview {
        TODO("Not yet implemented")
    }

    override suspend fun getArtistTop10Tracks(id: String): List<MetadataTrack> {
        val response = jiosaavn.webapi.getArtist(token = IdTokenPair.token(id))

        return response.topSongs
            ?.take(10)
            ?.map { it.toMetadataTrack() }
            ?: emptyList()
    }

    override suspend fun relatedArtists(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataArtist.Basic> {
        TODO("Not yet implemented")
    }

    override suspend fun featuredPlaylists(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataPlaylist> {
        TODO("Not yet implemented")
    }

    override suspend fun getArtistAlbums(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataAlbum.Detailed> {
        val limit = when (pagination) {
            is PaginationStrategy.Offset -> pagination.limit.coerceIn(1, 50)
            else -> 50
        }
        val offset = when (pagination) {
            is PaginationStrategy.Offset -> pagination.offset
            else -> 0
        }

        val page = (offset / limit) + 1

        val response = jiosaavn.webapi.getArtistAlbums(
            token = IdTokenPair.token(id),
            page = page,
            nAlbum = limit,
        )

        val albums = response.albums.map { it.toMetadataAlbumDetailed() }

        return PaginationResult(
            items = albums,
            totalCount = response.albums.size,
            nextPagination = if (albums.size >= limit) {
                PaginationStrategy.Offset(offset + limit, limit)
            } else null
        )
    }

    override suspend fun savedArtists(pagination: PaginationStrategy?): PaginationResult<MetadataArtist.Detailed> {
        val libraryIds = jiosaavn.library.getAll()
        val artistIds = libraryIds.artists

        val limit = when (pagination) {
            is PaginationStrategy.Offset -> pagination.limit
            else -> 50
        }
        val offset = when (pagination) {
            is PaginationStrategy.Offset -> pagination.offset
            else -> 0
        }

        val pagedIds = artistIds.drop(offset).take(limit)
        val batches = pagedIds.chunked(50)

        val allMiniArtists = batches.flatMap { batch ->
            jiosaavn.library.getArtistDetails(batch)
        }

        val artists = allMiniArtists.map { it.toMetadataArtistDetailed() }

        return PaginationResult(
            items = artists,
            totalCount = artistIds.size,
            nextPagination = if (artists.size == limit) {
                PaginationStrategy.Offset(offset + limit, limit)
            } else null
        )
    }

    override suspend fun isSavedArtists(ids: List<String>): List<Boolean> {
        val libraryIds = jiosaavn.library.getAll()
        val savedArtistIds = libraryIds.artists.toSet()
        return ids.map { IdTokenPair.id(it) in savedArtistIds }
    }

    override suspend fun saveArtists(ids: List<String>) {
        for (id in ids) {
            jiosaavn.library.add(entityId = IdTokenPair.id(id), entityType = "artist")
        }
    }

    override suspend fun removeSavedArtists(ids: List<String>) {
        for (id in ids) {
            jiosaavn.library.delete(entityId = IdTokenPair.id(id), entityType = "artist")
        }
    }
}

private fun LibraryMiniArtist.toMetadataArtistDetailed(): MetadataArtist.Detailed {
    return MetadataArtist.Detailed(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        name = this.title,
        thumbnails = listOfNotNull(this.image).map {
            Thumbnail(
                url = it,
                width = 500,
                height = 500
            )
        },
        genres = emptyList(),
        biography = null,
        followersCount = null,
        externalUri = this.permaUrl
    )
}

private fun WebapiArtistResponse.toMetadataArtist(): MetadataArtist.Detailed {
    return MetadataArtist.Detailed(
        id = IdTokenPair.asString(this.artistId, this.urls?.overview.orEmpty()),
        name = this.name,
        thumbnails = listOfNotNull(this.image).map {
            Thumbnail(
                url = it,
                width = 500,
                height = 500
            )
        },
        genres = emptyList(),
        biography = null,
        followersCount = this.fanCount?.toIntOrNull() ?: this.followerCount?.toIntOrNull(),
        externalUri = this.urls?.overview,
    )
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

private fun Song.toMetadataTrack(): MetadataTrack {
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
        album = if (this.moreInfo.albumId == null) null else MetadataAlbum.Detailed(
            id = IdTokenPair.asString(this.moreInfo.albumId, this.moreInfo.albumUrl!!),
            title = this.moreInfo.album.orEmpty(),
            description = null,
            thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
            albumType = MetadataAlbumType.Album,
            artists = artists,
            releaseDate = this.year,
            genres = emptyList(),
            trackCount = 0,
            externalUri = this.moreInfo.albumUrl
        ),
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        explicit = this.explicitContent == "1",
        popularity = null,
        isrcCode = null,
        externalUri = this.permaUrl
    )
}

private fun WebapiAlbumResponse.toMetadataAlbumDetailedFromWebapi(): MetadataAlbum.Detailed {
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

private fun Album.toMetadataAlbumDetailed(): MetadataAlbum.Detailed {
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
        description = this.headerDesc ?: this.subtitle,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        albumType = MetadataAlbumType.Album,
        artists = artists,
        releaseDate = this.moreInfo?.releaseDate ?: this.year,
        genres = emptyList(),
        trackCount = this.moreInfo?.songCount?.toIntOrNull() ?: 0,
        externalUri = this.permaUrl
    )
}
