package com.presencial.app.data.remote

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection

class OpenAiChatClientTest {

    @Test
    fun `inject constructor should create client with default connection factory`() {
        val client = OpenAiChatClient(Dispatchers.Unconfined)
        assertNotNull(client)
    }

    @Test
    fun `chatCompletion should parse successful response`() = runTest {
        val connection = mockConnection(
            status = 200,
            body = """
                {
                  "choices": [
                    { "message": { "content": "📅 Mensagem gerada" } }
                  ]
                }
            """.trimIndent()
        )
        val client = OpenAiChatClient.create(
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
            connectionFactory = { connection }
        )

        val result = client.chatCompletion("sk-test", "system", "user")

        assertEquals("📅 Mensagem gerada", result.getOrNull())
        verify { connection.setRequestProperty("Authorization", "Bearer sk-test") }
        verify { connection.inputStream }
    }

    @Test
    fun `chatCompletion should fail on http error`() = runTest {
        val connection = mockConnection(status = 401, body = "unauthorized")
        val client = OpenAiChatClient.create(
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
            connectionFactory = { connection }
        )

        val result = client.chatCompletion("sk-test", "system", "user")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("401") == true)
        verify { connection.errorStream }
    }

    private fun mockConnection(status: Int, body: String): HttpURLConnection {
        val connection = mockk<HttpURLConnection>(relaxed = true)
        every { connection.outputStream } returns ByteArrayOutputStream()
        every { connection.responseCode } returns status
        every { connection.inputStream } returns ByteArrayInputStream(body.toByteArray())
        every { connection.errorStream } returns ByteArrayInputStream(body.toByteArray())
        return connection
    }
}
