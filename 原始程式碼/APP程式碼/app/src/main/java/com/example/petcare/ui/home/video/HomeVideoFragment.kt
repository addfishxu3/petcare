package com.example.petcare.ui.home.video

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.petcare.R
import com.example.petcare.databinding.FragmentHomeVideoBinding
import com.example.petcare.ui.settings.petsInfo.PetViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class HomeVideoFragment : Fragment() {
    private var _binding: FragmentHomeVideoBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedPet: String
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var pid: String
    private lateinit var intervalsec : String
    private var checkcam:String="null"





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeVideoBinding.inflate(inflater, container, false)


        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        val model = ViewModelProvider(requireActivity())[PetViewModel::class.java]
        model.message.observe(viewLifecycleOwner, Observer {
            selectedPet = it
            getdata()

        })

        binding.refreshBtn.setOnClickListener {
            getdata()
        }

        binding.settingBtn.setOnClickListener {
            if(intervalsec!="null"){
                if(binding.intervalsec.text.toString()!="") {
                    val interval = binding.intervalsec.text.toString().toInt()
                    if (interval >= 10) {
                        databaseReference.child(uid).child("petList").child(pid)
                            .child("Cam/intervalsec").setValue(interval)
                        Toast.makeText(activity, "修改成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "秒數有誤", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(activity,"失敗!無數值", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(activity,"失敗!無設備資訊", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun getdata(){
        if(uid.isNotEmpty()){
            databaseReference.child("$uid/petList").addValueEventListener(object: ValueEventListener {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children) {

                        val name = snapshot.child(ds.key.toString()).child("name").value.toString()
                        if (name == "$selectedPet") {
                            intervalsec=snapshot.child(ds.key.toString()+"/Cam/intervalsec").value.toString()
                            if(intervalsec!="null"){
                                binding.intervalsec.setText(intervalsec)
                                checkcam="check"
                                binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.s__60211206))
                                binding.imageView.isVisible
                                val storageRef= FirebaseStorage.getInstance().reference.child("pic/$uid/"+ds.key.toString()+"/S__60252174.jpg")
                                val localfile= File.createTempFile("tempImage","jpg")
                                storageRef.getFile(localfile).addOnSuccessListener {
                                    val bitmap=BitmapFactory.decodeFile(localfile.absolutePath)
                                    binding.imageView.setImageBitmap(bitmap)
                                }.addOnFailureListener{
                                    Snackbar.make(binding.root, "testfail", Snackbar.LENGTH_SHORT).show()
                                }

                            }else{
                                binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.s__60211206))
                                binding.intervalsec.setText(null)
                                checkcam="null"
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

}