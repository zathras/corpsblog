package com.jovial.blog.md.extensions

import com.jovial.blog.Site
import com.jovial.os.OSImage
import com.jovial.util.JpegMetadata
import java.io.File

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
    val archiveImageSize : OSImage.Size
    val socialImageName : String        /** Relative to posts directory  */
    val socialImageSize : OSImage.Size
    val postImageName : String
    val postImageSize : OSImage.Size

   init {
       var r = doGenerate("archive", 114, 64)   //  I picked max size out of the air
       archiveImageName = r.first
       archiveImageSize = r.second
       // Kotlin doesn't have destructuring assignment, I guess :-(
       // This is tempting, but doesn't work:  (archiveImageName, archiveImageSize) = r
       r = doGenerate("social", 1080, 512)  // That's about 2x the expected height and width
                                            // See https://github.com/zathras/corpsblog/issues/3
       socialImageName = r.first
       socialImageSize = r.second
       r = doGenerate("post", 250, 120)
       postImageName = r.first
       postImageSize = r.second
       flushSourceImage()
    }

    private fun doGenerate(dirName: String, maxWidth : Int, maxHeight : Int) : Pair<String, OSImage.Size> {
        val fileName = "thumbnails/$dirName/$baseName.jpg"
        val dest = File(postsDir, fileName)
        val dep = site.dependencies.get(dest)
        if (dep.changed(listOf(source))) {
            val im = getImage()
            dest.parentFile.mkdirs()
            var scaleFactor = 1.0
            if (im.size.width > maxWidth) {
                scaleFactor = maxWidth.toDouble() / im.size.width.toDouble()
            }
            if (im.size.height > maxHeight) {
                val sf = maxHeight.toDouble() / im.size.height.toDouble()
                if (scaleFactor > sf)
                    scaleFactor = sf
            }
            val scaled = if (scaleFactor == 1.0) {
                im
            } else {
                im.scaledBy(scaleFactor)
            }
            scaled.writeJpeg(dest)
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
