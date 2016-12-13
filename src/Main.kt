import com.github.rjeschke.txtmark.Processor
import com.jovial.blog.Site
import com.jovial.blog.md.extensions.GalleryExtension
import com.jovial.blog.md.extensions.VideoExtension
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.google.OAuth
import com.jovial.google.YouTube
import com.jovial.google.remote_hack.UploadFromRemote
import com.jovial.util.processFileName
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
    println("Usage:  corpsblog (publish | offline | fb | remote_hack)")
    println()
    println("    publish")
    println("        Ready the target for \"git commit -a\" and \"git push\"")
    println()
    println("    offline")
    println("        Trial run; don't upload anything (e.g. no YouTube uploads)")
    println()
    println("    fb")
    println("        Post to Facebook about recent activity (do this after git push)")
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
        "fb" -> postToFacebook(args.drop(1))
    }
}

private fun generateSite(publish: Boolean, args: List<String>) {
    val inputDir=File("test")
    val blogConfig = BlogConfig(File(inputDir, "corpsblog.config"))
    val site = Site(
            inputDir=inputDir,
            outputDir=File("out/test"),
            blogConfig=blogConfig
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
    /*
    val googleClient = blogConfig.googleClient
    if (googleClient != null) {
        val oa = OAuth(googleClient, site.dbDir)
        val yt = YouTube(blogConfig.remote_upload, oa)
        val vid = processFileName("~/github/moom-www/movies/2006_09_messengers_h264.mp4")
        val url = yt.uploadVideo(vid, "Test video.  This is a test video.\n\nHere's a link:  http://www.guardian.co.uk/world/")
        println("Uploaded to $url")
        println("@@ stop here")
    }
    System.exit(1)
    */
    site.generate()
    if (site.hasErrors()) {
        println()
        site.printErrors()
        System.exit(1)
    }
    System.exit(0)
}

private fun postToFacebook(args: List<String>) {
    throw RuntimeException("@@ not implemented")
}
