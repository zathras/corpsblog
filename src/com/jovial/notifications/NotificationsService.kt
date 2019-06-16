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
 * a new post, and value2 set to the summary text of the post.  It's meant for this
 * to go to a Facebook Pages page, via "Create a link post".
 *
 * Created by billf on 6/15/19
 */
abstract class NotificationsService ()
    : WebService()
{
    protected val postsSent : MutableSet<String> by lazy { // Contains the absolute path of the generated posts
        val result = mutableSetOf<String>()
        val m = readDbFile()
        if (m != null) {
            @Suppress("UNCHECKED_CAST")
            for (p in (m as List<Any>)) {
                result+= p as String
            }
        }
        writeSentFile(result)      // To make sure we can
        result
    }

    protected fun writeSentFile(sent : Set<String> = postsSent) {
        val json = sent.toList()
        writeDbFile(json)
    }

    /**
     *  Check notifications for site, and tell the user (via site.note())
     */
    fun checkNotifications(site : Site) {
        for (p in site.posts) {
            if (!postsSent.contains(p.outputFile.absolutePath)) {
                site.note("Pending ifttt/FB notification for ${p.outputFile.path}")
            }
        }
    }

    abstract protected val serviceName : String

    protected fun getPendingPostNotifications(site : Site) : List<Post> {
        val pending = mutableListOf<Post>()
        for (p in site.posts) {
            if (!postsSent.contains(p.outputFile.absolutePath)) {
                pending.add(p)
                // site.posts is already sorted by date, so we shouldn't re-sort
            }
        }
        if (pending.isEmpty()) {
            Stdout.println("No $serviceName notification is pending.")
        } else {
            Stdout.println("Sending $serviceName notification for ${pending.size} post(s).")
        }
        return pending
    }
}
