package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import org.spotube.plugin_interfaces.extras.logger.Logger
import org.spotube.plugin_interfaces.extras.spotor.JsonContentSerializer
import org.spotube.plugin_interfaces.extras.spotor.SpotrClient
import org.spotube.plugin_interfaces.host_apis.Cookie
import org.spotube.plugin_interfaces.host_apis.HttpClientAPI
import org.spotube.plugin_interfaces.host_apis.PersistedStorageAPI
import org.spotube.plugin_interfaces.host_apis.WebViewAPI
import org.spotube.plugin_interfaces.plugin_apis.core.CoreAPI
import org.spotube.plugin_interfaces.plugin_apis.core.PluginUpdateInfo
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.swiftzer.semver.SemVer

@Serializable
private data class Release(
    @SerialName("tag_name") val tagName: String,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val body: String? = null,
    val assets: List<Asset> = emptyList(),
) {
    @Serializable
    data class Asset(
        val name: String,
        @SerialName("browser_download_url") val downloadUrl: String,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class RealCoreAPI(
    val webViewAPI: WebViewAPI,
    val storage: PersistedStorageAPI,
    val jiosaavn: Jiosaavn,
    httpClientAPI: HttpClientAPI,
) : CoreAPI {
    companion object {
        private val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
        private const val STORAGE_KEY = "jiosaavn_session_cookies"
        private const val RELEASES_URL =
            "https://api.github.com/repos/piper-plumber/spotube-plugin-jiosaavn/releases"
    }

    private val github = SpotrClient(httpClientAPI) {
        serializer = JsonContentSerializer(json)
    }


    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = Logger("RealCoreAPI")
    private val loggedInStateFlow = MutableStateFlow(false)
    override val requiresAuthentication: Boolean = true
    override val loggedInFlow: StateFlow<Boolean> = loggedInStateFlow.asStateFlow()

    override suspend fun checkPluginUpdates(currentVersion: SemVer): PluginUpdateInfo? {
        val releases = runCatching {
            github.get {
                url(RELEASES_URL)
                parameter("per_page", "10")
                headers {
                    append("Accept", "application/vnd.github+json")
                    append("User-Agent", "spotube-plugin-jiosaavn")
                }
            }.body<List<Release>>()
        }.getOrElse {
            logger.d { "Failed to fetch releases: ${it.message}" }
            return null
        }

        // Releases also include "nightly" builds, so only consider stable semver tags.
        val (release, version) = releases
            .asSequence()
            .filter { !it.draft && !it.prerelease }
            .mapNotNull { release ->
                runCatching { SemVer.parse(release.tagName.removePrefix("v")) }
                    .getOrNull()
                    ?.let { release to it }
            }
            .maxByOrNull { it.second }
            ?: return null

        if (version <= currentVersion) return null

        val asset = release.assets.find { it.name.endsWith(".smplug") } ?: return null
        logger.d { "Update available: $currentVersion -> $version" }

        return PluginUpdateInfo(
            latestVersion = version.toString(),
            directDownloadUrl = asset.downloadUrl,
            releaseNotes = release.body.orEmpty(),
        )
    }

    override fun supportMarkdownText(currentVersion: SemVer): String {
        return "This plugin is running on Spotube version $currentVersion. Markdown support is not available for this version."
    }

    private suspend fun storeSession(cookies: List<Cookie>) {
        logger.d { "Storing session data..." }
        val cookieHeaderStr = json.encodeToString(cookies)
        storage.putString(STORAGE_KEY, cookieHeaderStr)
        jiosaavn.setCookies(cookies)
    }

    private fun restoreSession() {
        scope.launch {
            val cookieHeaderStr = storage.getString(STORAGE_KEY)
            if (cookieHeaderStr != null) {
                logger.d { "Restoring session from stored cookies..." }
                val cookies = json.decodeFromString<List<Cookie>>(cookieHeaderStr)
                val isLoggedIn = cookies.any { it.name == "I" }
                logger.d { "Session restored, logged in: $isLoggedIn" }
                loggedInStateFlow.value = isLoggedIn
                jiosaavn.setCookies(cookies)
            } else {
                logger.d { "No session data found to restore." }
            }
        }
    }

    init {
        restoreSession()
    }

    override suspend fun login() {
        webViewAPI.webviewCreatedFlow()
            .take(1)
            .flatMapLatest {
                logger.d { "Webview created, waiting for URL change..." }
                webViewAPI.urlChangeFlow()
            }.onEach {
                val cookies = webViewAPI.getCookies("https://www.jiosaavn.com")
                // Look for "I" cookie which indicates logged in state for JioSaavn
                val isLoggedIn =
                    cookies.any { cookie -> cookie.name == "I" && cookie.value.isNotBlank() }
                logger.d { "URL changed to $it, logged in: $isLoggedIn" }
                loggedInStateFlow.value = isLoggedIn
                if (isLoggedIn) {
                    storeSession(cookies)
                    webViewAPI.exitWebView()
                }
            }.launchIn(scope)

        webViewAPI.navigateTo("https://www.jiosaavn.com/login?redirect=/")
    }

    override suspend fun logout() {
        logger.d { "Logging out, clearing session data..." }
        storage.remove(STORAGE_KEY)
        loggedInStateFlow.value = false
    }
}