package com.example.petcare.ui.settings.petsInfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.petcare.R
import com.example.petcare.data.PetData
import com.example.petcare.databinding.FragmentAddPetBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import org.w3c.dom.Comment


class AddPetFragment : Fragment() {
    private var _binding: FragmentAddPetBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPetBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = activity?.supportFragmentManager
        val transaction = manager?.beginTransaction()


        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        if(uid.isNotEmpty()){
            getUserData()
        }

        binding.petSubmitBtn.setOnClickListener {
            val petName = binding.editTextTextPetName.text.toString()
            val petAge = binding.editTextPetAge.text.toString()
            val petType = binding.editTextTextPetType.text.toString()
            radioGroup = view.findViewById(R.id.petGenderGroup)
            Log.d("radioGroup", "$radioGroup")
            val selectedId: Int = radioGroup.checkedRadioButtonId
            Log.d("selectedId", "${radioGroup.checkedRadioButtonId}")
            radioButton = view.findViewById(selectedId)
            Log.d("radioButton", "$radioButton")

            val petList = PetData(petName, petType, radioButton.text.toString(), petAge)

            if (petName.isNotEmpty() && petAge.isNotEmpty() && petType.isNotEmpty() && radioButton.text.toString().isNotEmpty()) {
                var r = (Math.random()*999).toInt()+1
                databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(ds in snapshot.child("petList").children) {
                            val pid = ds.key.toString()
                           if(pid==r.toString()){
                               r=(Math.random()*999).toInt()+1
                           }
                        }
                    }

                    // Failed to read value
                    override fun onCancelled(error: DatabaseError) {
                        Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
                    }
                })
                databaseReference.child(uid).child("petList").child(r.toString()).setValue(petList)
                transaction?.replace(R.id.AddPetFragment, PetsInfoFragment())?.commit()

            }
        }

        binding.petCancelBtn.setOnClickListener {
            transaction?.replace(R.id.AddPetFragment, PetsInfoFragment())?.commit()

        }
    }


    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.ownerName.setText(snapshot.child("user").value.toString())
            }

            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }
        })
    }


}