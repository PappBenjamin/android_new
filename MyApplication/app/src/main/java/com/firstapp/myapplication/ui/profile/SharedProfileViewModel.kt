package com.firstapp.myapplication.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedProfileViewModel : ViewModel() {
    private val _profileImageUpdated = MutableLiveData<Long>()
    val profileImageUpdated: LiveData<Long> = _profileImageUpdated

    fun notifyImageUpdated(cacheBuster: Long) {
        _profileImageUpdated.value = cacheBuster
    }
}

