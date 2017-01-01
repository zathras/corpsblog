package com.jovial.mailchimp

import com.jovial.util.JsonIO
import com.jovial.util.notNull
import com.jovial.util.nullOK
import com.jovial.util.processFileName
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.properties.Delegates.notNull

/**
 * Created by billf on 12/23/16.
 */
class MailchimpClientConfig(file : String, defaultDir: File?){
    val client_id : String
    val auth_uri : String = "https://login.mailchimp.com/oauth2/authorize"
    val token_uri : String = "https://login.mailchimp.com/oauth2/token"
    val metadata_uri : String = "https://login.mailchimp.com/oauth2/metadata"
    val client_secret : String
    val list_id : String
    val signup_url : String
    val reply_to : String   // e-mail address
    val maillist_blog_image : String    // Path relative to the blog's root URL
    val social_card_image_url : String
    val facebook_page_ids : List<String>
      // http://inlinevision.com/apps/how-to-find-your-facebook-page-id/ explains how to find a profile_id/page_id.
      // It appears that a profile_id is a page_id (as of this writing, that's not yet tested).
      // cf. http://kb.mailchimp.com/integrations/facebook/connect-or-disconnect-the-facebook-integration

    init {
        val input = BufferedReader(InputStreamReader(FileInputStream(processFileName(file, defaultDir)), "UTF-8"))
        @Suppress("UNCHECKED_CAST")
        val m = JsonIO.readJSON(input) as HashMap<Any, Any>
        input.close()
        client_id = notNull(m, "client_id")
        client_secret = notNull(m, "client_secret")
        list_id = notNull(m, "list_id")
        signup_url = notNull(m, "signup_url")
        reply_to = notNull(m, "reply_to")
        maillist_blog_image = notNull(m, "maillist_blog_image")
        social_card_image_url = notNull(m, "social_card_image_url")
        val ids = m["facebook_page_ids"]
        if (ids == null) {
            facebook_page_ids = listOf<String>()
        } else {
            @Suppress("UNCHECKED_CAST")
            facebook_page_ids = ids as List<String>
        }
    }
}
