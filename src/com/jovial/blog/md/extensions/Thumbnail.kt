package com.jovial.blog.md.extensions

import com.jovial.blog.Site
import com.jovial.util.JpegMetadata
import java.awt.Dimension
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.io.File
import javax.imageio.ImageIO

/**
 * A thumbnail representing a page, for presentation in the archive, or on social media.
 *
 * Created by w.foote on 3/8/2017.
 */
class Thumbnail(
        sourceName : File,
        val site : Site,
        val postsDir : File,
        val baseName : String) : AbstractImage(sourceName)
{
    val archiveImageName : String       /** Relative to posts directory  */
    val archiveImageSize : Dimension
    val socialImageName : String        /** Relative to posts directory  */
    val socialImageSize : Dimension

   init {
        var r = doGenerate("archive", 114, 64)   //  I picked max size out of the air
        archiveImageName = r.first
        archiveImageSize = r.second     // Kotlin doesn't have destructuring assignment, I guess :-(
        r = doGenerate("social", 476, 249)   // That's the size I observed on FB
        socialImageName = r.first
        socialImageSize = r.second
        flushSourceImage()
    }

    private fun doGenerate(dirName: String, maxWidth : Int, maxHeight : Int) : Pair<String, Dimension> {
        val fileName = "thumbnails/$dirName/$baseName.jpg"
        val dest = File(postsDir, fileName)
        val dep = site.dependencies.get(dest)
        if (dep.changed(listOf(source))) {
            var im = getImage()
            dest.parentFile.mkdirs()
            var scaleFactor = 1.0
            if (im.width > maxWidth) {
                scaleFactor = maxWidth.toDouble() / im.width.toDouble()
            }
            if (im.height > maxHeight) {
                val sf = maxHeight.toDouble() / im.height.toDouble()
                if (scaleFactor > sf)
                    scaleFactor = sf
            }
            val scaled = if (scaleFactor == 1.0) {
                im
            } else {
                val xform = AffineTransform.getScaleInstance(scaleFactor, scaleFactor)
                val op = AffineTransformOp(xform, null)
                val newImage = op.filter(im, null)
                newImage
            }
            ImageIO.write(scaled, "jpeg", dest)
            if (scaled != im) {
                scaled.flush()
            }
        }
        val jpegMetadata = JpegMetadata(dest)
        jpegMetadata.read()
        val size = jpegMetadata.size!!
        return Pair(fileName, size)
    }
}