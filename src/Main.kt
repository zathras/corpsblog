import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.Site
import com.jovial.blog.md.extensions.GalleryExtension
import com.jovial.blog.md.extensions.VideoExtension
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.google.OAuth
import templates.Post
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 */

private fun usage() {
    println("Usage:  corpsblog [publish]")
    System.exit(1)
}

fun main(args : Array<String>) {
    var publish = false
    for (a in args) {
        if (a == "publish") {
            publish = true
        } else {
            usage()
        }
    }
    val site = Site(
            inputDir=File("test"),
            outputDir=File("out/test"),
            publish=publish
    )
    site.deferredTxtmarkConfig = com.github.rjeschke.txtmark.Configuration.builder().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            build()
    site.deferredTxtmarkPostConfig = com.github.rjeschke.txtmark.Configuration.builder().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            addExtension(GalleryExtension(site)).
            addExtension(VideoExtension(site)).
            build()
    val oa = OAuth(site.blogConfig.googleClient!!)
    oa.run()
    throw RuntimeException("@@ stop here")
    site.generate()
}
