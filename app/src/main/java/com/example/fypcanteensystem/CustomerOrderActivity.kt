package com.example.fypcanteensystem

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.adapter.OrderFoodListAdapter
import com.example.fypcanteensystem.adapter.OrderListAdapter
import com.example.fypcanteensystem.dataModels.OrderFoodListData
import com.example.fypcanteensystem.dataModels.OrderListData
import com.example.fypcanteensystem.databinding.ActivityCustomerOrderBinding
import com.example.fypcanteensystem.databinding.OrderFoodListPopupBinding
import com.example.fypcanteensystem.databinding.OrderRatingPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList

class CustomerOrderActivity : AppCompatActivity(), OrderListAdapter.onItemClickListener {

    private lateinit var binding: ActivityCustomerOrderBinding
    private lateinit var bindingOrderFoodList: OrderFoodListPopupBinding
    private lateinit var bindingOrderRating: OrderRatingPopupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseVendReference: DatabaseReference
    private lateinit var databaseCustReference: DatabaseReference
    private lateinit var orderReference: DatabaseReference
    private lateinit var orderFoodReference: DatabaseReference
    private lateinit var ratingCustReference: DatabaseReference
    private lateinit var ratingVendReference: DatabaseReference

    private lateinit var database: FirebaseDatabase
    private lateinit var database2: FirebaseDatabase
    private lateinit var orderListArray: ArrayList<OrderListData>
    private lateinit var orderFoodListArray: ArrayList<OrderFoodListData>
    private var context = this

    private lateinit var orderListener: ValueEventListener
    private lateinit var orderFoodListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar!!.title = "My Order"
        actionbar.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        database2 = FirebaseDatabase.getInstance()
        databaseCustReference = database?.reference!!.child("customerProfile")
        databaseVendReference = database?.reference!!.child("vendorProfile")

        binding.rvOrderList.layoutManager = LinearLayoutManager(this)
        binding.rvOrderList.setHasFixedSize(true)

        orderListArray = arrayListOf<OrderListData>()
        orderFoodListArray = arrayListOf<OrderFoodListData>()

        loadOrderlistData()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun loadOrderlistData() {
        val userId = auth.currentUser?.uid!!
        orderReference = databaseCustReference.child(userId)?.child("orderItem")
        orderListener = orderReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    orderListArray = arrayListOf()
                    for (orderSnapshot in snapshot.children) {
                        val orderId = orderSnapshot.key
                        val vendorId =
                            orderSnapshot.child("orderVendorId").getValue(String::class.java)
                        val vendorName =
                            orderSnapshot.child("orderVendorName").getValue(String::class.java)
                        val orderTotalQty =
                            orderSnapshot.child("orderTotalQty").getValue(String::class.java)
                        val orderNote =
                            orderSnapshot.child("orderNote").getValue(String::class.java)
                        val orderStatus =
                            orderSnapshot.child("orderStatus").getValue(String::class.java)
                        val orderDateTime =
                            orderSnapshot.child("orderDateTime").getValue(String::class.java)
                        val paymentMethod =
                            orderSnapshot.child("orderPaymentMethod").getValue(String::class.java)
                        val orderTotalPrice =
                            orderSnapshot.child("orderTotalPrice").getValue(String::class.java)
                        val orderRating =
                            orderSnapshot.child("orderRating").getValue(String::class.java)

                        if(orderRating == "" || orderRating == "Waiting Rate") {
                            val orderList = OrderListData(
                                orderId,
                                vendorId,
                                vendorName,
                                orderTotalQty,
                                orderNote,
                                orderStatus,
                                orderDateTime,
                                paymentMethod,
                                orderTotalPrice,
                                orderRating
                            )
                            orderListArray.add(orderList)
                        }
                    }
                    binding.rvOrderList.adapter = OrderListAdapter(orderListArray, context, context)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }

    override fun itemClick(position: Int) {

        val dialogBuilder = AlertDialog.Builder(this)
        bindingOrderFoodList = OrderFoodListPopupBinding.inflate(layoutInflater)
        dialogBuilder.setView(bindingOrderFoodList.root)
        val dialog = dialogBuilder.create()
        dialog.show()

        bindingOrderFoodList.rvOrderFoodItemList.layoutManager = LinearLayoutManager(this)
        bindingOrderFoodList.rvOrderFoodItemList.setHasFixedSize(true)

        val selectedOrder = orderListArray[position]
        val orderId = selectedOrder.orderId.toString()

        val userId = auth.currentUser?.uid!!
        orderFoodReference =
            databaseCustReference.child(userId)?.child("orderItem")?.child(orderId)
                ?.child("orderFoodItem")
        //orderFoodReference = database2?.reference!!.child("customerProfile").child(userId)?.child("orderItem")?.child(orderId)?.child("orderFoodItem")

        orderFoodListener =
            orderFoodReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        orderFoodListArray = arrayListOf()
                        for (orderFoodSnapshot in snapshot.children) {
                            val orderFoodItemId = orderFoodSnapshot.key
                            val orderFoodId = orderFoodSnapshot.child("orderFoodId")
                                .getValue(String::class.java)
                            val orderFoodImage =
                                orderFoodSnapshot.child("ImageUri").getValue(String::class.java)
                            val orderFoodName = orderFoodSnapshot.child("orderFoodName")
                                .getValue(String::class.java)
                            val orderFoodPrice = orderFoodSnapshot.child("orderFoodPrice")
                                .getValue(String::class.java)
                            val orderFoodQty = orderFoodSnapshot.child("orderFoodQty")
                                .getValue(String::class.java)

                            val orderFoodList = OrderFoodListData(
                                orderFoodItemId,
                                orderFoodId,
                                orderFoodImage,
                                orderFoodName,
                                orderFoodPrice,
                                orderFoodQty
                            )
                            orderFoodListArray.add(orderFoodList)
                        }
                        bindingOrderFoodList.rvOrderFoodItemList.adapter =
                            OrderFoodListAdapter(orderFoodListArray, context)
                        bindingOrderFoodList.tvOrderIdPopUp.text = "Order Id: $orderId"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }
            })

    }

    override fun itemClickRating(position: Int) {
        if (position == -1) {
            Toast.makeText(this, "Please RATE after order completed", Toast.LENGTH_SHORT).show()
        } else {
            val dialogBuilder = AlertDialog.Builder(this)
            bindingOrderRating = OrderRatingPopupBinding.inflate(layoutInflater)
            dialogBuilder.setView(bindingOrderRating.root)
            val dialog = dialogBuilder.create()
            dialog.show()

            val userId = auth.currentUser?.uid!!
            val orderId = orderListArray[position].orderId
            val vendorId = orderListArray[position].vendorId
            var totalRate: String? = null
            var totalRateNo: String? = null

            bindingOrderRating.btnRatingSubmit.setOnClickListener() {
                val rating = bindingOrderRating.ratingBar.rating.toString()

                ratingCustReference =
                    databaseCustReference.child(userId)?.child("orderItem")?.child(orderId!!)
                ratingCustReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            ratingCustReference?.child("orderRating")?.setValue(rating)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })

                ratingVendReference = databaseVendReference.child(vendorId!!)
                ratingVendReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var totalRate = snapshot.child("Total Rate").getValue(String::class.java)?.toDoubleOrNull()
                            var totalRateNo =
                                snapshot.child("Total Rate Number").getValue(String::class.java)
                                    ?.toIntOrNull()
                            if (totalRate != null && totalRateNo != null) {
                                totalRate += rating.toDouble()
                                totalRateNo += 1

                            } else {
                                totalRate = rating.toDouble()
                                totalRateNo = 1
                            }
                            ratingVendReference?.child("Total Rate")?.setValue(totalRate.toString())
                            ratingVendReference?.child("Total Rate Number")
                                ?.setValue(totalRateNo.toString())

                            var averageRate = totalRate!!.toDouble() / totalRateNo!!.toDouble()
                            ratingVendReference?.child("Rate Average")
                                ?.setValue(averageRate.format(1).toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })
                Toast.makeText(this, "Rated Successfully", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }

            bindingOrderRating.btnRatingSkip.setOnClickListener() {
                ratingCustReference = databaseCustReference.child(userId)?.child("orderItem")?.child(orderId!!)
                ratingCustReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            ratingCustReference?.child("orderRating")?.setValue("Skipped")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })
                Toast.makeText(this, "Rate Skipped", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //orderListener?.let { orderReference!!.removeEventListener(it)}
        //orderFoodListener?.let { orderFoodReference!!.removeEventListener(it)}
    }
}