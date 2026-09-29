package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.user.MetadataUser
import dev.krtirtho.plugin_interfaces.plugin_apis.metadata.user.MetadataUserAPI
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn

class RealMetadataUserAPI(
    private val jiosaavn: Jiosaavn
) : MetadataUserAPI {

    override suspend fun getUser(id: String): MetadataUser? {
        val details = jiosaavn.user.getDetails()
        return MetadataUser(
            id = details.uid,
            username = details.customUsername ?: details.username,
            displayName = "${details.firstName} ${details.lastName}".takeIf { it.isNotBlank() },
            thumbnails = listOfNotNull(details.imageUrl).filter { it.isNotBlank() }.map {
                Thumbnail(url = it, width = 500, height = 500)
            },
            externalUri = details.permaUrl
        )
    }

}
