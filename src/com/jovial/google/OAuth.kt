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
                val jsonToken = httpPost(tokenServer, args) as HashMap<Any, Any>
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
            val jsonResult = httpPost(tokenServer, args) as HashMap<Any, Any>
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

    //
    // Do an HTTP post and receive a JSON value in return
    //
    fun httpPost(server: URL, args: Map<String, String>) : Any {
        val content = StringBuffer()
        for ((key, value) in args) {
            if (content.length > 0)  {
                content.append("&")
            }
            content.append(key)
            content.append('=')
            content.append(urlEncode(value))
        }
        val conn = server.openConnection() as HttpURLConnection
        val contentBytes = content.toString().toByteArray(Charsets.UTF_8)
        // UTF-8 is fine since urlencoded is all in ASCII7
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", contentBytes.size.toString())
        // Content-Length is size in octets sent to the recipient.
        // cf. http://stackoverflow.com/questions/2773396/whats-the-content-length-field-in-http-header
        val os = conn.getOutputStream();
        os.write(contentBytes)
        os.close()
        val enc = conn.contentType
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
        try {
            val input = BufferedReader(InputStreamReader(conn.inputStream, charset))
            val result = JsonIO.readJSON(input)
            return result
        } catch (ex : IOException) {
            try {
                val err = BufferedReader(InputStreamReader(conn.errorStream, charset))
                println("Error from server:")
                while (true) {
                    val c = err.read()
                    if (c == -1) {
                        break;
                    }
                    print(c.toChar())
                }
                println()
                println()
            } catch (ex : Exception) {
                println("Error trying to read error message:  $ex")
            }
            throw ex
        }
    }

    private fun urlEncode(s : String) : String =
            URLEncoder.encode(s, "UTF-8")
}