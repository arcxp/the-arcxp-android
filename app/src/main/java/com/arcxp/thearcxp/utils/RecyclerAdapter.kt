package com.arcxp.thearcxp.utils

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.ItemLayoutBinding
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.bumptech.glide.Glide

private const val FIRST_ITEM = 0
private const val NOT_FIRST_ITEM = 1

class RecyclerAdapter(
    private val ids: Map<Int, String>,
    private val titles: Map<Int, String>,
    private val details: Map<Int, String>,
    private val images: Map<Int, String>,
    private val authors: Map<Int, String>,
    private val dates: Map<Int, String>,
    private val vm: MainViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FirstViewHolder(val binding: FirstItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            id: String,
            title: String,
            details: String,
            image: String,
            author: String,
            date: String
        ) {
            binding.idTv.text = id
            binding.title1.text = title
            binding.description1.text = details
            binding.author.text = author
            binding.date.text = date
            Glide.with(itemView.context).load(image)
                .error(R.drawable.ic_baseline_error_24_black)
                .placeholder(spinner(itemView.context))
                .centerInside()
                .into(binding.ivImageView1)
            if (details.isEmpty()) {
                binding.description1.visibility = GONE
            }
            if (author.isEmpty()) {
                binding.author.visibility = GONE
                binding.date.visibility = GONE
                binding.bullet.visibility = GONE
            }
            itemView.setOnClickListener {
                vm.openArticle(id = binding.idTv.text.toString())
            }
        }
    }

    inner class RemainingViewHolder(val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            id: String,
            title: String,
            details: String,
            image: String,
            author: String,
            date: String
        ) {
            binding.idTv.text = id
            binding.title.text = title
            Glide.with(itemView.context).load(image)
                .error(R.drawable.ic_baseline_error_24_black)
                .placeholder(spinner(itemView.context))
                .fitCenter()
                .into(binding.ivImageView)
            if (details.isEmpty()) {
                binding.description.visibility = GONE
            } else {
                binding.description.text = details
            }
            if (author.isEmpty()) {
                binding.author.visibility = GONE
                binding.date.visibility = GONE
                binding.bullet.visibility = GONE
            } else {
                binding.author.text = author
                binding.date.text = date
            }
            itemView.setOnClickListener {
                vm.openArticle(id = binding.idTv.text.toString())
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
            (holder as FirstViewHolder).bind(
                id = ids[position] ?: "",
                title = titles[position] ?: "",
                details = details[position] ?: "",
                image = images[position] ?: "",
                author = authors[position] ?: "",
                date = dates[position] ?: ""
            )
        } else {
            (holder as RemainingViewHolder).bind(
                id = ids[position] ?: "",
                title = titles[position] ?: "",
                details = details[position] ?: "",
                image = images[position] ?: "",
                author = authors[position] ?: "",
                date = dates[position] ?: ""
            )
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            FIRST_ITEM
        } else {
            NOT_FIRST_ITEM
        }
    }
}