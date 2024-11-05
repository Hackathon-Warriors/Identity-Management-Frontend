package com.thales.idverification.modules.login.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.thales.idverification.R
import com.thales.idverification.databinding.FragmentRegistrationBinding
import com.thales.idverification.modules.base.BaseFragment
import com.thales.idverification.modules.login.viewmodel.RegistrationViewModel
import com.thales.idverification.utils.DialogUtil
import com.thales.idverification.utils.Status
import com.thales.idverification.utils.WOTRSharedPreference.userMobile
import com.thales.idverification.utils.WOTRSharedPreference.userRegistered
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationFragment : BaseFragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private val NAME_REGEX: String = "[a-zA-Z\\s]+"
    private val registrationViewModel: RegistrationViewModel by viewModels()
    private var isOTPInvalid: Boolean = false

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
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isOTPInvalid = false
        enableMobileField(false)
        enableSubmitButton(false)

        wotrPref.userMobile?.let {
            binding.editText.setText(it)
            sendOTP(it, wotrPref.userRegistered)
        }

        binding.editText1.addTextChangedListener {
            if (checkDataComplete(
                    it.toString(),
                    binding.editText2.text.toString(),
                    binding.editText3.text.toString())) {
                enableSubmitButton(true)
            } else {
                enableSubmitButton(false)
            }
        }

        binding.editText2.addTextChangedListener {
            if (checkDataComplete(
                    binding.editText1.text.toString(),
                    it.toString(),
                    binding.editText3.text.toString())) {
                enableSubmitButton(true)
            } else {
                enableSubmitButton(false)
            }
        }

        binding.editText3.addTextChangedListener {
            if (checkDataComplete(
                    binding.editText1.text.toString(),
                    binding.editText2.text.toString(),
                    it.toString())) {
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
            binding.editText2.text.toString().let { it1 ->
                wotrPref.userMobile?.let { it2 ->
                    binding.editText1.text.toString().let { it3 ->
                        registerUser(
                            it1,
                            it2,
                            wotrPref.userRegistered,
                            it3,
                            binding.editText3.text.toString()
                        )
                    }
                }
            }
        }
    }

    private fun sendOTP(mobileNumber: String, isUserRegistered: Boolean) {
        showProgress()
        registrationViewModel.sendOTP(mobileNumber,
            isUserRegistered).observe(viewLifecycleOwner) {
            hideProgress()
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == null && it.errorData != null) {
                        showGenericError()
                    } else {
                        showResendButton(false)
                        startResendOTPTimer(180)
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

    private fun registerUser(emailAddress: String,
                             mobileNumber: String,
                             isUserRegistered: Boolean,
                             fullName: String,
                             otp: String
    ) {
        showProgress()
        registrationViewModel.registerUser(
            emailAddress,
            mobileNumber,
            isUserRegistered,
            fullName,
            otp
        ).observe(viewLifecycleOwner) {
            hideProgress()
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == null && it.errorData != null) {
                        if (it.errorData.errorCode == "LOGIN_ERR_003")
                            showInvalidOTPInlineError(true)
                        else
                            showGenericError()
                    } else {
                        navigateToRegistrationSuccessFragment()
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

    private fun startResendOTPTimer(seconds: Int) {
        binding.textView2.visibility = View.VISIBLE
        object : CountDownTimer((seconds*1000).toLong(), 1000) {

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

    private fun enableSubmitButton(enable: Boolean) {
        if (enable && checkForOTPField()) {
            binding.loginButton.isEnabled = true
            binding.loginButton.isClickable = true
        } else {
            binding.loginButton.isEnabled = false
            binding.loginButton.isClickable = false
        }
    }

    private fun checkForOTPField(): Boolean {
        return (binding.editText3.text.toString().length == 4)
    }

    private fun checkDataComplete(name: String?, email: String?, otp: String?): Boolean{
        val nameValid: Boolean = !name.isNullOrEmpty() && name.matches(Regex(NAME_REGEX))
        val emailValid: Boolean = email.isNullOrEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val otpValid: Boolean = !name.isNullOrEmpty() && otp?.length == 4
        if (nameValid && emailValid && otpValid) return true
        return false
    }

    private fun showProgress() {
        enableNameField(false)
        enableEmailField(false)
        enableOTPField(false)
        enableSubmitButton(false)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        enableNameField(true)
        enableEmailField(true)
        enableOTPField(true)
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun enableMobileField(enable: Boolean) {
        binding.editText.isEnabled = enable
        binding.editText.isClickable = enable
    }

    private fun enableNameField(enable: Boolean) {
        binding.editText1.isEnabled = enable
        binding.editText1.isClickable = enable
    }

    private fun enableEmailField(enable: Boolean) {
        binding.editText2.isEnabled = enable
        binding.editText2.isClickable = enable
    }

    private fun enableOTPField(enable: Boolean) {
        binding.editText3.isEnabled = enable
        binding.editText3.isClickable = enable
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
            binding.editText3.text.clear()
            binding.textView2.visibility = View.VISIBLE
            binding.textView2.setText("Incorrect OTP. Please try again!")
            showResendButton(true)
        }
    }

    private fun navigateToRegistrationSuccessFragment() {
        val action = RegistrationFragmentDirections
            .actionRegistrationFragmentToRegistrationSuccessFragment()
        binding.root.findNavController().navigate(action)
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

//    private fun navigateToEnterOTPFragment() {
//        val action = RegistrationFragmentDirections
//            .actionRegistrationFragmentToEnterOTPFragment()
//        binding.root.findNavController().navigate(action)
//    }
}