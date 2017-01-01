package com.jovial.blog.model

import java.io.File
import java.util.*

/**
 * Records the dependencies for a given asset.  This is fairly conservative.  For example, if
 * anything changes about a post, that post and anything that references that post will be
 * re-generated, even if the change has no impact on the referrer.  From a performance standpoint,
 * the only really important thing is to avoid re-generating gallery images all the time.  It's
 * nice to avoid gratituously re-generating all of the HTML files, though, so that the timestamps
 * on them have some meaning, and the sitemap doesn't claim updates to stuff that hasn't really
 * changed.
 *
 * Created by billf on 11/18/16.
 */
class AssetDependencies  {

    val generatedAsset : File
    private var values : List<String>
    var fileTimes : List<Pair<File, Long>>


    public constructor(generatedAsset: File)  {
        this.generatedAsset = generatedAsset
        this.values = listOf<String>()
        this.fileTimes = listOf<Pair<File, Long>>()
    }

    constructor (jsonRecord: HashMap<String, Any>) {
        this.generatedAsset = File(jsonRecord["asset"] as String)
        this.values = listOf<String>()
        val files = jsonRecord["files"]
        if (files == null) {
            this.fileTimes = listOf<Pair<File, Long>>()
        } else {
            @Suppress("UNCHECKED_CAST")
            val fl = files as ArrayList<ArrayList<Any>>
            this.fileTimes = fl.map { Pair(File(it[0] as String), (it[1] as Number).toLong()) }
        }
        val values = jsonRecord["values"]
        if (values == null) {
            this.values = listOf<String>()
        } else {
            @Suppress("UNCHECKED_CAST")
            this.values = values as ArrayList<String>
        }
    }

    /**
     * If anything this asset depends on changed, return true and update this dependencies
     * object.  If the asset doesn't exist, return true.  Otherwise, return false.
     *
     * newFiles can contain source or generated files.  Values should contain any other values
     * the asset depends on, like a file name.
     */
    fun changed(newFiles: List<File>, newValues: List<String> = listOf<String>()) : Boolean {
        val newFileTimes = newFiles.map { Pair(it.canonicalFile, it.lastModified()) }
        if (newValues != values || newFileTimes != fileTimes) {
            values = newValues
            fileTimes = newFileTimes
            return true
        } else {
            return !generatedAsset.exists()
        }
    }

    fun asJsonValue() : HashMap<Any, Any> {
        // OK, I know, this is ugly.  However, it's less work to do this than it would be for me to
        // integrate a fancy, full-blown JSON library that's Kotlin-friendly, like Jackson.  JsonIO
        // was written with old-school Java in mind, obviously, so I have to do these ugly conversions
        // to array lists and such...  But it's easy and fast, works fine, and the result is plenty
        // extensible.
        val result = HashMap<Any, Any>()
        result["asset"] = generatedAsset.absolutePath!!
        if (values.size > 0) {
            result["values"] = values.toTypedArray()
        }
        if (fileTimes.size > 0) {
            result["files"] = (fileTimes.map {
                val v = ArrayList<Any>()
                v.add(it.first.absolutePath!!)
                v.add(it.second)
                v
            }).toTypedArray()
        }
        return result
    }
}