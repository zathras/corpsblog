package com.jovial.os;

import com.jovial.google.ResumableUpload;
import com.jovial.util.processFileName
import com.jovial.util.urlEncode
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL



/**
 * Upload a file by executing a bash command.  This makes it possible
 * to avoid double-uploading big video files, once to the blog, and a
 * second time to YouTube.  It's a bit of a hack.
 * 
 * Here's the idea:  First, you generate the site in "offline" mode, and 
 * you push just the (new) video files to the blog's web site.  Nothing 
 * references them in the blog, so this doesn't disturb anything, but it 
 * does make the video files available on the interwebs, if you know the 
 * URL.
 *
 * Next, you run corpsblog in publish mode.  This causes OSRemoteUpload
 * commands to run.  The upload command is supposed to ssh to a place with
 * good, cheap internet connectivity.  That remote computer pulls down the
 * videos from the public blog site, and uploads them to YouTube, giving
 * you the YouTube URL.
 *
 * Finally, you push the full blog site.  Since the video files are already
 * there, git runs quickly and uses little bandwidth (except for all those
 * images :-)
 *
 * It's a neat little hack to optimize network usage, under the assumption
 * that you're publishing from a third-world country, with limited and/or
 * expensive internet.  And, it's maybe pointless, since uploading
 * videos to YouTube isn't such a big deal anymore, now that browsers
 * are better at playing mp4 video.
 *
 * That said, it was fun to figure all this out, and at the end of the
 * day, isn't that what coding is all about?  :-)
 */
public class OSRemoteUpload {
    val command: File?
    val upload : ResumableUpload

    constructor(command: String?, upload: ResumableUpload) {
        this.command = if (command == null) {
            null
        } else {
            processFileName(command, null)
        }
        this.upload = upload
    }

    val enabled : Boolean get() = command != null

    fun upload() : URL {
        assert(enabled)
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
        Stdout.println("Exit value is $exitValue")
        Stdout.println("Time:  ${System.currentTimeMillis()-start} ms")
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
