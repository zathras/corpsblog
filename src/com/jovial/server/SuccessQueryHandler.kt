package com.jovial.server

import java.io.OutputStream
import java.io.PrintWriter

import java.net.URLEncoder
import java.io.UnsupportedEncodingException

/**

 * @author      Bill Foote
 */


class SuccessQueryHandler(private val message: String, rawOut: OutputStream, out: PrintWriter) : QueryHandler(rawOut, out) {

    override fun run() {
        startHttpResult(200)
        startHtml("success")
        safePrint("Thank you.")
        out.print("<br><br>")
        safePrint("""Got "$message"""")
        endHtml()
    }

}
