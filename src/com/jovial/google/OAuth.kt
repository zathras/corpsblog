package com.jovial.google

import com.jovial.server.SimpleHttp
import com.jovial.util.JsonIO
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * Support for OAuth2.0, as used by Google.
 * cf. https://developers.google.com/youtube/v3/guides/authentication
 * cf. https://developers.google.com/youtube/v3/guides/uploading_a_video
 *
 * Created by w.foote on 12/8/2016.
 */
class OAuth (val config : GoogleClientConfig, val dbDir : File) {

    val tokenFile = File(dbDir, "google_oauth.json")

    private var token : OAuthToken? = null

    fun getToken() : OAuthToken {
        if (token == null) {
            if (tokenFile.exists()) {
                token = OAuthToken(JsonIO.readJSON(BufferedReader(FileReader(tokenFile))))
        } else {
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
                    println("Please start manually.")
                }
                val server = SimpleHttp(7001)
                println("Running server to wait for OAuth redirect from browser...")
                val query = server.run()
                println("@@ query is $query")
                if (!query.startsWith("/google_oauth?code=")) {
                    throw IOException("Failed to get Google authorization:  $query")
                }
                val singleUseCode = query.drop(19)
                val tokenServer = URL("https://accounts.google.com/o/oauth2/token")
                val args = mapOf(
                        "code" to singleUseCode,
                        "client_id" to config.client_id,
                        "client_secret" to config.client_secret,
                        "redirect_uri" to "http://localhost",
                        "grant_type" to "authorization_code")
                val jsonToken = httpPost(tokenServer, args)
                token = OAuthToken(jsonToken)
                // /google_oauth?error=access_denied
                // /google_oauth?error=access_denied
            }
        }
        // @@ Check for expiration
        return token!!
    }
    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty( "Content-Type", type );
    conn.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
    OutputStream os = conn.getOutputStream();
    os.write(encodedData.getBytes());
    private fun urlEncode(s : String) : String =
            URLEncoder.encode(s, "UTF-8")
}