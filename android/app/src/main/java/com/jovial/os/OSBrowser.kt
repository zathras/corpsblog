package com.jovial.os

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URL

@SuppressLint("StaticFieldLeak")    // Yes, IDEA, I know.  I null out context when the activity goes away.
object OSBrowser {

    public var context : Context? = null
        private set

    private var contextAdds = 0

    public fun addContext(c: Context) {
        if (context === c) {
            contextAdds++
        } else {
            context = c
            contextAdds = 1
        }
    }

    public fun removeContext(c: Context) {
        if (context === c) {
            contextAdds--
            if (contextAdds == 0) {
                context = null
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun launch(browser: String, url: URL) {
        try {
            val c = context;
            if (c == null) {
                throw RuntimeException("Bug:  OSBrowser.context not set")
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            c.startActivity(intent);
        } catch (ex : Exception) {
            Stdout.println("Unable to start browser.")
            Stdout.println("Please start manually.")
            Stdout.println()
            Stdout.println(ex.toString())
            Stdout.println()
            Stdout.println("URL:  $url")
            Stdout.println()
        }
    }
}
