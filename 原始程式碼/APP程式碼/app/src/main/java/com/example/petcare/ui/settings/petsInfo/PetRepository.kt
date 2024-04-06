package com.example.petcare.ui.settings.petsInfo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.petcare.data.PetData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PetRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val uid: String = auth.currentUser?.uid.toString()
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("User")
    private var INSTANCE: PetRepository? = null

    fun getInstance(): PetRepository {
        return INSTANCE?: synchronized(this) {
            val instance = PetRepository()
            INSTANCE = instance
            instance
        }
    }

    fun loadPets(petList: MutableLiveData<List<PetData>>) {
        databaseReference.child("$uid/petList").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val _petList: List<PetData> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(PetData::class.java)!!
                    }
                    petList.postValue(_petList)
                } catch ( _: Exception) {
                    Log.d("ERROR", "ERROR")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}