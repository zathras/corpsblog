package com.jovial.os

import com.jovial.corpsblog.R
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object OSResources {

    fun getResourceAsStream(name: String) : InputStream {
        assert(name[0] == '/')
        val nameMatch = name.drop(1)
        val zip = ZipInputStream(OSBrowser.context!!.resources.openRawResource(R.raw.blog_resources))
        while (true) {
            val entry = zip.nextEntry
            if (entry == null) {
                throw IOException("Resource $name not found")
            } else if (entry.name == nameMatch) {
                return zip
            }
        }
    }
}
