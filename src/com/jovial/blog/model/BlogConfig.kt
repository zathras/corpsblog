package com.jovial.blog.model

import com.jovial.google.GoogleClientConfig
import com.jovial.mailchimp.MailchimpClientConfig
import com.jovial.util.JsonIO
import com.jovial.util.notNull
import com.jovial.util.nullOK
import com.jovial.util.processFileName
import java.io.*
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 */

class BlogConfig(configFile: File) {

    public val siteBaseURL : String
    public val siteDescription : String
    public val siteAuthor : String
    public val siteTitle: String
    public val siteTitleBodyHTML: String?
    public val siteTitleBodyImage: String?
    public val myProfilePhoto : String
    public val coverImage : String
    public val shareTwitter : String?
    public val shareLinkedIn : String?
    public val shareGitHub : String?
    public val shareFlickr : String?
    public val shareGarmin : String?    // The GPS company.
    public val shareEmail : String?     // This is a "contact me" e-mail address
    public val shareDisqus : String?    // Blog comments, https://en.wikipedia.org/wiki/DisqusA
                                        // Ad-suported by taking a cut from "reveal," but blogs can opt out.
    public val googleAnalyticsAccount : String?
    public val googleOauthBrowser : String         // Used for Google OAuth login flow.  Defaults to firefox.
    public val googleClient : GoogleClientConfig?
    public val mailchimpClient : MailchimpClientConfig?
    public val mailchimpOauthBrowser : String         // Used for Mailchimp OAuth login flow.  Defaults to firefox.

    /**
     * Name of the remote shell command for YouTube uploads, if one exists.  See
     * com.jovial.google.remote_hack.
     */
    val remote_upload: String?

    init {
        try {
            val input = BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8"))
            val m = JsonIO.readJSON(input) as HashMap<Any, Any>
            input.close()
            siteBaseURL = notNull(m, "siteBaseURL")
            siteDescription = notNull(m, "siteDescription")
            siteAuthor = notNull(m, "siteAuthor")
            siteTitle = notNull(m, "siteTitle")
            siteTitleBodyHTML = nullOK(m, "siteTitleBodyHTML")
            siteTitleBodyImage = nullOK(m, "siteTitleBodyImage")
            myProfilePhoto = notNull(m, "myProfilePhoto")
            coverImage = notNull(m, "coverImage")
            shareTwitter = nullOK(m, "shareTwitter")
            shareLinkedIn = nullOK(m, "shareLinkedIn")
            shareGitHub = nullOK(m, "shareGitHub")
            shareFlickr = nullOK(m, "shareFlickr")
            shareGarmin = nullOK(m, "shareGarmin")
            shareEmail = nullOK(m, "shareEmail")
            shareDisqus = nullOK(m, "shareDisqus")
            googleAnalyticsAccount = nullOK(m, "googleAnalyticsAccount")
            googleOauthBrowser = nullOK(m, "googleOauthBrowser") ?: "firefox"
            val googleClientName = nullOK(m, "google_id_file")
            if (googleClientName == null) {
                googleClient = null;
            } else {
                googleClient = GoogleClientConfig(googleClientName, configFile.parentFile)
            }
            remote_upload = nullOK(m, "remote_upload")
            val mailchimpClientName = nullOK(m, "mailchimp_id_file")
            if (mailchimpClientName == null) {
                mailchimpClient = null
            } else {
                mailchimpClient = MailchimpClientConfig(mailchimpClientName, configFile.parentFile)
            }
            mailchimpOauthBrowser = nullOK(m, "mailchimp_oauth_browser") ?: "firefox"
        } catch (e: Exception) {
            println()
            println("Error reading configuration file ${configFile.absolutePath}")
            println()
            throw e
        }
    }
}