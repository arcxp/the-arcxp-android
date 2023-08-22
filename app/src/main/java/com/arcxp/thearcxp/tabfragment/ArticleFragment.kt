package com.arcxp.thearcxp.tabfragment

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.arc.arcvideo.ArcMediaPlayer
import com.arc.arcvideo.ArcVideoStreamCallback
import com.arc.arcvideo.model.ArcVideoResponse
import com.arc.arcvideo.model.ArcVideoSDKErrorType
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.model.VideoVO
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.content.sdk.extendedModels.*
import com.arcxp.content.sdk.models.*
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentArticleBinding
import com.arcxp.thearcxp.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Displays an article in a fragment
 */
class ArticleFragment : BaseFragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    var wordsInArticle = 0.0
    lateinit var id: String
    private val arcMediaPlayers = mutableListOf<ArcMediaPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Capture onBackPressed().  This is required if the article contains
        //a video so that the video player can be shut down properly.  It also
        //pops the back stack to remove the article.
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressedHandler()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.spin.setImageDrawable(spinner(requireContext()))
        id = requireArguments().getString(KEY, "")

        //Retrieve the story from the Content SDK
        vm.getStory(id = id).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> displayStory(storyResponse = it.success)
                is Failure -> {
                    //TODO show error
                }
            }
        }

        if (ArcXPCommerceSDK.isInitialized()) {
            //Check to see if the paywall needs to be triggered
            vm.evaluateForPaywall(
                id = id,
                contentType = AnsTypes.STORY.type,
                section = null,
                deviceType = getString(R.string.device_type)
            ).observe(viewLifecycleOwner) {
                if (!it.show) {
                    Paywall().show(parentFragmentManager, getString(R.string.paywall))
                }
            }
        }
        binding.backButton.setOnClickListener {
            onBackPressedHandler()
        }
        binding.shareButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.share_message), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun containGalleries(storyResponse: ArcXPStory) =
        storyResponse.content_elements?.any { it is Gallery } ?: false

    /**
     * Display the story contents.  Loop through each element in the response
     * and build a view for it.  Dynamically attach each view to the layout.
     */
    private fun displayStory(storyResponse: ArcXPStory) {
        binding.storyTitle.text = storyResponse.title()
        if (storyResponse.subheadlines().isNotEmpty()) {
            binding.storySubtitle.text = storyResponse.subheadlines()
            binding.storySubtitle.visibility = VISIBLE
        }
        val containsGalleries = containGalleries(storyResponse = storyResponse)

        //if article contains a gallery, show first at top, else should show promo image
        if (containsGalleries) {
            val gallery = storyResponse.content_elements?.find { it is Gallery }
            gallery?.let { gallery(gallery = it as Gallery) }
        } else {

            if (storyResponse.imageUrl().isNotEmpty()) {
                Glide.with(this)
                    .load(storyResponse.imageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .dontAnimate()
                    .error(Glide.with(this)
                        .load(storyResponse.fallback())
                        .error(R.drawable.ic_baseline_error_24)
                        .apply(RequestOptions().transform(RoundedCorners(requireContext().resources.getInteger(R.integer.rounded_corner_radius)))))
                    .placeholder(spinner(requireContext()))
                    .apply(RequestOptions().transform(FitCenter(), RoundedCorners(requireContext().resources.getInteger(R.integer.rounded_corner_radius))))
                    .into(binding.storyTopImage)
            }
        }

        storyResponse.credits?.by?.let {
            if (storyResponse.author().isNotEmpty()) {
                binding.storyDate.visibility = VISIBLE
                binding.storyAuthor.visibility = VISIBLE
                binding.storyAuthor.text = "By ${storyResponse.author()}"
                binding.storyDate.text = storyResponse.date()
            }
        }
        var skipNextGallerySinceAlreadyDisplayed = containsGalleries
        //Building out story from content_elements
        storyResponse.content_elements?.forEach { it ->
            when (it) {
                is Text -> {
                    if (!it.content.isNullOrEmpty()) {
                        val textView = createTextView(it.content ?: "", requireContext())
                        textView.setLinkTextColor(Color.BLUE)
                        val wordsInContent = textView.text.split(" ")
                        wordsInArticle += wordsInContent.size
                        binding.innerLayout.addView(textView)
                        textView.movementMethod = LinkMovementMethod.getInstance()
                    }
                }
                is InterstitialLink -> {
                    val textView =
                        createTextView(
                            "[ <a href=${it.url} target=_blank>${it.content}</a> ]",
                            requireContext()
                        )
                    binding.innerLayout.addView(textView)
                    textView.movementMethod = LinkMovementMethod.getInstance()
                }
                is Image -> {
                    val imageAndCaption = createImageView(
                        item = it,
                        caption = it.caption.toString(),
                        activity = requireActivity()
                    )
                    binding.innerLayout.addView(imageAndCaption.first)
                    if (!it.caption.isNullOrEmpty()) {
                        binding.innerLayout.addView((imageAndCaption.second))
                    }
                }
                is Video -> {
                    if (it._id != null) {
                        val player = ArcMediaPlayer.createPlayer(requireActivity())
                        binding.innerLayout.addView(
                            createVideoView(
                                activity = requireActivity(),
                                arcMediaPlayer = player
                            )
                        )
                        arcMediaPlayers.add(player)

                        vm.videoClient.findByUuid(
                            uuid = it._id!!,
                            listener = object : ArcVideoStreamCallback {
                                override fun onVideoResponse(arcVideoResponse: ArcVideoResponse?) {
                                }

                                override fun onVideoStream(videos: List<ArcVideoStream>?) {
                                    if (videos?.isNotEmpty() == true) {
                                        player.initMedia(videos[0])
                                        player.displayVideo()
                                        player.pause()
                                    }
                                }

                                override fun onLiveVideos(videos: List<VideoVO>?) {}

                                override fun onError(
                                    type: ArcVideoSDKErrorType,
                                    message: String,
                                    value: Any?
                                ) {
                                    onError(
                                        ArcXPContentError(
                                            ArcXPContentSDKErrorType.SERVER_ERROR,
                                            message,
                                            value
                                        )
                                    )
                                }

                            })
                    }
                }
                is Gallery -> {
                    //if we have a gallery, it is already shown at top, so no need to show it twice
                    if (skipNextGallerySinceAlreadyDisplayed == true) {
                        skipNextGallerySinceAlreadyDisplayed = false
                    } else {
                        gallery(gallery = it)
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
            }
        }

        binding.spin.visibility = GONE
    }

    /**
     * creates a gallery viewpager2 and associated tabLayout, adds to inner layout
     *
     * @param gallery
     */
    private fun gallery(gallery: Gallery) {
        val images = ArrayList<Image>()
        gallery.content_elements?.forEach {
            it as Image
            images.add(it)
        }
        val viewPager = createGalleryView(
            images = images,
            context = requireContext()
        )

        val tabLayout = TabLayout(requireContext())
        tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
        tabLayout.setSelectedTabIndicatorHeight(0) //TODO find non deprecated way to do this

        binding.innerLayout.addView(viewPager)
        binding.innerLayout.addView(tabLayout)

        TabLayoutMediator(tabLayout, viewPager) { tab, _ -> // Styling each tab here
            tab.setIcon(R.drawable.gallery_tab_layout_indicator_selector)
            val paddingSize =
                requireContext().resources.getInteger(R.integer.gallery_tab_padding)
            tab.view.setPadding(paddingSize, 0, paddingSize, 0)
        }.attach()
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            error = error,
            view = binding.root,
            viewId = R.id.loginCL
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onBackPressedHandler() {
        arcMediaPlayers.forEach {
            if (!it.onBackPressed()) {
                it.finish()
            }
        }
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {
        private const val KEY = "id"

        @JvmStatic
        fun newInstance(id: String): ArticleFragment {
            val articleFragment = ArticleFragment()
            val args = Bundle()
            args.putString(KEY, id)
            articleFragment.arguments = args
            return articleFragment
        }
    }
}