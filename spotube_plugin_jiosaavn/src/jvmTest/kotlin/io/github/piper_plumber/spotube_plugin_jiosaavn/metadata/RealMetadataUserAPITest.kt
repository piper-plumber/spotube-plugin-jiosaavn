package io.github.piper_plumber.spotube_plugin_jiosaavn.metadata

import io.github.piper_plumber.spotube_plugin_jiosaavn.test_utils.TestFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealMetadataUserAPITest {
    val client = RealMetadataUserAPI(TestFixture.authenticatedJiosaavn)

    @Test
    fun `test getUser`() = runTest {
        val user = client.getUser("")

        assertNotNull(user)
        assertTrue(user.id.isNotBlank(), "User ID should not be blank")
        assertTrue(user.username.isNotBlank(), "Username should not be blank")
    }
}
