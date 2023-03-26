package com.side.project.video.ui.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.side.project.video.R
import com.side.project.video.databinding.ItemVideoThumbnailBinding
import com.side.project.video.utils.AsyncImageLoader
import com.side.project.video.utils.Method

class ThumbnailAdapter(private val count: Int)
    : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
    private val thumbnails: MutableList<Bitmap?> = MutableList(count) { null }

    fun addThumbnail(thumbnail: Bitmap, position: Int) {
        thumbnails[position] = thumbnail
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thumbnail = thumbnails[position] ?: return
        holder.binding.run {
            AsyncImageLoader.loadImage(imgThumbnail, thumbnail)
        }
        // 設置Tag
        holder.itemView.tag = position
    }

    override fun getItemCount(): Int = count

    class ViewHolder(val binding: ItemVideoThumbnailBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemVideoThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
