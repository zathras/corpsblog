package com.jovial.mailchimp

import com.jovial.oauth.OAuth
import com.jovial.util.httpPostJSON
import java.io.File
import java.net.URL

/**
 * cf. http://developer.mailchimp.com/documentation/mailchimp/guides/get-started-with-mailchimp-api-3/
 *
 * Created by billf on 12/23/16.
 */
class Mailchimp (val oAuth : OAuth, val dbDir: File, val config: MailchimpClientConfig) {

    fun test() {
        val token = oAuth.getToken()

        val url = URL(config.metadata_uri)
        val headers = mapOf("Authorization" to "${token.token_type} ${token.access_token}")
        val metadataResponse = httpPostJSON(url, null, headers).readJsonValue()
        println("@@ Metadata server gives us ${metadataResponse}")
        val apiEndpoint = (metadataResponse as Map<Any, Any>)["api_endpoint"] as String
        println("@@ API endpoint is $apiEndpoint")
        throw RuntimeException("@@ todo")

        val subjectLine = "Test Using Mailchimp API"
        val newCampaignSettings = mutableMapOf<String, Any> (
                "subject_line" to subjectLine
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
        // @@@@ I am here
        // Use metadataResponse["api_endpoint"] for URL
        // http://developer.mailchimp.com/documentation/mailchimp/reference/campaigns/#create-post_campaigns
    }
}