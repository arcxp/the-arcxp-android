package com.arcxp.thearcxp.utils

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.content.sdk.models.ArcXPContentCallback
import com.arcxp.thearcxp.databinding.FirstItemLayoutBinding
import com.arcxp.thearcxp.databinding.ItemLayoutBinding
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
    private var listener: ArcXPContentCallback
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FirstViewHolder(val binding: FirstItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            id: String,
            title: String,
            details: String,
            image: String,
            author: String,
            date: String,
            listener: ArcXPContentCallback
        ) {
            binding.idTv.text = id
            binding.title1.text = title
            binding.description1.text = details
            binding.author.text = author
            binding.date.text = date
            Glide.with(itemView.context).load(image)
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
                listener.onClickResponse(binding.idTv.text.toString())
            }
        }
    }

    class RemainingViewHolder(val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            id: String,
            title: String,
            details: String,
            image: String,
            author: String,
            date: String,
            listener: ArcXPContentCallback
        ) {
            binding.idTv.text = id
            binding.title.text = title
            Glide.with(itemView.context).load(image)
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
                listener.onClickResponse(binding.idTv.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FIRST_ITEM) {
            val view =
                FirstItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FirstViewHolder(view)
        } else {
            val view =
                ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return RemainingViewHolder(view)
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
                date = dates[position] ?: "",
                listener = listener
            )
        } else {
            (holder as RemainingViewHolder).bind(
                id = ids[position] ?: "",
                title = titles[position] ?: "",
                details = details[position] ?: "",
                image = images[position] ?: "",
                author = authors[position] ?: "",
                date = dates[position] ?: "",
                listener = listener
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