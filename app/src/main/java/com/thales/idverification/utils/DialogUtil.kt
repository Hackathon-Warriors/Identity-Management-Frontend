package com.thales.idverification.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.thales.idverification.R
import com.thales.idverification.WaterBudgetApplication
import com.thales.idverification.databinding.FragmentCustomDialogBinding

object DialogUtil {
    enum class DialogType{
        DEFAULT,
        ENABLE_AUTO_ROTATE,
        ENABLE_LOCATION,
        ERROR
    }

    private lateinit var binding: FragmentCustomDialogBinding

    fun showCustomDialog(context: Context,
                         inflater: LayoutInflater,
                         type: DialogType? = DialogType.ERROR,
                         title: String,
                         description: String,
                         buttonText: String,
                         onButtonClick: (() -> Unit)? = null
    ) {
        binding = FragmentCustomDialogBinding.inflate(inflater)
        val view = binding.root
        val customDialog = AlertDialog.Builder(context, 0).create()

        customDialog.apply {
            setView(view)
            setCancelable(false)
        }.show()

        binding.apply {

            if (type == DialogType.DEFAULT) {
                image.setImageResource(R.drawable.success_tick)
            } else if (type == DialogType.ENABLE_AUTO_ROTATE) {
                image.setImageResource(R.drawable.ic_baseline_screen_rotation_24)
            } else if (type == DialogType.ENABLE_LOCATION) {
                image.setImageResource(R.drawable.ic_baseline_location_on_24)
            }
            textView1.text = title
            textView2.text = description
            textView2.textAlignment = View.TEXT_ALIGNMENT_CENTER
            btnOk.text = buttonText
            btnOk.setOnClickListener {
                customDialog.dismiss()
                onButtonClick?.let {
                    onButtonClick()
                }
            }
        }
    }

    fun showEnableAutoRotateMessage(context: Context, inflater: LayoutInflater) {
        showCustomDialog(
            context,
            inflater,
            DialogType.ENABLE_AUTO_ROTATE,
            WaterBudgetApplication.getContext().resources.getString(R.string.enable_auto_rotate_dialog_title),
            WaterBudgetApplication.getContext().resources.getString(R.string.enable_auto_rotate_dialog_description),
            WaterBudgetApplication.getContext().resources.getString(R.string.enable_auto_rotate_dialog_button_text)
        )
    }

    fun showEnableLocationMessage(context: Context, inflater: LayoutInflater) {
        showCustomDialog(
            context,
            inflater,
            DialogType.ENABLE_LOCATION,
            WaterBudgetApplication.getContext().resources.getString(R.string.enable_location),
            WaterBudgetApplication.getContext().resources.getString(R.string.enable_location_description),
            WaterBudgetApplication.getContext().resources.getString(R.string.enable_location_dialog_button_text)
        )
    }
}