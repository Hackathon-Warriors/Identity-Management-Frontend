package com.thales.idverification.modules.login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.thales.idverification.databinding.FragmentRegistrationSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationSuccessFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            navigateToLoginFragment()
        }
    }

    private fun navigateToLoginFragment() {
        val action = RegistrationSuccessFragmentDirections
            .actionRegistrationSuccessFragmentToLoginFragment()
        binding.root.findNavController().navigate(action)
    }
}