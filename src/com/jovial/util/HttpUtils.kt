package com.jovial.util

import com.jovial.server.SimpleHttp
import com.jovial.util.JsonIO
import java.io.*
import java.net.*
import java.util.*

class PostResult (
        val input : Reader,
        val connection : HttpURLConnection
) {

    fun readJsonValue() : Any {
        val result = JsonIO.readJSON(input)
        input.close()
        return result
    }

}

/**
 * Do an HTTP post of a set of A-V pairs, where the value is
 * URLEncoded.  Receive a JSON value in return
 */
fun httpPostForm(server: URL, args: Map<String, String>,
                 headers: Map<String, String> = mapOf<String, String>()) : PostResult
{
    val content = StringBuffer()
    for ((key, value) in args) {
        if (content.length > 0) {
            content.append("&")
        }
        content.append(key)
        content.append('=')
        content.append(urlEncode(value))
    }
    println("@@ posting to URL $server")
    println("@@ content:  $content")
    val headers = mapOf("Accept" to "application/json") // @@@@  Added when hacking mailchimp...
    return httpPost(server, content.toString().toByteArray(Charsets.UTF_8),
                    "application/x-www-form-urlencoded", headers)
    // UTF-8 is fine since urlencoded is all in ASCII7
}

/** Do an HTTP post of a JSON value, and receive a JSON value in return. */
fun httpPostJSON(server: URL, content: Any?,
                 headers: Map<String, String> = mapOf<String, String>(),
                 requestMethod: String = "POST") : PostResult
{
    val sw = StringWriter()
    if (content != null) {
        JsonIO.writeJSON(sw, content)
    }
    println("@@ post to $server")
    println("@@ posting $sw")
    return httpPost(server, sw.toString().toByteArray(Charsets.UTF_8),
                    "application/json; charset=UTF-8", headers, requestMethod)
}

/**
 * Do an HTTP post of a byte array.
 * Returns an HttpURLConnection with a successfully opened input stream.
 */
fun httpPost(server: URL,
             contentBytes : ByteArray,
             contentType: String,
             headers: Map<String, String>,
             requestMethod : String = "POST") : PostResult
{
    val conn = server.openConnection() as HttpURLConnection
    conn.setDoOutput(true);
    conn.setRequestMethod(requestMethod);
    println("  @@ requestMethod is $requestMethod")
    conn.setRequestProperty("Content-Type", contentType)
    conn.setRequestProperty("Content-Length", contentBytes.size.toString())
    for ((key, value) in headers) {
        conn.setRequestProperty(key, value)
        println("  @@ $key: $value")
    }
    // Content-Length is size in octets sent to the recipient.
    // cf. http://stackoverflow.com/questions/2773396/whats-the-content-length-field-in-http-header
    val os = conn.getOutputStream();
    os.write(contentBytes)
    os.close()
    return PostResult(getConnectionReader(conn), conn)
}

fun getConnectionReader(conn : HttpURLConnection) : Reader {
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
        return input
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
        } catch (ex: Exception) {
            println("Error trying to read error message:  $ex")
        }
        throw ex
    }
}

fun urlEncode(s : String) : String = URLEncoder.encode(s, "UTF-8")
fun urlDecode(s : String) : String = URLDecoder.decode(s, "UTF-8")
