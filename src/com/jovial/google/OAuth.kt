package com.jovial.google

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
                      + "&scope=" + urlEncode("https://www.googleapis.com/")
                      + "&response_type=code"
                      + "&access_type=offline")

        println("@@ Hello, oauth")
        println(url)
        println(config)
    }

    private fun urlEncode(s : String) : String =
            URLEncoder.encode(s, "UTF-8")
}