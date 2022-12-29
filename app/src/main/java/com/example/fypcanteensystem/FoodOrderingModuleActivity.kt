package com.example.fypcanteensystem

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.databinding.ActivityFoodOrderingModuleBinding
import com.example.fypcanteensystem.adapter.VendorsListAdapter
import com.example.fypcanteensystem.dataModels.VendorListData
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.*

import java.util.ArrayList

class FoodOrderingModuleActivity : AppCompatActivity(), VendorsListAdapter.onItemClickListener {

    private lateinit var binding: ActivityFoodOrderingModuleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseVendReference: DatabaseReference
    private lateinit var databaseCustReference: DatabaseReference
    private lateinit var orderSoundReference: DatabaseReference
    private lateinit var orderCancelReference: DatabaseReference

    private lateinit var vendorListArray: ArrayList<VendorListData>
    private var context = this

    private lateinit var databaseListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodOrderingModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar!!.title = "Peter & Jx Food"
        actionbar.setDisplayHomeAsUpEnabled(true)

        binding.rvVendorsList.layoutManager = LinearLayoutManager(this)
        binding.rvVendorsList.setHasFixedSize(true)


        vendorListArray = arrayListOf<VendorListData>()
        readVendorListData()

        binding.cvCart.setOnClickListener {
            startActivity(Intent(this@FoodOrderingModuleActivity, CustomerCartActivity::class.java))
        }

        binding.cvOrder.setOnClickListener {
            startActivity(
                Intent(
                    this@FoodOrderingModuleActivity,
                    CustomerOrderActivity::class.java
                )
            )
        }

        binding.cvProfile.setOnClickListener {
            startActivity(
                Intent(
                    this@FoodOrderingModuleActivity,
                    CustomerProfileActivity::class.java
                )
            )
        }

        binding.cvWishlist.setOnClickListener {
            startActivity(
                Intent(
                    this@FoodOrderingModuleActivity,
                    CustomerWishlistActivity::class.java
                )
            )
        }

        binding.cvSetting.setOnClickListener {
            startActivity(
                Intent(
                    this@FoodOrderingModuleActivity,
                    CustomerReportActivity::class.java
                )
            )
        }


        //advertisement
        val imageArray = ArrayList<Int>()
        imageArray.add(R.drawable.ad_demo1)
        imageArray.add(R.drawable.ad_demo2)

        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            var i = 0
            override fun run() {
                binding.imgAd.setImageResource(imageArray[i])
                i++
                if (i > imageArray.size - 1) {
                    i = 0
                }
                handler.postDelayed(this, 2000)
            }
        }
        handler.postDelayed(runnable, 2000)

        //sound on if order done

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseCustReference = database?.reference!!.child("customerProfile")
        val userId = auth.currentUser?.uid!!

        orderSoundReference = databaseCustReference.child(userId)?.child("orderItem")
        orderSoundReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (orderSnapshot in snapshot.children) {
                        val orderId = orderSnapshot.key
                        val orderStatus = orderSnapshot.child("orderStatus")
                            .getValue(String::class.java)
                        val orderRating = orderSnapshot.child("orderRating")
                            .getValue(String::class.java)
                        if (orderStatus == "Completed" && orderRating == "") {
                            playReceiveOrderTune("Completed", orderId.toString())
                        } else if (orderStatus == "Cancelled" && orderRating == "") {
                            playReceiveOrderTune("Cancelled", orderId.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //custom receive new order sound notification
    private fun playReceiveOrderTune(soundType: String, orderId: String) {

        try {
            val track: MediaPlayer
            if (soundType == "Completed") {
                track = MediaPlayer.create(applicationContext, R.raw.notice)
                isNotice(orderId, "Waiting Rate")
            } else{
                track = MediaPlayer.create(applicationContext, R.raw.error)
                isNotice(orderId, "Cancelled")
            }
            track?.start()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Failed to play this File", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun isNotice(orderId: String, notice: String) {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseCustReference = database?.reference!!.child("customerProfile")
        val userId = auth.currentUser?.uid!!

        orderCancelReference =
            databaseCustReference.child(userId)?.child("orderItem").child(orderId)
        orderCancelReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    orderCancelReference?.child("orderRating").setValue(notice)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }

    private fun readVendorListData() {
        database = FirebaseDatabase.getInstance()
        databaseVendReference = database?.reference!!.child("vendorProfile")
        databaseVendReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    vendorListArray = arrayListOf()
                    for (vendorListSnapshot in snapshot.children) {
                        val vendorId = vendorListSnapshot.key
                        val merchantName =
                            vendorListSnapshot.child("Merchant Name").getValue(String::class.java)
                        val phoneNo =
                            vendorListSnapshot.child("Phone Number").getValue(String::class.java)
                        val vendorImg =
                            vendorListSnapshot.child("ImageUri").getValue(String::class.java)
                        val rentalCode =
                            vendorListSnapshot.child("Rental Code").getValue(String::class.java)
                        val vendorAvgRate =
                            vendorListSnapshot.child("Rate Average").getValue(String::class.java)

                        val vendors = VendorListData(
                            vendorId,
                            merchantName,
                            phoneNo,
                            vendorImg,
                            rentalCode,
                            vendorAvgRate
                        )
                        vendorListArray.add(vendors!!)
                    }
                    binding.rvVendorsList.adapter =
                        VendorsListAdapter(vendorListArray, context, context)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

    }

    override fun itemClick(position: Int) {

        val selectedVendor = vendorListArray[position]
        val intent = Intent(this, CustomerMenuDisplayActivity::class.java)
        intent.putExtra("vendorId", selectedVendor.vendorId)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        //databaseListener?.let { database!!.removeEventListener(it)}
    }

}
