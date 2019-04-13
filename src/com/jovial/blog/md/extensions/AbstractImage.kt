package com.jovial.blog.md.extensions

import com.jovial.os.OSImage
import com.jovial.os.OSImageFactory
import com.jovial.os.Stdout
import java.io.File
import java.io.IOException

/**
 * Created by w.foote on 3/8/2017.
 */
abstract  class AbstractImage(
        val source : File
) {
    private var sourceImage: OSImage? = null

    protected fun getImage(): OSImage {
        var im = sourceImage
        if (im != null) {
            return im;
        }
        try {
            Stdout.println("Processing ${source.absolutePath}")
            im = OSImageFactory.readImage(source)
        } catch (ex : IOException) {
            Stdout.println("Error reading $source")
            throw ex
        }
        sourceImage = im;
        return im
    }

    protected fun flushSourceImage() {
        val im = sourceImage
        if (im != null) {
            im.flush()
            sourceImage = null
        }
    }
}
