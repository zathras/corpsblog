import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.model.Config
import com.jovial.blog.model.Content
import templates.Post
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Created by w.foote on 11/3/2016.
 */

fun main(args : Array<String>) {
    val configuration = Configuration.builder().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            build()
    val input = BufferedReader(FileReader("README.md"))
    val r = Processor.process(input, configuration)
    val config = Config()
    val content = Content(
            rootPath = "./",
            bodyHTML = r,
            title = "Test HTML",
            synopsis = "This is the synopsis"
    )
    val p = Post(config, content)
    val f = File("out/production/corpsblog/out.html")
    val w = FileWriter(f)
    w.write(p.generate().toString())
    w.close();
    println("Wrote to ${f.toURI()}")
}
