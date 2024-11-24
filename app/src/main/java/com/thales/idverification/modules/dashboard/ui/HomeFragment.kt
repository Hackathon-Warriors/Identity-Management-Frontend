package com.thales.idverification.modules.dashboard.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentHomeBinding
import com.thales.idverification.modules.imagepreview.ui.ImagePreviewActivity
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.dashboard.viewmodel.HomeViewModel
import com.thales.idverification.utils.DialogUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Home"

        binding.apply {
            linearLayoutCaptur.setOnClickListener {
                if (checkLocationEnabled()) {
                    startActivity(Intent(activity, ImagePreviewActivity::class.java))
                } else {
                    DialogUtil.showEnableLocationMessage(requireContext(), layoutInflater)
                }
            }

//            cardUpload.setOnClickListener {
//                startActivity(Intent(activity, UploadImageActivity::class.java))
//            }
//
//            cardView.setOnClickListener {
//                startActivity(Intent(activity, SavedLocationsActivity::class.java))
//            }
        }
    }

    private fun checkLocationEnabled(): Boolean {
        val locationManager = (activity as AppCompatActivity).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return gps
    }

    private fun showGenericError() {
        DialogUtil.showCustomDialog(
            requireContext(),
            layoutInflater,
            DialogUtil.DialogType.ERROR,
            getString(R.string.error_string),
            getString(R.string.something_went_wrong),
            getString(R.string.back_text)
        )
    }
}
