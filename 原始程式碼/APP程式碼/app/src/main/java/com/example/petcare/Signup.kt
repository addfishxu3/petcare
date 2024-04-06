package com.example.petcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.petcare.data.UserData
import com.example.petcare.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    // 身分驗證
    private lateinit var firebaseAuth: FirebaseAuth
    // 儲存個人資料
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivitySignupBinding
    private val emailpatten = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //隱藏action bar
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        // 初始化 FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        binding.summitBtn.setOnClickListener {
            // 取得元素中的數值
            val userText = binding.editTextTextPersonNameSignup.text.toString()
            val pwdText = binding.editTextTextPasswordSignup.text.toString()
            val confirmPwdText = binding.editTextTextPasswordConfirm.text.toString()
            val emailText = binding.editTextTextEmailAddress.text.toString()
            val phoneText = binding.editTextPhone.text.toString()

            val users = UserData(userText, pwdText, emailText, phoneText, emptyList())
            // tableName
            databaseReference = FirebaseDatabase.getInstance().getReference("User")

            // 確認欄位都有填寫
            if (userText.isNotEmpty() && pwdText.isNotEmpty() && confirmPwdText.isNotEmpty() && emailText.isNotEmpty() && phoneText.isNotEmpty()) {
                if (pwdText == confirmPwdText && emailText.matches(emailpatten.toRegex()) && phoneText.length == 10) {
                    // 驗證mail跟密碼都通過後才會儲存使用者資料
                    // 之後可以做電話號碼的驗證 之後
                    firebaseAuth.createUserWithEmailAndPassword(emailText, pwdText).addOnCompleteListener{
                        if (it.isSuccessful){
                            // 儲存資料到資料庫中
                            // 用id當作子標
                            databaseReference.child(firebaseAuth.currentUser!!.uid).setValue(users).addOnSuccessListener {
                                Snackbar.make(binding.root, "成功儲存", Snackbar.LENGTH_SHORT).show()
                            }.addOnFailureListener{
                                Snackbar.make(binding.root, "錯誤", Snackbar.LENGTH_SHORT).show()
                            }
                            Snackbar.make(binding.root, "註冊成功", Snackbar.LENGTH_SHORT).show()

                            // 切換為登入介面
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        }else{
                            // 密碼太短會不給過
                            Snackbar.make(binding.root, it.exception.toString(), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }else if(!emailText.matches(emailpatten.toRegex())) {
                    Snackbar.make(binding.root, "信箱格式不符", Snackbar.LENGTH_SHORT).show()
                }else if(phoneText.length != 10) {
                    Snackbar.make(binding.root, "電話長度不符", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(binding.root, "確認密碼不相符", Snackbar.LENGTH_SHORT).show()
                }

            } else {
                Snackbar.make(binding.root, "您仍有欄位未填寫", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


}