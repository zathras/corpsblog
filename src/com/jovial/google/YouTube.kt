package com.jovial.google

import com.jovial.google.remote_hack.RemoteUpload
import com.jovial.util.JsonIO
import com.jovial.util.httpPostJSON
import com.jovial.util.processFileName
import java.io.*
import java.net.URL
import java.util.*

/**
 * Upload a video to YouTube.
 *
 * cf. https://developers.google.com/youtube/v3/guides/using_resumable_upload_protocol
 * cf. https://developers.google.com/youtube/v3/guides/uploading_a_video
 *
 * Created by billf on 12/10/16.
 */
class YouTube(val remoteCommand: String?, val oAuth : OAuth, val dbDir: File) {


    private val uploadsFile = File(dbDir, "youtube_uploads.json")
    //
    // Map from absolute path of source video file to Youtube URL
    //
    private val videoUploads = mutableMapOf<File, URL>()

    init {
        if (uploadsFile.exists()) {
            val input = BufferedReader(InputStreamReader(FileInputStream(uploadsFile), "UTF-8"))
            val m = JsonIO.readJSON(input) as HashMap<Any, Any>
            for ((k, v) in m) {
                videoUploads[File(k as String)] = URL(v as String)
            }
            input.close()
        } else {
            writeUploadsFile()      // To make sure we can
        }
    }

    private fun writeUploadsFile() {
        val json = HashMap<Any, Any>()
        for ((k, v) in videoUploads) {
            json[k.absolutePath] = v.toString()
        }
        val output = BufferedWriter(OutputStreamWriter(FileOutputStream(uploadsFile), "UTF8"))
        JsonIO.writeJSON(output, json)
        output.close()
    }

    /**
     * Get the video upload record for the given asset (which should be a video).
     * Return null if it hasn't been uploaded
     */
    fun getVideoURL(asset: File) : URL?  = videoUploads[asset.absoluteFile]

    fun uploadVideo(videoFile : File, description: String) : URL {
        val token = oAuth.getToken()

        val snippet = HashMap<String, Any>()
        snippet["title"] = "Test Post from CorpsBlog"
        snippet["description"] = description
        snippet["tags"] = listOf("CorpsBlog", "Test")
        snippet["categoryId"] = 22

        val status = HashMap<String, Any>()
        status["privacyStatus"] = "private"
        status["embeddable"] = true
        status["license"] = "youtube"

        val videoResource = mapOf<String, Any> (
                "snippet" to mapOf<String, Any> (
                        "title" to "Test Post from CorpsBlog",
                        "description" to description,
                        "tags" to listOf("CorpsBlog", "Test"),
                        "categoryId" to 22
                ),
                "status" to mapOf<String, Any> (
                        "privacyStatus" to "private",
                        "embeddable" to true,
                        "license" to "youtube"
                )
        )
        val authorization = "${token.token_type} ${token.access_token}"
        val headers = mapOf(
                "Authorization" to "${token.token_type} ${token.access_token}",
                "X-Upload-Content-Length" to videoFile.length().toString(),
                "X-Upload-Content-Type" to "video/*"
        )
        val server = URL("https://www.googleapis.com/upload/youtube/v3/videos"
                         + "?uploadType=resumable"
                         + "&part=snippet,status,contentDetails")
        val reply = httpPostJSON(server, videoResource, headers)
        while (reply.input.read() != -1) { }
        val uploadURL = URL(reply.connection.getHeaderField("Location"))
        // @@ val uploadURL = URL("https://www.googleapis.com/upload/youtube/v3/videos?uploadType=resumable&part=snippet,status,contentDetails&upload_id=AEnB2Uq5Pq6uCU9fRT44OqIHqEuitnWvvOqAuuGTmL9V6VwM9Ysit-LRs3NnYHahnpUDQyBObrh2PW-XUGehGrS9BgiP8IWnJE7VTtuadXETdqpwGebyNXU")
        println("@@ uploading to $uploadURL")
        val u = ResumableUpload(
                authorization=authorization,
                src=videoFile.toURI().toURL(),
                size=videoFile.length(),
                contentType="video/*",
                dest=uploadURL)
        val youtubeURL = if (remoteCommand == null) {
            u.upload()
        } else {
            RemoteUpload(processFileName(remoteCommand), u).upload()
        }
        videoUploads[videoFile.absoluteFile] = youtubeURL
        writeUploadsFile()
        return youtubeURL
    }
}