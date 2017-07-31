package com.jovial.util

import java.io.*

/**
 * A simple-to-use Data stream class
 *
 * Created by billf on 7/30/17.
 */
class DataStream : Closeable {
    class EOF : IOException("EOF");

    private val str : DataInputStream
    private var next : Int


    constructor(name : File) {
        str = DataInputStream(BufferedInputStream(FileInputStream(name)))
        next = str.read()
    }

    override fun close() {
        str.close()
    }

    fun eof() : Boolean = next == -1

    fun readByte() : Int {
        if (eof()) {
            throw EOF()
        }
        val result = next
        next = str.read()
        return result
    }

    fun readShort() = (readByte() shl 8) or readByte()

    fun skip(count : Int) {
        var bytes = count
        if (bytes <= 0) {
            return
        }
        bytes--;    // To make up for populating next
        while (bytes > 0) {
            val skipped = str.skipBytes(bytes)
            bytes -= skipped
            if (skipped == 0) {
                readByte()
                bytes--
            }
        }
        next = str.read()
    }

}
