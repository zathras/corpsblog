package com.jovial.blog.md.extensions

import com.jovial.blog.Site
import net.sourceforge.jheader.App1Header
import net.sourceforge.jheader.JpegHeaders
import net.sourceforge.jheader.enumerations.Orientation
import java.awt.Dimension
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Created by w.foote on 3/8/2017.
 */
abstract  class AbstractImage(
        val source : File
) {
    private var sourceImage: BufferedImage? = null

    protected fun getImage(): BufferedImage {
        var im = sourceImage
        if (im != null) {
            return im;
        }
        try {
            println("Processing ${source.absolutePath}")
            im = ImageIO.read(source)!!
        } catch (ex : IOException) {
            println("Error reading $source")
            throw ex
        }
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
            println("  Warning:  Ignoring $ignored")
            println("    Check rotation of result?")
        }
        sourceImage = im
        return im
    }

    protected fun flushSourceImage() {
        if (sourceImage != null) {
            sourceImage!!.flush()
            sourceImage = null
        }
    }
}