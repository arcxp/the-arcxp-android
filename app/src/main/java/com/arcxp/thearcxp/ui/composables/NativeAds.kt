package com.arcxp.thearcxp.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.arcxp.thearcxp.databinding.NativeAdBinding
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun NativeAds(ad: NativeAd? = null) {
    AndroidViewBinding(factory = NativeAdBinding::inflate) {
        val adView = root.also { adView ->
            adView.headlineView = adHeadline
            adView.bodyView = adBody
            adView.iconView = adAppIcon
            adView.starRatingView = adStars
            adView.priceView = adPrice
            adView.storeView = adStore
            adView.advertiserView = adAdvertiser
        }

        ad?.let {
            it.headline?.let { headline ->
                adHeadline.text = headline
            }

            it.starRating?.let { rating ->
                adStars.rating = rating.toFloat()
            }

            it.body?.let { body ->
                adBody.text = body
            }

            it.price?.let { price ->
                adPrice.text = price
            }

            it.store?.let { store ->
                adStore.text = store
            }

            it.advertiser?.let { advertiser ->
                adAdvertiser.text = advertiser
            }

            it.icon?.let { icon ->
                adAppIcon.setImageDrawable(icon.drawable)
            }

            adView.setNativeAd(it) }
    }
}