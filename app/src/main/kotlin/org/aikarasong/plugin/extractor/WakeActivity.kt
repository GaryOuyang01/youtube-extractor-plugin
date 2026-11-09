package org.aikarasong.plugin.extractor

import android.app.Activity
import android.os.Bundle

/**
 * Opens and closes at once, without a window. The app that talks to this plug-in starts it one time after the install,
 * because some phones do not start the service of an app that was never opened.
 */
class WakeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
