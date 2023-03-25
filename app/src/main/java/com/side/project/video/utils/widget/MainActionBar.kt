package com.side.project.video.utils.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.side.project.video.databinding.WidgetActionbarBinding
import com.side.project.video.utils.display
import com.side.project.video.utils.getDrawableCompat

class MainActionBar(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding = WidgetActionbarBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : this(context, null)

    val icon: ImageView = binding.imgIcon

    fun setTitle(str: String) {
        binding.tvTitle.text = str
    }

    fun setImage(number: Int, drawable: Int): ImageView? {
        val img = when (number) {
            1 -> binding.img1
            2 -> binding.img2
            3 -> binding.img3
            else -> return null
        }

        img.display()
        img.setImageDrawable(context.getDrawableCompat(drawable))
        return img
    }
}