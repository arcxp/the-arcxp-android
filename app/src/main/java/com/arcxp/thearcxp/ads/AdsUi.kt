package com.arcxp.thearcxp.ads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import com.arcxp.thearcxp.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView() {
    val adId = LocalContext.current.resources.getString(R.string.admob_banner_id)
    val attribution = LocalContext.current.resources.getString(R.string.ad_attribution)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(thickness = dimensionResource(id = R.dimen.ad_divider_size))
        //Text(text = attribution, Modifier.size(Dp(10.0f)))
        Spacer(Modifier.height(dimensionResource(id = R.dimen.ad_padding)))
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = adId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.ad_padding)))
        Divider(thickness = dimensionResource(id = R.dimen.ad_divider_size))
        Spacer(Modifier.height(dimensionResource(id = R.dimen.ad_padding)))
    }
}