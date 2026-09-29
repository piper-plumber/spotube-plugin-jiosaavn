package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.UserDetails

class UserEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun getDetails(): UserDetails {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "user.getDetails",
                            "api_version" to "4",
                        )
                    )
                )
            }
        }
        return response.body<UserDetails>()
    }
}
