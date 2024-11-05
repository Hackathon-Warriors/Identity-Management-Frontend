package com.thales.idverification.modules.cropplan.ui

import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.thales.idverification.R
import com.thales.idverification.databinding.AddMoreBottomSheetCropDetailsBinding
import com.thales.idverification.databinding.FragmentCropDetailsBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.cropplan.viewmodel.CropDetailsViewModel
import com.thales.idverification.modules.customviews.adapter.TableViewAdapter
import com.thales.idverification.modules.customviews.adapter.TableViewSpaceDecoration
import com.thales.idverification.modules.customviews.model.TableItemView
import com.thales.idverification.modules.customviews.ui.AddMoreBottomSheetDialogFragment
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.network.CropMasterData
import com.thales.idverification.network.CropPlanDetailsResponse
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.clearAndAddAll
import com.thales.idverification.utils.round
import com.thales.idverification.utils.ProgressBarUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CropDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentCropDetailsBinding
    private lateinit var addMoreBottomCropDetailsBinding: AddMoreBottomSheetCropDetailsBinding

    private lateinit var addMoreBottomSheetDialogFragment: AddMoreBottomSheetDialogFragment

    private lateinit var selectedTableItemView: TextView
    private var selectedTableItemViewOriginalBgColor: Int = 0

    private lateinit var cropToIndex: HashMap<String, Int>

    private lateinit var cropPlanMasterDataList: MutableLiveData<List<CropMasterData>>
    private lateinit var newCrops: MutableList<CropMasterData>

    private var currentOrientation: Int = Configuration.ORIENTATION_UNDEFINED

    private lateinit var tableViewAdapter: TableViewAdapter
    private lateinit var tableData: MutableList<HashMap<String, String?>>
    private lateinit var recyclerViewDataPortrait: MutableList<TableItemView>
    private lateinit var recyclerViewDataLandscape: MutableList<TableItemView>

    private val cropDetailsViewModel: CropDetailsViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()

    private var arrayAdapter: ArrayAdapter<String>? = null

    private lateinit var villageId: String
    private lateinit var scheduleId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCropDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.crop_details_crops)

        ProgressBarUtil.showProgressBar(
            binding.cropDetailsLoadingPanel.loadingPanel,
            binding.cropDetailsScrollView
        )

        currentOrientation = resources.configuration.orientation

        initTableViewAdapter()
        if (!(dashboardDrawerViewModel.getCanEditVillageData())) {
            binding.cropDetailsTable.editTableButton.visibility = View.GONE
            binding.cropDetailsTable.addMoreButton.visibility = View.GONE
        }
        villageId = dashboardDrawerViewModel.getVillageId().toString()
        scheduleId = dashboardDrawerViewModel.getScheduleId().toString()

        getCropPlanDetailsData(villageId, scheduleId)

        binding.cropDetailsTable.tableLayout.apply {

            tableViewAdapter.setTableItemOnClickListener(object:
                TableViewAdapter.TableItemOnClickListener {
                override fun onClickTableItem(textView: TextView, rowId: Int, tableRow: HashMap<String, String?>) {
                    if (binding.cropDetailsTable.editTableButton.isSelected) {
                        selectedTableItemView = textView
                        selectedTableItemViewOriginalBgColor = (textView.background as ColorDrawable).color
                        textView.setBackgroundResource(
                            if (rowId%2 ==0) R.drawable.table_item_selected_even_row_style
                            else R.drawable.table_item_selected_odd_row_style
                        )
                        initAddMoreBottomSheetSurfaceStorage(true, tableRow, villageId, scheduleId)
                        showAddMoreBottomSheetSurfaceStorage()
                    }
                }
            }
            )
            adapter = tableViewAdapter
            addItemDecoration(TableViewSpaceDecoration(context, 1))
        }

        binding.cropDetailsTable.apply {
            if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                deviceOrientationButton.setBackgroundResource(R.drawable.landscape_orientation_button)
            } else {
                moreColumnsButton.visibility = View.INVISIBLE
                deviceOrientationButton.setBackgroundResource(R.drawable.portrait_orientation_button)
            }

            deviceOrientationButton.setOnClickListener {
                if(checkAutoRotateEnabled()) {
                    if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        dashboardDrawerViewModel.lockLandscape()
                    } else {
                        dashboardDrawerViewModel.lockPortrait()
                    }
                } else {
                    DialogUtil.showEnableAutoRotateMessage(requireContext(), layoutInflater)
                }
            }

            editTableButton.setOnClickListener {
                editTableButton.isSelected =
                    !editTableButton.isSelected
            }

            moreColumnsButton.setOnClickListener {
                if(checkAutoRotateEnabled()) {
                    dashboardDrawerViewModel.lockLandscape()
                } else {
                    DialogUtil.showEnableAutoRotateMessage(requireContext(), layoutInflater)
                }
            }

            addMoreButton.setOnClickListener {
                initAddMoreBottomSheetSurfaceStorage(false, villageId = villageId, scheduleId = scheduleId)
                showAddMoreBottomSheetSurfaceStorage()
            }
        }
    }

    private fun initAddMoreBottomSheetSurfaceStorage(
        isEditBottomSheet: Boolean,
        tableRow: HashMap<String, String?>? = null,
        villageId: String,
        scheduleId: String
    ) {
        addMoreBottomSheetDialogFragment = AddMoreBottomSheetDialogFragment.newInstance()
        addMoreBottomSheetDialogFragment.setBottomSheetLayoutInflater(object:
            AddMoreBottomSheetDialogFragment.BottomSheetLayoutInflater{
            override fun inflateBottomSheetLayout(
                inflater: LayoutInflater,
                container: ViewGroup?
            ): View? {
                addMoreBottomCropDetailsBinding =
                    AddMoreBottomSheetCropDetailsBinding.inflate(inflater, container, false)
                return addMoreBottomCropDetailsBinding.root
            }

            override fun initBottomSheetLayoutViews(view: View, savedInstanceState: Bundle?) {
                cropPlanMasterDataList = MutableLiveData(listOf())
                cropToIndex = HashMap()

                getCropPlanMasterData()
                arrayAdapter = context?.let { ArrayAdapter(it, R.layout.dropdown_item, mutableListOf()) }
                arrayAdapter?.setNotifyOnChange(true)

                with(addMoreBottomCropDetailsBinding.cropNameValues) {
                    adapter = arrayAdapter
                }

                cropPlanMasterDataList.observe(viewLifecycleOwner
                ) { newCropMasterDataList ->
                    updateCropsValues(
                        newCropMasterDataList,
                        isEditBottomSheet,
                        if(isEditBottomSheet) tableRow?.get(cropId)?.toInt()
                        else null
                    )
                    if(!isEditBottomSheet && newCropMasterDataList.isNotEmpty()) {
                        addMoreBottomCropDetailsBinding.cropNameValues
                            .setSelection(0, false)
                    }
                }

                initDerivedFields()

                if(isEditBottomSheet)
                    tableRow?.let { populateAllFields(it, cropToIndex) }

                addMoreBottomCropDetailsBinding.removeButton.setOnClickListener {
                    clearAllFields()
                }

                addMoreBottomCropDetailsBinding.doneButton.setOnClickListener {
                    if(checkMandatoryFields()) {
                        addCropPlanDetailsData(villageId, scheduleId, isEditBottomSheet)
                    } else {
                        val missingFieldMessage = getMissingFieldMessage()
                        Toast.makeText(context, missingFieldMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        )
        addMoreBottomSheetDialogFragment.setBottomSheetDismissCallback(object:
            AddMoreBottomSheetDialogFragment.BottomSheetDismissCallback {
            override fun onDismissBottomSheet() {
                if (binding.cropDetailsTable.editTableButton.isSelected) {
                    selectedTableItemView.setBackgroundResource(0)
                    selectedTableItemView.setBackgroundColor(selectedTableItemViewOriginalBgColor)
                }
            }
        }
        )
    }

    private fun showAddMoreBottomSheetSurfaceStorage() {
        activity?.supportFragmentManager?.let {
            addMoreBottomSheetDialogFragment.show(
                it,
                "AddMoreBottomSheetSurfaceStorage"
            )
        }
    }

    private fun populateAllFields(
        tableRow: HashMap<String, String?>,
        cropToIndex: HashMap<String, Int>
    ) {
        with(addMoreBottomCropDetailsBinding.cropNameValues) {
            cropToIndex[tableRow[crops]]?.let { setSelection(it,  false) }
        }
        addMoreBottomCropDetailsBinding.apply {
            cropAreaValue.setText(tableRow[totalArea])
            rainfedOrFloodIrrigationAreaValue.setText(tableRow[Companion.rainfedOrFloodIrrigationArea])
            perHaCropwaterRequirementValue.text = tableRow[perHectareCropwaterRequirement]
            waterRequiredForRainfedAndFloodIrrigationValue.text = tableRow[Companion.waterRequiredForRainfedAndFloodIrrigation]
            microIrrigationAreaValue.text = tableRow[Companion.microIrrigationArea]
            perHaCropwaterRequirementForMicroIrrigationAreaValue.text = tableRow[perHectareCropwaterRequirementForMicroIrrigationArea]
            waterRequiredForMicroIrrigationAreaValue.text = tableRow[Companion.waterRequiredForMicroIrrigationArea]
            totalWaterRequiredForCropsValue.text = tableRow[Companion.totalWaterRequiredForCrops]
        }
    }

    private fun clearAllFields() {
        addMoreBottomCropDetailsBinding.apply {
            cropAreaValue.text.clear()
            rainfedOrFloodIrrigationAreaValue.text.clear()
            perHaCropwaterRequirementValue.text = ""
            waterRequiredForRainfedAndFloodIrrigationValue.text = ""
            microIrrigationAreaValue.text = ""
            perHaCropwaterRequirementForMicroIrrigationAreaValue.text = ""
            waterRequiredForMicroIrrigationAreaValue.text = ""
            totalWaterRequiredForCropsValue.text = ""
        }
    }

    private fun initDerivedFields() {
        addMoreBottomCropDetailsBinding.apply {
            cropNameValues.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    perHaCropwaterRequirementValue.text =
                        cropPlanMasterDataList.value?.let { cropPlanMasterDataList ->
                            cropPlanMasterDataList[position].waterRequirementConstant?.round(2).toString()
                                .orEmpty()
                        } ?: ""
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            rainfedOrFloodIrrigationAreaValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            waterRequiredForRainfedAndFloodIrrigationValue.text = ""
                            microIrrigationAreaValue.text = ""
                        } else {

                            val rainfedOrFloodIrrigationArea = s.toString().toDouble().round(2)

                            if(perHaCropwaterRequirementValue.text.isNotEmpty()) {
                                val perHectareCropwaterRequirement = perHaCropwaterRequirementValue.text.toString().toDouble().round(2)

                                waterRequiredForRainfedAndFloodIrrigationValue.text =
                                    cropDetailsViewModel.getWaterRequiredForRainfedAndFloodIrrigation(
                                        rainfedOrFloodIrrigationArea,
                                        perHectareCropwaterRequirement
                                    ).round(2).toString()
                            }

                            if(cropAreaValue.text.isNotEmpty()) {
                                val cropArea = cropAreaValue.text.toString().toDouble().round(2)

                                microIrrigationAreaValue.text = cropDetailsViewModel.getMicroIrrigationArea(
                                    cropArea,
                                    rainfedOrFloodIrrigationArea
                                ).round(2).toString()
                            }
                        }
                    }
                }
            )

            perHaCropwaterRequirementValue.addTextChangedListener(
                object: TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            waterRequiredForRainfedAndFloodIrrigationValue.text = ""
                        } else {

                            val perHectareCropwaterRequirement = s.toString().toDouble().round(2)
                            perHaCropwaterRequirementForMicroIrrigationAreaValue.text =
                                cropDetailsViewModel.getPerHectareCropwaterRequiredUnderMicroIrrigation(
                                    perHectareCropwaterRequirement
                                ).round(2).toString()

                            if(rainfedOrFloodIrrigationAreaValue.text.isNotEmpty()) {
                                val rainfedOrFloodIrrigationArea = rainfedOrFloodIrrigationAreaValue.text.toString().toDouble().round(2)

                                waterRequiredForRainfedAndFloodIrrigationValue.text =
                                    cropDetailsViewModel.getWaterRequiredForRainfedAndFloodIrrigation(
                                        rainfedOrFloodIrrigationArea,
                                        perHectareCropwaterRequirement
                                    ).round(2).toString()
                            }
                        }
                    }

                }
            )

            cropAreaValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            microIrrigationAreaValue.text = ""
                        } else if(rainfedOrFloodIrrigationAreaValue.text.isNotEmpty()) {
                            val cropArea = s.toString().toDouble().round(2)
                            val rainfedOrFloodIrrigationArea = rainfedOrFloodIrrigationAreaValue.text.toString().toDouble().round(2)

                            microIrrigationAreaValue.text = cropDetailsViewModel.getMicroIrrigationArea(
                                cropArea,
                                rainfedOrFloodIrrigationArea
                            ).round(2).toString()
                        }
                    }

                }
            )

            microIrrigationAreaValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            waterRequiredForMicroIrrigationAreaValue.text = ""
                        } else if(perHaCropwaterRequirementForMicroIrrigationAreaValue.text.isNotEmpty()){
                            val microIrrigationArea = s.toString().toDouble().round(2)
                            val perHectareCropwaterRequirementForMicroIrrigationArea =
                                perHaCropwaterRequirementForMicroIrrigationAreaValue.text.toString()
                                    .toDouble().round(2)
                            waterRequiredForMicroIrrigationAreaValue.text =
                                cropDetailsViewModel.getWaterRequiredForMicroIrrigationArea(
                                    microIrrigationArea,
                                    perHectareCropwaterRequirementForMicroIrrigationArea
                                ).round(2).toString()
                        }
                    }

                }
            )

            perHaCropwaterRequirementForMicroIrrigationAreaValue.addTextChangedListener(
                object: TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            waterRequiredForMicroIrrigationAreaValue.text = ""
                        } else if(microIrrigationAreaValue.text.isNotEmpty()) {
                            val perHectareCropwaterRequirementForMicroIrrigationArea = s.toString().toDouble().round(2)
                            val microIrrigationArea = microIrrigationAreaValue.text.toString().toDouble().round(2)

                            waterRequiredForMicroIrrigationAreaValue.text =
                                cropDetailsViewModel.getWaterRequiredForMicroIrrigationArea(
                                    microIrrigationArea,
                                    perHectareCropwaterRequirementForMicroIrrigationArea
                                ).round(2).toString()
                        }
                    }

                }
            )

            waterRequiredForRainfedAndFloodIrrigationValue.addTextChangedListener(
                object: TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            totalWaterRequiredForCropsValue.text = ""
                        } else if(waterRequiredForMicroIrrigationAreaValue.text.isNotEmpty()) {
                            val waterRequiredForRainfedAndFloodIrrigation = s.toString().toDouble().round(2)
                            val waterRequiredForMicroIrrigationArea =
                                waterRequiredForMicroIrrigationAreaValue.text.toString().toDouble().round(2)

                            totalWaterRequiredForCropsValue.text =
                                cropDetailsViewModel.getTotalWaterRequiredForCrops(
                                    waterRequiredForRainfedAndFloodIrrigation,
                                    waterRequiredForMicroIrrigationArea
                                ).round(2).toString()
                        }
                    }

                }
            )

            waterRequiredForMicroIrrigationAreaValue.addTextChangedListener(
                object: TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty()) {
                            totalWaterRequiredForCropsValue.text = ""
                        } else if(waterRequiredForRainfedAndFloodIrrigationValue.text.isNotEmpty()) {
                            val waterRequiredForMicroIrrigationArea = s.toString().toDouble().round(2)
                            val waterRequiredForRainfedAndFloodIrrigation = waterRequiredForRainfedAndFloodIrrigationValue.text.toString().toDouble().round(2)

                            totalWaterRequiredForCropsValue.text =
                                cropDetailsViewModel.getTotalWaterRequiredForCrops(
                                    waterRequiredForRainfedAndFloodIrrigation,
                                    waterRequiredForMicroIrrigationArea
                                ).round(2).toString()
                        }
                    }

                }
            )
        }
    }

    private fun checkMandatoryFields(): Boolean {
        addMoreBottomCropDetailsBinding.apply {
            return !(cropAreaValue.text.isNullOrEmpty() ||
                rainfedOrFloodIrrigationAreaValue.text.isNullOrEmpty())
        }
    }

    private fun getMissingFieldMessage(): String {
        addMoreBottomCropDetailsBinding.apply {
            return if(cropAreaValue.text.isNullOrEmpty()) {
                "Please enter a value for Crop Area"
            } else if(rainfedOrFloodIrrigationAreaValue.text.isNullOrEmpty()) {
                "Please enter a value for Rainfed or flood irrigation area"
            } else ""
        }
    }

    private fun getCropPlanMasterData() {
        cropDetailsViewModel.getCropPlanMasterData().observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        it.data?.let { cropPlanMasterDataResponse ->
                            cropPlanMasterDataList.value = cropPlanMasterDataResponse.cropMasterDataList
                        }
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {}
            }
        }
    }

    private fun addCropPlanDetailsData(villageId: String, scheduleId: String, isEditBottomSheet: Boolean) {
        addMoreBottomCropDetailsBinding.apply {
            ProgressBarUtil.showProgressBar(
                addMoreCropDetailsLoadingPanel.loadingPanel,
                addMoreCropDetailsScrollView
            )

            val index = cropToIndex[cropNameValues.selectedItem.toString()] ?: 0
            cropDetailsViewModel.addCropPlanDetailsData(
                1,
                villageId,
                scheduleId,
                newCrops[index].cropId,
                cropAreaValue.text.toString().toDoubleOrNull(),
                rainfedOrFloodIrrigationAreaValue.text.toString().toDoubleOrNull()
            ).observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    addMoreCropDetailsLoadingPanel.loadingPanel,
                    addMoreCropDetailsScrollView
                )
                when(it.status) {
                    Status.SUCCESS -> {
                        if(it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        } else {
                            showSuccessMessage(
                                if(isEditBottomSheet) getString(R.string.crop_details_data_update_message)
                                else getString(R.string.crop_details_data_add_message)
                            )
                        }
                        addMoreBottomSheetDialogFragment.dismiss()
                    }
                    Status.PROGRESS -> {}
                    Status.FAIL -> {
                        addMoreBottomSheetDialogFragment.dismiss()
                    }
                }
            }
        }
    }

    private fun updateCropsValues(
        newCropMasterDataList: List<CropMasterData>,
        isEditBottomSheet: Boolean,
        currentCropId: Int?
    ) {
        newCrops = getNewCrops(newCropMasterDataList).toMutableList()

        cropToIndex.clear()
        arrayAdapter?.clear()

        if(isEditBottomSheet) {
            val currentCrop =
                newCropMasterDataList.firstOrNull { it.cropId == currentCropId }
            currentCrop?.let {
                newCrops.add(0, it)
            }
        }

        for(index in newCrops.indices) {
            val newCropName = newCrops[index].cropName.toString()
            cropToIndex[newCropName] = index
            arrayAdapter?.add(newCropName)
        }
    }

    private fun getNewCrops(newCropMasterDataList: List<CropMasterData>): List<CropMasterData> {
        val allCropIds = newCropMasterDataList.map { it.cropId }.toSet()
        val existingCropIds = tableData.map { it[cropId]?.toInt() }.toSet()
        val newCropIds = allCropIds.minus(existingCropIds)
        return newCropMasterDataList.filter { it.cropId in newCropIds }
    }

    private fun initTableViewAdapter() {
        tableData = mutableListOf()
        context?.let {
            binding.cropDetailsTable.tableLayout.apply {
                tableViewAdapter = if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerViewDataPortrait = mutableListOf()
                    layoutManager = GridLayoutManager(context, 3)
                    TableViewAdapter(recyclerViewDataPortrait, it, tableData)
                } else {
                    recyclerViewDataLandscape = mutableListOf()
                    layoutManager = GridLayoutManager(context, 9)
                    TableViewAdapter(recyclerViewDataLandscape, it, tableData)
                }
            }
        }
    }

    private fun getCropPlanDetailsData(villageId: String, scheduleId: String) {
        cropDetailsViewModel.getCropPlanDetailsData(villageId, scheduleId).observe(viewLifecycleOwner) {
            ProgressBarUtil.hideProgressBar(
                binding.cropDetailsLoadingPanel.loadingPanel,
                binding.cropDetailsScrollView
            )
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                       checkForAuthorizationError(it.errorData)
                    } else {
                        binding.cropDetailsTable.tableLayout.apply {
                            tableData.clearAndAddAll(getTableData(it.data))

                            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                                recyclerViewDataPortrait.clearAndAddAll(getRecyclerViewDataPortrait(tableData))
                            }
                            else {
                                recyclerViewDataLandscape.clearAndAddAll(getRecyclerViewDataLandscape(tableData))
                            }

                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {}
            }
        }
    }

    private fun getTableData(cropPlanDetails: CropPlanDetailsResponse?): List<HashMap<String, String?>> {

        val tableData: MutableList<HashMap<String, String?>> = mutableListOf()

        if(cropPlanDetails == null) { return tableData }

        for(cropData in cropPlanDetails.cropDataList) {
            val tableRow = HashMap<String, String?>()

            tableRow[cropId] = cropData.cropId.toString()
            tableRow[crops] = cropData.cropName
            tableRow[totalArea] = cropData.cropArea?.round(2).toString()
            tableRow[rainfedOrFloodIrrigationArea] = cropData.rainfedFloodArea?.round(2).toString()
            tableRow[perHectareCropwaterRequirement] = cropData.waterReqConstant?.round(2).toString()
            tableRow[waterRequiredForRainfedAndFloodIrrigation] = cropData.waterReqRainfedCrop?.round(2).toString()
            tableRow[microIrrigationArea] = cropData.microIrrigationArea?.round(2).toString()
            tableRow[perHectareCropwaterRequirementForMicroIrrigationArea] = cropData.microWaterReqConstant?.round(2).toString()
            tableRow[waterRequiredForMicroIrrigationArea] = cropData.waterReqMicroCrop?.round(2).toString()
            tableRow[totalWaterRequiredForCrops] = cropData.totalWaterRequirement?.round(2).toString()

            tableData.add(tableRow)
        }

        return tableData
    }

    private fun getRecyclerViewDataPortrait(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataPortrait: MutableList<TableItemView> = mutableListOf(
            TableItemView(crops,  1, -1, 1, true),
            TableItemView(totalArea,  1, -1, 2, true),
            TableItemView(totalWaterRequired,  1, -1, 3, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataPortrait.addAll(
                listOf(
                    TableItemView(tableRow[crops],  1, index, 1),
                    TableItemView(tableRow[totalArea],1, index, 2),
                    TableItemView(tableRow[totalWaterRequiredForCrops],  1, index, 3),
                )
            )
        }

        return recyclerViewDataPortrait
    }

    private fun getRecyclerViewDataLandscape(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataLandscape: MutableList<TableItemView> = mutableListOf(
            TableItemView(crops,  1, -1, 1, true),
            TableItemView(totalArea,  1, -1, 2, true),
            TableItemView(rainfedOrFloodIrrigationArea, 1, -1, 3,true),
            TableItemView(perHectareCropwaterRequirement, 1, -1, 4, true),
            TableItemView(waterRequiredForRainfedAndFloodIrrigation, 1, -1, 5, true),
            TableItemView(microIrrigationArea, 1, -1, 6, true),
            TableItemView(perHectareCropwaterRequirementForMicroIrrigationArea, 1, -1, 7, true),
            TableItemView(waterRequiredForMicroIrrigationArea, 1, -1, 8, true),
            TableItemView(totalWaterRequired,  1, -1, 9, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataLandscape.addAll(
                listOf(
                    TableItemView(tableRow[crops],  1, index, 1),
                    TableItemView(tableRow[totalArea],  1, index, 2),
                    TableItemView(tableRow[rainfedOrFloodIrrigationArea],  1, index, 3),
                    TableItemView(tableRow[perHectareCropwaterRequirement],  1, index, 4),
                    TableItemView(tableRow[waterRequiredForRainfedAndFloodIrrigation],  1, index, 5),
                    TableItemView(tableRow[microIrrigationArea],  1, index, 6),
                    TableItemView(tableRow[perHectareCropwaterRequirementForMicroIrrigationArea],  1, index, 7),
                    TableItemView(tableRow[waterRequiredForMicroIrrigationArea],  1, index, 8),
                    TableItemView(tableRow[totalWaterRequiredForCrops],  1, index, 9)
                )
            )
        }
        return recyclerViewDataLandscape
    }

    private fun showGenericError() {
        DialogUtil.showCustomDialog(
            requireContext(),
            layoutInflater,
            DialogUtil.DialogType.ERROR,
            getString(R.string.generic_error_title),
            getString(R.string.generic_error_description),
            getString(R.string.generic_error_button_text)
        )
    }

    private fun showSuccessMessage(message: String) {
        DialogUtil.showCustomDialog(
            requireContext(),
            layoutInflater,
            DialogUtil.DialogType.DEFAULT,
            "Success",
            message,
            "Dismiss"
        ) {
            getCropPlanDetailsData(villageId, scheduleId)
        }
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
            .navigate(R.id.action_cropDetailsFragment_to_loginActivity)
    }

    private fun checkAutoRotateEnabled() =
        (activity as DashboardDrawerActivity).checkAutoRotateEnabled()

    companion object {
        private const val cropId = "Crop Id"
        private const val crops = "Crops"
        private const val totalArea = "Total area"
        private const val rainfedOrFloodIrrigationArea = "Rainfed area/flood irrigation"
        private const val perHectareCropwaterRequirement = "Crop water reqd/ha"
        private const val waterRequiredForRainfedAndFloodIrrigation = "Water reqd for rainfed & irrigated"
        private const val microIrrigationArea = "Micro-irrigation area"
        private const val perHectareCropwaterRequirementForMicroIrrigationArea = "Micro-irrigation water reqd/ha"
        private const val waterRequiredForMicroIrrigationArea = "Water reqd under micro-irrigation"
        private const val totalWaterRequired = "Total water reqd"
        private const val totalWaterRequiredForCrops = "Total water required for crops"
    }
}