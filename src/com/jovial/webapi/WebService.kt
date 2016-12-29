package com.jovial.webapi

import com.jovial.util.JsonIO
import java.io.*
import java.net.URL
import java.util.*

/**
 * Created by w.foote on 12/29/2016.
 */
abstract class WebService {

    abstract protected val dbFile : File

    /**
     * Read the database file, returning a JSON value.  If there is no file, return null.
     */
    protected fun readDbFile() : Any? {
        if (!dbFile.exists()) {
            return null
        }
        val input = BufferedReader(InputStreamReader(FileInputStream(dbFile), "UTF-8"))
        val json = JsonIO.readJSON(input)
        input.close()
        return json
    }

    protected fun writeDbFile(json: Any) {
        val output = BufferedWriter(OutputStreamWriter(FileOutputStream(dbFile), "UTF8"))
        JsonIO.writeJSON(output, json)
        output.close()
    }
}