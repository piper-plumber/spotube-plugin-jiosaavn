package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSupportedSearchType
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchAlbumResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchArtistResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SearchPlaylistResult
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song


class RealMetadataSearchAPI(
    private val jiosaavn: Jiosaavn
) : MetadataSearchAPI {

    override val supportedSearchTypes: List<MetadataSupportedSearchType>
        get() = listOf(
            MetadataSupportedSearchType.TRACK,
            MetadataSupportedSearchType.ARTIST,
            MetadataSupportedSearchType.ALBUM,
            MetadataSupportedSearchType.PLAYLIST,
            MetadataSupportedSearchType.ALL,
        )

    override suspend fun search(query: String): List<MetadataSearchResult> {
        val tracks = searchTracks(query, null).items
        val artists = searchArtists(query, null).items
        val albums = searchAlbums(query, null).items
        val playlists = searchPlaylists(query, null).items

        return buildList {
            addAll(tracks)
            addAll(artists)
            addAll(albums)
            addAll(playlists)
        }
    }

    override suspend fun searchTracks(
        query: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataSearchResult.Track> {
        val paging = when (pagination) {
            is PaginationStrategy.Page -> pagination
            null -> PaginationStrategy.Page(page = 1, pageSize = 20)
            else -> throw IllegalArgumentException("Unsupported pagination strategy: ${pagination::class}")
        }

        val response = jiosaavn.search.getResults(q = query, p = paging.page, n = paging.pageSize)
        val tracks = response.results.map { song ->
            MetadataSearchResult.Track(song.toMetadataTrack())
        }

        val nextPagination = if (response.results.size == paging.pageSize) {
            paging.copy(page = paging.page + 1)
        } else {
            null
        }

        return PaginationResult(
            items = tracks,
            totalCount = response.total,
            nextPagination = nextPagination
        )
    }

    override suspend fun searchArtists(
        query: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataSearchResult.Artist> {
        val paging = when (pagination) {
            is PaginationStrategy.Page -> pagination
            null -> PaginationStrategy.Page(page = 1, pageSize = 20)
            else -> throw IllegalArgumentException("Unsupported pagination strategy: ${pagination::class}")
        }

        val response =
            jiosaavn.search.getArtistResults(q = query, p = paging.page, n = paging.pageSize)
        val artists = response.results.map { result ->
            MetadataSearchResult.Artist(result.toMetadataArtist())
        }

        val nextPagination = if (response.results.size == paging.pageSize) {
            paging.copy(page = paging.page + 1)
        } else {
            null
        }

        return PaginationResult(
            items = artists,
            totalCount = response.total,
            nextPagination = nextPagination
        )
    }

    override suspend fun searchAlbums(
        query: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataSearchResult.Album> {
        val paging = when (pagination) {
            is PaginationStrategy.Page -> pagination
            null -> PaginationStrategy.Page(page = 1, pageSize = 20)
            else -> throw IllegalArgumentException("Unsupported pagination strategy: ${pagination::class}")
        }

        val response =
            jiosaavn.search.getAlbumResults(q = query, p = paging.page, n = paging.pageSize)
        val albums = response.results.map { result ->
            MetadataSearchResult.Album(result.toMetadataAlbum())
        }

        val nextPagination = if (response.results.size == paging.pageSize) {
            paging.copy(page = paging.page + 1)
        } else {
            null
        }

        return PaginationResult(
            items = albums,
            totalCount = response.total,
            nextPagination = nextPagination
        )
    }

    override suspend fun searchPlaylists(
        query: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataSearchResult.Playlist> {
        val paging = when (pagination) {
            is PaginationStrategy.Page -> pagination
            null -> PaginationStrategy.Page(page = 1, pageSize = 20)
            else -> throw IllegalArgumentException("Unsupported pagination strategy: ${pagination::class}")
        }

        val response =
            jiosaavn.search.getPlaylistResults(q = query, p = paging.page, n = paging.pageSize)
        val playlists = response.results.map { result ->
            MetadataSearchResult.Playlist(result.toMetadataPlaylist())
        }

        val nextPagination = if (response.results.size == paging.pageSize) {
            paging.copy(page = paging.page + 1)
        } else {
            null
        }

        return PaginationResult(
            items = playlists,
            totalCount = response.total,
            nextPagination = nextPagination
        )
    }

    override suspend fun searchUsers(
        query: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataSearchResult.User> {
        return PaginationResult(
            items = emptyList(),
            totalCount = 0,
            nextPagination = null
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
        album = this.moreInfo.albumId?.let {
            MetadataAlbum.Detailed(
                id = IdTokenPair.asString(this.moreInfo.albumId, this.moreInfo.albumUrl!!),
                title = this.moreInfo.album!!,
                description = null,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                albumType = MetadataAlbumType.Album,
                artists = artists,
                externalUri = this.moreInfo.albumUrl,
                trackCount = 0,
                genres = emptyList(),
                releaseDate = this.year
            )
        },
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        explicit = this.explicitContent == "1",
        popularity = null,
        isrcCode = null,
        externalUri = this.permaUrl
    )
}

private fun SearchArtistResult.toMetadataArtist(): MetadataArtist.Basic {
    return MetadataArtist.Basic(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        name = this.name,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        externalUri = this.permaUrl
    )
}

private fun SearchAlbumResult.toMetadataAlbum(): MetadataAlbum.Basic {
    val artistNames = this.subtitle.split(",").map { it.trim() }
    val artists = artistNames.map { name ->
        MetadataArtist.Basic(
            id = "",
            name = name,
            thumbnails = emptyList(),
            externalUri = null
        )
    }

    return MetadataAlbum.Basic(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        description = null,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        albumType = MetadataAlbumType.Album,
        artists = artists,
        externalUri = this.permaUrl
    )
}

private fun SearchPlaylistResult.toMetadataPlaylist(): MetadataPlaylist {
    return MetadataPlaylist(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        description = this.subtitle,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        trackCount = this.numSongs ?: 0,
        externalUri = this.permaUrl,
        owner = null
    )
}
