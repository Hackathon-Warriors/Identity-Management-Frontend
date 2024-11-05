package com.thales.idverification.modules.savedlocations

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.thales.idverification.R
import com.thales.idverification.databinding.ActivitySavedLocationsBinding
import com.thales.idverification.modules.customviews.adapter.LocationsListAdapter
import com.thales.idverification.modules.customviews.model.LocationItemView
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.ACTIVITY_DATE
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.ACTIVITY_NAME
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.ADDRESS
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.DESCRIPTION
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.ENTITY_NAME
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.IMAGE
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.LATITUDE
import com.thales.idverification.modules.savedlocations.SavedLocationsConstants.Companion.LONGITUDE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedLocationsActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivitySavedLocationsBinding

    private lateinit var locationsListAdapter: LocationsListAdapter

    private lateinit var locationsList: List<LocationItemView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySavedLocationsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        locationsList = listOf(
            LocationItemView(R.drawable.mango_tree, "Plantation drive", "Tarawade Vasti, Mohammadwadi, Pune, Maharashtra 411028", "2023:07:02"),
            LocationItemView(R.drawable.mula_mutha_river_cleaning, "River cleaning activity", "Mula Mutha River, Pune, Maharashtra", "2023:07:01"),
            LocationItemView(R.drawable.bopkhel_dam, "Construction of checkdam", "Dashrath Sayajirao Jadhav Path, Bopkhel, Pune, Maharashtra 411015", "2023:06:30"),
            LocationItemView(R.drawable.taljai_forest, "Plantation drive", "Taljai Forest Area, Pune, Maharashtra 411009", "2023:05:31"),
        )

        initLocationsListAdapter()
    }

    private fun initLocationsListAdapter() {

        locationsListAdapter = LocationsListAdapter(locationsList)

        locationsListAdapter.setLocationItemOnClickListener(object:
            LocationsListAdapter.LocationItemOnClickListener {
                override fun onClickLocationItem() {
                    val intent = Intent(this@SavedLocationsActivity, ImageDetailsActivity::class.java).apply {
                        putExtra(IMAGE, R.drawable.mango_tree)
                        putExtra(ACTIVITY_NAME, "Plantation drive")
                        putExtra(ADDRESS, "Tarawade Vasti, Mohammadwadi, Pune, Maharashtra 411028")
                        putExtra(ACTIVITY_DATE, "2023:07:02")
                        putExtra(ENTITY_NAME, "Mango Tree")
                        putExtra(DESCRIPTION, "Mango tree is grown in July-Aug in rainfed areas.")
                        putExtra(LATITUDE, "18.468107")
                        putExtra(LONGITUDE, "73.929344")
                    }
                    startActivity(intent)
                }
            }
        )

        viewBinding.savedLocationsList.apply {
            layoutManager = LinearLayoutManager(this@SavedLocationsActivity)
            adapter = locationsListAdapter
        }
    }

}