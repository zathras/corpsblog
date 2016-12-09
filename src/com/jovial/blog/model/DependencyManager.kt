package com.jovial.blog.model

import com.jovial.util.JsonIO
import java.io.*
import java.util.*

/**
 * A dependency manaager records the assets that each of the generated products depends on.  This is done so that
 * we can avoid re-generating files needlessly.  Images take a long time to re-generate.  We also avoid
 * re-generating HTML files if there are no changes, so that the timestamp of the HTML file reflects when
 * the current version was first generated.
 *
 * Created by billf on 11/18/16.
 */
class DependencyManager (val inputDir : File, val dependencyFile : String) {

    private var generatedAssets = mutableMapOf<File, AssetDependencies>()

    private var videoUploads = mutableMapOf<File, VideoUpload>()

    /**
     * Get the object that describes the things the provided generated asset depends on.
     */
    fun get(asset: File) : AssetDependencies {
        val canon = asset.canonicalFile
        val result = generatedAssets.get(canon)
        if (result != null) {
            return result
        } else {
            val v = AssetDependencies(asset)
            generatedAssets.put(canon, v)
            return v
        }
    }

    /**
     * Check to see if there is a dependencies object for the given asset, but if there isn't,
     * don't create one.
     */
    fun check(asset: File) : AssetDependencies? {
        return generatedAssets.get(asset.canonicalFile)
    }

    /**
     * Get the video upload record for the given asset (which should be a video)
     */
    fun getVideo(asset: File) : VideoUpload {
        val canon = asset.canonicalFile
        val result = videoUploads.get(canon)
        if (result != null) {
            return result
        } else {
            val v = VideoUpload(asset)
            videoUploads.put(canon, v)
            return v
        }
    }

    /**
     * Read our state from dependencyFile, if it exists.  If not, do nothing.
     */
    fun read() {
        assert(generatedAssets.size == 0)
        val f = File(inputDir, dependencyFile)
        if (f.exists()) {
            val input = BufferedReader(InputStreamReader(FileInputStream(f), "UTF8"))
            val json = JsonIO.readJSON(input) as HashMap<String, Any>
            input.close()
            if (json["version"] != "1.0") {
                throw IOException("Version mismatch:  got ${json["version"]}")
            }
            val readAssets = json["generatedAssets"] as List<HashMap<String, Any>>
            for (readAsset in readAssets) {
                val a = AssetDependencies(readAsset)
                generatedAssets[a.generatedAsset.canonicalFile] = a
            }
            val readVideos = json["videos"] as List<HashMap<String, Any>>
            for (readVideo in readVideos) {
                val v = VideoUpload(readVideo)
                videoUploads[v.videoFile.canonicalFile] = v
            }
        }
        get(f)      // Just so we don't count as a stray file in the output dir
    }

    /**
     * Write our state out to dependencyFile, for a permanent record.
     */
    fun write() {
        val json = HashMap<Any, Any>()
        json["version"] = "1.0"
        json["generatedAssets"] = generatedAssets.values.map { it.asJsonValue() }
        json["videos"] = videoUploads.values.map { it.asJsonValue() }
        val output = BufferedWriter(OutputStreamWriter(FileOutputStream(File(inputDir, dependencyFile)), "UTF8"))
        JsonIO.writeJSON(output, json)
        output.close()
    }
}