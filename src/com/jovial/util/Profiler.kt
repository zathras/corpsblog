package com.jovial.util

/**
 * A cheezy little execution time profiler, using System.nanoTime
 */
class Profiler(val name : String) {

    var calls : Int = 0;
    var cumulativeTime : Long = 0;

    inline fun<T> run(block : () -> T) : T {
        calls++;
        val start : Long = System.nanoTime();
        val res = block();
        cumulativeTime += System.nanoTime() - start;
        return res;
    }

    fun print() {
        println("$name ${calls} calls\t${cumulativeTime / 1000000.0} ms");
    }

}
