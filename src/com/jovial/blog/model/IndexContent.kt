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

private val dateFormat = SimpleDateFormat("MM/dd/yy")

class IndexContent (
        val txtmarkConfig : Configuration<Content>
) : Content()
{
    var date : Date = Date(0)
        private set

    protected override fun processHeader(key : String, value : String ) : Boolean {
        when (key) {
            "date"      -> date = dateFormat.parse(value)
            else        -> return false
        }
        return true
    }

    override fun readBody(input: Reader) {
        body = Processor<Content>(input, txtmarkConfig).process(this)
    }
}
