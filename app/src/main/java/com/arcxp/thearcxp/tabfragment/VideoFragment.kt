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
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.content.extendedModels.*
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.FragmentVideoBinding
import com.arcxp.thearcxp.databinding.ItemLayoutBinding
import com.arcxp.thearcxp.utils.spinner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
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
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.pagination_toast_message_video),
                            Toast.LENGTH_SHORT
                        )
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

    private fun onError(error: ArcXPException) {
        //to ignore inevitable empty list error at end of collections
        if (error.message != getString(R.string.empty_collection)) {
            showSnackBar(
                error = error,
                view = binding.recycler,
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

        inner class FirstViewHolder(val binding: FirstItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            var itemId: String? = null
            fun bind(
                item: ArcXPCollection
            ) {
                itemId = item.id
                binding.sectionHeroArticleId.text = item.id
                binding.sectionListHeroTitle.text = item.title()
                binding.playIcon.visibility = GONE
                if (item.thumbnail().isEmpty()) {
                    binding.sectionListHeroImage.visibility = GONE
                } else {
                    Glide.with(itemView.context).load(item.imageUrl())
                        .error(
                            Glide.with(itemView.context).load(item.fallback())
                                .error(R.drawable.ic_baseline_error_24)
                                .apply(
                                    RequestOptions().transform(
                                        RoundedCorners(
                                            itemView.resources.getInteger(
                                                R.integer.rounded_corner_radius
                                            )
                                        )
                                    )
                                )
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
                        )
                        .fitCenter()
                        .apply(
                            RequestOptions().transform(
                                RoundedCorners(
                                    itemView.resources.getInteger(
                                        R.integer.rounded_corner_radius
                                    )
                                )
                            )
                        )
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
                        .into(binding.sectionListHeroImage)
                }
                if (item.author().isEmpty()) {
                    binding.author.visibility = GONE
                    binding.bullet.visibility = GONE
                } else {
                    binding.author.text = item.author()
                }
                if (item.date().isEmpty()) {
                    binding.bullet.visibility = GONE
                    binding.date.visibility = GONE
                } else {
                    binding.date.text = item.date()
                }
            }
        }

        inner class RemainingViewHolder(val binding: ItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            var itemId: String? = null

            fun bind(
                item: ArcXPCollection
            ) {
                itemId = item.id
                binding.title.text = item.title()
                binding.playIcon.visibility = GONE
                if (item.thumbnail().isNullOrEmpty()) {
                    binding.sectionThumbnail.visibility = GONE
                } else {
                    Glide.with(itemView.context).load(item.thumbnail())
                        .error(
                            Glide.with(itemView.context).load(item.fallback())
                                .error(R.drawable.ic_baseline_error_24)
                                .apply(
                                    RequestOptions().transform(
                                        RoundedCorners(
                                            itemView.resources.getInteger(
                                                R.integer.rounded_corner_radius_thumbnail
                                            )
                                        )
                                    )
                                )
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
                        )
                        .placeholder(spinner(requireContext()))
                        .fitCenter()
                        .apply(
                            RequestOptions().transform(
                                RoundedCorners(
                                    itemView.resources.getInteger(
                                        R.integer.rounded_corner_radius_thumbnail
                                    )
                                )
                            )
                        )
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
                        .into(binding.sectionThumbnail)
                }
                if (item.author().isEmpty()) {
                    binding.author.visibility = GONE
                    binding.bullet.visibility = GONE
                } else {
                    binding.author.text = item.author()
                }
                if (item.date().isEmpty()) {
                    binding.bullet.visibility = GONE
                    binding.date.visibility = GONE
                } else {
                    binding.date.text = item.date()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == FIRST_ITEM) {
                FirstViewHolder(
                    FirstItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            } else {
                RemainingViewHolder(
                    ItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            item?.let {
                if (getItemViewType(position) == FIRST_ITEM) {
                    (holder as FirstViewHolder).bind(
                        item = it
                    )
                } else {
                    (holder as RemainingViewHolder).bind(
                        item = it
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