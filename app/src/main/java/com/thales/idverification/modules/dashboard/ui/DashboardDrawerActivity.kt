package com.thales.idverification.modules.dashboard.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.thales.idverification.R
import com.thales.idverification.databinding.ActivityDashboardDrawerBinding
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.WOTRSharedPreference.locale
import com.thales.idverification.utils.WOTRSharedPreference.sessionToken
import com.thales.idverification.utils.WOTRSharedPreference.userEmail
import com.thales.idverification.utils.WOTRSharedPreference.userFullName
import com.thales.idverification.utils.WOTRSharedPreference.userMobile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DashboardDrawerActivity : AppCompatActivity() {

    @Inject
    lateinit var wotrPref : SharedPreferences

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardDrawerBinding
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDashboardDrawer.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val mobileNumberView =  navView.getHeaderView(0).findViewById<TextView>(R.id.textViewPhone)
        mobileNumberView.text = wotrPref.userMobile

        val navController = findNavController(R.id.nav_host_fragment_content_dashboard_drawer)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_crop_scenarios,
                R.id.nav_water_harvest,
                R.id.nav_weather_trends,
                R.id.nav_settings,
                R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawers()
                    true
                }
                in listOf(
                    R.id.nav_crop_scenarios,
                    R.id.nav_water_harvest,
                    R.id.nav_weather_trends,
                    R.id.nav_settings
                ) -> {
                    drawerLayout.closeDrawers()
                    showDismissDialog()
                    true
                }
                R.id.nav_logout -> {
                    wotrPref.sessionToken = null
                    wotrPref.locale = null
                    wotrPref.userEmail = null
                    wotrPref.userFullName = null
                    wotrPref.userMobile = null
                    navController.navigate(R.id.action_nav_home_to_loginActivity)
                    true
                }
                else -> false
            }
        }
        dashboardDrawerViewModel.initOrientationListener(this)
    }

    override fun onResume() {
        super.onResume()
        if(checkAutoRotateEnabled()) {
            dashboardDrawerViewModel.enableOrientationListener()
        } else {
            dashboardDrawerViewModel.disableOrientationListener()
        }
    }

    override fun onPause() {
        super.onPause()
        if(checkAutoRotateEnabled()) {
            dashboardDrawerViewModel.disableOrientationListener()
        } else {
            dashboardDrawerViewModel.enableOrientationListener()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dashboard_drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun checkAutoRotateEnabled(): Boolean = Settings.System.getInt(
        contentResolver,
        Settings.System.ACCELEROMETER_ROTATION,
        0
    ) == 1

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notifications -> {
                showDismissDialog()
                return true
            }
            else -> return false
        }
    }

    private fun showDismissDialog() {
        DialogUtil.showCustomDialog(
            this@DashboardDrawerActivity,
            layoutInflater,
            DialogUtil.DialogType.DEFAULT,
            getString(R.string.attention_message),
            getString(R.string.new_feature_description),
            getString(R.string.dismiss_text)
        )
    }
}