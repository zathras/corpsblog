package com.jovial.blog.md.extensions

import com.github.rjeschke.txtmark.TxtmarkExtension
import com.github.rjeschke.txtmark.Block
import com.github.rjeschke.txtmark.Emitter
import com.github.rjeschke.txtmark.Line
import com.jovial.blog.Site
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.BodyTag
import com.jovial.lib.html.bodyFragment
import com.jovial.util.processFileName
import java.io.File
import java.util.*

/**
 * Created by billf on 11/7/16.
 */


class GalleryExtension (val site: Site) : TxtmarkExtension<PostContent>() {
    override fun emitIfHandled(emitter: Emitter<PostContent>, out: StringBuilder, block: Block,
                               context: PostContent) : Boolean
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
            p.generate(i.toString(), makeGallery, site)
        }

        // Output the photoswipe code for these images
        val pswpItems = "pswpItems${context.galleryCount}"
        val photoswipe = bodyFragment {
            script(type="text/javascript") {
                +"var $pswpItems = ["
                var first = true
                for (p in pictures) {
                    context.dependsOn.add(p.source)
                    if (first) {
                        +"  {"
                        first = false
                    } else {
                        +"  }, {"
                    }
                    val title = p.caption.trim().replace("'", "\\'")
                    +"    src: '${p.largeImage}',"
                    +"    msrc: '${p.smallImage}',"
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
                        img(src = gallery[0].largeImage!!)
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
                section(class_ = "photogrid-${if (gallery.size == 9) 33 else cols}") {
                    for (i in 0..gallery.size - 1) {
                        val p = gallery[i]
                        val index = indexes?.elementAt(i) ?: i
                        a(href = "javascript:openPhotoSwipe(${index + 1}, $pswpItems)") {
                            val style = if (i % cols != 0) {
                                "margin-left: 0"
                            } else {
                                null
                            }
                            img(src = p.mosaicImage!!, style=style)
                        }
                    }
                    if (needPlusIcon) {
                        a(href = "javascript:openPhotoSwipe(1, $pswpItems)") {
                            img(src = context.rootPath + "images/plus-sign.png")
                        }
                    } else if (gallery.size % cols != 0) {
                        for (i in (gallery.size % cols)..(cols-1)) {
                            a(href = "javascript:openPhotoSwipe(1, $pswpItems)") {
                                img(src = context.rootPath + "images/grey.png", style="margin-left: 0")
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
                           emitter: Emitter<PostContent>,
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
}