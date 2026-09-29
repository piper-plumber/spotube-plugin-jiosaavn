package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.SocialResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.FollowingDetails
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.TopArtistsResponse

class SocialEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun follow(entityId: String, entityType: String = "playlist"): SocialResponse {
        val response = jiosaavn.client.post {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "social.follow",
                            "entity_id" to entityId,
                            "entity_type" to entityType,
                        )
                    )
                )
            }
        }
        return response.body<SocialResponse.Success>()
    }

    suspend fun unfollow(entityId: String, entityType: String = "playlist"): SocialResponse {
        val response = jiosaavn.client.post {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "social.unfollow",
                            "entity_id" to entityId,
                            "entity_type" to entityType,
                        )
                    )
                )
            }
        }
        return response.body<SocialResponse.Success>()
    }

    suspend fun getFollowingDetails(
        entityType: String = "artist",
        userId: String,
    ): FollowingDetails {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "social.getFollowingDetails",
                            "entity_type" to entityType,
                            "userid" to userId,
                        )
                    )
                )
            }
        }
        return response.body<FollowingDetails>()
    }

    suspend fun getTopArtists(): TopArtistsResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "social.getTopArtists",
                        )
                    )
                )
            }
        }
        return response.body<TopArtistsResponse>()
    }
}
