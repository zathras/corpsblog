package com.jovial.os

import java.io.File
import java.io.InputStream

object OSFiles {

    fun copyReplace(inputFile: File, outputFile: File) {
        val buf = ByteArray(16 * 1024)
        inputFile.inputStream().use { input ->
            outputFile.outputStream().use { output ->
                while (true) {
                    val gotten = input.read(buf)
                    if (gotten == -1) {
                        break
                    }
                    output.write(buf, 0, gotten)
                }
            }
        }
    }

    fun setLastModifiedTimeMS(outputFile: File, modifiedTimeMS: Long) {
        outputFile.setLastModified(modifiedTimeMS)
    }
}