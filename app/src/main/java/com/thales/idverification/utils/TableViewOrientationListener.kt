package com.thales.idverification.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import kotlin.math.abs

class TableViewOrientationListener(val activity: Activity?) : OrientationEventListener(activity) {
    companion object {
        private const val ROT_THRESHOLD = 5

        private const val ROT_0 = 0
        private const val ROT_90 = 90
        private const val ROT_180 = 180
        private const val ROT_270 = 270
    }

    private var orientationLockedPortrait = false
    private var orientationLockedLandscape = false

    fun lockLandscape() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        orientationLockedLandscape = true
        orientationLockedPortrait = false
    }

    fun lockPortrait() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        orientationLockedPortrait = true
        orientationLockedLandscape = false
    }

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == ORIENTATION_UNKNOWN) {
            return
        }

        val rotation: Int =
            when {
                abs(orientation - ROT_0) < ROT_THRESHOLD -> ROT_0
                abs(orientation - ROT_90) < ROT_THRESHOLD -> ROT_90
                abs(orientation - ROT_180) < ROT_THRESHOLD -> ROT_180
                abs(orientation - ROT_270) < ROT_THRESHOLD -> ROT_270
                else -> ORIENTATION_UNKNOWN
            }

        when (rotation) {
            ROT_0 -> if (!orientationLockedLandscape) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                orientationLockedPortrait = false
            }
            ROT_90 -> if (!orientationLockedPortrait) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                orientationLockedLandscape = false
            }
            ROT_180 -> if (!orientationLockedLandscape) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                orientationLockedPortrait = false
            }
            ROT_270 -> if (!orientationLockedPortrait) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                orientationLockedLandscape = false
            }
        }
    }
}
