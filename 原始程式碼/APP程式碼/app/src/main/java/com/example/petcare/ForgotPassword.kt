package com.example.petcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.petcare.databinding.ActivityForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding
    private val emailpatten = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnFogotPsw.setOnClickListener {
            val email = binding.forgotMail.text.toString()
            if (email.matches(emailpatten.toRegex())){
                auth.sendPasswordResetEmail(email).addOnCompleteListener{
                    if (it.isSuccessful){
                        Snackbar.make(binding.root, "郵件已寄出，請到信箱確認", Snackbar.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Snackbar.make(binding.root, "請輸入正確的電子郵件", Snackbar.LENGTH_SHORT).show()
                    }

                }
            }else{
                Snackbar.make(binding.root, "信箱格式不符", Snackbar.LENGTH_SHORT).show()
            }

        }
    }



}