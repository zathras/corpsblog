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
        val galleryName = context.baseGeneratedDirName + "-gallery-" + context.galleryCount
        val galleryDir = File(context.outputDir, galleryName)
        galleryDir.mkdirs()
        // Parse the pictures from input
        currLine = currLine.next
        val pictures = ArrayList<Picture>()
        while (currLine != null) {
            currLine = addPicture(currLine, emitter, galleryDir, pictures)
        }

        // Prepare the images
        val needPlusIcon = pictures.size > 12
        var indexes : MutableList<Int>? = null
        val gallery : MutableList<Picture> = if (!needPlusIcon) {
            pictures
        } else {
            val list = ArrayList<Picture>(11)
            indexes = ArrayList<Int>(11)
            for (i in 0..10) {
                val index = (i * (pictures.size-1)) / 10
                list.add(pictures[index])
                indexes.add(index)
            }
            list
        }
        for (i in 0..pictures.size-1) {
            val p = pictures[i]
            val makeGallery = pictures.size > 1 && ((!needPlusIcon) || gallery.contains(p))
            p.generate(i.toString(), makeGallery)
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
                    +"    src: '$galleryName/${p.largeImage}',"
                    +"    msrc: '$galleryName/${p.smallImage}',"
                    +"    w: ${p.largeImageSize!!.width},"
                    +"    h: ${p.largeImageSize!!.height},"
                    +"    title: '${title}'"
                }
                +"  }"
                +"];"
            }
        }
        photoswipe.render(out, "")

        // Make a photo grid for the gallery of up to 12 pictures, or 11 if we need a plus icon.
        val doc = bodyFragment {
            if (gallery.size == 1) {
                section(class_ = "photogrid-1") {
                    a(href = "javascript:openPhotoSwipe(1, $pswpItems)") {
                        img(src = galleryName + "/" + gallery[0].largeImage!!)
                    }
                }
            } else {
                val cols = if (gallery.size == 2 || gallery.size == 4) {
                    2
                } else if (gallery.size in setOf(3, 5, 6, 9)) {
                    3
                } else {
                    4
                }
                section(class_ = "photogrid-$cols") {
                    for (i in 0..gallery.size - 1) {
                        val p = gallery[i]
                        val index = indexes?.elementAt(i) ?: i
                        a(href = "javascript:openPhotoSwipe(${index + 1}, $pswpItems)") {
                            img(src = galleryName + "/" + p.galleryImage!!)
                        }
                    }
                    if (needPlusIcon) {
                        a(href = "javascript:openPhotoSwipe(1, $pswpItems)") {
                            img(src = context.rootPath + "images/plus-sign.png")
                        }
                    } else if (gallery.size % cols != 0) {
                        for (i in (gallery.size % cols)..(cols-1)) {
                            a(href = "javascript:openPhotoSwipe(1, $pswpItems)") {
                                img(src = context.rootPath + "images/grey.png")
                            }
                        }
                    }
                }
            }
            div(style="clear: both") { }
        }
        doc.render(out, "")
        return true
    }

    private fun addPicture(start: Line,
                           emitter: Emitter<Content>,
                           galleryDir: File,
                           pictures: MutableList<Picture>) : Line? {
        val sourceName = processFileName(start.value)
        var line : Line? = start.next
        val caption = StringBuilder()
        while (line != null && line.value.startsWith(" ")) {
            emitter.extensionEmitText(caption, line.value)
            line = line.next
        }
        pictures += Picture(sourceName, galleryDir, caption.toString())
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