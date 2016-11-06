package com.jovial.blog.model

/**
 * Represents the content that is passed to a template, in order to generate an HTML
 * file.
 *
 * Created by w.foote on 11/3/2016.
 */

class Content (
        val rootPath : String,
        val bodyHTML: String,
        val title : String,
        val synopsis: String)
{
}
