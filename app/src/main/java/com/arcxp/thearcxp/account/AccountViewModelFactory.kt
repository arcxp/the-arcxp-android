package com.arcxp.thearcxp.account

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager

class AccountViewModelFactory(
    private val application: Application,
    private val analyticsManager: FirebaseAnalyticsManager?
) : ViewModelProvider.NewInstanceFactory() { // Use NewInstanceFactory for simplicity

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(app = application, analyticsManager = analyticsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}