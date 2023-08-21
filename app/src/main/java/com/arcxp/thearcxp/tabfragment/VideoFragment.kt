package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.models.ArcXPCollection
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.models.ArcXPContentSDKErrorType
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentVideoBinding
import com.arcxp.thearcxp.databinding.VideoFirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.VideoItemLayoutBinding
import com.arcxp.thearcxp.utils.spinner
import com.bumptech.glide.Glide

private const val FIRST_ITEM = 0
private const val NOT_FIRST_ITEM = 1

class VideoFragment : BaseFragment() {
    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    private val items = mutableMapOf<Int, ArcXPCollection>()

    private var canRequestNextPagination = true
    private var lastLoaded = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.recycledViewPool.setMaxRecycledViews(1, 0)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = VideoRecyclerAdapter(items)
        binding.spin.setImageDrawable(spinner(requireContext()))

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (canRequestNextPagination) {
                    if ((binding.recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == items.size - 1) {
                        loadData()
                        Toast.makeText(requireContext(), "Loading more videos", Toast.LENGTH_SHORT)
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

    private fun onError(error: ArcXPContentError) {
        //to ignore inevitable empty list error at end of collections
        if (error.localizedMessage.toString() != getString(R.string.empty_collection)) {
            showSnackBar(
                ArcXPContentError(error.type!!, error.localizedMessage),
                binding.videoViewFragment,
                R.id.video_view_fragment,
                true
            )
        } else {
            canRequestNextPagination = false
        }
    }


    private fun onGetVideosSuccess(response: Map<Int, ArcXPCollection>) {
        items.putAll(response)

        binding.recycler.adapter?.notifyItemRangeChanged(
            lastLoaded + 1,
            response.keys.last()
        )
        lastLoaded = response.keys.last()
        binding.spin.visibility = View.GONE
    }


    private fun loadData() {
        var from = 0
        if (lastLoaded > 0) from = lastLoaded + 1
        vm.getVideoCollection(
            from = from,
            size = requireContext().resources.getInteger(R.integer.collection_page_size)
        ).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    onGetVideosSuccess(response = it.success)
                }
                is Failure -> {
                    onError(it.failure)
                }
            }
        }
    }

    inner class VideoRecyclerAdapter(val items: Map<Int, ArcXPCollection>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class FirstViewHolder(val binding: VideoFirstItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            var itemId: String? = null
            fun bind(
                id: String,
                title: String,
                image: String?,
                author: String?,
                date: String
            ) {
                itemId = id
                binding.idTv.text = id
                binding.title1.text = title
                binding.author.text = author
                binding.date.text = date
                Glide.with(itemView.context).load(image)
                    .error(requireContext().resources.getDrawable(R.mipmap.ic_launcher)).fitCenter()
                    .into(binding.ivImageView1)
                if (author == null || author.isNullOrBlank()) {
                    binding.author.visibility = View.GONE
                    binding.bullet.visibility = View.GONE
                }
            }
        }

        inner class RemainingViewHolder(val binding: VideoItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            var itemId: String? = null

            fun bind(
                id: String,
                title: String,
                image: String?,
                author: String?,
                date: String
            ) {
                itemId = id
                binding.title.text = title
                Glide.with(itemView.context).load(image)
                    .error(requireContext().resources.getDrawable(R.mipmap.ic_launcher)).fitCenter()
                    .into(binding.videoImage)
                binding.author.text = author
                binding.date.text = date
                if (author == null || author.isNullOrBlank()) {
                    binding.author.visibility = View.GONE
                    binding.bullet.visibility = View.GONE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == FIRST_ITEM) {
                val view =
                    VideoFirstItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                FirstViewHolder(view)
            } else {
                val view =
                    VideoItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                RemainingViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val author = if (item!!.credits?.by == null || item.credits?.by?.isEmpty() == true) {
                ""
            } else {
                "By ${item.credits?.by!![0].name.toString()}"
            }
            val datestr = item.publishedDate!!.toLocaleString().split(":")
            val date = datestr[0].slice(0..datestr[0].length - 3)
            if (getItemViewType(position) == FIRST_ITEM) {
                (holder as FirstViewHolder).bind(
                    item.id,
                    item.headlines.basic ?: "",
                    item.promoItem?.basic?.url ?: "",
                    author,
                    date
                )
            } else {
                (holder as RemainingViewHolder).bind(
                    item.id,
                    item.headlines.basic ?: "",
                    item.promoItem?.basic?.url ?: "",
                    author,
                    date
                )
            }
            holder.itemView.setOnClickListener {
                val videoId = item.id

                if (videoId != null) {
                    (activity as MainActivity).openVideo(videoId)
                } else onError(
                    ArcXPContentError(
                        ArcXPContentSDKErrorType.INIT_ERROR,
                        getString(R.string.video_id_not_found)
                    )
                )
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                FIRST_ITEM
            } else {
                NOT_FIRST_ITEM
            }
        }
    }
}