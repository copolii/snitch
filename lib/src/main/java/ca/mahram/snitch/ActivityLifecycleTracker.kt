package ca.mahram.snitch

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

fun Application.injectMarkerHooks () {
    registerActivityLifecycleCallbacks(ActivityLifecycleTracker())
}

private const val TAG = "ACTIVITIES"

class ActivityLifecycleTracker:ActivityLifecycleCallbacks {

    private val log = EventMarker(TAG)

    private val preDrawListenerMap = mutableMapOf<Int, PreDrawListener>()
    private val fragTrack = mutableMapOf<Int, FragmentLifecycleTracker>()

    override fun onActivityCreated(activity: Activity, extras: Bundle?) {
        log.mark("Activity Created: ${activity.testId}")

        (activity as? AppCompatActivity)?.let {
            fragTrack[activity.mapKey] = it.trackFragmentLifecycle()
        } ?: run {
            Log.e(TAG, "Activity ${activity.testId} is not AppCompatActivity")
        }
    }

    override fun onActivityStarted(activity: Activity) {
        log.mark("Activity Started: ${activity.testId}")
    }

    override fun onActivityResumed(activity: Activity) {
        val testId = activity.testId
        log.mark("Activity Resumed: $testId")

        activity.findViewById<View>(android.R.id.content)?.let {
            preDrawListenerMap[activity.mapKey] = it.addOneTimePreDrawListener { log.mark("Activity PreDraw: $testId") }
            it.post { log.mark("Activity Post: $testId") }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        log.mark("Activity Paused: ${activity.testId}")

        preDrawListenerMap.remove(activity.mapKey)?.remove()
    }

    override fun onActivityStopped(activity: Activity) {
        log.mark("Activity Stopped: ${activity.testId}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, extras: Bundle) {
        log.mark("Activity SaveInstanceState: ${activity.testId}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        log.mark("Activity Destroyed: ${activity.testId}")

        val tracker = fragTrack.remove(activity.mapKey) ?: run {
            Log.w(TAG, "No store Fragment lifecycle tracker for Activity ${activity.testId}.")
            return
        }

        (activity as? AppCompatActivity)?.supportFragmentManager?.unregisterFragmentLifecycleCallbacks(tracker)?: run {
            Log.w(TAG, "Activity ${activity.testId} is not AppCompatActivity")
        }
    }
}
