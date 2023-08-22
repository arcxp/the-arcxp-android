package com.arcxp.thearcxp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.arc.arcvideo.ArcMediaClient
import com.arc.arcvideo.ArcMediaPlayer
import com.arc.arcvideo.ArcMediaPlayerConfig
import com.arc.arcvideo.ArcVideoStreamCallback
import com.arc.arcvideo.model.ArcVideoResponse
import com.arc.arcvideo.model.ArcVideoSDKErrorType
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.model.VideoVO
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.commerce.ArcXPPageviewEvaluationResult
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.extendedModels.ArcXPCollection
import com.arcxp.content.sdk.extendedModels.ArcXPStory
import com.arcxp.content.sdk.models.*
import com.arcxp.content.sdk.util.Either
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.tabfragment.BaseSectionFragment
import com.arcxp.thearcxp.tabfragment.SectionFragment
import com.arcxp.thearcxp.tabfragment.WebSectionFragment
import com.arcxp.thearcxp.utils.getNameToUseFromSection
import com.facebook.login.widget.LoginButton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.arcxp.commerce.util.Either as EitherCommerce

/**
 * View model class for the app
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    enum class FragmentView(val tag: String) {
        HOME("home"),
        VIDEO("video"),
        ACCOUNT("account")
    }

    //Event sent when the section list has been loaded
    private val _sectionsListEvent =
        MutableLiveData<Either<ArcXPContentError, List<ArcXPSection>>>()
    val sectionsListEvent: LiveData<Either<ArcXPContentError, List<ArcXPSection>>> =
        _sectionsListEvent

    //Event sent when a section is selected from the view pager
    private val _sectionEvent = MutableLiveData<ArcXPSection>()
    val sectionEvent: LiveData<ArcXPSection> = _sectionEvent

    //Event sent when navigating from paywall to open the article being viewed
    //when the paywall was invoked
    private val _openLastArticleEvent = MutableLiveData<Pair<String, String>>()
    val openLastArticleEvent: LiveData<Pair<String, String>> = _openLastArticleEvent

    //Event observed by MainActivity to open Article by id
    private val _openArticleEvent = Channel<String>()
    val openArticleEvent = _openArticleEvent.receiveAsFlow()

    //Event observed by MainActivity to open Video by id
    private val _openVideoEvent = Channel<String>()
    val openVideoEvent = _openVideoEvent.receiveAsFlow()

    //Event observed by MainActivity to lock rotation
    private val _sensorLockEvent = MutableLiveData<Boolean>()
    val sensorLockEvent: LiveData<Boolean> = _sensorLockEvent

    //Event observed by PlayVideoFragment to request video by id from Video Center
    private val _videoResultEvent = Channel<Either<ArcXPContentError, ArcVideoStream>>()
    val videoResultEvent = _videoResultEvent.receiveAsFlow()

    var sections = HashMap<String, ArcXPSection>()
    var sectionsIndexMap = HashMap<Int, String>()
    var indexSectionMap = HashMap<String, Int>()
    private val fragments = HashMap<String, BaseSectionFragment>()

    private var contentId = Pair("", "")

    var arcMediaPlayer: ArcMediaPlayer? = null

    var currentFragmentTag: FragmentView = FragmentView.HOME

    var isStartup = true

    val videoClient = ArcMediaClient.Companion.createClient(
        orgName = ArcXPContentSDK.arcxpContentConfig().orgName,
        environment = ArcXPContentSDK.arcxpContentConfig().environment
    )

    //Keep the video configuration here so it does not get
    //recreated on device rotation
    val arcMediaPlayerConfigBuilder = ArcMediaPlayerConfig.Builder()

    fun getSectionList(owner: LifecycleOwner) {
        ArcXPContentSDK.contentManager().getSectionList().observe(owner) { result ->
            when (result) {
                is Success -> {
                    sections.clear()
                    result.success.forEach { section ->
                        sections[section.getNameToUseFromSection()] = section
                    }
                    _sectionsListEvent.postValue(Success(result.success))
                }
                is Failure -> {
                    _sectionsListEvent.postValue(Failure(result.failure))
                }
            }
        }
    }

    fun updatePassword(
        newPassword: String,
        oldPassword: String,
        listener: ArcXPIdentityListener?
    ): LiveData<EitherCommerce<ArcXPError, ArcXPIdentity>> {
        return ArcXPCommerceSDK.commerceManager()
            .updatePassword(newPassword, oldPassword, object : ArcXPIdentityListener() {
                override fun onPasswordChangeSuccess(it: ArcXPIdentity) {
                    listener?.onPasswordChangeSuccess(it)
                }

                override fun onPasswordChangeError(error: ArcXPError) {
                    listener?.onPasswordChangeError(error)
                }
            })
    }

    fun sectionSelected(section: ArcXPSection) {
        _sectionEvent.postValue(section)
    }

    fun restoreContentEvent() {
        contentId.apply {
            if (first.isNotBlank() and second.isNotBlank()) {
                _openLastArticleEvent.postValue(Pair(first, second))
            }
        }
    }

    fun clearLastView() {
        _openLastArticleEvent.postValue(Pair("", ""))
    }

    fun loginWithGoogle(activity: MainActivity, owner: LifecycleOwner): LiveData<ArcXPAuth> {
        return ArcXPCommerceSDK.commerceManager().loginWithGoogle(activity)
    }

    fun loginWithFacebook(fbButton: LoginButton, owner: LifecycleOwner): LiveData<ArcXPAuth> {
        return ArcXPCommerceSDK.commerceManager().loginWithFacebook(fbButton)
    }

    fun login(
        email: String,
        password: String,
        owner: LifecycleOwner
    ): LiveData<EitherCommerce<ArcXPError, ArcXPAuth>> {
        return ArcXPCommerceSDK.commerceManager().login(email, password)
    }

    fun logout(listener: ArcXPIdentityListener? = null): LiveData<EitherCommerce<ArcXPError, Boolean>> {
        contentId = Pair("", "")
        return ArcXPCommerceSDK.commerceManager().logout(object : ArcXPIdentityListener() {
            override fun onLogoutSuccess() {
                listener?.onLogoutSuccess()
            }

            override fun onLogoutError(error: ArcXPError) {
                listener?.onLogoutError(error)

            }
        })
    }

    fun rememberUser(isChecked: Boolean) {
        ArcXPCommerceSDK.commerceManager().rememberUser(isChecked)
    }

    fun isLoggedIn(): LiveData<Boolean> {
        return ArcXPCommerceSDK.commerceManager().isLoggedIn()
    }

    fun commerceErrors() = ArcXPCommerceSDK.commerceManager().errors

    fun signUp(
        username: String,
        password: String,
        email: String,
        firstname: String,
        lastname: String
    ): LiveData<ArcXPUser> {
        return ArcXPCommerceSDK.commerceManager().signUp(
            username = username,
            password = password,
            email = email,
            firstname = firstname,
            lastname = lastname
        )
    }

    fun getUserProfile(listener: ArcXPIdentityListener? = null): LiveData<EitherCommerce<ArcXPError, ArcXPProfileManage>> {
        return ArcXPCommerceSDK.commerceManager().getUserProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                listener?.onFetchProfileSuccess(profileResponse)
            }

            override fun onProfileError(error: ArcXPError) {
                listener?.onProfileError(error)
            }
        })
    }

    fun getFragment(index: Int): BaseSectionFragment? {
        return if (fragments[sectionsIndexMap[index]] == null) {
            null
        } else fragments[sectionsIndexMap[index]] as BaseSectionFragment
    }

    fun createFragment(section: ArcXPSection): BaseSectionFragment {
        return when (section.type) {
            "web" -> createWebSection(section.id, section.name)
            else -> SectionFragment.create(section)
        }
    }

    //This is the call to the Commerce SDK to run the paywall algorithm.
    //The resulting object will contain a variable 'show'.  True means show the page,
    //False means the paywall should be shown.
    fun evaluateForPaywall(
        id: String,
        contentType: String?,
        section: String?,
        deviceType: String?
    ): LiveData<ArcXPPageviewEvaluationResult> {
        contentId = Pair(contentType!!, id)
        return ArcXPCommerceSDK.commerceManager().evaluatePage(
            pageId = id,
            contentType = contentType,
            contentSection = section,
            deviceClass = deviceType,
            otherConditions = null
        )
    }

    //Retrieve a collection based upon ID
    fun getCollection(
        id: String,
        from: Int = 0,
        size: Int = 20
    ): LiveData<Either<ArcXPContentError, Map<Int, ArcXPCollection>>> {
        return ArcXPContentSDK.contentManager().getCollection(id, from = from, size = size)
    }

    //Retrieve a video collection based user configured video collection name/content alias
    fun getVideoCollection(
        from: Int = 0,
        size: Int = 20
    ): LiveData<Either<ArcXPContentError, Map<Int, ArcXPCollection>>> {
        return ArcXPContentSDK.contentManager().getVideoCollection(from = from, size = size)
    }

    //Retrieve a story based on ID
    fun getStory(id: String): LiveData<Either<ArcXPContentError, ArcXPStory>> {
        return ArcXPContentSDK.contentManager().getArcXPStory(id)
    }

    //Retrieve a video based on ID
    fun loadVideo(id: String) {
        videoClient.findByUuid(uuid = id, listener = object : ArcVideoStreamCallback {
            override fun onVideoResponse(arcVideoResponse: ArcVideoResponse?) {
            }

            override fun onVideoStream(videos: List<ArcVideoStream>?) {
                if (videos?.isNotEmpty() == true) {
                    viewModelScope.launch {
                        _videoResultEvent.trySend(Success(success = videos[0]))
                    }
                }
            }

            override fun onLiveVideos(videos: List<VideoVO>?) {}

            override fun onError(
                type: ArcVideoSDKErrorType,
                message: String,
                value: Any?
            ) {
                viewModelScope.launch {
                    _videoResultEvent.trySend(Failure(
                        failure = ArcXPContentError(
                            ArcXPContentSDKErrorType.SERVER_ERROR,
                            message,
                            value
                        )
                    ))
                }
            }

        })
    }

    private fun createWebSection(key: String?, name: String?) =
        WebSectionFragment().withUrlAndName(key, name)

    //Create an instance of the video player from the Video SDK.  This is done
    //here so that it can be retained upon device rotation.
    fun createVideoPlayer() {
        arcMediaPlayer = ArcMediaPlayer.createPlayer(getApplication())
    }

    //Dispose of the video player.  If this is not called the audio
    //will continue to play even after the video player is not being shown.
    fun disposeVideoPlayer() {
        arcMediaPlayer?.finish()
        arcMediaPlayer = null
    }

    //Public function to request Article by id from sdk
    fun openArticle(id: String) {
        viewModelScope.launch {
            _openArticleEvent.send(id)
        }
    }

    //Public function to request Video by id from sdk
    fun openVideo(id: String) {
        viewModelScope.launch {
            _openVideoEvent.send(id)
        }
    }

    //Public function to request rotation Lock
    fun setPortrait(rotationOn: Boolean) {
        _sensorLockEvent.postValue(rotationOn)
    }

    override fun onCleared() {
        super.onCleared()
        disposeVideoPlayer()
    }
}