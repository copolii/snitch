package ca.mahram.snitch

import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener

fun View.addOneTimePreDrawListener(marker: () -> Unit): PreDrawListener {
    val listener = PreDrawListener(this, marker)
    viewTreeObserver.addOnPreDrawListener(listener)
    return listener
}

fun View.removeOneTimePreDrawListener(listener: OnPreDrawListener):Boolean {
    if (!viewTreeObserver.isAlive) return false

    viewTreeObserver.removeOnPreDrawListener(listener)
    return true
}

class PreDrawListener internal constructor (private val view: View, private val marker: () -> Unit) : OnPreDrawListener {
    override fun onPreDraw(): Boolean {
        view.removeOneTimePreDrawListener(this)
        marker()
        return true
    }

    fun remove() = view.removeOneTimePreDrawListener(this)
}
