package com.arcxp.thearcxp.tabfragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.extendedModels.*
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentSearchresultsBinding
import com.arcxp.thearcxp.databinding.ItemLayoutBinding
import com.arcxp.thearcxp.utils.AnsTypes
import com.arcxp.thearcxp.utils.spinner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

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
                        exit()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        query = arguments?.getString(QUERY_KEY)

        binding.backButton.setOnClickListener { exit() }

        createList()

        //we only want to trigger the initial load once, otherwise on revisit we are requesting more data without any scrolling
        if (lastLoaded == 0) {
            loadData()
        }
    }

    private fun createList() {
        binding.recycler.recycledViewPool.setMaxRecycledViews(1, 0)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = SearchRecyclerAdapter(results)

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (canRequestNextPagination) {
                    if ((binding.recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == results.size - 1) {
                        loadData()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.loading_results),
                            Toast.LENGTH_SHORT
                        )
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
        ArcXPContentSDK.contentManager().search(
            searchTerm = query!!,
            from = from,
            size = requireContext().resources.getInteger(R.integer.collection_page_size)
        ).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    onGetCollectionSuccess(response = it.success)
                }
                is Failure -> {
                    onError(error = it.failure)
                    binding.recycler.visibility = GONE
                    binding.noResults.visibility = VISIBLE
                }
            }
            binding.spin.visibility = GONE
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

            binding.recycler.visibility = VISIBLE
            binding.noResults.visibility = GONE

            binding.recycler.adapter?.notifyItemRangeChanged(
                lastLoaded + 1,
                response.keys.size
            )
            lastLoaded = response.keys.last()
        }

        if (results.isEmpty()) {
            binding.recycler.visibility = GONE
            binding.noResults.visibility = VISIBLE
        } else {
            binding.recycler.visibility = VISIBLE
            binding.noResults.visibility = GONE
        }

        binding.spin.visibility = GONE
    }

    private fun openStory(id: String) {
        exit()
        vm.openArticle(id)
    }

    private fun openVideo(id: String) {
        exit()
        vm.openVideo(id)
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            error = error,
            view = binding.root,
            viewId = R.id.collection_view_fragment
        )
    }

    inner class SearchRecyclerAdapter(
        private val results: Map<Int, ArcXPContentElement>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class SearchViewHolder(val binding: ItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            var itemId: String? = null
            fun bind(
                item: ArcXPContentElement
            ) {
                itemId = item._id
                binding.title.text = item.title()
                binding.description.text = item.description()
                binding.playIcon.visibility = GONE
                if (item.thumbnail().isNotEmpty()) {
                    Glide.with(itemView.context).load(item.thumbnail())
                        .error(Glide.with(itemView)
                            .load(item.fallback())
                            .error(R.drawable.ic_baseline_error_24)
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    if (item.type == AnsTypes.VIDEO.type) {
                                        binding.playIcon.visibility = VISIBLE
                                    }
                                    return false
                                }

                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ) = false
                            })
                            .apply(RequestOptions().transform(RoundedCorners(binding.root.resources.getInteger(R.integer.rounded_corner_radius))))
                        )
                        .placeholder(spinner(requireContext()))
                        .fitCenter()
                        .listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (item.type == AnsTypes.VIDEO.type) {
                                    binding.playIcon.visibility = VISIBLE
                                }
                                return false
                            }

                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ) = false
                        })
                        .apply(RequestOptions().transform(RoundedCorners(binding.root.resources.getInteger(R.integer.rounded_corner_radius))))
                        .into(binding.sectionThumbnail)
                } else {
                    binding.sectionThumbnail.visibility = GONE
                }
                if (binding.author.text.toString().isEmpty()) {
                    binding.author.visibility = GONE
                    binding.bullet.visibility = GONE
                } else {
                    binding.author.text = item.author()
                }
                if (item.publish_date == null) {
                    binding.bullet.visibility = GONE
                    binding.date.visibility = GONE
                } else {
                    binding.date.text = item.date()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SearchViewHolder).bind(results[position]!!)
            holder.itemView.setOnClickListener {
                if (results[position]!!.type == AnsTypes.VIDEO.type) {
                    openVideo(results[position]!!._id)
                } else {
                    openStory(results[position]!!._id)
                }
            }
        }

        override fun getItemCount(): Int {
            return results.size
        }
    }

    private fun exit() {
        (requireActivity() as MainActivity).clearSearch()
        parentFragmentManager.popBackStack()
    }

    companion object {

        private const val QUERY_KEY = "query"

        @JvmStatic
        fun create(query: String): SearchResultsFragment {
            val frag = SearchResultsFragment()
            val args = Bundle()
            args.putString(QUERY_KEY, query)
            frag.arguments = args
            return frag
        }
    }

}