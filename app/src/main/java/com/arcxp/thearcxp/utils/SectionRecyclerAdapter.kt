package com.arcxp.thearcxp.utils

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.extendedModels.*
import com.arcxp.content.models.Taxonomy
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.AdItemLayoutBinding
import com.arcxp.thearcxp.databinding.FirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.ItemLayoutBinding
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


private const val FIRST_ITEM = 0
private const val NOT_FIRST_ITEM = 1
private const val AD_ITEM = 2

class SectionRecyclerAdapter(
    private val items: Map<Int, Any?>,
    private val vm: MainViewModel,
    private val fragment: Fragment
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FirstViewHolder(val binding: FirstItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: ArcXPCollection,
        ) {
            binding.sectionHeroArticleId.text = item.id
            binding.sectionListHeroTitle.text = item.title()
            binding.sectionListHeroDescription.text = item.description()
            binding.author.text = item.author()
            binding.date.text = item.date()
            if (item.imageUrl().isNotEmpty()) {
                Glide.with(itemView).load(item.imageUrl())
                    .error(
                        if (item.fallback().isNotEmpty()) {
                            Glide.with(itemView).load(item.fallback())
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
                        } else {
                            binding.sectionListHeroImage.visibility = GONE
                        }
                    )
                    .placeholder(spinner(itemView.context))
                    .apply(RequestOptions().transform(RoundedCorners(itemView.resources.getInteger(R.integer.rounded_corner_radius))))
                    .into(binding.sectionListHeroImage)
            } else {
                binding.sectionListHeroImage.visibility = GONE
            }
            if (item.description().isEmpty()) {
                binding.sectionListHeroDescription.visibility = GONE
            }
            if (item.author().isEmpty()) {
                binding.author.visibility = GONE
                binding.date.visibility = GONE
                binding.bullet.visibility = GONE
            }
            itemView.setOnClickListener {
                vm.openArticle(id = binding.sectionHeroArticleId.text.toString())
            }
        }
    }

    inner class RemainingViewHolder(val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: ArcXPCollection
        ) {
            binding.sectionHeroArticleId.text = item.id
            binding.title.text = item.title()
            if (item.thumbnail().isNotEmpty()) {
                Glide.with(itemView.context).load(item.thumbnail())
                    .error(R.drawable.ic_baseline_error_24)
                    .placeholder(spinner(itemView.context))
                    .optionalCenterInside()
                    .apply(RequestOptions().transform(RoundedCorners(itemView.resources.getInteger(R.integer.rounded_corner_radius_thumbnail))))
                    .into(binding.sectionThumbnail)
            } else {
                binding.sectionThumbnail.visibility = GONE
                val params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
                params.guidePercent = 1f
                binding.guideline.layoutParams = params
            }
            if (item.description().isEmpty()) {
                binding.description.visibility = GONE
            } else {
                binding.description.text = item.description()
            }
            if (item.author().isEmpty()) {
                binding.author.visibility = GONE
                binding.bullet.visibility = GONE
            } else {
                binding.author.text = item.author()
            }
            if (item.date().isEmpty()) {
                binding.date.visibility = GONE
                binding.bullet.visibility = GONE
            } else {
                binding.date.text = item.date()
            }
            itemView.setOnClickListener {
                vm.openArticle(id = binding.sectionHeroArticleId.text.toString())
            }
        }
    }

    inner class AdViewHolder(val binding: AdItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Taxonomy?
        ) {
            createNativeAdView(fragment.requireActivity(), binding.adFrame, item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FIRST_ITEM) {
            return FirstViewHolder(
                binding = FirstItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if (viewType == NOT_FIRST_ITEM) {
            return RemainingViewHolder(
                binding = ItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return AdViewHolder(
                binding = AdItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == FIRST_ITEM) {
            items[position]?.let {
                (holder as FirstViewHolder).bind(
                    item = it as ArcXPCollection
                )
            }
        } else if (getItemViewType(position) == NOT_FIRST_ITEM) {
            items[position]?.let {
                (holder as RemainingViewHolder).bind(
                    item = it as ArcXPCollection
                )
            }
        } else {
            items[position]?.let {
                (holder as AdViewHolder).bind(
                    item = it as Taxonomy
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return FIRST_ITEM
        } else {
            return if (items[position] is Taxonomy || items[position] == null) {
                AD_ITEM
            } else {
                NOT_FIRST_ITEM
            }
        }
    }
}