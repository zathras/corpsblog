/**
 * Simple HTTP server, using code swiped from hat.
 * <p>
 *
 * Starts an http server, and wait for a query from an authorization
 * server.  Shut down once we've gotten one.
 */

package com.jovial.server

import java.net.InetAddress
import java.security.SecureRandom

import java.io.*
import java.net.NetworkInterface
import java.net.Inet4Address;
import java.util.*

class SimpleHttp(port: Int) : QueryListener(port, false) {
    override fun getHandler(query: String, rawOut: OutputStream, out: PrintWriter): QueryHandler? {
        sendResult(query)
        return SuccessQueryHandler(query, rawOut, out)
    }

    override fun handlePost(headers: HashMap<String, String>, input: BufferedInputStream) {
    }
}

