package com.jovial.google

import com.jovial.server.SimpleHttp
import com.jovial.util.JsonIO
import java.io.*
import java.net.HttpURLConnection
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
                println(url)
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
                tokenChanged = true
            }
            savedToken = token
        }
        if (token.expires.time < System.currentTimeMillis() - 1000 * 60 * 5) {  // < 5 minutes left
            val tokenServer = URL("https://accounts.google.com/o/oauth2/token")
            val args = mapOf(
                    "client_id" to config.client_id,
                    "client_secret" to config.client_secret,
                    "refresh_token" to token.refresh_token,
                    "grant_type" to "refresh_token")
            val jsonResult = httpPost(tokenServer, args)
            token.refreshToken(jsonResult)
            tokenChanged = true
        }
        if (tokenChanged) {
            val out = BufferedWriter(OutputStreamWriter(FileOutputStream(tokenFile), "UTF-8"))
            JsonIO.writeJSON(out, token.toJsonValue())
            out.close()
        }
        return token
    }

    fun httpPost(server: URL, args: Map<String, String>) : String {
        val query = StringBuffer()
        for ((key, value) in args) {
            if (query.length > 0)  {
                query.append('&')
            }
            query.append(key)
            query.append("=")
            query.append(urlEncode(value))
        }
        val conn = server.openConnection() as HttpURLConnection
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", query.length.toString())
        val os = conn.getOutputStream();
        os.write(query.toString().toByteArray());  // That's UTF-8, which is OK since urlencoded is ASCII7
        os.close()
        val enc = conn.contentEncoding
        val pos = enc.indexOf("charset=", ignoreCase = true)
        val charset =
                if (pos < 0) {
                    "UTF-8"
                } else {
                    val s = enc.drop(pos + 8)
                    val p = s.indexOf(";")
                    if (p < 0) {
                        s.toUpperCase()
                    } else {
                        s.substring(0, p).toUpperCase()
                    }
                }
        val input = BufferedReader(InputStreamReader(conn.getInputStream(), charset))
        val sb = StringBuffer()
        while(true) {
            val c = input.read()
            if (c == -1) {
                break
            }
            sb.append(c.toChar())
        }
        return sb.toString()
    }

    private fun urlEncode(s : String) : String =
            URLEncoder.encode(s, "UTF-8")
}