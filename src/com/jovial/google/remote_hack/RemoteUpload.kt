package com.jovial.google.remote_hack

import com.jovial.google.ResumableUpload
import com.jovial.util.urlEncode
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * Created by billf on 12/11/16.
 */
class RemoteUpload (val command : File, val upload: ResumableUpload) {

    fun upload() : URL {
        val start = System.currentTimeMillis()
        val pb = ProcessBuilder(
                    command.toString(),
                    urlEncode(upload.authorization),
                    urlEncode(upload.src.toString()),
                    urlEncode(upload.size.toString()),
                    urlEncode(upload.contentType.toString()),
                    urlEncode(upload.dest.toString())
        )
            // I urlEncode the args because of an apparent bug somewhere, whereby a space
            // in an argument was splitting the argument into two on Linux.  Authorization
            // contains a space.
        pb.redirectError(ProcessBuilder.Redirect.INHERIT)
        val p = pb.start()
        p.outputStream.close()
        val out = BufferedReader(InputStreamReader(p.inputStream, "UTF-8"))
        val results = mutableListOf<String>()
        while (true) {
            val line = out.readLine()
            if (line == null) {
                break
            }
            results.add(line)
        }
        val exitValue = p.waitFor()
        println("Exit value is $exitValue")
        println("Time:  ${System.currentTimeMillis()-start} ms")
        if (exitValue !=0 || results.size != 1) {
            val sb = StringBuilder()
            for (l in results) {
                sb.append(l)
                sb.append('\n')
            }
            throw IOException("Remote upload failed with code $exitValue:\n$sb")
        }
        return URL(results[0])
    }
}