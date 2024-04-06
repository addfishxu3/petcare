package com.example.petcare.ui.settings.petsInfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petcare.data.PetData

class PetViewModel: ViewModel() {
    private val repository: PetRepository = PetRepository().getInstance()
    private val _allPets = MutableLiveData<List<PetData>>()
    val allPets: LiveData<List<PetData>> = _allPets
    val message = MutableLiveData<String>()

    init {
        repository.loadPets(_allPets)
    }

    fun sendMsg(text: String) {
        message.value = text
    }
}