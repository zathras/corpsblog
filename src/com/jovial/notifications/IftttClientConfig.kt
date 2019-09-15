package com.jovial.notifications

import com.jovial.util.JsonIO
import com.jovial.util.notNull
import com.jovial.util.processFileName
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

/**
 * Config for sending notifications to a Facebook "Pages" page via IFTTT
 */
class IftttClientConfig(file : String, defaultDir: File?){
    val client_key : String
    val post_uri : String = "https://maker.ifttt.com/trigger/post/with/key/"

    init {
        val input = BufferedReader(InputStreamReader(FileInputStream(processFileName(file, null, defaultDir)), "UTF-8"))
        @Suppress("UNCHECKED_CAST")
        val m = JsonIO.readJSON(input) as HashMap<Any, Any>
        input.close()
        client_key = notNull(m, "client_key")
    }
}
