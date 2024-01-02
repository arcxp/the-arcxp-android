package com.arcxp.thearcxp.video

import ErrorText
import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcxp.thearcxp.LocalVideoViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.components.LoadingSpinner
import com.arcxp.thearcxp.ui.composables.Paywall
import com.arcxp.thearcxp.utils.AnsTypes
import com.arcxp.thearcxp.utils.findActivity
import com.arcxp.thearcxp.utils.shareSheet
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.url
import kotlinx.coroutines.launch


@Composable
fun VideoMainScreen(
    videoViewModel: VideoViewModel = LocalVideoViewModel.current,
    modifier: Modifier = Modifier,
    openCreateAccount: () -> Unit = {},
    openSignIn: () -> Unit = {},
    backNavigation: () -> Unit,
) {
    val state by videoViewModel.state.collectAsStateWithLifecycle(VideoViewModel.State.LOADING)

    when (state) {
        is VideoViewModel.State.Success -> {
            val videos = (state as VideoViewModel.State.Success).arcXPVideo!!
            if (videos.isNotEmpty()) {
                VideoScreen(
                    modifier = modifier,
                    video = videos[0],
                    openCreateAccount = openCreateAccount,
                    openSignIn = openSignIn,
                    backNavigation = backNavigation
                )
            }
        }

        is VideoViewModel.State.LOADING -> {
            LoadingSpinner()
        }

        is VideoViewModel.State.Error -> {
            ErrorText(
                text = stringResource(id = R.string.get_video_error),
                message = (state as VideoViewModel.State.Error).arcXPException.localizedMessage.orEmpty()
            )
        }

        else -> {}
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoScreen(modifier: Modifier = Modifier,
                video: ArcVideoStream,
                videoViewModel: VideoViewModel = LocalVideoViewModel.current,
                openCreateAccount: () -> Unit = {},
                openSignIn: () -> Unit = {},
                backNavigation: () -> Unit,) {

    val context = LocalContext.current
    val activity = context.findActivity()
    val player = remember { videoViewModel.arcMediaPlayer }

    val lifecycleObserver = rememberUpdatedState(
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    videoViewModel.setHideBars(true)
                }

                Lifecycle.Event.ON_RESUME -> {
                    player.resume()
                }

                Lifecycle.Event.ON_STOP -> {
                    player.pause()
                    videoViewModel.setHideBars(false)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    player.finish()
                }

                else -> { /* Do nothing for other lifecycle events */
                }
            }
        }
    )
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentLifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver.value)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver.value)
        }
    }

    val modalBottomSheetState =
        androidx.compose.material.rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { false }
        )

    val view = remember {
        videoViewModel.createVideoView(
            activity = activity,
        )
    }
    LaunchedEffect(key1 = activity) {
        videoViewModel.configurePlayer(arcVideoFrame = view, activity = activity)
        video.apply {
            videoViewModel.setVideoTracking(
                backNavigation = backNavigation,
                shareAction = { context.shareSheet(url = url()) })
            player.initMediaWithShareURL(video = this, shareURL = url())
            videoViewModel.clearVideos()//TODO make a one time event
            player.displayVideo()
        }
    }

    val onBack = {
        checkPip(activity, videoViewModel, backNavigation)
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Column(
                modifier.fillMaxWidth()
            ) {
                Paywall(modifier = modifier,
                    openCreateAccount = openCreateAccount,
                    openSignIn = openSignIn,
                    backNavigation = backNavigation)
            }
        }
    ) {

        BackPressHandler(onBackPressed = onBack)

        DisposableEffect(
            AndroidView(
                modifier = modifier,
                factory = { view },
            )
        ) {
            onDispose {
                player.finish()
            }
        }
    }

    val paywallState by videoViewModel.paywallState.collectAsState(initial = VideoViewModel.PaywallState.Idle)

    when (paywallState) {
        is VideoViewModel.PaywallState.Done -> {
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(key1 = true) {
                coroutineScope.launch {
                    if (!(paywallState as VideoViewModel.PaywallState.Done).result.show) {
                        modalBottomSheetState.show()
                    } else {
                        modalBottomSheetState.hide()
                    }
                }
            }
        }

        is VideoViewModel.PaywallState.Idle -> {
            val lifecycleOwner = LocalLifecycleOwner.current
            val deviceType = stringResource(id = R.string.device_type)
            LaunchedEffect(key1 = true) {
                videoViewModel.evaluateForPaywall(
                    id = video.id,
                    contentType = AnsTypes.VIDEO.type,
                    section = null,
                    deviceType = deviceType,
                    lifecycleOwner
                )
            }
        }

        else -> {}
    }
}

fun checkPip(
    activity: Activity,
    videoViewModel: VideoViewModel,
    backNavigation: () -> Unit)
{
    if (videoViewModel.arcXPVideoConfig!!.isEnablePip) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
            )
            videoViewModel.arcMediaPlayer.resume()
            videoViewModel.arcMediaPlayer.hideControls()
        }
    } else {
        backNavigation.invoke()
    }
}

@Composable
fun BackPressHandler(
    backPressedDispatcher: OnBackPressedDispatcher? =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    onBackPressed: () -> Unit
) {
    val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
    }

    DisposableEffect(key1 = backPressedDispatcher) {
        backPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.remove()
        }
    }
}