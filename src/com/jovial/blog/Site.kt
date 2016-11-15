package com.jovial.blog

import com.jovial.blog.md.gallery.Picture
import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.IndexContent
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.HTML
import templates.Archive
import templates.Feed
import templates.Index
import templates.Post
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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

    var deferredTxtmarkPostConfig : com.github.rjeschke.txtmark.Configuration? = null

    val txtmarkPostConfig: com.github.rjeschke.txtmark.Configuration by lazy {
        deferredTxtmarkPostConfig!!
    }

    val blogConfig = BlogConfig(File(inputDir, "corpsblog.config"))

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
        val images = File(inputDir, "images")
        val fOutDir = File(outputDir, "images")
        for (s in images.list()) {
            Files.copy(File(images, s).toPath(), File(fOutDir, s).toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        writeFile(Archive(blogConfig, "", posts).generate().toString(), File(outputDir, "archive.html"))
        writeFile(Feed(blogConfig, posts).generate(), File(outputDir, "feed.xml"))

        val content = IndexContent(txtmarkConfig)
        content.read(File(inputDir, "index.md"))
        writeFile(Index(blogConfig, content).generate().toString(), File(outputDir, "index.html"))
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
        val content = PostContent(txtmarkPostConfig, postOutputDir, baseName, pathFrom)
        content.read(File(inputDir, "$pathTo/$name"))
        val p = Post(
                blogConfig =blogConfig,
                content=content,
                pathTo=pathTo,
                fileName="$baseName.html")
        postOutputDir.mkdirs()
        val f = File(postOutputDir, "$baseName.html")
        val html = p.generate(olderName, newerName)
        writeFile(html.toString(), f)
        p.content.discardBody()
        posts.add(p)
    }

    private fun writeFile(content: String, outFile: File) {
        val w = OutputStreamWriter(FileOutputStream(outFile), "UTF-8")
        w.write(content)
        w.close();
        println("Wrote to file ${outFile.absolutePath}")
    }
}