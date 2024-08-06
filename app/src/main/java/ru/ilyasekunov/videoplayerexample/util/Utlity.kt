package ru.ilyasekunov.videoplayerexample.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun setLandscape(context: Context) {
    val activity = context.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    activity?.setFullSensorAfterDelay(4000)
}

@SuppressLint("SourceLockedOrientationActivity")
fun setPortrait(context: Context) {
    val activity = context.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    activity?.setFullSensorAfterDelay(4000)
}

private fun Activity.setFullSensorAfterDelay(delay: Long) {
    if (Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
        Handler(Looper.getMainLooper()).postDelayed(
            { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR },
            delay
        )
    }
}

fun hideSystemUi(activity: Activity) {
    with(activity) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}