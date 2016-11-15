import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.Site
import com.jovial.blog.md.gallery.GalleryExtension
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import templates.Post
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 */

fun main(args : Array<String>) {
    val site = Site(
            inputDir=File("test"),
            outputDir=File("out/production/corpsblog")
    )
    val txtmarkConfig = com.github.rjeschke.txtmark.Configuration.builder().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            build()
    site.deferredTxtmarkConfig = txtmarkConfig
    val txtmarkPostConfig = com.github.rjeschke.txtmark.Configuration.builder().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            addExtension(GalleryExtension(site)).
            build()
    site.deferredTxtmarkPostConfig = txtmarkConfig
    site.generate()
}
