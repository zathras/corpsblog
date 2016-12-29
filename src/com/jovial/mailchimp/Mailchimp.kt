package com.jovial.mailchimp

import com.jovial.blog.Site
import com.jovial.webapi.OAuth
import com.jovial.util.JsonIO
import com.jovial.util.ddMMMMyyyyDateFormat
import com.jovial.util.httpPostJSON
import com.jovial.webapi.WebService
import templates.Post
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * cf. http://developer.mailchimp.com/documentation/mailchimp/guides/get-started-with-mailchimp-api-3/
 *
 * Created by billf on 12/23/16.
 */
class Mailchimp (val dbDir: File, val config: MailchimpClientConfig, val browser : String)
    : WebService()
{

    val oAuth : OAuth

    protected override val dbFile = File(dbDir, "mailchimp.json")

    val postsMailed = mutableSetOf<String>()
    // Contains the absolute path of the generated posts

    init {
        oAuth = OAuth(authURL = config.auth_uri,
                      clientId = config.client_id,
                      clientSecret = config.client_secret,
                      tokenFile = File(dbDir, "mailchimp_oauth.json"),
                      tokenURL = config.token_uri,
                      browser = browser,
                      localhostName = "127.0.0.1")

        val m = readDbFile()
        if (m != null) {
            for (p in (m as List<Any>)) {
                postsMailed += p as String
            }
        }
        writeMailedFile()      // To make sure we can
    }

    private fun writeMailedFile() {
        val json = postsMailed.toList()
        writeDbFile(json)
    }

    /**
     *  Generate notifications for site.  If site.publish is false and notifications are pending,
     *  note this (via site.note())
     */
    fun generateNotifications(site : Site) {
        val pending = mutableListOf<Post>()
        for (p in site.posts) {
            if (!postsMailed.contains(p.outputFile.absolutePath)) {
                pending.add(p)
                // site.posts is already sorted by date, so we shouldn't re-sort
            }
        }
        if (pending.isEmpty()) {
            return
        }
        if (!site.readyToSendToMailList()) {
            for (p in pending) {
                site.note("Pending mail list notification for ${p.outputFile.path}")
            }
            return
        }
        println("Sending mail list notification for ${pending.size} post(s).")

        val subject = if (pending.size > 1) {
            "${pending.size} new posts on ${site.blogConfig.siteTitle}"
        } else {
            "New post on ${site.blogConfig.siteTitle}"
        }
        val fromName = site.blogConfig.siteTitle
        val html = StringBuilder()
        // I don't generate plaintext, because Mailchip does a fine job of it.
        val baseURL = site.blogConfig.siteBaseURL + "/"
        for (p in pending) {
            html.append("<h2>${p.title}</h2>\n")
            html.append("<p><i>${ddMMMMyyyyDateFormat.format(p.date)}</i></p>\n")
            html.append("<p>${p.synopsis}</p>\n")
            val url = baseURL + "posts/" + p.outputFile.name
            html.append("""<p>Read post:  <a href="$url">$url</a>${'\n'}""")
        }
        html.append("<p>&nbsp;</p>\n")
        html.append("<p>&nbsp;</p>\n")
        val imageURL = baseURL + config.maillist_blog_image
        html.append("""<p><a href="$baseURL"><img src="$imageURL"></a></p>${'\n'}""")

        sendMessage(subject, fromName, html.toString())

        for (p in pending) {
            postsMailed += p.outputFile.absolutePath
        }
        writeMailedFile()
    }

    private fun sendMessage(subjectLine: String, fromName: String, bodyHtml: String) {
        val token = oAuth.getToken()

        var url = URL(config.metadata_uri)
        val headers = mapOf("Authorization" to "${token.token_type} ${token.access_token}",
                            "content-type" to "application/json")
        val metadataResponse = httpPostJSON(url, null, headers).readJsonValue()
        val apiEndpoint = (metadataResponse as Map<Any, Any>)["api_endpoint"] as String
        println("Got Mailchimp API endpoint:  $apiEndpoint")

        // Create a "campaign," that is, a mass e-mail
        // http://developer.mailchimp.com/documentation/mailchimp/reference/campaigns/#create-post_campaigns
        val newCampaignSettings = mutableMapOf<String, Any> (
                "subject_line" to subjectLine,
                "from_name" to fromName,
                "reply_to" to config.reply_to,
                "type" to "regular"
        )
        if (config.facebook_page_ids.size > 0) {
            // As of 12/28/16, Mailchimp support said there "was an issue with" posting to Facebook.
            // I take that to mean "it's broken."  They didn't say when it might be fixed, or give me
            // a way of tracking the issue.  The format of the strings in auto_fb_post is undocumented,
            // but it's maybe a decimal or a hex number?
            newCampaignSettings["auto_fb_post"] = config.facebook_page_ids
            newCampaignSettings["fb_comments"] = true
            newCampaignSettings["social_card"] = mapOf(
                    "image_url" to config.social_card_image_url,
                    "description" to subjectLine,
                    "title" to fromName
            )
        }
        val newCampaignPostData = mutableMapOf<String, Any> (
                "recipients" to mapOf<String, String> (
                        "list_id" to config.list_id
                ),
                "type" to "regular",
                "settings" to newCampaignSettings
        )
        url = URL(apiEndpoint + "/3.0/campaigns")
        val newCampaignResponse = httpPostJSON(url, newCampaignPostData, headers).readJsonValue()
        val campaignId = (newCampaignResponse as Map<Any, Any>)["id"] as String
        println("Created campaign $campaignId.")

        val postData = mapOf("html" to bodyHtml)
        url = URL(apiEndpoint + "/3.0/campaigns/$campaignId/content")
        val bodyResponse = httpPostJSON(url, postData, headers, requestMethod = "PUT").readJsonValue() as Map<Any, Any>
        if (bodyResponse["html"] == null || bodyResponse["plain_text"] == null) {
            throw IOException("Unexpected body response:  $bodyResponse")
        }
        // We don't use the content response, but it contains fields like "plain_text" and "html".

        url = URL(apiEndpoint + "/3.0/campaigns/$campaignId/actions/send")
        val sendResponse = httpPostJSON(url, null, headers)
        // If not 204, there's a problem
        val rc = sendResponse.connection.responseCode
        if (rc != 204) {
            throw IOException("Unexpected response code of $rc instead of 204")
        }
        sendResponse.input.close()
        println("Sent campaign.")
    }
}