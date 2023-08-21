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
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.models.ArcXPContentElement
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentArticleBinding
import com.arcxp.thearcxp.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
                contentType = "story",
                section = null,
                deviceType = "mobile"
            ).observe(viewLifecycleOwner) {
                if (!it.show) {
                    Paywall().show(requireFragmentManager(), getString(R.string.paywall))
                }
            }
        }
        binding.backButton.setOnClickListener {
            onBackPressedHandler()
        }
        binding.shareButton.setOnClickListener {
            Toast.makeText(requireContext(), "Share not implemented yet", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun containGalleries(storyResponse: ArcXPContentElement) =
        storyResponse.content_elements?.any { it.type == "gallery" }

    /**
     * Display the story contents.  Loop through each element in the response
     * and build a view for it.  Dynamically attach each view to the layout.
     */
    private fun displayStory(storyResponse: ArcXPContentElement) {
        binding.storyTitleTv.text = storyResponse.headlines?.basic
        if (storyResponse.subheadlines?.basic?.isNotEmpty() == true) {
            binding.storySubtitleTv.text = storyResponse.subheadlines?.basic
            binding.storySubtitleTv.visibility = VISIBLE
        }
        val containsGalleries = containGalleries(storyResponse = storyResponse)

        //if article contains a gallery, show first at top, else should show promo image
        if (containsGalleries == true) {
            val gallery = storyResponse.content_elements?.find { it.type == "gallery" }
            gallery?.let { gallery(gallery = it) }
        } else {
            val topImage =
                "${ArcXPContentSDK.arcxpContentConfig().baseUrl}${storyResponse.promoItem?.basic?.additional_properties?.thumbnailResizeUrl}"
            if (topImage.isNotBlank()) {
                Glide.with(this)
                    .load(topImage)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .dontAnimate()
                    .placeholder(spinner(this.requireContext()))
                    .into(binding.storyImageIv)
            }
        }

        storyResponse.credits?.by?.let {
            if (it.isNotEmpty()) {
                binding.storyDateTv.visibility = VISIBLE
                binding.storyAuthorTv.visibility = VISIBLE
                val authorText = "By ${storyResponse.credits?.by?.get(0)?.name}"
                binding.storyAuthorTv.text = authorText
                binding.storyDateTv.text = storyResponse.publish_date?.toLocaleString()
            }
        }
        var skipNextGallerySinceAlreadyDisplayed = containsGalleries
        //Building out story from content_elements
        storyResponse.content_elements?.forEach { it ->
            when (it.type) {
                AnsTypes.TEXT.type -> {
                    if (!it.content.isNullOrEmpty()) {
                        val textView = createTextView(it.content ?: "", requireContext())
                        textView.setLinkTextColor(Color.BLUE)
                        val wordsInContent = textView.text.split(" ")
                        wordsInArticle += wordsInContent.size
                        binding.innerLayout.addView(textView)
                        textView.movementMethod = LinkMovementMethod.getInstance()
                    }
                }
                AnsTypes.LINK.type -> {
                    val textView =
                        createTextView(
                            "[ <a href=${it.url} target=_blank>${it.content}</a> ]",
                            requireContext()
                        )
                    binding.innerLayout.addView(textView)
                    textView.movementMethod = LinkMovementMethod.getInstance()
                }
                AnsTypes.IMAGE.type -> {
                    val imageAndCaption = createImageView(
                        url = "${
                            ArcXPContentSDK.arcxpContentConfig().baseUrl
                        }${it.additional_properties?.thumbnailResizeUrl}",
                        caption = it.caption.toString(),
                        activity = requireActivity()
                    )
                    binding.innerLayout.addView(imageAndCaption.first)
                    if (!it.caption.isNullOrEmpty()) {
                        binding.innerLayout.addView((imageAndCaption.second))
                    }
                }
                AnsTypes.VIDEO.type -> {
                    val player = ArcMediaPlayer.createPlayer(requireActivity())
                    binding.innerLayout.addView(
                        createVideoView(
                            content = it,
                            activity = requireActivity(),
                            player
                        )
                    )
                    arcMediaPlayers.add(player)
                }
                AnsTypes.GALLERY.type -> {
                    //if we have a gallery, it is already shown at top, so no need to show it twice
                    if (skipNextGallerySinceAlreadyDisplayed == true) {
                        skipNextGallerySinceAlreadyDisplayed = false
                    } else {
                        gallery(gallery = it)
                    }
                }

            }
        }

        binding.spin.visibility = GONE
    }

    /**
     * creates a gallery viewpager2 and associated tabLayout, adds to inner layout
     *
     * @param gallery
     */
    private fun gallery(gallery: ArcXPContentElement) {
        val imageUrls = ArrayList<String?>()
        val titles = ArrayList<String?>()
        val captions = ArrayList<String?>()
        gallery.content_elements?.forEach {
            imageUrls.add(it.url)
            titles.add(it.subtitle)
            captions.add(it.caption)
        }
        val viewPager = createGalleryView(
            images = imageUrls,
            context = requireContext(),
            captions = captions,
            titles = titles
        )

        val tabLayout = TabLayout(requireContext())
        tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
        tabLayout.setSelectedTabIndicatorHeight(0) //TODO find non deprecated way to do this

        binding.innerLayout.addView(viewPager)
        binding.innerLayout.addView(tabLayout)

        TabLayoutMediator(tabLayout, viewPager) { tab, _ -> // Styling each tab here
            tab.setIcon(R.drawable.gallery_tab_layout_indicator_selector)
            val paddingSize = requireContext().resources.getInteger(R.integer.gallery_tab_padding)
            tab.view.setPadding(paddingSize, 0, paddingSize, 0)
        }.attach()
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.root,
            R.id.loginCL,
            true
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