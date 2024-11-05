package com.thales.idverification.modules.customviews.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thales.idverification.databinding.LocationItemLayoutBinding
import com.thales.idverification.modules.customviews.model.LocationItemView

class LocationsListAdapter(private val locationsList: List<LocationItemView>) :
    RecyclerView.Adapter<LocationsListAdapter.ViewHolder>() {

    private var locationItemOnClickListener: LocationsListAdapter.LocationItemOnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationsListAdapter.ViewHolder {
        val locationItemBinding = LocationItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationsListAdapter.ViewHolder(locationItemBinding)
    }

    override fun onBindViewHolder(holder: LocationsListAdapter.ViewHolder, position: Int) {
        val locationItem: LocationItemView = locationsList[position]
        holder.bind(locationItem)
        holder.itemViewBinding.root.setOnClickListener {
            locationItemOnClickListener?.onClickLocationItem()
        }
        locationItem.image?.let { holder.itemViewBinding.imageView.setImageResource(it) }
    }

    override fun getItemCount(): Int {
        return locationsList.size
    }

    class ViewHolder(val itemViewBinding: LocationItemLayoutBinding) : RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bind(locationItem: LocationItemView) {
            itemViewBinding.apply {
                activityName.text = locationItem.activityName
                address.text = locationItem.address
                activityDate.text = locationItem.activityDate
            }
        }
    }

    fun setLocationItemOnClickListener(onClickListener: LocationItemOnClickListener) {
        locationItemOnClickListener = onClickListener
    }

    interface LocationItemOnClickListener {
        fun onClickLocationItem()
    }
}