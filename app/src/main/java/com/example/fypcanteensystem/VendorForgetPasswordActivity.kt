package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.fypcanteensystem.databinding.ActivityVendorForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class VendorForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityVendorForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Vendor Forget Password")

        binding.btnSubmitForgotPassword.setOnClickListener(){
            resetUserPassword()
        }
    }

    private fun resetUserPassword() {
        val email: String = binding.emailEditText.text.toString().trim{ it <= ' '}
        if(email.isEmpty()){
            Toast.makeText(this@VendorForgetPasswordActivity, "Please Enter Email Address", Toast.LENGTH_SHORT).show()
        }
        else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        Toast.makeText(this@VendorForgetPasswordActivity, "Email sent successfully to reset your password"
                            , Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
                        Toast.makeText(this@VendorForgetPasswordActivity, task.exception!!.message.toString()
                            , Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}