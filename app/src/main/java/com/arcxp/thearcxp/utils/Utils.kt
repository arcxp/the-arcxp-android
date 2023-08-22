package com.arcxp.thearcxp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
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
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.models.*
import com.arcxp.thearcxp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

fun createImageView(url: String, caption: String?, activity: Activity): Pair<ImageView, TextView> {
    val imageView = ImageView(activity)
    val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    layoutParams.setMargins(0, 10, 0, 0)
    imageView.layoutParams = layoutParams
    Glide.with(activity)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .error(R.drawable.ic_baseline_error_24_black)
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
    content: Video,
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

    arcMediaPlayerConfigBuilder.setVideoFrame(videoFrame = videoView)
    arcMediaPlayerConfigBuilder.setActivity(activity = activity)
    arcMediaPlayerConfigBuilder.setMaxBitRate(rate = Int.MAX_VALUE)
    arcMediaPlayerConfigBuilder.setShouldShowFullScreenButton(shouldShowFullScreenButton = true)
    arcMediaPlayer.configureMediaPlayer(arcMediaPlayerConfigBuilder.build())
    videoView.clearFocus()
    return videoView
}

fun createTextView(content: String, context: Context): TextView {
    val newTextView = TextView(context)
    newTextView.setTextColor(context.resources.getColor(R.color.grey))
    val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
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

private const val thumbnailResizeUrlKey = "thumbnailResizeUrl"

fun ArcXPContentElement.imageUrl() =
    when (this.type) {
        AnsTypes.VIDEO.type -> {
            this.promoItem?.basic?.url ?: ""
        }
        else -> {
            (this.promoItem?.basic?.additional_properties?.get(thumbnailResizeUrlKey) as? String
                ?: this.promoItem?.lead_art?.additional_properties?.get(thumbnailResizeUrlKey) as? String
                ?: this.promoItem?.lead_art?.promo_items?.basic?.additional_properties?.get(
                    thumbnailResizeUrlKey
                ) as? String)
                ?.let { createFullImageUrl(url = it) }
                ?: ""
        }
    }

fun ArcXPStory.imageUrl() =
    (this.promoItem?.basic?.additional_properties?.get(thumbnailResizeUrlKey) as? String
        ?: this.promoItem?.lead_art?.additional_properties?.get(thumbnailResizeUrlKey) as? String
        ?: this.promoItem?.lead_art?.promo_items?.basic?.additional_properties?.get(
            thumbnailResizeUrlKey
        ) as? String)
        ?.let { createFullImageUrl(url = it) }
        ?: ""


fun Image.imageUrl() =
    (this.additional_properties?.get(thumbnailResizeUrlKey) as? String)
        ?.let { createFullImageUrl(url = it) }
        ?: ""


fun ArcXPCollection.imageUrl() =
    this.promoItem?.basic?.url ?: (this.promoItem?.basic?.additional_properties?.get(
        thumbnailResizeUrlKey
    ) as? String
        ?: this.promoItem?.lead_art?.additional_properties?.get(thumbnailResizeUrlKey) as? String
        ?: this.promoItem?.lead_art?.promo_items?.basic?.additional_properties?.get(
            thumbnailResizeUrlKey
        ) as? String)
        ?.let { createFullImageUrl(url = it) }
    ?: ""

private fun createFullImageUrl(url: String) = "${ArcXPContentSDK.arcxpContentConfig().baseUrl}$url"