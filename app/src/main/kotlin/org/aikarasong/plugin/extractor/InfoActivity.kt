package org.aikarasong.plugin.extractor

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

/** Opens from the launcher: says what this app is and where its source code is (the GPL asks for that). */
class InfoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (resources.displayMetrics.density * PADDING_DP).toInt()
        val view =
            TextView(this).apply {
                setPadding(pad, pad * 2, pad, pad)
                textSize = TEXT_SP
                text = getString(R.string.info_body, SOURCE_URL)
                setTextIsSelectable(true)
            }
        setContentView(ScrollView(this).apply { addView(view) })
    }

    private companion object {
        const val PADDING_DP = 16
        const val TEXT_SP = 16f

        /** Where this app's source code is published. */
        const val SOURCE_URL = "https://github.com/GaryOuyang01/youtube-extractor-plugin"
    }
}
