package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseGenre
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseSection
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Album
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Chart
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LaunchItem
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LaunchSong
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LaunchAlbum
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LaunchPlaylist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Playlist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.TopSearch
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Trending

class RealMetadataBrowseAPI(
    private val jiosaavn: Jiosaavn
) : MetadataBrowseAPI {

    override suspend fun featured(): List<MetadataBrowseItem> {
        val featuredPlaylists = jiosaavn.content.getFeaturedPlaylists(n = 20)
        return featuredPlaylists.map { it.toPlaylistBrowseItem() }
    }

    override suspend fun genres(): List<MetadataBrowseGenre> {
        TODO("Not yet implemented")
    }

    override suspend fun list(genreId: String, pagination: PaginationStrategy?): PaginationResult<MetadataBrowseSection> {
        val launchData = jiosaavn.webapi.getLaunchData()
        val albums = jiosaavn.content.getAlbums(n = 20)
        val topSearches = jiosaavn.content.getTopSearches()
        val trending = jiosaavn.content.getTrending()
        val charts = jiosaavn.content.getCharts()

        val sections = mutableListOf<MetadataBrowseSection>()

        launchData.newTrending?.takeIf { it.isNotEmpty() }?.let { items ->
            sections.add(
                MetadataBrowseSection(
                    title = "New Trending",
                    description = "New trending content",
                    items = items.map { it.toLaunchBrowseItem() },
                    moreLink = null
                )
            )
        }

        launchData.topPlaylists?.takeIf { it.isNotEmpty() }?.let { items ->
            sections.add(
                MetadataBrowseSection(
                    title = "Top Playlists",
                    description = "Popular playlists",
                    items = items.map { it.toLaunchBrowseItem() },
                    moreLink = null
                )
            )
        }

        launchData.charts?.takeIf { it.isNotEmpty() }?.let { items ->
            sections.add(
                MetadataBrowseSection(
                    title = "Charts",
                    description = "Top charts",
                    items = items.map { it.toLaunchBrowseItem() },
                    moreLink = null
                )
            )
        }

        launchData.topShows?.takeIf { it.isNotEmpty() }?.let { items ->
            sections.add(
                MetadataBrowseSection(
                    title = "Top Shows",
                    description = "Popular shows",
                    items = items.map { it.toLaunchBrowseItem() },
                    moreLink = null
                )
            )
        }

        if (topSearches.isNotEmpty()) {
            sections.add(
                MetadataBrowseSection(
                    title = "Top Searches",
                    description = "Most searched content",
                    items = topSearches.map { it.toTopSearchBrowseItem() },
                    moreLink = null
                )
            )
        }

        if (trending.isNotEmpty()) {
            sections.add(
                MetadataBrowseSection(
                    title = "Trending",
                    description = "Currently trending",
                    items = trending.map { it.toTrendingBrowseItem() },
                    moreLink = null
                )
            )
        }

        if (charts.isNotEmpty()) {
            sections.add(
                MetadataBrowseSection(
                    title = "Charts",
                    description = "Music charts",
                    items = charts.map { it.toChartBrowseItem() },
                    moreLink = null
                )
            )
        }

        if (albums.isNotEmpty()) {
            sections.add(
                MetadataBrowseSection(
                    title = "New Albums",
                    description = "Recently released albums",
                    items = albums.map { it.toAlbumBrowseItem() },
                    moreLink = "content.getAlbums"
                )
            )
        }

        return PaginationResult(
            items = sections,
            totalCount = sections.size,
            nextPagination = null
        )
    }

    override suspend fun sublist(
        genreId: String,
        sectionId: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataBrowseItem> {
        if (sectionId != "content.getAlbums") {
            return PaginationResult(
                items = emptyList(),
                totalCount = 0,
                nextPagination = null
            )
        }

        val limit = when (pagination) {
            is PaginationStrategy.Offset -> pagination.limit
            else -> 20
        }
        val offset = when (pagination) {
            is PaginationStrategy.Offset -> pagination.offset
            else -> 0
        }

        val p = (offset / limit) + 1

        val albums = jiosaavn.content.getAlbums(n = limit, p = p)
        val items = albums.map { it.toAlbumBrowseItem() }

        return PaginationResult(
            items = items,
            totalCount = albums.size,
            nextPagination = if (albums.size >= limit) {
                PaginationStrategy.Offset(offset + limit, limit)
            } else null
        )
    }

    private fun LaunchItem.toLaunchBrowseItem(): MetadataBrowseItem {
        return when (this) {
            is LaunchSong -> MetadataBrowseItem.Track(this.toMetadataTrack())
            is LaunchAlbum -> MetadataBrowseItem.Album(this.toMetadataAlbum())
            is LaunchPlaylist -> MetadataBrowseItem.Playlist(this.toMetadataPlaylist())
        }
    }

    private fun LaunchSong.toMetadataTrack(): dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack {
        val artists = this.moreInfo.artistMap?.primaryArtists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toArtistBasic() }
            ?: this.moreInfo.artistMap?.artists
                ?.takeIf { it.isNotEmpty() }
                ?.map { it.toArtistBasic() }
            ?: emptyList()

        return dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack(
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

    private fun LaunchAlbum.toMetadataAlbum(): MetadataAlbum.Basic {
        val artists = this.moreInfo.artistMap?.primaryArtists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toArtistBasic() }
            ?: this.moreInfo.artistMap?.artists
                ?.takeIf { it.isNotEmpty() }
                ?.map { it.toArtistBasic() }
            ?: emptyList()

        return MetadataAlbum.Basic(
            id = IdTokenPair.asString(this.id, this.permaUrl),
            title = this.title,
            description = this.subtitle,
            thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
            albumType = MetadataAlbumType.Album,
            artists = artists,
            externalUri = this.permaUrl
        )
    }

    private fun LaunchPlaylist.toMetadataPlaylist(): MetadataPlaylist {
        return MetadataPlaylist(
            id = IdTokenPair.asString(this.id, this.permaUrl),
            title = this.title,
            description = this.subtitle,
            thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
            trackCount = this.listCount?.toIntOrNull() ?: 0,
            externalUri = this.permaUrl,
            owner = null
        )
    }

    private fun TopSearch.toTopSearchBrowseItem(): MetadataBrowseItem {
        return MetadataBrowseItem.Playlist(
            MetadataPlaylist(
                id = IdTokenPair.asString(this.id, this.permaUrl),
                title = this.title,
                description = this.subtitle,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                trackCount = 0,
                externalUri = this.permaUrl,
                owner = null
            )
        )
    }

    private fun Trending.toTrendingBrowseItem(): MetadataBrowseItem {
        return MetadataBrowseItem.Playlist(
            MetadataPlaylist(
                id = IdTokenPair.asString(this.id, this.permaUrl),
                title = this.title,
                description = this.subtitle,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                trackCount = this.listCount.toIntOrNull() ?: 0,
                externalUri = this.permaUrl,
                owner = null
            )
        )
    }

    private fun Chart.toChartBrowseItem(): MetadataBrowseItem {
        return MetadataBrowseItem.Playlist(
            MetadataPlaylist(
                id = IdTokenPair.asString(this.id, this.permaUrl),
                title = this.title,
                description = this.subtitle,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                trackCount = 0,
                externalUri = this.permaUrl,
                owner = null
            )
        )
    }

    private fun Album.toAlbumBrowseItem(): MetadataBrowseItem {
        val artists = this.moreInfo?.artistMap?.primaryArtists
            ?.takeIf { it.isNotEmpty() }
            ?.map { it.toArtistBasic() }
            ?: this.moreInfo?.artistMap?.artists
                ?.takeIf { it.isNotEmpty() }
                ?.map { it.toArtistBasic() }
            ?: emptyList()

        return MetadataBrowseItem.Album(
            MetadataAlbum.Basic(
                id = IdTokenPair.asString(this.id, this.permaUrl),
                title = this.title,
                description = this.subtitle,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                albumType = MetadataAlbumType.Album,
                artists = artists,
                externalUri = this.permaUrl
            )
        )
    }

    private fun Playlist.toPlaylistBrowseItem(): MetadataBrowseItem {
        return MetadataBrowseItem.Playlist(
            MetadataPlaylist(
                id = IdTokenPair.asString(this.id, this.permaUrl),
                title = this.title,
                description = this.subtitle,
                thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
                trackCount = this.listCount?.toIntOrNull() ?: 0,
                externalUri = this.permaUrl,
                owner = this.moreInfo.uid?.let {
                    MetadataUser(
                        id = it,
                        username = it,
                        displayName = this.moreInfo.firstName,
                        thumbnails = emptyList(),
                        externalUri = "https://www.jiosaavn.com/user/${it}"
                    )
                }
            )
        )
    }

    private fun Artist.toArtistBasic(): MetadataArtist.Basic {
        return MetadataArtist.Basic(
            id = IdTokenPair.asString(this.id, this.permaUrl),
            name = this.name,
            thumbnails = listOfNotNull(this.image).map { Thumbnail(url = it, width = 500, height = 500) },
            externalUri = this.permaUrl
        )
    }
}
