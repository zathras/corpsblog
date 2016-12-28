package com.jovial.mailchimp

import com.jovial.oauth.OAuth
import com.jovial.util.httpPostJSON
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * cf. http://developer.mailchimp.com/documentation/mailchimp/guides/get-started-with-mailchimp-api-3/
 *
 * Created by billf on 12/23/16.
 */
class Mailchimp (val oAuth : OAuth, val dbDir: File, val config: MailchimpClientConfig) {

    fun test() {
        val token = oAuth.getToken()

        var url = URL(config.metadata_uri)
        val headers = mapOf("Authorization" to "${token.token_type} ${token.access_token}",
                            "content-type" to "application/json")
        val metadataResponse = httpPostJSON(url, null, headers).readJsonValue()
        println("@@ Metadata server gives us ${metadataResponse}")
        val apiEndpoint = (metadataResponse as Map<Any, Any>)["api_endpoint"] as String
        println("@@ API endpoint is $apiEndpoint")

        // Create a "campaign," that is, a mass e-mail
        // http://developer.mailchimp.com/documentation/mailchimp/reference/campaigns/#create-post_campaigns
        val subjectLine = "Test Using Mailchimp API"
        val newCampaignSettings = mutableMapOf<String, Any> (
                "subject_line" to subjectLine,
                "from_name" to "The Adventures of Burkinabè Bill",
                "reply_to" to "billf@jovial.com"
        )
        if (config.facebook_page_ids.size > 0) {
            newCampaignSettings["auto_fb_post"] = config.facebook_page_ids
            newCampaignSettings["fb_comments"] = true
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
        println("@@ Campaign response:  $newCampaignResponse")
        val campaignId = (newCampaignResponse as Map<Any, Any>)["id"] as String

        var postData = mapOf("html" to "<p>This is a <b>test</b> message sent via MailChimp.</p>")
        url = URL(apiEndpoint + "/3.0/campaigns/$campaignId/content")
        val contentResponse = httpPostJSON(url, postData, headers, requestMethod = "PUT").readJsonValue()
        println("@@ Content response:  $contentResponse")
        println("@@ plain text is " + (contentResponse as Map<Any, String>)["plain_text"])
        println("@@ html text is " + (contentResponse as Map<Any, String>)["html"])

        url = URL(apiEndpoint + "/3.0/campaigns/$campaignId/actions/send")
        val sendResponse = httpPostJSON(url, null, headers)
        println("@@ Response code is " + sendResponse.connection.responseCode)
        // If not 204, there's a problem
        val rc = sendResponse.connection.responseCode
        if (rc != 204) {
            throw IOException("Unexpected response code of $rc instead of 204")
        }
        sendResponse.input.close()


        throw RuntimeException("@@ todo")
        // @@@@ I am here
        // Use metadataResponse["api_endpoint"] for URL
        // http://developer.mailchimp.com/documentation/mailchimp/reference/campaigns/#create-post_campaigns
    }
}