package com.example.petcare.ui.settings.petsInfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.petcare.R
import com.example.petcare.data.PetData
import com.example.petcare.databinding.FragmentPetDetailBinding
import com.example.petcare.ui.home.HomeFragment
import com.example.petcare.ui.home.pet.HomePetFragment
import com.example.petcare.ui.home.record.HomeRecordFragment
import com.example.petcare.ui.home.video.HomeVideoFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.snapshots


class PetDetailFragment : Fragment() {
    private var _binding: FragmentPetDetailBinding?= null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var clickedItemName: String
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton
    private lateinit var petName: String
    private lateinit var petId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = activity?.supportFragmentManager
        val transaction = manager?.beginTransaction()

        val model = ViewModelProvider(requireActivity())[PetViewModel::class.java]
        model.message.observe(viewLifecycleOwner, Observer {
            clickedItemName = it
            binding.detailPetName.setText(it)
        })

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        if(uid.isNotEmpty()){
            getUserData()
        }

        // 儲存寵物資料
        binding.detailPetSubmitBtn.setOnClickListener {
            radioGroup = view.findViewById(R.id.detailPetGenderGroup);
            val selectedId: Int = radioGroup.checkedRadioButtonId
            radioButton = view.findViewById(selectedId)
            val gender = radioButton.text.toString()
            changeInfo(gender)
            // updateData()
            transaction?.replace(R.id.PetDetailFragment, PetsInfoFragment())?.commit()
        }

        // 刪除寵物資料
        binding.detailPetDeleteBtn.setOnClickListener {
            databaseReference.child(uid).child("petList").child(petId).removeValue()
            transaction?.replace(R.id.PetDetailFragment, PetsInfoFragment())?.commit()
        }


        // 取消執行
        binding.detailPetCancelBtn.setOnClickListener {
            transaction?.replace(R.id.PetDetailFragment, PetsInfoFragment())?.commit()
        }


    }


    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("clickedItemName", clickedItemName)

                for(ds in snapshot.child("petList").children) {
                    petName = snapshot.child("petList").child(ds.key.toString())
                        .child("name").value.toString()
                    if (petName == clickedItemName) {
                        petId = ds.key.toString()
                        Log.d(petId, "${ds.key.toString()}")
                        binding.ownerNameDetail.setText(snapshot.child("user").value.toString())
                        binding.detailPetAge.setText(snapshot.child("petList").child(ds.key.toString()).child("age").value.toString())
                        binding.editTextTextPetTypeDetail.setText(snapshot.child("petList").child(ds.key.toString()).child("type").value.toString())
                        if (snapshot.child("petList").child(ds.key.toString()).child("gender").value.toString() == "公") {
                            binding.detailMaleRadioBtn.isChecked = true
                            binding.detailFemaleRadioBtn.isChecked = false
                        } else {
                            binding.detailFemaleRadioBtn.isChecked = true
                            binding.detailMaleRadioBtn.isChecked = false
                        }
                    }
                }

            }

            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun changeInfo(gender: String) {
        val petName = binding.detailPetName.text.toString()
        val petAge = binding.detailPetAge.text.toString()
        val petType = binding.editTextTextPetTypeDetail.text.toString()
        val petList = PetData(petName, petType, gender, petAge)

        if (petName.isNotEmpty() && petAge.isNotEmpty() && petType.isNotEmpty() && gender.isNotEmpty()) {

                databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("clickedItemName", clickedItemName)

                        for(ds in snapshot.child("petList").children) {
                            val name = snapshot.child("petList").child(ds.key.toString())
                                .child("name").value.toString()
                            if (name == clickedItemName) {
                                if (clickedItemName != petName) {
                                    //有改petname
                                    databaseReference.child(uid).child("petList")
                                        .child(ds.key.toString()).removeValue()
                                    clickedItemName = petName
                                }
                                databaseReference.child(uid).child("petList")
                                    .child(ds.key.toString()).child("age").setValue(petAge)
                                databaseReference.child(uid).child("petList")
                                    .child(ds.key.toString()).child("gender").setValue(gender)
                                databaseReference.child(uid).child("petList")
                                    .child(ds.key.toString()).child("name").setValue(petName)
                                databaseReference.child(uid).child("petList")
                                    .child(ds.key.toString()).child("type").setValue(petType)

                            }

                            Snackbar.make(binding.root, "成功儲存資料", Snackbar.LENGTH_SHORT).show()
                        }

                    }
                    // Failed to read value
                    override fun onCancelled(error: DatabaseError) {
                        Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
                    }
                })


        }else{
            Snackbar.make(binding.root, "更新的資料不符合規則", Snackbar.LENGTH_SHORT).show()
        }
    }

}