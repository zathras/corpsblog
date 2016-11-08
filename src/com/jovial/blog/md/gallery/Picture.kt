package com.jovial.blog.md.gallery

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
    var bigImage : String? = null   /** Relative file name of big image */
        private set

    var smallImage : String? = null /** Relative file name of small image */
        private set

    fun generateScaled(baseDir: File, relDir: String, name: String) {
        println("Processing ${source.absolutePath}")
        bigImage = doGenerate(baseDir, "${relDir}big/$name.jpg", 1920)
        smallImage = doGenerate(baseDir, "${relDir}small/$name.jpg", 384)
        // 384 is arbitrary, but it is 1920/5
        if (sourceImage != null) {
            sourceImage!!.flush()
            sourceImage = null
        }
    }

    private fun doGenerate(baseDir: File, relName: String, maxDimension: Int) : String {
        val dest = File(baseDir, relName)
        if (!dest.exists()) {
            var im = sourceImage
            if (im == null) {
                im = ImageIO.read(source)!!
                sourceImage = im
            }
            dest.parentFile.mkdirs()
            val maxSrc = if (im.width > im.height) im.width else im.height
            val scaled = if (maxDimension > maxSrc) {
                im
            } else {
                val scaleFactor = maxDimension.toDouble() / maxSrc.toDouble()
                val w = Math.round(im.width.toDouble() * scaleFactor).toInt()
                val h = Math.round(im.height.toDouble() * scaleFactor).toInt()
                val xform = AffineTransform.getScaleInstance(scaleFactor, scaleFactor)
                val op = AffineTransformOp(xform, null)
                val newImage = op.filter(sourceImage, null)
                newImage
            }
            ImageIO.write(scaled, "jpeg", dest)
            scaled.flush()
        }
        return relName
    }
}
