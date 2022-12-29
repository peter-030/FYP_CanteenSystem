package com.example.fypcanteensystem

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.fypcanteensystem.databinding.ActivityVendorLoginBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class VendorLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendorLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: androidx.biometric.BiometricPrompt
    private lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Vendor Login")

        //face recognition purpose
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = androidx.biometric.BiometricPrompt(this@VendorLoginActivity,executor,object
            :androidx.biometric.BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                //Toast.makeText(this@VendorLoginActivity, "Auth Error", Toast.LENGTH_LONG).show()

            }

            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                //Toast.makeText(this@VendorLoginActivity, "Auth successful", Toast.LENGTH_LONG).show()
                var email:String?=null
                email = sharedPreferences.getString("email","")
                var password:String? =null
                password = sharedPreferences.getString("password","")

                loginUser(email!!,password!!)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                //Toast.makeText(this@VendorLoginActivity, "Auth Failed", Toast.LENGTH_LONG).show()

            }

            })

        //setup title, subtitle on auth dialog
        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Login using fingerprint or face Id")
            .setNegativeButtonText("Cancel")
            .build()


        //below is previous normal without biometric login
        auth = FirebaseAuth.getInstance()

        //keep user sign in, they will only log out when they click log out button
        if(auth.currentUser!=null){
            startActivity(Intent(this@VendorLoginActivity,VendorDashboardActivity::class.java))
            finish()
        }

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

        binding.btnFingerPrintLogin.setOnClickListener{
            biometricPrompt.authenticate(promptInfo)
        }

        binding.registerText.setOnClickListener(){
            startActivity(Intent(this@VendorLoginActivity,VendorRegistrationActivity::class.java))
        }

        binding.forgotPasswordText.setOnClickListener()
        {
            startActivity(Intent(this@VendorLoginActivity,VendorForgetPasswordActivity::class.java))
        }

        binding.btnSwitchToCustomer.setOnClickListener()
        {
            startActivity(Intent(this@VendorLoginActivity,CustomerLoginActivity::class.java))
        }
    }

    private fun loginUser(email: String, psw: String) {

        auth.signInWithEmailAndPassword(email,psw)
            .addOnCompleteListener{
                if(it.isSuccessful)
                {
                    var editor :SharedPreferences.Editor
                    editor=getSharedPreferences("data", MODE_PRIVATE).edit()
                    editor.putString("email",email)
                    editor.putString("password",psw)
                    editor.apply()

                    Toast.makeText(this@VendorLoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@VendorLoginActivity,VendorDashboardActivity::class.java))
                    finish()
                }
                else
                {
                    Toast.makeText(this@VendorLoginActivity, "Incorrect Email Address or Password", Toast.LENGTH_LONG).show()
                }
            }
    }
}