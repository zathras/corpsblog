import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.Site
import com.jovial.blog.md.extensions.GalleryExtension
import com.jovial.blog.md.extensions.VideoExtension
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.webapi.OAuth
import com.jovial.google.YouTube
import com.jovial.google.remote_hack.UploadFromRemote
import com.jovial.util.processFileName
import com.jovial.templates.Post
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import javax.imageio.ImageIO

/**
 * Created by w.foote on 11/3/2016.
 */

private fun usage() {
    println()
    println("Usage:  corpsblog (publish | offline | fb | remote_hack)")
    println()
    println("    publish")
    println("        Ready the target for \"git commit -a\" and \"git push\"")
    println()
    println("    offline")
    println("        Trial run; don't upload anything (e.g. no YouTube uploads)")
    println()
    println("    mail")
    println("        Post to mail list about recent activity (do this after git push)")
    println()
    println("    remote_hack (advanced)")
    println("        The remote shell side of a remote YouTube upload.")
    println("        cf. com.jovial.google.remote_hack.")
    println()
    System.exit(1)
}

fun main(args : Array<String>) {
    if (args.size == 0) {
        usage()
    }
    when (args[0]) {
        "remote_hack" -> UploadFromRemote(args.drop(1)).run()
        "publish" -> generateSite(true, args.drop(1))
        "offline" -> generateSite(false, args.drop(1))
        "mail" -> postToMaillist(args.drop(1))
        else -> usage()
    }
    System.exit(0)
}

private fun generateSite(publish: Boolean, args: List<String>) : Site {
    if (args.size != 0) {
        usage()
    }
    val inputDir=File("test")
    val blogConfig = BlogConfig(File(inputDir, "corpsblog.config"))
    val site = Site(
            inputDir=inputDir,
            outputDir=File("out/test"),
            blogConfig=blogConfig,
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
    site.generate()
    site.printNotes()
    println()
    if (site.hasErrors()) {
        site.printErrors()
        System.exit(1)
    }
    return site
}

private fun postToMaillist(args: List<String>) {
    val site = generateSite(publish=false, args=args)
    val mgr = site.mailchimpManager
    if (mgr == null) {
        println("Mailchimp not configured, so I can't post to a maillist.")
        System.exit(0)
    } else {
        mgr.generateNotifications(site)
    }
}

fun foo() {
    val r = Random()
    for (i in 1..1) {
        val im = BufferedImage(200 + r.nextInt(2000), 200 + r.nextInt(2000), BufferedImage.TYPE_INT_RGB)
        val g = im.getGraphics()
        g.setFont(Font("Courier", Font.BOLD, im.getHeight() / 2))
        g.setColor(Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)))
        g.fillRect(0, 0, 4000, 4000)
        g.setColor(Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)))
        g.drawString(i.toString() ,im.getWidth() / 2, im.getHeight() * 7 / 8)
        ImageIO.write(im, "JPEG", File("image_$i.jpg"))
    }
}
