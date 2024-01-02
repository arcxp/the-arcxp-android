package com.arcxp.thearcxp

import android.os.Bundle
import com.arcxp.thearcxp.analytics.AnalyticsManager
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.utils.BundleFactory
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseAnalyticsManagerTest {
    @RelaxedMockK
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    @RelaxedMockK
    private lateinit var bundle: Bundle

    private lateinit var testObject: FirebaseAnalyticsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(BundleFactory)
        every { BundleFactory.createBundle() } returns bundle
        testObject = FirebaseAnalyticsManager(firebaseAnalytics)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `logEvent with non-null params`() {
        val eventName = "test_event"
        val params = mapOf("key1" to "value1", "key2" to 42, "key3" to 1000L, "key4" to 3.14f, "key5" to 2.718, "key6" to true)

        testObject.logEvent(eventName, params)

        verifySequence {
            bundle.putString("key1", "value1")
            bundle.putInt("key2", 42)
            bundle.putLong("key3", 1000L)
            bundle.putFloat("key4", 3.14f)
            bundle.putDouble("key5", 2.718)
            bundle.putBoolean("key6", true)
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }

    @Test
    fun `logEvent with null params`() {
        val eventName = "test_event"
        val params: Map<String, Any>? = null

        testObject.logEvent(eventName, params)

        verify {
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }

    @Test
    fun `logScreenView() returns expected bundle`() {
        val screenName = "TestScreen"
        val screenClass = "TestActivity"

        testObject.logScreenView(screenName, screenClass)

        val expectedBundle = bundle.apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        verify {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, expectedBundle)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `logEvent with unsupported data type - Char`() {
        val eventName = "test_event"
        val params = mapOf("key1" to 'a')
        testObject.logEvent(eventName, params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `logEvent with unsupported data type - CustomDataType()`() {
        val eventName = "test_event"
        val params = mapOf("key1" to CustomDataType("custom"))
        testObject.logEvent(eventName, params)
    }
    data class CustomDataType(val data: String)

    @Test
    fun `logSignUpMethod should log correct event and parameter`() {
        val signUpMethod = "ArcXP"
        val eventName = AnalyticsManager.EVENT_SIGN_UP

        testObject.logSignUpMethod(signUpMethod)

        verify {
            bundle.putString(AnalyticsManager.PARAM_SIGN_UP_METHOD, signUpMethod)
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }

    @Test
    fun `logLoginType should log correct event and parameter`() {
        val loginType = "Google"
        val eventName = AnalyticsManager.EVENT_LOGIN

        testObject.logLoginType(loginType)

        verify {
            bundle.putString(AnalyticsManager.PARAM_LOGIN_TYPE, loginType)
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }
}