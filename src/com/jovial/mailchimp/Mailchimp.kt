package com.jovial.mailchimp

import com.jovial.oauth.OAuth
import com.jovial.util.JsonIO
import com.jovial.util.httpPostJSON
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
class Mailchimp (val dbDir: File, val config: MailchimpClientConfig, val browser : String) {

    val oAuth : OAuth

    init {
        oAuth = OAuth(authURL = config.auth_uri,
                      clientId = config.client_id,
                      clientSecret = config.client_secret,
                      tokenFile = File(dbDir, "mailchimp_oauth.json"),
                      tokenURL = config.token_uri,
                      browser = browser,
                      localhostName = "127.0.0.1")

    }

    fun test() {
        val token = oAuth.getToken()

        var url = URL(config.metadata_uri)
        val headers = mapOf("Authorization" to "${token.token_type} ${token.access_token}",
                            "content-type" to "application/json")
        println(headers)
        val metadataResponse = httpPostJSON(url, null, headers).readJsonValue()
        val apiEndpoint = (metadataResponse as Map<Any, Any>)["api_endpoint"] as String

        // Create a "campaign," that is, a mass e-mail
        // http://developer.mailchimp.com/documentation/mailchimp/reference/campaigns/#create-post_campaigns
        val subjectLine = "Test Using Mailchimp API"
        val newCampaignSettings = mutableMapOf<String, Any> (
                "subject_line" to subjectLine,
                // "from_name" to "The Adventures of Burkinabè Bill",
                "from_name" to "Test Program",
                "reply_to" to "billf@jovial.com",
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
                    "image_url" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Harry_Whittier_Frees_-_What%27s_Delaying_My_Dinner.jpg/170px-Harry_Whittier_Frees_-_What%27s_Delaying_My_Dinner.jpg",
                    "description" to "This is the description field of the social_card",
                    "title" to "This is the title field of the social_card"
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

        var postData = mapOf("html" to """
<p>This is a <b>test</b> message sent via MailChimp.</p>
<p>With luck, it will post to facebook.  Here's a random link:
<a href="https://en.wikipedia.org/wiki/Lolcat">Nostalgia</a>.  OK, that's enough of a test, I
guess...  Now using hex page IDs!
""")
        url = URL(apiEndpoint + "/3.0/campaigns/$campaignId/content")
        val contentResponse = httpPostJSON(url, postData, headers, requestMethod = "PUT").readJsonValue()

        url = URL(apiEndpoint + "/3.0/campaigns/$campaignId/actions/send")
        val sendResponse = httpPostJSON(url, null, headers)
        // If not 204, there's a problem
        val rc = sendResponse.connection.responseCode
        if (rc != 204) {
            throw IOException("Unexpected response code of $rc instead of 204")
        }
        sendResponse.input.close()
    }
}