package com.jovial.mailchimp

import com.jovial.oauth.OAuth
import java.io.File

/**
 * cf. http://developer.mailchimp.com/documentation/mailchimp/guides/get-started-with-mailchimp-api-3/
 *
 * Created by billf on 12/23/16.
 */
class Mailchimp (val oAuth : OAuth, val dbDir: File, val listID : String, val facebookPageIDs : List<String>) {

    fun test() {
        throw RuntimeException("@@ todo")
        val token = oAuth.getToken()
        val subjectLine = "Test Using Mailchimp API"
        // setRequestProperty("Authorization", "OAuth $token")
        val newCampaignSettings = mutableMapOf<String, Any> (
                "subject_line" to subjectLine
        )
        if (facebookPageIDs.size > 0) {
            newCampaignSettings["auto_fb_post"] = facebookPageIDs
            newCampaignSettings["fb_comments"] = true
        }
        val newCampaignPostData = mutableMapOf<String, Any> (
                "recipients" to mapOf<String, String> (
                        "list_id" to listID
                ),
                "type" to "regular",
                "settings" to newCampaignSettings
        )
        // @@@@ I am here
        // http://developer.mailchimp.com/documentation/mailchimp/reference/campaigns/#create-post_campaigns
    }
}