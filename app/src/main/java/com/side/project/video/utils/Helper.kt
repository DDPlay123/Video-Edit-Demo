package com.side.project.video.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

// region: Context
fun Context.displayToast(message: String, isShort: Boolean = true) =
    Toast.makeText(this, message, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()

fun Context.getColorCompat(color: Int) =
    ContextCompat.getColor(this, color)

fun Context.getDrawableCompat(drawableResId: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableResId)
// endregion

// region: View
fun View.display() { this.visibility = View.VISIBLE }

fun View.hidden() { this.visibility = View.INVISIBLE }

fun View.gone() { this.visibility = View.GONE }
// endregion