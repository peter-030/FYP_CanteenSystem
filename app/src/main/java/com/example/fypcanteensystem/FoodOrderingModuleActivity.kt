package com.example.fypcanteensystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.databinding.ActivityFoodOrderingModuleBinding
import com.example.fypcanteensystem.adapter.VendorsListAdapter
import com.example.fypcanteensystem.dataModels.VendorsListData
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.*

import java.util.ArrayList

class FoodOrderingModuleActivity : AppCompatActivity(), VendorsListAdapter.onItemClickListener {

    private lateinit var binding: ActivityFoodOrderingModuleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var vendorListArray: ArrayList<VendorsListData>
    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodOrderingModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.title = "Peter & Jx Food"

        binding.rvVendorsList.layoutManager = LinearLayoutManager(this)
        binding.rvVendorsList.setHasFixedSize(true)


        vendorListArray = arrayListOf<VendorsListData>()
        readVendorListData()

        binding.cvCart.setOnClickListener{
            startActivity(Intent(this@FoodOrderingModuleActivity, CustomerCartActivity::class.java))
        }

        binding.cvOrder.setOnClickListener {
            startActivity(Intent(this@FoodOrderingModuleActivity, CustomerOrderActivity::class.java))
        }

        binding.cvProfile.setOnClickListener {
            startActivity(Intent(this@FoodOrderingModuleActivity, CustomerProfileActivity::class.java))
        }

        binding.cvWishlist.setOnClickListener {
            startActivity(Intent(this@FoodOrderingModuleActivity, CustomerWishlistActivity::class.java))
        }

        binding.cvSetting.setOnClickListener {
            startActivity(Intent(this@FoodOrderingModuleActivity, CustomerSettingActivity::class.java))
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
    }

    private fun readVendorListData() {
        database = FirebaseDatabase.getInstance().getReference("vendorProfile")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    vendorListArray = arrayListOf()
                    for (vendorListSnapshot in snapshot.children){
                        val vendorId = vendorListSnapshot.key
                        val merchantName = vendorListSnapshot.child("Merchant Name").getValue(String::class.java)
                        val phoneNo = vendorListSnapshot.child("Phone Number").getValue(String::class.java)
                        val vendorImg = vendorListSnapshot.child("ImageUri").getValue(String::class.java)


                        val vendors = VendorsListData(vendorId, merchantName, phoneNo, vendorImg)
                        vendorListArray.add(vendors!!)
                    }
                    binding.rvVendorsList.adapter = VendorsListAdapter(vendorListArray, context, context)
                }

                //binding.rvVendorsList.adapter = VendorsListAdapter(vendorListArray)

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun itemClick(position: Int) {

        val selectedVendor = vendorListArray[position]
        //Toast.makeText(applicationContext, selectedVendor.vendorId, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, CustomerMenuDisplayActivity::class.java)
        intent.putExtra("vendorId",selectedVendor.vendorId)
        startActivity(intent)

        //startActivity(Intent(this, CustomerMenuDisplayActivity::class.java))
    }

}
