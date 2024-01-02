package com.arcxp.thearcxp.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.arcxp.thearcxp.R
import com.arcxp.video.ArcMediaPlayer
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.views.ArcVideoFrame
import java.net.URLDecoder
import java.net.URLEncoder

fun createVideoView(
    activity: Activity,
    arcMediaPlayer: ArcMediaPlayer,
): ArcVideoFrame {
    val videoView = ArcVideoFrame(context = activity.applicationContext)
    // setting height and width of the VideoView in our linear layout

    videoView.layoutParams = LinearLayout.LayoutParams(
        MATCH_PARENT,
        activity.resources.getInteger(R.integer.article_video_height)
    ).apply {
        setMargins(10, 20, 10, 20) //TODO extract these numbers
    }

    arcMediaPlayer.configureMediaPlayer(
        config = ArcXPVideoConfig.Builder()
            .setVideoFrame(videoFrame = videoView)
            .setActivity(activity = activity)
            .setMaxBitRate(rate = Int.MAX_VALUE)
            .setShouldShowFullScreenButton(shouldShowFullScreenButton = true)
            .setVideoResizeMode(mode = ArcXPVideoConfig.VideoResizeMode.FIT)
            .build()
    )

    videoView.clearFocus()
    return videoView
}

fun spinner(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(context, R.color.primaryDark))
    circularProgressDrawable.start()
    return circularProgressDrawable
}

object BundleFactory {
    fun createBundle() = Bundle()
}

// function to encode your URL
fun encodeUrl(url: String) = URLEncoder.encode(url, "UTF-8")

// function to decode your URL
fun decodeUrl(url: String) = URLDecoder.decode(url, "UTF-8")


fun Context.shareSheet(url: String) {
    if (url.isNotEmpty()) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            this.type = "text/plain"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }, null))
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}