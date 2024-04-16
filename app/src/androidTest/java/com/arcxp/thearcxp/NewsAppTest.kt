package com.arcxp.thearcxp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.title
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.article.ArticleViewModel
import com.arcxp.thearcxp.push.IntentNavigationDataItem
import com.arcxp.thearcxp.ui.composables.NewsApp
import com.arcxp.thearcxp.video.VideoViewModel
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.arcxp.thearcxp.web.WebViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsAppTest {


    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity


    @RelaxedMockK
    private lateinit var mainViewModel: MainViewModel

    @RelaxedMockK
    private lateinit var articleViewModel: ArticleViewModel

    @RelaxedMockK
    private lateinit var videoViewModel: VideoViewModel

    @RelaxedMockK
    private lateinit var webViewModel: WebViewModel

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
    fun alertBarTest() = runTest{
        // Create a MutableSharedFlow that you will control in the test
        val testFlow = MutableLiveData<ArcXPContentElement>()

        // Mock the ViewModel to use the testFlow
        coEvery { mainViewModel.alertBarState } returns testFlow


        val videoHideBarsLiveData = MutableLiveData(false)
        coEvery { videoViewModel.hideBars } returns videoHideBarsLiveData

        val articleHideBarsLiveData = MutableLiveData(false)
        coEvery { articleViewModel.hideBars } returns articleHideBarsLiveData

        val webHideBarsLiveData = MutableLiveData(false)
        coEvery { webViewModel.hideBars } returns webHideBarsLiveData

        val sectionListLiveData = MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>(null)
        coEvery { mainViewModel.sectionsListEvent } returns sectionListLiveData

        val alertBarContentElement = mockk<ArcXPContentElement> {
            coEvery { title() } returns "ALERT"
            coEvery { type } returns "video"
            coEvery { _id } returns "854d0c5d-5c9f-4009-bc32-2c51ab6796e0"
        }

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalVideoViewModel provides videoViewModel,
                LocalArticleViewModel provides articleViewModel,
                LocalMainViewModel provides mainViewModel,
                LocalWebViewModel provides webViewModel
            ) {
                NewsApp()
            }
        }

        runBlocking {
            testFlow.postValue(alertBarContentElement) // Replace with a mock or real object
        }


        composeTestRule.onRoot().printToLog("MY TAG")//prints entire semantic tree, leaving for reference
        composeTestRule.onNodeWithText("ALERT").assertExists()
        composeTestRule.onNodeWithText("open video").performClick()

        val slot = slot<IntentNavigationDataItem>()
        coVerify(exactly = 1) {
            mainViewModel.triggerNavigation(destination = capture(slot))
        }

        assertEquals("video", slot.captured.contentType)
        assertEquals("854d0c5d-5c9f-4009-bc32-2c51ab6796e0", slot.captured.uuid)
    }
    @Test
    fun alertBarTestWithNullNotVisible() = runTest{

        val testFlow = MutableLiveData<ArcXPContentElement>(null)
        coEvery { mainViewModel.alertBarState } returns testFlow

        val videoHideBarsLiveData = MutableLiveData(false)
        coEvery { videoViewModel.hideBars } returns videoHideBarsLiveData

        val articleHideBarsLiveData = MutableLiveData(false)
        coEvery { articleViewModel.hideBars } returns articleHideBarsLiveData

        val webHideBarsLiveData = MutableLiveData(false)
        coEvery { webViewModel.hideBars } returns webHideBarsLiveData

        val sectionListLiveData = MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>(null)
        coEvery { mainViewModel.sectionsListEvent } returns sectionListLiveData


        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalVideoViewModel provides videoViewModel,
                LocalArticleViewModel provides articleViewModel,
                LocalMainViewModel provides mainViewModel,
                LocalWebViewModel provides webViewModel
            ) {
                NewsApp()
            }
        }

        runBlocking {
            testFlow.postValue(null) // Replace with a mock or real object
        }


        composeTestRule.onRoot().printToLog("MY TAG")//prints entire semantic tree, leaving for reference
        composeTestRule.onNodeWithText("ALERT").assertDoesNotExist()
        composeTestRule.onNodeWithText("open").assertDoesNotExist()


        coVerify(exactly = 0) {
            mainViewModel.triggerNavigation(destination = any())
        }
    }
}