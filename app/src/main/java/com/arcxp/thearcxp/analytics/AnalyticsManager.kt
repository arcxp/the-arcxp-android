package com.arcxp.thearcxp.analytics

import android.os.Bundle
import com.arcxp.thearcxp.analytics.AnalyticsManager.Companion.EVENT_LOGIN
import com.arcxp.thearcxp.analytics.AnalyticsManager.Companion.EVENT_SIGN_UP
import com.arcxp.thearcxp.analytics.AnalyticsManager.Companion.PARAM_LOGIN_TYPE
import com.arcxp.thearcxp.analytics.AnalyticsManager.Companion.PARAM_SIGN_UP_METHOD
import com.arcxp.thearcxp.utils.BundleFactory
import com.google.firebase.analytics.FirebaseAnalytics

interface AnalyticsManager {
    companion object {
        const val EVENT_SIGN_UP = "signup"
        const val EVENT_LOGIN = "login"
        const val PARAM_SIGN_UP_METHOD = "signupType"
        const val PARAM_LOGIN_TYPE = "loginType"
    }

    fun logEvent(eventName: String, params: Map<String, Any>? = null)
    fun logScreenView(screenName: String, screenClass: String)
    fun logSignUpMethod(signUpMethod: String)
    fun logLoginType(loginType: String)
}

class FirebaseAnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics) :
    AnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        firebaseAnalytics.logEvent(
            eventName,
            params?.let { toBundle(it) } ?: BundleFactory.createBundle())
    }

    override fun logScreenView(screenName: String, screenClass: String) {
        val bundle = BundleFactory.createBundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun logSignUpMethod(signUpMethod: String) {
        logEvent(eventName = EVENT_SIGN_UP, params = mapOf(PARAM_SIGN_UP_METHOD to signUpMethod))
    }

    override fun logLoginType(loginType: String) {
        logEvent(eventName = EVENT_LOGIN, params = mapOf(PARAM_LOGIN_TYPE to loginType))
    }

    private fun toBundle(params: Map<String, Any>): Bundle {
        val bundle = BundleFactory.createBundle()
        for ((key, value) in params) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Float -> bundle.putFloat(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> throw IllegalArgumentException("Unsupported data type: ${value.javaClass.simpleName}")
            }
        }
        return bundle
    }
}