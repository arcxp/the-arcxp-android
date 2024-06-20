package com.arcxp.thearcxp.article

import ErrorText
import ImageFromUrl
import LoadingSpinner
import android.annotation.SuppressLint
import android.app.Activity
import android.text.Spanned
import android.text.method.LinkMovementMethod
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.fromHtml
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.extendedModels.author
import com.arcxp.content.extendedModels.date
import com.arcxp.content.extendedModels.imageUrl
import com.arcxp.content.extendedModels.subheadlines
import com.arcxp.content.extendedModels.title
import com.arcxp.content.extendedModels.url
import com.arcxp.content.models.Code
import com.arcxp.content.models.Correction
import com.arcxp.content.models.CustomEmbed
import com.arcxp.content.models.Divider
import com.arcxp.content.models.ElementGroup
import com.arcxp.content.models.Endorsement
import com.arcxp.content.models.Gallery
import com.arcxp.content.models.Header
import com.arcxp.content.models.Image
import com.arcxp.content.models.InterstitialLink
import com.arcxp.content.models.LinkList
import com.arcxp.content.models.NumericRating
import com.arcxp.content.models.OembedResponse
import com.arcxp.content.models.Quote
import com.arcxp.content.models.RawHTML
import com.arcxp.content.models.StoryElement
import com.arcxp.content.models.StoryList
import com.arcxp.content.models.Table
import com.arcxp.content.models.Text
import com.arcxp.content.models.Video
import com.arcxp.content.models.imageUrl
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.LocalArticleViewModel
import com.arcxp.thearcxp.LocalFirebaseAnalyticsManager
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.ads.BannerAdView
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.ui.composables.Paywall
import com.arcxp.thearcxp.utils.AnsTypes
import com.arcxp.thearcxp.utils.LinkifyText
import com.arcxp.thearcxp.utils.OnLifecycleEvent
import com.arcxp.thearcxp.utils.createVideoView
import com.arcxp.thearcxp.utils.shareSheet
import com.arcxp.video.ArcMediaPlayer
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.model.ArcVideoStream
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch


lateinit var uriHandler: UriHandler

private val arcMediaPlayers = mutableListOf<ArcMediaPlayer>()

@Composable
fun ArticleMainScreen(
    articleViewModel: ArticleViewModel = LocalArticleViewModel.current,
    openCreateAccount: () -> Unit = {},
    openSignIn: () -> Unit = {},
    backNavigation: () -> Unit,
) {
    val state by articleViewModel.state.collectAsStateWithLifecycle(ArticleViewModel.State.LOADING)

    when (state) {
        is ArticleViewModel.State.Success -> {

            ArticleScreen(
                story = (state as ArticleViewModel.State.Success).arcXPStory,
                modifier = Modifier,
                openCreateAccount = openCreateAccount,
                openSignIn = openSignIn,
                backNavigation = backNavigation
            )
        }

        is ArticleViewModel.State.LOADING -> {
            LoadingSpinner()
        }

        is ArticleViewModel.State.Error -> {
            val error = (state as ArticleViewModel.State.Error).arcXPException
            ErrorText(
                text = stringResource(id = R.string.get_article_error),
                message = error.localizedMessage.orEmpty()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ArticleScreen(
    story: ArcXPStory,
    modifier: Modifier = Modifier,
    articleViewModel: ArticleViewModel = LocalArticleViewModel.current,
    accountViewModel: AccountViewModel = LocalAccountViewModel.current,
    openCreateAccount: () -> Unit = {},
    openSignIn: () -> Unit = {},
    backNavigation: () -> Unit,
    analyticsManager: FirebaseAnalyticsManager? = LocalFirebaseAnalyticsManager.current
) {
    uriHandler = LocalUriHandler.current

    val lifecycleObserver = rememberUpdatedState(
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    articleViewModel.setHideBars(true)
                }
                Lifecycle.Event.ON_STOP -> {
                    articleViewModel.setHideBars(false)
                }

                else -> { /* Do nothing for other lifecycle events */
                }
            }
        }
    )
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver.value)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver.value)
        }
    }

    val showAds by accountViewModel.showAds.observeAsState()

    OnLifecycleEvent(onEvent = { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            analyticsManager?.logScreenView("ArticleScreen", "ArticleScreen")
        }
    })

    val modalBottomSheetState =
        androidx.compose.material.rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { false }
        )

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Column(
                modifier.fillMaxWidth()
            ) {
                Paywall(
                    modifier = modifier,
                    openCreateAccount = openCreateAccount,
                    openSignIn = openSignIn,
                    backNavigation = backNavigation
                )
            }
        }
    ) {
        Column {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            backNavigation()
                            cleanup()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            context.shareSheet(url = story.url())
                        }) {
                        Icon(
                            Icons.Filled.Share,
                            null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            )
            Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(id = R.dimen.space))
            ) {
                if (showAds == true) {
                    BannerAdView()
                }
                PostContentItems(story, articleViewModel = articleViewModel)
                if (showAds == true) {
                    BannerAdView()
                }
            }
        }
    }

    val paywallState by articleViewModel.paywallState.collectAsState(initial = ArticleViewModel.PaywallState.Idle)

    when (paywallState) {
        is ArticleViewModel.PaywallState.Done -> {
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(key1 = true) {
                coroutineScope.launch {
                    if (!(paywallState as ArticleViewModel.PaywallState.Done).result.show) {
                        modalBottomSheetState.show()
                    } else {
                        modalBottomSheetState.hide()
                    }
                }
            }
        }

        is ArticleViewModel.PaywallState.Idle -> {
            val lifecycleOwner = LocalLifecycleOwner.current
            val deviceType = stringResource(id = R.string.device_type)
            LaunchedEffect(key1 = true) {
                articleViewModel.evaluateForPaywall(
                    id = story._id!!,
                    contentType = AnsTypes.STORY.type,
                    section = null,
                    deviceType = deviceType,
                    lifecycleOwner
                )
            }
        }

        else -> {}
    }
}

@Composable
fun PostContentItems(post: ArcXPStory, articleViewModel: ArticleViewModel) {

    val containsGalleries = containGalleries(storyResponse = post)

    Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
    Text(
        post.title(),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(dimensionResource(id = R.dimen.space_small)))
    Text(post.subheadlines(), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))

    if (containsGalleries) {
        val gallery = post.content_elements?.find { it is Gallery }
        gallery?.let { InlineGallery(gallery = it as Gallery) }
    } else {
        PostHeaderImage(post)
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
    }
    post.credits?.by?.let {
        if (post.author().isNotEmpty()) {
            Text(post.author(), style = MaterialTheme.typography.bodySmall)
            Text(post.date(), style = MaterialTheme.typography.bodySmall)
        }
    }
    Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))

    var skipNextGallerySinceAlreadyDisplayed = containsGalleries

    post.content_elements?.forEach {
        when (it) {
            is Text -> {
                if (!it.content.isNullOrEmpty()) {
                    InlineText(text = fromHtml(it.content!!, HtmlCompat.FROM_HTML_MODE_COMPACT))
                }
            }

            is InterstitialLink -> {
                InterstitialLink(it, onClickLink = { uriHandler.openUri(it.url!!) })
            }

            is Image -> {
                InlineImage(it)
            }

            is Video -> {
                if (it._id != null) {
                    InlineVideo(video = it, articleViewModel = articleViewModel)
                }
            }

            is Gallery -> {
                if (skipNextGallerySinceAlreadyDisplayed) {
                    skipNextGallerySinceAlreadyDisplayed = false
                } else {
                    InlineGallery(gallery = it)
                }
            }

            // We don't use these types in this News App but you may need these fields for something.
            is Code -> {}
            is Correction -> {}
            is CustomEmbed -> {}
            is Divider -> {}
            is ElementGroup -> {}
            is Endorsement -> {}
            is Header -> {}
            is LinkList -> {}
            is NumericRating -> {}
            is OembedResponse -> {}
            is Quote -> {}
            is RawHTML -> {}
            is StoryList -> {}
            is Table -> {}
            is StoryElement.UnknownStoryElement -> {}
            else -> {}
        }
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
    }
}

@Composable
private fun InlineVideo(video: Video, articleViewModel: ArticleViewModel) {
    val activity = LocalContext.current as Activity
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    val player = remember { ArcMediaPlayer.createPlayer(activity) }

    val view = remember {
        createVideoView(
            activity = activity,
            arcMediaPlayer = player
        )
    }

    DisposableEffect(
        AndroidView(factory = {
            view
        })
    ) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    player.resume()
                }

                else -> {}
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            player.finish()
            lifecycle.removeObserver(observer)
        }
    }

    arcMediaPlayers.add(player)

    articleViewModel.videoClient.findByUuid(
        uuid = video._id!!,
        listener = object : ArcVideoStreamCallback {

            override fun onVideoStream(videos: List<ArcVideoStream>?) {
                if (videos?.isNotEmpty() == true) {
                    player.initMedia(videos[0])
                    player.displayVideo()
                    player.pause()
                }
            }

            override fun onError(
                type: ArcXPSDKErrorType,
                message: String,
                value: Any?
            ) {
//                    onError(
//                        ArcXPSDKErrorType.SERVER_ERROR, message, value
//                    )
            }

        })
}

@Composable
private fun InlineText(text: Spanned) {
    val highlightColor = MaterialTheme.colorScheme.secondary
    val textColor = MaterialTheme.colorScheme.onBackground
    AndroidView(
        modifier = Modifier,
        factory = {
            MaterialTextView(it).apply {
                // setting the color for text and highlighting the links
                setTextColor(textColor.toArgb())
                setLinkTextColor(highlightColor.toArgb())
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = {
            //it.maxLines = currentMaxLines
            it.text = text
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InlineGallery(gallery: Gallery) {
    val pageCount = gallery.content_elements!!.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top, //add this
        modifier = Modifier
            .wrapContentHeight() //and this
            .fillMaxWidth()


    ) { page ->
        GalleryImage(gallery.content_elements!![page] as Image)
    }
    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth(),
        //.align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { iteration ->
            val color =
                if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onTertiaryContainer
            Box(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.space_xsmall))
                    .clip(CircleShape)
                    .background(color)
                    .size(dimensionResource(id = R.dimen.space))

            )
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
private fun InterstitialLink(
    link: InterstitialLink,
    onClickLink: () -> Unit
) {
    val handler = LocalUriHandler.current

    LinkifyText(text = link.content!!,
        linkColor = MaterialTheme.colorScheme.secondary,
        linkEntire = true,
        clickable = true,
        onClickLink = {
            handler.openUri(it)
        }
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GalleryImage(image: Image) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_xlarge)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageFromUrl(
            model = image.imageUrl(),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.gallery_item_container_height)),
            contentDescription = image.caption.orEmpty()
        )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
        Text(
            image.caption.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
        Text(
            image.subtitle.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun InlineImage(image: Image) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageFromUrl(
            model = image.imageUrl(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = dimensionResource(id = R.dimen.rounded_corner_radius_dp),
                        topEnd = dimensionResource(id = R.dimen.rounded_corner_radius_dp),
                        bottomStart = dimensionResource(id = R.dimen.rounded_corner_radius_dp),
                        bottomEnd = dimensionResource(id = R.dimen.rounded_corner_radius_dp)
                    )
                ),
            contentDescription = image.caption.orEmpty(),
            contentScale = ContentScale.FillWidth,

            )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
        Text(
            image.caption ?: "",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.space)))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PostHeaderImage(post: ArcXPStory) {

    ImageFromUrl(
        model = post.imageUrl(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.rounded_corner_radius_dp),
                    topEnd = dimensionResource(id = R.dimen.rounded_corner_radius_dp),
                    bottomStart = dimensionResource(id = R.dimen.rounded_corner_radius_dp),
                    bottomEnd = dimensionResource(id = R.dimen.rounded_corner_radius_dp)
                )
            ),
        contentScale = ContentScale.FillWidth,
        contentDescription = post.content.orEmpty()
    )
}

private fun containGalleries(storyResponse: ArcXPStory) =
    storyResponse.content_elements?.any { it is Gallery } ?: false

fun cleanup() {
    arcMediaPlayers.forEach {
        if (!it.onBackPressed()) {
            it.finish()
        }
    }
}