package com.side.project.video.utils

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.side.project.video.R
import java.lang.ref.WeakReference

/**
 * Glide Image Loader
 */
object AsyncImageLoader {
    /**
     * Glide 載入圖片
     * @param imageView ImageView
     * @param url String
     */
    fun loadImage(imageView: ImageView?, url: String) {
        if (imageView == null || url.isEmpty()) return

        val context = WeakReference(imageView.context)

        val activity = if (context.get() is Activity) {
            context.takeIf { !(it.get() as Activity).isDestroyed } ?: return
        } else
            return

        Glide.with(activity.get() ?: return).load(url).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.img_placeholder).error(R.drawable.img_placeholder)
            .dontAnimate()).into(imageView)
    }

    fun loadImage(imageView: ImageView?, any: Any) {
        if (imageView == null) return

        val context = WeakReference(imageView.context)

        val activity = if (context.get() is Activity) {
            context.takeIf { !(it.get() as Activity).isDestroyed } ?: return
        } else
            return

        Glide.with(activity.get() ?: return).load(any).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.img_placeholder).error(R.drawable.img_placeholder)
            .dontAnimate()).into(imageView)
    }

    /**
     * Glide 清空所有圖片
     * @param imageView ImageView
     */
    fun clear(imageView: ImageView?) {
        imageView ?: return
        Glide.with(imageView.context).clear(imageView)
    }

    /**
     * Glide 清空資源
     * @param context Context
     */
    fun cleanGlide(context: Context) = Glide.get(context).clearMemory()
}