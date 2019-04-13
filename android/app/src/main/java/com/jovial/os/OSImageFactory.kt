 
package com.jovial.os

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.File
import com.jovial.util.JpegMetadata
import com.jovial.util.JpegMetadata.Orientation
import com.jovial.os.OSImage.Size
import java.io.BufferedOutputStream

object OSImageFactory {

    fun readImage(source: File) : OSImage = OSFileImageImpl(source)
    // see https://gist.github.com/9re/1990019
}

private abstract class OSImageImpl : OSImage {

    protected val image : Bitmap    // A pixmap

    constructor(image: Bitmap) {
        this.image = image
    }

    override fun writeJpeg(dest: File) {
        BufferedOutputStream(dest.outputStream()).use { out ->
            val xform = getRotationXform()
            if (xform == null) {
                image.compress(Bitmap.CompressFormat.JPEG, 90, out)
            } else {
                val newImage = Bitmap.createBitmap(image, 0, 0, image.width, image.height, xform, true)
                newImage.compress(Bitmap.CompressFormat.JPEG, 90, out)
                newImage.recycle()
            }
        }
    }

    protected open fun getRotationXform() : Matrix? = null

    override fun scaledToSquare(length: Int) : OSImage {
        val minDimension = Math.min(size.width, size.height)
        val factor = length.toFloat() / minDimension.toFloat()
        val dx = if (size.width > minDimension) {
            (minDimension - size.width) * factor / 2
        } else {
            0.0f
        }
        val dy = if (size.height > minDimension) {
            (minDimension - size.height) * factor / 2
        } else {
            0.0f
        }
        val square = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888)
        val g = Canvas(square)
        // NOTE:  Android's "pre-concatenate" with matrix operations is actually post-concatenate.
        //        They screwed up.  cf. http://i-rant.arnaudbos.com/2d-transformations-android-java/
        //        In fainess, the correct definitions are counter-intuitive:  a concatenate operation
        //        (androids "preConcat") has the same effect as first transforming the image by the
        //        concatenated operation.
        val combinedXform = Matrix()
        combinedXform.setTranslate(dx, dy)
        combinedXform.preScale(factor, factor)
        val rotation = getRotationXform()
        if (rotation != null) {
            combinedXform.preConcat(rotation)
        }
        g.drawBitmap(image, combinedXform, null)
        return OSScaledImageImpl(square, Size(length, length))

    }

    override fun flush() {
        image.recycle()
    }
}

private class OSFileImageImpl : OSImageImpl {

    private val rotateXform : Matrix?       // if needed, fixes rotation
    override val size: OSImage.Size
        get() = theSize
    private val theSize : Size

    constructor(source: File) : super(BitmapFactory.decodeFile(source.absolutePath, null)) {
        var mySize: Size? = null;
        // NOTE:  Android's "pre-concatenate" with matrix operations is actually post-concatenate.
        //        They screwed up.  cf. http://i-rant.arnaudbos.com/2d-transformations-android-java/
        //        In fainess, the correct definitions are counter-intuitive:  a concatenate operation
        //        (androids "preConcat") has the same effect as first transforming the image by the
        //        concatenated operation.
        rotateXform = try {
            // I could use Android's android.media.ExifInterface instead of this home grown/copied
            // JpegMetadata, but JpegData is what's used on desktop, so this way it's guaranteed
            // to be consistent.
            val jpegMetadata = JpegMetadata(source.absoluteFile)
            jpegMetadata.read()
            // Didn't bother to check filename extension, since that could be wrong anyway.
            // Instead, the JPEG parser checks if it's really a JPEG File by sniffing the
            // first few bytes.
            when (jpegMetadata.orientation) {
                null, Orientation.TOP_LEFT -> {
                    mySize = Size(image.width, image.height)
                    null
                }
                Orientation.TOP_RIGHT -> {
                    mySize = Size(image.width, image.height)
                    val m = Matrix()
                    m.setScale(-1.0f, 1.0f)
                    m.preTranslate(-image.width.toFloat(), 0f)
                    m
                }
                Orientation.BOTTOM_RIGHT -> {
                    mySize = Size(image.width, image.height)
                    val m = Matrix()
                    m.setTranslate(image.width.toFloat(), image.height.toFloat())
                    m.preRotate(180f)
                    m
                }
                Orientation.BOTTOM_LEFT -> {
                    mySize = Size(image.width, image.height)
                    val m = Matrix()
                    m.setScale(1.0f, -1.0f)
                    m.preTranslate(0f, -image.height.toFloat())
                    m
                }
                Orientation.LEFT_TOP -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val m = Matrix()
                    m.setRotate(270f)
                    m.preScale(-1.0f, 1.0f)
                    m
                }
                Orientation.RIGHT_TOP -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val m = Matrix()
                    m.setTranslate(image.height.toFloat(), 0f)
                    m.preRotate(90f)
                    m
                }
                Orientation.RIGHT_BOTTOM -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val m = Matrix()
                    m.setScale(-1f, 1f)
                    m.preTranslate(-image.height.toFloat(), image.width.toFloat())
                    m.preRotate(270f)
                    m
                }
                Orientation.LEFT_BOTTOM -> {
                    mySize = Size(image.height, image.width)  // swapped
                    val m = Matrix()
                    m.setTranslate(0f, image.width.toFloat())
                    m.preRotate(270f)
                    m
                }
            }
        } catch (ignored: Exception) {
            println("  Warning:  Ignoring $ignored for image $source")
            println("    Check rotation of result?")
            mySize = Size(image.width, image.height)
            null
        }
        theSize = mySize!!
    }

    override fun getRotationXform() = rotateXform

    override fun scaledBy(factor: Double) : OSImage {
        val fFactor = factor.toFloat()
        val combinedXform = Matrix()
        combinedXform.setScale(fFactor, fFactor)
        if (rotateXform != null) {
            combinedXform.preConcat(rotateXform)
        }
        val newImage = Bitmap.createBitmap(image, 0, 0, image.width, image.height, combinedXform, true)
        return OSScaledImageImpl(newImage, Size(newImage.width, newImage.height))   }
}

private class OSScaledImageImpl : OSImageImpl {

    override val size: Size

    constructor(image: Bitmap, size: Size) : super(image) {
        this.size = size
    }

    override fun scaledBy(factor: Double): OSImage {
        val fFactor = factor.toFloat()
        val xform = Matrix()
        xform.setScale(fFactor, fFactor)
        val newImage = Bitmap.createBitmap(image, 0, 0, image.width, image.height, xform, true)
        return OSScaledImageImpl(newImage, Size(newImage.width, newImage.height))
    }
}
