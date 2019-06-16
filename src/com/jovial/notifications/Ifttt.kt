package com.jovial.notifications

import com.jovial.blog.Site
import com.jovial.os.Stdout
import com.jovial.util.ddMMMMyyyyDateFormat
import com.jovial.util.httpPostJSON
import com.jovial.webapi.WebService
import com.jovial.templates.Post
import com.jovial.util.JsonIO
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * An ifttt service.  This posts to a Maker Webhook, with value1 set to the URL of
 * a new post, and value2 set to "o.  It's meant for this
 * to go to a Facebook Pages page, via "Create a link post".
 *
 * Created by billf on 6/15/19
 */
class Ifttt (dbDir: File, val config: IftttClientConfig)
    : NotificationsService()
{
    protected override val dbFile = File(dbDir, "ifttt.json")
    override val serviceName = "Ifttt/FB"

    /**
     *  Generate notifications for site.  If site.publish is false and notifications are pending,
     *  note this (via site.note())
     */
    fun generateNotifications(site : Site) {
        val pending = getPendingPostNotifications(site)

        val baseURL = site.blogConfig.siteBaseURL
        var iftttUrl = URL(config.post_uri + config.client_key)
        val headers = mapOf("Content-Type" to "application/json")
        for (p in pending) {
            val postValues = mapOf(
                "value1" to baseURL + "posts/" + p.outputFile.name,
                "value2" to ""      // p.synopsis appears with the link anyway
            )
            val response = httpPostJSON(iftttUrl, postValues, headers)
            // If not 204, there's a problem
            val rc = response.connection.responseCode
            if (rc < 200 || rc > 299) {
                throw IOException("Unexpected response code of $rc not between 200 and 299")
            }
            val responseText = response.input.readText()

            Stdout.println("    Got from ifttt:  $responseText")
            postsSent += p.outputFile.absolutePath
            writeSentFile()
                // Yes, we might write multiple times.  This should be rare, and this way, we
                // won't repeat posts if the internet drops in the middle.
        }
    }
}
