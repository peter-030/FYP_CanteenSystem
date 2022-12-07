package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.example.fypcanteensystem.databinding.ActivityCustomerRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CustomerRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference: DatabaseReference? = null
    private var database: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("customerProfile")

        binding.custSignUpButton.setOnClickListener()
        {

            if (TextUtils.isEmpty(binding.fullNameEditText.text.toString())) {
                binding.fullNameContainer.setError("*Required!")
                return@setOnClickListener
            } else {
                binding.fullNameContainer.error = null
            }

            if (TextUtils.isEmpty(binding.emailEditText.text.toString())) {
                binding.emailContainer.setError("*Required!")
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEditText.text.toString())
                    .matches()
            ) {
                binding.emailContainer.setError("*Invalid Email Address!")
                return@setOnClickListener
            } else {
                binding.emailContainer.error = null
            }

            if (TextUtils.isEmpty(binding.passwordEditText.text.toString())) {
                binding.passwordContainer.setError("*Required!")
                binding.passwordContainer.errorIconDrawable = null
                return@setOnClickListener
            } else if (binding.passwordEditText.text.toString().length < 6) {
                binding.passwordContainer.setError("*Minimum 6 Character Password!")
                binding.passwordContainer.errorIconDrawable = null
                return@setOnClickListener
            } else {
                binding.passwordContainer.error = null
            }

            if (TextUtils.isEmpty(binding.phoneEditText.text.toString())) {
                binding.phoneContainer.setError("*Required!")
                return@setOnClickListener
            } else if (!binding.phoneEditText.text.toString()
                    .matches("^(\\+?6?01)[02-46-9]-*[0-9]{7}\$|^(\\+?6?01)[1]-*[0-9]{8}\$".toRegex())
            ) {
                binding.phoneContainer.setError("*Invalid Phone Number format!")
                return@setOnClickListener
            } else {
                binding.phoneContainer.error = null
            }

            if (TextUtils.isEmpty(binding.studentIdEditText.text.toString())) {
                binding.studentIdContainer.setError("*Required!")
                return@setOnClickListener
            } else {
                binding.studentIdContainer.error = null
            }

            registerCust(
                binding.emailEditText.text.toString(), binding.passwordEditText.text.toString(),
                binding.fullNameEditText.text.toString(), binding.phoneEditText.text.toString(),
                binding.studentIdEditText.text.toString()
            )

        }
        this.setTitle("User Sign Up")


    }

    private fun registerCust(
        email: String,
        psw: String,
        name: String,
        phoneNo: String,
        studentId: String
    ) {
        auth.createUserWithEmailAndPassword(email, psw)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val currentUser = auth.currentUser
                    val currentUserDb = databaseReference?.child((currentUser?.uid!!))
                    currentUserDb?.child("Full Name")?.setValue(name)
                    currentUserDb?.child("Phone Number")?.setValue(phoneNo)
                    currentUserDb?.child("Student Id")?.setValue(studentId)

                    Toast.makeText(
                        this@CustomerRegistrationActivity,
                        "Registration Successful",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@CustomerRegistrationActivity,
                        it.exception?.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}