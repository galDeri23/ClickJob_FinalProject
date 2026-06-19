package com.example.clickjob_finalproject.ui.myjobs

import androidx.lifecycle.ViewModel
import com.example.clickjob_finalproject.adapters.JobTabType

// Survives navigation between screens so the user's last chosen mode and tab
// are restored when they come back via the bottom nav bar.
// Ignored when the user arrives from the "פרסום משרה" button in HomeFragment,
// which forces employer mode + history tab via an argument.
class MyJobsViewModel : ViewModel() {
    var isWorkerMode: Boolean = true
    var currentTab: JobTabType = JobTabType.ACTIVE
}