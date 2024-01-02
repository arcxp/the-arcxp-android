package com.arcxp.thearcxp.ui.composables

import ErrorText
import ImageFromUrl
import LoadingSpinner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.arcxp.content.extendedModels.ArcXPCollection
import com.arcxp.content.extendedModels.author
import com.arcxp.content.extendedModels.date
import com.arcxp.content.extendedModels.description
import com.arcxp.content.extendedModels.imageUrl
import com.arcxp.content.extendedModels.isVideo
import com.arcxp.content.extendedModels.thumbnail
import com.arcxp.content.extendedModels.title
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.LocalFirebaseAnalyticsManager
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.ui.composables.SectionType.ARTICLE
import com.arcxp.thearcxp.ui.composables.SectionType.SEARCH
import com.arcxp.thearcxp.ui.composables.SectionType.VIDEO
import com.arcxp.thearcxp.utils.OnLifecycleEvent
import kotlinx.coroutines.flow.Flow

enum class SectionType {
    VIDEO,
    ARTICLE,
    SEARCH
}

@Composable
fun SectionList(
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = LocalAccountViewModel.current,
    collectionFlow: Flow<PagingData<ArcXPCollection>>,
    sectionType: SectionType,
    openArticle: (String) -> Unit = {},
    openVideo: (String) -> Unit = {},
    analyticsManager: FirebaseAnalyticsManager? = LocalFirebaseAnalyticsManager.current
) {
    val collections = remember(collectionFlow) { collectionFlow }.collectAsLazyPagingItems()

    if (sectionType == VIDEO) {
        OnLifecycleEvent(onEvent = { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                analyticsManager?.logScreenView("VideoScreen", "VideoScreen")
            }
        })
    }

    val ads = accountViewModel.nativeAdEvent.observeAsState()
    val isLoggedIn = accountViewModel.isLoggedIn.observeAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(
            items = collections,
            key = { _, collection -> collection.id } // Use unique id as a key
        ) { index, collection ->
            collection?.apply {

                val onClick = {
                    if (isVideo()) {
                        openVideo(id)
                    } else {
                        openArticle(id)
                    }
                }


                if (index == 0) {
                    CollectionCardTop(
                        onContentClick = onClick
                    )
                } else {
                    SectionListCard(
                        onContentClick = onClick
                    )
                    if (index <= collections.itemCount - 1) {
                        SectionDivider()
                    }
                    if (index % 5 == 0 && isLoggedIn.value == false && accountViewModel.shouldWeShowAds()) {
                        val ad = ads.value?.random()
                        ad?.let {
                            Text(
                                text = stringResource(id = R.string.native_ad),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(
                                        start = dimensionResource(id = R.dimen.padding_large),
                                        bottom = dimensionResource(id = R.dimen.padding_large)
                                    )
                                    .fillMaxWidth()
                            )
                            NativeAds(ad = it)
                            SectionDivider()
                        }
                    }
                }
            }

        }

        collections.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item { LoadingSpinner() }
                }

                loadState.append is LoadState.Loading -> {
                    item { LoadingSpinner() }
                }

                loadState.refresh is LoadState.Error -> {
                    item {
                        ErrorText(
                            text = stringResource(id = R.string.get_collection_error),
                            message = stringResource(
                                id =
                                when (sectionType) {
                                    ARTICLE -> R.string.empty_collection
                                    VIDEO -> R.string.empty_video_collection
                                    SEARCH -> R.string.no_search_results
                                }

                            )
                        )
                    }
                }

                loadState.append is LoadState.Error -> {
                    val e = (loadState.append as LoadState.Error).error
                    item {
                        //we expect the pagination source to run out and eventually be empty,
                        // so don't show a error text on empty result
                        if (e.localizedMessage != stringResource(id = R.string.empty_collection)) {
                            ErrorText(
                                text = stringResource(id = R.string.get_collection_error),
                                message = e.localizedMessage.orEmpty()
                            )
                        }
                    }
                }

                loadState.append is LoadState.NotLoading -> {
                    if (collections.itemCount == 0 ) {
                        item {
                            ErrorText(
                                text = stringResource(id = R.string.no_search_results)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ArcXPCollection.SectionListCard(
    onContentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onContentClick)
            .fillMaxWidth()
            .padding(
                vertical = dimensionResource(id = R.dimen.padding_small),
                horizontal = dimensionResource(id = R.dimen.padding_large)
            )
    ) {
        Column(
            Modifier
                .weight(2f)
                .padding(end = dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = title(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
            )
            if (description().isNotBlank()) {
                Text(
                    text = description(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
            }
            Text(
                text = date(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (thumbnail().isNotBlank()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(percent = 10))
                    .size(dimensionResource(id = R.dimen.search_item_image_height))
            ) {
                ImageFromUrl(
                    model = thumbnail(),
                    contentDescription = title(),
                    contentScale = ContentScale.Crop,
                )
                if (isVideo()) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.play_arrow),
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .height(height = dimensionResource(id = R.dimen.icon_height))
                            .width(width = dimensionResource(id = R.dimen.icon_width)),
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }

        }
    }
}

@Composable
fun ArcXPCollection.CollectionCardTop(
    onContentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_small))
            .clickable(onClick = onContentClick)
    ) {
        if (imageUrl().isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_xlarge))
                    .heightIn(max = dimensionResource(id = R.dimen.top_section_image_height))
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(percent = 7))
            ) {
                ImageFromUrl(
                    model = imageUrl(),
                    contentDescription = title(),
                    contentScale = ContentScale.Crop
                )
                if (isVideo()) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.play_arrow),
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .height(height = dimensionResource(id = R.dimen.icon_height_large))
                            .width(
                                width = dimensionResource(id = R.dimen.icon_width_large)
                            ),
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }

        }
        Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_large)))

        Text(
            text = title(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
        )
        Row {
            if (author().isNotBlank()) {
                Text(
                    text = author(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.bullet),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_large))
                )
            }
            Text(
                text = date(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Divider(
        color = MaterialTheme.colorScheme.tertiary, thickness = .5.dp, modifier = Modifier.padding(
            dimensionResource(id = R.dimen.padding_small)
        )
    )
}

@Composable
fun SectionDivider() {
    Divider(
        color = MaterialTheme.colorScheme.tertiary,
        thickness = dimensionResource(id = R.dimen.section_divider_size),
        modifier = Modifier.padding(
            dimensionResource(id = R.dimen.padding_large)
        )
    )
}