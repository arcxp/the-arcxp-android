package com.arcxp.thearcxp.article

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.ArcXPPageviewEvaluationResult
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.thearcxp.video.VideoViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ArticleViewModel(application: Application) : AndroidViewModel(application) {

    sealed class State {
        class Success(val arcXPStory: ArcXPStory) : State()
        object LOADING : State()
        class Error(val arcXPException: ArcXPException) : State()
    }

    sealed class PaywallState {
        class Done(val result: ArcXPPageviewEvaluationResult) : PaywallState()

        object Evaluating: PaywallState()
        object Idle: PaywallState()
    }

    private val _state = Channel<State>()
    val state = _state.receiveAsFlow()

    private val _paywallState = Channel<PaywallState>()
    val paywallState = _paywallState.receiveAsFlow()

    private val _hideBars = MutableLiveData(false)
    val hideBars: LiveData<Boolean> = _hideBars

    fun setHideBars(value: Boolean) {
        _hideBars.postValue(value)
    }

    val videoClient = ArcXPMobileSDK.mediaClient()

    private var prePaywallContentId = Pair("", "")

    //Event sent when navigating from paywall to open the article being viewed
    //when the paywall was invoked
    private val _openLastArticleEvent = Channel<Pair<String, String>>()
    val openLastArticleEvent = _openLastArticleEvent.receiveAsFlow()

    //Retrieve a story based on ID
    fun getStory(id: String) {
        ArcXPMobileSDK.contentManager().getArcXPStory(id, listener = object : ArcXPContentCallback {
            override fun onGetStorySuccess(response: ArcXPStory) {
                viewModelScope.launch {
                    _state.trySend(State.Success(arcXPStory = response))
                }
            }

            override fun onError(error: ArcXPException) {
                viewModelScope.launch {
                    _state.trySend(State.Error(arcXPException = error))
                }
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

    /* if user logs in from paywall dialog, should go back to article being viewed */
    fun restoreContentEvent() {
        viewModelScope.launch {
            prePaywallContentId.apply {
                if (first.isNotBlank() and second.isNotBlank()) {
                    _openLastArticleEvent.trySend(Pair(first, second))
                }
            }
        }
    }
    /* this clears the cached article/video to 'return' to if a user has manually navigated away from paywall dialog */
    fun clearLastView() {
        prePaywallContentId = Pair("", "") //clear the cached item
        _openLastArticleEvent.trySend(prePaywallContentId) // in case the article event has not been observed, clear it
    }

    /* if a user logs in from paywall screen, it should return to the content viewed so we cache that value here */
    fun setContentToReturnToFromPaywall(id: String, contentType: String?) {
        prePaywallContentId = Pair(contentType.orEmpty(), id)
    }
}