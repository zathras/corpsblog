package com.jovial.google

import com.jovial.server.SimpleHttp
import com.jovial.util.JsonIO
import com.jovial.util.httpPostForm
import com.jovial.util.urlEncode
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * Support for OAuth2.0, as used by Google.
 * cf. https://developers.google.com/youtube/v3/guides/authentication
 *
 * Created by w.foote on 12/8/2016.
 */
class OAuth (val config : GoogleClientConfig, val dbDir : File) {

    val tokenFile = File(dbDir, "google_oauth.json")

    private var savedToken: OAuthToken? = null

    fun getToken() : OAuthToken {
        var tokenChanged = false;
        var token = savedToken
        if (token == null) {
            if (tokenFile.exists()) {
                val input = BufferedReader(InputStreamReader(FileInputStream(tokenFile), "UTF-8"))
                token = OAuthToken(JsonIO.readJSON(input))
                input.close()
            } else {
                val url = URL("http://accounts.google.com/o/oauth2/auth"
                        + "?client_id=" + urlEncode(config.client_id)
                        + "&redirect_uri=" + urlEncode("http://localhost:7001/google_oauth")
                        + "&scope=" + urlEncode("https://www.googleapis.com/auth/youtube")
                        + "&response_type=code"
                        + "&access_type=offline")

                println("@@ Hello, oauth")
                val pb = ProcessBuilder("firefox", url.toString())
                val p = pb.start()
                val result = p.isAlive()
                if (!result) {
                    println("""Unable to start "firefox $url"""")
                    println("Please start manually.")
                }
                val server = SimpleHttp(7001)
                println("Running server to wait for OAuth redirect from browser...")
                val query = server.run()
                if (!query.startsWith("/google_oauth?code=")) {
                    throw IOException("Failed to get Google authorization:  $query")
                }
                val singleUseCode = query.drop(19)
                val tokenServer = URL("https://accounts.google.com/o/oauth2/token")
                val args = mapOf(
                        "code" to singleUseCode,
                        "client_id" to config.client_id,
                        "client_secret" to config.client_secret,
                        "redirect_uri" to "http://localhost:7001/google_oauth",
                        "grant_type" to "authorization_code")
                val jsonToken = httpPostForm(tokenServer, args).jsonValue() as HashMap<Any, Any>
                token = OAuthToken(jsonToken)
                tokenChanged = true
            }
            savedToken = token
        }
        if (token.expires.time - 1000 * 60 * 5 < System.currentTimeMillis()) {  // < 5 minutes left
            println("Old google token, refreshing -- expiry was ${token.expires}")
            val tokenServer = URL("https://accounts.google.com/o/oauth2/token")
            val args = mapOf(
                    "client_id" to config.client_id,
                    "client_secret" to config.client_secret,
                    "refresh_token" to token.refresh_token,
                    "grant_type" to "refresh_token")
            val jsonResult = httpPostForm(tokenServer, args).jsonValue() as HashMap<Any, Any>
            token.refreshToken(jsonResult)
            tokenChanged = true
        }
        println("Google token expires ${token.expires}")
        if (tokenChanged) {
            val out = BufferedWriter(OutputStreamWriter(FileOutputStream(tokenFile), "UTF-8"))
            JsonIO.writeJSON(out, token.toJsonValue())
            out.close()
        }
        return token
    }

}