package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.models.ArcXPContentElement
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.util.Either
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentSearchresultsBinding
import com.arcxp.thearcxp.databinding.SearchItemLayoutBinding
import com.arcxp.thearcxp.utils.spinner
import com.bumptech.glide.Glide

class SearchResultsFragment : BaseFragment() {

    private var _binding: FragmentSearchresultsBinding? = null
    private val binding get() = _binding!!
    private val results = mutableMapOf<Int, ArcXPContentElement>()

    private var canRequestNextPagination = true
    private var lastLoaded = 0

    private var query: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (this@SearchResultsFragment.isVisible) {
                        (requireActivity() as MainActivity).clearSearch()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchresultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    //@ExperimentalPagerApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        query = arguments?.getString("query")

        binding.backButton.setOnClickListener {
            (requireActivity() as MainActivity).clearSearch()
            requireActivity().supportFragmentManager.popBackStack()
        }

        createList()

        //we only want to trigger the initial load once, otherwise on revisit we are requesting more data without any scrolling
        if (lastLoaded == 0) {
            loadData()
        }
    }

    fun createList() {
        binding.recycler.recycledViewPool.setMaxRecycledViews(1, 0)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = SearchRecyclerAdapter(results)

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (canRequestNextPagination) {
                    if ((binding.recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == results.size - 1) {
                        loadData()
                        Toast.makeText(requireContext(), "Loading results stories", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    private fun loadData() {
        binding.spin.setImageDrawable(spinner(requireContext()))
        var from = 0
        if (lastLoaded > 0) from = lastLoaded + 1
        ArcXPContentSDK.contentManager().searchByKeyword(
            query!!,
            from = from,
            size = requireContext().resources.getInteger(R.integer.collection_page_size)
        ).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    onGetCollectionSuccess(response = it.success)
                }
                is Failure -> {
                    onError(it.failure)
                    binding.recycler.visibility = View.GONE
                    binding.noResults.visibility = View.VISIBLE
                }
            }
            binding.spin.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onGetCollectionSuccess(response: Map<Int, ArcXPContentElement>) {
        canRequestNextPagination = response.isNotEmpty()
        response.forEach {
            results[it.key] = it.value
        }

        if (response.isNotEmpty()) {

            binding.recycler.visibility = View.VISIBLE
            binding.noResults.visibility = View.GONE

            binding.recycler.adapter?.notifyItemRangeChanged(
                lastLoaded + 1,
                response.keys.last()
            )
            lastLoaded = response.keys.last()
        }

        if (results.isEmpty()) {
            binding.recycler.visibility = View.GONE
            binding.noResults.visibility = View.VISIBLE
        } else {
            binding.recycler.visibility = View.VISIBLE
            binding.noResults.visibility = View.GONE
        }

        binding.spin.visibility = View.GONE
    }

    private fun openStory(id: String) {
        (activity as MainActivity).openArticle(id)
    }

    private fun openVideo(id: String) {
        (activity as MainActivity).openVideo(id)
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.root,
            R.id.collection_view_fragment,
            true
        )
    }

    inner class SearchRecyclerAdapter(
        private val results: Map<Int, ArcXPContentElement>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class SearchViewHolder(val binding: SearchItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            var itemId: String? = null
            fun bind(
                item: ArcXPContentElement
            ) {
                itemId = item._id
                binding.title.text = if (item.headlines != null) {
                    item.headlines?.basic
                } else {
                    "No title"
                }
                binding.description.text = if (item.content != null) {
                    item.content
                } else {
                    ""
                }
                binding.author.text = if (item.credits != null
                    && item.credits?.by != null && item.credits?.by?.isNotEmpty()!!
                ) {
                    item.credits?.by?.get(0)?.name!!
                } else {
                    ""
                }
                binding.date.text = if (item.publish_date != null) {
                    item.publish_date.toString().slice(0..item.publish_date.toString().length - 3)
                } else {
                    ""
                }
                Glide.with(itemView.context).load(item.promoItem?.basic?.url)
                    .error(requireContext().resources.getDrawable(R.mipmap.ic_launcher)).fitCenter()
                    .into(binding.videoImage)

                if (item.type != null &&  item.type == "video") {
                    binding.playIcon.visibility = View.VISIBLE
                }
                if (binding.author.text.toString().isEmpty()) {
                    binding.author.visibility = View.GONE
                    binding.bullet.visibility = View.GONE
                }
                if (item.publish_date == null) {
                    binding.bullet.visibility = View.GONE
                    binding.date.visibility = View.GONE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                SearchItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SearchViewHolder).bind(results[position]!!)
            holder.itemView.setOnClickListener {
                if (results[position]!!.type.equals("video")) {
                    openVideo(results[position]!!._id!!)
                } else {
                    openStory(results[position]!!._id!!)
                }
            }
        }

        override fun getItemCount(): Int {
            return results.size
        }
    }

    companion object {
        @JvmStatic
        fun create(query: String): SearchResultsFragment {
            val frag = SearchResultsFragment()
            val args = Bundle()
            args.putString("query", query)
            frag.arguments = args
            return frag
        }
    }

}