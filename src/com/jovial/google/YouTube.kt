package com.jovial.google

import com.jovial.google.remote_hack.RemoteUpload
import com.jovial.webapi.OAuth
import com.jovial.util.JsonIO
import com.jovial.util.httpPostJSON
import com.jovial.util.processFileName
import com.jovial.util.urlEncode
import com.jovial.webapi.WebService
import java.io.*
import java.net.URL
import java.util.*

/**
 * Upload a video to YouTube.
 *
 * cf. https://developers.google.com/youtube/v3/guides/using_resumable_upload_protocol
 * cf. https://developers.google.com/youtube/v3/guides/uploading_a_video
 *
 * @param remoteCommand     An optional system command to remotely upload a URL.
 *
 * Created by billf on 12/10/16.
 */
class YouTube(val dbDir: File, val config : GoogleClientConfig, val remoteCommand: String?, val browser : String)
    : WebService()
{

    val oAuth : OAuth

    protected override val dbFile = File(dbDir, "youtube_uploads.json")
    //
    // Map from absolute path of source video file to Youtube URL
    //
    private val videoUploads = mutableMapOf<File, URL>()

    init {
        val authParams =
                "&scope=" + urlEncode("https://www.googleapis.com/auth/youtube") +
                        "&access_type=offline"
        oAuth = OAuth(authURL = config.auth_uri,
                clientId = config.client_id,
                clientSecret = config.client_secret,
                tokenFile = File(dbDir, "google_oauth.json"),
                authParams = authParams,
                tokenURL = config.token_uri,
                browser = browser,
                localhostName = "localhost")

        val m = readDbFile()
        if (m != null) {
            for ((k, v) in (m as Map<Any, Any>)) {
                videoUploads[File(k as String)] = URL(v as String)
            }
        }
        writeUploadsFile()      // To make sure we can
    }

    private fun writeUploadsFile() {
        val json = mutableMapOf<Any, Any>()
        for ((k, v) in videoUploads) {
            json[k.absolutePath] = v.toString()
        }
        writeDbFile(json)
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
        val u = ResumableUpload(
                authorization=authorization,
                src=videoFile.toURI().toURL(),
                size=videoFile.length(),
                contentType="video/*",
                dest=uploadURL)
        val youtubeURL = if (remoteCommand == null) {
            u.upload()
        } else {
            // @@ Need to git push the video file, and give the appropriate URL
            // @@ That can be done in corpsblog_remote_upload
            // @@
            // @@  Easier if we put all videos in one directory...  Then "git add ." will suffice
            RemoteUpload(processFileName(remoteCommand), u).upload()
        }
        videoUploads[videoFile.absoluteFile] = youtubeURL
        writeUploadsFile()
        return youtubeURL
    }
}