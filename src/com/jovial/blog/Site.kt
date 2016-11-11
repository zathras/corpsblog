package com.jovial.blog

import com.jovial.blog.md.gallery.Picture
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.Content
import com.jovial.lib.html.HTML
import templates.Archive
import templates.Index
import templates.Post
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter

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

    val posts = mutableListOf<Post>()

    public fun generate() {
        val postsSrc = File(inputDir, "posts")
        val postFiles = postsSrc.list().
                sortedBy { s -> s.toLowerCase() }.
                filter {
                    if (!it.toLowerCase().endsWith(".md")) {
                        println("""Skipping post $it in $postsSrc:  File name doesn't end in ".md".""")
                        false
                    } else {
                        true
                    }
                }
        for (i in 0..postFiles.size-1) {
            generatePost("posts", "../", postFiles, i)
        }
        writeHtml(Archive(blogConfig, "", posts).generate(), File(outputDir, "archive.html"))
    }

    private fun writeHtml(html: HTML, outFile: File) {
        val w = OutputStreamWriter(FileOutputStream(outFile), "UTF-8")
        w.write(html.toString())
        w.close();
    }

    /**
     * Generate a post
     *
     * @param pathTo    Relative path from the base directory to the posts directory
     * @param pathFrom  Relative path back to the root (like "../")
     * @param name      Name of the file within pathTo, ending in ".md"
     */
    private fun generatePost(pathTo: String, pathFrom: String, src: List<String>, index: Int) {
        val name = src[index]
        val baseName = name.dropLast(3)
        val olderName = if (index == 0) { null } else { src[index-1].dropLast(3)+".html" }
        val newerName = if (index+1 == src.size) { null } else { src[index+1].dropLast(3)+".html" }
        val postOutputDir = File(outputDir, pathTo)
        val content = Content(txtmarkConfig, postOutputDir, baseName, pathFrom)
        content.read(File(inputDir, "$pathTo/$name"))
        val p = Post(blogConfig, content, "$baseName.html")
        postOutputDir.mkdirs()
        val f = File(postOutputDir, "$baseName.html")
        val html = p.generate(olderName, newerName)
        writeHtml(html, f)
        println("Wrote post to ${f.toURI()}")
        p.content.discardBody()
        posts.add(p)
    }
}