package com.thales.idverification.modules.customviews.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thales.idverification.R

class AddMoreBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private var bottomSheetLayoutInflater: BottomSheetLayoutInflater? = null
    private var bottomSheetDismissCallback: BottomSheetDismissCallback? = null

    interface BottomSheetLayoutInflater {
        fun inflateBottomSheetLayout(inflater: LayoutInflater, container: ViewGroup?): View?
        fun initBottomSheetLayoutViews(view: View, savedInstanceState: Bundle?)
    }

    interface BottomSheetDismissCallback {
        fun onDismissBottomSheet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bottomSheetLayoutInflater?.inflateBottomSheetLayout(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bottomSheetLayoutInflater?.initBottomSheetLayoutViews(view, savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bottomSheetDismissCallback?.onDismissBottomSheet()
    }

    fun setBottomSheetLayoutInflater(layoutInflater: BottomSheetLayoutInflater) {
        bottomSheetLayoutInflater = layoutInflater
    }

    fun setBottomSheetDismissCallback(dismissCallback: BottomSheetDismissCallback) {
        bottomSheetDismissCallback = dismissCallback
    }

    companion object {
        fun newInstance(): AddMoreBottomSheetDialogFragment {
            return AddMoreBottomSheetDialogFragment()
        }
    }
}
