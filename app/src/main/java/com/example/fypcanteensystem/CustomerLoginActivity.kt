package com.example.fypcanteensystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.fypcanteensystem.databinding.ActivityCustomerLoginBinding
import com.google.firebase.auth.FirebaseAuth

class CustomerLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Customer Login")

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener()
        {
            if(TextUtils.isEmpty(binding.emailEditText.text.toString())){
                binding.emailContainer.setError("*Required!")
                return@setOnClickListener
            }
            else{
                binding.emailContainer.error = null
            }

            if(TextUtils.isEmpty(binding.passwordEditText.text.toString())) {
                binding.passwordContainer.setError("*Required!")
                binding.passwordContainer.errorIconDrawable = null
                return@setOnClickListener
            }
            else{
                binding.passwordContainer.error = null
            }

            loginUser(binding.emailEditText.text.toString(),binding.passwordEditText.text.toString())
        }


        binding.registerText.setOnClickListener(){
            startActivity(Intent(this@CustomerLoginActivity,CustomerRegistrationActivity::class.java))
        }

        binding.forgotPasswordText.setOnClickListener() {
            startActivity(Intent(this@CustomerLoginActivity,CustomerForgetPasswordActivity::class.java))
        }

        binding.btnSwitchToVendor.setOnClickListener(){
            startActivity(Intent(this@CustomerLoginActivity,VendorLoginActivity::class.java))
        }
    }

    private fun loginUser(email: String, psw: String) {

        auth.signInWithEmailAndPassword(email,psw)
            .addOnCompleteListener{
                if(it.isSuccessful)
                {
                    Toast.makeText(this@CustomerLoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@CustomerLoginActivity,FoodOrderingModuleActivity::class.java))
                    finish()
                }
                else
                {
                    Toast.makeText(this@CustomerLoginActivity, "Incorrect Email Address or Password", Toast.LENGTH_LONG).show()
                }
            }
    }
}