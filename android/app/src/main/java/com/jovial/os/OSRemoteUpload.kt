
package com.jovial.os

import com.jovial.google.ResumableUpload
import java.net.URL

/**
 * Upload a file by executing a bash command.  This makes it possible
 * to avoid double-uploading big video files, once to the blog, and a
 * second time to YouTube.  It's a bit of a hack, and it's not
 * implemented on Android.  To make it work, I'd need to ssh to
 * a remote unix system from here.
 */
public class OSRemoteUpload {

    @Suppress("UNUSED_PARAMETER")
    constructor(command: String?, upload: ResumableUpload) {
    }

    val enabled : Boolean get() = false

    fun upload() : URL {
        throw RuntimeException("Attempt to upload when not enabled")
    }
}
