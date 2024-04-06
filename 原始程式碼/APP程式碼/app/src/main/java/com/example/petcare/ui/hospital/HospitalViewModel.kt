package com.example.petcare.ui.hospital

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HospitalViewModel : ViewModel() {
    val message = MutableLiveData<String>()

    fun sendMsg(text: String) {
        message.value = text
    }
}