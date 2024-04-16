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
}