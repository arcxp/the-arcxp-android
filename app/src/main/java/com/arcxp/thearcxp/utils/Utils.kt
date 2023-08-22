package com.arcxp.thearcxp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.widget.ViewPager2
import com.arc.arcvideo.ArcMediaPlayer
import com.arc.arcvideo.ArcMediaPlayerConfig
import com.arc.arcvideo.views.ArcVideoFrame
import com.arcxp.content.sdk.models.Image
import com.arcxp.content.sdk.models.imageUrl
import com.arcxp.content.sdk.util.fallback
import com.arcxp.thearcxp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


fun createImageView(item: Image, caption: String?, activity: Activity): Pair<ImageView, TextView> {
    val imageView = ImageView(activity)
    val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    layoutParams.setMargins(0, 10, 0, 0)
    imageView.layoutParams = layoutParams
    Glide.with(activity)
        .load(item.imageUrl())
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .error(
            Glide.with(activity)
                .load(item.fallback())
                .error(R.drawable.ic_baseline_error_24)
                .apply(RequestOptions().transform(RoundedCorners(activity.resources.getInteger(R.integer.rounded_corner_radius))))
        )
        .placeholder(spinner(activity.applicationContext))
        .dontAnimate()
        .apply(RequestOptions().transform(RoundedCorners(activity.resources.getInteger(R.integer.rounded_corner_radius))))
        .into(imageView)
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
    activity: Activity,
    arcMediaPlayer: ArcMediaPlayer
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
        config = ArcMediaPlayerConfig.Builder()
            .setVideoFrame(videoFrame = videoView)
            .setActivity(activity = activity)
            .setMaxBitRate(rate = Int.MAX_VALUE)
            .setShouldShowFullScreenButton(shouldShowFullScreenButton = true)
            .setVideoResizeMode(mode = ArcMediaPlayerConfig.VideoResizeMode.FIT)
            .build()
    )

    videoView.clearFocus()
    return videoView
}

fun createTextView(content: String, context: Context): TextView {
    val newTextView = TextView(context)
    newTextView.setTextColor(context.resources.getColor(R.color.grey))
    val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    layoutParams.setMargins(0, 10, 0, 10) //TODO extract these numbers
    newTextView.layoutParams = layoutParams

    val formatted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(content, Build.VERSION.SDK_INT)
    } else {
        Html.fromHtml(content)
    }
    newTextView.setTextAppearance(R.style.ArticleBodyText)
    newTextView.text = formatted
    return newTextView
}

fun createGalleryView(
    context: Context,
    images: List<Image>
): ViewPager2 {
    val newGalleryView = ViewPager2(context)
    val adapter = GalleryAdapter(
        images = images,
        context = context
    )
    newGalleryView.adapter = adapter
    return newGalleryView
}

fun spinner(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorSchemeColors(context.getColor(R.color.primaryDark))
    circularProgressDrawable.start()
    return circularProgressDrawable
}

private const val thumbnailResizeUrlKey = "thumbnailResizeUrl"
private const val resizeUrlKey = "resizeUrl"