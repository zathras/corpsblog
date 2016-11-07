package com.jovial.blog.model

import java.util.*

/**
 * Represents the content that is passed to a template, in order to generate an HTML
 * file.
 *
 * Created by w.foote on 11/3/2016.
 */

class Content (
        val rootPath : String,  /** Path to the base directory of the blog wihin our site **/
        val body: String,       /** Body text, in HTML */
        val title : String,     /** Title, escaped for HTML */
        val synopsis: String,    /** Synopsis, escaped for HTML */
        val date : Date,
        val tags : List<String> = listOf<String>()

) {
}
