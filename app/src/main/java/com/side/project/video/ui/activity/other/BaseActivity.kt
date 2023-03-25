package com.side.project.video.ui.activity.other

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.side.project.video.R
import com.side.project.video.utils.Constants
import com.side.project.video.utils.Method
import com.side.project.video.utils.displayToast

/**
 * Activity基底
 */
abstract class BaseActivity : AppCompatActivity() {
    lateinit var mActivity: BaseActivity

    /**
     * LifeCycle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level <= TRIM_MEMORY_BACKGROUND)
            System.gc()
    }

    /**
     * Method
     */
    fun requestMediaPermission(): Boolean {
        if (!Method.requestPermission(this, *Constants.media_permission)) {
            displayToast(getString(R.string.ask_media_permission))
            return false
        }
        return true
    }

    fun start(next: Class<*>, bundle: Bundle?, finished: Boolean) {
        Intent(applicationContext, next).also { intent ->
            if (bundle == null)
                intent.putExtras(Bundle())
            else
                intent.putExtras(bundle)
            // jump activity
            startActivity(intent)
            // close activity
            if (finished)
                this.finish()
        }
    }

    fun start(next: Class<*>) {
        this.start(next, null, false)
    }

    fun start(next: Class<*>, bundle: Bundle?) {
        this.start(next, bundle, false)
    }

    fun start(next: Class<*>, finished: Boolean) {
        this.start(next, null, finished)
    }
}