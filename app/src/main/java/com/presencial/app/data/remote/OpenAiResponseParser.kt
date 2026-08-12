package com.presencial.app.data.remote

import org.json.JSONObject

internal object OpenAiResponseParser {

    fun parseChatContent(body: String): String {
        val content = JSONObject(body)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
        require(content.isNotEmpty()) { "Resposta vazia da OpenAI" }
        return content
    }

    fun formatHttpError(status: Int, body: String): String = "OpenAI HTTP $status: $body"
}
