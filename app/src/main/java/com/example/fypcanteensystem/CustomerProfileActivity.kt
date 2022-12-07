package com.example.fypcanteensystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.example.fypcanteensystem.databinding.ActivityCustomerProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class CustomerProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCustomerProfileBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference : DatabaseReference? =null
    private var database : FirebaseDatabase? =null
    private lateinit var ImageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        this.setTitle("User Profile")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("customerProfile")

        loadCustomerProfile()

        binding.btnSelectUserImg.setOnClickListener(){
            selectUserImg()
        }

        binding.btnUpdate.setOnClickListener(){

            if(TextUtils.isEmpty(binding.fullNameEditText.text.toString())){
                binding.fullNameContainer.setError("*Required!")
                return@setOnClickListener
            }
            else{
                binding.fullNameContainer.error = null
            }

            if(TextUtils.isEmpty(binding.phoneEditText.text.toString())){
                binding.phoneContainer.setError("*Required!")
                return@setOnClickListener
            }
            else if(!binding.phoneEditText.text.toString().matches("^(\\+?6?01)[02-46-9]-*[0-9]{7}\$|^(\\+?6?01)[1]-*[0-9]{8}\$".toRegex())){
                binding.phoneContainer.setError("*Invalid Phone Number format!")
                return@setOnClickListener
            }
            else{
                binding.phoneContainer.error = null
            }

            if(TextUtils.isEmpty(binding.studentIdEditText.text.toString())){
                binding.studentIdContainer.setError("*Required!")
                return@setOnClickListener
            }
            else{
                binding.studentIdContainer.error = null
            }


            updateVendorData(binding.fullNameEditText.text.toString(), binding.phoneEditText.text.toString()
                ,binding.studentIdEditText.text.toString())
        }
    }

    private fun selectUserImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent, 100)
    }

    private fun updateVendorData(fullname: String, phoneNo: String, studentId: String) {

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val currentTime = Date()
        val filename = formatter.format(currentTime)
        val storageReference = FirebaseStorage.getInstance().getReference("images/${filename}.png")

        val currentUser = auth.currentUser
        val currentUserDb = databaseReference?.child((currentUser?.uid!!))

        //got error if no input image, click update btn
        if(::ImageUri.isInitialized)
        {
            storageReference.putFile(ImageUri)
                .addOnSuccessListener {
                    val result = it.metadata!!.reference!!.downloadUrl;
                    result.addOnSuccessListener {

                        val imageLink = it.toString()
                        currentUserDb?.child("ImageUri")?.setValue(imageLink)

                    }
                }
        }
        else{

        }

        currentUserDb?.child("Full Name")?.setValue(fullname)
        currentUserDb?.child("Phone Number")?.setValue(phoneNo)
        currentUserDb?.child("Student Id")?.setValue(studentId)
            ?.addOnSuccessListener {
                Toast.makeText(this@CustomerProfileActivity, "Update Successful", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadCustomerProfile() {
        val user = auth.currentUser
        val userReference = databaseReference?.child(user?.uid!!)

        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                var image: Bitmap? = null
                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    val imgURL = snapshot.child("ImageUri").value.toString()

                    // get the image and post it in the ImageView
                    try {
                        val `in` = java.net.URL(imgURL).openStream()
                        image = BitmapFactory.decodeStream(`in`)
                        handler.post {
                            binding.userPicIcon.setImageBitmap(image)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if(user==null){
                    userReference?.removeEventListener(this)
                }

                //load user email,username,phoneNumber
                binding.emailEditText.setText(user?.email)
                binding.fullNameEditText.setText(snapshot.child("Full Name").value.toString())
                binding.phoneEditText.setText(snapshot.child("Phone Number").value.toString())
                binding.studentIdEditText.setText(snapshot.child("Student Id").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.btnLogOut.setOnClickListener(){
            auth.signOut()
            startActivity(Intent(this@CustomerProfileActivity,VendorLoginActivity::class.java))
            finish()
        }
    }

}