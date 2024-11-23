package com.thales.idverification.modules.success.ui

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.core.animation.addListener
import com.thales.idverification.databinding.ActivitySuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuccessActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivitySuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.apply {

            title.alpha = 0f
            subtitle.alpha = 0f

            ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).apply {
                duration = 1000
                addListener(onStart = {
                    title.visibility = VISIBLE
                })
                addListener (onEnd  = {
                    ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f).apply {
                        duration = 1000
                        addListener(onStart= {
                            subtitle.visibility = VISIBLE
                        })
                        addListener (onEnd ={
                            successAnimation.visibility = VISIBLE
                            successAnimation.playAnimation()
                        })
                    }.start()
                })
            }.start()

        }

    }
}