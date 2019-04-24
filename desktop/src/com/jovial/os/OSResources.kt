package com.jovial.os

import java.io.IOException
import java.io.InputStream

object OSResources {

    fun getResourceAsStream(name: String) : InputStream =
        javaClass.getResourceAsStream(name) ?:
            (throw IOException("resource $name not found"))
}