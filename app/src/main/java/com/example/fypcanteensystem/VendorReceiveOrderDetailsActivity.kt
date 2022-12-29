package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.databinding.ActivityVendorReceiveOrderDetailsBinding
import com.example.fypcanteensystem.model.OrderReceiveData
import com.example.fypcanteensystem.model.OrderReceiveDetailsData
import com.example.fypcanteensystem.view.OrderReceiveAdapter
import com.example.fypcanteensystem.view.OrderReceiveDetailsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.StringBuilder

class VendorReceiveOrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityVendorReceiveOrderDetailsBinding
    private lateinit var recv : RecyclerView
    private lateinit var orderDetailsList: ArrayList<OrderReceiveDetailsData>
    private lateinit var orderReceiveDetailsAdapter: OrderReceiveDetailsAdapter
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference?=null
    private var database : FirebaseDatabase?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorReceiveOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("customerProfile")

        /**set List*/
        orderDetailsList = ArrayList()
        /**set find Id*/
        recv = binding.ordDetailsRecycler
        /**set Adapter*/
        orderReceiveDetailsAdapter = OrderReceiveDetailsAdapter(this,orderDetailsList)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = orderReceiveDetailsAdapter

        loadOrderDetails()
    }

    private fun loadOrderDetails() {

//        val user = auth.uid
//        val userReference = databaseReference?.child("PoM9wCQJtNPFu1xAAoQ1aVr1ZGP2")?.child("cartItem")
//
//        userReference?.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                //load order details
//                if(snapshot.exists()){
//                    orderDetailsList.clear() //clear previous data before load a new one
//                    for(userSnapshot in snapshot.children)
//                    {
//
//                        val name = userSnapshot.child("cartFoodName").getValue(String::class.java)
//                        val price = userSnapshot.child("cartFoodPrice").getValue(String::class.java)
//                        val qty = userSnapshot.child("cartFoodQty").getValue(String::class.java)
//
//                        val OrderDetailsData = OrderReceiveDetailsData(name,price,qty)
//                        orderDetailsList.add(OrderDetailsData)
//                    }
//
//                    recv.adapter = OrderReceiveDetailsAdapter(this@VendorReceiveOrderDetailsActivity,orderDetailsList)
//
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                //TODO("Not yet implemented")
//            }
//        })

    }
}