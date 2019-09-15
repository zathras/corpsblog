package com.jovial.blog.model

import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.Site
import com.jovial.util.escapeHtml
import com.jovial.blog.md.extensions.Thumbnail
import com.jovial.util.processFileName
import java.io.*
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

class PostContent (
        val txtmarkConfig : Configuration<PostContent>,
        val site : Site,
        val outputDir : File,
        val postBaseName: String,/** Name to use for any generated directories within outputDir, etc. */
        val rootPath : String,/** Relative to the base directory of the blog within our site **/
        val dependsOn : MutableList<File>  /** Our post depends on all of its generated pictures; we record that here */
) : Content()
{
    var title : String = ""     /** Title, escaped for HTML */
        private set
    var synopsis : String = ""  /** Synopsis, escaped for HTML */
        private set
    var date : Date = Date(0)
        private set
    var tags : List<String> = listOf<String>()
        private set
    var thumbnail : Thumbnail? = null  /** See also default_post_thumbnail in the blog config file.  */
        private set
    var galleryCount = 0

    val hasGallery : Boolean
        get() = galleryCount > 0

    val videoURLs = mutableListOf<String>()
    var videoCount = 0;

    override fun read(location: File) {
        date = fileDateFormat.parse(location.name)
        super.read(location)
    }

    protected override fun readBody(input: Reader) {
        body = Processor<PostContent>(input, txtmarkConfig).process(this)
    }

    protected override fun processHeader(key : String, value : String ) : Boolean {
        if (key == "tags") {
            if (tags.size > 0) {
                throw ParseError("Tags specified twice")
            }
            tags = value.split(commaSplitRegex)
        } else if (key == "synopsis") {
            if (synopsis != "") {
                throw ParseError("Synopsis specified twice")
            }
            synopsis = escapeHtml(value)
        } else if (key == "title") {
            if (title != "") {
                throw ParseError("Title specified twice")
            }
            title = escapeHtml(value)
        } else if (key == "thumbnail") {
            if (thumbnail != null) {
                throw ParseError("Thumbnail specified twice")
            }
            val sourceName = processFileName(value, site.blogConfig.pathMap, site.postsSrcDir)
            val t = Thumbnail(sourceName, site, outputDir, postBaseName)
            thumbnail = t
        } else {
            return false
        }
        return true
    }

    /**
     * Discard the body after writing, to save a bit of memory.
     */
    fun discardBody() {
        body = ""
    }
}
