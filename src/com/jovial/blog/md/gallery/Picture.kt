package com.jovial.blog.md.gallery

import net.sourceforge.jheader.App1Header
import net.sourceforge.jheader.JpegHeaders
import net.sourceforge.jheader.enumerations.Orientation
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.io.File
import javax.imageio.ImageIO

/**
 * Created by billf on 11/7/16.
 */

class Picture (
        val source : File,
        val caption : String
) {
    private var sourceImage: BufferedImage? = null
    var bigImage: String? = null /** Relative file name of big image */
    private set

    var smallImage: String? = null /** Relative file name of small image */
    private set

    var galleryImage: String? = null  /** Relative file name of square image for gallery */
    private set

    // @@@@  Only generate gallery images if needed
    fun generateScaled(baseDir: File, relDir: String, name: String) {
        println("Processing ${source.absolutePath}")
        bigImage = doGenerate(baseDir, "${relDir}big/$name.jpg", 1920)
        smallImage = doGenerate(baseDir, "${relDir}small/$name.jpg", 384)
        galleryImage = generateSquare(baseDir, "${relDir}gallery/$name.jpg", 400)
        // 384 is arbitrary, but it is 1920/5
        if (sourceImage != null) {
            sourceImage!!.flush()
            sourceImage = null
        }
    }

    private fun getImage(): BufferedImage {
        var im = sourceImage
        if (im != null) {
            return im;
        }
        im = ImageIO.read(source)!!
        try {
            val jpegParser = JpegHeaders(source.absoluteFile.toString())
            // Didn't bother to check filename extension, since that could be wrong anyway.
            // Instead, I rely on the JPEG parser to throw an exception if it gets a different
            // image type.
            val orientation = jpegParser.app1Header?.getValue(App1Header.Tag.ORIENTATION)?.
                    asEnumeration as? Orientation
            val xform: AffineTransform? = when (orientation?.asLong()) {
                null, Orientation.TOP_LEFT ->
                    null
                Orientation.TOP_RIGHT -> {
                    val t = AffineTransform()
                    t.scale(-1.0, 1.0)
                    t.translate(-im.width.toDouble(), 0.0)
                    t
                }
                Orientation.BOTTOM_RIGHT -> {
                    val t = AffineTransform()
                    t.translate(im.width.toDouble(), im.height.toDouble())
                    t.quadrantRotate(2)
                    t
                }
                Orientation.BOTTOM_LEFT -> {
                    val t = AffineTransform()
                    t.scale(1.0, -1.0)
                    t.translate(0.0, -im.height.toDouble())
                    t
                }
                Orientation.LEFT_TOP -> {
                    val t = AffineTransform()
                    t.quadrantRotate(3)
                    t.scale(-1.0, 1.0)
                    t
                }
                Orientation.RIGHT_TOP -> {
                    val t = AffineTransform()
                    t.translate(im.height.toDouble(), 0.0)
                    t.quadrantRotate(1)
                    t
                }
                Orientation.RIGHT_BOTTOM -> {
                    val t = AffineTransform()
                    t.scale(-1.0, 1.0)
                    t.translate(-im.height.toDouble(), 0.0)
                    t.translate(0.0, im.width.toDouble())
                    t.quadrantRotate(3)
                    t
                }
                Orientation.LEFT_BOTTOM -> {
                    val t = AffineTransform()
                    t.translate(0.0, im.width.toDouble())
                    t.quadrantRotate(3)
                    t
                }
                else -> {
                    null
                }
            }
            if (xform != null) {
                val op = AffineTransformOp(xform, null)
                val newImage = op.filter(im, null)
                im.flush()
                im = newImage
            }
        } catch (ignored: Exception) {
            // This seems to happen with valid JPEG images that don't have rotation data.
            // I'm not sure why, but it's harmless.
        }
        sourceImage = im
        return im
    }

    private fun doGenerate(baseDir: File, relName: String, maxDimension: Int): String {
        val dest = File(baseDir, relName)
        if (!dest.exists()) {
            var im = getImage()
            dest.parentFile.mkdirs()
            val maxSrc = if (im.width > im.height) im.width else im.height
            val scaled = if (maxDimension > maxSrc) {
                im
            } else {
                val scaleFactor = maxDimension.toDouble() / maxSrc.toDouble()
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
        return relName
    }

    private fun generateSquare(baseDir: File, relName: String, maxSize: Int): String {
        val dest = File(baseDir, relName)
        if (!dest.exists()) {
            var im = getImage()
            dest.parentFile.mkdirs()
            val minDimension = Math.min(im.width, im.height)
            val size = Math.min(maxSize, minDimension)
            val square  = BufferedImage(size, size, im.type)
            val g = square.createGraphics();
            val scaleFactor = size.toDouble() / minDimension.toDouble()
            g.scale(scaleFactor, scaleFactor)
            val dx = if (im.width > minDimension) {
                (minDimension - im.width) / 2
            } else {
                0
            }
            val dy = if (im.height > minDimension) {
                (minDimension - im.height) / 2
            } else {
                0
            }
            g.drawImage(im, dx, dy, null)
            g.dispose()
            ImageIO.write(square, "jpeg", dest)
            square .flush()
        }
        return relName
    }
}
