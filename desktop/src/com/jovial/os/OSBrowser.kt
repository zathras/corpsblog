package com.jovial.os

import java.io.IOException
import java.net.URL

object OSBrowser {

    fun launch(browser: String, url: URL) {
        val pb = ProcessBuilder(browser, url.toString())
        try {
            pb.start()
        } catch (ex : IOException) {
            Stdout.println("Unable to start $browser")
            Stdout.println("Please start manually.")
            Stdout.println()
            Stdout.println(ex)
            Stdout.println()
            Stdout.println("URL:  $url")
            Stdout.println()
        }
    }
}