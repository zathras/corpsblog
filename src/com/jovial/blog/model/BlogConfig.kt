package com.jovial.blog.model

import com.jovial.google.GoogleClientConfig
import com.jovial.notifications.IftttClientConfig
import com.jovial.notifications.MailchimpClientConfig
import com.jovial.os.Stdout
import com.jovial.util.*
import java.io.*
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 */

class BlogConfig(configFile: File) {

    public val pathMap : Map<String, File>
    public val dbDir : File
    public val siteBaseURL : String     // Terminated with a "/"
    public val siteDescription : String
    public val siteAuthor : String
    public val disclaimer : String?
    public val siteTitle: String
    public val siteTitleBodyHTML: String?
    public val siteTitleBodyImage: String?
    public val myProfilePhoto : String
    public val coverImage : String
    public val shareWeb: String?
    public val shareTwitter : String?
    public val shareLinkedIn : String?
    public val shareFacebook : String?
    public val shareGitHub : String?
    public val shareFlickr : String?
    public val shareGarmin : String?    // The GPS company.
    public val shareDisqus : String?    // Blog comments, https://en.wikipedia.org/wiki/Disqus
                                        // Ad-suported by taking a cut from "reveal," but blogs can opt out.
    public val googleAnalyticsAccount : String?
    public val googleOauthBrowser : String         // Used for Google OAuth login flow.  Defaults to firefox.
    public val googleClient : GoogleClientConfig?
    public val mailchimpClient : MailchimpClientConfig?
    public val iftttClient : IftttClientConfig?
    public val mailchimpOauthBrowser : String         // Used for Mailchimp OAuth login flow.  Defaults to firefox.
    public val defaultPostThumbnail : String?         // Relative to base directory
    public val indexThumbnail : String?               // Like the above, but for index.html

    /**
     * Name of the remote shell command for YouTube uploads, if one exists.  See
     * com.jovial.google.remote_hack.
     */
    val remote_upload: String?

    init {
        try {
            val input = BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8"))
            @Suppress("UNCHECKED_CAST")
            val m = JsonIO.readJSON(input) as HashMap<Any, Any>
            input.close()
            // An optional map of file prefix strings to something else.  This is useful
            // when moving a blog between Android and a normal computer, since Android's
            // file security model makes it hard to put everything in one place.
            val pathMapBuild = mutableMapOf<String, File>()
            val pathMapSrc = m["pathMap"]
            if (pathMapSrc != null) {
                @Suppress("UNCHECKED_CAST")
                pathMapSrc as Map<String, String>
                for (e in pathMapSrc.entries) {
                    pathMapBuild[e.key] = File(e.value)
                }
            }
            pathMap = pathMapBuild;
            dbDir = makeFile(configFile.parentFile, notNull(m, "dbDir"))
            siteBaseURL = withSlashAtEnd(notNull(m, "siteBaseURL"))
            siteDescription = notNull(m, "siteDescription")
            siteAuthor = notNull(m, "siteAuthor")
            siteTitle = notNull(m, "siteTitle")
            disclaimer = nullOK(m, "disclaimer")
            siteTitleBodyHTML = nullOK(m, "siteTitleBodyHTML")
            siteTitleBodyImage = nullOK(m, "siteTitleBodyImage")
            myProfilePhoto = notNull(m, "myProfilePhoto")
            coverImage = notNull(m, "coverImage")
            shareWeb = nullOK(m, "shareWeb")
            shareTwitter = nullOK(m, "shareTwitter")
            shareLinkedIn = nullOK(m, "shareLinkedIn")
            shareFacebook = nullOK(m, "shareFacebook")
            shareGitHub = nullOK(m, "shareGitHub")
            shareFlickr = nullOK(m, "shareFlickr")
            shareGarmin = nullOK(m, "shareGarmin")
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
            val iftttClientName = nullOK(m, "ifttt_id_file")
            if (iftttClientName == null) {
                iftttClient = null
            } else {
                iftttClient = IftttClientConfig(iftttClientName, configFile.parentFile)
            }
            mailchimpOauthBrowser = nullOK(m, "mailchimp_oauth_browser") ?: "firefox"
            defaultPostThumbnail = nullOK(m, "default_post_thumbnail")
            indexThumbnail = nullOK(m, "index_thumbnail")
        } catch (e: Exception) {
            Stdout.println()
            Stdout.println("Error reading configuration file ${configFile.absolutePath}")
            Stdout.println("    $e")
            Stdout.println()
            throw e
        }
    }
}
