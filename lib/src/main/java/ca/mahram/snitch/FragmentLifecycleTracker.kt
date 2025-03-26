package ca.mahram.snitch

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun AppCompatActivity.trackFragmentLifecycle(): FragmentLifecycleTracker {
    val tracker = FragmentLifecycleTracker(this::class.simpleName!!)
    supportFragmentManager.registerFragmentLifecycleCallbacks(tracker, true)
    return tracker
}

class FragmentLifecycleTracker internal constructor(prefix:String): FragmentManager.FragmentLifecycleCallbacks() {

    private val fragger = EventMarker("$prefix:FRAGMENTS")
    private val preDrawListenerMap = mutableMapOf<Int, Pair<PreDrawListener, ViewTreeObserver>>()

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        fragger.mark("PreAttached: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        fragger.mark("Attached: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        fragger.mark("Created: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        fragger.mark("ViewCreated: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        fragger.mark("Started: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        fragger.mark("Resumed: ${f::class.simpleName} @ ${System.identityHashCode(f)}. Visible? ${f.isVisible}")

        if (!f.isVisible) return

        val view = f.view ?: return

        val pdl = PreDrawListener(view) {
            fragger.mark("preDraw: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
        }

        val vto = view.viewTreeObserver!!

        preDrawListenerMap[f.mapKey] = pdl to vto
        vto.addOnPreDrawListener(pdl)

        view.post {
            fragger.mark("Post: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
        }
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        fragger.mark("Paused: ${f::class.simpleName} @ ${System.identityHashCode(f)}. Visible? ${f.isVisible}")

        removePreDrawListener(f)
    }

    private fun removePreDrawListener(f: Fragment) {
        preDrawListenerMap.remove(f.mapKey)?.let { (pdl, vto) ->
            vto.removeOnPreDrawListener(pdl)
        }
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        fragger.mark("Stopped: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        fragger.mark("PreCreated: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        fragger.mark("Detached: ${f::class.simpleName} @ ${System.identityHashCode(f)}")

        removePreDrawListener(f)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        fragger.mark("Destroyed: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        fragger.mark("ViewDestroyed: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        fragger.mark("SaveInstanceState: ${f::class.simpleName} @ ${System.identityHashCode(f)}")
    }
}