package com.jovial.blog

import com.jovial.blog.md.gallery.Picture
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.Content
import templates.Post
import java.io.File
import java.io.FileWriter

/**
 * Class to generate the site.
 *
 * Created by billf on 11/7/16.
 */
class Site (
        val inputDir: File,
        val outputDir: File
){
    var deferredTxtmarkConfig : com.github.rjeschke.txtmark.Configuration? = null

    val txtmarkConfig: com.github.rjeschke.txtmark.Configuration by lazy {
        deferredTxtmarkConfig!!
    }

    val blogConfig = BlogConfig()

    /**
     * We keep a record of all the generated we have, to avoid re-generating them
     */
    val allPictures = mutableMapOf<File, Picture>()

    public fun generate() {
        val postsSrc = File(inputDir, "posts")
        for (name in postsSrc.list().sortedBy{ s -> s.toLowerCase() } ) {
            if (!name.toLowerCase().endsWith(".md")) {
                println("""Skipping post $name in $postsSrc:  File name doesn't end in ".md".""")
                continue
            }
            generatePost("posts", "../", name)
        }
    }

    /**
     * Generate a post
     *
     * @param pathTo    Relative path from the base directory to the posts directory
     * @param pathFrom  Relative path back to the root (like "../")
     * @param name      Name of the file within pathTo, ending in ".md"
     */
    private fun generatePost(pathTo: String, pathFrom: String, name: String) {
        val baseName = name.dropLast(3)
        val postOutputDir = File(outputDir, pathTo)
        val content = Content(txtmarkConfig, postOutputDir, baseName, pathFrom)
        content.read(File(inputDir, "$pathTo/$name"))
        val p = Post(blogConfig, content)
        postOutputDir.mkdirs()
        val f = File(postOutputDir, "$baseName.html")
        val w = FileWriter(f)
        w.write(p.generate().toString())
        w.close();
        println("Wrote post to ${f.toURI()}")
    }
}