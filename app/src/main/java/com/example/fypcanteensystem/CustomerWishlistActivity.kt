package com.example.fypcanteensystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.adapter.VendorsListAdapter
import com.example.fypcanteensystem.dataModels.VendorListData
import com.example.fypcanteensystem.dataModels.VendorIdData
import com.example.fypcanteensystem.databinding.ActivityCustomerWishlistBinding
import com.example.fypcanteensystem.functionClass.SwipeToRemove
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList

class CustomerWishlistActivity : AppCompatActivity(), VendorsListAdapter.onItemClickListener {
    private lateinit var binding: ActivityCustomerWishlistBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseVendReference: DatabaseReference
    private lateinit var databaseCustReference: DatabaseReference
    private lateinit var wishRefrence: DatabaseReference
    private lateinit var wishlistDeleteReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var vendorListArray: ArrayList<VendorListData>
    private lateinit var vendorWishlistArray: ArrayList<VendorListData>
    private lateinit var wishlistArray: ArrayList<VendorIdData>
    private var context = this
    private lateinit var vendListener: ValueEventListener
    private lateinit var wishListener: ValueEventListener
    private lateinit var wishVendListener: ValueEventListener
    private lateinit var wishlistDeleteListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar!!.title = "Wishlist"
        actionbar.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseCustReference = database?.reference!!.child("customerProfile")
        databaseVendReference = database?.reference!!.child("vendorProfile")

        binding.rvWishlistVendor.layoutManager = LinearLayoutManager(this)
        binding.rvWishlistVendor.setHasFixedSize(true)

        vendorListArray = arrayListOf<VendorListData>()
        wishlistArray = arrayListOf<VendorIdData>()

        loadWishlistData()

        //remove wishlist
        val swipeToRemoveWishlist = object : SwipeToRemove(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                removeWishlist(position)
            }

        }
        val itemTouchHelper = ItemTouchHelper(swipeToRemoveWishlist)
        itemTouchHelper.attachToRecyclerView(binding.rvWishlistVendor)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadWishlistData() {
        val userId = auth.currentUser?.uid!!

        vendListener = databaseVendReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    vendorListArray = arrayListOf()
                    for (vendorListSnapshot in snapshot.children){
                        val vendorId = vendorListSnapshot.key
                        val merchantName = vendorListSnapshot.child("Merchant Name").getValue(String::class.java)
                        val phoneNo = vendorListSnapshot.child("Phone Number").getValue(String::class.java)
                        val vendorImg = vendorListSnapshot.child("ImageUri").getValue(String::class.java)
                        val rentalCode = vendorListSnapshot.child("Rental Code").getValue(String::class.java)
                        val vendorAvgRate = vendorListSnapshot.child("Rate Average").getValue(String::class.java)

                        val vendors = VendorListData(vendorId, merchantName, phoneNo, vendorImg, rentalCode, vendorAvgRate)
                        vendorListArray.add(vendors!!)

                        wishRefrence = databaseCustReference?.child(userId)?.child("wishlist")
                        wishListener = wishRefrence?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    wishlistArray = arrayListOf()
                                    for (wishlistVendorSnapshot in snapshot.children) {
                                        val wishListId = wishlistVendorSnapshot.key
                                        val wishVendorId = wishlistVendorSnapshot.child("wishlistVendorId").getValue(String::class.java)

                                        val wishlist = VendorIdData(wishVendorId)
                                        wishlistArray.add(wishlist)
                                    }
                                    vendorWishlistArray = arrayListOf()
                                    for(wishVendor in wishlistArray){
                                        for(vendor in vendorListArray){
                                            if(wishVendor.vendorId == vendor.vendorId){
                                                vendorWishlistArray.add(VendorListData(vendor.vendorId,
                                                    vendor.merchantName, vendor.phoneNumber, vendor.vendorImg,
                                                    vendor.rentalCode, vendor.vendorAvgRate))
                                            }
                                        }
                                    }
                                    binding.rvWishlistVendor.adapter = VendorsListAdapter(vendorWishlistArray, context, context)
                                    binding.rvWishlistVendor.adapter?.notifyDataSetChanged()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                //TODO("Not yet implemented")
                            }
                        })

                        /*
                        for(checkId in wishlistArray){
                            if(checkId.vendorId == vendorId){
                                val vendors = VendorsListData(vendorId, merchantName, phoneNo, vendorImg, rentalCode, vendorAvgRate)
                                vendorListArray.add(vendors!!)
                            }
                        }

                         */

                    }


                }
            }
            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }

    override fun itemClick(position: Int) {
        val selectedVendor = wishlistArray[position]
        val intent = Intent(this, CustomerMenuDisplayActivity::class.java)
        intent.putExtra("vendorId",selectedVendor.vendorId)
        startActivity(intent)
        finish()
    }

    private fun removeWishlist(position: Int){
        val userId = auth.currentUser?.uid!!
        wishlistDeleteReference = databaseCustReference?.child(userId)?.child("wishlist")

        val selectedWishlist = wishlistArray[position]
        val selectedVendId = selectedWishlist.vendorId

        wishlistDeleteReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (wishListSnapshot in snapshot.children){
                        val wishlistId = wishListSnapshot.key
                        //val wishlistVendorId = wishListSnapshot.child(wishlistId!!).child("wishlistVendorId").getValue(String::class.java)
                        val wishlistVendorId = wishListSnapshot.child("wishlistVendorId").getValue(String::class.java)

                        if(selectedVendId == wishlistVendorId){
                            wishlistArray.clear()
                            wishlistDeleteReference?.child(wishlistId!!).removeValue()
                            binding.rvWishlistVendor.adapter?.notifyDataSetChanged()
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        Toast.makeText(this,"Wishlist Removed.", Toast.LENGTH_SHORT).show()

    }
    override fun onDestroy() {
        super.onDestroy()
        vendListener?.let { databaseVendReference!!.removeEventListener(it)}
        wishListener?.let { wishRefrence!!.removeEventListener(it)}
        //wishVendListener?.let { databaseVendReference!!.removeEventListener(it)}
        //wishlistDeleteListener?.let { wishlistDeleteReference!!.removeEventListener(it)}

    }
}