package com.jovial.os

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A wrapper around stdout, so that on Android we can easily re-direct it
 * to a widget.  Of course, it's possible to re-map stdout in Java,
 * but this is simpler, and it leaves open the possibility of using
 * Android's stdout for debugging.
 */
object Stdout {

    private val lock : Lock = ReentrantLock()
    private val printlnLock : Lock = ReentrantLock()
    private val listeners = mutableListOf<(String) -> Unit>()
    private var listenersCopy : List<(String) -> Unit>? = null
    private val logBuffer = StringBuffer()


    /**
     * Returns the contents of stdout so far, so that a text
     * field can be initialized.
     */
    fun addListener(l: (String) -> Unit) : String {
        lock.withLock {
            listeners.add(l)
            listenersCopy = null
            return logBuffer.toString()
        }
    }

    fun removeListener(l: (String) -> Unit) {
        lock.withLock {
            listeners.remove(l)
            listenersCopy = null
        }
    }

    fun println(message: Any?) {
        val s = if (message == null)  "null\n" else (message.toString() + '\n')
        Stdout.print(s)
    }

    fun println() {
        Stdout.print("\n")
    }

    fun print(message: Any?) {
        val s = if (message == null) "null" else message.toString()
        printlnLock.withLock {
            val toNotify = lock.withLock {
                if (listenersCopy == null) {
                    listenersCopy = listeners.toList()
                }
                logBuffer.append(s)
                listenersCopy!!
            }
            for (recipient in toNotify) {
                recipient(s)
            }
        }
    }
}