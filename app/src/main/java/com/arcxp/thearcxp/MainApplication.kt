package com.arcxp.thearcxp

import android.app.Application
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.content.sdk.ArcXPContentConfig
import com.arcxp.content.sdk.ArcXPContentSDK

/**
 * Main application class for the app.
 * This app is uses the single activity model with MainActivity being the only activity.
 * Therefore there is only ViewModel for the app.
 *
 * This class does the following :
 * 1. Instantiates the viewmodel
 * 2. Initializes all of the ArcXP SDKs.
 *
 */
class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        //Initialize the ArcXP Content SDK
        val contentConfig = ArcXPContentConfig.Builder()
            //Set the base URL for content.  Set the organization, site and environment.
            //These values can be gotten from the ArcXP admin
            .setBaseUrl(url = getString(R.string.contentUrl))
            .setOrgName(name = getString(R.string.orgName))
            .setSite(site = getString(R.string.siteName))
            .setEnvironment(env = getString(R.string.environment))
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
        ArcXPContentSDK.initialize(this, contentConfig)

        //If the client code caches UUID, refresh token and access token they can
        //be passed into the SDK using this variable
        val commerceAuthData = mutableMapOf<String, String>()
        //Initialize the Commerce SDK.
        val arcCommerceConfig = ArcXPCommerceConfig.Builder()
            .setContext(this)
            //IDs for Facebook and Google.  Needed for third party login capabilities.
            .setFacebookAppId(getString(R.string.facebook_app_id))
            .setGoogleClientId(getString(R.string.google_key))
             //Base URLs provided by ArcXP admin
            .setBaseUrl(getString(R.string.commerceUrl))
            .setBaseSalesUrl(getString(R.string.commerceUrl))
            .setBaseRetailUrl(getString(R.string.commerceUrl))
            .setUrlComponents(getString(R.string.orgName), getString(R.string.siteName), getString(R.string.environment))
            //Will the users email be used as their username.
            .setUserNameIsEmail(false)
            .enableAutoCache(true)
            .usePaywallCache(true)
            .build()

        /**
         * Comment out this line if you are not using the Commerce SDK
         */
        ArcXPCommerceSDK.initialize(this, commerceAuthData, arcCommerceConfig)

    }
}