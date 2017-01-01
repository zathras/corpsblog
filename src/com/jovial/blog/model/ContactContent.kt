package com.jovial.blog.model

import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.jovial.util.escapeHtml
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents the content for index.html
 */

class ContactContent (
        txtmarkConfig : Configuration
) : Content(txtmarkConfig)
{
    protected override fun processHeader(key : String, value : String ) : Boolean {
        @Suppress("UNUSED_EXPRESSION")
        when (key) {
            else        -> return false
        }
    }
}
