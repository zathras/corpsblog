import com.jovial.blog.Site
import com.jovial.blog.model.BlogConfig
import com.jovial.os.OSUploadFromRemote
import com.jovial.os.Stdout
import java.io.File
import kotlin.system.exitProcess

/**
 * Created by w.foote on 11/3/2016.
 */

private fun usage() {
    Stdout.println()
    Stdout.println("Usage:  corpsblog (publish | offline | mail | cleanup | remote_hack)")
    Stdout.println()
    Stdout.println("    publish <srcdir> <destdir>")
    Stdout.println("        Ready the target for \"git commit -a\" and \"git push\".")
    Stdout.println()
    Stdout.println("    offline <srcdir> <destdir>")
    Stdout.println("        Trial run; don't upload anything (e.g. no YouTube uploads).")
    Stdout.println()
    Stdout.println("    clean <srcdir> <destdir>")
    Stdout.println("        Generate site and clean up stray files in the output directory.")
    Stdout.println()
    Stdout.println("    mail <srcdir> <destdir>")
    Stdout.println("        Post to mail list about recent activity (do this after git push).")
    Stdout.println()
    Stdout.println("    remote_hack (advanced)")
    Stdout.println("        The remote shell side of a remote YouTube upload.")
    Stdout.println()
    exitProcess(1)
}

fun main(args : Array<String>) {
    if (args.isEmpty()) {
        usage()
    }
    when (args[0]) {
        "remote_hack" -> OSUploadFromRemote(args.drop(1)).run()
        "publish" -> generateSite(true, args.drop(1))
        "offline" -> generateSite(false, args.drop(1))
        "cleanup" -> cleanupSite(args.drop(1))
        "mail" -> postToMaillist(args.drop(1))
        else -> usage()
    }
    exitProcess(0)
}

private fun generateSite(publish: Boolean, args: List<String>) : Site {
    if (args.size != 2) {
        usage()
    }
    val inputDir = File(args[0])
    val outputDir = File(args[1])
    val blogConfig = BlogConfig(File(inputDir, "corpsblog.config"))
    val site = Site(
            inputDir=inputDir,
            outputDir=outputDir,
            blogConfig=blogConfig,
            publishYT=publish
    )
    site.generate()
    site.printNotes()
    Stdout.println()
    if (site.hasErrors()) {
        site.printErrors()
        exitProcess(1)
    }
    return site
}

private fun cleanupSite(args: List<String>) {
    val site = generateSite(publish=false, args=args)
    site.deleteStrayFiles()
}

private fun postToMaillist(args: List<String>) {
    val site = generateSite(publish=false, args=args)
    val mgr = site.mailchimpManager
    if (mgr == null) {
        Stdout.println("Mailchimp not configured, so I can't post to a maillist.")
    } else {
        mgr.generateNotifications(site)
    }
    val ifttt = site.iftttManager
    if (ifttt == null) {
        Stdout.println("Ifttt not configured, so I can't post to Facebook Pages.")
    } else {
        ifttt.generateNotifications(site)
    }
}

