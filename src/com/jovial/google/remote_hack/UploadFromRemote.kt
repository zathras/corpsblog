package com.jovial.google.remote_hack

import java.net.URL
import com.jovial.google.ResumableUpload
import com.jovial.util.urlDecode
import java.io.IOException

/**
 * Created by billf on 12/11/16.
 */
class UploadFromRemote (val args : Array<String>) {

    fun run() : Boolean {
        if (args.size != 6) {
            val sb = StringBuilder()
            for (i in 0..args.size-1) {
                sb.append("    $i:  ${args[i]}\n")
            }
            throw IOException("Expected 6 args, got ${args.size}:\n$sb")
            return false
        }
        val ru = ResumableUpload(
                    authorization = urlDecode(args[1]),
                    src = URL(urlDecode(args[2])),
                    size = urlDecode(args[3]).toLong(),
                    contentType = urlDecode(args[4]),
                    dest = URL(urlDecode(args[5]))
        )
        val url = ru.upload()
        println(url)
        return true
    }
}