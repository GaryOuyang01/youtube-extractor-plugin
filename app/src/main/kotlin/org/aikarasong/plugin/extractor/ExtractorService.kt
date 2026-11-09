package org.aikarasong.plugin.extractor

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.DeadObjectException
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The bound service the main app talks to: a request is a [Message] (what = [MSG_REQUEST]) whose Bundle holds the
 * JSON text under [KEY_JSON]; the reply goes to the message's replyTo with the same layout.
 */
class ExtractorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val backend by lazy { NewPipeBackend() }
    private val version by lazy { packageManager.getPackageInfo(packageName, 0).versionName.orEmpty() }
    private lateinit var messenger: Messenger

    override fun onCreate() {
        messenger =
            Messenger(
                object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        val to = msg.replyTo
                        val json = msg.data.getString(KEY_JSON)
                        if (msg.what == MSG_REQUEST && to != null && json != null) serve(json, to)
                    }
                },
            )
    }

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    private fun serve(
        json: String,
        to: Messenger,
    ) {
        scope.launch {
            val answer = Wire.handle(json, backend, version)
            val reply =
                Message.obtain(
                    null,
                    MSG_REQUEST,
                ).apply { data = Bundle().apply { putString(KEY_JSON, answer) } }
            try {
                to.send(reply)
            } catch (_: DeadObjectException) {
                // The asker is gone; nothing to deliver to.
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }

    companion object {
        const val MSG_REQUEST = 1
        const val KEY_JSON = "json"
    }
}
