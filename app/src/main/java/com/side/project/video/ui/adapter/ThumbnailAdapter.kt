package com.side.project.video.ui.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.side.project.video.databinding.ItemVideoThumbnailBinding
import com.side.project.video.utils.trim.VideoTrimmerUtil

class ThumbnailAdapter : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
    private val thumbnails: MutableList<Bitmap?> = ArrayList()

    fun addThumbnail(thumbnail: Bitmap) {
        thumbnails.add(thumbnail)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thumbnail = thumbnails[position] ?: return
        holder.binding.run {
            val layoutParams = imgThumbnail.layoutParams
            layoutParams.width = VideoTrimmerUtil.VIDEO_FRAMES_WIDTH / VideoTrimmerUtil.MAX_COUNT_RANGE
            imgThumbnail.layoutParams = layoutParams
            imgThumbnail.setImageBitmap(thumbnail)
        }
    }

    override fun getItemCount(): Int = thumbnails.size

    class ViewHolder(val binding: ItemVideoThumbnailBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemVideoThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
