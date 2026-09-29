package io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils

import io.github.piper_plumber.spotube_plugin_jiosaavn.BuildKonfig
import io.github.piper_plumber.spotube_plugin_jiosaavn.core.Jiosaavn

object TestFixture {
    val httpClientAPI = RealHttpClientAPI()
    val jiosaavn = Jiosaavn(httpClientAPI)

    val authenticatedJiosaavn = Jiosaavn(httpClientAPI).apply {
        setCookies(BuildKonfig.COOKIE)
    }
}