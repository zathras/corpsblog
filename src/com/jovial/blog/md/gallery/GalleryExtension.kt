package com.jovial.blog.md.gallery

import com.github.rjeschke.txtmark.TxtmarkExtension
import com.github.rjeschke.txtmark.Block
import com.github.rjeschke.txtmark.Emitter
import com.github.rjeschke.txtmark.Line
import com.jovial.blog.Site
import com.jovial.lib.html.BodyTag
import com.jovial.lib.html.bodyFragment
import java.io.File

/**
 * Created by billf on 11/7/16.
 */


private val homeDir = System.getenv("HOME")

class GalleryExtension (val site: Site) : TxtmarkExtension() {
    override fun emitIfHandled(emitter: Emitter, out: StringBuilder, block: Block) : Boolean {
        var currLine : Line? = block.lines;    // It's a raw linked list
        if (currLine == null || currLine.value != "\$gallery\$") {
            return false
        }
        currLine = currLine.next
        val pictures = mutableListOf<Picture>()
        while (currLine != null) {
            currLine = addPicture(currLine, emitter, pictures)
        }
        var num = 0;
        for (p in pictures) {
            p.generateScaled(site.baseDir, "pix/", num.toString())
            num++
        }
        val doc = bodyFragment {
            h2 {
                +"@@ Gallery"
            }
            section(class_ = "photogrid") {
                for (p in pictures) {
                    img(src = p.smallImage!!)
                }
            }
        }
        doc.render(out, "")
        return true
    }

    private fun addPicture(start: Line, emitter: Emitter,
                           pictures: MutableList<Picture>) : Line? {
        val fileName = processFileName(start.value)
        var line : Line? = start.next
        val caption = StringBuilder()
        while (line != null && line.value.startsWith(" ")) {
            emitter.extensionEmitText(caption, line.value)
            line = line.next
        }
        pictures += Picture(fileName, caption.toString())
        return line
    }

    private fun processFileName(name: String) : File {
        if (name.startsWith("~/")) {
            return File(homeDir + name.substring(1))
        } else {
            return File(name)
        }
    }
}