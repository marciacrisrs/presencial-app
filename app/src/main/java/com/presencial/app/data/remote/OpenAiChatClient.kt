package com.presencial.app.data.remote

import com.presencial.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiChatClient @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun chatCompletion(
        apiKey: String,
        systemPrompt: String,
        userPrompt: String
    ): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val connection = (URL(CHAT_COMPLETIONS_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
            }

            val payload = JSONObject().apply {
                put("model", MODEL)
                put("temperature", TEMPERATURE)
                put("max_tokens", MAX_TOKENS)
                put(
                    "messages",
                    JSONArray().apply {
                        put(JSONObject().put("role", "system").put("content", systemPrompt))
                        put(JSONObject().put("role", "user").put("content", userPrompt))
                    }
                )
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
            }

            val status = connection.responseCode
            val stream = if (status in HTTP_SUCCESS_RANGE) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream.bufferedReader().use(BufferedReader::readText)
            if (status !in HTTP_SUCCESS_RANGE) {
                error(OpenAiResponseParser.formatHttpError(status, body))
            }

            OpenAiResponseParser.parseChatContent(body)
        }
    }

    companion object {
        private const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-4o-mini"
        private const val TEMPERATURE = 0.7
        private const val MAX_TOKENS = 220
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 20_000
        private val HTTP_SUCCESS_RANGE = 200..299
    }
}
