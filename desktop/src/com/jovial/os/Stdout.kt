package com.jovial.os

/**
 * A wrapper around stdout, so that on Android we can easily re-direct it
 * to a widget.  Of course, it's possible to re-map stdout in Java,
 * but this is simpler, and it leaves open the possibility of using
 * Android's stdout for debugging.
 */
object Stdout {
    fun println(message: Any?) {
        kotlin.io.println(message)
    }

    fun print(message: Any?) {
        kotlin.io.print(message)
    }

    fun println() {
        kotlin.io.println()
    }
}