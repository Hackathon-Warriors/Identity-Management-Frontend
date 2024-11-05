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
import com.thales.idverification.databinding.AddMoreBottomSheetOtherSourcesBinding
import com.thales.idverification.databinding.FragmentOthersSourcesBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.customviews.adapter.TableViewAdapter
import com.thales.idverification.modules.customviews.adapter.TableViewSpaceDecoration
import com.thales.idverification.modules.customviews.model.TableItemView
import com.thales.idverification.modules.customviews.ui.AddMoreBottomSheetDialogFragment
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.modules.watersources.viewmodel.OtherSourcesViewModel
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.network.OtherSourcesDataResponse
import com.thales.idverification.network.OtherSourcesMasterData
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.clearAndAddAll
import com.thales.idverification.utils.round
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.HashMap


@AndroidEntryPoint
class OtherSourcesFragment : BaseFragment() {
    private lateinit var binding: FragmentOthersSourcesBinding
    private lateinit var addMoreBottomSheetOtherSourcesBinding: AddMoreBottomSheetOtherSourcesBinding

    private lateinit var addMoreBottomSheetDialogFragment: AddMoreBottomSheetDialogFragment

    private lateinit var selectedTableItemView: TextView
    private var selectedTableItemViewOriginalBgColor: Int = 0

    private lateinit var sourcesToIndex: HashMap<String, Int>

    private lateinit var otherSourcesMasterDataList: MutableLiveData<List<OtherSourcesMasterData>>
    private lateinit var newSources: MutableList<OtherSourcesMasterData>

    private var currentOrientation: Int = ORIENTATION_UNDEFINED

    private lateinit var tableViewAdapter: TableViewAdapter
    private lateinit var tableData: MutableList<HashMap<String, String?>>
    private lateinit var recyclerViewDataPortrait: MutableList<TableItemView>
    private lateinit var recyclerViewDataLandscape: MutableList<TableItemView>

    private val otherSourcesViewModel: OtherSourcesViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()

    private var arrayAdapter: ArrayAdapter<String>? = null

    private lateinit var villageId: String
    private lateinit var scheduleId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOthersSourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.other_sources)

        ProgressBarUtil.showProgressBar(
            binding.otherSourcesLoadingPanel.loadingPanel,
            binding.otherSourcesScrollView
        )

        currentOrientation = resources.configuration.orientation

        initTableViewAdapter()

        villageId = dashboardDrawerViewModel.getVillageId().toString()
        scheduleId = dashboardDrawerViewModel.getScheduleId().toString()

        if(!(dashboardDrawerViewModel.getCanEditVillageData())) {
            binding.otherSourcesTable.editTableButton.visibility = View.GONE
            binding.otherSourcesTable.addMoreButton.visibility = View.GONE
        }

        getOtherSourcesData(villageId, scheduleId)

        binding.otherSourcesTable.tableLayout.apply {
            tableViewAdapter.setTableItemOnClickListener(object :
                TableViewAdapter.TableItemOnClickListener {
                override fun onClickTableItem(
                    textView: TextView,
                    rowId: Int,
                    tableRow: HashMap<String, String?>
                ) {
                    if (binding.otherSourcesTable.editTableButton.isSelected) {
                        selectedTableItemView = textView
                        selectedTableItemViewOriginalBgColor =
                            (textView.background as ColorDrawable).color
                        textView.setBackgroundResource(
                            if (rowId % 2 == 0) R.drawable.table_item_selected_even_row_style
                            else R.drawable.table_item_selected_odd_row_style
                        )
                        initAddMoreBottomSheetOtherSources(true, tableRow, villageId, scheduleId)
                        showAddMoreBottomSheetOtherSources()
                    }
                }
            }
            )
            adapter = tableViewAdapter
            addItemDecoration(TableViewSpaceDecoration(context, 1))
        }

        binding.otherSourcesTable.apply {
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                deviceOrientationButton.setBackgroundResource(R.drawable.landscape_orientation_button)
            } else {
                moreColumnsButton.visibility = View.INVISIBLE
                deviceOrientationButton.setBackgroundResource(R.drawable.portrait_orientation_button)
            }

            deviceOrientationButton.setOnClickListener {
                if (checkAutoRotateEnabled()) {
                    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
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
                if (checkAutoRotateEnabled()) {
                    dashboardDrawerViewModel.lockLandscape()
                } else {
                    DialogUtil.showEnableAutoRotateMessage(requireContext(), layoutInflater)
                }
            }

            addMoreButton.setOnClickListener {
                initAddMoreBottomSheetOtherSources(
                    false,
                    villageId = villageId,
                    scheduleId = scheduleId
                )
                showAddMoreBottomSheetOtherSources()
            }
        }
    }

    private fun initAddMoreBottomSheetOtherSources(
        isEditBottomSheet: Boolean,
        tableRow: HashMap<String, String?>? = null,
        villageId: String,
        scheduleId: String
    ) {
        addMoreBottomSheetDialogFragment = AddMoreBottomSheetDialogFragment.newInstance()
        addMoreBottomSheetDialogFragment.setBottomSheetLayoutInflater(object :
            AddMoreBottomSheetDialogFragment.BottomSheetLayoutInflater {
            override fun inflateBottomSheetLayout(
                inflater: LayoutInflater,
                container: ViewGroup?
            ): View? {
                addMoreBottomSheetOtherSourcesBinding =
                    AddMoreBottomSheetOtherSourcesBinding.inflate(inflater, container, false)
                return addMoreBottomSheetOtherSourcesBinding.root
            }

            override fun initBottomSheetLayoutViews(view: View, savedInstanceState: Bundle?) {
                otherSourcesMasterDataList = MutableLiveData(listOf())
                sourcesToIndex = HashMap()

                getOtherSourcesMasterData()
                arrayAdapter = context?.let {
                    ArrayAdapter(
                        it,
                        R.layout.dropdown_item,
                        mutableListOf()
                    )
                }
                arrayAdapter?.setNotifyOnChange(true)

                with(addMoreBottomSheetOtherSourcesBinding.waterSourceValues) {
                    adapter = arrayAdapter
                }

                otherSourcesMasterDataList.observe(
                    viewLifecycleOwner
                ) { newOtherSourcesMasterDataList ->
                    updateSourceValues(
                        newOtherSourcesMasterDataList,
                        isEditBottomSheet,
                        if (isEditBottomSheet) tableRow?.get(sourceId)?.toInt()
                        else null
                    )
                    if (!isEditBottomSheet && newOtherSourcesMasterDataList.isNotEmpty()) {
                        addMoreBottomSheetOtherSourcesBinding.waterSourceValues
                            .setSelection(0, false)
                    }
                }

                initDerivedFields()

                if (isEditBottomSheet)
                    tableRow?.let { populateAllFields(it, sourcesToIndex) }

                addMoreBottomSheetOtherSourcesBinding.removeButton.setOnClickListener {
                    clearAllFields()
                }

                addMoreBottomSheetOtherSourcesBinding.doneButton.setOnClickListener {
                    if (checkMandatoryFields()) {
                        addOtherSourcesData(villageId, scheduleId, isEditBottomSheet)
                    } else {
                        val missingFieldMessage = getMissingFieldMessage()
                        Toast.makeText(context, missingFieldMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        )
        addMoreBottomSheetDialogFragment.setBottomSheetDismissCallback(object :
            AddMoreBottomSheetDialogFragment.BottomSheetDismissCallback {
            override fun onDismissBottomSheet() {
                if (binding.otherSourcesTable.editTableButton.isSelected) {
                    selectedTableItemView.setBackgroundResource(0)
                    selectedTableItemView.setBackgroundColor(selectedTableItemViewOriginalBgColor)
                }
            }
        }
        )
    }

    private fun showAddMoreBottomSheetOtherSources() {
        activity?.supportFragmentManager?.let {
            addMoreBottomSheetDialogFragment.show(
                it,
                "AddMoreBottomOtherSources"
            )
        }
    }

    private fun populateAllFields(
        tableRow: HashMap<String, String?>,
        sourcesToIndex: HashMap<String, Int>
    ) {
        with(addMoreBottomSheetOtherSourcesBinding.waterSourceValues) {
            sourcesToIndex[tableRow[sourceName]]?.let { setSelection(it, false) }
        }
        addMoreBottomSheetOtherSourcesBinding.apply {
            numberOfPumpsValue.setText(tableRow[Companion.numberOfPumps])
            perHourDischargeOfPumpValue.setText(tableRow[dischargePerPump])
            numberOfPumpingDaysInAYearValue.setText(tableRow[numberOfPumpingDaysinYear])
            averagePumpingHoursValue.setText(tableRow[avgHoursPumpingPerDay])
            totalWaterImportValue.setText(tableRow[Companion.totalWaterImport])
        }
    }

    private fun clearAllFields() {
        addMoreBottomSheetOtherSourcesBinding.apply {
            numberOfPumpsValue.text.clear()
            perHourDischargeOfPumpValue.text.clear()
            numberOfPumpingDaysInAYearValue.text.clear()
            averagePumpingHoursValue.text.clear()
            totalWaterImportValue.text = ""
        }
    }

    private fun initDerivedFields() {
        addMoreBottomSheetOtherSourcesBinding.apply {
            numberOfPumpsValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        if (p0.isNullOrEmpty()) {
                            totalWaterImportValue.setText("")
                        } else if (perHourDischargeOfPumpValue.text.isNotEmpty() && numberOfPumpingDaysInAYearValue.text.isNotEmpty() && averagePumpingHoursValue.text.isNotEmpty()) {
                            totalWaterImportValue.setText(
                                (((numberOfPumpsValue.text.toString()
                                    .toDouble()) * (perHourDischargeOfPumpValue.text.toString()
                                    .toDouble()) * (numberOfPumpingDaysInAYearValue.text.toString()
                                    .toDouble()) * (averagePumpingHoursValue.text.toString()
                                    .toDouble())) / 10000000).round(2).toString()
                            )
                        } else {
                            totalWaterImportValue.setText("")
                        }
                    }
                }
            )

            perHourDischargeOfPumpValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        if (p0.isNullOrEmpty()) {
                            totalWaterImportValue.setText("")
                        } else if (numberOfPumpsValue.text.isNotEmpty() && numberOfPumpingDaysInAYearValue.text.isNotEmpty() && averagePumpingHoursValue.text.isNotEmpty()) {
                            totalWaterImportValue.setText(
                                (((numberOfPumpsValue.text.toString()
                                    .toDouble()) * (perHourDischargeOfPumpValue.text.toString()
                                    .toDouble()) * (numberOfPumpingDaysInAYearValue.text.toString()
                                    .toDouble()) * (averagePumpingHoursValue.text.toString()
                                    .toDouble())) / 10000000).round(2).toString()
                            )
                        } else {
                            totalWaterImportValue.setText("")
                        }
                    }
                }
            )
            numberOfPumpingDaysInAYearValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        if (p0.isNullOrEmpty()) {
                            totalWaterImportValue.setText("")
                        } else if (perHourDischargeOfPumpValue.text.isNotEmpty() && numberOfPumpsValue.text.isNotEmpty() && averagePumpingHoursValue.text.isNotEmpty()) {
                            totalWaterImportValue.setText(
                                (((numberOfPumpsValue.text.toString()
                                    .toDouble()) * (perHourDischargeOfPumpValue.text.toString()
                                    .toDouble()) * (numberOfPumpingDaysInAYearValue.text.toString()
                                    .toDouble()) * (averagePumpingHoursValue.text.toString()
                                    .toDouble())) / 10000000).round(2).toString()
                            )
                        } else {
                            totalWaterImportValue.setText("")
                        }
                    }
                }
            )

            averagePumpingHoursValue.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        if (p0.isNullOrEmpty()) {
                            totalWaterImportValue.setText("")
                        } else if (perHourDischargeOfPumpValue.text.isNotEmpty() && numberOfPumpsValue.text.isNotEmpty() && numberOfPumpingDaysInAYearValue.text.isNotEmpty()) {
                            totalWaterImportValue.setText(
                                (((numberOfPumpsValue.text.toString()
                                    .toDouble()) * (perHourDischargeOfPumpValue.text.toString()
                                    .toDouble()) * (numberOfPumpingDaysInAYearValue.text.toString()
                                    .toDouble()) * (averagePumpingHoursValue.text.toString()
                                    .toDouble())) / 10000000).round(2).toString()
                            )
                        } else {
                            totalWaterImportValue.setText("")
                        }
                    }
                }
            )
        }
    }

    private fun checkMandatoryFields(): Boolean {
        addMoreBottomSheetOtherSourcesBinding.apply {
            return !(numberOfPumpsValue.text.isNullOrEmpty() ||
                    averagePumpingHoursValue.text.isNullOrEmpty() ||
                    numberOfPumpingDaysInAYearValue.text.isNullOrEmpty() ||
                    perHourDischargeOfPumpValue.text.isNullOrEmpty())
        }
    }

    private fun getMissingFieldMessage(): String {
        addMoreBottomSheetOtherSourcesBinding.apply {
            return if (numberOfPumpsValue.text.isNullOrEmpty()) {
                "Please enter a value for Number of Pumps"
            } else if (averagePumpingHoursValue.text.isNullOrEmpty()) {
                "Please enter a value for Average Pumping Hours"
            } else if (numberOfPumpingDaysInAYearValue.text.isNullOrEmpty()) {
                "Please enter a value for Number of Pumping Days in a Year"
            } else if (perHourDischargeOfPumpValue.text.isNullOrEmpty()) {
                "Please enter a value for per hour discharge of pump"
            } else ""
        }
    }

    private fun getOtherSourcesMasterData() {
        otherSourcesViewModel.getOtherSourcesMasterData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == null && it.errorData != null) {
                        checkForAuthorizationError(it.errorData)
                    } else {
                        it.data?.let { otherSourcesMasterDataResponse ->
                            otherSourcesMasterDataList.value =
                                otherSourcesMasterDataResponse.otherSourcesMasterDataList
                        }
                    }
                }
                Status.PROGRESS -> {}
                Status.FAIL -> {}
            }
        }
    }


    private fun addOtherSourcesData(villageId: String, scheduleId: String, isEditBottomSheet: Boolean) {
        addMoreBottomSheetOtherSourcesBinding.apply {
            ProgressBarUtil.showProgressBar(
                addMoreOtherSourcesLoadingPanel.loadingPanel,
                addMoreOtherSourcesScrollView
            )

            val index = sourcesToIndex[waterSourceValues.selectedItem.toString()] ?: 0
            otherSourcesViewModel.addOtherSourcesData(
                villageId,
                scheduleId,
                newSources[index].sourceId.toString(),
                numberOfPumpsValue.text.toString().toIntOrNull(),
                perHourDischargeOfPumpValue.text.toString().toDoubleOrNull(),
                numberOfPumpingDaysInAYearValue.text.toString().toIntOrNull(),
                averagePumpingHoursValue.text.toString().toIntOrNull()
            ).observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    addMoreOtherSourcesLoadingPanel.loadingPanel,
                    addMoreOtherSourcesScrollView
                )
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        } else {
                            showSuccessMessage(
                                if(isEditBottomSheet) getString(R.string.other_sources_update_message)
                                else getString(R.string.other_sources_add_message)
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

    private fun updateSourceValues(
        newOtherSourcesMasterDataList: List<OtherSourcesMasterData>,
        isEditBottomSheet: Boolean,
        currentSourceId: Int?
    ) {
        newSources = getNewSources(newOtherSourcesMasterDataList).toMutableList()

        sourcesToIndex.clear()
        arrayAdapter?.clear()

        if (isEditBottomSheet) {
            val currentSource =
                newOtherSourcesMasterDataList.firstOrNull {
                    it.sourceId == currentSourceId
                }
            currentSource?.let {
                newSources.add(0, it)
            }
        }

        for (index in newSources.indices) {
            val newSourceName = newSources[index].sourceName.toString()
            sourcesToIndex[newSourceName] = index
            arrayAdapter?.add(newSourceName)
        }
    }

    private fun getNewSources(newOtherSourcesMasterDataList: List<OtherSourcesMasterData>): List<OtherSourcesMasterData> {
        val allSourceIds = newOtherSourcesMasterDataList.map { it.sourceId }.toSet()
        val existingSourcesIds = tableData.map { it[sourceId]?.toInt() }.toSet()
        val newSourceId = allSourceIds.minus(existingSourcesIds)
        return newOtherSourcesMasterDataList.filter { it.sourceId in newSourceId }
    }

    private fun initTableViewAdapter() {
        tableData = mutableListOf()
        context?.let {
            binding.otherSourcesTable.tableLayout.apply {
                tableViewAdapter = if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerViewDataPortrait = mutableListOf()
                    layoutManager = GridLayoutManager(context, 3)
                    TableViewAdapter(recyclerViewDataPortrait, it, tableData)
                } else {
                    recyclerViewDataLandscape = mutableListOf()
                    layoutManager = GridLayoutManager(context, 7)
                    TableViewAdapter(recyclerViewDataLandscape, it, tableData)
                }
            }
        }
    }

    private fun getOtherSourcesData(villageId: String, scheduleId: String) {
        otherSourcesViewModel.getOtherSourcesData(villageId, scheduleId)
            .observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    binding.otherSourcesLoadingPanel.loadingPanel,
                    binding.otherSourcesScrollView
                )
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        } else {
                            binding.otherSourcesTable.tableLayout.apply {
                                tableData.clearAndAddAll(getTableData(it.data))

                                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                                    recyclerViewDataPortrait.clearAndAddAll(
                                        getRecyclerViewDataPortrait(tableData)
                                    )
                                } else {
                                    recyclerViewDataLandscape.clearAndAddAll(
                                        getRecyclerViewDataLandscape(tableData)
                                    )
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

    private fun getTableData(otherSourcesData: OtherSourcesDataResponse?): List<HashMap<String, String?>> {

        val tableData: MutableList<HashMap<String, String?>> = mutableListOf()

        if (otherSourcesData == null) {
            return tableData
        }

        for (waterSources in otherSourcesData.sources) {
            val tableRow = HashMap<String, String?>()

            tableRow[sourceName] = waterSources.sourceName
            tableRow[sourceId] = waterSources.sourceId
            tableRow[numberOfPumps] =
                waterSources.numberOfPumps.toString()
            tableRow[dischargePerPump] =
                waterSources.dischargePerPump?.round(2).toString()
            tableRow[numberOfPumpingDaysinYear] =
                waterSources.numberOfPumpingDaysinYear.toString()
            tableRow[avgHoursPumpingPerDay] =
                waterSources.avgHoursPumpingPerDay.toString().toFloat().toString()
            tableRow[totalWaterImport] =
                waterSources.totalWaterImport?.round(2).toString()


            tableData.add(tableRow)
        }

        return tableData
    }

    private fun getRecyclerViewDataPortrait(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataPortrait: MutableList<TableItemView> = mutableListOf(
            TableItemView(sourceId, 1, -1, 1, true),
            TableItemView(sourceName, 1, -1, 2, true),
            TableItemView(totalWaterImport, 1, -1, 3, true)
        )
        for (index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataPortrait.addAll(
                listOf(
                    TableItemView(tableRow[sourceId], 1, index, 1),
                    TableItemView(tableRow[sourceName], 1, index, 2),
                    TableItemView(tableRow[totalWaterImport], 1, index, 3),
                )
            )
        }

        return recyclerViewDataPortrait
    }


    private fun getRecyclerViewDataLandscape(tableData: List<HashMap<String, String?>>): List<TableItemView> {
        val recyclerViewDataLandscape: MutableList<TableItemView> = mutableListOf(
            TableItemView(sourceId, 1, -1, 1, true),
            TableItemView(sourceName, 1, -1, 2, true),
            TableItemView(numberOfPumps, 1, -1, 3, true),
            TableItemView(dischargePerPump, 1, -1, 4, true),
            TableItemView(numberOfPumpingDaysinYear, 1, -1, 5, true),
            TableItemView(avgHoursPumpingPerDay, 1, -1, 6, true),
            TableItemView(totalWaterImport, 1, -1, 7, true)
        )
        for (index in tableData.indices) {
            val tableRow = tableData[index]
            recyclerViewDataLandscape.addAll(
                listOf(
                    TableItemView(tableRow[sourceId], 1, 1, 1),
                    TableItemView(tableRow[sourceName], 1, 1, 2),
                    TableItemView(tableRow[numberOfPumps], 1, 1, 3),
                    TableItemView(tableRow[dischargePerPump], 1, 1, 4),
                    TableItemView(
                        tableRow[numberOfPumpingDaysinYear],
                        1,
                        1,
                        5
                    ),
                    TableItemView(tableRow[avgHoursPumpingPerDay], 1, 1, 6),
                    TableItemView(tableRow[totalWaterImport], 1, 1, 7),
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
            getOtherSourcesData(villageId, scheduleId)
        }
    }


    private fun checkAutoRotateEnabled() =
        (activity as DashboardDrawerActivity).checkAutoRotateEnabled()

    companion object {

        private const val sourceId = "Source Id"
        private const val sourceName = "Source Name"
        private const val numberOfPumps = "Number of Pumps"
        private const val dischargePerPump = "Per Hour Discharge of Pump"
        private const val numberOfPumpingDaysinYear = "Number of Pumping Days In a Year"
        private const val avgHoursPumpingPerDay = "Average Pumping Hours In a Day"
        private val totalWaterImport = "Total water import (" + WaterBudgetApplication.getContext().resources.getString(R.string.water_measurement_unit) + ")"
    }

    private fun navigateToLoginActivity() {
        binding.root.findNavController()
            .navigate(R.id.action_otherSourcesFragment_to_loginActivity)
    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if (errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        } else {
            showGenericError()
        }
    }
}