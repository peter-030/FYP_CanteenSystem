package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.fypcanteensystem.databinding.ActivityCustomerForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class CustomerForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCustomerForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("User Forget Password")

        binding.btnSubmitForgotPassword.setOnClickListener(){
            resetUserPassword()
        }
    }
    private fun resetUserPassword() {
        val email: String = binding.emailEditText.text.toString().trim{ it <= ' '}
        if(email.isEmpty()){
            Toast.makeText(this@CustomerForgetPasswordActivity, "Please Enter Email Address", Toast.LENGTH_SHORT).show()
        }
        else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        Toast.makeText(this@CustomerForgetPasswordActivity, "Email sent successfully to reset your password"
                            , Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
                        Toast.makeText(this@CustomerForgetPasswordActivity, task.exception!!.message.toString()
                            , Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}