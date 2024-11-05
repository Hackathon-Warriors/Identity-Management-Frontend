package com.thales.idverification.modules.watersources.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentRainfallDetailsBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.dashboard.viewmodel.DashboardDrawerViewModel
import com.thales.idverification.modules.watersources.viewmodel.RainFallDetailsViewModel
import com.thales.idverification.network.NetworkErrorDataModel
import com.thales.idverification.network.RainFallDetailsRequest
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.ProgressBarUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.round
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RainfallDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentRainfallDetailsBinding
    private val rainFallDetailsViewModel: RainFallDetailsViewModel by viewModels()
    private val dashboardDrawerViewModel: DashboardDrawerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRainfallDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Rainfall"

        ProgressBarUtil.showProgressBar(
            binding.rainfallDetailsLoadingPanel.loadingPanel,
            binding.rainfallDetailsScrollView
        )

        val villageId = dashboardDrawerViewModel.getVillageId()
        val scheduleId = dashboardDrawerViewModel.getScheduleId().toString()

//        configureRainfallChart()
        initUI(villageId.toString(), scheduleId)
        if(!(dashboardDrawerViewModel.getCanEditVillageData())) {
            binding.editTextRainFall.visibility = View.GONE
            binding.editTextVillageArea.visibility = View.GONE
            binding.textTotalWater.visibility = View.GONE
            binding.viewTextRainFall.visibility = View.VISIBLE
            binding.viewTextVillageArea.visibility = View.VISIBLE
            binding.textTotalWaterView.visibility = View.VISIBLE
            binding.btnSave.visibility = View.GONE
        } else {
            binding.editTextRainFall.visibility = View.VISIBLE
            binding.editTextVillageArea.visibility = View.VISIBLE
            binding.textTotalWater.visibility = View.VISIBLE
            binding.viewTextRainFall.visibility = View.GONE
            binding.viewTextVillageArea.visibility = View.GONE
            binding.textTotalWaterView.visibility = View.GONE

        }
        binding.apply {
            editTextRainFall.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        if (p0.isNullOrEmpty()) {
                            textTotalWater.setText("")
                        } else if (editTextVillageArea.text.isNotEmpty()) {
                            textTotalWater.setText(
                                (((editTextRainFall.text.toString()
                                    .toDouble()) * (editTextVillageArea.text.toString()
                                    .toDouble())) / 1000).round(2).toString()
                            )
                        } else {
                            textTotalWater.setText("")
                        }
                    }

                }
            )

            editTextVillageArea.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0.isNullOrEmpty()) {
                        textTotalWater.setText("")
                    } else if (editTextRainFall.text.isNotEmpty()) {
                        textTotalWater.setText(
                            (((editTextRainFall.text.toString()
                                .toDouble()) * (editTextVillageArea.text.toString()
                                .toDouble())) / 1000).round(2).toString()
                        )
                    } else {
                        textTotalWater.setText("")
                    }
                }

            })

            btnSave.setOnClickListener {
                ProgressBarUtil.showProgressBar(
                    binding.rainfallDetailsLoadingPanel.loadingPanel,
                    binding.rainfallDetailsScrollView
                )

                rainFallDetailsViewModel.putRainFallDetailsData(
                    villageId.toString(),
                    scheduleId,
                    RainFallDetailsRequest(
                        this.editTextRainFall.text.toString().toDoubleOrNull()?.round(2),
                        this.editTextVillageArea.text.toString().toDoubleOrNull()?.round(2),
                        villageId)

                ).observe(viewLifecycleOwner) {
                    ProgressBarUtil.hideProgressBar(
                        binding.rainfallDetailsLoadingPanel.loadingPanel,
                        binding.rainfallDetailsScrollView
                    )
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it.data == null && it.errorData != null) {
                                checkForAuthorizationError(it.errorData)
                            } else {
                                showSuccessMessage()
                            }
                        }
                        Status.FAIL -> {
                            Toast.makeText(activity, it.message.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun initUI(villageId: String, scheduleId: String) {
        rainFallDetailsViewModel.getRainFallDetailsData(
            villageId = villageId,
            scheduleId = scheduleId
        ).observe(viewLifecycleOwner) {
                ProgressBarUtil.hideProgressBar(
                    binding.rainfallDetailsLoadingPanel.loadingPanel,
                    binding.rainfallDetailsScrollView
                )
                when (it.status) {
                    Status.SUCCESS -> {
                        if(it.data == null && it.errorData != null) {
                            checkForAuthorizationError(it.errorData)
                        }
                        binding.editTextRainFall.setText(it.data?.rainfall?.round(2).toString())
                        binding.editTextVillageArea.setText(it.data?.villageArea?.round(2).toString())
                        binding.textTotalWater.setText(it.data?.totalWaterAvailable?.round(2).toString())
                        binding.viewTextRainFall.setText(it.data?.rainfall?.round(2).toString())
                        binding.viewTextVillageArea.setText(it.data?.villageArea?.round(2).toString())
                        binding.textTotalWaterView.setText(it.data?.totalWaterAvailable?.round(2).toString())
                    }
                    else -> {
                        binding.editTextRainFall.setText("")
                        binding.editTextVillageArea.setText("")
                        binding.textTotalWater.setText("")
                        binding.viewTextRainFall.setText("")
                        binding.viewTextVillageArea.setText("")
                        binding.textTotalWaterView.setText("")
                    }
                }
            }
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

    private fun showSuccessMessage() {
        DialogUtil.showCustomDialog(
            requireContext(),
            layoutInflater,
            DialogUtil.DialogType.DEFAULT,
            "Success",
            "Rainfall Details Updated Successfully",
            "Ok"
        )
    }

    private fun navigateToLoginActivity() {
        binding.root.findNavController()
            .navigate(R.id.action_rainfallDetailsFragment_to_loginActivity)
    }

    private fun checkForAuthorizationError(errorData : NetworkErrorDataModel? ) {
        if (errorData?.errorCode?.equals("401") == true) {
            navigateToLoginActivity()
        } else {
            showGenericError()
        }
    }
}