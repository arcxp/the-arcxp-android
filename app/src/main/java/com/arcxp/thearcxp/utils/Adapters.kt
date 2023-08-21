package com.arcxp.thearcxp.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.thearcxp.databinding.GalleryViewItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryAdapter(
    private val context: Context,
    private val images: List<String?>,
    private val captions: List<String?>,
    private val titles: List<String?>
) :
    RecyclerView.Adapter<GalleryAdapter.MyViewHolder>() {

    override fun getItemCount() = images.size

    inner class MyViewHolder(val binding: GalleryViewItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.caption.text = captions[position]
        holder.binding.title.text = titles[position]

        Glide.with(context)
            .load(images[position])
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(spinner(context))
            .dontAnimate()
            .optionalFitCenter()
            .into(holder.binding.imageViewMain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        GalleryViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )
}
