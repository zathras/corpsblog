import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.Site
import com.jovial.blog.md.gallery.GalleryExtension
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.Content
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
    val site = Site(File("out/production/corpsblog/blog"))
    val configuration = Configuration.builder().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            addExtension(GalleryExtension(site)).
            build()
    val config = BlogConfig()
    val content = Content(configuration)
    content.read("../", File("test/blog/2013-08-25-lorem.md"))
    val p = Post(config, content)
    val d = File("out/production/corpsblog/blog")
    d.mkdirs()
    val f = File(d, "out.html")
    val w = FileWriter(f)
    w.write(p.generate().toString())
    w.close();
    println("Wrote to ${f.toURI()}")
}
