package org.aikarasong.plugin.extractor

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WireTest {
    private class Fake : Backend {
        var lastCursor: String? = "unset"

        override fun searchPage(
            query: String,
            cursor: String?,
        ): PageDto {
            lastCursor = cursor
            return PageDto(listOf(VideoDto("abc", "歌 $query", "頻道", 200, null, 5)), if (cursor == null) "tok" else null)
        }

        override fun resolveStream(videoId: String) = StreamDto("v-$videoId", "a-$videoId")

        override fun details(videoId: String) =
            DetailsDto(listOf(TrackDto("zh", listOf(FormatDto("vtt", "u")))), "desc", listOf("ko"), listOf("zh", "ko"))

        override fun playlist(playlistId: String) = listOf(VideoDto("p1", "t", "c", 10))
    }

    private fun call(request: String) = Json.parseToJsonElement(Wire.handle(request, Fake(), "1.2.3")).jsonObject

    @Test
    fun `ping gives the version and the protocol`() {
        val r = call("""{"v":1,"id":7,"op":"ping"}""")
        assertEquals(7, r["id"]!!.jsonPrimitive.content.toInt())
        assertTrue(r["ok"]!!.jsonPrimitive.boolean)
        assertEquals("1.2.3", r["version"]!!.jsonPrimitive.content)
        assertEquals(Wire.PROTOCOL, r["protocol"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun `a search gives its videos and the next token`() {
        val r = call("""{"id":1,"op":"searchPage","query":"晴天"}""")
        assertEquals("abc", r["videos"]!!.jsonArray[0].jsonObject["id"]!!.jsonPrimitive.content)
        assertEquals("歌 晴天", r["videos"]!!.jsonArray[0].jsonObject["title"]!!.jsonPrimitive.content)
        assertEquals("tok", r["next"]!!.jsonPrimitive.content)
    }

    @Test
    fun `the cursor is handed on and the end is a null`() {
        val fake = Fake()
        val reply =
            Json.parseToJsonElement(
                Wire.handle("""{"id":2,"op":"searchPage","query":"q","cursor":"tok"}""", fake, "1"),
            )
        assertEquals("tok", fake.lastCursor)
        assertEquals("null", reply.jsonObject["next"]!!.toString())
    }

    @Test
    fun `stream details and playlist have their fields`() {
        assertEquals("v-x", call("""{"id":3,"op":"resolveStream","videoId":"x"}""")["videoUrl"]!!.jsonPrimitive.content)
        val d = call("""{"id":4,"op":"details","videoId":"x"}""")
        assertEquals("desc", d["description"]!!.jsonPrimitive.content)
        assertEquals("ko", d["autoLangs"]!!.jsonArray[0].jsonPrimitive.content)
        assertEquals(
            "p1",
            call(
                """{"id":5,"op":"playlist","playlistId":"L"}""",
            )["videos"]!!.jsonArray[0].jsonObject["id"]!!.jsonPrimitive.content,
        )
    }

    @Test
    fun `bad requests answer with an error and keep the id`() {
        val unknown = call("""{"id":9,"op":"nope"}""")
        assertFalse(unknown["ok"]!!.jsonPrimitive.boolean)
        assertEquals(9, unknown["id"]!!.jsonPrimitive.content.toInt())
        assertFalse(call("""{"id":10,"op":"details"}""")["ok"]!!.jsonPrimitive.boolean)
        val garbage = call("not json")
        assertFalse(garbage["ok"]!!.jsonPrimitive.boolean)
        assertEquals("null", garbage["id"]!!.toString())
    }

    @Test
    fun `the cursor store forgets the oldest past its capacity`() {
        val store = CursorStore<Int>(capacity = 2)
        val a = store.put(1)
        val b = store.put(2)
        val c = store.put(3)
        assertNull(store.get(a))
        assertEquals(2, store.get(b))
        assertEquals(3, store.get(c))
    }
}
