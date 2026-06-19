package com.example.clickjob_finalproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Shared across MainActivity, MyJobsFragment and NotificationsFragment.
// Holds the global worker/employer mode so the bottom nav color stays
// in sync with whichever toggle the user last touched.
class AppViewModel : ViewModel() {

    private val _isWorkerMode = MutableLiveData(true)
    val isWorkerMode: LiveData<Boolean> = _isWorkerMode

    fun setWorkerMode() {
        _isWorkerMode.value = true
    }

    fun setEmployerMode() {
        _isWorkerMode.value = false
    }
}