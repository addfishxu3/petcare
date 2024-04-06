package com.example.petcare.ui.home.record

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.petcare.MainActivity
import com.example.petcare.MainPage
import com.example.petcare.R
import com.example.petcare.databinding.FragmentHomeBinding
import com.example.petcare.databinding.FragmentHomePetBinding
import com.example.petcare.databinding.FragmentHomeRecordBinding
import com.example.petcare.ui.home.pet.HomePetFragment
import com.example.petcare.ui.settings.petsInfo.PetViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class HomeRecordFragment : Fragment() {
    private var _binding: FragmentHomeRecordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var selectedPet: String
    private lateinit var pid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeRecordBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        val model = ViewModelProvider(requireActivity())[PetViewModel::class.java]
        model.message.observe(viewLifecycleOwner, Observer {
            selectedPet = it

            if(uid.isNotEmpty()){
                //getPidData()
                getUserData()
            }
        })


    }

    // 更新資料的方法
    fun getUserData() {
        // TODO: 更新資料
        databaseReference.child("$uid/petList").addValueEventListener(object: ValueEventListener {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds in snapshot.children){
                    val name =snapshot.child(ds.key.toString()).child("name").value.toString()
                    if(name=="$selectedPet"){
                        pid=ds.key.toString()
                    }
                }
                val time=snapshot.child("$pid/SG90/time").value.toString()
                val gettime : String
                if(time!="null"){
                    val time=snapshot.child("$pid/SG90/time").value.toString().toLong()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    gettime = formatter.format(time)
                }else{
                    gettime= "沒有設備訊息"
                }

                var switcher=snapshot.child("$pid/SG90/switcher").value.toString()
                if(switcher=="by user"){
                    switcher="飼主餵食"
                }else if(switcher=="by pet"){
                    switcher="寵物取食"
                }else if(switcher=="by auto"){
                    switcher="自動餵食"
                }else{
                    switcher="沒有設備訊息"
                }
                var amount=snapshot.child("$pid/SG90/feedamount").value.toString()
                if(amount!="null"){
                    amount.toLong()
                }else{
                    amount="沒有設備訊息"
                }

                binding.record.text =  gettime
                binding.recordswitcher.text = switcher
                binding.recordamount.text =  amount
            }

            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }

        })

    }

    override fun onHiddenChanged(hidden: Boolean){
        super.onHiddenChanged(hidden)
        if(!hidden){
            auth = FirebaseAuth.getInstance()
            uid = auth.currentUser?.uid.toString()
            databaseReference = FirebaseDatabase.getInstance().getReference("User")

            val model = ViewModelProvider(requireActivity())[PetViewModel::class.java]
            model.message.observe(viewLifecycleOwner, Observer {
                selectedPet = it

                if(uid.isNotEmpty()){
                    //getPidData()
                    getUserData()
                }
            })

        }

    }

}