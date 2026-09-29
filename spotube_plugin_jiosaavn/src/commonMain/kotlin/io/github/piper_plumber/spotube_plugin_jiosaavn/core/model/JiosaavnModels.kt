package io.github.piper_plumber.spotube_plugin_jiosaavn.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

data class IdTokenPair(
    val id: String,
    val token: String,
) {
    companion object {
        fun fromString(pair: String): IdTokenPair {
            val parts = pair.split("@@")
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid id-token pair format: $pair")
            }
            return IdTokenPair(
                id = parts[0],
                token = parts[1]
            )
        }

        fun id(pair: String): String {
            return pair.substringBefore("@@")
        }

        fun token(pair: String): String {
            return pair.substringAfter("@@")
        }

        fun tokenFromUrl(permaUrl: String): String {
            return permaUrl.substringAfterLast("/").takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Invalid perma URL: $permaUrl")
        }

        fun from(id: String, permaUrl: String): IdTokenPair {
            val token = permaUrl.substringAfterLast("/").takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Invalid perma URL: $permaUrl")
            return IdTokenPair(id, token)
        }

        fun asString(id: String, permaUrl: String): String {
            val token = permaUrl.substringAfterLast("/").takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Invalid perma URL: $permaUrl")
            return "$id@@$token"
        }

        fun isValidFormat(pair: String): Boolean {
            val parts = pair.split("@@")
            return parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()
        }
    }

    override fun toString(): String {
        return "$id@@$token"
    }
}

// ==================== Response Sealed Interfaces ====================

sealed interface ApiResponse {
    val success: Boolean
}

@Serializable
data class ApiSuccess(
    @SerialName("message")
    val message: String? = null,
) : ApiResponse {
    override val success: Boolean = true
}

@Serializable
data class ApiError(
    @SerialName("error")
    val error: String,
    @SerialName("message")
    val message: String? = null,
) : ApiResponse {
    override val success: Boolean = false
}

// ==================== Library Response ====================

sealed interface LibraryResponse {
    @Serializable
    data class Success(
        @SerialName("message")
        val message: String? = null,
    ) : LibraryResponse

    @Serializable
    data class Error(
        @SerialName("error")
        val error: String,
        @SerialName("message")
        val message: String? = null,
    ) : LibraryResponse
}

@Serializable
data class LibraryGetAllResponse(
    @SerialName("album")
    val albums: List<String> = emptyList(),
    @SerialName("song")
    val songs: List<String> = emptyList(),
    @SerialName("playlist")
    val playlists: List<LibraryPlaylistRef> = emptyList(),
    @SerialName("show")
    val shows: List<String> = emptyList(),
    @SerialName("artist")
    val artists: List<String> = emptyList(),
    @SerialName("user")
    val user: LibraryUser? = null,
    @SerialName("create_pl_cta_text")
    val createPlCtaText: String? = null,
)

@Serializable
data class LibraryPlaylistRef(
    @SerialName("id")
    val id: String,
    @SerialName("ts")
    val ts: String,
)

@Serializable
data class LibraryUser(
    @SerialName("fbid")
    val fbid: String? = null,
    @SerialName("firstname")
    val firstname: String? = null,
    @SerialName("lastname")
    val lastname: String? = null,
    @SerialName("uid")
    val uid: String,
    @SerialName("username")
    val username: String,
    @SerialName("follower_count")
    val followerCount: String? = null,
    @SerialName("following_count")
    val followingCount: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("initials")
    val initials: String? = null,
)

@Serializable
data class LibraryDetails(
    @SerialName("data")
    val data: LibraryDetailsData? = null,
    @SerialName("total")
    val total: Int? = null,
)

@Serializable
data class LibraryDetailsData(
    @SerialName("entity_id")
    val entityId: String? = null,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("songs")
    val songs: List<Song>? = null,
    @SerialName("count")
    val count: Int? = null,
)

@Serializable
data class LibrarySongDetails(
    @SerialName("songs")
    val songs: List<Song> = emptyList(),
)

@Serializable
data class LibraryMiniAlbum(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("image")
    val image: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("more_info")
    val moreInfo: LibraryMiniAlbumMoreInfo? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("mini_obj")
    val miniObj: Boolean? = null,
)

@Serializable
data class LibraryMiniAlbumMoreInfo(
    @SerialName("artistMap")
    val artistMap: ArtistMap? = null,
    @SerialName("contents")
    val contents: String? = null,
    @SerialName("year")
    val year: String? = null,
)

@Serializable
data class LibraryMiniPlaylist(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String? = null,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("list_count")
    val listCount: String? = null,
    @SerialName("list_type")
    val listType: String? = null,
    @SerialName("list")
    val list: List<Song>? = null,
    @SerialName("more_info")
    val moreInfo: LibraryMiniPlaylistMoreInfo? = null,
    @SerialName("button_tooltip_info")
    val buttonTooltipInfo: List<String>? = null,
    @SerialName("pro_hva_campaigns")
    val proHvaCampaigns: List<String>? = null,
)

@Serializable
data class LibraryMiniPlaylistMoreInfo(
    @SerialName("listid")
    val listId: String? = null,
    @SerialName("username")
    val username: String? = null,
    @SerialName("uid")
    val uid: String? = null,
    @SerialName("listname")
    val listName: String? = null,
    @SerialName("contents")
    val contents: String? = null,
    @SerialName("creation_date")
    val creationDate: String? = null,
    @SerialName("share")
    val share: String? = null,
    @SerialName("last_updated")
    val lastUpdated: Int? = null,
    @SerialName("favourite")
    val favourite: String? = null,
    @SerialName("owner")
    val owner: String? = null,
    @SerialName("owner_listid")
    val ownerListId: String? = null,
    @SerialName("normalized_listname")
    val normalizedListName: String? = null,
    @SerialName("follower_count")
    val followerCount: String? = null,
    @SerialName("extra_info")
    val extraInfo: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("firstname")
    val firstname: String? = null,
    @SerialName("lastname")
    val lastname: String? = null,
    @SerialName("is_followed")
    val isFollowed: String? = null,
    @SerialName("isFY")
    val isFY: Boolean? = null,
    @SerialName("video_count")
    val videoCount: String? = null,
)

@Serializable
data class LibraryMiniArtist(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("type")
    val type: String,
    @SerialName("image")
    val image: String? = null,
    @SerialName("perma_url")
    val permaUrl: String,
)

// ==================== Social Response ====================

sealed interface SocialResponse {
    @Serializable
    data class Success(
        @SerialName("message")
        val message: String? = null,
    ) : SocialResponse

    @Serializable
    data class Error(
        @SerialName("error")
        val error: String,
        @SerialName("message")
        val message: String? = null,
    ) : SocialResponse
}

@Serializable
data class FollowingDetails(
    @SerialName("data")
    val data: List<FollowedArtist>? = null,
    @SerialName("count")
    val count: Int? = null,
)

@Serializable
data class FollowedArtist(
    @SerialName("artistid")
    val artistId: String,
    @SerialName("name")
    val name: String,
    @SerialName("image")
    val image: String? = null,
    @SerialName("follower_count")
    val followerCount: Int? = null,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("is_followed")
    val isFollowed: Boolean? = null,
)

@Serializable
data class TopArtistsResponse(
    @SerialName("top_artists")
    val topArtists: List<TopArtist>,
)

@Serializable
data class TopArtist(
    @SerialName("artistid")
    val artistId: String,
    @SerialName("name")
    val name: String,
    @SerialName("image")
    val image: String? = null,
    @SerialName("follower_count")
    val followerCount: Int,
    @SerialName("is_followed")
    val isFollowed: Boolean,
    @SerialName("perma_url")
    val permaUrl: String,
)

// ==================== Song Response ====================

sealed interface AuthTokenResponse {
    @Serializable
    data class Success(
        @SerialName("auth_url")
        val authUrl: String,
        @SerialName("type")
        val type: String,
    ) : AuthTokenResponse

    @Serializable
    data class Error(
        @SerialName("error")
        val error: String,
        @SerialName("message")
        val message: String? = null,
    ) : AuthTokenResponse
}

// ==================== Webradio Response ====================

sealed interface RadioResponse {
    @Serializable
    data class StationCreated(
        @SerialName("stationid")
        val stationId: String,
    ) : RadioResponse

    @Serializable
    data class StationError(
        @SerialName("error")
        val error: String,
        @SerialName("message")
        val message: String? = null,
    ) : RadioResponse
}

sealed interface RadioSongResponse {
    @Serializable
    data class Success(
        @SerialName("stationid")
        val stationId: String,
        @SerialName("song")
        val song: Song,
    ) : RadioSongResponse

    @Serializable
    data class Error(
        @SerialName("error")
        val error: String,
        @SerialName("message")
        val message: String? = null,
        @SerialName("stationid")
        val stationId: String? = null,
    ) : RadioSongResponse
}

@Serializable
data class RadioStation(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("type")
    val type: String,
    @SerialName("image")
    val image: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("more_info")
    val moreInfo: RadioStationMoreInfo,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("mini_obj")
    val miniObj: Boolean? = null,
)

@Serializable
data class RadioStationMoreInfo(
    @SerialName("description")
    val description: String? = null,
    @SerialName("featured_station_type")
    val featuredStationType: String? = null,
    @SerialName("query")
    val query: String? = null,
    @SerialName("color")
    val color: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("station_display_text")
    val stationDisplayText: String? = null,
)

// ==================== Webapi Response ====================

@Serializable
data class LaunchData(
    @SerialName("history")
    val history: List<Song>? = null,
    @SerialName("new_trending")
    val newTrending: List<LaunchItem>? = null,
    @SerialName("top_playlists")
    val topPlaylists: List<LaunchItem>? = null,
    @SerialName("top_shows")
    val topShows: List<LaunchItem>? = null,
    @SerialName("charts")
    val charts: List<LaunchItem>? = null,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
)

// Decoupled webapi.get response types (no polymorphism)

@Serializable
data class WebapiArtistSongsResponse(
    @SerialName("songs")
    val songs: List<Song> = emptyList(),
)

@Serializable
data class WebapiArtistAlbumsResponse(
    @SerialName("albums")
    val albums: List<Album> = emptyList(),
)

@Serializable
data class WebapiPlaylistResponse(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("list_type")
    val listType: String,
    @SerialName("list")
    val list: List<Song>,
    @SerialName("more_info")
    val moreInfo: PlaylistDetailsMoreInfo,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
)

@Serializable
data class WebapiAlbumResponse(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("list_type")
    val listType: String,
    @SerialName("list")
    val list: List<Song>,
    @SerialName("more_info")
    val moreInfo: AlbumMoreInfo,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
)

@Serializable
data class WebapiSongResponse(
    @SerialName("id")
    val id: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("perma_url")
    val permaUrl: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("list_count")
    val listCount: String? = null,
    @SerialName("list_type")
    val listType: String? = null,
    @SerialName("more_info")
    val moreInfo: SongMoreInfo? = null,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
)

@Serializable
data class WebapiSongDetailsResponse(
    @SerialName("songs")
    val songs: List<Song>,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
)

@Serializable
data class WebapiShowResponse(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("list_type")
    val listType: String,
    @SerialName("season_number")
    val seasonNumber: Int? = null,
    @SerialName("total_episodes")
    val totalEpisodes: Int? = null,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
)

@Serializable
data class WebapiArtistResponse(
    @SerialName("artistId")
    val artistId: String,
    @SerialName("name")
    val name: String,
    @SerialName("title")
    val title: String? = null,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("image")
    val image: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("list_count")
    val listCount: String? = null,
    @SerialName("list_type")
    val listType: String? = null,
    @SerialName("follower_count")
    val followerCount: String? = null,
    @SerialName("fan_count")
    val fanCount: String? = null,
    @SerialName("is_followed")
    val isFollowed: Boolean? = null,
    @SerialName("isVerified")
    val isVerified: Boolean? = null,
    @SerialName("dominantLanguage")
    val dominantLanguage: String? = null,
    @SerialName("dominantType")
    val dominantType: String? = null,
    @SerialName("topSongs")
    val topSongs: List<Song>? = null,
    @SerialName("topAlbums")
    val topAlbums: List<Album>? = null,
    @SerialName("singles")
    val singles: List<Song>? = null,
    @SerialName("dedicated_artist_playlist")
    val dedicatedArtistPlaylist: List<CompactPlaylist>? = null,
    @SerialName("featured_artist_playlist")
    val featuredArtistPlaylist: List<CompactPlaylist>? = null,
    @SerialName("similarArtists")
    val similarArtists: List<SimilarArtist>? = null,
    @SerialName("modules")
    val modules: Map<String, LaunchModule>? = null,
    @SerialName("urls")
    val urls: ArtistUrls? = null,
) {
    @Serializable
    data class ArtistUrls(
        val albums: String? = null,
        val bio: String? = null,
        val comments: String? = null,
        val songs: String? = null,
        val overview: String? = null,
    )
}

@Serializable
data class SimilarArtist(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("image_url")
    val image: String? = null,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("type")
    val type: String,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("is_followed")
    val isFollowed: Boolean? = null,
    @SerialName("mini_obj")
    val miniObj: Boolean? = null,
    @SerialName("isRadioPresent")
    val isRadioPresent: Boolean? = null,
    @SerialName("dominantType")
    val dominantType: String? = null,
)

@Serializable(with = LaunchItemSerializer::class)
sealed interface LaunchItem {
    val id: String
    val title: String
    val subtitle: String?
    val type: String
    val permaUrl: String
    val image: String
    val language: String?
    val year: String?
    val playCount: String?
    val explicitContent: String?
    val listCount: String?
    val listType: String?
    val modules: Map<String, LaunchModule>?
}

@Serializable
data class LaunchSong(
    @SerialName("id")
    override val id: String,
    @SerialName("title")
    override val title: String,
    @SerialName("subtitle")
    override val subtitle: String? = null,
    @SerialName("type")
    override val type: String,
    @SerialName("perma_url")
    override val permaUrl: String,
    @SerialName("image")
    override val image: String,
    @SerialName("language")
    override val language: String? = null,
    @SerialName("year")
    override val year: String? = null,
    @SerialName("play_count")
    override val playCount: String? = null,
    @SerialName("explicit_content")
    override val explicitContent: String? = null,
    @SerialName("list_count")
    override val listCount: String? = null,
    @SerialName("list_type")
    override val listType: String? = null,
    @SerialName("more_info")
    val moreInfo: SongMoreInfo,
    @SerialName("modules")
    override val modules: Map<String, LaunchModule>? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
) : LaunchItem

@Serializable
data class LaunchAlbum(
    @SerialName("id")
    override val id: String,
    @SerialName("title")
    override val title: String,
    @SerialName("subtitle")
    override val subtitle: String? = null,
    @SerialName("type")
    override val type: String,
    @SerialName("perma_url")
    override val permaUrl: String,
    @SerialName("image")
    override val image: String,
    @SerialName("language")
    override val language: String? = null,
    @SerialName("year")
    override val year: String? = null,
    @SerialName("play_count")
    override val playCount: String? = null,
    @SerialName("explicit_content")
    override val explicitContent: String? = null,
    @SerialName("list_count")
    override val listCount: String? = null,
    @SerialName("list_type")
    override val listType: String? = null,
    @SerialName("more_info")
    val moreInfo: AlbumMoreInfo,
    @SerialName("modules")
    override val modules: Map<String, LaunchModule>? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
) : LaunchItem

@Serializable
data class LaunchPlaylist(
    @SerialName("id")
    override val id: String,
    @SerialName("title")
    override val title: String,
    @SerialName("subtitle")
    override val subtitle: String? = null,
    @SerialName("type")
    override val type: String,
    @SerialName("perma_url")
    override val permaUrl: String,
    @SerialName("image")
    override val image: String,
    @SerialName("language")
    override val language: String? = null,
    @SerialName("year")
    override val year: String? = null,
    @SerialName("play_count")
    override val playCount: String? = null,
    @SerialName("explicit_content")
    override val explicitContent: String? = null,
    @SerialName("list_count")
    override val listCount: String? = null,
    @SerialName("list_type")
    override val listType: String? = null,
    @SerialName("more_info")
    val moreInfo: PlaylistMoreInfo,
    @SerialName("modules")
    override val modules: Map<String, LaunchModule>? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
) : LaunchItem

object LaunchItemSerializer : JsonContentPolymorphicSerializer<LaunchItem>(LaunchItem::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        element is JsonObject && element["type"]?.jsonPrimitive?.content == "song" -> LaunchSong.serializer()
        element is JsonObject && element["type"]?.jsonPrimitive?.content == "album" -> LaunchAlbum.serializer()
        element is JsonObject && element["type"]?.jsonPrimitive?.content == "playlist" -> LaunchPlaylist.serializer()
        else -> LaunchSong.serializer()
    }
}

@Serializable
data class LaunchModule(
    @SerialName("source")
    val source: String? = null,
    @SerialName("position")
    val position: Int? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("highlight")
    val highlight: String? = null,
    @SerialName("simple_header")
    val simpleHeader: Boolean? = null,
    @SerialName("no_header")
    val noHeader: Boolean? = null,
    @SerialName("view_more")
    val viewMore: JsonElement? = null,
)

@Serializable
data class BrowseHoverDetails(
    @SerialName("mega_menu")
    val megaMenu: MegaMenu? = null,
)

@Serializable
data class MegaMenu(
    @SerialName("top_artists")
    val topArtists: List<MegaMenuItem>? = null,
    @SerialName("top_playlists")
    val topPlaylists: List<MegaMenuItem>? = null,
    @SerialName("new_releases")
    val newReleases: List<MegaMenuItem>? = null,
)

@Serializable
data class MegaMenuItem(
    @SerialName("title")
    val title: String,
    @SerialName("perma_url")
    val permaUrl: String,
)

// ==================== Search Response ====================

@Serializable
data class SearchResponse<T>(
    @SerialName("total")
    val total: Int,
    @SerialName("start")
    val start: Int,
    @SerialName("results")
    val results: List<T>,
)

@Serializable
data class SearchAlbumResult(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("list_type")
    val listType: String,
    @SerialName("list")
    val list: String,
    @SerialName("more_info")
    val moreInfo: SearchAlbumMoreInfo,
)

@Serializable
data class SearchAlbumMoreInfo(
    @SerialName("query")
    val query: String? = null,
    @SerialName("text")
    val text: String? = null,
    @SerialName("music")
    val music: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("artistMap")
    val artistMap: ArtistMap? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
)

@Serializable
data class SearchArtistResult(
    @SerialName("name")
    val name: String,
    @SerialName("id")
    val id: String,
    @SerialName("ctr")
    val ctr: Int,
    @SerialName("entity")
    val entity: Int,
    @SerialName("image")
    val image: String,
    @SerialName("role")
    val role: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("type")
    val type: String,
    @SerialName("mini_obj")
    val miniObj: Boolean? = null,
    @SerialName("isRadioPresent")
    val isRadioPresent: Boolean? = null,
    @SerialName("is_followed")
    val isFollowed: Boolean? = null,
)

@Serializable
data class SearchPlaylistResult(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("type")
    val type: String,
    @SerialName("image")
    val image: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("more_info")
    val moreInfo: SearchPlaylistMoreInfo,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("mini_obj")
    val miniObj: Boolean? = null,
    @SerialName("numsongs")
    val numSongs: Int? = null,
)

@Serializable
data class SearchPlaylistMoreInfo(
    @SerialName("uid")
    val uid: String? = null,
    @SerialName("firstname")
    val firstName: String? = null,
    @SerialName("artist_name")
    val artistName: List<String>? = null,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("entity_sub_type")
    val entitySubType: String? = null,
    @SerialName("video_available")
    val videoAvailable: Boolean? = null,
    @SerialName("is_dolby_content")
    val isDolbyContent: Boolean? = null,
    @SerialName("sub_types")
    val subTypes: List<String>? = null,
    @SerialName("images")
    val images: String? = null,
    @SerialName("lastname")
    val lastName: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("language")
    val language: String? = null,
)

@Serializable
data class SearchMoreResult(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String,
    @SerialName("title")
    val title: String,
    @SerialName("image_file_url")
    val imageFileUrl: String? = null,
    @SerialName("partner_name")
    val partnerName: String? = null,
    @SerialName("disable_ads")
    val disableAds: String? = null,
    @SerialName("label_name")
    val labelName: String? = null,
    @SerialName("explicit_content")
    val explicitContent: Int,
    @SerialName("song_info")
    val songInfo: String? = null,
    @SerialName("latest_season_sequence")
    val latestSeasonSequence: Int? = null,
    @SerialName("square_image_url")
    val squareImageUrl: String? = null,
    @SerialName("artists")
    val artists: List<Artist>? = null,
    @SerialName("featured_artists")
    val featuredArtists: List<Artist>? = null,
    @SerialName("primary_artists")
    val primaryArtists: List<Artist>? = null,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("subtitle")
    val subtitle: String,
)

// ==================== Shared Models ====================

@Serializable
data class JiosaavnResponse<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("total")
    val total: Int? = null,
)

@Serializable
data class Album(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String? = null,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("list_count")
    val listCount: String? = null,
    @SerialName("list_type")
    val listType: String? = null,
    @SerialName("more_info")
    val moreInfo: AlbumMoreInfo? = null,
)

@Serializable
data class AlbumMoreInfo(
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("artistMap")
    val artistMap: ArtistMap? = null,
)

@Serializable
data class Chart(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("more_info")
    val moreInfo: ChartMoreInfo,
)

@Serializable
data class ChartMoreInfo(
    @SerialName("firstname")
    val firstName: String? = null,
)

@Serializable
data class CompactPlaylist(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("more_info")
    val moreInfo: CompactPlaylistMoreInfo? = null,
    @SerialName("mini_obj")
    val miniObj: Boolean? = null,
    @SerialName("numsongs")
    val numSongs: Int? = null,
)

@Serializable
data class CompactPlaylistMoreInfo(
    @SerialName("uid")
    val uid: String? = null,
    @SerialName("firstname")
    val firstName: String? = null,
    @SerialName("lastname")
    val lastName: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("language")
    val language: String? = null,
)

@Serializable
data class Playlist(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String? = null,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String? = null,
    @SerialName("list_count")
    val listCount: String? = null,
    @SerialName("list_type")
    val listType: String? = null,
    @SerialName("more_info")
    val moreInfo: PlaylistMoreInfo,
)

@Serializable
data class PlaylistMoreInfo(
    @SerialName("listid")
    val listId: String? = null,
    @SerialName("isWeekly")
    val isWeekly: String? = null,
    @SerialName("listname")
    val listName: String? = null,
    @SerialName("firstname")
    val firstName: String? = null,

    @SerialName("uid")
    val uid: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("follower_count")
    val followerCount: String? = null,
    @SerialName("fan_count")
    val fanCount: String? = null,
)

@Serializable
data class ArtistMap(
    @SerialName("primary_artists")
    val primaryArtists: List<Artist>,
    @SerialName("featured_artists")
    val featuredArtists: List<Artist>? = null,
    @SerialName("artists")
    val artists: List<Artist>? = null,
)

@Serializable
data class Artist(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("role")
    val role: String,
    @SerialName("image")
    val image: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
)

@Serializable
data class Song(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String? = null,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("list_type")
    val listType: String? = null,
    @SerialName("list")
    val list: String? = null,
    @SerialName("more_info")
    val moreInfo: SongMoreInfo,
    @SerialName("button_tooltip_info")
    val buttonTooltipInfo: List<String>? = null,
    @SerialName("pro_hva_campaigns")
    val proHvaCampaigns: List<String>? = null,
)

@Serializable
data class SongMoreInfo(
    @SerialName("music")
    val music: String? = null,
    @SerialName("album_id")
    val albumId: String? = null,
    @SerialName("album")
    val album: String? = null,
    @SerialName("label")
    val label: String? = null,
    @SerialName("label_id")
    val labelId: String? = null,
    @SerialName("origin")
    val origin: String? = null,
    @SerialName("is_dolby_content")
    val isDolbyContent: Boolean? = null,
    @SerialName("320kbps")
    val has320kbps: String? = null,
    @SerialName("encrypted_media_url")
    val encryptedMediaUrl: String? = null,
    @SerialName("encrypted_cache_url")
    val encryptedCacheUrl: String? = null,
    @SerialName("encrypted_drm_cache_url")
    val encryptedDrmCacheUrl: String? = null,
    @SerialName("encrypted_drm_media_url")
    val encryptedDrmMediaUrl: String? = null,
    @SerialName("album_url")
    val albumUrl: String? = null,
    @SerialName("duration")
    val duration: String? = null,
    @SerialName("rights")
    val rights: Rights? = null,
    @SerialName("cache_state")
    val cacheState: String? = null,
    @SerialName("has_lyrics")
    val hasLyrics: String? = null,
    @SerialName("lyrics_snippet")
    val lyricsSnippet: String? = null,
    @SerialName("starred")
    val starred: String? = null,
    @SerialName("copyright_text")
    val copyrightText: String? = null,
    @SerialName("artistMap")
    val artistMap: ArtistMap? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("label_url")
    val labelUrl: String? = null,
    @SerialName("vcode")
    val vcode: String? = null,
    @SerialName("vlink")
    val vlink: String? = null,
    @SerialName("triller_available")
    val trillerAvailable: Boolean? = null,
    @SerialName("request_jiotune_flag")
    val requestJiotuneFlag: Boolean? = null,
    @SerialName("webp")
    val webp: String? = null,
    @SerialName("lyrics_id")
    val lyricsId: String? = null,
)

@Serializable
data class Rights(
    @SerialName("code")
    val code: String? = null,
    @SerialName("cacheable")
    val cacheable: String? = null,
    @SerialName("delete_cached_object")
    val deleteCachedObject: String? = null,
    @SerialName("reason")
    val reason: String? = null,
)

@Serializable
data class TopSearch(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("more_info")
    val moreInfo: TopSearchMoreInfo,
)

@Serializable
data class TopSearchMoreInfo(
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("artistMap")
    val artistMap: List<Artist>? = null,
)

@Serializable
data class TopShow(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("more_info")
    val moreInfo: TopShowMoreInfo,
)

@Serializable
data class TopShowMoreInfo(
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("artistMap")
    val artistMap: ArtistMap? = null,
)

@Serializable
data class Trending(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("more_info")
    val moreInfo: TrendingMoreInfo,
)

@Serializable
data class ContentGetAlbumsResponse(
    val data: List<Album>,
    val count: Int,
    @SerialName("last_page")
    val lastPage: Boolean,
)

@Serializable
data class TrendingMoreInfo(
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("artistMap")
    val artistMap: ArtistMap? = null,
)

@Serializable
data class ListeningHistory(
    @SerialName("data")
    val songs: List<Song>,
    @SerialName("last_page")
    val lastPage: Boolean? = null,
)

@Serializable
data class PlaylistDetails(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("header_desc")
    val headerDesc: String,
    @SerialName("type")
    val type: String,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("image")
    val image: String,
    @SerialName("language")
    val language: String,
    @SerialName("year")
    val year: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    @SerialName("explicit_content")
    val explicitContent: String,
    @SerialName("list_count")
    val listCount: String,
    @SerialName("list_type")
    val listType: String,
    @SerialName("list")
    val list: List<Song>,
    @SerialName("more_info")
    val moreInfo: PlaylistDetailsMoreInfo,
)

@Serializable
data class PlaylistDetailsMoreInfo(
    @SerialName("follower_count")
    val followerCount: String? = null,
    @SerialName("fan_count")
    val fanCount: String? = null,
    @SerialName("isFY")
    val isFY: Boolean? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("listid")
    val listId: String? = null,
    @SerialName("listname")
    val listName: String? = null,
    @SerialName("uid")
    val uid: String? = null,
    @SerialName("favourite")
    val favourite: String? = null,
    @SerialName("contents")
    val contents: String? = null,
    @SerialName("song_count")
    val songCount: String? = null,
    @SerialName("video_count")
    val videoCount: String? = null,
    @SerialName("share")
    val share: String? = null,
    val username: String? = null,
    @SerialName("firstname")
    val firstName: String? = null,
    @SerialName("lastname")
    val lastName: String? = null,
    @SerialName("is_followed")
    val isFollowed: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String? = null,

    )

// ==================== Playlist Operation Responses ====================

@Serializable
data class PlaylistDetailsResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("details")
    val details: PlaylistDetails,
)

@Serializable
data class PlaylistStatusResponse(
    @SerialName("status")
    val status: String,
)

// ==================== User Response ====================

@Serializable
data class UserDetails(
    @SerialName("public_playlist_count")
    val publicPlaylistCount: Int,
    @SerialName("private_playlist_count")
    val privatePlaylistCount: Int,
    @SerialName("playlist_count")
    val playlistCount: Int,
    @SerialName("firstname")
    val firstName: String,
    @SerialName("lastname")
    val lastName: String,
    @SerialName("initials")
    val initials: String,
    @SerialName("custom_username")
    val customUsername: String? = null,
    @SerialName("perma_url")
    val permaUrl: String,
    @SerialName("uid")
    val uid: String,
    @SerialName("pro")
    val pro: Int,
    @SerialName("bio_limit")
    val bioLimit: Int,
    @SerialName("username")
    val username: String,
    @SerialName("fbid")
    val fbid: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("email")
    val email: String,
    @SerialName("gender")
    val gender: String,
    @SerialName("dob")
    val dob: String,
    @SerialName("age")
    val age: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("following")
    val following: UserFollowing,
    @SerialName("followed_by")
    val followedBy: UserFollowedBy,
    @SerialName("is_followed")
    val isFollowed: Boolean,
    @SerialName("playlists")
    val playlists: List<Playlist>,
    @SerialName("status")
    val status: String,
)

@Serializable
data class UserFollowing(
    @SerialName("usersCount")
    val usersCount: Int,
    @SerialName("artistsCount")
    val artistsCount: Int,
    @SerialName("playlistsCount")
    val playlistsCount: Int,
)

@Serializable
data class UserFollowedBy(
    @SerialName("usersCount")
    val usersCount: Int,
)
