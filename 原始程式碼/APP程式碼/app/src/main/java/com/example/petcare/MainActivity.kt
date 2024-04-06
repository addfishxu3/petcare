package com.example.petcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.petcare.databinding.ActivityMainBinding
import com.example.petcare.ui.settings.petsInfo.PetDetailFragment
import com.example.petcare.ui.settings.petsInfo.PetsInfoFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    // 身分驗證
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //隱藏action bar
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        // 初始化 FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")


        binding.loginBtn.setOnClickListener {
            val mailText = binding.editTextTextPersonName.text.toString()
            val pwdText = binding.editTextTextPassword.text.toString()

            if (mailText.isNotEmpty() && pwdText.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(mailText, pwdText).addOnCompleteListener{
                    if (it.isSuccessful){
                        Snackbar.make(binding.root, "登入成功", Snackbar.LENGTH_SHORT).show()
                        // 以防資料庫的密碼因為郵件而沒有更新，所以在這邊更新
                        databaseReference.child(firebaseAuth.currentUser?.uid.toString())
                            .child("pwd").setValue(pwdText)

                        // 轉換到app首頁
                        val intent = Intent(applicationContext, MainPage::class.java, )
                        startActivity(intent)
                    }else{
                        Snackbar.make(binding.root, "請再次確認登入資訊", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } else{
                Snackbar.make(binding.root, "您仍有欄位未填寫", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.signupBtn.setOnClickListener {
            //轉換到註冊頁面
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        binding.forgetPassword.setOnClickListener {
            //轉換到忘記密碼頁面
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }
}