package com.jovial.google

import com.jovial.google.remote_hack.RemoteUpload
import com.jovial.util.JsonIO
import com.jovial.util.httpPostJSON
import com.jovial.util.processFileName
import java.io.File
import java.io.StringWriter
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
class YouTube(val remoteCommand: String?, val oAuth : OAuth) {

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
                // @@ src=videoFile.toURI().toURL(),
                src=URL("http://moomtastic.jovial.com/movies/2006_09_messengers_h264.mp4"),
                size=videoFile.length(),
                contentType="video/*",
                dest=uploadURL)
        if (remoteCommand == null) {
            return u.upload()
        } else {
            return RemoteUpload(processFileName(remoteCommand), u).upload()
        }
    }
}