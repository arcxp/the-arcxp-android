package com.arcxp.thearcxp.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.commons.util.Utils.fallback
import com.arcxp.content.models.Image
import com.arcxp.content.models.imageUrl
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.GalleryViewItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class GalleryAdapter(
    private val context: Context,
    private val images: List<Image>,
) :
    RecyclerView.Adapter<GalleryAdapter.MyViewHolder>() {


    override fun getItemCount() = images.size

    inner class MyViewHolder(val binding: GalleryViewItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.caption.text = images[position].caption
        holder.binding.title.text = images[position].subtitle

        Glide.with(context)
            .load(images[position].imageUrl())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .error(Glide.with(context)
                .load(images[position].fallback())
                .error(R.drawable.ic_baseline_error_24)
                .apply(RequestOptions().transform(RoundedCorners(context.resources.getInteger(R.integer.rounded_corner_radius))))
            )
            .placeholder(spinner(context))
            .dontAnimate()
            .optionalFitCenter()
            .apply(RequestOptions().transform(RoundedCorners(context.resources.getInteger(R.integer.rounded_corner_radius))))
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
