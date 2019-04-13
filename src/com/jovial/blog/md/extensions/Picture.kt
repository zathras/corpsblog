package com.jovial.blog.md.extensions

import com.jovial.blog.Site
import com.jovial.os.OSImage
import com.jovial.util.JpegMetadata
import java.io.File

/**
 * Created by billf on 11/7/16.
 */

class Picture (
        source : File,
        val galleryDir : File,
        val caption : String
    )                           : AbstractImage(source)
{
    public var largeImage: String? = null /** file name of big image, including gallery dir name */
        private set

    public var largeImageSize: OSImage.Size? = null
        private set

    var smallImage: String? = null /** file name of small image, including gallery dir */
        private set

    var mosaicImage: String? = null  /** Relative file name of square image for mosaic */
            private set

    /**
     * Generate any images that need to be generated
     */
    fun generate(name: String, doGallery: Boolean, site: Site) {
        val other = site.allPictures[source]
        if (other != null) {
            largeImage = other.largeImage
            largeImageSize = other.largeImageSize
            smallImage = other.smallImage
            mosaicImage = other.mosaicImage
        } else {
            site.allPictures[source] = this
            largeImage = doGenerate(site, galleryDir, "large/$name.jpg", 1920)
            // Often the image is there already, so we just always parse the image to extract the width and
            // height.  It's an image we generated, so this is safe -- it's definitely a jpeg.
            val largeImageFile = File(galleryDir, "large/$name.jpg")
            val jpegMetadata = JpegMetadata(largeImageFile)
            jpegMetadata.read()
            largeImageSize = jpegMetadata.size!!
            smallImage = doGenerate(site, galleryDir, "small/$name.jpg", 384)
        }
        if (doGallery && mosaicImage == null) {
            mosaicImage = generateSquare(site, galleryDir, "mosaic/$name.jpg", 400)
            if (other != null) {
                assert(other.mosaicImage == null)
                other.mosaicImage = mosaicImage
            }
        }
        // 384 is arbitrary, but it is 1920/5
        flushSourceImage()
    }

    private fun doGenerate(site: Site, baseDir: File, relName: String, maxDimension: Int): String {
        val dest = File(baseDir, relName)
        val dep = site.dependencies.get(dest)
        if (dep.changed(listOf(source))) {
            val im = getImage()
            dest.parentFile.mkdirs()
            val maxSrc = if (im.size.width > im.size.height) im.size.width else im.size.height
            val scaled = if (maxDimension > maxSrc) {
                im
            } else {
                im.scaledBy(maxDimension.toDouble() / maxSrc.toDouble())
            }
            scaled.writeJpeg(dest)
            if (scaled != im) {
                scaled.flush()
            }
        }
        return "pictures/" + baseDir.name + "/" + relName
    }

    private fun generateSquare(site: Site, baseDir: File, relName: String, maxSize: Int): String {
        val dest = File(baseDir, relName)
        val dep = site.dependencies.get(dest)
        if (dep.changed(listOf(source))) {
            val im = getImage()
            dest.parentFile.mkdirs()
            val minDimension = Math.min(im.size.width, im.size.height)
            val size: Int = Math.min(maxSize, minDimension)
            val square = im.scaledToSquare(size)
            square.writeJpeg(dest)
        }
        return "pictures/" + baseDir.name + "/" + relName
    }
}
