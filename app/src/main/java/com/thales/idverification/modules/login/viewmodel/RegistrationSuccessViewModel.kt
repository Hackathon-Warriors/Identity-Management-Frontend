package com.thales.idverification.modules.login.viewmodel

import com.thales.idverification.modules.base.BaseViewModel
import com.thales.idverification.repository.WOTRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegistrationSuccessViewModel @Inject constructor(private val repository: WOTRRepository):
    BaseViewModel() {
}