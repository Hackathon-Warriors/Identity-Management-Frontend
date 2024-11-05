package com.thales.idverification.modules.login.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentLoginBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.dashboard.ui.DashboardDrawerActivity
import com.thales.idverification.modules.login.viewmodel.LoginViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.WOTRSharedPreference.userMobile
import com.thales.idverification.utils.WOTRSharedPreference.userRegistered
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private var isUserVerified: Boolean = false
    private var isOTPInvalid: Boolean = false
    private lateinit var countDownTimer: CountDownTimer

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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isUserVerified = false
        isOTPInvalid = false
        enableLoginButton(false)

        binding.editText1.addTextChangedListener {
//            if (isOTPInvalid) showInvalidOTPInlineError(false)
            if (it.toString().length == 10) {
                enableLoginButton(true)
            } else {
                enableLoginButton(false)
            }
        }

        binding.editText2.addTextChangedListener {
            if (it.toString().length == 4 && it.toString() == "1234") {
                enableLoginButton(true)
            } else {
                enableLoginButton(false)
            }
        }

        binding.textView3.setOnClickListener{
            wotrPref.userMobile?.let {
                sendOTP(it, wotrPref.userRegistered)
            }
        }

        binding.loginButton.setOnClickListener {
            if (!isUserVerified) {
//                verifyUsername(binding.editText1.text.toString())
                isUserVerified = true
                binding.textView1.text = getString(R.string.login_title)
                showOTPField()
                startResendOTPTimer(180)
                showResendButton(false)
                enableLoginButton(false)
            } else {
                cancelResendOTPTimer()
                loginUser(binding.editText1.text.toString(),
                    binding.editText2.text.toString())
            }
        }
    }

    private fun showProgress() {
        enableMobileNumberField(false)
        enableOTPField(false)
        enableLoginButton(false)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        enableMobileNumberField(true)
        enableOTPField(true)
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun verifyUsername(mobileNumber: String) {
        showProgress()
        loginViewModel.verifyUser(mobileNumber).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == null && it.errorData != null) {
                        hideProgress()
                        DialogUtil.showCustomDialog(
                            requireContext(),
                            layoutInflater,
                            DialogUtil.DialogType.ERROR,
                            "Error",
                            it.errorData.errorReason.toString(),
                            "Retry"
                        )
                    } else {
                        wotrPref.userMobile = mobileNumber
                        isUserVerified = true
                        it.data?.isUserRegistered?.let {
                            wotrPref.userRegistered = it
                            if (it == true) {
                                // FIXME: VERIFY THIS CHANGE
//                                binding.textView1.setText("LOGIN")
                                binding.textView1.text = getString(R.string.login_title)
                                sendOTP(mobileNumber, it)
                            } else {
                                navigateToRegistrationFragment()
                            }
                        }
                    }
                }
                Status.PROGRESS -> {
                }
                Status.FAIL -> {
                }
            }
        }
    }

    private fun sendOTP(mobileNumber: String, isUserRegistered: Boolean) {
        loginViewModel.sendOTP(mobileNumber,
            isUserRegistered).observe(viewLifecycleOwner) {
            hideProgress()
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == null && it.errorData != null) {
//                        Toast.makeText(activity, it.errorData.toString(), Toast.LENGTH_SHORT).show()
                        showGenericError()
                        isUserVerified = false
                    } else {
                        showOTPField()
                        startResendOTPTimer(180)
                        showResendButton(false)
                        enableLoginButton(false)
//                        Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
                Status.PROGRESS -> {
//                    Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                Status.FAIL -> {
//                    Toast.makeText(activity, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loginUser(mobileNumber: String, otp: String) {
        startActivity(Intent(activity, DashboardDrawerActivity::class.java))
        requireActivity().finish()
//        showProgress()
//        loginViewModel.loginUser(mobileNumber,
//            otp).observe(viewLifecycleOwner) {
//            hideProgress()
//            when (it.status) {
//                Status.SUCCESS -> {
//                    if (it.data == null && it.errorData != null) {
//                        if (it.errorData.errorCode == "LOGIN_ERR_003")
//                            showInvalidOTPInlineError(true)
//                        else
//                            showGenericError()
////                        Toast.makeText(activity, it.errorData.toString(), Toast.LENGTH_SHORT).show()
//                    } else {
//                        it.data?.token.let {
//                            wotrPref.sessionToken = it
//                        }
//                        startActivity(Intent(activity, DashboardDrawerActivity::class.java))
//                        requireActivity().finish()
////                        Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
//                    }
//                }
//                Status.PROGRESS -> {
////                    Toast.makeText(activity, it.data.toString(), Toast.LENGTH_SHORT).show()
//                }
//                Status.FAIL -> {
////                    Toast.makeText(activity, it.message.toString(), Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    private fun startResendOTPTimer(seconds: Int) {
        binding.textView2.visibility = View.VISIBLE
         countDownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var seconds = (millisUntilFinished/1000)
                val minutes = seconds/60
                seconds %= 60
                binding.textView2.setText("Not received OTP? Retry after " +  minutes + ":" + seconds)
            }

            override fun onFinish() {
                binding.textView2.setText("Not received OTP? Retry now!")
                showResendButton(true)
            }
        }.start()
    }

    private fun cancelResendOTPTimer() {
        binding.textView2.visibility = View.INVISIBLE
        showResendButton(false)
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    private fun showResendButton(enable: Boolean) {
        binding.textView3.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        binding.textView3.isEnabled = enable
        binding.textView3.isEnabled = enable
    }

    private fun showInvalidOTPInlineError(enable: Boolean) {
        isOTPInvalid = enable
//        binding.textView2.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        if (enable) {
            binding.editText2.text.clear()
            binding.textView2.visibility = View.VISIBLE
            binding.textView2.setText("Incorrect OTP. Please try again!")
            showResendButton(true)
        }
    }

    private fun showOTPField() {
        binding.editText2.visibility = View.VISIBLE
        enableOTPField(true)
        enableMobileNumberField(false)
    }

    private fun enableLoginButton(enable: Boolean) {
        binding.loginButton.isEnabled = enable
        binding.loginButton.isClickable = enable
    }

    private fun enableMobileNumberField(enable: Boolean) {
        binding.editText1.isEnabled = enable
        binding.editText1.isClickable = enable
    }

    private fun enableOTPField(enable: Boolean) {
        binding.editText2.isEnabled = enable
        binding.editText2.isClickable = enable
    }

    private fun showGenericError() {
        DialogUtil.showCustomDialog(
            requireContext(),
            layoutInflater,
            DialogUtil.DialogType.ERROR,
            getString(R.string.error_string),
            getString(R.string.something_went_wrong),
            getString(R.string.back_text)
        )
    }

    private fun navigateToRegistrationFragment() {
        val action = LoginFragmentDirections
            .actionLoginFragmentToRegistrationFragment(loginStatus = true)
        binding.root.findNavController().navigate(action)
    }
}