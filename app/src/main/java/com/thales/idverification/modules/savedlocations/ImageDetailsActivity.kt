package com.thales.idverification.modules.savedlocations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.thales.idverification.R
import com.thales.idverification.databinding.ActivityImageDetailsBinding
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
class ImageDetailsActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityImageDetailsBinding
    private var latitude: String? = null
    private var longitude: String? = null
    private var entityName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityImageDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        intent.extras?.apply {

            latitude = getString(LATITUDE)
            longitude = getString(LONGITUDE)
            entityName = getString(ENTITY_NAME)

            val gpsCoordinatesFormatted = HtmlCompat.fromHtml(
                getString(R.string.gps_coordinates, latitude, longitude),
                FROM_HTML_MODE_LEGACY
            )

            viewBinding.apply {
                img.setImageResource(getInt(IMAGE))
                updateImageFieldData(activityName, getString(ACTIVITY_NAME))
                updateImageFieldData(address, getString(ADDRESS))
                updateImageFieldData(activityDate, getString(ACTIVITY_DATE))
                updateImageFieldData(entityName, this@ImageDetailsActivity.entityName)
                updateImageFieldData(activityDescription, getString(DESCRIPTION))
                gpsCoordinates.text = gpsCoordinatesFormatted
            }
        }

        viewBinding.viewOnMapButton.setOnClickListener {
            val mapUri = Uri.parse("geo:0,0?q=$latitude,$longitude($entityName)")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun updateImageFieldData(imageFieldView: TextView, imageFieldValue: String?) {
        imageFieldValue?.let {
            imageFieldView.text = it
        }
    }
}