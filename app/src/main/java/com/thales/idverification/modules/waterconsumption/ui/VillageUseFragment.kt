package com.thales.idverification.modules.waterconsumption.ui

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_UNDEFINED
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
import com.thales.idverification.WaterBudgetApplication
import com.thales.idverification.databinding.AddMoreBottomSheetVillageUseBinding
import com.thales.idverification.databinding.FragmentVillageUseBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.customviews.adapter.TableViewAdapter
import com.thales.idverification.modules.customviews.adapter.TableViewSpaceDecoration
import com.thales.idverification.modules.customviews.model.TableItemView
import com.thales.idverification.modules.customviews.ui.AddMoreBottomSheetDialogFragment
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.network.VillageUseDataResponse
import com.thales.idverification.network.WaterConsumptionMasterDataList
import com.thales.idverification.network.WaterConsumptionMasterDataResponse
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.modules.waterconsumption.viewmodel.VillageWaterViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.clearAndAddAll
import com.thales.idverification.utils.round
import com.thales.idverification.utils.ProgressBarUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VillageUseFragment : BaseFragment() {

    private lateinit var binding: FragmentVillageUseBinding
    private lateinit var addMoreBottomSheetVillageUseBinding: AddMoreBottomSheetVillageUseBinding

    private lateinit var addMoreBottomSheetDialogFragment: AddMoreBottomSheetDialogFragment

    private lateinit var selectedTableItemView: TextView
    private var selectedTableItemViewOriginalBgColor: Int = 0

    private lateinit var structureToIndex: HashMap<String, Int>
    private lateinit var newStructures: MutableList<WaterConsumptionMasterDataList>
    private lateinit var waterConsumptionMasterData: MutableLiveData<WaterConsumptionMasterDataResponse>
    private lateinit var waterConsumptionMasterDataList: MutableLiveData<List<WaterConsumptionMasterDataList>>
    private lateinit var villageWaterUseData: MutableLiveData<VillageUseDataResponse>
    private var currentOrientation: Int = ORIENTATION_UNDEFINED

    private lateinit var tableViewAdapter: TableViewAdapter
    private lateinit var tableData: MutableList<HashMap<String, String?>>
    private lateinit var recyclerViewDataPortrait: MutableList<TableItemView>
    private lateinit var recyclerViewDataLandscape: MutableList<TableItemView>

    private val villageWaterViewModel: VillageWaterViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()


    private var arrayAdapter: ArrayAdapter<String>? = null

    private lateinit var villageId: String
    private lateinit var scheduleId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVillageUseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.village_use_village_use)

        ProgressBarUtil.showProgressBar(
            binding.villageUseLoadingPanel.loadingPanel,
            binding.villageUseScrollView
        )

        currentOrientation = resources.configuration.orientation

        initTableViewAdapter()

        if (!(dashboardDrawerViewModel.getCanEditVillageData())) {
            binding.villageUseTable.editTableButton.visibility = View.GONE
            binding.villageUseTable.addMoreButton.visibility = View.GONE
        }

        villageId = dashboardDrawerViewModel.getVillageId().toString()
        scheduleId = dashboardDrawerViewModel.getScheduleId().toString()

        getVillageUseData(villageId, scheduleId)

        binding.villageUseTable.tableLayout.apply {

            tableViewAdapter.setTableItemOnClickListener(object:
                TableViewAdapter.TableItemOnClickListener {
                    override fun onClickTableItem(textView: TextView, rowId: Int, tableRow: HashMap<String, String?>) {
                        if (binding.villageUseTable.editTableButton.isSelected) {
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

        binding.villageUseTable.apply {
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
                    (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.village_use_village_use)
                } else {
                    DialogUtil.showEnableAutoRotateMessage(requireContext(), layoutInflater)
                }
            }

            addMoreButton.setOnClickListener {
                initAddMoreBottomSheetSurfaceStorage(true, villageId = villageId, scheduleId = scheduleId)
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
                ): View {
                    addMoreBottomSheetVillageUseBinding =
                        AddMoreBottomSheetVillageUseBinding.inflate(inflater, container, false)
                    return addMoreBottomSheetVillageUseBinding.root
                }

                override fun initBottomSheetLayoutViews(view: View, savedInstanceState: Bundle?) {
                    waterConsumptionMasterData = MutableLiveData()
                    waterConsumptionMasterDataList = MutableLiveData(listOf())
                    getVillageWaterData()
                    structureToIndex = HashMap()
                    arrayAdapter = context?.let { ArrayAdapter(it, R.layout.dropdown_item, mutableListOf()) }
                    arrayAdapter?.setNotifyOnChange(true)

                    with(addMoreBottomSheetVillageUseBinding.structuresValues) {
                        adapter = arrayAdapter
                    }

                    waterConsumptionMasterDataList.observe(viewLifecycleOwner
                    ) { newWaterConsumptionMasterDataList ->
                        updateStructuresValues(
                            newWaterConsumptionMasterDataList,
                            isEditBottomSheet,
                            if(isEditBottomSheet) tableRow?.get(structureId)?.toInt()
                            else null
                        )
                        if(!isEditBottomSheet && newWaterConsumptionMasterDataList != null) {
                            addMoreBottomSheetVillageUseBinding.structuresValues
                                .setSelection(0, false)
                        }
                    }

                    if(isEditBottomSheet)
                        tableRow?.let { populateAllFields(it, structureToIndex) }

                    initDerivedFields()

                    addMoreBottomSheetVillageUseBinding.removeButton.setOnClickListener {
                        clearAllFields()
                    }

                    addMoreBottomSheetVillageUseBinding.doneButton.setOnClickListener {

                        if(checkMandatoryFields()) {
                            addVillageUseData(villageId, scheduleId, isEditBottomSheet)
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
                    if (binding.villageUseTable.editTableButton.isSelected) {
                        selectedTableItemView.setBackgroundResource(0)
                        selectedTableItemView.setBackgroundColor(selectedTableItemViewOriginalBgColor)
                    }
                }
            }
        )
    }

    private fun initDerivedFields() {

        var perDayWaterRequirement = 0.0
        addMoreBottomSheetVillageUseBinding.apply {
            structuresValues.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    perDayWaterRequirement =  villageWaterUseData.value?.let { villageWaterUseData ->
                        villageWaterUseData.structures[position].perDayWaterRequirement
                    } ?: 0.0
                    perDayWaterRequirementValue.text = perDayWaterRequirement.toString()

                    //need to modify the annualWaterRequirementValue as well
                    if(totalNumberValue.text.isNotEmpty()) {
                        val totalNumber = totalNumberValue.text.toString().toDouble()
                        val annualWaterRequirement = totalNumber.times(perDayWaterRequirement.toString().toDouble()).times(365.00).div(10000000)

                        annualWaterRequirementValue.text = annualWaterRequirement.toString()

                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            totalNumberValue.addTextChangedListener(
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
                        if(s.isNullOrEmpty()) { annualWaterRequirementValue.text = ""
                        }
                        else if(totalNumberValue.text.isNotEmpty()) {
                            val totalNumber = s.toString().toDouble()
                            val annualWaterRequirement = totalNumber.times(perDayWaterRequirement.toString().toDouble()).times(365.00).div(10000000)

                            annualWaterRequirementValue.text = annualWaterRequirement.round(2).toString()

                        }
                    }
                }
            )

        }
    }

    private fun addVillageUseData(villageId: String, scheduleId: String, isEditBottomSheet: Boolean) {
        addMoreBottomSheetVillageUseBinding.apply {
            ProgressBarUtil.showProgressBar(
                addMoreVillageUseLoadingPanel.loadingPanel,
                addMoreVillageUseScrollView
            )

            val index = structureToIndex[structuresValues.selectedItem.toString()] ?: 0
            villageWaterViewModel.addVillageUseData(
                villageId,
                scheduleId,
                index,
                perDayWaterRequirementValue.text.toString().toDoubleOrNull(),
                totalNumberValue.text.toString().toDoubleOrNull(),
                annualWaterRequirementValue.text.toString().toDoubleOrNull()
            ).observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    addMoreVillageUseLoadingPanel.loadingPanel,
                    addMoreVillageUseScrollView
                )
                when(it.status) {
                    Status.SUCCESS -> {
                        if(it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        } else {
                            showSuccessMessage(if(isEditBottomSheet) getString(R.string.village_data_update_message)
                            else getString(R.string.village_data_add_message))
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

    private fun checkMandatoryFields(): Boolean {
        addMoreBottomSheetVillageUseBinding.apply {
            return !(totalNumberValue.text.isNullOrEmpty())
        }
    }

    private fun getMissingFieldMessage(): String {
        addMoreBottomSheetVillageUseBinding.apply {
            return if(totalNumberValue.text.isNullOrEmpty()) {
                "Please enter a value for total number"
            } else ""
        }
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
        structureToIndex: HashMap<String, Int>
    ) {
        with(addMoreBottomSheetVillageUseBinding.structuresValues) {
            structureToIndex[tableRow[structures]]?.let { setSelection(it,  false) }
        }
    }

    private fun clearAllFields() {
        addMoreBottomSheetVillageUseBinding.apply {
            totalNumberValue.text.clear()
            perDayWaterRequirementValue.text = ""
            annualWaterRequirementValue.text = ""
        }
    }

    private fun getVillageWaterData() {
        villageWaterViewModel.getWaterConsumptionMasterData().observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        it.data?.let { waterConsumptionMasterDataResponse ->
                            waterConsumptionMasterData.value = waterConsumptionMasterDataResponse
                        }
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {}
            }
        }
    }

    private fun initTableViewAdapter() {
        tableData = mutableListOf()
        context?.let {
            binding.villageUseTable.tableLayout.apply {
                tableViewAdapter = if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerViewDataPortrait = mutableListOf()
                    layoutManager = GridLayoutManager(context, 3)
                    TableViewAdapter(recyclerViewDataPortrait, it, tableData)
                } else {
                    recyclerViewDataLandscape = mutableListOf()
                    layoutManager = GridLayoutManager(context, 4)
                    TableViewAdapter(recyclerViewDataLandscape, it, tableData)
                }
            }
        }
    }

    private fun getVillageUseData(villageId: String, scheduleId: String) {
        villageWaterViewModel.getVillageUseData(villageId, scheduleId).observe(viewLifecycleOwner) {
            ProgressBarUtil.hideProgressBar(
                binding.villageUseLoadingPanel.loadingPanel,
                binding.villageUseScrollView
            )
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        binding.villageUseTable.tableLayout.apply {
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

    private fun getTableData(villageWaterUseData: VillageUseDataResponse?): List<HashMap<String, String?>> {

        val tableData: MutableList<HashMap<String, String?>> = mutableListOf()

        if(villageWaterUseData == null) { return tableData }

        for(structureData in villageWaterUseData.structures) {
            val tableRow = HashMap<String, String?>()

            tableRow[structures] = structureData.structureName
            tableRow[totalNumber] = structureData.totalWaterRequirementForDomesticUse.toString()
            tableRow[perDayWaterRequirement] =  structureData.perDayWaterRequirement.toString()
            tableRow[annualWaterRequirement] = (structureData.totalWaterRequirementForDomesticUse?.let {
                structureData.perDayWaterRequirement?.times(
                    it
                )
            })?.times(365)?.div(10000000)?.round(2).toString()
            tableData.add(tableRow)
        }

        return tableData
    }

    private fun getRecyclerViewDataPortrait(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataPortrait: MutableList<TableItemView> = mutableListOf(
            TableItemView(structures,  1, -1, 1, true),
            TableItemView(totalNumber,  1, -1, 2, true),
            TableItemView(annualWaterRequirement,  1, -1, 3, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataPortrait.addAll(
                listOf(
                    TableItemView(tableRow[structures],  1, index, 1),
                    TableItemView(tableRow[totalNumber],1, index, 2),
                    TableItemView(tableRow[annualWaterRequirement],  1, index, 3)
                )
            )
        }

        return recyclerViewDataPortrait
    }

    private fun getRecyclerViewDataLandscape(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataLandscape: MutableList<TableItemView> = mutableListOf(
            TableItemView(structures,  1, -1, 1, true),
            TableItemView(totalNumber,  1, -1, 2, true),
            TableItemView(perDayWaterRequirement, 1, -1, 3,true),
            TableItemView(annualWaterRequirement, 1, -1, 4, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataLandscape.addAll(
                listOf(
                    TableItemView(tableRow[structures],  1, index, 1),
                    TableItemView(tableRow[totalNumber],1, index, 2),
                    TableItemView(tableRow[perDayWaterRequirement], 1, index, 3),
                    TableItemView(tableRow[annualWaterRequirement], 1, index, 4)
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
            getString(R.string.success_dialog_title),
            message,
            getString(R.string.success_dialog_button_text)
        ) {
            getVillageUseData(villageId, scheduleId)
        }
    }

    private fun updateStructuresValues(
        newWaterConsumptionMasterDataList: List<WaterConsumptionMasterDataList>,
        isEditBottomSheet: Boolean,
        currentStructureId: Int?
    ) {
        newStructures = getNewStructures(newWaterConsumptionMasterDataList).toMutableList()

        structureToIndex.clear()
        arrayAdapter?.clear()

        if(isEditBottomSheet) {
            val currentStructure =
                newWaterConsumptionMasterDataList.firstOrNull { it.waterUserMasterId == currentStructureId }
            currentStructure?.let {
                newStructures.add(0, it)
            }
        }

        for(index in newStructures.indices) {
            val newStructureName = newStructures[index].waterUserMasterName.toString()
            structureToIndex[newStructureName] = index
            arrayAdapter?.add(newStructureName)
        }
    }

    private fun getNewStructures(newWaterConsumptionMasterDataList: List<WaterConsumptionMasterDataList>): List<WaterConsumptionMasterDataList> {
        val allStructureIds = newWaterConsumptionMasterDataList.map { it.waterUserMasterId }.toSet()
        val existingStructureIds = tableData.map { it[structureId]?.toInt() }.toSet()
        val newStructureIds = allStructureIds.minus(existingStructureIds)
        return newWaterConsumptionMasterDataList.filter { it.waterUserMasterId in newStructureIds }
    }

    private fun navigateToLoginActivity() {
        binding.root.findNavController()
            .navigate(R.id.action_villageUseFragment_to_loginActivity)
    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if(errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        } else {
            showGenericError()
        }
    }

    private fun checkAutoRotateEnabled() =
        (activity as DashboardDrawerActivity).checkAutoRotateEnabled()

    companion object {
        private const val structureId = "Structure Id"
        private const val structures = "Type of use"
        private const val totalNumber = "Total number"
        private const val perDayWaterRequirement = "Per day water requirement (litres)"
        private val annualWaterRequirement = "Annual water required (" + WaterBudgetApplication.getContext().resources.getString(R.string.water_measurement_unit) + ")"
    }
}