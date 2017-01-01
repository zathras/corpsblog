package com.jovial.blog

import com.jovial.blog.md.extensions.Picture
import com.jovial.blog.model.*
import com.jovial.webapi.OAuth
import com.jovial.google.YouTube
import com.jovial.templates.lib.HTML
import com.jovial.mailchimp.Mailchimp
import com.jovial.util.processFileName
import com.jovial.util.urlEncode
import com.jovial.templates.*
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Class to generate the site.
 *
 * Created by billf on 11/7/16.
 */
class Site (
        val inputDir: File,
        val outputDir: File,
        val blogConfig : BlogConfig,
        val publish : Boolean
){

    var deferredTxtmarkConfig : com.github.rjeschke.txtmark.Configuration? = null

    val txtmarkConfig: com.github.rjeschke.txtmark.Configuration by lazy {
        deferredTxtmarkConfig!!
    }

    var deferredTxtmarkPostConfig : com.github.rjeschke.txtmark.Configuration? = null

    val txtmarkPostConfig: com.github.rjeschke.txtmark.Configuration by lazy {
        deferredTxtmarkPostConfig!!
    }

    val dbDir = File(outputDir.canonicalPath + ".db")

    val postsSrcDir = File(inputDir, "posts")

    val dependencies = DependencyManager(dbDir, "dependencies.json")

    val youtubeManager : YouTube?

    val mailchimpManager : Mailchimp?

    private val errors = mutableListOf<String>()
    private val notes = mutableListOf<String>()

    init {
        dbDir.mkdirs()
        val googleClientConfig = blogConfig.googleClient
        if (googleClientConfig == null) {
            youtubeManager = null
        } else {
            youtubeManager = YouTube(dbDir, googleClientConfig, blogConfig.remote_upload, blogConfig.googleOauthBrowser)
        }

        val mailchimpClientConfig = blogConfig.mailchimpClient
        if (mailchimpClientConfig == null) {
            mailchimpManager = null
        } else {
            mailchimpManager = Mailchimp(dbDir, mailchimpClientConfig, blogConfig.mailchimpOauthBrowser)
        }
    }

    /**
     * We keep a record of all the generated we have, to avoid re-generating them
     */
    val allPictures = mutableMapOf<File, Picture>()

    val posts = mutableListOf<Post>()

    private val tagMap = mutableMapOf<String, MutableList<Post>>()
    // Map from tag name to list of posts, in date order starting with the earliest post


    public fun error(message: String) {
        println(message)
        errors.add(message)
    }

    public fun note(message: String) {
        println(message)
        notes.add(message)
    }

    public fun printNotes() : Unit {
        if (!notes.isEmpty()) {
            println()
            println("Notes about site generation:")
            for (e in notes) {
                println("    $e")
            }
        }
    }

    public fun hasErrors() : Boolean = !errors.isEmpty()

    public fun printErrors() : Unit {
        println("Site errors:")
        for (e in errors) {
            println("    $e")
        }
    }

    fun generate() {
        dependencies.read()

        val images = File(inputDir, "images")
        val fOutDir = File(outputDir, "images")
        fOutDir.mkdirs()
        for (s in images.list()) {
            val inputFile = File(images, s)
            val outputFile = File(fOutDir, s)
            val dep = dependencies.get(outputFile)
            if (dep.changed(listOf(inputFile))) {
                Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
        copyCorpsblogAssets()
        val postFiles = postsSrcDir.list().
                sortedBy { s -> s.toLowerCase() }.
                filter {
                    if (!it.toLowerCase().endsWith(".md")) {
                        if (!File(postsSrcDir, it).isDirectory()) {
                            println("""Skipping file $it in $postsSrcDir:  File name doesn't end in ".md".""")
                            // Directories are for assets referenced in a post, so we don't isssue a warning.
                        }
                        false
                    } else {
                        true
                    }
                }
        for (i in 0..postFiles.size - 1) {
            generatePost("posts", "../", postFiles, i)
        }
        for ((t, taggedPosts) in tagMap) {
            generateTagFile("tags", "../", t, taggedPosts)
        }

        val generatedPosts = posts.map { it.outputFile }
        val archiveFile = File(outputDir, "archive.html")
        generateDependingOn(generatedPosts, archiveFile) {
            Archive(blogConfig, posts).generate().toString()
        }
        generateDependingOn(listOf(archiveFile), File(outputDir, "feed.xml")) {
            Feed(blogConfig, posts).generate()
        }
        val indexContent = IndexContent(txtmarkConfig)
        val indexOutputFile = File(outputDir, "index.html")
        val indexInputFile = File(inputDir, "index.md")
        generateDependingOn(listOf(archiveFile, indexInputFile), indexOutputFile) {
            indexContent.read(indexInputFile)
            Index(blogConfig, indexContent, posts).generate().toString()
        }
        generateDependingOn(listOf(indexOutputFile), File(outputDir, "sitemap.xml")) {
            if (!indexContent.readWasCalled) {
                indexContent.read(indexInputFile)   // For the date
            }
            Sitemap(this, indexContent.date).generate()
        }
        val contactInputFile = File(inputDir, "contact.md")
        generateDependingOn(listOf(contactInputFile), File(outputDir, "contact.html")) {
            val contactContent = ContactContent(txtmarkConfig)
            contactContent.read(contactInputFile)
            Contact(blogConfig, contactContent).generate().toString()
        }
        dependencies.write()

        mailchimpManager?.checkNotifications(this)

        checkForStrayOutputFiles(outputDir)
    }

    /**
     * Copy the built-in corpsblog assets
     */
    private fun copyCorpsblogAssets() {
        val input = BufferedReader(InputStreamReader(
                        javaClass.getResourceAsStream("/src/resource_list.txt")!!, "UTF8"))
        val buffer = ByteArray(65536)
        while (true) {
            val line = input.readLine()
            if (line == null) {
                break
            } else if (line.startsWith("src/") || line.startsWith("./src/")) {
                continue
            }
            val outputFile = File(outputDir, line)
            val dependsOn = dependencies.get(outputFile)
            if (dependsOn.changed(listOf<File>())) {
                // We don't really depend on anything, because our source is intrinsic.
                // This records the copy, and it checks for the existence of the destination.
                println("Copying " + line)
                outputFile.parentFile.mkdirs()
                val resOut = FileOutputStream(outputFile)
                val resIn = javaClass.getResource("/" + line).openStream()
                while (true) {
                    val read = resIn.read(buffer)
                    if (read == -1) {
                        break
                    }
                    resOut.write(buffer, 0, read)
                }
                resIn.close()
                resOut.close()
            }
        }
        input.close()
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
        val olderName = if (index == 0) {
            null
        } else {
            src[index - 1].dropLast(3) + ".html"
        }
        val newerName = if (index + 1 == src.size) {
            null
        } else {
            src[index + 1].dropLast(3) + ".html"
        }
        val postOutputDir = File(outputDir, pathTo)
        val outputFile = File(postOutputDir, "$baseName.html")
        val dependencyFiles = mutableListOf<File>()
        val content = PostContent(txtmarkPostConfig, postOutputDir, baseName, pathFrom, dependencyFiles)
        val inputFile = File(inputDir, "$pathTo/$name")
        dependencyFiles.add(inputFile)
        content.read(inputFile)
        val p = Post(
                blogConfig = blogConfig,
                content = content,
                pathTo = pathTo,
                outputFile = outputFile)
        val dependencyValues = mutableListOf<String>()
        if (olderName != null) dependencyValues.add(olderName)
        if (newerName != null) dependencyValues.add(newerName)
        for (u in content.videoURLs) {
            dependencyValues.add(u)
        }
        val dependsOn = dependencies.get(outputFile)
        if (dependsOn.changed(dependencyFiles, dependencyValues)) {
            postOutputDir.mkdirs()
            val html = p.generate(olderName, newerName)
            writeFile(html.toString(), outputFile)
        }
        p.content.discardBody()
        posts.add(p)
        for (t in content.tags) {
            val list = tagMap[t]
            if (list == null) {
                tagMap[t] = mutableListOf(p)
            } else {
                list.add(p)
            }
        }
    }

    private fun generateTagFile(pathTo: String, pathFrom: String, tag: String, taggedPosts: List<Post>) {
        val tagOutputDir = File(outputDir, pathTo)
        val outputFile = File(tagOutputDir, "$tag.html")
        val dependsOn = dependencies.get(outputFile)
        val dependencyFiles = mutableListOf<File>()
        for (p in taggedPosts) {
            dependencyFiles.add(p.outputFile)
        }
        if (dependsOn.changed(dependencyFiles)) {
            val html = Tags(blogConfig, pathTo, pathFrom, tag, taggedPosts).generate()
            tagOutputDir.mkdirs()
            writeFile(html.toString(), outputFile)
        }
    }

    private fun generateDependingOn(dependencyFiles: List<File>,
                                    outputFile: File,
                                    contents: () -> String)
    {
        val dependsOn = dependencies.get(outputFile)
        if (dependsOn.changed(dependencyFiles)) {
            writeFile(contents(), outputFile)
        }
    }

    private fun writeFile(content: String, outFile: File) {
        val w = OutputStreamWriter(FileOutputStream(outFile), "UTF-8")
        w.write(content)
        w.close();
        println("Wrote to file ${outFile.absolutePath}")
    }

    private fun checkForStrayOutputFiles(dir : File, foundInput: Boolean = false) : Boolean {
        var found = foundInput
        for (s in dir.list()) {
            val f = File(dir, s)
            if (f.isDirectory) {
                if (checkForStrayOutputFiles(f, found)) {
                    found = true
                }
            } else if (dependencies.check(f) == null) {
                if (!found) {
                    found = true
                    println("Warning -- Stray files found in output directory:")
                }
                println("    $f")
            }
        }
        return found
    }
}