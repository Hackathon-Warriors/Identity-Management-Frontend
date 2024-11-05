package com.thales.idverification.modules.watersources.ui.moisture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentMoistureDetailsBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.round
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoistureDetailsFragment : BaseFragment() {

    private lateinit var binding: FragmentMoistureDetailsBinding
    private val moistureDetailsViewModel: MoistureDetailsViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMoistureDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Soil moisture and Ground water"
        ProgressBarUtil.showProgressBar(
            binding.moistureDetailsLoadingPanel.loadingPanel,
            binding.moistureDetailsConstraintLayout
        )
        initUI()

    }

    private fun initUI() {
        val villageId = dashboardDrawerViewModel.getVillageId().toString()
        val scheduleId = dashboardDrawerViewModel.getScheduleId().toString()
        moistureDetailsViewModel.getMoistureSourceData(
            villageId = villageId,
            scheduleId = scheduleId
        ).observe(viewLifecycleOwner) {
            ProgressBarUtil.hideProgressBar(
                binding.moistureDetailsLoadingPanel.loadingPanel,
                binding.moistureDetailsConstraintLayout
            )
            when (it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    }
                    binding.soilMoistureDataDisplay.text = it.data?.soilMoisture?.round(2).toString()
                    binding.groundMoistureDataDisplay.text = it.data?.groundWater?.round(2).toString()
                }
                else -> {
                    binding.soilMoistureDataDisplay.text = "0"
                    binding.groundMoistureDataDisplay.text = "0"
                }
            }
        }

    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if (errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        }
    }
        private fun navigateToLoginActivity() {
            binding.root.findNavController()
                .navigate(R.id.action_moistureFragment_to_loginActivity)
        }

}