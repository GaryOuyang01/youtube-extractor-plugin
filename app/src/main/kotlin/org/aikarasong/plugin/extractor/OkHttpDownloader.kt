package org.aikarasong.plugin.extractor

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

/** The HTTP layer NewPipeExtractor needs, implemented with OkHttp. */
class OkHttpDownloader(
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
            .build(),
) : Downloader() {
    override fun execute(request: Request): Response {
        val body = request.dataToSend()?.let { localizedListBody(request.url(), it) }?.toRequestBody()
        val builder =
            okhttp3.Request.Builder()
                .method(request.httpMethod(), body)
                .url(request.url())
                .addHeader("User-Agent", USER_AGENT)
        request.headers().forEach { (name, values) ->
            builder.removeHeader(name)
            values.forEach { builder.addHeader(name, it) }
        }

        client.newCall(builder.build()).execute().use { response ->
            if (response.code == HTTP_TOO_MANY_REQUESTS) {
                throw ReCaptchaException("reCaptcha challenge requested", request.url())
            }
            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                response.body?.string(),
                response.request.url.toString(),
            )
        }
    }

    /**
     * NewPipe asks YouTube for search and browse lists with `hl=en-GB` whatever it was initialised with, so a video
     * whose creator gave a Chinese title is listed under its English one. Asking for zh-TW gets the title the
     * person sees on youtube.com.
     */
    private fun localizedListBody(
        url: String,
        data: ByteArray,
    ): ByteArray {
        if (LIST_ENDPOINTS.none { it in url }) return data
        val text = String(data, Charsets.UTF_8)
        return text.replace(FORCED_LANGUAGE, WANTED_LANGUAGE).toByteArray(Charsets.UTF_8)
    }

    private companion object {
        val LIST_ENDPOINTS = listOf("/youtubei/v1/search", "/youtubei/v1/browse")
        const val FORCED_LANGUAGE = "\"hl\":\"en-GB\""
        const val WANTED_LANGUAGE = "\"hl\":\"zh-TW\""
        const val READ_TIMEOUT_S = 30L
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:128.0) Gecko/20100101 Firefox/128.0"
    }
}
