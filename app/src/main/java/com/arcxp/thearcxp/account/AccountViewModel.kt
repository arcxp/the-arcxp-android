package com.arcxp.thearcxp.account

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPRequestPasswordReset
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.MainApplication
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.facebook.login.widget.LoginButton
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class AccountViewModel(val analyticsManager: FirebaseAnalyticsManager?, val app: Application) :
    AndroidViewModel(app) {
    sealed class LoginState {
        object IDLE : LoginState()
        object Success : LoginState()
        object LOADING : LoginState()
        class Error(val arcXPException: ArcXPException) : LoginState()
    }

    sealed class ForgotPasswordState {
        object IDLE : ForgotPasswordState()
        object Success : ForgotPasswordState()
        object LOADING : ForgotPasswordState()
        class Error(val arcXPException: ArcXPException) : ForgotPasswordState()
    }

    sealed class ChangePasswordState {
        object IDLE : ChangePasswordState()
        object Success : ChangePasswordState()
        object LOADING : ChangePasswordState()
        class Error(val arcXPException: ArcXPException) : ChangePasswordState()
    }

    private val _googleLoginEvent = MutableLiveData<Boolean>()
    val openGoogleLoginEvent: LiveData<Boolean> = _googleLoginEvent

    private val _facebookLoginEvent = MutableLiveData<Boolean>()
    val openFacebookLoginEvent: LiveData<Boolean> = _facebookLoginEvent

    val isLoggedIn: LiveData<Boolean> = ArcXPMobileSDK.commerceManager().loggedInState

    private val _showAds = MutableLiveData(true)
    val showAds: LiveData<Boolean> = _showAds

    private val _userProfile = MutableLiveData<Either<ArcXPException, ArcXPProfileManage>>()
    val userProfile: LiveData<Either<ArcXPException, ArcXPProfileManage>> = _userProfile

    private val _loginState = Channel<LoginState>()
    val loginState = _loginState.receiveAsFlow()

    private val _forgotPasswordState = Channel<ForgotPasswordState>()
    val forgotPasswordState = _forgotPasswordState.receiveAsFlow()

    private val _changePasswordState = Channel<ChangePasswordState>()
    val changePasswordState = _changePasswordState.receiveAsFlow()

    private val _policiesState = MutableLiveData<List<ArcXPSection>>()
    val policiesState: LiveData<List<ArcXPSection>> = _policiesState

    private var contentId = Pair("", "")

    var registeredForPushNotifications = false

    init {
        downloadAds(app)
        getPolicies()
    }

    private lateinit var adLoader: AdLoader
    private val ads: ArrayList<NativeAd> = arrayListOf()

    // Event observed by SectionListFragment to preload native ads
    private val _nativeAdEvent = MutableLiveData<List<NativeAd>>()
    val nativeAdEvent: LiveData<List<NativeAd>> = _nativeAdEvent

    fun shouldWeShowAds() = (app as MainApplication).showAds() && isLoggedIn.value == false

    private fun downloadAds(context: Context) {
        adLoader = AdLoader.Builder(context, context.getString(R.string.admob_native_id))
            .forNativeAd { ad: NativeAd ->
                ads.add(ad)
                if (!adLoader.isLoading && ads.isNotEmpty()) {
                    _nativeAdEvent.postValue(ads)
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {}
                override fun onAdLoaded() {}
            })
            .build()
        adLoader.loadAds(AdRequest.Builder().build(), 5)
    }

    override fun onCleared() {
        super.onCleared()
        ads.forEach {
            it.destroy()
        }
    }

    fun checkSession() {
        ArcXPMobileSDK.commerceManager().validateSession(object : ArcXPIdentityListener() {
            override fun onValidateSessionSuccess() {
                toggleLogin(true)
            }

            override fun onValidateSessionError(error: ArcXPException) {
                toggleLogin(false)
            }
        })
    }

    fun getUserProfile() {
        ArcXPMobileSDK.commerceManager().getUserProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                _userProfile.postValue(Success(profileResponse))
            }

            override fun onProfileError(error: ArcXPException) {
                _userProfile.postValue(Failure(error))
            }
        })
    }

    fun login(
        email: String,
        password: String,
    ) {
        ArcXPMobileSDK.commerceManager().login(email, password, object : ArcXPIdentityListener() {
            override fun onLoginSuccess(response: ArcXPAuth) {
                _loginState.trySend(LoginState.Success)
                toggleLogin(true)
            }

            override fun onLoginError(error: ArcXPException) {
                _loginState.trySend(LoginState.Error(error))
                toggleLogin(false)
            }
        })
    }

    fun forgotPassword(email: String) {
        ArcXPMobileSDK.commerceManager()
            .requestResetPassword(email, object : ArcXPIdentityListener() {
                override fun onPasswordResetNonceSuccess(response: ArcXPRequestPasswordReset?) {
                    _forgotPasswordState.trySend(ForgotPasswordState.Success)
                }

                override fun onPasswordResetNonceFailure(error: ArcXPException) {
                    _forgotPasswordState.trySend(ForgotPasswordState.Error(error))
                }
            })
    }

    fun changePassword(newPassword: String, oldPassword: String) {
        ArcXPMobileSDK.commerceManager()
            .updatePassword(newPassword, oldPassword, object : ArcXPIdentityListener() {
                override fun onPasswordChangeSuccess(response: ArcXPIdentity) {
                    _changePasswordState.trySend(ChangePasswordState.Success)
                }

                override fun onPasswordChangeError(error: ArcXPException) {
                    _changePasswordState.trySend(ChangePasswordState.Error(error))
                }
            }
            )
    }

    fun idleForgotPassword() {
        _forgotPasswordState.trySend(ForgotPasswordState.IDLE)
    }

    fun idleChangePassword() {
        _changePasswordState.trySend(ChangePasswordState.IDLE)
    }

    fun setLoginInProgress() {
        _loginState.trySend(LoginState.LOADING)
    }

    fun setForgotPasswordProgress() {
        _forgotPasswordState.trySend(ForgotPasswordState.LOADING)
    }

    fun setChangePasswordProgress() {
        _changePasswordState.trySend(ChangePasswordState.LOADING)
    }

    fun rememberUser(isChecked: Boolean) {
        ArcXPMobileSDK.commerceManager().rememberUser(isChecked)
    }

    fun logout() {
        contentId = Pair("", "")
        ArcXPMobileSDK.commerceManager().logout(object : ArcXPIdentityListener() {
            override fun onLogoutSuccess() {
                toggleLogin(false)
            }
        })
    }

    fun googleLogin() {
        analyticsManager?.logLoginType(SIGN_IN_GOOGLE)
        _googleLoginEvent.postValue(true)
    }

    fun loginWithGoogle(activity: MainActivity, owner: LifecycleOwner): LiveData<ArcXPAuth> {
        return ArcXPMobileSDK.commerceManager().loginWithGoogle(activity)
    }

    fun loginWithFacebook(fbButton: LoginButton, owner: LifecycleOwner): LiveData<ArcXPAuth> {
        return ArcXPMobileSDK.commerceManager().loginWithFacebook(fbButton)
    }

    fun facebookLogin() {
        analyticsManager?.logLoginType(SIGN_IN_FACEBOOK)
        _facebookLoginEvent.postValue(true)
    }


    private fun toggleLogin(value: Boolean) {
        _showAds.postValue((app as MainApplication).showAds() && !value)
    }

    private fun getPolicies() {
        viewModelScope.launch {
            ArcXPMobileSDK.contentManager().getSectionListSuspend(
                siteHierarchy = getApplication<Application>().resources.getString(R.string.policies_hierarchy)
            ).apply {
                when (this) {
                    is Success -> {
                        _policiesState.postValue(this.success)
                    }
                    is Failure -> {
                        //TODO
                    }
                }
            }
        }

    }

    companion object {
        const val SIGN_IN_ARC = "ArcXP"
        const val SIGN_IN_GOOGLE = "Google"
        const val SIGN_IN_FACEBOOK = "Facebook"
    }
}