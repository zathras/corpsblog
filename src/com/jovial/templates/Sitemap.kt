package com.jovial.templates

import com.jovial.blog.Site
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by billf on 11/7/16.
 */

private val dateFormat = SimpleDateFormat("yyyy-mm-dd")

class Sitemap (val site : Site, val indexDate: Date){

    fun generate() : String {
        val sb = StringBuffer()
        sb.append(
"""<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">""")
        addFile(sb, "index.html", indexDate)
        for (p in site.posts) {
            addFile(sb, "${p.pathTo}/${p.outputFile.name}", p.date)
        }
        // We don't add feed.xml to the sitemap.
        sb.append("\n</urlset>\n")
        return sb.toString()
    }

    private fun addFile(sb: StringBuffer, fileName: String, date: Date) {
        sb.append("""
    <url>
        <loc>${site.blogConfig.siteBaseURL}/${fileName}</loc>
        <lastmod>${dateFormat.format(date)}</lastmod>
    </url>""")
    }
}
