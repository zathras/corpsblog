package com.jovial.blog.model

import com.jovial.util.JsonIO
import java.io.*
import java.util.*

/**
 * A dependency manaager records the assets that each of the generated products depends on.  This is done so that
 * we can avoid re-generating files needlessly.  Images take a long time to re-generate.  We also avoid
 * re-generating HTML files if there are no changes, so that the timestamp of the HTML file reflects when
 * the current version actually needed to be generated.
 *
 * Created by billf on 11/18/16.
 */
class DependencyManager (inputDir : File, dependencyFileName : String) {

    private val dependencyFile = File(inputDir, dependencyFileName)

    private var generatedAssets = mutableMapOf<File, AssetDependencies>()

    /**
     * Get the object that describes the things the provided generated asset depends on.
     */
    fun get(asset: File) : AssetDependencies {
        assert(asset.path == asset.canonicalPath);
        val result = generatedAssets.get(asset)
        if (result != null) {
            return result
        } else {
            val v = AssetDependencies(asset)
            generatedAssets.put(asset, v)
            return v
        }
    }

    /**
     * Check to see if there is a dependencies object for the given asset, but if there isn't,
     * don't create one.
     */
    fun check(asset: File) : AssetDependencies? {
        assert(asset.path == asset.canonicalPath);
        return generatedAssets.get(asset)
    }

    /**
     * Remove the given dependencies object, if it exists
     */
    fun remove(asset: File) {
        generatedAssets.remove(asset)
    }

    /**
     * Read our state from dependencyFile, if it exists.  If not, do nothing.
     */
    fun read() {
        assert(generatedAssets.size == 0)
        if (dependencyFile.exists()) {
            val input = dependencyFile.bufferedReader()

            @Suppress("UNCHECKED_CAST")
            val json = JsonIO.readJSON(input) as HashMap<String, Any>
            input.close()
            if (json["version"] != "1.0") {
                throw IOException("Version mismatch:  got ${json["version"]}")
            }
            @Suppress("UNCHECKED_CAST")
            val readAssets = json["generatedAssets"] as List<HashMap<String, Any>>
            for (readAsset in readAssets) {
                val a = AssetDependencies(readAsset)
                assert(a.generatedAsset.path == a.generatedAsset.canonicalPath);
                generatedAssets[a.generatedAsset] = a
            }
        }
    }

    /**
     * Write our state out to dependencyFile, for a permanent record.
     */
    fun write() {
        val json = HashMap<Any, Any>()
        json["version"] = "1.0"
        json["generatedAssets"] = generatedAssets.values.map { it.asJsonValue() }
        val output = dependencyFile.bufferedWriter()
        JsonIO.writeJSON(output, json)
        output.close()
    }
}
