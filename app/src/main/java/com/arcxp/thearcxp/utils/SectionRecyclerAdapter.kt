package com.arcxp.thearcxp.utils

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.extendedModels.*
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.ItemLayoutBinding
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


private const val FIRST_ITEM = 0
private const val NOT_FIRST_ITEM = 1

class SectionRecyclerAdapter(
    private val items: Map<Int, ArcXPCollection>,
    private val vm: MainViewModel
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FIRST_ITEM) {
            FirstViewHolder(
                binding = FirstItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            RemainingViewHolder(
                binding = ItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == FIRST_ITEM) {
            items[position]?.let {
                (holder as FirstViewHolder).bind(
                    item = it,
                )
            }
        } else {
            items[position]?.let {
                (holder as RemainingViewHolder).bind(
                    item = it
                )
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