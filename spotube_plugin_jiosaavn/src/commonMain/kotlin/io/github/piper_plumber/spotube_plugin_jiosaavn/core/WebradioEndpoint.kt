package io.github.piper_plumber.spotube_plugin_jiosaavn.core

import dev.krtirtho.plugin_interfaces.extras.spotor.SpotrClient
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.RadioResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.RadioSongResponse
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.model.RadioStation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebradioEndpoint(private val jiosaavn: Jiosaavn) {
    suspend fun createEntityStation(
        entityIds: List<String>,
        entityType: String = "queue",
    ): RadioResponse {
        val entityJson = Json.encodeToString(entityIds)
        val response = jiosaavn.client.post {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "webradio.createEntityStation",
                            "entity_id" to entityJson,
                            "entity_type" to entityType,
                        )
                    )
                )
            }
        }
        return response.body<RadioResponse.StationCreated>()
    }

    suspend fun getFeaturedStations(language: String = "hindi"): List<RadioStation> {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "webradio.getFeaturedStations",
                            "language" to language,
                        )
                    )
                )
            }
        }
        return response.body<List<RadioStation>>()
    }

    suspend fun getSong(
        stationId: String = "",
        count: Int = 5,
        next: Int = 1,
    ): RadioSongResponse {
        val response = jiosaavn.client.get {
            url {
                parameters.putAll(
                    withDefaultQueryParams(
                        mapOf(
                            "__call" to "webradio.getSong",
                            "stationid" to stationId,
                            "k" to count.toString(),
                            "next" to next.toString(),
                        )
                    )
                )
            }
        }
        return response.body<RadioSongResponse.Success>()
    }
}
