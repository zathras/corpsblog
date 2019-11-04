package com.jovial.blog

import com.jovial.blog.md.extensions.GalleryExtension
import com.jovial.blog.md.extensions.GalleryPicture
import com.jovial.blog.md.extensions.VideoExtension
import com.jovial.blog.model.*
import com.jovial.google.YouTube
import com.jovial.notifications.Ifttt
import com.jovial.notifications.Mailchimp
import com.jovial.os.OSFiles
import com.jovial.os.OSResources
import com.jovial.os.Stdout
import com.jovial.templates.*
import java.io.*

/**
 * Class to generate the site.
 *
 * Created by billf on 11/7/16.
 */
class Site (
        val inputDir: File,
        val outputDir: File,
        val blogConfig : BlogConfig,
        val publishYT : Boolean       /** Publish with youtube links - see remote_hack command-line option */
){
    val txtmarkConfig: com.github.rjeschke.txtmark.Configuration<Content> by lazy {
        com.github.rjeschke.txtmark.Configuration.Builder<Content>().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            build()
    }

    val txtmarkPostConfig: com.github.rjeschke.txtmark.Configuration<PostContent> by lazy {
        com.github.rjeschke.txtmark.Configuration.Builder<PostContent>().
            enableSafeMode().               // Escapes unsafe XML tags
            forceExtentedProfile().         // Include txtmark extensions.  Note misspelling :-)
            setEncoding("UTF-8").
            addExtension(GalleryExtension(this)).
            addExtension(VideoExtension(this)).
            build()
    }

    val postsSrcDir = File(inputDir, "posts")

    val dependencies = DependencyManager(blogConfig.dbDir, "dependencies.json")

    val youtubeManager : YouTube?

    val mailchimpManager : Mailchimp?

    val iftttManager : Ifttt?

    private val errors = mutableListOf<String>()
    private val notes = mutableListOf<String>()

    init {
        blogConfig.dbDir.mkdirs()
        val googleClientConfig = blogConfig.googleClient
        if (googleClientConfig == null) {
            youtubeManager = null
        } else {
            youtubeManager = YouTube(blogConfig.dbDir, googleClientConfig, blogConfig.remote_upload,
                                     blogConfig.googleOauthBrowser)
        }

        val mailchimpClientConfig = blogConfig.mailchimpClient
        if (mailchimpClientConfig == null) {
            mailchimpManager = null
        } else {
            mailchimpManager = Mailchimp(blogConfig.dbDir, mailchimpClientConfig, blogConfig.mailchimpOauthBrowser)
        }

        val iftttClientConfig = blogConfig.iftttClient
        if (iftttClientConfig == null) {
            iftttManager = null
        } else {
            iftttManager = Ifttt(blogConfig.dbDir, iftttClientConfig)
        }
    }

    /**
     * We keep a record of all the generated we have, to avoid re-generating them
     */
    val allPictures = mutableMapOf<File, GalleryPicture>()

    val posts = mutableListOf<Post>()

    private val tagMap = mutableMapOf<String, MutableList<Post>>()
    // Map from tag name to list of posts, in date order starting with the earliest post


    public fun error(message: String) {
        Stdout.println(message)
        errors.add(message)
    }

    public fun note(message: String) {
        Stdout.println(message)
        notes.add(message)
    }

    public fun printNotes() : Unit {
        if (!notes.isEmpty()) {
            Stdout.println()
            Stdout.println("Notes about site generation:")
            for (e in notes) {
                Stdout.println("    $e")
            }
        }
    }

    public fun hasErrors() : Boolean = !errors.isEmpty()

    public fun printErrors() : Unit {
        Stdout.println("Site errors:")
        for (e in errors) {
            Stdout.println("    $e")
        }
    }

    fun generate() {
        dependencies.read()

        // Copy the static files that come with corpsblog
        copyCorpsblogAssets()

	// Copy the contents of the assets directory to the output.
	// These are for fixed images that go with the blog, the CNAME
        // file for github, etc.
        copy(File(inputDir, "assets"), outputDir)

        // Process the posts directory
        val dirContents = postsSrcDir.list()
        if (dirContents == null) {
            note("$postsSrcDir does not exist.")
        } else {
            val postFiles = postsSrcDir.list().
                    sortedBy { s -> s.toLowerCase() }.
                    filter {
                        if (!it.toLowerCase().endsWith(".md")) {
                            if (!File(postsSrcDir, it).isDirectory()) {
                                Stdout.println("""Skipping file $it in $postsSrcDir:  File name doesn't end in ".md".""")
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
        iftttManager?.checkNotifications(this)

        checkForStrayOutputFiles(outputDir)
    }

    private fun copy(input : File, output : File) {
        if (!input.exists()) {
            note("Asset $input does not exist")
        } else if (input.isDirectory) {
            if (!output.isDirectory && !output.mkdirs()) {
                error("Unable to make directory $output")
                System.exit(1)
            }
            for (s in input.list()) {
                copy(File(input, s), File(output, s))
            }
        } else {
            val dep = dependencies.get(output)
            if (dep.changed(listOf(input))) {
                OSFiles.copyReplace(input, output)
                Stdout.println("Copying $input")
            }
        }
    }

    /**
     * Copy the built-in corpsblog assets
     */
    private fun copyCorpsblogAssets() {
        val input = BufferedReader(InputStreamReader(
                        OSResources.getResourceAsStream("/src/resource_list.txt"), "UTF8"))
        val buffer = ByteArray(65536)
        while (true) {
            val line = input.readLine()
            if (line == null) {
                break
            } else if (line.startsWith("src/") || line.startsWith("./src/")) {
                continue
            }
            val modifiedTime = input.readLine()
            if (modifiedTime == null) {
                throw IOException("Malformed resource_list.txt")
            }
            val modifiedTimeMS = modifiedTime.toLong()      // Throw NumberFormatException if malformed
            val outputFile = File(outputDir, line)
            val dependsOn = dependencies.get(outputFile)
            if (dependsOn.changed(listOf<File>(), listOf(modifiedTime))) {
                Stdout.println("Copying " + line)
                outputFile.parentFile.mkdirs()
                val resOut = FileOutputStream(outputFile)
                val resIn = OSResources.getResourceAsStream("/" + line)
                while (true) {
                    val read = resIn.read(buffer)
                    if (read == -1) {
                        break
                    }
                    resOut.write(buffer, 0, read)
                }
                resIn.close()
                resOut.close()
                OSFiles.setLastModifiedTimeMS(outputFile, modifiedTimeMS)
            }
        }
        input.close()
    }

    /**
     * Generate a post
     *
     * @param pathTo    Relative path from the base directory to the posts directory
     * @param pathFrom  Relative path back to the root (like "../")
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
        val content = PostContent(txtmarkPostConfig, this, postOutputDir, baseName, pathFrom, dependencyFiles)
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
        if (content.thumbnail != null) {
            dependencyFiles.add(content.thumbnail!!.source)
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
        Stdout.println("Wrote to file ${outFile.absolutePath}")
    }

    fun deleteStrayFiles() {
        val done = checkForStrayOutputFiles(outputDir, delete=true)
        deleteEmptyDirectories(outputDir)
        if (done) {
            dependencies.write()
        } else {
            Stdout.println("No stray files to remove.")
        }
    }

    private fun deleteEmptyDirectories(dir: File)  {
        for (f in dir.listFiles()) {
            if (f.name.equals(".git")) {
                continue
            }
            if (f.isDirectory) {
                deleteEmptyDirectories(f)
            }
        }
        for (f in dir.listFiles()) {
            return
        }
        if (dir.delete()) {
            Stdout.println("Removed empty directory $dir")
        } else {
            Stdout.println("Unable to remove empty directory $dir")
        }
    }

    private fun checkForStrayOutputFiles(dir : File, foundInput: Boolean = false, delete: Boolean = false) : Boolean {
        var found = foundInput
        for (f in dir.listFiles()) {
            if (f.name.equals(".git")) {
                continue
            }
            if (f.isDirectory) {
                if (checkForStrayOutputFiles(f, found, delete)) {
                    found = true
                }
            } else if (dependencies.check(f)?.checkedThisTime != true) {
                if (!found) {
                    found = true
                    if (!delete) {
                        Stdout.println("Warning -- Stray files found in output directory:")
                    }
                }
                if (delete) {
                    if (f.delete()) {
                        System.out.println("Removed $f")
                        dependencies.remove(f)
                    } else {
                        System.out.println("Unable to remove $f")
                    }
                } else {
                    Stdout.println("    $f")
                }
            }
        }
        return found
    }
}
