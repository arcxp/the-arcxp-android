package com.arcxp.thearcxp.tabfragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.models.ArcXPCollection
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentVideoBinding
import com.arcxp.thearcxp.databinding.VideoFirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.VideoItemLayoutBinding
import com.arcxp.thearcxp.utils.imageUrl
import com.arcxp.thearcxp.utils.spinner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

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
        if (error.message != getString(R.string.empty_collection)) {
            showSnackBar(
                error = error,
                view = binding.videoViewFragment,
                viewId = R.id.video_view_fragment
            )
        } else {
            canRequestNextPagination = false
        }
    }


    private fun onGetVideosSuccess(response: Map<Int, ArcXPCollection>) {
        items.putAll(response)

        binding.recycler.adapter?.notifyItemRangeChanged(
            lastLoaded + 1,
            response.keys.size
        )
        lastLoaded = response.keys.last()
        binding.spin.visibility = GONE
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
                    onError(error = it.failure)
                }
            }
        }
    }

    inner class VideoRecyclerAdapter(private val items: Map<Int, ArcXPCollection>) :
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
                binding.playIcon.visibility = GONE
                Glide.with(itemView.context).load(image)
                    .error(R.drawable.ic_baseline_error_24_black)
//                    .placeholder(spinner(itemView.context)) //TODO this is making hero image not visible
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.playIcon.visibility = VISIBLE
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ) = false

                    })
                    .fitCenter()
                    .into(binding.ivImageView1)
                if (author == null || author.isNullOrBlank()) {
                    binding.author.visibility = GONE
                    binding.bullet.visibility = GONE
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
                binding.playIcon.visibility = GONE
                Glide.with(itemView.context).load(image)
                    .error(R.drawable.ic_baseline_error_24_black)
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
                            binding.playIcon.visibility = VISIBLE
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ) = false
                    })
                    .into(binding.videoImage)

                binding.author.text = author
                binding.date.text = date
                if (author == null || author.isNullOrBlank()) {
                    binding.author.visibility = GONE
                    binding.bullet.visibility = GONE
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
            item?.let {
                val author = if (it.credits?.by?.isNotEmpty() == true)
                    "By ${it.credits!!.by!![0].name}" else ""
                val dateString = it.publishedDate?.toLocaleString()?.split(":")
                val date = if (dateString?.isNotEmpty() == true)
                    dateString[0].slice(0..dateString[0].length - 3) else ""
                if (getItemViewType(position) == FIRST_ITEM) {
                    (holder as FirstViewHolder).bind(
                        id = it.id,
                        title = it.headlines.basic ?: "",
                        image = it.imageUrl(),
                        author = author,
                        date = date
                    )
                } else {
                    (holder as RemainingViewHolder).bind(
                        id = it.id,
                        title = it.headlines.basic ?: "",
                        image = it.imageUrl(),
                        author = author,
                        date = date
                    )
                }
                holder.itemView.setOnClickListener {
                    vm.openVideo(id = item.id)
                }
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