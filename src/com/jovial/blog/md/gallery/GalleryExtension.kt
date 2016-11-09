package com.jovial.blog.md.gallery

import com.github.rjeschke.txtmark.TxtmarkExtension
import com.github.rjeschke.txtmark.Block
import com.github.rjeschke.txtmark.Emitter
import com.github.rjeschke.txtmark.Line
import com.jovial.blog.Site
import com.jovial.blog.model.Content
import com.jovial.lib.html.BodyTag
import com.jovial.lib.html.bodyFragment
import java.io.File
import java.util.*

/**
 * Created by billf on 11/7/16.
 */


private val homeDir = System.getenv("HOME") ?: "/."
    // Null home?  I read about this on alt.windows.die.die.die somewhere

class GalleryExtension (val site: Site) : TxtmarkExtension<Content>() {
    override fun emitIfHandled(emitter: Emitter<Content>, out: StringBuilder, block: Block,
                               context: Content) : Boolean
    {
        var currLine : Line? = block.lines;    // It's a raw linked list
        if (currLine == null || currLine.value != "\$gallery\$") {
            return false
        }
        context.galleryCount++
        // Parse the pictures from input
        currLine = currLine.next
        val pictures = ArrayList<Picture>()
        while (currLine != null) {
            currLine = addPicture(currLine, emitter, pictures)
        }

        // Prepare the images
        val needPlusIcon = pictures.size > 12
        val gallery : MutableList<Picture> = if (!needPlusIcon) {
            pictures
        } else {
            val list = ArrayList<Picture>(11)
            for (i in 0..10) {
                list.add(pictures[(i * pictures.size) / 11])
            }
            list
        }
        var num = 0;
        for (p in pictures) {
            val makeGallery = (!needPlusIcon) || gallery.contains(p)
            p.generate(site.baseDir, "pix/", num.toString(), makeGallery)
            num++
        }

        // Output the photoswipe code for these images
        val pswpItems = "pswpItems${context.galleryCount}"
        val photoswipe = bodyFragment {
            script(type="text/javascript") {
                +"var $pswpItems = ["
                var first = true
                for (p in pictures) {
                    if (first) {
                        +"  {"
                        first = false
                    } else {
                        +"  }, {"
                    }
                    val title = p.caption.trim().replace("'", "\\'")
                    +"    src: '${p.bigImage}',"
                    +"    msrc: '${p.smallImage}',"
                    +"    w: ${p.bigImageSize!!.width},"
                    +"    h: ${p.bigImageSize!!.height},"
                    +"    title: '${title}'"
                }
                +"  }"
                +"];"
                // @@@@ todo:
                +"""
// define options (if needed)
var options = {
    // optionName: 'option value'
    // for example:
    index: 0 // start at first slide
};

// Initializes and opens PhotoSwipe
var gallery = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, $pswpItems, options);
gallery.init();
"""
            }
        }
        photoswipe.render(out, "")

        // Make a photo grid for the gallery of up to 12 pictures, or 11 if we need a plus icon.
        val doc = bodyFragment {
            h2 {
                +"@@ Gallery"
            }
            section(class_ = "photogrid-4") {
                for (p in gallery) {
                    img(src = p.galleryImage!!)
                }
                if (needPlusIcon) {
                    img(src = context.rootPath  + "images/plus-sign.png")
                }
            }
        }
        doc.render(out, "")
        return true
    }

    private fun addPicture(start: Line, emitter: Emitter<Content>,
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