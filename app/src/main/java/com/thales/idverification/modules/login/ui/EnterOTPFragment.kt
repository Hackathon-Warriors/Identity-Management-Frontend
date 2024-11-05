package com.thales.idverification.modules.login.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentEnterOtpBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.login.viewmodel.EnterOTPViewModel
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.WOTRSharedPreference.userEmail
import com.thales.idverification.utils.WOTRSharedPreference.userFullName
import com.thales.idverification.utils.WOTRSharedPreference.userMobile
import com.thales.idverification.utils.WOTRSharedPreference.userRegistered
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class EnterOTPFragment : BaseFragment() {
    private lateinit var binding: FragmentEnterOtpBinding
    private val enterOTPViewModel: EnterOTPViewModel by viewModels()

    @Inject
    lateinit var wotrPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEnterOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wotrPref.userMobile?.let {
            sendOTP(it, wotrPref.userRegistered)
        }

        binding.editText1.addTextChangedListener {
            if (it.toString().length == 4) {
                enableSubmitButton(true)
            } else {
                enableSubmitButton(false)
            }
        }

        binding.textView3.setOnClickListener{
            wotrPref.userMobile?.let {
                sendOTP(it, wotrPref.userRegistered)
            }
        }

        binding.loginButton.setOnClickListener{
            wotrPref.userEmail?.let { it1 ->
                wotrPref.userMobile?.let { it2 ->
                    wotrPref.userFullName?.let { it3 ->
                        registerUser(
                            it1,
                            it2,
                            wotrPref.userRegistered,
                            it3,
                            binding.editText1.text.toString()
                        )
                    }
                }
            }
        }
    }

    private fun sendOTP(mobileNumber: String, isUserRegistered: Boolean) {
        showProgress()
        enterOTPViewModel.sendOTP(mobileNumber,
            isUserRegistered).observe(viewLifecycleOwner) {
            hideProgress()
            when (it.status) {
                Status.SUCCESS -> {
                    showResendButton(false)
                    startResendOTPTimer(30)
                    Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                Status.PROGRESS -> {
                    Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                Status.FAIL -> {
                    Toast.makeText(activity, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUser(emailAddress: String,
                        mobileNumber: String,
                        isUserRegistered: Boolean,
                        fullName: String,
                        otp: String
    ) {
        showProgress()
        enterOTPViewModel.registerUser(
            emailAddress,
            mobileNumber,
            isUserRegistered,
            fullName,
            otp
        ).observe(viewLifecycleOwner) {
            hideProgress()
            when (it.status) {
                Status.SUCCESS -> {
//                    navigateToRegistrationSuccessFragment()
                    Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                Status.PROGRESS -> {
                    Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                Status.FAIL -> {
                    Toast.makeText(activity, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startResendOTPTimer(seconds: Int) {
        binding.textView2.visibility = View.VISIBLE
        object : CountDownTimer((seconds*1000).toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.textView2.setText(getString(R.string.otp_message) + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                binding.textView2.setText(getString(R.string.otp_retry_text))
                showResendButton(true)
            }
        }.start()
    }

    private fun showProgress() {
        enableOTPField(false)
        enableSubmitButton(false)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        enableOTPField(true)
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun enableOTPField(enable: Boolean) {
        binding.editText1.isEnabled = enable
        binding.editText1.isClickable = enable
    }

    private fun showResendButton(enable: Boolean) {
        binding.textView3.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        binding.textView3.isEnabled = enable
        binding.textView3.isEnabled = enable
    }

    private fun enableSubmitButton(enable: Boolean) {
        binding.loginButton.isEnabled = enable
        binding.loginButton.isClickable = enable
    }

//    private fun navigateToRegistrationSuccessFragment() {
//        val action = EnterOTPFragmentDirections
//            .actionEnterOTPFragmentToRegistrationSuccessFragment()
////        val navOptions: NavOptions = NavOptions.Builder()
////            .setPopUpTo(action, true)
////            .build()
//        binding.root.findNavController().navigate(action)
//    }
}