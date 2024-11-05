package com.thales.idverification.modules.watersources.ui

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_UNDEFINED
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.thales.idverification.R
import com.thales.idverification.WaterBudgetApplication
import com.thales.idverification.databinding.AddMoreBottomSheetSurfaceStorageBinding
import com.thales.idverification.databinding.FragmentSurfaceStorageBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.customviews.adapter.TableViewAdapter
import com.thales.idverification.modules.customviews.adapter.TableViewSpaceDecoration
import com.thales.idverification.modules.customviews.model.TableItemView
import com.thales.idverification.modules.customviews.ui.AddMoreBottomSheetDialogFragment
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.modules.watersources.viewmodel.SurfaceStorageViewModel
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.network.SurfaceStorageDataResponse
import com.thales.idverification.network.SurfaceWaterMasterData
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.clearAndAddAll
import com.thales.idverification.utils.round
import com.thales.idverification.utils.ProgressBarUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SurfaceStorageFragment : BaseFragment() {
    private lateinit var binding: FragmentSurfaceStorageBinding
    private lateinit var addMoreBottomSheetSurfaceStorageBinding: AddMoreBottomSheetSurfaceStorageBinding

    private lateinit var addMoreBottomSheetDialogFragment: AddMoreBottomSheetDialogFragment

    private lateinit var selectedTableItemView: TextView
    private var selectedTableItemViewOriginalBgColor: Int = 0

    private lateinit var structureToIndex: HashMap<String, Int>

    private lateinit var surfaceWaterMasterDataList: MutableLiveData<List<SurfaceWaterMasterData>>
    private lateinit var newStructures: MutableList<SurfaceWaterMasterData>

    private var currentOrientation: Int = ORIENTATION_UNDEFINED

    private lateinit var tableViewAdapter: TableViewAdapter
    private lateinit var tableData: MutableList<HashMap<String, String?>>
    private lateinit var recyclerViewDataPortrait: MutableList<TableItemView>
    private lateinit var recyclerViewDataLandscape: MutableList<TableItemView>

    private val surfaceStorageViewModel: SurfaceStorageViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()

    private var arrayAdapter: ArrayAdapter<String>? = null
    
    private lateinit var villageId: String
    private lateinit var scheduleId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSurfaceStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.surface_storage_surface_storage)

        ProgressBarUtil.showProgressBar(
            binding.surfaceStorageLoadingPanel.loadingPanel,
            binding.surfaceStorageScrollView
        )

        currentOrientation = resources.configuration.orientation

        initTableViewAdapter()
        if(!(dashboardDrawerViewModel.getCanEditVillageData())) {
            binding.surfaceStorageTable.editTableButton.visibility = View.GONE
            binding.surfaceStorageTable.addMoreButton.visibility = View.GONE
        }

        villageId = dashboardDrawerViewModel.getVillageId().toString()
        scheduleId = dashboardDrawerViewModel.getScheduleId().toString()

        getSurfaceStorageData(villageId, scheduleId)

        binding.surfaceStorageTable.tableLayout.apply {

            tableViewAdapter.setTableItemOnClickListener(object:
                TableViewAdapter.TableItemOnClickListener {
                    override fun onClickTableItem(textView: TextView, rowId: Int, tableRow: HashMap<String, String?>) {
                        if (binding.surfaceStorageTable.editTableButton.isSelected) {
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

        binding.surfaceStorageTable.apply {
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
                    addMoreBottomSheetSurfaceStorageBinding =
                        AddMoreBottomSheetSurfaceStorageBinding.inflate(inflater, container, false)
                    return addMoreBottomSheetSurfaceStorageBinding.root
                }

                override fun initBottomSheetLayoutViews(view: View, savedInstanceState: Bundle?) {
                    surfaceWaterMasterDataList = MutableLiveData(listOf())
                    structureToIndex = HashMap()

                    getSurfaceStorageMasterData()
                    arrayAdapter = context?.let { ArrayAdapter(it, R.layout.dropdown_item, mutableListOf()) }
                    arrayAdapter?.setNotifyOnChange(true)

                    with(addMoreBottomSheetSurfaceStorageBinding.structuresValues) {
                        adapter = arrayAdapter
                    }

                    surfaceWaterMasterDataList.observe(viewLifecycleOwner
                    ) { newSurfaceWaterMasterDataList ->
                        updateStructuresValues(
                            newSurfaceWaterMasterDataList,
                            isEditBottomSheet,
                            if(isEditBottomSheet) tableRow?.get(structureId)?.toInt()
                            else null
                        )
                        if(!isEditBottomSheet && newSurfaceWaterMasterDataList.isNotEmpty()) {
                            addMoreBottomSheetSurfaceStorageBinding.structuresValues
                                .setSelection(0, false)
                        }
                    }

                    initDerivedFields()

                    if(isEditBottomSheet)
                        tableRow?.let { populateAllFields(it, structureToIndex) }

                    val isWaterStoragePotentialValueEditable = tableRow?.get(
                        waterStoragePotentialOfStructurePerUnit
                    ) == null

                    addMoreBottomSheetSurfaceStorageBinding.removeButton.setOnClickListener {
                        clearAllFields(isWaterStoragePotentialValueEditable)
                    }

                    addMoreBottomSheetSurfaceStorageBinding.doneButton.setOnClickListener {
                        if(checkMandatoryFields(isWaterStoragePotentialValueEditable)) {
                            addSurfaceStorageData(villageId, scheduleId, isEditBottomSheet, isWaterStoragePotentialValueEditable)
                        } else {
                            val missingFieldMessage = getMissingFieldMessage(isWaterStoragePotentialValueEditable)
                            Toast.makeText(context, missingFieldMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
        addMoreBottomSheetDialogFragment.setBottomSheetDismissCallback(object:
            AddMoreBottomSheetDialogFragment.BottomSheetDismissCallback {
                override fun onDismissBottomSheet() {
                    if (binding.surfaceStorageTable.editTableButton.isSelected) {
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
        structureToIndex: HashMap<String, Int>
    ) {
        with(addMoreBottomSheetSurfaceStorageBinding.structuresValues) {
            structureToIndex[tableRow[structures]]?.let { setSelection(it,  false) }
        }
        addMoreBottomSheetSurfaceStorageBinding.apply {
            countPerAreaHaValue.setText(tableRow[countPerArea])
            if(tableRow[waterStoragePotentialOfStructurePerUnit] == null) {
                waterStoragePotentialOfStructureValueEditable.setText(tableRow[Companion.waterStoragePotentialOfStructure])
            } else {
                waterStoragePotentialOfStructureValueNonEditable.text = tableRow[Companion.waterStoragePotentialOfStructure]
            }
            actualWaterStoredInStructureValue.setText(tableRow[Companion.actualWaterStoredInStructure])
            numberOfFillingValue.setText(tableRow[numberOfFillings])
            totalWaterStoredMultipleFillingsValue.text =
                tableRow[totalWaterStoredWithMultipleFillings]
            evaporationPercentageValue.text = tableRow[Companion.evaporationPercentage]
            totalWaterStoreValue.text = tableRow[totalWaterInStore]
        }
    }

    private fun clearAllFields(isWaterStoragePotentialValueEditable: Boolean) {
        addMoreBottomSheetSurfaceStorageBinding.apply {
            countPerAreaHaValue.text.clear()
            if(isWaterStoragePotentialValueEditable) {
                waterStoragePotentialOfStructureValueEditable.text.clear()
            } else {
                waterStoragePotentialOfStructureValueNonEditable.text = ""
            }
            actualWaterStoredInStructureValue.text.clear()
            numberOfFillingValue.text.clear()
            totalWaterStoredMultipleFillingsValue.text = ""
            evaporationPercentageValue.text = ""
            totalWaterStoreValue.text = ""
        }
    }

    private fun initDerivedFields() {
        addMoreBottomSheetSurfaceStorageBinding.apply {
            structuresValues.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    evaporationPercentageValue.text =
                        newStructures[position].evaporationConstant?.round(2).toString().orEmpty()

                    if (newStructures[position].storagePotentialConstant == null) {
                        showWaterStoragePotentialOfStructureValueEditable()
                    } else {
                        showWaterStoragePotentialOfStructureValueNoEditable()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            countPerAreaHaValue.addTextChangedListener(
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
                        if (structuresValues.adapter.count > 0) {
                            val selectedStructureIndex = structuresValues.selectedItemPosition

                            newStructures[selectedStructureIndex].storagePotentialConstant?.let { waterStoragePotentialOfStructurePerUnit ->
                                if(s.isNullOrEmpty()) {
                                    waterStoragePotentialOfStructureValueNonEditable.text = ""
                                }
                                else {
                                    val countPerAreaHa = s.toString().toDouble().round(2)

                                    waterStoragePotentialOfStructureValueNonEditable.text =
                                        surfaceStorageViewModel.getWaterStoragePotentialOfStructure(
                                            waterStoragePotentialOfStructurePerUnit,
                                            countPerAreaHa
                                        ).round(2).toString()
                                }
                            }
                        }
                    }
                }
            )

            actualWaterStoredInStructureValue.addTextChangedListener(
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
                            totalWaterStoredMultipleFillingsValue.text = ""
                        }
                        else if(numberOfFillingValue.text.isNotEmpty()) {

                            val actualWaterStored = s.toString().toDouble().round(2)
                            val numberOfFilling = numberOfFillingValue.text.toString().toInt()

                            totalWaterStoredMultipleFillingsValue.text =
                                surfaceStorageViewModel.getTotalWaterStoredWithMultipleFillings(
                                    actualWaterStored,
                                    numberOfFilling
                                ).round(2).toString()
                        }
                    }
                }
            )

            numberOfFillingValue.addTextChangedListener(
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
                            totalWaterStoredMultipleFillingsValue.text = ""
                        } else if(actualWaterStoredInStructureValue.text.isNotEmpty()) {
                            val numberOfFilling = s.toString().toInt()
                            val actualWaterStored = actualWaterStoredInStructureValue.text.toString().toDouble().round(2)

                            totalWaterStoredMultipleFillingsValue.text =
                                surfaceStorageViewModel.getTotalWaterStoredWithMultipleFillings(
                                    actualWaterStored,
                                    numberOfFilling
                                ).round(2).toString()
                        }
                    }
                }
            )

            totalWaterStoredMultipleFillingsValue.addTextChangedListener(
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
                            totalWaterStoreValue.text = ""
                        } else {
                            val totalWaterStoredWithMultipleFillings =
                                totalWaterStoredMultipleFillingsValue.text.toString().toDouble()
                            val waterAvailableAfterEvaporation =
                                100 - (evaporationPercentageValue.text.toString().toDoubleOrNull() ?: 0.0)

                            totalWaterStoreValue.text =
                                surfaceStorageViewModel.getTotalWaterInStore(
                                    totalWaterStoredWithMultipleFillings,
                                    waterAvailableAfterEvaporation
                                ).round(2).toString()
                        }
                    }
                }
            )
        }
    }

    private fun showWaterStoragePotentialOfStructureValueNoEditable() {

        addMoreBottomSheetSurfaceStorageBinding.apply {

            waterStoragePotentialOfStructureValueView.apply {

                displayedChild = 1

                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = WRAP_CONTENT
                    startToStart = ConstraintLayout.LayoutParams.UNSET
                    endToEnd = R.id.add_more_bottom_sheet_constraint_layout
                    topToBottom = R.id.count_per_area_ha_value
                }
            }

            waterStoragePotentialOfStructure.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = resources.getDimensionPixelSize(R.dimen.surface_storage_text_view_width)
            }

        }

    }

    private fun showWaterStoragePotentialOfStructureValueEditable() {

        addMoreBottomSheetSurfaceStorageBinding.apply {

            waterStoragePotentialOfStructureValueView.apply {

                displayedChild = 0

                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = MATCH_PARENT
                    startToStart = R.id.add_more_bottom_sheet_constraint_layout
                    endToEnd = ConstraintLayout.LayoutParams.UNSET
                    topToBottom = R.id.water_storage_potential_of_structure
                }
            }

            waterStoragePotentialOfStructure.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = WRAP_CONTENT
            }
        }
    }

    private fun checkMandatoryFields(isWaterStoragePotentialValueEditable: Boolean): Boolean {
        addMoreBottomSheetSurfaceStorageBinding.apply {
            return !(countPerAreaHaValue.text.isNullOrEmpty() ||
                (isWaterStoragePotentialValueEditable && waterStoragePotentialOfStructureValueEditable.text.isNullOrEmpty()) ||
                actualWaterStoredInStructureValue.text.isNullOrEmpty() ||
                numberOfFillingValue.text.isNullOrEmpty())
        }
    }

    private fun getMissingFieldMessage(isWaterStoragePotentialValueEditable: Boolean): String {
        addMoreBottomSheetSurfaceStorageBinding.apply {
            return if(countPerAreaHaValue.text.isNullOrEmpty()) {
                resources.getString(R.string.surface_storage_count_per_area_missing_field_message)
            } else if(isWaterStoragePotentialValueEditable && waterStoragePotentialOfStructureValueEditable.text.isNullOrEmpty()) {
                resources.getString(R.string.surface_storage_water_storage_potential_of_structure_missing_field_message)
            } else if(actualWaterStoredInStructureValue.text.isNullOrEmpty()) {
                resources.getString(R.string.surface_storage_actual_water_stored_in_structure_missing_field_message)
            } else if(numberOfFillingValue.text.isNullOrEmpty()) {
                resources.getString(R.string.surface_storage_number_of_fillings_missing_field_message)
            } else ""
        }
    }

    private fun getSurfaceStorageMasterData() {
        surfaceStorageViewModel.getSurfaceStorageMasterData().observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        it.data?.let { surfaceStorageMasterDataResponse ->
                            surfaceWaterMasterDataList.value = surfaceStorageMasterDataResponse.surfaceWaterMasterDataList
                        }
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {}
            }
        }
    }

    private fun addSurfaceStorageData(
        villageId: String,
        scheduleId: String,
        isEditBottomSheet: Boolean,
        isWaterStoragePotentialValueEditable: Boolean
    ) {
        addMoreBottomSheetSurfaceStorageBinding.apply {
            ProgressBarUtil.showProgressBar(
                addMoreSurfaceStorageLoadingPanel.loadingPanel,
                addMoreSurfaceStorageScrollView
            )

            val index = structureToIndex[structuresValues.selectedItem.toString()] ?: 0
            surfaceStorageViewModel.addSurfaceStorageData(
                villageId,
                scheduleId,
                newStructures[index].structureId,
                countPerAreaHaValue.text.toString().toDoubleOrNull(),
                0.0,
                actualWaterStoredInStructureValue.text.toString().toDoubleOrNull(),
                numberOfFillingValue.text.toString().toIntOrNull(),
                if (isWaterStoragePotentialValueEditable)
                    waterStoragePotentialOfStructureValueEditable.text.toString().toDoubleOrNull()
                else
                    waterStoragePotentialOfStructureValueNonEditable.text.toString().toDoubleOrNull()
            ).observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    addMoreSurfaceStorageLoadingPanel.loadingPanel,
                    addMoreSurfaceStorageScrollView
                )
                when(it.status) {
                    Status.SUCCESS -> {
                        if(it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        } else {
                            showSuccessMessage(
                                if(isEditBottomSheet) getString(R.string.surface_storage_data_update_message)
                                else getString(R.string.surface_storage_data_add_message)
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

    private fun updateStructuresValues(
        newSurfaceWaterMasterDataList: List<SurfaceWaterMasterData>,
        isEditBottomSheet: Boolean,
        currentStructureId: Int?
    ) {
        newStructures = getNewStructures(newSurfaceWaterMasterDataList).toMutableList()

        structureToIndex.clear()
        arrayAdapter?.clear()

        if(isEditBottomSheet) {
            val currentStructure =
                newSurfaceWaterMasterDataList.firstOrNull { it.structureId == currentStructureId }
            currentStructure?.let {
                newStructures.add(0, it)
            }
        }

        for(index in newStructures.indices) {
            val newStructureName = newStructures[index].structureName.toString()
            structureToIndex[newStructureName] = index
            arrayAdapter?.add(newStructureName)
        }
    }

    private fun getNewStructures(newSurfaceWaterMasterDataList: List<SurfaceWaterMasterData>): List<SurfaceWaterMasterData> {
        val allStructureIds = newSurfaceWaterMasterDataList.map { it.structureId }.toSet()
        val existingStructureIds = tableData.map { it[structureId]?.toInt() }.toSet()
        val newStructureIds = allStructureIds.minus(existingStructureIds)
        return newSurfaceWaterMasterDataList.filter { it.structureId in newStructureIds }
    }

    private fun initTableViewAdapter() {
        tableData = mutableListOf()
        context?.let {
            binding.surfaceStorageTable.tableLayout.apply {
                tableViewAdapter = if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerViewDataPortrait = mutableListOf()
                    layoutManager = GridLayoutManager(context, 3)
                    TableViewAdapter(recyclerViewDataPortrait, it, tableData)
                } else {
                    recyclerViewDataLandscape = mutableListOf()
                    layoutManager = GridLayoutManager(context, 8)
                    TableViewAdapter(recyclerViewDataLandscape, it, tableData)
                }
            }
        }
    }

    private fun getSurfaceStorageData(villageId: String, scheduleId: String) {
        surfaceStorageViewModel.getSurfaceStorageData(villageId, scheduleId).observe(viewLifecycleOwner) {
            ProgressBarUtil.hideProgressBar(
                binding.surfaceStorageLoadingPanel.loadingPanel,
                binding.surfaceStorageScrollView
            )
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        binding.surfaceStorageTable.tableLayout.apply {
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

    private fun getTableData(surfaceStorageData: SurfaceStorageDataResponse?): List<HashMap<String, String?>> {

        val tableData: MutableList<HashMap<String, String?>> = mutableListOf()

        if(surfaceStorageData == null) { return tableData }

        for(structureData in surfaceStorageData.structures) {
            val tableRow = HashMap<String, String?>()
            tableRow[structureId] = structureData.structureId.toString()
            tableRow[structures] = structureData.structureName
            tableRow[countPerArea] = structureData.countArea?.round(2).toString()
            tableRow[waterStoragePotentialOfStructurePerUnit] = structureData.storagePotentialConstant?.round(2)?.toString()
            tableRow[waterStoragePotentialOfStructure] = structureData.actualStoragePotentialValue?.round(2)?.toString() ?: ""
            tableRow[actualWaterStoredInStructure] = structureData.actualWaterStored?.round(2).toString()
            tableRow[numberOfFillings] = structureData.numberOfFillings.toString()
            tableRow[totalWaterStoredWithMultipleFillings] = structureData.totalWaterStoredWithFillings?.round(2).toString()
            tableRow[evaporationPercentage] = structureData.evaporation?.round(2).toString()
            tableRow[totalWaterInStore] = structureData.totalWaterStored?.round(2).toString()

            tableData.add(tableRow)
        }

        return tableData
    }

    private fun getRecyclerViewDataPortrait(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataPortrait: MutableList<TableItemView> = mutableListOf(
            TableItemView(structures,  1, -1, 1, true),
            TableItemView(countPerArea,  1, -1, 2, true),
            TableItemView(totalWaterInStore,  1, -1, 3, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataPortrait.addAll(
                listOf(
                    TableItemView(tableRow[structures],  1, index, 1),
                    TableItemView(tableRow[countPerArea],1, index, 2),
                    TableItemView(tableRow[totalWaterInStore],  1, index, 3),
                )
            )
        }

        return recyclerViewDataPortrait
    }

    private fun getRecyclerViewDataLandscape(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataLandscape: MutableList<TableItemView> = mutableListOf(
            TableItemView(structures,  1, -1, 1, true),
            TableItemView(countPerArea,  1, -1, 2, true),
            TableItemView(waterStoragePotentialOfStructure, 1, -1, 3,true),
            TableItemView(actualWaterStoredInStructure, 1, -1, 4, true),
            TableItemView(numberOfFillings, 1, -1, 5, true),
            TableItemView(totalWaterStoredWithMultipleFillings, 1, -1, 6, true),
            TableItemView(evaporationPercentage, 1, -1, 7, true),
            TableItemView(totalWaterInStore,  1, -1, 8, true)
        )
        for(index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataLandscape.addAll(
                listOf(
                    TableItemView(tableRow[structures],  1, index, 1),
                    TableItemView(tableRow[countPerArea],1, index, 2),
                    TableItemView(tableRow[waterStoragePotentialOfStructure], 1, index, 3),
                    TableItemView(tableRow[actualWaterStoredInStructure], 1, index, 4),
                    TableItemView(tableRow[numberOfFillings], 1, index, 5),
                    TableItemView(tableRow[totalWaterStoredWithMultipleFillings], 1, index, 6),
                    TableItemView(tableRow[evaporationPercentage], 1, index, 7),
                    TableItemView(tableRow[totalWaterInStore],  1, index, 8),
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
            getSurfaceStorageData(villageId, scheduleId)
        }
    }

    private fun checkAutoRotateEnabled() =
        (activity as DashboardDrawerActivity).checkAutoRotateEnabled()

    companion object {
        private val structureId =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_structure_id)
        private val structures =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_structures)
        private val countPerArea =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_count_per_area)
        private val waterStoragePotentialOfStructurePerUnit =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_water_storage_potential_of_structure_per_unit)
        private val waterStoragePotentialOfStructure =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_water_storage_potential_of_structure)
        private val actualWaterStoredInStructure =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_actual_water_stored_in_structure)
        private val numberOfFillings =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_number_of_fillings)
        private val totalWaterStoredWithMultipleFillings =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_total_water_stored_with_multiple_fillings)
        private val evaporationPercentage =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_evaporation_percentage)
        private val totalWaterInStore =
            WaterBudgetApplication.getContext().resources.getString(R.string.surface_storage_total_water_in_store)
    }

    private fun navigateToLoginActivity() {
        binding.root.findNavController()
            .navigate(R.id.action_surfaceStorageFragment_to_loginActivity)
    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if (errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        } else {
            showGenericError()
        }
    }
}