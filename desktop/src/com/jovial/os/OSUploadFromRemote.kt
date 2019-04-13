package com.jovial.os

import java.net.URL
import com.jovial.google.ResumableUpload
import com.jovial.util.urlDecode
import java.io.IOException

/**
 * Created by billf on 12/11/16.
 */
class OSUploadFromRemote (val args : List<String>) {

    fun run() : Unit {
        if (args.size != 5) {
            val sb = StringBuilder()
            for (i in 0..args.size-1) {
                sb.append("    $i:  ${args[i]}\n")
            }
            throw IOException("Expected 5 args, got ${args.size}:\n$sb")
        }
        val ru = ResumableUpload(
                    authorization = urlDecode(args[0]),
                    src = URL(urlDecode(args[1])),
                    size = urlDecode(args[2]).toLong(),
                    contentType = urlDecode(args[3]),
                    dest = URL(urlDecode(args[4]))
        )
        val url = ru.upload()
        Stdout.println(url)
    }
}