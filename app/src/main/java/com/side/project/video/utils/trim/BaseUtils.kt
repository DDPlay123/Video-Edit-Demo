package com.side.project.video.utils.trim

import android.content.Context
import java.lang.ref.WeakReference

object BaseUtils {
    private const val ERROR_INIT = "Initialize BaseUtils with invoke init()"
    private var mWeakReferenceContext: WeakReference<Context>? = null

    fun init(context: Context) {
        if (mWeakReferenceContext != null) return
        mWeakReferenceContext = WeakReference(context)
        // something to do...
    }

    fun getContext(): Context {
        requireNotNull(mWeakReferenceContext) { ERROR_INIT }
        return mWeakReferenceContext?.get()?.applicationContext!!
    }
}