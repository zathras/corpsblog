package com.jovial.blog.md.extensions

import com.github.rjeschke.txtmark.Block
import com.github.rjeschke.txtmark.Emitter
import com.github.rjeschke.txtmark.Line
import com.github.rjeschke.txtmark.TxtmarkExtension
import com.jovial.blog.Site
import com.jovial.blog.model.PostContent
import com.jovial.blog.model.VideoUpload
import com.jovial.util.GitManager
import com.jovial.util.processFileName
import java.io.File
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
        context.videoCount++
        currLine = currLine.next
        val sourceFile = processFileName(currLine.value)
	var s = sourceFile.name
        val extension = s.substring(s.lastIndexOf('.') .. s.length-1)
        val destFileName = context.baseGeneratedDirName + "-video-" + context.videoCount + extension
	val destFile = File(context.outputDir, destFileName)
        context.dependsOn.add(sourceFile)

        // Read the caption
        currLine = currLine.next
	val caption = StringBuilder()
        while (currLine != null) {
            emitter.extensionEmitText(caption, currLine.value)
	    currLine = currLine.next
        }

        // Copy the video file
        val dep = site.dependencies.get(destFile)
        val upload = site.dependencies.getVideo(destFile)
        if (dep.changed(listOf(sourceFile))) {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            try {
                GitManager.addFile(destFile)
            } catch (ex : Exception) {
                destFile.delete()
                throw ex
            }
            upload.uploadedAddress = null
        }

        // Make a video tag
        // <video width="$width" height="$height" controls>
        val dim = if (width > height) {
            """width="100%""""
        } else {
            out.append("<center>")
            """height="480""""   // @@ Do better than this!
        }
        out.append("""
<video $dim controls>
    <source src="${destFile.name}">
</video>""")
        if (width > height) {
            out.append("<center>\n")
        }
        out.append("<br>\n")
        val a = upload.uploadedAddress
        if (a == null) {
            out.append("""<em><a href="http://INVALID">(view on YouTube PENDING)</a></em>""")
            site.pass!!.addTask {
                uploadVideo(upload)
            }
        } else {
            out.append("""<em><a href="${a.toString()}">(view on YouTube)</a></em>""")
        }
        out.append("\n</center>\n")
	if (caption.length > 0) {
	    out.append("""
<blockquote>
${caption.toString()}
</blockquote>
<div style="clear: both"></div>""")
	}
        return true
    }

    private fun uploadVideo(vu : VideoUpload) {
        throw RuntimeException("@@ Not implemented:  Upload video ${vu.videoFile}")
    }
}
