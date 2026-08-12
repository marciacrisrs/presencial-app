package com.presencial.app.data.remote

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class OpenAiResponseParserTest {

    @Test
    fun `parseChatContent should extract message content`() {
        val body = """
            {
              "choices": [
                {
                  "message": {
                    "content": "  📅 Mensagem de teste  "
                  }
                }
              ]
            }
        """.trimIndent()

        assertEquals("📅 Mensagem de teste", OpenAiResponseParser.parseChatContent(body))
    }

    @Test
    fun `parseChatContent should fail on empty content`() {
        val body = """
            {
              "choices": [
                {
                  "message": {
                    "content": "   "
                  }
                }
              ]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            OpenAiResponseParser.parseChatContent(body)
        }
    }

    @Test
    fun `formatHttpError should include status`() {
        assertEquals(
            "OpenAI HTTP 401: unauthorized",
            OpenAiResponseParser.formatHttpError(401, "unauthorized")
        )
    }
}
