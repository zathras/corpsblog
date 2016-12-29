package com.jovial.webapi

import com.jovial.server.SimpleHttp
import com.jovial.util.JsonIO
import com.jovial.util.httpPostForm
import com.jovial.util.urlEncode
import java.io.*
import java.net.URL
import java.util.*

/**
 * Support for OAuth2.0, as used by Google and Mailchimp.
 * cf. https://developers.google.com/youtube/v3/guides/authentication
 * cf. https://apidocs.mailchimp.com/oauth2/
 *
 * Created by w.foote on 12/8/2016.
 */
class OAuth (val authURL : String,
             val clientId : String,
             val clientSecret : String,
             val tokenFile : File,
             val authParams : String = "",
             val tokenURL : String,
             val browser : String,
             val localhostName : String)
{
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
                val url = URL(authURL
                                + "?client_id=" + urlEncode(clientId)
                                + "&redirect_uri=" + urlEncode("http://$localhostName:7001/corpsblog_oauth")
                                + "&response_type=code"
                                + authParams)

                println("@@ Sending browser to $url")
                val pb = ProcessBuilder(browser, url.toString())
                try {
                    val p = pb.start()
                } catch (ex : IOException) {
                    println("Unable to start $browser")
                    println("Please start manually.")
                    println()
                    println(ex)
                    println()
                    println("URL:  $url")
                    println()
                }
                val server = SimpleHttp(7001)
                println("Running server to wait for OAuth redirect from browser...")
                val query = server.run()
                val queryStart = "/corpsblog_oauth?code="
                if (!query.startsWith(queryStart)) {
                    throw IOException("Failed to get authorization:  $query")
                }
                val singleUseCode = query.drop(queryStart.length)
                val tokenServer = URL(tokenURL)
                val args = mapOf(
                        "code" to singleUseCode,
                        "client_id" to clientId,
                        "client_secret" to clientSecret,
                        "redirect_uri" to "http://$localhostName:7001/corpsblog_oauth",
                        "grant_type" to "authorization_code")
                val jsonToken = httpPostForm(tokenServer, args).readJsonValue() as HashMap<Any, Any>
                token = OAuthToken(jsonToken)
                tokenChanged = true
            }
            savedToken = token
        }
        if (token.expires.time - 1000 * 60 * 5 < System.currentTimeMillis()) {  // < 5 minutes left
            val rt = token.refresh_token
            if (rt == null) {
                throw IOException("Expired token, refresh_token not given")
            }
            println("Old oauth token, refreshing -- expiry was ${token.expires}")
            val tokenServer = URL(tokenURL)
            val args = mapOf(
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "refresh_token" to rt,
                    "grant_type" to "refresh_token")
            val jsonResult = httpPostForm(tokenServer, args).readJsonValue() as HashMap<Any, Any>
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
}