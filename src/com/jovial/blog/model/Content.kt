package com.jovial.blog.model

import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import java.io.*

/**
 * Created by billf on 11/14/16.
 */
abstract class Content(
        val txtmarkConfig: Configuration
) {
    var body : String = ""      /** Body of the content, in HTML */
        protected set

    var readWasCalled : Boolean = false
        private set

    class ParseError(message: String) : Exception(message)

    /**
     * Read the input
     */
    @Throws(ParseError::class)
    open fun read(location: File) : Unit {
        readWasCalled = true
        val input = BufferedReader(InputStreamReader(FileInputStream(location), "UTF-8"))
        var lineNumber = 0;
        while (true) {
            lineNumber++
            val line = input.readLine()
            if (line == null) {
                throw ParseError("Unexpected EOF in header of $location")
            } else if (line.startsWith("~~~~~")) {
                break
            }
            val i = line.indexOf('=')
            if (i < 0) {
                throw ParseError("""Missing "=" in metadata block """
                        + "at line $lineNumber of $location")
            }
            val key = line.substring(0, i)
            val value = line.substring(i + 1)
            try {
                if (!processHeader(key, value)) {
                    throw ParseError("""Unrecognized key "$key" at line $lineNumber of $location""")
                }
            } catch (ex : Exception) {
                throw ParseError("""Error at line $lineNumber of $location:  "$ex"""")
            }
        }
        readBody(input)
        input.close()
    }

    protected abstract fun processHeader(key: String, value: String) :  Boolean

    protected open fun readBody(input: Reader) {
        body = Processor<Content>(input, txtmarkConfig).process(this)
    }
}