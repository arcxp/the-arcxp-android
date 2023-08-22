package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.arc.arcvideo.listeners.ArcVideoEventsListener
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.model.TrackingType
import com.arc.arcvideo.model.TrackingType.*
import com.arc.arcvideo.model.TrackingTypeData
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.models.ArcXPContentSDKErrorType
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.MoshiController.fromJson
import com.arcxp.content.sdk.util.MoshiController.toJson
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentPlayvideoBinding
import com.arcxp.thearcxp.utils.AnsTypes
import com.arcxp.thearcxp.utils.Paywall

class PlayVideoFragment : BaseFragment(), Paywall.OnPaywallCancelledListener {

    private var _binding: FragmentPlayvideoBinding? = null
    private val binding get() = _binding!!

    private val paywall = Paywall()

    //Constant for setting pip
    private val pipEnabled = true

    //Keep track of if we are in PIP so the video player can be shut down properly
    private var isInPip: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayvideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        paywall.setOnPaywallCancelledListener(this)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressedHandler()
                }
            })

        val id = requireArguments().getString(KEY, "")

        if (vm.arcMediaPlayer == null) {
            loadVideo(id)
            if (ArcXPCommerceSDK.isInitialized()) {
                vm.evaluateForPaywall(
                    id = id,
                    contentType = AnsTypes.VIDEO.type,
                    section = null,
                    deviceType = getString(R.string.device_type)
                ).observe(viewLifecycleOwner) {
                    if (!it.show) {
                        paywall.show(parentFragmentManager, getString(R.string.paywall))
                    }
                }
            }
        } else {
            restartVideo()
        }
    }

    private fun playVideo(video: ArcVideoStream) {
        vm.createVideoPlayer()
        vm.arcMediaPlayerConfigBuilder.setVideoFrame(videoFrame = binding.videoFrame)
        vm.arcMediaPlayerConfigBuilder.enablePip(enable = pipEnabled)
        vm.arcMediaPlayerConfigBuilder.setActivity(activity = requireActivity())
        vm.arcMediaPlayerConfigBuilder.setAutoStartPlay(play = false)
        vm.arcMediaPlayerConfigBuilder.setShouldShowBackButton(shouldShowBackButton = true)
        vm.arcMediaPlayerConfigBuilder.setShouldShowFullScreenButton(shouldShowFullScreenButton = false)
        vm.arcMediaPlayerConfigBuilder.showSeekButton(show = true)
        vm.arcMediaPlayerConfigBuilder.useDialogForFullscreen(use = true)
        vm.arcMediaPlayerConfigBuilder.setDisableControlsToggleWithTouch(disable = true)
        vm.arcMediaPlayer?.configureMediaPlayer(config = vm.arcMediaPlayerConfigBuilder.build())
        vm.arcMediaPlayer?.initMediaWithShareURL(video = video, "a url string")
        vm.arcMediaPlayer?.setFullscreen(full = true)
        vm.arcMediaPlayer?.displayVideo()
        setVideoTracking()
    }

    //There are a lot of places in the code that need to shut
    //down the video and the fragment.  They all call this method
    private fun cleanupVideoOnClose() {
        vm.arcMediaPlayer?.setFullscreen(full = false)
        vm.disposeVideoPlayer()
        requireActivity().supportFragmentManager.popBackStack()
        dismissSnackBar()
    }

    //When the user clicks the close button on the PIP window
    //the only way to detect it is here.
    override fun onStop() {
        super.onStop()
        if (isInPip) {
            cleanupVideoOnClose()
        }
    }

    //Called when PIP is entered and exited
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean
    ) {
        isInPip = isInPictureInPictureMode
        if (!isInPictureInPictureMode) {
            vm.arcMediaPlayer?.returnToNormalFromPip()
        }
    }

    private fun restartVideo() {
        vm.arcMediaPlayerConfigBuilder.setVideoFrame(binding.videoFrame)
        vm.arcMediaPlayer?.configureMediaPlayer(vm.arcMediaPlayerConfigBuilder.build())
        vm.arcMediaPlayer?.displayVideo()
        setVideoTracking()
    }

    private fun setVideoTracking() {
        vm.arcMediaPlayer?.trackMediaEvents(object : ArcVideoEventsListener {
            override fun onVideoTrackingEvent(
                type: TrackingType?,
                videoData: TrackingTypeData.TrackingVideoTypeData?
            ) {
                when (type) {
                    ON_PLAYER_TOUCHED -> {
                        if (vm.arcMediaPlayer?.isControlsVisible == true) {
                            vm.arcMediaPlayer?.hideControls()
                        } else {
                            vm.arcMediaPlayer?.showControls()
                        }
                    }
                    ON_SHARE -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.share_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BACK_BUTTON_PRESSED -> {
                        cleanupVideoOnClose()
                    }
                    else -> {}
                }
            }

            override fun onAdTrackingEvent(
                type: TrackingType?,
                adData: TrackingTypeData.TrackingAdTypeData?
            ) {
            }

            override fun onSourceTrackingEvent(
                type: TrackingType?,
                source: TrackingTypeData.TrackingSourceTypeData?
            ) {
            }

            override fun onError(
                type: TrackingType?,
                video: TrackingTypeData.TrackingErrorTypeData?
            ) {
            }
        })
    }

    private fun loadVideo(id: String) {
        vm.getVideo(id).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    val video = fromJson(
                        toJson(it.success)!!,
                        ArcVideoStream::class.java
                    )!! //TODO remove this re-serialization and pass directly to video sdk in shared format
                    playVideo(video)
                }
                is Failure -> {
                    onError(
                        error = ArcXPContentError(
                            ArcXPContentSDKErrorType.SERVER_ERROR,
                            getString(R.string.video_load_error)
                        )
                    )
                }
            }

        }
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.root,
            R.id.video_view_fragment,
            true,
            this::onBackPressedHandler
        )
    }

    private fun showSpinner(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    override fun onPaywallCancel() {
        dismissSnackBar()
        vm.disposeVideoPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onBackPressedHandler() {
        //If the back press does not cause us to go into PIP then
        //close everything down
        if (vm.arcMediaPlayer != null && !vm.arcMediaPlayer!!.onBackPressed()) {
            cleanupVideoOnClose()
        }
    }

    companion object {

        private const val KEY = "id"

        @JvmStatic
        fun newInstance(id: String): PlayVideoFragment {
            val fragment = PlayVideoFragment()

            val args = Bundle()
            args.putString(KEY, id)
            fragment.arguments = args

            return fragment
        }
    }
}