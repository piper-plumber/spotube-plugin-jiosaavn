package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.AuthTokenResponse

class SongEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun generateAuthToken(
        encryptedMediaUrl: String,
        bitrate: String = "128",
    ): AuthTokenResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "song.generateAuthToken",
                            "url" to encryptedMediaUrl,
                            "bitrate" to bitrate,
                        )
                    )
                )
            }
        }
        return response.body<AuthTokenResponse.Success>()
    }
}
