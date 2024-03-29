package com.jovial.util

import com.jovial.blog.model.BlogConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

/**
 * Created by billf on 11/14/16.
 */

val MMMMyyyyFormat = SimpleDateFormat("MMMM yyyy")
val ddFormat = SimpleDateFormat("dd")
val ddMMMMyyyyDateFormat = SimpleDateFormat("dd MMMM yyyy")

private val homeDir = File(System.getenv("HOME") ?: "/.").canonicalPath;
// Null home?  I read about this on alt.windows.die.die.die somewhere

/**
 *  Utility for reading a string value out of a map, like the one that JsonIO gives us
 */
fun nullOK(map: Map<Any, Any>, key: Any) : String? {
    val v = map.get(key)
    if (v == null) {
        return null
    }
    if (v is String) {
        return v
    } else {
        throw IOException("Key $key has non-string value $v")
    }
}

/**
 *  Utility for reading a string value out of a map, like the one that JsonIO gives us
 */
fun notNull(map: Map<Any, Any>, key: Any) : String {
    val v = nullOK(map, key)
    if (v == null) {
        throw IOException("Key $key not found")
    } else {
        return v
    }
}

fun makeFile(defaultDir: File, name: String) : File =
        if (name.startsWith("/")) {
            File(name)
        } else {
            File(defaultDir, name)
        }

/**
 * Utility to make sure a string ends with a slash
 */
fun withSlashAtEnd(s: String) : String =
    if (s.endsWith('/')) {
        s
    } else {
        s + '/'
    }

public fun escapeHtml(line: String) =
        line.replace("<", "&lt;", false).
                replace(">", "&gt;").
                replace("&", "&amp;")

public fun processFileName(name: String, pathMap: Map<String, File>?, pseudoHomeDir: File? = null) : File {
    if (name.startsWith("~/")) {
        return File(homeDir + name.substring(1))
    } else {
        val f = mapPath(name, pathMap)
        if (pseudoHomeDir != null && !f.isAbsolute()) {
            return File(pseudoHomeDir, name).canonicalFile
        } else {
            return f.canonicalFile
        }
    }
}

private fun mapPath(name: String, pathMap: Map<String, File>?) : File {
    if (pathMap != null) {
        for (e in pathMap) {
            if (name.startsWith(e.key)) {
                return File(e.value, name.drop(e.key.length))
            }
        }
    }
    return File(name)
}

