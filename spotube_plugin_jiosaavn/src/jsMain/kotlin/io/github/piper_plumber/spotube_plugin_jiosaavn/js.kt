package io.github.piper_plumber.spotube_plugin_jiosaavn

import app.cash.zipline.Zipline
import dev.krtirtho.plugin_interfaces.core.runPluginInitialized
import dev.krtirtho.plugin_interfaces.host_apis.CryptoAPI
import dev.krtirtho.plugin_interfaces.host_apis.CryptoAPI_SERVICE_NAME
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import dev.krtirtho.plugin_interfaces.host_apis.HttpClientAPI
import dev.krtirtho.plugin_interfaces.host_apis.HttpClientAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.host_apis.PersistedStorageAPI
import dev.krtirtho.plugin_interfaces.host_apis.PersistedStorageAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.host_apis.WebViewAPI
import dev.krtirtho.plugin_interfaces.host_apis.WebViewAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.audio.AudioAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.core.CoreAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.core.CoreAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtistAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylistAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylistAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.search.MetadataSearchAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrackAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.track.MetadataTrackAPI_SERVICE_NAME
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.user.MetadataUserAPI
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.user.MetadataUserAPI_SERVICE_NAME
import io.github.piper_plumber.spotube_plugin_jiosaavn.audio.RealAudioAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealCoreAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataAlbumAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataArtistAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataBrowseAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataPlaylistAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataSearchAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataTrackAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.metadata.RealMetadataUserAPI

private val zipline by lazy { Zipline.get() }

@OptIn(ExperimentalJsExport::class)
@JsExport
fun main() {
    runPluginInitialized {
        val crypto = zipline.take<CryptoAPI>(CryptoAPI_SERVICE_NAME)
        val httpClient = zipline.take<HttpClientAPI>(HttpClientAPI_SERVICE_NAME)
        val webview = zipline.take<WebViewAPI>(WebViewAPI_SERVICE_NAME)
        val storage = zipline.take<PersistedStorageAPI>(PersistedStorageAPI_SERVICE_NAME)

        val client = Jiosaavn(httpClient)

        zipline.bind<CoreAPI>(CoreAPI_SERVICE_NAME, RealCoreAPI(webview, storage, client))

        zipline.bind<MetadataAlbumAPI>(MetadataAlbumAPI_SERVICE_NAME, RealMetadataAlbumAPI(client))
        zipline.bind<MetadataArtistAPI>(
            MetadataArtistAPI_SERVICE_NAME,
            RealMetadataArtistAPI(client)
        )
        zipline.bind<MetadataBrowseAPI>(
            MetadataBrowseAPI_SERVICE_NAME,
            RealMetadataBrowseAPI(client)
        )
        zipline.bind<MetadataPlaylistAPI>(
            MetadataPlaylistAPI_SERVICE_NAME,
            RealMetadataPlaylistAPI(client)
        )
        zipline.bind<MetadataSearchAPI>(
            MetadataSearchAPI_SERVICE_NAME,
            RealMetadataSearchAPI(client)
        )
        zipline.bind<MetadataTrackAPI>(MetadataTrackAPI_SERVICE_NAME, RealMetadataTrackAPI(client))
        zipline.bind<MetadataUserAPI>(MetadataUserAPI_SERVICE_NAME, RealMetadataUserAPI(client))

        // Audio Plugin
        zipline.bind<AudioAPI>(AudioAPI_SERVICE_NAME, RealAudioAPI(client, crypto))
    }
}
