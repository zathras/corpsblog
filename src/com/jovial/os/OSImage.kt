package com.jovial.os

import java.io.File

/**
 * Represents an image whose orientation is correctly set,
 * e.g. from the JPEG metadata, if available.
 */
interface OSImage {

    data class Size(
        val width : Int,
        val height : Int
    )

    fun flush()

    val size: Size

    fun scaledBy(factor: Double) : OSImage

    fun scaledToSquare(length: Int) : OSImage

    fun writeJpeg(dest: File)

}