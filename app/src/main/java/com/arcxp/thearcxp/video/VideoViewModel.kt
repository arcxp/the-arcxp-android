package com.arcxp.thearcxp.video

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.ArcXPPageviewEvaluationResult
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.video.ArcMediaPlayer
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.ArcVideoEventsListener
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.model.VideoVO
import com.arcxp.video.views.ArcVideoFrame
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    sealed class State {
        class Success(val arcXPVideo: List<ArcVideoStream>?) : State()
        data object LOADING : State()
        class Error(val arcXPException: ArcXPException) : State()
    }

    sealed class PaywallState {
        class Done(val result: ArcXPPageviewEvaluationResult) : PaywallState()

        object Evaluating: PaywallState()
        object Idle: PaywallState()
    }

    private val _paywallState = Channel<PaywallState>()
    val paywallState = _paywallState.receiveAsFlow()

    private val _state = Channel<State>()
    val state = _state.receiveAsFlow()

    private val videoClient = ArcXPMobileSDK.mediaClient()

    private val _hideBars = MutableLiveData(false)
    val hideBars: LiveData<Boolean> = _hideBars

    fun setHideBars(value: Boolean) {
        _hideBars.postValue(value)
    }

    val arcMediaPlayer: ArcMediaPlayer by lazy {
        // Create an instance of the video player from the Video Module of the SDK.
        // This is done here so that it can be retained upon device rotation.
        ArcMediaPlayer.createPlayer(getApplication())
    }

    //Keep the video configuration here so it does not get
    //recreated on device rotation
    private val arcXPVideoConfigBuilder = ArcXPVideoConfig.Builder()

    private var arcXPVideoConfig: ArcXPVideoConfig? = null

    fun configurePlayer(arcVideoFrame: ArcVideoFrame, activity: Activity) {
        arcXPVideoConfigBuilder.setVideoFrame(videoFrame = arcVideoFrame)
        arcXPVideoConfigBuilder.enablePip(enable = true)
        arcXPVideoConfigBuilder.setActivity(activity = activity)
        arcXPVideoConfigBuilder.setAutoStartPlay(play = false)
        arcXPVideoConfigBuilder.setShouldShowFullScreenButton(shouldShowFullScreenButton = false)
        arcXPVideoConfigBuilder.showSeekButton(show = true)
        arcXPVideoConfigBuilder.useDialogForFullscreen(use = true)
        arcXPVideoConfigBuilder.showProgressBar(show = true)
        arcXPVideoConfig = arcXPVideoConfigBuilder.build()
        arcMediaPlayer.configureMediaPlayer(config = arcXPVideoConfig)
    }

    fun setVideoTracking(shareAction: () -> Unit) {
        arcMediaPlayer.trackMediaEvents(object : ArcVideoEventsListener {
            override fun onVideoTrackingEvent(
                type: TrackingType?,
                videoData: TrackingTypeData.TrackingVideoTypeData?
            ) {
                when (type) {
                    TrackingType.ON_SHARE -> shareAction()
                    else -> {}
                }
            }

            override fun onAdTrackingEvent(
                type: TrackingType?,
                adData: TrackingTypeData.TrackingAdTypeData?
            ) {
            }

            override fun onSourceTrackingEvent(
                type: TrackingType?,
                source: TrackingTypeData.TrackingSourceTypeData?
            ) {
            }

            override fun onError(
                type: TrackingType?,
                video: TrackingTypeData.TrackingErrorTypeData?
            ) {
            }
        })

    }

    fun fetchVideo(id: String) {
        videoClient.findByUuid(
            uuid = id,
            listener = object : ArcVideoStreamCallback {

                override fun onVideoStream(videos: List<ArcVideoStream>?) {
                    if (videos?.isNotEmpty() == true) {
                        _state.trySend(State.Success(arcXPVideo = videos))
                    }
                }

                override fun onLiveVideos(videos: List<VideoVO>?) {}

                override fun onError(
                    type: ArcXPSDKErrorType,
                    message: String,
                    value: Any?
                ) {
                    val error = ArcXPException(
                        message = message,
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        value = value
                    )
                    _state.trySend(State.Error(arcXPException = error))
                }

            })
    }

    //This is the call to the Commerce SDK to run the paywall algorithm.
    //The resulting object will contain a variable 'show'.  True means show the page,
    //False means the paywall should be shown.
    fun evaluateForPaywall(
        id: String,
        contentType: String?,
        section: String?,
        deviceType: String?,
        viewLifecycleOwner: LifecycleOwner
    ) {
        _paywallState.trySend(PaywallState.Evaluating)
        if (ArcXPMobileSDK.commerceInitialized()) {
            ArcXPMobileSDK.commerceManager().evaluatePage(
                pageId = id,
                contentType = contentType,
                contentSection = section,
                deviceClass = deviceType,
                otherConditions = null
            ).observe(viewLifecycleOwner) { result ->
                _paywallState.trySend(PaywallState.Done(result))
            }
        } else {
            val result = ArcXPPageviewEvaluationResult(id, true)
            viewModelScope.launch {
                _paywallState.trySend(PaywallState.Done(result))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposeVideoPlayer()
    }

    //Dispose of the video player.  If this is not called the audio
    //will continue to play even after the video player is not being shown.
    private fun disposeVideoPlayer() {
        arcMediaPlayer.finish()
    }

    fun createVideoView(
        activity: Activity
    ) = ArcVideoFrame(context = activity.applicationContext)
}