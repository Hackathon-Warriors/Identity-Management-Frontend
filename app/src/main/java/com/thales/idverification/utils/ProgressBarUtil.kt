package com.thales.idverification.utils

import android.view.View
import android.widget.RelativeLayout

object ProgressBarUtil {

    fun showProgressBar(loadingPanel: RelativeLayout, vararg originalLayouts: View) {
        for (originalLayout in originalLayouts)
            originalLayout.visibility = View.GONE
        loadingPanel.visibility = View.VISIBLE
    }

    fun hideProgressBar(loadingPanel: RelativeLayout, vararg originalLayouts: View) {
        loadingPanel.visibility = View.GONE
        for (originalLayout in originalLayouts)
            originalLayout.visibility = View.VISIBLE
    }
}
