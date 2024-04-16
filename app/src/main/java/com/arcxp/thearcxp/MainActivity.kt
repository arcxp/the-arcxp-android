package com.arcxp.thearcxp

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.arcxp.ArcXPMobileSDK
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.account.AccountViewModelFactory
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.article.ArticleViewModel
import com.arcxp.thearcxp.push.IntentNavigationDataItem
import com.arcxp.thearcxp.push.SettingsNavigationItem
import com.arcxp.thearcxp.ui.composables.NewsApp
import com.arcxp.thearcxp.video.VideoViewModel
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.arcxp.thearcxp.web.WebViewModel
import com.arcxp.thearcxp.widget.ArcXPWidget.Companion.WIDGET_ARTICLE_ID_KEY
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

/**
 * The only activity in the app.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var webViewModel: WebViewModel
    private var analyticsManager: FirebaseAnalyticsManager? = null
    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        openContentFromPushNotification(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        //init firebase if setting is true and has google services json
        if (resources.getBoolean(R.bool.use_firebase) && BuildConfig.HAS_GOOGLE_SERVICES) {
            analyticsManager = FirebaseAnalyticsManager(
                firebaseAnalytics = Firebase.analytics
            )
            initPushNotifications()
        }
        articleViewModel = ViewModelProvider(this)[ArticleViewModel::class.java]
        val factory = AccountViewModelFactory(application, analyticsManager)
        accountViewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
        videoViewModel = ViewModelProvider(this)[VideoViewModel::class.java]
        webViewModel = ViewModelProvider(this)[WebViewModel::class.java]

        mainViewModel.fetchSectionsList()

        setContent {
            CompositionLocalProvider(
                LocalAccountViewModel provides accountViewModel,
                LocalVideoViewModel provides videoViewModel,
                LocalArticleViewModel provides articleViewModel,
                LocalMainViewModel provides mainViewModel,
                LocalFirebaseAnalyticsManager provides analyticsManager,
                LocalWebViewModel provides webViewModel
            ) {
                NewsApp()
            }
            openArticleFromWidget()
            openContentFromPushNotification(intent)
        }

        //Isolate all ads logic
        initAds()

        accountViewModel.openGoogleLoginEvent.observe(this) {
            if (it) {
                ArcXPMobileSDK.commerceManager()
                    .loginWithGoogle(this)
                    .observe(this)
                    {
                        mainViewModel.triggerNavigation(destination = SettingsNavigationItem)
                    }
            }
        }
    }

    private fun openArticleFromWidget() {
        val extras = intent.extras
        if (extras != null) {
            val articleId = extras.getString(WIDGET_ARTICLE_ID_KEY)
            if (articleId != null) {
                val navItem = IntentNavigationDataItem(articleId, CONTENT_TYPE_ARG_ARTICLE)
                mainViewModel.triggerNavigation(navItem)
            }
        }
    }

    private fun openContentFromPushNotification(intent: Intent?) {

        if (intent != null) {
            val extras = intent.extras

            val uuid = extras?.getString(CONTENT_UUID_ARG)
            val contentType = extras?.getString(CONTENT_TYPE_ARG)

            if (uuid != null && contentType != null) {
                val navItem = IntentNavigationDataItem(uuid, contentType)
                mainViewModel.triggerNavigation(navItem)
            } else {
                //do nothing
            }
        }
    }

    private fun initAds() {
        if (accountViewModel.shouldWeShowAds()) {
            MobileAds.initialize(
                this
            ) { }

            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(emptyList()).build()
            MobileAds.setRequestConfiguration(configuration)

            val debugSettings = ConsentDebugSettings.Builder(this)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                .addTestDeviceHashedId("...")//
                /*  for this value, check logcat with device for the following line (where ... is actual id):
                    Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("...") to set this as a debug device.
                    note the id and add or replace a test device here
                */
                /* this now gets used regardless of id with all emulators */

                .build()

            val params = ConsentRequestParameters
                .Builder()
//                .setConsentDebugSettings(debugSettings)
                .build()

            consentInformation = UserMessagingPlatform.getConsentInformation(this)

            consentInformation.reset()

            consentInformation.requestConsentInfoUpdate(
                this,
                params,
                {
                    if (consentInformation.isConsentFormAvailable) {
                        loadForm()
                    }
                },
                {
                    Log.e("ArcXP", it.message)
                })
        }
    }

    //Load the ads consent form
    //you will need a test device id hash added to initAds function
    private fun loadForm() {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(
            this,
            {
                this.consentForm = it
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(
                        this
                    ) {
                        if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                            // App can start requesting ads.
                        }

                        // Handle dismissal by reloading form.
                        loadForm()
                    }
                }
            },
            {
                // Handle the error.
            }
        )
    }

    private fun initPushNotifications() {
        askNotificationPermission()

        mainViewModel.loadPushNotificationSubscribedTopics()

        Firebase.messaging.token.addOnCompleteListener { task ->
            accountViewModel.registeredForPushNotifications = task.isSuccessful
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 101)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "arcxp_mobile_channel"
            val descriptionText = "arcxp_mobile_channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("arcxp_mobile_channel", name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }


    //Called when PIP is entered and exited
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        configuration: Configuration
    ) {
        videoViewModel.arcMediaPlayer.onPictureInPictureModeChanged(isInPictureInPictureMode = isInPictureInPictureMode, null)
        if (!isInPictureInPictureMode) {
            videoViewModel.arcMediaPlayer.returnToNormalFromPip()
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration)
    }

    companion object {
        const val CONTENT_UUID_ARG = "uuid"
        const val CONTENT_TYPE_ARG = "content-type"
        const val CONTENT_TYPE_ARG_ARTICLE = "article"
        const val CONTENT_TYPE_ARG_VIDEO = "video"
        const val CONTENT_TYPE_ARG_NONE = "none"
    }


}

val LocalAccountViewModel = compositionLocalOf<AccountViewModel> {
    error("No ViewModel provided")
}
val LocalVideoViewModel = compositionLocalOf<VideoViewModel> {
    error("No ViewModel provided")
}
val LocalMainViewModel = compositionLocalOf<MainViewModel> {
    error("No ViewModel provided")
}
val LocalArticleViewModel = compositionLocalOf<ArticleViewModel> {
    error("No ViewModel provided")
}
val LocalFirebaseAnalyticsManager = compositionLocalOf<FirebaseAnalyticsManager?> {
    error("No FirebaseAnalyticsManager provided")
}
val LocalWebViewModel = compositionLocalOf<WebViewModel> {
    error("No ViewModel provided")
}