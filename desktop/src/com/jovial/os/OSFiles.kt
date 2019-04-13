package com.jovial.os

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime

object OSFiles {

    fun copyReplace(input: File, output: File) {
        Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    fun setLastModifiedTimeMS(outputFile: File, modifiedTimeMS: Long) {
        Files.setLastModifiedTime(outputFile.toPath(), FileTime.fromMillis(modifiedTimeMS))
    }
}