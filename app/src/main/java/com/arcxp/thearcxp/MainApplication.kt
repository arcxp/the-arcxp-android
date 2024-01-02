package com.arcxp.thearcxp

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.content.ArcXPContentConfig

/**
 * Main application class for the app.
 * This app uses the single activity model with MainActivity being the only activity.
 *
 * This class does the following :
 * 1.  Initialize the ArcXP SDK (with video module (default) and optional content and commerce modules).
 * 2.  Retrieves the showAd setting from strings.xml as source of truth on whether to enable ads.
 *
 */
class MainApplication : Application() {

    private var showAds = false

    override fun onCreate() {
        super.onCreate()

        showAds = resources.getBoolean(R.bool.show_ads)

        // Initialize the optional Content Module for SDK
        val contentConfig = ArcXPContentConfig.Builder()
            //This is an additional parameter put on the base URL that retrieves the
            //section data for mobile devices.
            .setNavigationEndpoint(endpoint = getString(R.string.navigation_endpoint))
            //This is a string corresponding to a video collection content alias
            .setVideoCollectionName(videoCollectionName = getString(R.string.video_endpoint))
            //Content SDK caches data to decrease the amount of bandwidth needed.
            //This value can be between 10 and 1024 MB
            .setCacheSize(sizeInMB = 1024)
            //After a specified number of minutes cached items will be updated to
            //ensure the latest version is available.
            .setCacheTimeUntilUpdate(minutes = 5)
            //if true will pre-fetch and store in db any stories returned by a collection call
            .setPreloading(preLoading = true)
            .build()


        // Initialize the optional Commerce Module for SDK

        //If the client code caches UUID, refresh token and access token they can
        //be passed into the SDK using this variable
        val commerceAuthData = mutableMapOf<String, String>()

        val commerceConfig = ArcXPCommerceConfig.Builder()
            .setContext(this)
            //IDs for Facebook and Google.  Needed for third party login capabilities.
            .setFacebookAppId(getString(R.string.facebook_app_id))
            .setGoogleClientId(getString(R.string.google_key))
            //Base URLs provided by ArcXP admin
            .setBaseUrl(getString(R.string.commerceUrl))
            .setBaseSalesUrl(getString(R.string.commerceUrl))
            .setBaseRetailUrl(getString(R.string.commerceUrl))
            //Will the users email be used as their username.
            .setUserNameIsEmail(false)
            .enableAutoCache(true)
            .usePaywallCache(true)
            .build()

        //Set the base URL for content.  Set the organization, site and environment.
        //These values can be gotten from your ArcXP admin
        ArcXPMobileSDK.initialize(
            application = this,
            site = getString(R.string.siteName),
            org = getString(R.string.orgName),
            environment = getString(R.string.environment),
            commerceConfig = commerceConfig, //comment out this line if you are not using the Commerce SDK
            contentConfig = contentConfig,
            baseUrl = getString(R.string.contentUrl)
        )

        //Check for ad enablement
        if (ArcXPMobileSDK.commerceInitialized() && ArcXPMobileSDK.commerceManager().sessionIsActive()) {
            showAds = false;
        }
    }

    fun showAds() = showAds
}