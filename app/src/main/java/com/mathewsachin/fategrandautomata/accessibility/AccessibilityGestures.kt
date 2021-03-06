package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.mathewsachin.fategrandautomata.scripts.prefs.IGesturesPreferences
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.extensions.IDurationExtensions
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

/**
 * Class to perform gestures using Android's [AccessibilityService].
 */
class AccessibilityGestures @Inject constructor(
    private var AccessibilityService: AccessibilityService?,
    val gesturePrefs: IGesturesPreferences,
    durationExtensions: IDurationExtensions
) : IGestureService, IDurationExtensions by durationExtensions {
    override fun swipe(Start: Location, End: Location) {
        val swipePath = Path()
        swipePath.moveTo(Start.X.toFloat(), Start.Y.toFloat())
        swipePath.lineTo(End.X.toFloat(), End.Y.toFloat())

        logger.debug { "swipe $Start, $End" }

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            gesturePrefs.swipeDuration.toLongMilliseconds()
        )
        performGesture(swipeStroke)

        gesturePrefs.swipeWaitTime.wait()
    }

    override fun click(Location: Location, Times: Int) {
        val swipePath = Path()
        swipePath.moveTo(Location.X.toFloat(), Location.Y.toFloat())

        val stroke = GestureDescription.StrokeDescription(
            swipePath,
            gesturePrefs.clickDelay.toLongMilliseconds(),
            gesturePrefs.clickDuration.toLongMilliseconds()
        )

        logger.debug { "click $Location x$Times" }

        repeat(Times) {
            performGesture(stroke)
        }

        gesturePrefs.clickWaitTime.wait()
    }

    private fun performGesture(StrokeDesc: GestureDescription.StrokeDescription) {
        val acc = AccessibilityService ?: return

        val gestureDesc = GestureDescription.Builder()
            .addStroke(StrokeDesc)
            .build()

        val callback = GestureCompletedCallback()

        acc.dispatchGesture(gestureDesc, callback, null)

        callback.waitTillFinish()
    }

    override fun close() {
        AccessibilityService = null
    }
}