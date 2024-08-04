package ru.ilyasekunov.videoplayerexample.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Locale

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun setLandscape(context: Context) {
    val activity = context.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

@SuppressLint("SourceLockedOrientationActivity")
fun setPortrait(context: Context) {
    val activity = context.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

fun Long.formatMillis(): String {
    val seconds = this / 1000
    val minutesFormatted = seconds / 60
    val secondsAfterMinute = seconds % 60

    return String.format(
        locale = Locale.getDefault(),
        format = "%02d:%02d",
        args = arrayOf(minutesFormatted, secondsAfterMinute)
    )
}

fun hideSystemUi(activity: Activity) {
    with(activity) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}