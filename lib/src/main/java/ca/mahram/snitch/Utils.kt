package ca.mahram.snitch

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.ContinuationInterceptor

fun CoroutineScope.dispatcherName(): String {
    return when (coroutineContext[ContinuationInterceptor]) {
        Dispatchers.Main -> "Main"
        Dispatchers.Default -> "Default"
        Dispatchers.IO -> "IO"
        Dispatchers.Unconfined -> "Unconfined"
        else -> "Unknown"
    }
}

fun now() = SystemClock.elapsedRealtime()

val Any.testId:String get() = "${this::class.java.simpleName}@${mapKey}"

val Any.mapKey:Int get() = System.identityHashCode(this)
