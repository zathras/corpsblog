package com.jovial.util

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

/**
 * Created by billf on 11/14/16.
 */

val MMMMyyyyFormat = SimpleDateFormat("MMMM yyyy")
val ddFormat = SimpleDateFormat("dd")

private val homeDir = System.getenv("HOME") ?: "/."
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

public fun escapeHtml(line: String) =
        line.replace("<", "&lt;", false).
                replace(">", "&gt;").
                replace("&", "&amp;")

public fun processFileName(name: String) : File {
    if (name.startsWith("~/")) {
        return File(homeDir + name.substring(1))
    } else {
        return File(name)
    }
}