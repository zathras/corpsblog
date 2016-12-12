package com.jovial.google

import com.jovial.util.JsonIO
import com.jovial.util.getConnectionReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * See https://developers.google.com/youtube/v3/guides/using_resumable_upload_protocol
 * Created by billf on 12/11/16.
 */
class ResumableUpload (
        val authorization: String,
        val src : URL,
        val size : Long,
        val contentType : String,
        val dest : URL)
{
    class NoRetryIOException(m : String) : IOException(m)

    fun upload() : URL {
        val maxRetryWait = 64 * 1000;   // A bit over a minute
        var skip = 0L
        var retryWait = 500L;    // First time, wait 1 second
        while (true) {
            try {
                return tryUpload(skip)
            } catch (ex : NoRetryIOException) {
                throw ex
            } catch (ex: IOException) {
                retryWait *= 2
                if (retryWait > maxRetryWait) {
                    throw IOException("Max retry wait exceeded", ex)
                }
                Thread.sleep(retryWait)
                skip = fetchResumeStart()
            }
        }
    }

    private fun openDest() : HttpURLConnection {
        val conn = dest.openConnection() as HttpURLConnection
        conn.setRequestMethod("PUT")
        conn.setDoOutput(true)
        conn.setRequestProperty("Authorization", authorization)
        return conn
    }


    private fun tryUpload(skip : Long) : URL {
        val srcInput = src.openStream()
        var remain = skip
        while (remain > 0L) {
            remain -= srcInput.skip(remain)
            if (remain > 0L) {
                val result = srcInput.read()
                remain--
                if (result < 0) {
                    throw NoRetryIOException("Unexpected EOF skipping in $src")
                }
            }
        }
        val conn = openDest()
        if (skip == 0L) {
            conn.setRequestProperty("Content-Length", "$size")
        } else {
            conn.setRequestProperty("Content-Length", "${size-skip}")
            conn.setRequestProperty("Content-Range", "bytes ${skip}-${size-1}/$size")
        }
        conn.setRequestProperty("Content-Type", contentType)
        var outClosed = false
        var inClosed = true
        try {
            val out = conn.outputStream
            val buffer = ByteArray(256 * 256)
            while (true) {
                val len = srcInput.read(buffer)
                if (len == -1) {
                    break
                }
                out.write(buffer, 0, len)
            }
            out.close()
            srcInput.close()
            outClosed = true
            inClosed = false
            val input = getConnectionReader(conn)
            val result = JsonIO.readJSON(input)
            input.close()
            val youtubeID : String? = if (result is Map<*, *>) {
                val x = result["id"]
                if (x is String) {
                    x
                } else {
                    null
                }
            } else {
                null
            }
            inClosed = true
            if (conn.responseCode == 200) {
                if (youtubeID != null) {
                    return URL("https://www.youtube.com/watch?v=$youtubeID")
                } else {
                    throw NoRetryIOException("No id in response:  $result")
                }
            } else {
                throw IOException("Unsuccessful response code ${conn.responseCode}")
                // So, retry
            }
        } finally {
            if (!outClosed) {
                conn.outputStream.close()
                srcInput.close()
            }
            if (!inClosed) {
                getConnectionReader(conn).close()
            }
        }
    }

    /** Figure out how many bytes have already been uploaded */
    private fun fetchResumeStart() : Long {
        val conn = openDest()
        conn.setRequestProperty("Content-Length", "0")
        conn.setRequestProperty("Content-Range", "bytes */$size")
        conn.outputStream.close()
        getConnectionReader(conn).close()
        val range = conn.getHeaderField("Range")
        if (range != null && range.startsWith("bytes=0-")) {
            return range.drop(8).toLong() + 1
        } else {
            return 0L
        }
    }
}