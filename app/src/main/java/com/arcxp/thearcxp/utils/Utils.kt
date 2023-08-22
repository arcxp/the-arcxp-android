package com.arcxp.thearcxp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.widget.ViewPager2
import com.arcxp.commons.util.fallback
import com.arcxp.video.ArcMediaPlayer
import com.arcxp.video.ArcMediaPlayerConfig
import com.arcxp.video.views.ArcVideoFrame
import com.arcxp.content.models.Image
import com.arcxp.content.models.Taxonomy
import com.arcxp.content.models.imageUrl
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.BannerAdBinding
import com.arcxp.thearcxp.databinding.NativeAdBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions


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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        newTextView.setTextAppearance(R.style.ArticleBodyText)
    }
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
    circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(context, R.color.primaryDark))
    circularProgressDrawable.start()
    return circularProgressDrawable
}

private const val thumbnailResizeUrlKey = "thumbnailResizeUrl"
private const val resizeUrlKey = "resizeUrl"

fun createBannerAdView(
    context: Context,
    bannerAdFrame: ViewGroup,
    taxonomy: Taxonomy? = null,
    listener: AdListener
) {

    val bannerAd = BannerAdBinding.inflate(
            LayoutInflater.from(context),
        bannerAdFrame,
            false)

    val adView = AdView(context)
    adView.adListener = listener
    adView.setAdSize(AdSize.BANNER)
    adView.adUnitId = context.getString(R.string.admob_banner_id)

    val adRequestBuilder = AdRequest.Builder()

    taxonomy?.keywords?.forEach {
        if (it.keyword != null) {
            adRequestBuilder.addKeyword(it.keyword!!)
        }
    }

    adView.loadAd(adRequestBuilder.build())

    bannerAd.adView.removeAllViews()
    bannerAd.adView.addView(adView)

    bannerAdFrame.removeAllViews()
    bannerAdFrame.addView(bannerAd.root)
}

fun createNativeAdView(
    activity: Activity,
    adFrame: ViewGroup,
    taxonomy: Taxonomy? = null
) {
    val builder = AdLoader.Builder(activity, activity.getString(R.string.admob_native_id))

    builder.forNativeAd { nativeAd ->
        val activityDestroyed: Boolean = activity.isDestroyed
        if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
            nativeAd.destroy()
            return@forNativeAd
        }
        val nativeAdBinding = NativeAdBinding.inflate(activity.layoutInflater)
        populateNativeAdView(nativeAd, nativeAdBinding)
        adFrame.removeAllViews()
        adFrame.addView(nativeAdBinding.root)
    }

    val videoOptions =
        VideoOptions.Builder()
            .setStartMuted(true)
            .build()

    val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

    builder.withNativeAdOptions(adOptions)

    val adLoader =
        builder
            .withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        val error =
                            """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                        Toast.makeText(
                            activity,
                            "Failed to load native ad with error $error",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            )
            .build()

    val adRequestBuilder = AdRequest.Builder()

    taxonomy?.keywords?.forEach {
        if (it.keyword != null) {
            adRequestBuilder.addKeyword(it.keyword!!)
        }
    }

    adLoader.loadAd(adRequestBuilder.build())
}

private fun populateNativeAdView(nativeAd: NativeAd, adBinding: NativeAdBinding) {
    val nativeAdView = adBinding.root

    nativeAdView.mediaView = adBinding.adMedia

    nativeAdView.headlineView = adBinding.adHeadline
    nativeAdView.bodyView = adBinding.adBody
    nativeAdView.callToActionView = adBinding.adCallToAction
    nativeAdView.iconView = adBinding.adAppIcon
    nativeAdView.priceView = adBinding.adPrice
    nativeAdView.starRatingView = adBinding.adStars
    nativeAdView.storeView = adBinding.adStore
    nativeAdView.advertiserView = adBinding.adAdvertiser

    adBinding.adHeadline.text = nativeAd.headline
    nativeAd.mediaContent?.let { adBinding.adMedia.setMediaContent(it) }

    if (nativeAd.body == null) {
        adBinding.adBody.visibility = View.INVISIBLE
    } else {
        adBinding.adBody.visibility = View.VISIBLE
        adBinding.adBody.text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adBinding.adCallToAction.visibility = View.INVISIBLE
    } else {
        adBinding.adCallToAction.visibility = View.VISIBLE
        adBinding.adCallToAction.text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        adBinding.adAppIcon.visibility = View.GONE
    } else {
        adBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
        adBinding.adAppIcon.visibility = View.VISIBLE
    }

    if (nativeAd.price == null) {
        adBinding.adPrice.visibility = View.INVISIBLE
    } else {
        adBinding.adPrice.visibility = View.VISIBLE
        adBinding.adPrice.text = nativeAd.price
    }

    if (nativeAd.store == null) {
        adBinding.adStore.visibility = View.INVISIBLE
    } else {
        adBinding.adStore.visibility = View.VISIBLE
        adBinding.adStore.text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
        adBinding.adStars.visibility = View.INVISIBLE
    } else {
        adBinding.adStars.rating = nativeAd.starRating!!.toFloat()
        adBinding.adStars.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        adBinding.adAdvertiser.visibility = View.INVISIBLE
    } else {
        adBinding.adAdvertiser.text = nativeAd.advertiser
        adBinding.adAdvertiser.visibility = View.VISIBLE
    }

    nativeAdView.setNativeAd(nativeAd)

    // Get the video controller for the ad. One will always be provided, even if the ad doesn't
    // have a video asset.
    val mediaContent = nativeAd.mediaContent
    val vc = mediaContent?.videoController

    // Updates the UI to say whether or not this ad has a video asset.
    if (vc != null && mediaContent.hasVideoContent()) {
        vc.videoLifecycleCallbacks =
            object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {

                    super.onVideoEnd()
                }
            }
    }
}