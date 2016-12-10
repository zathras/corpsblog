package com.jovial.blog.model

import com.jovial.google.GoogleClientConfig
import com.jovial.util.JsonIO
import com.jovial.util.notNull
import com.jovial.util.nullOK
import java.io.*
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 */

class BlogConfig(configFile: File) {

    public val siteBaseURL : String
    public val feedURL : String
    public val siteDescription : String
    public val siteAuthor : String
    public val siteTitle: String
    public val myProfilePhoto : String
    public val coverImage : String
    public val shareTwitter : String?
    public val shareLinkedIn : String?
    public val shareGitHub : String?
    public val shareFlickr : String?
    public val shareGarmin : String?
    public val shareEmail : String?
    public val shareDisqus : String?
    public val googleAnalyticsAccount : String?
    public val googleClient : GoogleClientConfig?

    init {
        try {
            val input = BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8"))
            val m = JsonIO.readJSON(input) as HashMap<Any, Any>
            input.close()
            siteBaseURL = notNull(m, "siteBaseURL")
            siteDescription = notNull(m, "siteDescription")
            siteAuthor = notNull(m, "siteAuthor")
            siteTitle = notNull(m, "siteTitle")
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
            val googleClientName = nullOK(m, "google_id")
            if (googleClientName == null) {
                googleClient = null;
            } else {
                googleClient = GoogleClientConfig(googleClientName)
            }
        } catch (e: Exception) {
            println()
            println("Error reading configuration file ${configFile.absolutePath}")
            println()
            throw e
        }
        feedURL = siteBaseURL + "/feed.xml"
    }
}