# Snitch
[![](https://jitpack.io/v/copolii/snitch.svg)](https://jitpack.io/#copolii/snitch)

Snitches on Activity & Fragment lifecycles with a single line of code.

## Usage
For the most basic monitoring of Activity lifecycle events, fragment lifecycle events and transactions, post and predraw snitching, simply add the following to your `Application`'s `onCreate` function:

```kotlin
package fancypackage

import ca.mahram.snitch.injectMarkerHooks

class SomeFancyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        injectMarkerHooks()
        // blah blah blah
    }
}
```

## How it works
The `injectMarkerHooks` `Application` extension function registers an `ActivityLifecycleCallbacks` instance with the application instance. For every activity, it logs the lifecycle events. When an activity is created, a `FragmentManager.FragmentLifecycleCallbacks` is registered with the activity's `supportFragmentManager` to track the fragment transactions and lifecycle events. When an activity is destroyed, the `FragmentLifecycleCallbacks` is unregistered. The view readiness is measured for each activity and fragment by posting a runnable to the root view as well as adding an `OnPreDrawListener` to the root view's `viewTreeObserver`. For activities the pre-draw listener is added in `onResume` and removed in `onPause` and for fragments in `onStart` and `onStop`. 
Each log marker contains an absolute (curren time - marker instantiation time) and a relative (current time - last event time) timestamp. These timestamps allow you to track time spent in each lifecycle stage.

### Default Markers
#### Activity Lifecycle Events
The activity lifecycle events are logged with tag `ACTIVITIES`. Example: `ACTIVITIES(11600): MARK [Activity Created: FancyActivity@x] 1340 (1340ms)` where `x` is `System.identityHashCode(activity)`. Including the _identity hash code_ facilitates duplicate instance detection (e.g. when 2 separate instances of the same activity appear in the log). The example log indicates that the `FancyActivity` was created 1340ms after the marker initialization, which was also the previous event logged.
#### Fragment Lifecycle Events
The fragment transactions and fragment lifecycle events within activity `FancyActivity` are logged with tag `FancyActivity:FRAGMENTS`. Example: `FancyActivity:FRAGMENTS: MARK [Attached: FancyFragment@x] 80 (8ms)` indicates that the `FancyFragment` was attached to `FancyActivity` 80ms after the activity was created and 8ms after the _preAttach_ event for the same fragment. 

### Additional Event Snitching
To snitch on additional events (e.g. non-lifecycle function calls within your activity) or components (e.g. service init time), create an `EventMarker` instance in the component's constructor. Each call to `mark(event)` logs the event and its timestamps, which can then be compared to the marker init time or other events on the same marker.

## Terminology
The following terms are used to describe a variety of events in the scope of Snitch

| **Term**                                                                  | **Description**                                                                                                                                                                                                                                                                             |
|---------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tap-to-post`                                                             | Time from when the user taps the app icon to when the last `Post:` event occurs. This is a good indication of how much time it took from when the intent to launch the app was fired to when the main activity's view was laid out.                                                         |
| `wasted time`                                                             | Time spent on work that could be deferred, skipped, or parallelized. Often seen in lifecycle delays before first UI draw. In other words, total time minus time spent in known lifecycle events. This isn't an absolute measure, but a relative measure of how one run compares to another. |
| `Baseline`                                                                | The average of _n_ reference (control) runs (used for comparison).                                                                                                                                                                                                                          |
| `preDraw`                                                                 | The point in the lifecycle just before a frame is drawn. Used to assess visual readiness.                                                                                                                                                                                                   |
| `post`                                                                    | A marker logged after the first draw to indicate completion of primary setup/UI.                                                                                                                                                                                                            |
| `MarkerInit`                                                              | A standardized timestamp used to normalize logs and calculate relative timings. For each marker instance this is the creation timestamp. It is best to instantiate the `EventMarker` instance during the `init` (constructor) of the unit to be measured (e.g. a Service).                  |
| `PreAttached`, `Attached`, `Created`, `ViewCreated`, `Started`, `Resumed` | Fragment lifecycle states observed and marked.                                                                                                                                                                                                                                              |

## Known Issues
### onPreDrawSpam 
Although the pre-draw listener is supposed to remove itself from the view's `viewTreeObserver` after the first callback, for certain fragments, this call fails. As a result the pre-draw listener spams the log once per frame. 
