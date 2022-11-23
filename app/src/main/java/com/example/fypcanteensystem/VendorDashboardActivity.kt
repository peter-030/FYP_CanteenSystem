package com.example.fypcanteensystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.fypcanteensystem.databinding.ActivityVendorDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.Executors
import android.util.Log
class VendorDashboardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityVendorDashboardBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference : DatabaseReference? =null
    private var database : FirebaseDatabase? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVendorDashboardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.setTitle("Vendor Dashboard")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")

        loadVendorDashboardProfile()

        binding.btnprofile.setOnClickListener(){
            startActivity(Intent(this@VendorDashboardActivity,VendorProfileActivity::class.java))
            finish()
        }
    }

    private fun loadVendorDashboardProfile() {
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

                //load user email,username,phoneNumber
                binding.merchantName.text = snapshot.child("Merchant Name").value.toString()
                binding.fullName.text = snapshot.child("Full Name").value.toString()
                binding.email.text = user?.email

            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("aaaaaaaa", error.message)
                TODO("Not yet implemented")
            }
        })
    }
}