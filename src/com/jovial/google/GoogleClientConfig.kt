package com.jovial.google

import com.jovial.util.JsonIO
import com.jovial.util.processFileName
import com.jovial.util.notNull
import com.jovial.util.nullOK
import java.io.*
import java.util.*

/**
 * Created by w.foote on 12/8/2016.
 */
class GoogleClientConfig(file : String, defaultDir: File?){
    val client_id : String
    val project_id : String
    val auth_uri : String
    val token_uri : String
    val auth_provider_x509_cert_url : String
    val client_secret : String
    // We don't give the list of redirect_uris.  "http://localhost" isn't interesting.

    init {
        val input = BufferedReader(InputStreamReader(FileInputStream(processFileName(file, null, defaultDir)), "UTF-8"))
        @Suppress("UNCHECKED_CAST")
        val m = JsonIO.readJSON(input) as HashMap<Any, Any>
        input.close()
        @Suppress("UNCHECKED_CAST")
        val cm = m["installed"]!! as HashMap<Any, Any>
        client_id = notNull(cm, "client_id")
        project_id = notNull(cm, "project_id")
        auth_uri = notNull(cm, "auth_uri")
        token_uri = notNull(cm, "token_uri")
        auth_provider_x509_cert_url = notNull(cm, "auth_provider_x509_cert_url")
        client_secret = notNull(cm, "client_secret")
    }
}
