
package com.jovial.os

import com.jovial.util.JpegMetadata
import com.jovial.util.JpegMetadata.Orientation
import com.jovial.os.OSImage.Size
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object OSImageFactory {
    fun readImage(source: File) : OSImage = OSFileImageImpl(source)
}

private abstract class OSImageImpl : OSImage {

    protected val image : BufferedImage           // Unrotated image

    constructor(image: BufferedImage) {
        this.image = image
    }

    override fun writeJpeg(dest: File) {
        val xform = getRotationXform()
        if (xform == null) {
            ImageIO.write(image, "jpeg", dest)
        } else {
            val op = AffineTransformOp(xform, null)
            val newImage = op.filter(image, null)
            ImageIO.write(newImage, "jpeg", dest)
            newImage.flush()
        }
    }

    protected open fun getRotationXform() : AffineTransform? = null

    override fun scaledToSquare(length: Int) : OSImage {
        val minDimension = Math.min(size.width, size.height)
        val factor = length.toDouble() / minDimension.toDouble()
        val dx = if (size.width > minDimension) {
            (minDimension - size.width) * factor / 2
        } else {
            0.0
        }
        val dy = if (size.height > minDimension) {
            (minDimension - size.height) * factor / 2
        } else {
            0.0
        }
        val square  = BufferedImage(length, length, image.type)
        val g = square.createGraphics();
        val combinedXform = AffineTransform.getTranslateInstance(dx, dy);
        combinedXform.concatenate(AffineTransform.getScaleInstance(factor, factor))
        val rotation = getRotationXform()
        if (rotation != null) {
            combinedXform.concatenate(rotation)
        }
        g.transform = combinedXform
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return OSScaledImageImpl(square, Size(length, length))
    }

    override fun flush() {
        image.flush()
    }
}

private class OSFileImageImpl : OSImageImpl {

    private val rotateXform : AffineTransform?        // If needed, fixes rotation
    override val size
        get() = theSize
    private val theSize : Size

    constructor(source: File) : super(ImageIO.read(source)){
        var mySize: Size? = null
        rotateXform = try {
            val jpegMetadata = JpegMetadata(source.absoluteFile)
            jpegMetadata.read()
            // Didn't bother to check filename extension, since that could be wrong anyway.
            // Instead, the JPEG parser checks if it's really a JPEG File by sniffing the
            // first few bytes..
            when (jpegMetadata.orientation) {
                null, Orientation.TOP_LEFT -> {
                    mySize = Size(image.width, image.height)
                    null
                }
                Orientation.TOP_RIGHT -> {
                    mySize = Size(image.width, image.height)
                    val t = AffineTransform()
                    t.scale(-1.0, 1.0)
                    t.translate(-image.width.toDouble(), 0.0)
                    t
                }
                Orientation.BOTTOM_RIGHT -> {
                    mySize = Size(image.width, image.height)
                    val t = AffineTransform()
                    t.translate(image.width.toDouble(), image.height.toDouble())
                    t.quadrantRotate(2)
                    t
                }
                Orientation.BOTTOM_LEFT -> {
                    mySize = Size(image.width, image.height)
                    val t = AffineTransform()
                    t.scale(1.0, -1.0)
                    t.translate(0.0, -image.height.toDouble())
                    t
                }
                Orientation.LEFT_TOP -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val t = AffineTransform()
                    t.quadrantRotate(3)
                    t.scale(-1.0, 1.0)
                    t
                }
                Orientation.RIGHT_TOP -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val t = AffineTransform()
                    t.translate(image.height.toDouble(), 0.0)
                    t.quadrantRotate(1)
                    t
                }
                Orientation.RIGHT_BOTTOM -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val t = AffineTransform()
                    t.scale(-1.0, 1.0)
                    t.translate(-image.height.toDouble(), 0.0)
                    t.translate(0.0, image.width.toDouble())
                    t.quadrantRotate(3)
                    t
                }
                Orientation.LEFT_BOTTOM -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val t = AffineTransform()
                    t.translate(0.0, image.width.toDouble())
                    t.quadrantRotate(3)
                    t
                }
            }
        } catch (ignored: Exception) {
            Stdout.println("  Warning:  Ignoring $ignored for image $source")
            Stdout.println("    Check rotation of result?")
            mySize = Size(image.width, image.height)
            null
        }
        theSize = mySize!!
    }

    override fun getRotationXform() = rotateXform

    override fun scaledBy(factor: Double) : OSImage {
        val combinedXform = AffineTransform.getScaleInstance(factor, factor)
        if (rotateXform != null) {
            combinedXform.concatenate(rotateXform)
        }
        val op = AffineTransformOp(combinedXform, null)
        val newImage = op.filter(image, null)
        return OSScaledImageImpl(newImage, Size(newImage.width, newImage.height))
    }
}

private class OSScaledImageImpl : OSImageImpl {

    override val size: Size

    constructor(image: BufferedImage, size: Size) : super(image) {
        this.size = size
    }

    override fun scaledBy(factor: Double) : OSImage {
        val scaleXform = AffineTransform.getScaleInstance(factor, factor)
        val op = AffineTransformOp(scaleXform, null)
        val newImage = op.filter(image, null)
        return OSScaledImageImpl(newImage, Size(newImage.width, newImage.height))
    }
}

