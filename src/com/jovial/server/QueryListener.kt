package com.jovial.server

/**
 * @author      Bill Foote
 */


import java.net.Socket
import java.net.ServerSocket
import java.net.InetAddress

import java.io.InputStream
import java.io.BufferedInputStream
import java.io.IOException
import java.io.Writer
import java.io.BufferedWriter
import java.io.PrintWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.BufferedOutputStream
import java.net.SocketException
import java.util.HashMap
import javax.net.ssl.SSLServerSocketFactory

abstract class QueryListener(val port: Int, val enableSsl : Boolean) {

    abstract fun getHandler(query: String, rawOut: OutputStream, out: PrintWriter): QueryHandler?

    abstract fun handlePost(headers: HashMap<String, String>, input: BufferedInputStream)

    private var serverSocket : ServerSocket? = null

    var last: Thread? = null

    private var result : String? = null

    fun run() : String {
        waitForRequests()   // Can throw IOException
        last!!.join()
        return result!!
    }

    @Throws(IOException::class)
    private fun waitForRequests() {
        serverSocket = if (enableSsl) {
            SSLServerSocketFactory.getDefault().createServerSocket(port)
        } else {
            ServerSocket(port)
        }
        while (true) {
            try {
                val s = serverSocket!!.accept()
                val t = Thread(HttpReader(s, this))
                t.priority = Thread.NORM_PRIORITY - 1
                if (last != null) {
                    last!!.join()
                }
                t.start()
                last = t
            } catch (ex : SocketException) {
                if (result == null) {
                    throw ex
                }
                return
            }
        }
    }

    fun sendResult(result : String) : Unit {
        this.result = result;
        serverSocket!!.close()
    }

}
