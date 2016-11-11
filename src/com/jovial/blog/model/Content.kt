package com.jovial.blog.model

import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents the content that is passed to a template, in order to 
 * generate an HTML file.
 *
 * Created by w.foote on 11/3/2016.
 */

private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd")
private val commaSplitRegex = Regex(" *, *")

class Content (
        val configuration : Configuration,
        val outputDir : File,
        val baseGeneratedDirName : String,    /** Name to use for any generated directories within outputDir, etc. */
        val rootPath : String   /** Relative to the base directory of the blog within our site **/
        )
{
    var body : String = ""      /** Body of the content, in HTML */
        private set
    var title : String = ""     /** Title, escaped for HTML */
        private set
    var synopsis : String = ""  /** Synopsis, escaped for HTML */
        private set
    var date : Date = Date(0)
        private set
    var tags : List<String> = listOf<String>()
        private set
    var galleryCount = 0

    val hasGallery : Boolean
        get() = galleryCount > 0

    class ParseError(message: String) : Exception(message)


    public fun escapeHtml(line: String) =
        line.replace("<", "&lt;", false).
                replace(">", "&gt;").
                replace("&", "&amp;")

    @Throws(ParseError::class)
    fun read(location: File) {
        try {
            date = fileDateFormat.parse(location.name)
        } catch (ex: ParseException) {
            // No date.
        }
        val input = BufferedReader(FileReader(location))
        var lineNumber = 0;
        while (true) {
            lineNumber++
            val line = input.readLine()
            if (line == null) {
                throw ParseError("Unexpected EOF in header of $location")
            } else if (line.startsWith("~~~~~")) {
                break
            }
            val i = line.indexOf('=')
            if (i < 0) {
                throw ParseError("""Missing "=" in metadata block """
				 +"at line $lineNumber of $location")
            }
	    val key = line.substring(0, i)
            val value = line.substring(i+1)
            when (key) {
                "tags"
                    -> tags = value.split(commaSplitRegex)
                "synopsis"
                    -> synopsis = escapeHtml(value)
                "title"
                    -> title = escapeHtml(value)
                else
                    ->  throw ParseError("Unrecognized key "
                                         +"at line $lineNumber of $location")
            }
        }
        body = Processor<Content>(input, configuration).process(this)
        input.close()
    }
}
