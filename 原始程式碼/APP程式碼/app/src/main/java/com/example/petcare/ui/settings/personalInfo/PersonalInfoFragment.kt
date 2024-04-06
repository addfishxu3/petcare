package com.example.petcare.ui.settings.personalInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petcare.R
import com.example.petcare.databinding.FragmentPersonalInfoBinding
import com.example.petcare.ui.settings.SettingsFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
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

        binding.submitBtn.setOnClickListener {
            //按下儲存後更新DB資料
            changeInfo()
        }

        binding.cancelBtn.setOnClickListener {
            transaction?.replace(R.id.PersonalInfoFragment, SettingsFragment())?.commit()
        }
    }

    private fun changeInfo() {
        // 讀取使用者資訊 mail被鎖住不能改變數值 所以就不弄出來
        val userName = binding.editTextTextPersonNameSetting.text.toString()
        val userPsw = binding.editTextTextPasswordSetting.text.toString()
        val userPhone = binding.editTextPhoneSetting.text.toString()
        val user = auth.currentUser
        if (userName.isNotEmpty() && userPsw.isNotEmpty()  && userPhone.isNotEmpty() && userPhone.length == 10 && checkPassword()) {
            user?.updatePassword(userPsw)?.addOnCompleteListener{
                if (it.isSuccessful){
                    databaseReference.child(uid)
                        .child("user").setValue(userName)
                    databaseReference.child(uid)
                        .child("pwd").setValue(userPsw)
                    databaseReference.child(uid)
                        .child("phone").setValue(userPhone)
                    Snackbar.make(binding.root, "成功儲存資料", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(binding.root, "錯誤", Snackbar.LENGTH_SHORT).show()
                }
            }
        }else{
            Snackbar.make(binding.root, "更新的資料不符合規則", Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun checkPassword(): Boolean {
        if (binding.editTextTextPasswordSetting.text.toString() == ""){
            return false
        }else if (binding.editTextTextPasswordSetting.text.length < 6){
            return false
        }
        return true
    }

    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object: ValueEventListener{
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.editTextTextPersonNameSetting.setText(snapshot.child("user").value.toString())
                binding.editTextTextPasswordSetting.setText(snapshot.child("pwd").value.toString())
                binding.editTextTextEmailAddressSetting.setText(snapshot.child("email").value.toString())
                binding.editTextPhoneSetting.setText(snapshot.child("phone").value.toString())
            }

            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}