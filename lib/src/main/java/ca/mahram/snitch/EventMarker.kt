package ca.mahram.snitch

import android.util.Log

class EventMarker(private val tag: String) {
    private val start = now()

    @field:Volatile
    private var last = start

    init {
        mark("MarkerInit")
    }

    @Synchronized
    fun mark(event: String) {
        val now = now()
        Log.v(tag, "MARK [$event]: ${now - start} (${now - last}ms)")
        last = now
    }
}