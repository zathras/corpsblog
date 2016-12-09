package com.jovial.google

import com.jovial.server.SimpleHttp
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

/**
 * Support for OAuth2.0, as used by Google.
 * cf. https://developers.google.com/youtube/v3/guides/authentication
 * cf. https://developers.google.com/youtube/v3/guides/uploading_a_video
 *
 * Created by w.foote on 12/8/2016.
 */
class OAuth (val config : GoogleClientConfig){

    fun run() {
        val url = URL("http://accounts.google.com/o/oauth2/auth"
                      + "?client_id=" + urlEncode(config.client_id)
                      + "&redirect_uri=" + urlEncode("http://localhost:7001/google_oauth")
                      + "&scope=" + urlEncode("https://www.googleapis.com/auth/youtube")
                      + "&response_type=code"
                      + "&access_type=offline")

        println("@@ Hello, oauth")
        println(url)
        val pb = ProcessBuilder("firefox", url.toString())
        val p = pb.start()
        val result = p.isAlive()
        if (!result || true) {
            println("""Unable to start "firefox $url"""")
        }
        val server = SimpleHttp(7001)
        println("Running server to wait for OAuth redirect from browser...")
        val query = server.run()
        println("@@ query is $query")
        // /google_oauth?error=access_denied
        // /google_oauth?error=access_denied
        Thread.sleep(1000)
        System.exit(1)
    }

    private fun urlEncode(s : String) : String =
            URLEncoder.encode(s, "UTF-8")
}