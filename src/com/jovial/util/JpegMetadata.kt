package com.jovial.util

import com.jovial.os.OSImage
import java.io.*

import com.jovial.util.JpegMetadata.Orientation.TOP_LEFT
import com.jovial.util.JpegMetadata.Orientation.TOP_RIGHT
import com.jovial.util.JpegMetadata.Orientation.BOTTOM_RIGHT
import com.jovial.util.JpegMetadata.Orientation.BOTTOM_LEFT
import com.jovial.util.JpegMetadata.Orientation.LEFT_TOP
import com.jovial.util.JpegMetadata.Orientation.RIGHT_TOP
import com.jovial.util.JpegMetadata.Orientation.RIGHT_BOTTOM
import com.jovial.util.JpegMetadata.Orientation.LEFT_BOTTOM

/**
 * Minimal code to read Exif orientation data and JPEG height and width.  Based on
 * https://www.codeproject.com/Articles/47486/Understanding-and-Reading-Exif-Data, but
 * it's wrong in places.
 * https://www.media.mit.edu/pia/Research/deepview/exif.html seems more reliable, but is
 * substantially harder to read.
 * See also https://www.daveperrett.com/articles/2012/07/28/exif-orientation-handling-is-a-ghetto/
 *
 *
 * Created by billf on 7/30/17.
 */


class JpegMetadata(val src : File) {

    enum class Orientation(val value : Int) {
            // Values cribbed from net.sourceforge.jheader.enumerations.Orientation
        TOP_LEFT(1),        //  0th row top, 0th col left
        TOP_RIGHT(2),       // 0th row top, 0th col right
        BOTTOM_RIGHT(3),    // 0th row bottom, 0th col right
        BOTTOM_LEFT(4),     // 0th row bottom, 0th col left
        LEFT_TOP(5),        // 0th row left, 0th col top
        RIGHT_TOP(6),       // 0th row right, 0th col top
        RIGHT_BOTTOM(7),    // 0th row right, 0th col bottom
        LEFT_BOTTOM(8)      // 0th row left, 0th col bottom
    }

    var isJpeg = false;
    var size : OSImage.Size? = null
        private set

    var orientation : Orientation? = null
        private set


    private companion object {
        val SOS = 0xda;     // Start of scan.  Image data follows
        val SOF0 = 0xc0;    // Start of frame 0.  Where the height and width are.
        val EXIF_MARKER = 0xe1
    }

    fun read() {
        DataStream(src).use { jpeg ->
            isJpeg = jpeg.readByte() == 0xff && jpeg.readByte() == 0xd8
            if (!isJpeg) {
                return
            }
            while (true) {
                if (jpeg.eof()) {
                    return;     // Odd...  No scan data.
                }
                if (orientation != null && size != null) {
                    return;     // We have what we need
                }
                var b = jpeg.readByte()
                if (b != 0xff) {
                    throw IOException("Unexpected byte ${hexStr(b)}")
                }
                b = jpeg.readByte()
                if (b == SOS) {
                    // Once we hit image data, there's no more metadata to read.
                    return;
                }
                val len = jpeg.readShort()
                if (b == SOF0) {
                    jpeg.readByte()     // precision
                    val height = jpeg.readShort()
                    val width = jpeg.readShort()
                    size = OSImage.Size(width, height)
                    jpeg.skip(len - 7)
                } else if (b == EXIF_MARKER) {
                    val exif = ExifReader(jpeg)
                    val read = exif.read()
                    if (exif.orientation != null) {
                        orientation = exif.orientation
                    }
                    jpeg.skip(len - 2 - read)
                } else {
                    jpeg.skip(len - 2)
                }
            }
        }
    }
    private fun hexStr(b : Int) = "0x%02x".format(b)

}

private class ExifReader(val jpeg : DataStream) {

    var bytesRead = 0
    var bigEndian = true
    var orientation : JpegMetadata.Orientation? = null
        private set

    fun readByte() : Int {
        bytesRead++
        return jpeg.readByte()
    }

    fun readChar() : Char {
        bytesRead++
        return jpeg.readByte().toChar()
    }

    fun readInt(bytes : Int) : Int {
        var result = 0
        if (bigEndian) {
            for (i in 1..bytes) {
                result = (result shl 8) or readByte()
            }
        } else {
            for (shift in 0..(bytes-1)) {
                val digit = readByte()
                result = result or (digit shl (8 * shift))
            }
        }
        return result
    }

    fun skip(bytes : Int) {
        bytesRead += bytes
        jpeg.skip(bytes)
    }

    fun read() : Int {
        if (readChar() != 'E'
                || readChar() != 'x'
                || readChar() != 'i'
                || readChar() != 'f'
                || readByte() != 0
                || readByte() != 0)
        {
            throw IOException("EXIF format error")
        }
        val e1 = readByte()
        val e2 = readByte()
        if (e1 != e2 || (e1 != 'M'.code && e2 != 'I'.code)) {
            throw IOException("EXIF endian error:  $e1, $e2 (I is ${'I'.code}, M is ${'M'.code})")
        }
        bigEndian = e1 != 'I'.code
        val check = readInt(2)
        if (check != 0x2a) {
            throw IOException("EXIF check error:  $check seen, 0x2a (42) expected")
        }
        val offset = readInt(4)
        skip(offset - (bytesRead - 6))
        val numTags = readInt(2)
        for (i in 1..numTags) {
            val tag = readInt(2)  // tag
            readInt(2)  // fmt
            readInt(4)   // numComponents
            val value = readInt(4)
            if (tag == 0x112) {     // orientation
                orientation = when (value) {
                    TOP_LEFT.value      ->  TOP_LEFT
                    TOP_RIGHT.value     ->  TOP_RIGHT
                    BOTTOM_RIGHT.value  -> BOTTOM_RIGHT
                    BOTTOM_LEFT.value   -> BOTTOM_LEFT
                    LEFT_TOP.value      -> LEFT_TOP
                    RIGHT_TOP.value     -> RIGHT_TOP
                    RIGHT_BOTTOM.value  -> RIGHT_BOTTOM
                    LEFT_BOTTOM.value   -> LEFT_BOTTOM
                    else                -> throw IOException("Bad orientation value $value")
                }
            }
        }
        return bytesRead
    }
}
