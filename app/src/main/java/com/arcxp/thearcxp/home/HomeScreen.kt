package com.arcxp.thearcxp.home

import ErrorText
import LoadingSpinner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.LocalFirebaseAnalyticsManager
import com.arcxp.thearcxp.LocalMainViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.ui.composables.SectionList
import com.arcxp.thearcxp.ui.composables.SectionType
import com.arcxp.thearcxp.utils.OnLifecycleEvent
import com.arcxp.thearcxp.utils.getNameToUseFromSection
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel = LocalMainViewModel.current,
    openArticle: (String) -> Unit,
    openVideo: (String) -> Unit,
    analyticsManager: FirebaseAnalyticsManager? = LocalFirebaseAnalyticsManager.current
) {
    val sectionsResult: Either<ArcXPException, List<ArcXPSection>>? by mainViewModel.sectionsListEvent.observeAsState()

    val sectionSelected: ArcXPSection? by mainViewModel.sectionEvent.observeAsState()

    when (sectionsResult) {
        is Success -> {
            // Handle Success case
            val sections = (sectionsResult as Success<List<ArcXPSection>>).success

            val pagerState = rememberPagerState(pageCount = { sections.size })

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(true) {
                coroutineScope.launch {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        mainViewModel.pageSelected(page)
                    }
                }
            }

            mainViewModel.sectionEvent.observe(LocalLifecycleOwner.current) {
                coroutineScope.launch {
                    if (sectionSelected != null) {
                        val index = sectionSelected?.getNameToUseFromSection() ?: 0
                        pagerState.animateScrollToPage(mainViewModel.indexSectionMap[index]!!)
                        mainViewModel.sectionSelected(null)
                    }
                }
            }

            OnLifecycleEvent(onEvent = { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    analyticsManager?.logScreenView("HomeScreen", "HomeScreen")
                }
            })

            Column {
                if (mainViewModel.sectionsIndexMap.isNotEmpty()) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                            )
                        },
                        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
                    ) {
                        mainViewModel.sectionsIndexMap.toSortedMap().forEach {
                            val selected = pagerState.currentPage == it.key

                            Tab(
                                selected = selected,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(it.key)
                                        mainViewModel.pageSelected(it.key)
                                    }
                                }

                            ) {
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.space_small)))
                                Text(
                                    text = it.value,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.space_small)))
                            }

                        }

                    }
                    HorizontalPager(state = pagerState, beyondBoundsPageCount = 5) { page ->
                        SectionList(
                            collectionFlow = mainViewModel.getCollection(collectionAlias = mainViewModel.sections[mainViewModel.sectionsIndexMap[page]]!!.id),
                            sectionType = SectionType.ARTICLE,
                            openArticle = openArticle,
                            openVideo = openVideo
                        )
                    }
                } else {
                    ErrorText(
                        text = stringResource(id = R.string.site_service_error),
                        message = stringResource(id = R.string.site_service_result_was_empty)
                    )
                }
            }
        }

        is Failure -> {
            ErrorText(
                text = stringResource(id = R.string.site_service_error),
                message = (sectionsResult as Failure<ArcXPException>).failure.localizedMessage
            )
        }

        else -> {
            LoadingSpinner()
        }
    }
}