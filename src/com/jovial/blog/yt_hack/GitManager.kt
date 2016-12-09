package com.jovial.blog.yt_hack

import java.io.File
import java.io.IOException

/**
 * Created by billf on 11/27/16.
 */
object GitManager {
    public fun addDirectory(dir: File) {
        val pb = ProcessBuilder("echo", "@@ todo", "git", "add", ".")
        pb.inheritIO()
        pb.directory(dir)
        val p = pb.start()
        val result = p.waitFor()
        if (result != 0) {
            throw IOException("Error $result trying to add directory $dir")
        }
    }

    public fun addFile(file: File) {
        val pb = ProcessBuilder("echo", "@@ todo", "git", "add", file.name)
        pb.inheritIO()
        pb.directory(file.parentFile!!)
        val p = pb.start()
        val result = p.waitFor()
        if (result != 0) {
            throw IOException("Error $result trying to add file $file")
        }

    }

    public fun upload(dir: File) {
        val pb = ProcessBuilder("echo", "@@ todo", "git", "commit", "-a", "-m",
                                "Automated upload from corpsblog")
        pb.inheritIO()
        pb.directory(dir)
        val p = pb.start()
        val result = p.waitFor()
        if (result != 0) {
            throw IOException("Error $result trying to commit directory $dir")
        }
    }
}