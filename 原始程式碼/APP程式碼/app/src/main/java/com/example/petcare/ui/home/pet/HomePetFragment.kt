package com.example.petcare.ui.home.pet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.petcare.R
import com.example.petcare.databinding.FragmentHomeBinding
import com.example.petcare.databinding.FragmentHomePetBinding
import com.example.petcare.ui.settings.petsInfo.PetViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomePetFragment : Fragment() {
    private var _binding: FragmentHomePetBinding? = null
    private lateinit var selectedPet: String
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var pid: String



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePetBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        //顯示寵物資料
        val model = ViewModelProvider(requireActivity())[PetViewModel::class.java]
        model.message.observe(viewLifecycleOwner, Observer {
            selectedPet = it
            binding.homePetName.text = it
            if(uid.isNotEmpty()){
                getUserData()
            }

        })


        //餵食
        binding.feedBtn.setOnClickListener {
            if (uid != null) {
                    if(binding.homePetCount.text!="沒有設備訊息") {
                        val time = Calendar.getInstance().time
                        //val date = SimpleDateFormat("dd", Locale.getDefault()).toString().toInt()
                        //val hour = SimpleDateFormat("HH", Locale.getDefault()).toString().toInt()
                        //val minute = SimpleDateFormat("mm", Locale.getDefault()).toString().toInt()
                        //val current = formatter.format(time)
                        databaseReference.child("$uid/petList/$pid/SG90/switch").setValue(true)
                        databaseReference.child("$uid").child("petList/$pid/SG90").child("time")
                            .setValue(time.time)
                        databaseReference.child("$uid").child("petList/$pid/SG90").child("timedate")
                            .setValue(time.date)
                        databaseReference.child("$uid").child("petList/$pid/SG90").child("timehour")
                            .setValue(time.hours)
                        databaseReference.child("$uid").child("petList/$pid/SG90")
                            .child("timeminute").setValue(time.minutes)
                        databaseReference.child("$uid").child("petList/$pid/SG90").child("switcher")
                            .setValue("by user")
                        Toast.makeText(getActivity(), "已餵食"+selectedPet, Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(activity,"失敗!無設備資訊", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

    //顯示寵物資料
    private fun getUserData() {
        databaseReference.child("$uid/petList").addValueEventListener(object: ValueEventListener {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds in snapshot.children){
                    val name =snapshot.child(ds.key.toString()).child("name").value.toString()
                    if(name=="$selectedPet"){
                        binding.homePetAge.text = snapshot.child(ds.key.toString()+"/age").value.toString()
                        binding.homePetType.text = snapshot.child(ds.key.toString()+"/type").value.toString()
                        binding.homePetSex.text = snapshot.child(ds.key.toString()+"/gender").value.toString()
                        var amount=snapshot.child(ds.key.toString()+"/SG90/feedamount").value.toString()
                        var hourint=snapshot.child(ds.key.toString()+"/SG90/hourinterval").value.toString()
                        var minint=snapshot.child(ds.key.toString()+"/SG90/minuteinterval").value.toString()
                        var autohour=snapshot.child(ds.key.toString()+"/SG90/autohour").value.toString()
                        var automin=snapshot.child(ds.key.toString()+"/SG90/autominute").value.toString()
                        if(amount!="null"){
                            binding.homePetCount.text = amount+"份/次"
                        }else{
                            binding.homePetCount.text="沒有設備訊息"
                        }

                        if(hourint!="null" && minint!="null"){
                            binding.homePetTime.text = hourint+"小時"+minint+"分鐘"
                        }else{
                            binding.homePetTime.text="沒有設備訊息"
                        }

                        if(autohour!="null" && automin!="null"){
                            binding.homePetFeed.text = autohour+"小時"+automin+"分鐘"
                        }else{
                            binding.homePetFeed.text="沒有設備訊息"
                        }

                        pid=ds.key.toString()
                    }
                }

            }
            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }
        })

    }


}