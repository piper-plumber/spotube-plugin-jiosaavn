package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationResult
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.PaginationStrategy
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylistAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.IdTokenPair
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.PlaylistDetails
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Song

class RealMetadataPlaylistAPI(
    private val jiosaavn: Jiosaavn
) : MetadataPlaylistAPI {

    override suspend fun getPlaylist(id: String): MetadataPlaylist {
        val details = jiosaavn.playlist.getDetails(listid = IdTokenPair.id(id))
        return details.toMetadataPlaylist()
    }

    override suspend fun getPlaylistTracks(
        id: String,
        pagination: PaginationStrategy?
    ): PaginationResult<MetadataTrack> {
        val paging = when (pagination) {
            is PaginationStrategy.Page -> pagination
            null -> PaginationStrategy.Page(1, 50)
            else -> throw IllegalArgumentException("Unsupported pagination strategy: $pagination")
        }

        val details =
            jiosaavn.playlist.getDetails(
                listid = IdTokenPair.id(id),
                n = paging.pageSize,
                p = paging.page
            )
        val tracks = details.list.map { it.toMetadataTrack() }
        val totalCount = details.listCount.toIntOrNull() ?: tracks.size

        return PaginationResult(
            items = tracks,
            totalCount = totalCount,
            nextPagination = if (tracks.size == paging.pageSize) {
                paging.copy(page = paging.page + 1)
            } else null
        )
    }

    override suspend fun savedPlaylists(pagination: PaginationStrategy?): PaginationResult<MetadataPlaylist> {
        val libraryIds = jiosaavn.library.getAll()
        val playlistRefs = libraryIds.playlists

        val paging = when (pagination) {
            is PaginationStrategy.Offset -> pagination
            null -> PaginationStrategy.Offset(0, 50)
            else -> throw IllegalArgumentException("Unsupported pagination strategy: $pagination")
        }

        val offset = paging.offset
        val limit = paging.limit

        val pagedRefs = playlistRefs.drop(offset).take(limit)
        val playlistIds = pagedRefs.map { it.id }
        val batches = playlistIds.chunked(50)

        val allMiniPlaylists = batches.flatMap { batch ->
            jiosaavn.library.getPlaylistDetails(batch)
        }

        val playlists = allMiniPlaylists.map { it.toMetadataPlaylistFromModel() }

        return PaginationResult(
            items = playlists,
            totalCount = playlistRefs.size,
            nextPagination = if (playlists.size == limit) {
                paging.copy(offset = offset + limit)
            } else null
        )
    }

    override suspend fun isSavedPlaylists(ids: List<String>): List<Boolean> {
        val libraryIds = jiosaavn.library.getAll()
        val savedPlaylistIds = libraryIds.playlists.map { it.id }.toSet()
        return ids.map { IdTokenPair.id(it) in savedPlaylistIds }
    }

    override suspend fun savePlaylists(ids: List<String>) {
        for (id in ids) {
            jiosaavn.social.follow(entityId = IdTokenPair.id(id), entityType = "playlist")
        }
    }

    override suspend fun removeSavedPlaylists(ids: List<String>) {
        for (id in ids) {
            jiosaavn.social.unfollow(entityId = IdTokenPair.id(id), entityType = "playlist")
        }
    }

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        isPublic: Boolean,
        isCollaborating: Boolean,
        imageBase64: String,
        trackIds: List<String>
    ): MetadataPlaylist {
        val contents = trackIds.joinToString("^") { "~~$it~" }
        val response = jiosaavn.playlist.create(
            listname = name,
            contents = contents,
            share = isPublic
        )
        return response.details.toMetadataPlaylist()
    }

    override suspend fun updatePlaylist(
        id: String,
        name: String?,
        description: String?,
        isPublic: Boolean?,
        isCollaborating: Boolean?,
        imageBase64: String?,
        trackIds: List<String>?
    ): MetadataPlaylist {
        if (name != null) {
            jiosaavn.playlist.rename(listid = IdTokenPair.id(id), listname = name)
        }
        if (isPublic != null) {
            if (isPublic) {
                jiosaavn.playlist.makePublic(listid = IdTokenPair.id(id))
            } else {
                jiosaavn.playlist.makePrivate(listid = IdTokenPair.id(id))
            }
        }
        if (!trackIds.isNullOrEmpty()) {
            val contents = trackIds.joinToString("^") { "~~${IdTokenPair.id(it)}~" }
            jiosaavn.playlist.add(listid = IdTokenPair.id(id), contents = contents)
        }
        return getPlaylist(id)
    }

    override suspend fun deletePlaylist(id: String) {
        jiosaavn.playlist.delete(listid = IdTokenPair.id(id))
    }

    override suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>) {
        val contents = trackIds.joinToString("^") { "~~${IdTokenPair.id(it)}~" }
        jiosaavn.playlist.add(listid = IdTokenPair.id(playlistId), contents = contents)
    }

    override suspend fun removeTracksFromPlaylist(playlistId: String, trackIds: List<String>) {
        for (trackId in trackIds) {
            jiosaavn.playlist.remove(
                listid = IdTokenPair.id(playlistId),
                pid = IdTokenPair.id(trackId)
            )
        }
    }
}

private fun PlaylistDetails.toMetadataPlaylist(): MetadataPlaylist {
    return MetadataPlaylist(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        description = this.headerDesc.takeIf { it.isNotBlank() } ?: this.subtitle,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        trackCount = this.listCount.toIntOrNull() ?: 0,
        externalUri = this.permaUrl,
        owner = if (this.moreInfo.uid.isNullOrBlank()) null else
            MetadataUser(
                id = this.moreInfo.uid,
                username = this.moreInfo.username!!,
                displayName = "${this.moreInfo.firstName} ${this.moreInfo.lastName}".trim(),
                thumbnails = emptyList(),
                externalUri = "https://www.jiosaavn.com/user/${this.moreInfo.uid}"
            )
    )
}

private fun io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.LibraryMiniPlaylist.toMetadataPlaylistFromModel(): MetadataPlaylist {
    return MetadataPlaylist(
        id = IdTokenPair.asString(this.id, this.permaUrl),
        title = this.title,
        description = this.subtitle,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        trackCount = this.listCount?.toIntOrNull() ?: 0,
        externalUri = this.permaUrl,
        owner = if (this.moreInfo?.uid.isNullOrBlank()) null else
            MetadataUser(
                id = this.moreInfo.uid,
                username = this.moreInfo.username.takeUnless { it.isNullOrBlank() }
                    ?: this.moreInfo.uid,
                displayName = "${this.moreInfo.firstname} ${this.moreInfo.lastname}".trim(),
                thumbnails = emptyList(),
                externalUri = "https://www.jiosaavn.com/user/${this.moreInfo.uid}"
            )
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
        album = null,
        thumbnails = listOf(Thumbnail(url = this.image, width = 500, height = 500)),
        explicit = this.explicitContent == "1",
        popularity = null,
        isrcCode = null,
        externalUri = this.permaUrl
    )
}

private fun io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.Artist.toMetadataArtistBasic(): MetadataArtist.Basic {
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
