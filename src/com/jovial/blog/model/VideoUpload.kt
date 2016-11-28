package com.jovial.blog.model

import java.io.File
import java.net.URL
import java.util.*

/**
 * Created by billf on 11/27/16.
 */

class VideoUpload {

    val videoFile : File
    var uploadedAddress : URL? = null

    public constructor(videoFile : File) {
	this.videoFile = videoFile
    }

    constructor (jsonRecord: HashMap<String, Any>) {
        this.videoFile = File(jsonRecord["asset"] as String)
	val a = jsonRecord["address"]
	if (a != null) {
	    this.uploadedAddress = URL(a as String)
	} else {
	    this.uploadedAddress = null
	}
    }

    fun asJsonValue() : HashMap<Any, Any> {
        val result = HashMap<Any, Any>()
        result["asset"] = videoFile.absolutePath!!
	if (uploadedAddress != null) {
	    result["address"] = uploadedAddress.toString()
	}
        return result
    }
}
