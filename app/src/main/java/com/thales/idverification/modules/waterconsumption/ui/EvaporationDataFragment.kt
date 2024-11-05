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
import com.thales.idverification.databinding.AddMoreBottomSheetVillageEvaporationBinding
import com.thales.idverification.databinding.FragmentEvaporationDataBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.customviews.adapter.TableViewAdapter
import com.thales.idverification.modules.customviews.adapter.TableViewSpaceDecoration
import com.thales.idverification.modules.customviews.model.TableItemView
import com.thales.idverification.modules.customviews.ui.AddMoreBottomSheetDialogFragment
import com.thales.idverification.network.EvaporationMasterDataResponse
import com.thales.idverification.network.MasterEvaporationDataList
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.network.VillageEvaporationResponse
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.modules.waterconsumption.viewmodel.EvaporationViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.clearAndAddAll
import com.thales.idverification.utils.round
import com.thales.idverification.utils.ProgressBarUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EvaporationDataFragment : BaseFragment() {

    private lateinit var binding: FragmentEvaporationDataBinding
    private lateinit var addMoreBottomSheetVillageEvaporationBinding: AddMoreBottomSheetVillageEvaporationBinding

    private lateinit var addMoreBottomSheetDialogFragment: AddMoreBottomSheetDialogFragment

    private lateinit var selectedTableItemView: TextView
    private var selectedTableItemViewOriginalBgColor: Int = 0

    private lateinit var structureToIndex: HashMap<String, Int>
    private lateinit var newStructures: MutableList<MasterEvaporationDataList>
    private lateinit var villageEvaporationData: MutableLiveData<VillageEvaporationResponse>
    private lateinit var evaporationMasterDataResponse: MutableLiveData<EvaporationMasterDataResponse>
    private lateinit var evaporationMasterDataList: MutableLiveData<List<MasterEvaporationDataList>>
    private var currentOrientation: Int = ORIENTATION_UNDEFINED

    private lateinit var tableViewAdapter: TableViewAdapter
    private lateinit var tableData: MutableList<HashMap<String, String?>>
    private lateinit var recyclerViewDataPortrait: MutableList<TableItemView>
    private lateinit var recyclerViewDataLandscape: MutableList<TableItemView>

    private val evaporationViewModel: EvaporationViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()

    private var arrayAdapter: ArrayAdapter<String>? = null

    private lateinit var villageId: String
    private lateinit var scheduleId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEvaporationDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.evaporation)

        ProgressBarUtil.showProgressBar(
            binding.evaporationDataLoadingPanel.loadingPanel,
            binding.fragmentEvaporationData
        )

        currentOrientation = resources.configuration.orientation

        initTableViewAdapter()
        if (!(dashboardDrawerViewModel.getCanEditVillageData())) {
            binding.evaporationDataTable.editTableButton.visibility = View.GONE
            binding.evaporationDataTable.addMoreButton.visibility = View.GONE
        }
        villageId = dashboardDrawerViewModel.getVillageId().toString()
        scheduleId = dashboardDrawerViewModel.getScheduleId().toString()

        getVillageUseData(villageId, scheduleId)

        binding.evaporationDataTable.tableLayout.apply {

            tableViewAdapter.setTableItemOnClickListener(object:
                TableViewAdapter.TableItemOnClickListener {
                    override fun onClickTableItem(textView: TextView, rowId: Int, tableRow: HashMap<String, String?>) {
                        if (binding.evaporationDataTable.editTableButton.isSelected) {
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

        binding.evaporationDataTable.apply {
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
                    addMoreBottomSheetVillageEvaporationBinding =
                        AddMoreBottomSheetVillageEvaporationBinding.inflate(inflater, container, false)
                    return addMoreBottomSheetVillageEvaporationBinding.root
                }

                override fun initBottomSheetLayoutViews(view: View, savedInstanceState: Bundle?) {
                    evaporationMasterDataResponse = MutableLiveData()
                    evaporationMasterDataList = MutableLiveData(listOf())
                    getEvaporationMasterData()
                    structureToIndex = HashMap()
                    arrayAdapter = context?.let { ArrayAdapter(it, R.layout.dropdown_item, mutableListOf()) }
                    arrayAdapter?.setNotifyOnChange(true)

                    with(addMoreBottomSheetVillageEvaporationBinding.typeOfAreaValues) {
                        adapter = arrayAdapter
                    }

                    evaporationMasterDataList.observe(viewLifecycleOwner
                    ) { newEvaporationMasterDataList ->
                        updateStructuresValues(
                            newEvaporationMasterDataList,
                            isEditBottomSheet,
                            if(isEditBottomSheet) tableRow?.get(demandEvaporationDataId)?.toInt()
                            else null
                        )
                        if(!isEditBottomSheet && newEvaporationMasterDataList != null) {
                            addMoreBottomSheetVillageEvaporationBinding.typeOfAreaValues
                                .setSelection(0, false)
                        }
                    }

                    if(isEditBottomSheet)
                        tableRow?.let { populateAllFields(it, structureToIndex) }

                    initDerivedFields()

                    addMoreBottomSheetVillageEvaporationBinding.removeButton.setOnClickListener {
                        clearAllFields()
                    }

                    addMoreBottomSheetVillageEvaporationBinding.doneButton.setOnClickListener {

                        if(checkMandatoryFields()) {
                            addVillageEvaporationData(villageId, scheduleId, isEditBottomSheet)
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
                    if (binding.evaporationDataTable.editTableButton.isSelected) {
                        selectedTableItemView.setBackgroundResource(0)
                        selectedTableItemView.setBackgroundColor(selectedTableItemViewOriginalBgColor)
                    }
                }
            }
        )
    }

    private fun initDerivedFields() {

        var evaporationInMm = 0.0
        addMoreBottomSheetVillageEvaporationBinding.apply {
            typeOfAreaValues.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    evaporationInMm =  villageEvaporationData.value?.let { villageEvaporationData ->
                        villageEvaporationData.waterEvaporationDataList[position].evaporationConstant
                    } ?: 0.0
                    evaporationInMmValues.text = evaporationInMm.round(2).toString()

                    if(areaInHaValues.text.isNotEmpty()) {
                        val areaInHa = areaInHaValues.text.toString().toDouble().round(2)
                        val totalEvaporationInTcm = areaInHa.times(evaporationInMm.toString().toDouble()).div(1000).round(2)
                        totalEvaporationInTcmValues.text = totalEvaporationInTcm.round(2).toString()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            areaInHaValues.addTextChangedListener(
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
                            totalEvaporationInTcmValues.text = ""
                        }
                        else if(areaInHaValues.text.isNotEmpty()) {
                            val areaInHa = s.toString().toDouble().round(2)
                            val totalEvaporationInTcm = areaInHa.times(evaporationInMm.toString().toDouble()).div(1000)
                            totalEvaporationInTcmValues.text = totalEvaporationInTcm.round(2).toString()
                        }
                    }
                }
            )

        }


    }

    private fun addVillageEvaporationData(villageId: String, scheduleId: String, isEditBottomSheet: Boolean) {
        addMoreBottomSheetVillageEvaporationBinding.apply {
            ProgressBarUtil.showProgressBar(
                addMoreVillageEvaporationLoadingPanel.loadingPanel,
                addMoreVillageEvaporationScrollView
            )

            val index = structureToIndex[typeOfAreaValues.selectedItem.toString()] ?: 0
            evaporationViewModel.addVillageEvaporationData(
                villageId,
                scheduleId,
                index,
                areaInHaValues.text.toString().toDoubleOrNull()
            ).observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    addMoreVillageEvaporationLoadingPanel.loadingPanel,
                    addMoreVillageEvaporationScrollView
                )
                when(it.status) {
                    Status.SUCCESS -> {
                        if(it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        } else {
                            showSuccessMessage(if(isEditBottomSheet) getString(R.string.evaporation_data_update_message)
                            else getString(R.string.evaporation_data_add_message))
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
        addMoreBottomSheetVillageEvaporationBinding.apply {
            return !(areaInHaValues.text.isNullOrEmpty())
        }
    }

    private fun getMissingFieldMessage(): String {
        addMoreBottomSheetVillageEvaporationBinding.apply {
            return if(areaInHaValues.text.isNullOrEmpty()) {
                "Please enter a value for area in ha"
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
        with(addMoreBottomSheetVillageEvaporationBinding.typeOfAreaValues) {
            structureToIndex[tableRow[totalEvaporation]]?.let { setSelection(it,  false) }
        }
    }

    private fun clearAllFields() {
        addMoreBottomSheetVillageEvaporationBinding.apply {
            areaInHaValues.text.clear()
        }
    }

    private fun getEvaporationMasterData() {
        evaporationViewModel.getEvaporationMasterData().observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        it.data?.let { evaporationMasterData ->
                            evaporationMasterDataResponse.value = evaporationMasterData
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
            binding.evaporationDataTable.tableLayout.apply {
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
        evaporationViewModel.getVillageEvaporationData(villageId, scheduleId).observe(viewLifecycleOwner) {
            ProgressBarUtil.hideProgressBar(
                binding.evaporationDataLoadingPanel.loadingPanel,
                binding.fragmentEvaporationData
            )
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        binding.evaporationDataTable.tableLayout.apply {
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

    private fun getTableData(villageEvaporationData: VillageEvaporationResponse?): List<HashMap<String, String?>> {

        val tableData: MutableList<HashMap<String, String?>> = mutableListOf()

        if(villageEvaporationData == null) { return tableData }

        for(waterEvaporationData in villageEvaporationData.waterEvaporationDataList) {
            val tableRow = HashMap<String, String?>()

            tableRow[typeOfArea] = waterEvaporationData.evaporationAreaType
            tableRow[areaInHa] = waterEvaporationData.areaInHectare?.round(2).toString()
            tableRow[evaporationInMm] =  waterEvaporationData.evaporationConstant?.round(2).toString()
            tableRow[totalEvaporation] = waterEvaporationData.totalWaterEvaporation?.round(2).toString()
            tableData.add(tableRow)
        }

        return tableData
    }

    private fun getRecyclerViewDataPortrait(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataPortrait: MutableList<TableItemView> = mutableListOf(
            TableItemView(typeOfArea,  1, -1, 1, true),
            TableItemView(areaInHa,  1, -1, 2, true),
            TableItemView(totalEvaporation,  1, -1, 3, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataPortrait.addAll(
                listOf(
                    TableItemView(tableRow[typeOfArea],  1, index, 1),
                    TableItemView(tableRow[areaInHa],1, index, 2),
                    TableItemView(tableRow[totalEvaporation],  1, index, 3)
                )
            )
        }

        return recyclerViewDataPortrait
    }

    private fun getRecyclerViewDataLandscape(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataLandscape: MutableList<TableItemView> = mutableListOf(
            TableItemView(typeOfArea,  1, -1, 1, true),
            TableItemView(areaInHa,  1, -1, 2, true),
            TableItemView(evaporationInMm, 1, -1, 3,true),
            TableItemView(totalEvaporation, 1, -1, 4, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataLandscape.addAll(
                listOf(
                    TableItemView(tableRow[typeOfArea],  1, index, 1),
                    TableItemView(tableRow[areaInHa],1, index, 2),
                    TableItemView(tableRow[evaporationInMm], 1, index, 3),
                    TableItemView(tableRow[totalEvaporation], 1, index, 4)
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
        newEvaporationMasterDataList: List<MasterEvaporationDataList>,
        isEditBottomSheet: Boolean,
        currentStructureId: Int?
    ) {
        newStructures = getNewStructures(newEvaporationMasterDataList).toMutableList()

        structureToIndex.clear()
        arrayAdapter?.clear()

        if(isEditBottomSheet) {
            val currentStructure =
                newEvaporationMasterDataList.firstOrNull { it.evaporationMasterId == currentStructureId }
            currentStructure?.let {
                newStructures.add(0, it)
            }
        }

        for(index in newStructures.indices) {
            val newStructureName = newStructures[index].evaporationAreaType.toString()
            structureToIndex[newStructureName] = index
            arrayAdapter?.add(newStructureName)
        }
    }

    private fun getNewStructures(newEvaporationMasterDataList: List<MasterEvaporationDataList>): List<MasterEvaporationDataList> {
        val allStructureIds = newEvaporationMasterDataList.map { it.evaporationMasterId }.toSet()
        val existingStructureIds = tableData.map { it[demandEvaporationDataId]?.toInt() }.toSet()
        val newStructureIds = allStructureIds.minus(existingStructureIds)
        return newEvaporationMasterDataList.filter { it.evaporationMasterId in newStructureIds }
    }

    private fun navigateToLoginActivity() {
        binding.root.findNavController()
            .navigate(R.id.action_villageEvaporationFragment_to_loginActivity)
    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if (errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        } else {
            showGenericError()
        }
    }

    private fun checkAutoRotateEnabled() =
        (activity as DashboardDrawerActivity).checkAutoRotateEnabled()

    companion object {
        private const val demandEvaporationDataId = "Structure Id"
        private const val typeOfArea = "Type of area"
        private const val areaInHa = "Area in ha"
        private const val evaporationInMm = "Evaporation in mm"
        private const val totalEvaporation = "Total evaporation in tcm"
    }
}