package com.thales.idverification.modules.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentVillageDataBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.modules.dashboard.viewmodel.VillageDataViewModel
import com.thales.idverification.network.DashboardDataResponse
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.round
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VillageDataFragment : BaseFragment() {
    private lateinit var binding: FragmentVillageDataBinding
    private val villageDataViewModel: VillageDataViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()
    private val onClickCard = View.OnClickListener { view ->
        when(view.id) {
            R.id.card_sources -> binding.villageDashboardContent.displayedChild = 0
            R.id.card_consumption -> binding.villageDashboardContent.displayedChild = 1
            R.id.card_crop_plan -> binding.villageDashboardContent.displayedChild = 2
        }
        villageDataViewModel.displayedChild.postValue(binding.villageDashboardContent.displayedChild)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val villageId = dashboardDrawerViewModel.getVillageId().toString()

        val sortedSchedules = dashboardDrawerViewModel.getSortedSchedules()

        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.select_date_dropdown_item,
            sortedSchedules
        )
        binding.schedulesDropdown.adapter = arrayAdapter


        binding.schedulesDropdown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                dashboardDrawerViewModel.setCurrentScheduleIndex(position)

                binding.linearLayout.setBackgroundResource(0)
                binding.schedulesDropdown.setBackgroundResource(R.drawable.schedules_dropdown_style)
                ProgressBarUtil.showProgressBar(
                    binding.villageDataLoadingPanel.loadingPanel,
                    binding.waterBalanceConstraintLayout,
                    binding.bottomCntainer
                )

                dashboardDrawerViewModel.setCurrentScheduleIndex(position)

                val selectedScheduleId =
                    dashboardDrawerViewModel.getScheduleIdForScheduleIndex(position)

                getSourcesData(villageId, selectedScheduleId.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVillageDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = dashboardDrawerViewModel.getCurrentVillageName()
    }

    private fun navigateToRainfallDetailsFragment() {

        binding.root.findNavController()
            .navigate(R.id.action_villageDataFragment_to_rainfallDetailsFragment)
    }

    private fun navigateToSurfaceStorageFragment() {

        binding.root.findNavController()
            .navigate(R.id.action_villageDataFragment_to_surfaceStorageFragment)
    }

    private fun navigateToVillageUseFragment() {
        binding.root.findNavController()
            .navigate(R.id.action_villageDataFragment_to_villageUseFragment)
    }

    private fun navigateToVillageEvaporationFragment() {
        binding.root.findNavController()
            .navigate(R.id.action_villageDataFragment_to_villageEvaporationFragment)
    }

    private fun navigateToCropDetailsFragment() {
        binding.root.findNavController()
            .navigate(R.id.action_villageDataFragment_to_cropDetailsFragment)
    }

    private fun getSourcesData(villageId: String, scheduleId: String) {
        villageDataViewModel.getSourcesData(villageId, scheduleId).observe(viewLifecycleOwner) {
            ProgressBarUtil.hideProgressBar(
                binding.villageDataLoadingPanel.loadingPanel,
                binding.waterBalanceConstraintLayout,
                binding.bottomCntainer
            )
            binding.linearLayout.setBackgroundResource(R.color.village_data_schedule_background_color)
            binding.schedulesDropdown.setBackgroundResource(R.drawable.schedules_dropdown_village_data_style)
            when(it.status) {
                Status.SUCCESS -> {
                    if (it.data == null && it.errorData != null) {
                      checkForAuthorizationError(it.errorData)
                    } else {
                        initDashboard(it.data)
                        initSourcesTab(it.data)
                        initConsumptionTab(it.data)
                        initCropPlanTab(it.data)
                        displayCardsData(it.data)
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {}
            }
        }
    }

    private fun initDashboard(dashboardData: DashboardDataResponse?){
        binding.apply {

            waterBalance.text =
                "Water balance\n" + dashboardData?.totalWaterAvailable?.round(2) + " " + getString(R.string.water_measurement_unit)

            cardSources.setOnClickListener {
                onClickCard.onClick(it)
                selectCard(linearLayoutSources, imageSources)
                deselectCard(linearLayoutConsumption, imageConsumption)
                deselectCard(linearLayoutCropPlan, imageCropPlan)
            }

            cardConsumption.setOnClickListener {
                onClickCard.onClick(it)
                selectCard(linearLayoutConsumption, imageConsumption)
                deselectCard(linearLayoutSources, imageSources)
                deselectCard(linearLayoutCropPlan, imageCropPlan)
            }

            cardCropPlan.setOnClickListener {
                onClickCard.onClick(it)
                selectCard(linearLayoutCropPlan, imageCropPlan)
                deselectCard(linearLayoutSources, imageSources)
                deselectCard(linearLayoutConsumption, imageConsumption)
            }
            when (villageDataViewModel.displayedChild.value) {
                0 -> cardSources.performClick()
                1 -> cardConsumption.performClick()
                2 -> cardCropPlan.performClick()
            }
        }
    }

    private fun initSourcesTab(dashboardData: DashboardDataResponse?) {
        binding.sourcesTab.apply {

            rainfallItemLayout.apply {
                fieldName.text = getString(R.string.sources_rainfall)
                fieldValue.text = dashboardData?.sources?.rainfall?.round(2).toString()
                itemLayout.setOnClickListener {
                    navigateToRainfallDetailsFragment()
                }
            }

            soilMoistureAndGroundWaterItemLayout.apply {
                val villageDataItemHalfPadding = resources.getDimensionPixelOffset(R.dimen.village_dashboard_field_half_padding)
                val villageDataItemPadding =
                    resources.getDimensionPixelOffset(R.dimen.village_dashboard_field_padding)
                itemLayout.apply {
                    setPadding(
                        villageDataItemPadding,
                        villageDataItemHalfPadding,
                        villageDataItemPadding,
                        villageDataItemPadding
                    )
                    setOnClickListener {
                        navigateToSoilMoistureDetailsFragment()
                    }
                }
                fieldName.text = getString(R.string.sources_soil_moisture_and_ground_water)
                fieldValue.text = dashboardData?.sources?.moistureAndGroundWater?.round(2).toString()
            }

            surfaceStorageItemLayout.apply {
                fieldName.text = getString(R.string.sources_surface_storage)
                fieldValue.text = dashboardData?.sources?.surfaceStorage?.round(2).toString()
                itemLayout.setOnClickListener {
                    navigateToSurfaceStorageFragment()
                }
            }

            otherSourcesItemLayout.apply {
                fieldName.text = getString(R.string.sources_other_sources)
                fieldValue.text = dashboardData?.sources?.otherStorage?.round(2).toString()
                itemLayout.setOnClickListener {
                    navigateToOtherSourcesFragment()
                }
            }

            totalWaterAvailableItemLayout.apply {
                summaryFieldName.text = getString(R.string.sources_total_water_available)
                summaryFieldValue.text = dashboardData?.sources?.totalWaterAvailable?.round(2).toString()
            }
        }
    }

    private fun initConsumptionTab(dashboardData: DashboardDataResponse?) {
        binding.consumptionTab.apply {
            villageUseItemLayout.apply {
                fieldName.text = getString(R.string.consumption_village_area)
                fieldValue.text = dashboardData?.consumption?.totalWaterRequirementForDomesticUse?.round(2).toString()
                itemLayout.setOnClickListener{
                    navigateToVillageUseFragment()
                }
            }

            evaporationItemLayout.apply {
                fieldName.text = getString(R.string.consumption_evaporation)
                fieldValue.text = dashboardData?.consumption?.totalEvaporationWaterConsumption?.round(2).toString()
                itemLayout.setOnClickListener{
                    navigateToVillageEvaporationFragment()
                }
            }

            totalWaterConsumptionItemLayout.apply {
                summaryFieldName.text = getString(R.string.consumption_total_water_consumption)
                summaryFieldValue.text = dashboardData?.consumption?.totalConsumption?.round(2).toString()
            }
        }
    }

    private fun initCropPlanTab(dashboardData: DashboardDataResponse?) {
        binding.cropPlanTab.apply {
            kharifCropsItemLayout.apply {
                fieldName.text = getString(R.string.crop_plan_kharif_crops)
                fieldValue.text = dashboardData?.cropPlan?.cropConsumption?.Kharif?.totalWaterRequirement?.round(2).toString()
                itemLayout.setOnClickListener {
                    navigateToCropDetailsFragment()
                }
            }

            rabiCropsItemLayout.apply {
                fieldName.text = getString(R.string.crop_plan_rabi_crops)
                fieldValue.text = dashboardData?.cropPlan?.cropConsumption?.Rabi?.totalWaterRequirement?.round(2).toString()
                itemLayout.setOnClickListener {
                    navigateToCropDetailsFragment()
                }
            }
        }
    }

    private fun displayCardsData(dashboardData: DashboardDataResponse?) {
        binding.apply {
            sourcesValue.text = dashboardData?.sources?.totalWaterAvailable?.round(2).toString()
            consumptionValue.text = dashboardData?.consumption?.totalConsumption?.round(2).toString()
            cropPlanValue.text = dashboardData?.cropPlan?.totalCropWaterRequired?.round(2).toString()
        }
    }

    private fun selectCard(linearLayout: LinearLayout, imageView: ImageView) {
        setBackgroundResource(linearLayout, R.drawable.card_view_gradient_background)
        when(imageView.id) {
            R.id.image_sources -> imageView.setImageResource(R.drawable.cloud_white)
            R.id.image_consumption -> imageView.setImageResource(R.drawable.tap_white)
            R.id.image_crop_plan -> imageView.setImageResource(R.drawable.crop_plan_white)
        }
    }

    private fun deselectCard(linearLayout: LinearLayout, imageView: ImageView) {
        setBackgroundResource(linearLayout, R.drawable.card_view_gradient_gray_background)
        when(imageView.id) {
            R.id.image_sources -> imageView.setImageResource(R.drawable.cloud_blue_gradient)
            R.id.image_consumption -> imageView.setImageResource(R.drawable.tap_blue_gradient)
            R.id.image_crop_plan -> imageView.setImageResource(R.drawable.crop_plan_blue_gradient)
        }
    }

    private fun setBackgroundResource(linearLayout: LinearLayout, resid: Int) {
        val pL = linearLayout.paddingLeft
        val pT = linearLayout.paddingTop
        val pR = linearLayout.paddingRight
        val pB = linearLayout.paddingBottom
        linearLayout.setBackgroundResource(resid)
        linearLayout.setPadding(pL, pT, pR, pB)
    }

    private fun navigateToSoilMoistureDetailsFragment() {
        binding.root.findNavController().navigate(R.id.action_villageDataFragment_to_moistureFragment, arguments)
    }

    private fun navigateToOtherSourcesFragment() {
        binding.root.findNavController().navigate(R.id.action_villageDataFragment_to_otherSourcesFragment, arguments)
    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if (errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        } else {
            showGenericError()
        }
    }

    private fun navigateToLoginActivity() {
        binding.root.findNavController()
            .navigate(R.id.action_villageDataFragment_to_loginActivity)
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