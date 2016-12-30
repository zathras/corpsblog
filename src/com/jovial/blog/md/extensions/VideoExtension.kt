package com.jovial.blog.md.extensions

import com.github.rjeschke.txtmark.Block
import com.github.rjeschke.txtmark.Emitter
import com.github.rjeschke.txtmark.Line
import com.github.rjeschke.txtmark.TxtmarkExtension
import com.jovial.blog.Site
import com.jovial.blog.model.PostContent
import com.jovial.util.processFileName
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Created by billf on 11/20/16.
 */

class VideoExtension (val site: Site) : TxtmarkExtension<PostContent>() {
    override fun emitIfHandled(emitter: Emitter<PostContent>, out: StringBuilder, block: Block,
                               context: PostContent) : Boolean
    {
        var currLine : Line? = block.lines;    // It's a raw linked list
        if (currLine == null) {
            return false
        }
        if (currLine.value == null || !currLine.value.startsWith("\$video\$")) {
            return false
        }
        val dimensionStrings = currLine.value.drop(7).trim().split(' ')
        val width = Integer.parseInt(dimensionStrings[0])
        val height = Integer.parseInt(dimensionStrings[1])
        currLine = currLine.next
        val sourceFile = processFileName(currLine.value, site.postsSrcDir)
	var s = sourceFile.name
        val extension = s.substring(s.lastIndexOf('.') .. s.length-1)
        val destFileName = context.postBaseName + "-video-" + context.videoURLs.size + extension
        val videosDir = File(context.outputDir, "videos")
        // Putting videos in their own directory makes it easier to push them to the git repository before
        // doing the youtube upload, e.g. using "git add videos".  This makes it easier to build a workflow
        // that uses the remote hack of uploading videos to YouTube via an external shell account.
        videosDir.mkdirs()
	val destFile = File(videosDir, destFileName)
        context.dependsOn.add(sourceFile)

        // Read the caption
        currLine = currLine.next
	val captionSB = StringBuilder()
        while (currLine != null) {
            emitter.extensionEmitText(captionSB, currLine.value)
	    currLine = currLine.next
        }
        val caption = captionSB.toString()

        // Copy the video file
        val dep = site.dependencies.get(destFile)
        if (dep.changed(listOf(sourceFile))) {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        // Make a video tag
        // <video width="$width" height="$height" controls>
        val dim = if (width > height) {
            """width="100%""""
        } else {
            out.append("<center>")
            if (height > 480) {
                """height="480""""
            } else {
                """height="$height""""
            }
        }
        out.append("""
<video $dim controls>
    <source src="videos/${destFile.name}">
</video>""")
        if (width > height) {
            out.append("<center>\n")
        }
        out.append("<br>\n")
        val destURL = URL(site.blogConfig.siteBaseURL + "/posts/videos/" + destFile.name)
        val upload = getYoutubeURL(sourceFile, destURL, caption, context)
        if (upload == null) {
            context.videoURLs.add("Pending for $sourceFile")
            out.append("""<em><a href="http://INVALID">(view on YouTube PENDING)</a></em>""")
            if (site.publish) {
                site.error("Video not uploaded to YouTube:  $destFile")
            } else {
                site.note("Video not uploaded to YouTube:  $destFile")
            }
        } else {
            context.videoURLs.add(upload.toString())
            out.append("""<em><a href="${upload.toString()}">(view on YouTube)</a></em>""")
        }
        out.append("\n</center>\n")
	if (caption.length > 0) {
	    out.append("""
<blockquote>
${caption}
</blockquote>
<div style="clear: both"></div>""")
	}
        return true
    }

    private fun getYoutubeURL(src : File, destURL : URL, caption: String, context : PostContent) : URL? {
        val yt = site.youtubeManager
        if (yt == null) {
            return null
        }
        val u = yt.getVideoURL(src)
        if (u != null) {
            return u
        }
        if (!site.publish) {
            return null;
        }
        val postURL = site.blogConfig.siteBaseURL + "/posts/${context.postBaseName}.html"
        val seeMe = "This video is from the blog post at $postURL"
        val description = if (caption.length == 0) {
            seeMe
        } else {
            "$caption\n\n$seeMe"
        }
        return yt.uploadVideo(src, destURL, description)
    }
}
