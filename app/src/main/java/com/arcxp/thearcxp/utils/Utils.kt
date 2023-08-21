package com.arcxp.thearcxp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.widget.ViewPager2
import com.arc.arcvideo.ArcMediaPlayer
import com.arc.arcvideo.ArcMediaPlayerConfig
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.views.ArcVideoFrame
import com.arcxp.content.sdk.models.ArcXPContentElement
import com.arcxp.content.sdk.util.MoshiController.fromJson
import com.arcxp.content.sdk.util.MoshiController.toJson
import com.arcxp.thearcxp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

fun createImageView(url: String, caption: String?, activity: Activity): Pair<ImageView, TextView> {
    val imageView = ImageView(activity)
    val layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(0, 10, 0, 0)
    imageView.layoutParams = layoutParams
    Glide.with(activity)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(spinner(activity.applicationContext))
        .dontAnimate()
        .into(imageView)
    imageView.adjustViewBounds = true
    var captionView = TextView(activity.applicationContext)
    if (!caption.isNullOrEmpty()) {
        captionView = createTextView(content = caption, context = activity.applicationContext)
        styleCaption(captionView)
    }
    return Pair(imageView, captionView)
}

fun styleCaption(caption: TextView) {
    caption.textSize = 11F
    caption.setTypeface(null, Typeface.BOLD_ITALIC)
    caption.textAlignment = View.TEXT_ALIGNMENT_CENTER
}

fun createVideoView(
    content: ArcXPContentElement,
    activity: Activity,
    arcMediaPlayer: ArcMediaPlayer
): ArcVideoFrame {
    val arcMediaPlayerConfigBuilder = ArcMediaPlayerConfig.Builder()
    val videoView = ArcVideoFrame(context = activity.applicationContext)

    // setting height and width of the VideoView in our linear layout
    val displayMetrics = DisplayMetrics()
    activity.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    val width: Int = displayMetrics.widthPixels
    val layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        width / 2
    )
    layoutParams.setMargins(10, 20, 10, 20)
    videoView.layoutParams = layoutParams
    arcMediaPlayerConfigBuilder.setVideoFrame(videoView/*videoFrame*/)
    arcMediaPlayerConfigBuilder.setActivity(activity)
    arcMediaPlayerConfigBuilder.setMaxBitRate(235152000)
    arcMediaPlayer.configureMediaPlayer(arcMediaPlayerConfigBuilder.build())
    arcMediaPlayer.initMedia(
        fromJson(
            toJson(content)!!,
            ArcVideoStream::class.java
        )
    )
    arcMediaPlayer.displayVideo()
    arcMediaPlayer.pause()

    videoView.clearFocus()
    return videoView
}

fun createTextView(content: String, context: Context): TextView {
    val newTextView = TextView(context)
    newTextView.setTextColor(context.resources.getColor(R.color.grey))
    val layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(0, 10, 0, 10)
    newTextView.layoutParams = layoutParams

    val formatted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(content, Build.VERSION.SDK_INT)
    } else {
        Html.fromHtml(content)
    }
    newTextView.text = formatted
    return newTextView
}

fun createGalleryView(
    context: Context,
    images: List<String?>,
    captions: List<String?>,
    titles: List<String?>
): ViewPager2 {
    val newGalleryView = ViewPager2(context)
    val adapter = GalleryAdapter(
        images = images,
        context = context,
        captions = captions,
        titles = titles
    )
    newGalleryView.adapter = adapter
    return newGalleryView
}

fun spinner(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()
    return circularProgressDrawable
}