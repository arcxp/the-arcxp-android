package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.models.ArcXPCollection
import com.arcxp.content.sdk.models.ArcXPContentCallback
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.models.ArcXPSection
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentSectionBinding
import com.arcxp.thearcxp.utils.RecyclerAdapter
import com.arcxp.thearcxp.utils.getNameToUseFromSection
import com.arcxp.thearcxp.utils.spinner


class SectionFragment : BaseSectionFragment() {

    private var _binding: FragmentSectionBinding? = null
    private val binding get() = _binding!!
    private var sectionName: String? = null
    private var path: String? = null
    private val titles = mutableMapOf<Int, String>()
    private val details = mutableMapOf<Int, String>()
    private val images = mutableMapOf<Int, String>()
    private val ids = mutableMapOf<Int, String>()
    private val authors = mutableMapOf<Int, String>()
    private val dates = mutableMapOf<Int, String>()

    private var canRequestNextPagination = true
    private var lastLoaded = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAdapter()

        sectionName = arguments?.getString(SECTION_NAME_KEY)
        path = arguments?.getString(PATH_NAME_KEY)

        if (!path.isNullOrEmpty()) {
            path = path!!.replace("/", "")
        } else {
            path = "/"
        }

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (canRequestNextPagination) {
                    if ((binding.recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == ids.size - 1) {
                        loadData()
                        Toast.makeText(requireContext(), "Loading more stories", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })

        //we only want to trigger the initial load once, otherwise on revisit we are requesting more data without any scrolling
        if (lastLoaded == 0) {
            loadData()
        }
    }

    private fun loadData() {
        binding.spin.setImageDrawable(spinner(requireContext()))
        var from = 0
        if (lastLoaded > 0) from = lastLoaded + 1
        vm.getCollection(
            id = path!!,
            from = from,
            size = requireContext().resources.getInteger(R.integer.collection_page_size)
        ).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    onGetCollectionSuccess(response = it.success)
                }
                is Failure -> {
                    onError(it.failure)
                }
            }
            binding.spin.visibility = GONE
        }
    }

    override fun getSectionName(): String? {
        return sectionName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onError(error: ArcXPContentError) {
        //to ignore inevitable empty list error at end of collections
        if (error.localizedMessage.toString() != getString(R.string.empty_collection)) {
            showSnackBar(
                ArcXPContentError(error.type!!, error.localizedMessage),
                binding.recycler,
                R.id.collection_view_fragment,
                true
            )
        } else {
            canRequestNextPagination = false
        }
    }

    private fun addToMap(
        index: Int,
        id: String,
        title: String,
        description: String,
        image: String,
        author: String,
        date: String
    ) {
        ids[index] = id
        titles[index] = title
        details[index] = description
        images[index] = image
        authors[index] = author
        dates[index] = date
    }

    private fun onGetCollectionSuccess(response: Map<Int, ArcXPCollection>) {
        response.forEach {
            val date = it.value.publishedDate?.toLocaleString()?.split(":")
            var img = "N/A"
            if (it.value.promoItem?.basic?.additional_properties?.thumbnailResizeUrl?.isEmpty() == false) {
                img =
                    "${ArcXPContentSDK.arcxpContentConfig().baseUrl}${it.value.promoItem?.basic?.additional_properties?.thumbnailResizeUrl}"
            }
            addToMap(
                index = it.key,
                id = it.value.id,
                title = it.value.headlines.basic ?: "",
                description = it.value.description?.basic ?: "",
                image = img,
                author = if (it.value.credits?.by.isNullOrEmpty()) {
                    ""
                } else {
                    "By ${it.value.credits?.by!![0].name}"
                },
                date = date?.let { ourDate -> ourDate[0].slice(0..ourDate[0].length - 3) }
                    ?: ""
            )
        }

        binding.recycler.adapter?.notifyItemRangeChanged(
            lastLoaded + 1,
            response.keys.last()
        )
        lastLoaded = response.keys.last()
    }

    private fun initializeAdapter() {
        binding.recycler.recycledViewPool.setMaxRecycledViews(1, 0)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = RecyclerAdapter(
            ids,
            titles,
            details,
            images,
            authors,
            dates,
            listener = object : ArcXPContentCallback {
                override fun onClickResponse(_id: String) {
                    openStory(_id)
                }
            }
        )
    }

    private fun openStory(id: String) {
        (activity as MainActivity).openArticle(id)
    }

    companion object {

        private const val SECTION_NAME_KEY = "name"
        private const val PATH_NAME_KEY = "id"

        @JvmStatic
        fun create(section: ArcXPSection): SectionFragment {
            val frag = SectionFragment()
            val args = Bundle()
            args.putString(PATH_NAME_KEY, section.id)
            args.putString(SECTION_NAME_KEY, section.getNameToUseFromSection())
            frag.arguments = args
            return frag
        }
    }

}