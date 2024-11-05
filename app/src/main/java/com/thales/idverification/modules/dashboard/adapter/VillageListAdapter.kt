package com.thales.idverification.modules.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.thales.idverification.R
import com.thales.idverification.databinding.CardViewVillageItemBinding
import com.thales.idverification.modules.dashboard.model.VillageItemModel
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.utils.RoleStatus.Active
import com.thales.idverification.utils.Roles

class VillageListAdapter(
    private val villageList: List<VillageItemModel>,
    private var dashboardDrawerViewModel: DashboardDrawerViewModel
) :
    RecyclerView.Adapter<VillageListAdapter.VillageListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VillageListViewHolder {
        val view = CardViewVillageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VillageListViewHolder(view)
    }

    override fun onBindViewHolder(holder: VillageListViewHolder, position: Int) {
        val viewModel = villageList[position]
        holder.tvVillageName.text = viewModel.villageName
        if (viewModel.roleId == Roles.JalSevak.roleId) {
            when (viewModel.status) {
                Active -> {
                    holder.tvViewData.visibility = View.GONE
                    holder.tvAddData.visibility = View.VISIBLE
                }

                else -> {
                    holder.tvViewData.visibility = View.VISIBLE
                    holder.tvAddData.visibility = View.GONE
                }
            }
        } else {
            holder.tvViewData.visibility = View.VISIBLE
            holder.tvAddData.visibility = View.GONE
        }
        holder.tvViewData.setOnClickListener {
            viewModel.villageId?.let { villageId -> dashboardDrawerViewModel.setVillageId(villageId) }
            viewModel.villageName?.let { villageName ->
                dashboardDrawerViewModel.setCurrentVillageName(
                    villageName
                )
            }
            dashboardDrawerViewModel.setCanEditVillageData(false)
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_nav_home_to_villageDataFragment)
        }
        holder.tvAddData.setOnClickListener {
            viewModel.villageId?.let { villageId -> dashboardDrawerViewModel.setVillageId(villageId) }
            viewModel.villageName?.let { villageName ->
                dashboardDrawerViewModel.setCurrentVillageName(
                    villageName
                )
            }
            dashboardDrawerViewModel.setCanEditVillageData(true)
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_nav_home_to_villageDataFragment)
        }
    }

    override fun getItemCount(): Int {
        return villageList.size
    }

    inner class VillageListViewHolder(itemBinding: CardViewVillageItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        val tvVillageName: TextView = itemBinding.tvVillageName
        val tvViewData: TextView = itemBinding.tvViewData
        val tvAddData: TextView = itemBinding.tvAddData
    }
}