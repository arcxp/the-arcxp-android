package com.arcxp.thearcxp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.ui.composables.AccountScreen
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountScreenTest {


    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity


    @RelaxedMockK
    private lateinit var accountViewModel: AccountViewModel

    @RelaxedMockK
    private lateinit var firebaseAnalytics: FirebaseAnalyticsManager

    @RelaxedMockK
    private lateinit var openWebUrl: (String, Boolean) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        AppEnvironment.isTestEnvironment = true
    }

    @After
    fun tearDown() {
        AppEnvironment.isTestEnvironment = false
    }

    @Test
    fun accountScreenWithNoPoliciesDisplaysNothing() {
        val isLoggedInLiveData = MutableLiveData(false)
        coEvery { accountViewModel.isLoggedIn } returns isLoggedInLiveData
        val alertLiveData = MutableLiveData<List<ArcXPSection>>()
        coEvery { accountViewModel.policiesState } returns alertLiveData

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccountViewModel provides accountViewModel,
                LocalFirebaseAnalyticsManager provides firebaseAnalytics,
            ) {
                AccountScreen(openWebViewWithUrl = openWebUrl)
            }
        }

        composeTestRule.onRoot().printToLog("MY TAG")//prints entire semantic tree, leaving for reference

        composeTestRule.onNodeWithText("Policies").assertDoesNotExist()
        composeTestRule.onNodeWithText("Privacy Policy").assertDoesNotExist()
        composeTestRule.onNodeWithText("Terms of Service").assertDoesNotExist()
    }

    @Test
    fun accountScreenWithPoliciesDisplaysAsExpected() {
        val policies = fromJsonList("[{\"_id\":\"/mobile-policies\",\"_website\":\"arcsales\",\"node_type\":\"section\",\"name\":\"Mobile - Policies\",\"navigation\":{\"nav_title\":\"Policies\"},\"children\":[{\"_id\":\"/mobile-terms\",\"_website\":\"arcsales\",\"node_type\":\"section\",\"name\":\"Mobile - Terms Of Service\",\"navigation\":{\"nav_title\":\"Terms of Service\"},\"children\":[],\"parent\":{\"default\":\"/\",\"mobile-policies\":\"/mobile-policies\"},\"ancestors\":{\"default\":[],\"mobile-policies\":[\"/\",\"/mobile-policies\"]},\"site\":{\"site_url\":\"https://www.washingtonpost.com/information/2022/01/01/terms-of-service/\",\"site_about\":null,\"site_description\":null,\"pagebuilder_path_for_native_apps\":null,\"site_taglined\":null,\"site_title\":null,\"sitesite_keywords_title\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null}},{\"_id\":\"/mobile-privacy\",\"_website\":\"arcsales\",\"node_type\":\"section\",\"name\":\"Mobile - Privacy Policy\",\"navigation\":{\"nav_title\":\"Privacy Policy\"},\"children\":[],\"parent\":{\"default\":\"/\",\"mobile-policies\":\"/mobile-policies\"},\"ancestors\":{\"default\":[],\"mobile-policies\":[\"/\",\"/mobile-policies\"]},\"site\":{\"site_url\":\"https://www.arcxp.com/privacy/\",\"site_about\":null,\"site_description\":null,\"pagebuilder_path_for_native_apps\":null,\"site_taglined\":null,\"site_title\":null,\"sitesite_keywords_title\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null}}],\"parent\":{\"default\":\"/\",\"mobile-policies\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-policies\":[\"/\"]},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":null,\"pagebuilder_path_for_native_apps\":null,\"site_taglined\":null,\"site_title\":null,\"sitesite_keywords_title\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null}}]", ArcXPSection::class.java)
        val isLoggedInLiveData = MutableLiveData(false)
        coEvery { accountViewModel.isLoggedIn } returns isLoggedInLiveData
        val alertLiveData = MutableLiveData<List<ArcXPSection>>(policies)
        coEvery { accountViewModel.policiesState } returns alertLiveData


        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccountViewModel provides accountViewModel,
                LocalFirebaseAnalyticsManager provides firebaseAnalytics,
            ) {
                AccountScreen(openWebViewWithUrl = openWebUrl)
            }
        }


        composeTestRule.onRoot().printToLog("MY TAG")//prints entire semantic tree, leaving for reference
        composeTestRule.onNodeWithText("Policies").assertExists()
        composeTestRule.onNodeWithText("Privacy Policy").assertExists()
        composeTestRule.onNodeWithText("Privacy Policy").performClick()
        composeTestRule.onNodeWithText("Terms of Service").assertExists()
        composeTestRule.onNodeWithText("Terms of Service").performClick()
        verify(exactly = 1) {
            openWebUrl.invoke("https://www.washingtonpost.com/information/2022/01/01/terms-of-service/", false)
            openWebUrl.invoke("https://www.arcxp.com/privacy/", false)
        }
    }
}