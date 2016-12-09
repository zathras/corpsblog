package com.jovial.blog

import com.jovial.util.GitManager

/**
 * Represents a pass over the blog contents.  During a pass, we can accumulate actions that need to
 * be performed before the site can be considered to be completely built.  For example, videos are
 * uploaded to YouTube by a task; until that is done, the youtube URL that's needed for the video link
 * isn't available, so the final version of the blog page can't be generated.
 *
 * Created by billf on 11/27/16.
 */
class Pass (val site: Site){

    /**
     * Tasks that must be run before the site is finished.  Tasks that fail should throw an
     * exception.
     */
    private val pendingTasks = mutableListOf<() -> Unit>()

    val siteFinished : Boolean
        get() {
            return (!site.publish) || pendingTasks.isEmpty()
        }

    /**
     * Add a task that must run before the site is finished.  Tasks that fail should throw
     * an exception.
     */
    fun addTask(t : () -> Unit) : Unit {
        pendingTasks.add(t)
    }

    fun runTasks() {
        if (site.publish) {
            GitManager.upload(site.outputDir)
            for (t in pendingTasks) {
                t()
            }
        }
    }

}