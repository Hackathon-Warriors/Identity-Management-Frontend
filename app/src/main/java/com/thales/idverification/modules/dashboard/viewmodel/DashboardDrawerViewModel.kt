package com.thales.idverification.modules.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.network.Schedule
import com.thales.idverification.network.SchedulesData
import com.thales.idverification.utils.TableViewOrientationListener
import com.thales.idverification.utils.clearAndAddAll

class DashboardDrawerViewModel: ViewModel() {

    private lateinit var orientationListener: TableViewOrientationListener

    private var sortedSchedules: ArrayList<String> = ArrayList()

    private var scheduleId: Int? = null
    private val scheduleIndexToId: HashMap<Int, Int> = hashMapOf()
    private var currentScheduleIndex: Int = -1

    private var villageId: Int? = null
    private lateinit var villageName:String

    private var canEditVillageData : Boolean = false

    fun initOrientationListener(activity: DashboardDrawerActivity) {
        orientationListener = TableViewOrientationListener(activity)
    }

    fun enableOrientationListener() {
        orientationListener.enable()
    }

    fun disableOrientationListener() {
        orientationListener.disable()
    }

    fun lockPortrait() {
        orientationListener.lockPortrait()
    }

    fun lockLandscape() {
        orientationListener.lockLandscape()
    }

    fun getSortedSchedules(): ArrayList<String> {
        return this.sortedSchedules
    }

    fun setSortedSchedules(schedulesData: SchedulesData?) {
        val sortedSchedulesDropdownData = getSchedulesDropdownData(schedulesData)
        sortedSchedules.clearAndAddAll(sortedSchedulesDropdownData)
    }

    private fun mapScheduleIndexToScheduleId(index: Int, scheduleId: Int) {
        scheduleIndexToId[index] = scheduleId
    }

    fun getScheduleIdForScheduleIndex(index: Int): Int? {
        return scheduleIndexToId[index]
    }

    fun getCanEditVillageData(): Boolean {
        return this.canEditVillageData
    }

    fun setCanEditVillageData(canEditVillageData: Boolean) {
        this.canEditVillageData = canEditVillageData
    }

    fun setVillageId(villageId: Int) {
        this.villageId = villageId
    }

    fun getVillageId(): Int? {
        return this.villageId
    }

    private fun setScheduleId(scheduleId: Int) {
        this.scheduleId = scheduleId
    }

    fun getScheduleId(): Int? {
        return this.scheduleId
    }

    fun setCurrentVillageName(villageName: String) {
        this.villageName = villageName
    }

    fun getCurrentVillageName(): String {
        return this.villageName
    }

    fun getCurrentScheduleIndex(): Int {
        return currentScheduleIndex
    }

    fun setCurrentScheduleIndex(scheduleIndex: Int) {
        this.currentScheduleIndex = scheduleIndex
        scheduleIndexToId[currentScheduleIndex]?.let { setScheduleId(it) }
    }

    private fun getSchedulesDropdownData(schedulesData: SchedulesData?): ArrayList<String> {
        val sortedSchedulesData =
            schedulesData?.schedules?.sortedWith(compareBy(Schedule::year, { monthsMap[it.month] }))

        val sortedSchedulesDropdownData =
            sortedSchedulesData?.map { schedule ->
                schedule.month?.let { month ->
                    schedule.year?.let { year ->
                        "$month-$year"
                    } ?: ""
                } ?: ""
            }?.let {
                it as ArrayList<String>
            } ?: ArrayList()

        sortedSchedulesData?.let { sortedSchedulesArrayList ->
            for(index in sortedSchedulesArrayList.indices) {
                sortedSchedulesArrayList[index].scheduleId?.let { scheduleId ->
                    mapScheduleIndexToScheduleId(index, scheduleId)
                }
            }
        }

        return sortedSchedulesDropdownData
    }

    companion object {
        private val monthsMap = hashMapOf(
            "Jan" to 1,
            "Feb" to 2,
            "Mar" to 3,
            "Apr" to 4,
            "May" to 5,
            "Jun" to 6,
            "Jul" to 7,
            "Aug" to 8,
            "Sep" to 9,
            "Oct" to 10,
            "Nov" to 11,
            "Dec" to 12
        )
    }
}
