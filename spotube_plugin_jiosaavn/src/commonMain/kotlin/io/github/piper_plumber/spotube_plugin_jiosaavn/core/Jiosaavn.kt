package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.JsonContentSerializer
import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import dev.krtirtho.plugin_interfaces.host_apis.Cookie
import dev.krtirtho.plugin_interfaces.host_apis.HttpClientAPI
import kotlinx.serialization.json.Json

internal fun withDefaultQueryParams(params: Map<String, String> = emptyMap()): Map<String, String> {
    val defaultParams = mapOf(
        "_format" to "json",
        "_marker" to "0",
        "ctx" to "web6dot0",
        "api_version" to "4",
    )
    return defaultParams + params
}

class Jiosaavn(httpClientAPI: HttpClientAPI) {
    val client = SpotrClient(httpClientAPI) {
        defaultUrl = "https://www.jiosaavn.com/api.php"
        serializer = JsonContentSerializer(
            json = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }

    fun setCookies(cookies: List<Cookie>) {
        client.defaultHeaders["Cookie"] = cookies.joinToString("; ") { "${it.name}=${it.value}" }
    }

    fun setCookies(cookies: String) {
        client.defaultHeaders["Cookie"] = cookies
    }

    val content = ContentEndpoint(this)
    val library = LibraryEndpoint(this)
    val playlist = PlaylistEndpoint(this)
    val search = SearchEndpoint(this)
    val social = SocialEndpoint(this)
    val song = SongEndpoint(this)
    val user = UserEndpoint(this)
    val webapi = WebapiEndpoint(this)
    val webradio = WebradioEndpoint(this)
}
