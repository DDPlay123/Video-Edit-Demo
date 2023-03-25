package com.side.project.video.ui.activity

import android.os.Bundle
import com.side.project.video.databinding.ActivityMainBinding
import com.side.project.video.ui.activity.other.BaseActivity

class MainActivity : BaseActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}