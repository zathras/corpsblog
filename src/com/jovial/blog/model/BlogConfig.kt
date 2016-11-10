package com.jovial.blog.model

/**
 * Created by w.foote on 11/3/2016.
 */

class BlogConfig {
    // @@ Todo:  Read this from a config file
    public val feedURL: String
        get() = "@@ to do feed URL"
    public val siteDescription : String
        get() = "Peace Corps Burkina Faso"
    public val siteAuthor : String
        get() = "@@ to do site author"
    public val siteBaseURL : String
        get() = "@@ siteBaseURL"
    public val siteTitle: String
        get() = "The Adventures of Burkinabè Bill"
    public val shareTwitter = "@@ shareTwitter"
    public val shareLinkedIn = "@@ shareLinkedIn"
    public val shareGitHub = "@@ shareGitHub"
    public val shareFlickr = "@@ shareFlickr"
    public val shareGarmin = "@@ shareGarmin"
    public val shareEmail = "@@ shareEmail"
    public val shareDisqus = "@@ shareDisqus"
    public val googleAnalyticsAccount : String? = null
    public val myProfilePhoto = "images/where_is_burkina.png"   /** Relative to base URL */
    public val coverImage = "images/cover.jpg"
}