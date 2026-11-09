package org.aikarasong.plugin.extractor

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * The JSON the main app and this plug-in exchange (protocol version 1, see README.md).
 * Request: {"v":1,"id":n,"op":"ping|searchPage|resolveStream|details|playlist", ...fields}
 * Reply:   {"id":n,"ok":true, ...fields} or {"id":n,"ok":false,"error":"text"}
 */
object Wire {
    const val PROTOCOL = 1
    private const val MAX_ERROR = 300

    private val json = Json { encodeDefaults = true }

    fun handle(
        request: String,
        backend: Backend,
        version: String,
    ): String {
        val parsed = runCatching { json.parseToJsonElement(request).jsonObject }.getOrNull()
        val id = parsed?.get("id")
        return try {
            if (parsed == null) error("bad request")
            reply(id, answer(parsed, backend, version))
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            replyError(id, e.message ?: e.javaClass.simpleName)
        }
    }

    private fun answer(
        request: JsonObject,
        backend: Backend,
        version: String,
    ): JsonObject {
        fun text(name: String) = request[name]?.jsonPrimitive?.contentOrNull
        return when (val op = text("op")) {
            "ping" ->
                buildJsonObject {
                    put("protocol", PROTOCOL)
                    put("version", version)
                }
            "searchPage" -> obj(json.encodeToString(backend.searchPage(text("query").orEmpty(), text("cursor"))))
            "resolveStream" -> obj(json.encodeToString(backend.resolveStream(need(text("videoId")))))
            "details" -> obj(json.encodeToString(backend.details(need(text("videoId")))))
            "playlist" -> {
                val videos = backend.playlist(need(text("playlistId")))
                buildJsonObject { put("videos", json.parseToJsonElement(json.encodeToString(videos))) }
            }
            else -> error("unknown op $op")
        }
    }

    private fun need(value: String?): String = value?.takeIf { it.isNotBlank() } ?: error("missing field")

    private fun obj(text: String): JsonObject = json.parseToJsonElement(text).jsonObject

    private fun reply(
        id: JsonElement?,
        body: JsonObject,
    ): String =
        JsonObject(
            buildMap {
                put("id", id ?: JsonNull)
                put("ok", JsonPrimitive(true))
                putAll(body)
            },
        ).toString()

    private fun replyError(
        id: JsonElement?,
        message: String,
    ): String =
        buildJsonObject {
            put("id", id ?: JsonNull)
            put("ok", false)
            put("error", message.take(MAX_ERROR))
        }.toString()
}
