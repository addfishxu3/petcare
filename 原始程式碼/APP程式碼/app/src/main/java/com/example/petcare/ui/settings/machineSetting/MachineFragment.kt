package com.example.petcare.ui.settings.machineSetting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.petcare.R
import com.example.petcare.databinding.FragmentMachineBinding
import com.example.petcare.ui.settings.SettingsFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MachineFragment : Fragment() {
    private var _binding: FragmentMachineBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    private var spinnerList = ArrayList<String>()
    private lateinit var selectedPet: String
    private lateinit var checkdev: String
    private lateinit var pid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMachineBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        if(uid.isNotEmpty()){
            getUserData()
        }

        binding.choosePetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPet = binding.choosePetSpinner.selectedItem.toString()
                //model.sendMsg(selectedPet)
                databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(ds in snapshot.child("petList").children) {
                            val name = snapshot.child("petList").child(ds.key.toString())
                                .child("name").value.toString()
                            if (name == selectedPet) {
                                checkdev=snapshot.child("petList").child(ds.key.toString()).child("SG90").value.toString()
                                var amount=snapshot.child("petList").child(ds.key.toString()).child("SG90").child("feedamount").value.toString()
                                var hourint=snapshot.child("petList").child(ds.key.toString()).child("SG90/hourinterval").value.toString()
                                var minint=snapshot.child("petList").child(ds.key.toString()).child("SG90/minuteinterval").value.toString()
                                var autohour=snapshot.child("petList").child(ds.key.toString()).child("SG90/autohour").value.toString()
                                var automin=snapshot.child("petList").child(ds.key.toString()).child("SG90/autominute").value.toString()
                                if(amount!= "null"){
                                    binding.amount.setText(amount)
                                }else{
                                    binding.amount.setText(null)
                                }
                                if(hourint != "null" && minint != "null"){
                                    binding.hourinterval.setText(hourint)
                                    binding.minuteinterval.setText(minint)
                                }else{
                                    binding.hourinterval.setText(null)
                                    binding.minuteinterval.setText(null)
                                }
                                if(autohour != "null" && automin != "null"){
                                    binding.feedhourinterval.setText(autohour)
                                    binding.feedminuteinterval.setText(automin)
                                }else{
                                    binding.feedhourinterval.setText(null)
                                    binding.feedminuteinterval.setText(null)
                                }

                                pid=ds.key.toString()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.settingBtn.setOnClickListener {
            //var petName =
            if(checkdev!="null"){
                var amount = binding.amount.text.toString().toInt()
                var hour = binding.hourinterval.text.toString().toInt()
                var minute = binding.minuteinterval.text.toString().toInt()
                var autohour = binding.feedhourinterval.text.toString().toInt()
                var autominute = binding.feedminuteinterval.text.toString().toInt()
                if(amount>=0&&amount<=9){
                    if(hour>=0&&hour<=23&&autohour>=0&&autohour<=23) {
                        if(minute>=0&&minute<=59&&autominute>=0&&autominute<=59){
                            if (uid.isNotEmpty()) {
                                databaseReference.child(uid).child("petList").child(pid).child("SG90").child("feedamount").setValue(amount)
                                databaseReference.child(uid).child("petList").child(pid).child("SG90").child("hourinterval").setValue(hour)
                                databaseReference.child(uid).child("petList").child(pid).child("SG90").child("minuteinterval").setValue(minute)
                                databaseReference.child(uid).child("petList").child(pid).child("SG90").child("autohour").setValue(autohour)
                                databaseReference.child(uid).child("petList").child(pid).child("SG90").child("autominute").setValue(autominute)
                                Toast.makeText(getActivity(),selectedPet+"設備修改成功", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(getActivity(),"分鐘有誤", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(getActivity(),"小時有誤", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(getActivity(),"分量值有誤", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(getActivity(),"失敗!無設備資訊", Toast.LENGTH_SHORT).show()
            }

        }
        val manager = activity?.supportFragmentManager
        val transaction = manager?.beginTransaction()
        binding.cancelBtn.setOnClickListener {
            transaction?.replace(R.id.MachineFragment, SettingsFragment())?.commit()
        }
    }
    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                spinnerList.clear()
                for (ds in snapshot.child("petList").children) {
                    val name =snapshot.child("petList").child(ds.key.toString()).child("name").value.toString()
                    spinnerList.add(name)
                    Log.d("spinnerList", "$spinnerList ")
                }
                val spinnerAdapter = context?.let {

                    ArrayAdapter(
                        it,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        spinnerList
                    )

                }

                binding.choosePetSpinner.adapter = spinnerAdapter

                if(spinnerList.size > 0){
                    selectedPet = spinnerList[0]
                    binding.choosePetSpinner.setSelection(spinnerList.indexOf(selectedPet))
                }


            }
            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }
        })
    }




}