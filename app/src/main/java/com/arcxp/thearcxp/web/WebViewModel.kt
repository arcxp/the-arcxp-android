package com.arcxp.thearcxp.web

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WebViewModel(application: Application) : AndroidViewModel(application) {

    private val _hideBars = MutableLiveData(false)
    val hideBars: LiveData<Boolean> = _hideBars

    fun setHideBars(value: Boolean) {
        _hideBars.postValue(value)
    }
}