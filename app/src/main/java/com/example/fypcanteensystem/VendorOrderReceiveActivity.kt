package com.example.fypcanteensystem

import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.databinding.ActivityVendorOrderReceiveBinding
import com.example.fypcanteensystem.model.*
import com.example.fypcanteensystem.view.OrderHistoryAdapter
import com.example.fypcanteensystem.view.OrderReceiveAdapter
import com.example.fypcanteensystem.view.OrderReceiveDetailsAdapter
import com.example.fypcanteensystem.view.PromoCodeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.lang.Exception
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VendorOrderReceiveActivity : AppCompatActivity(),OrderReceiveAdapter.onItemClickListener {
    private lateinit var binding : ActivityVendorOrderReceiveBinding
    private lateinit var recv : RecyclerView
    private lateinit var orderList: ArrayList<OrderReceiveData>
    private lateinit var orderReceiveAdapter: OrderReceiveAdapter
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference?=null
    private var database : FirebaseDatabase?= null

    private lateinit var orderDetailsList: ArrayList<OrderReceiveDetailsData>
    private lateinit var orderHistoryList: ArrayList<OrderHistoryData>
    private lateinit var vendorsIDList: ArrayList<String>

    private lateinit var getfirstkey: String
    private lateinit var getsecondkey: String

    private lateinit var getStudId: String
    private lateinit var getStudBalance: String
    private lateinit var getRefundAmount: String
    private lateinit var getCurrentBalance: String
    private var bolNotify:Boolean = true
    private var curdatabaseReference : DatabaseReference?=null

    private var reportReference : DatabaseReference?=null

    private lateinit var calendar: Calendar
    private lateinit var simpleDateFormat : SimpleDateFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorOrderReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Order Receiving List")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("customerProfile")

        /**set List*/
        orderList = ArrayList()
        /**set find Id*/
        recv = binding.orderRecycler
        /**set Adapter*/
        orderReceiveAdapter = OrderReceiveAdapter(this,orderList,this)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = orderReceiveAdapter

        reportReference = database?.reference!!.child("reportProfile")

        //to check vendor e-wallet balance purpose
        curdatabaseReference = database?.reference!!.child("vendorProfile")
        val cuReference = database?.reference!!.child("vendorProfile")
        val user = auth.currentUser
        val bReference = cuReference?.child(user?.uid!!)?.child("E-wallet Balance")
        bReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                getCurrentBalance = snapshot.value.toString()

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        orderHistoryList = arrayListOf()

        loadAllItem()

        binding.btnOrderHistory.setOnClickListener(){
            showOrderHistory()
        }

        val actionbar = supportActionBar
        //actionbar!!.title = "My Cart"
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //custom receive new order sound notification
    private fun playReceiveOrderTune(){

        try{
            val track : MediaPlayer? = MediaPlayer.create(applicationContext,R.raw.receive_order_sound)
            track?.start()
        }
        catch (e: Exception){
            Toast.makeText(applicationContext,"Failed to play this File", Toast.LENGTH_SHORT).show()
        }

    }



    private fun showOrderHistory(){
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.dialog_order_history,null)
        val addDialog = AlertDialog.Builder(this)
        val user = auth.uid


        val historyCount = v.findViewById<TextView>(R.id.txt_orderHistory_count)

        addDialog.setView(v)

            .setNegativeButton("Close"){
                    dialog,_->
                dialog.dismiss()
            }
            .show()

        var recyclerView = v.findViewById<RecyclerView>(R.id.ordHistoryRecycler)
        var adapter = OrderHistoryAdapter(this,orderHistoryList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load order details
                if(snapshot.exists()){
                    orderHistoryList.clear() //clear previous data before load a new one
                    for(userSnapshot in snapshot.children)
                    {

                        val userId = userSnapshot.key!!
                        val name = userSnapshot.child("Full Name").getValue(String::class.java)


                        //for take the order that is belong to the specific vendor
                        for(snap in userSnapshot.child("orderItem").children){
                            val orderID = snap.key
                            val noOfItems = snap.child("orderFoodItem").children.count().toString() //to get Num of items in an order
                            val orderDateTime = snap.child("orderDateTime").getValue(String::class.java)
                            val orderStatus = snap.child("orderStatus").getValue(String::class.java)
                            val orderPaymentMethod = snap.child("orderPaymentMethod").getValue(String::class.java)
                            val orderTotalPrice = snap.child("orderTotalPrice").getValue(String::class.java)
                            if((snap.child("orderVendorId").value.toString() == user!!.toString() && snap.child("orderStatus").getValue(String::class.java) == "Completed")
                                || (snap.child("orderVendorId").value.toString() == user!!.toString() && snap.child("orderStatus").getValue(String::class.java) == "Cancelled")) {
                                val HistoryData = OrderHistoryData(orderID,orderDateTime,name,orderStatus,noOfItems,null,orderPaymentMethod, null,orderTotalPrice)
                                orderHistoryList.add(HistoryData)
                                //break
                            }
                        }


                    }
                    historyCount.setText(StringBuilder("Order History (").append(orderHistoryList.size).append(")"))
                    recyclerView.adapter = OrderHistoryAdapter(this@VendorOrderReceiveActivity,orderHistoryList)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

    }

    private fun loadAllItem() {

        val user = auth.uid
        val userReference = databaseReference?.child(user!!)

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load order details
                if(snapshot.exists()){
                    orderList.clear() //clear previous data before load a new one
                    for(userSnapshot in snapshot.children)
                    {

                        val userId = userSnapshot.key!!
                        val name = userSnapshot.child("Full Name").getValue(String::class.java)
                        //val noOfItems = userSnapshot.child("orderItem").children.count().toString()
                        val img = userSnapshot.child("ImageUri").getValue(String::class.java)

                        //for take the order that is belong to the specific vendor
                        for(snap in userSnapshot.child("orderItem").children){
                            val orderId= snap.key!!
                            val noOfItems= snap.child("orderFoodItem").children.count().toString() //to get Num of items in an order
                            val orderDateTime = snap.child("orderDateTime").getValue(String::class.java)
                            val orderStatus = snap.child("orderStatus").getValue(String::class.java)
                            val orderPaymentMethod = snap.child("orderPaymentMethod").getValue(String::class.java)
                                if((snap.child("orderVendorId").value.toString() == user!!.toString() && snap.child("orderStatus").getValue(String::class.java) == "Pending")
                                    || (snap.child("orderVendorId").value.toString() == user!!.toString() && snap.child("orderStatus").getValue(String::class.java) == "Cooking")){
                                    val orderData1st = OrderReceiveData(orderId,orderDateTime,name,orderStatus,noOfItems,img,orderPaymentMethod, userId)
                                    orderList.add(orderData1st)
                                    //break
                                    if(bolNotify && snap.child("orderStatus").getValue(String::class.java) == "Pending"){
                                        playReceiveOrderTune()
                                    }

                                }
                        }



                    }
                    if(bolNotify==false){
                        bolNotify=true
                    }
                    binding.txtOrderCount.setText(StringBuilder("Orders (").append(orderList.size).append(")"))
                    recv.adapter = OrderReceiveAdapter(this@VendorOrderReceiveActivity,orderList,this@VendorOrderReceiveActivity)
                    recv.adapter?.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })


    }

    override fun displayInfo(position: Int) {
        //TODO("Not yet implemented")
    }

    override fun updateOrder(position: Int) {
        //updateOrderStatus(position)
        //return
        val currentUser = auth.currentUser
        val vendorReference= database?.reference!!.child("vendorProfile")

        val v = LayoutInflater.from(this).inflate(R.layout.update_status,null)
        val showStatus = v.findViewById<TextView>(R.id.txt_showStatus)
        val showUpdateStatus = v.findViewById<RadioButton>(R.id.order_updateStatus)
        val statusGroup = v.findViewById<RadioGroup>(R.id.status_radioGroup)
        val list = arrayListOf<DataSnapshot>()
        val orderCancelStatus = v.findViewById<RadioButton>(R.id.order_cancelStatus)
        val orderUpdateStatus = v.findViewById<RadioButton>(R.id.order_updateStatus)

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load show status
                if(snapshot.exists()){

                    //under "if" is to avoid when order list = 0 then it go in here problem
                    if(orderList.size!=0){
                        val curOrder = orderList[position]
                        for(userSnapshot in snapshot.children)
                        {
                            list.add(userSnapshot)

                            for(snap in userSnapshot.child("orderItem").children){

                                if(snap.key == curOrder.orderId.toString()){
                                    showStatus.setText(snap.child("orderStatus").getValue(String::class.java))

                                    if(showStatus.text == "Cooking"){
                                        showUpdateStatus.text = "Completed"
                                    }
                                }

                            }
                        }
                    }


//                    for(userSnapshot in snapshot.children)
//                    {
//                        list.add(userSnapshot)
//                    }
//                    val keyClicked = list[position].key!!
//                    for(snap in snapshot?.child(keyClicked)?.child("orderItem").children){
//
//                        showStatus.setText(snap.child("orderStatus").getValue(String::class.java))
//
//                        if(showStatus.text == "Cooking"){
//                            showUpdateStatus.text = "Completed"
//                        }
//                    }


                    if(showStatus.text == "Completed"){

                        statusGroup.removeAllViews()

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        //below is to show dialog
        AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton("Ok"){
                    dialog,_->
                if(orderUpdateStatus.isChecked){
                    updateOrderStatus(position)
                }
                else if(orderCancelStatus.isChecked){
                    cancelOrderStatus(position)
                }




            }
            .setNegativeButton("Cancel"){
                    dialog,_->

                dialog.dismiss()

            }
            .show()
    }

    private fun cancelOrderStatus(position: Int){
        bolNotify=false
        val currentUserID = auth.currentUser!!.uid.toString()
        val vendorReference= database?.reference!!.child("vendorProfile")
        val user = auth.uid
        val currentUser = auth.currentUser
        val key = database!!.getReference("vendorProfile").push().key //to get random id for each food item
        val findBalanceDb = curdatabaseReference?.child((currentUser?.uid!!))?.child("E-wallet Balance")

        //setup datetime
        var date : String
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy, HH:mm")
        date = simpleDateFormat.format(calendar.time)


            val curOrder = orderList[position]

        //still got problem when update cooking to Cancelled (when payMethod is E-wallet)
                val newStatus = if (curOrder.orderStatus.toString() == "Pending" ||curOrder.orderStatus.toString() == "Cooking") "Cancelled" else "Cancelled"
                orderList[position].orderStatus = newStatus
                val childUpdates = hashMapOf<String, Any>(
                    "${curOrder.userID}/orderItem/${curOrder.orderId}/orderStatus" to newStatus
                )
                databaseReference!!.updateChildren(childUpdates).addOnSuccessListener {
                    Log.d("wdawd", "Updated")
                }

            if(curOrder.orderStatus.toString() == "Cancelled"){
                databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        //load order details
                        if(snapshot.exists()){
                            orderHistoryList.clear() //clear previous data before load a new one
                            for(userSnapshot in snapshot.children)
                            {

                                val userId = userSnapshot.key!!
                                val name = userSnapshot.child("Full Name").getValue(String::class.java)


                                //for take the order that is belong to the specific vendor
                                for(snap in userSnapshot.child("orderItem").children){
                                    val orderID = snap.key
                                    val noOfItems = snap.child("orderFoodItem").children.count().toString() //to get Num of items in an order
                                    val orderDateTime = snap.child("orderDateTime").getValue(String::class.java)
                                    val orderStatus = snap.child("orderStatus").getValue(String::class.java)
                                    val orderPaymentMethod = snap.child("orderPaymentMethod").getValue(String::class.java)
                                    val orderTotalPrice = snap.child("orderTotalPrice").getValue(String::class.java)
                                    if(snap.child("orderVendorId").value.toString() == user!!.toString()){
                                        val HistoryData = OrderHistoryData(orderID,orderDateTime,name,orderStatus,noOfItems,null,orderPaymentMethod, null,orderTotalPrice)
                                        orderHistoryList.add(HistoryData)
                                        //break
                                    }
                                    if(snap.key == curOrder.orderId.toString() && snap.child("orderVendorId").value.toString() == user!!.toString() && snap.child("orderPaymentMethod").getValue(String::class.java) == "PayByE-Wallet"){

                                        val getKey = userSnapshot.key
                                        val getVendorID = vendorReference?.child((currentUser?.uid!!))?.key
                                        getStudId = userSnapshot.key!!
                                        getStudBalance = userSnapshot.child("E-wallet Balance").getValue(String::class.java)!!
                                        getRefundAmount = snap.child("orderTotalPrice").getValue(String::class.java)!!

                                        databaseReference?.child(getKey!!)?.child("E-wallet")?.child(key!!)
                                            ?.setValue(EwalletData(null,getRefundAmount,null,date,getVendorID))

                                        //minus vendor balance here after recharge for students and add up student balance
                                        val getAmount = getRefundAmount.toDouble()
                                        val finalBalance = getCurrentBalance.toDouble().minus(getAmount).toString()
                                        findBalanceDb?.setValue(finalBalance)

                                        val studFinalBalance = getStudBalance.toDouble() + getRefundAmount.toDouble()

                                        databaseReference?.child(getStudId)?.child("E-wallet Balance")
                                            ?.setValue(studFinalBalance.toString())
                                    }
                                }


                            }

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })
                Toast.makeText(this@VendorOrderReceiveActivity,"Order has cancelled and successful refund",Toast.LENGTH_LONG).show()
            }


    }

    private fun updateOrderStatus(position: Int) {
        bolNotify=false
        val currentUserID = auth.currentUser!!.uid.toString()
        val vendorReference= database?.reference!!.child("vendorProfile")
        val user = auth.uid

        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.update_status,null)
        val orderCancelStatus = v.findViewById<RadioButton>(R.id.order_cancelStatus)
        val orderUpdateStatus = v.findViewById<RadioButton>(R.id.order_updateStatus)
        val cancelStatus = orderCancelStatus.text.toString()

        val key = database!!.getReference("reportProfile").push().key

        readVendors() {
            val curOrder = orderList[position]

                val newStatus = if (curOrder.orderStatus.toString() == "Pending") "Cooking" else "Completed"
                orderList[position].orderStatus = newStatus
                val childUpdates = hashMapOf<String, Any>(
                    "${curOrder.userID}/orderItem/${curOrder.orderId}/orderStatus" to newStatus
                )
                databaseReference!!.updateChildren(childUpdates).addOnSuccessListener {
                    Log.d("wdawd", "Updated")
                }



            if(curOrder.orderStatus.toString() == "Completed"){
                databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        //load order details
                        if(snapshot.exists()){
                            orderHistoryList.clear() //clear previous data before load a new one
                            for(userSnapshot in snapshot.children)
                            {

                                val userId = userSnapshot.key!!
                                val name = userSnapshot.child("Full Name").getValue(String::class.java)


                                //for take the order that is belong to the specific vendor
                                for(snap in userSnapshot.child("orderItem").children){
                                    val orderID = snap.key
                                    val noOfItems = snap.child("orderFoodItem").children.count().toString() //to get Num of items in an order
                                    val orderDateTime = snap.child("orderDateTime").getValue(String::class.java)
                                    val orderStatus = snap.child("orderStatus").getValue(String::class.java)
                                    val orderPaymentMethod = snap.child("orderPaymentMethod").getValue(String::class.java)
                                    val orderTotalPrice = snap.child("orderTotalPrice").getValue(String::class.java)
                                    if(snap.child("orderVendorId").value.toString() == user!!.toString()){
                                        val HistoryData = OrderHistoryData(orderID,orderDateTime,name,orderStatus,noOfItems,null,orderPaymentMethod, null,orderTotalPrice)
                                        orderHistoryList.add(HistoryData)
                                        bolNotify=false
                                        //break
                                    }
//                                    for(reportData in snap.child("orderFoodItem").children){
//                                        val foodId = reportData.child("orderFoodId").getValue(String::class.java)
//                                        reportReference?.child(key!!)?.child("Food Id")?.setValue(foodId)
//                                    }

                                }



                            }

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })
            }
        }
    }


    private fun readVendors(callback: ()->Unit) {
        val vendorReference= database?.reference!!.child("vendorProfile")

        vendorsIDList = arrayListOf()
        vendorReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (vendor in snapshot.children) {
                        vendorsIDList.add(vendor.key!!)
                    }
                }
                callback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun callCus(position: Int) {

        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.CALL_PHONE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {


                    val curOrder = orderList[position]
                    databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(userSnapshot in snapshot.children)
                            {
                                for(snap in userSnapshot.child("orderItem").children){

                                    if(userSnapshot.key == curOrder.userID.toString()){
                                        val phoneNo = userSnapshot.child("Phone Number").getValue(String::class.java)

                                        val intent = Intent()
                                        intent.setAction(Intent.ACTION_DIAL)
                                        intent.setData(
                                            Uri.parse(StringBuilder("tel: ").append(phoneNo).toString())
                                        )
                                        startActivity(intent)
                                    }

                                }

                            }




                        }

                        override fun onCancelled(error: DatabaseError) {
                            //TODO("Not yet implemented")
                        }
                    })


                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@VendorOrderReceiveActivity,"You must accept this permission"
                            + response!!.permissionName,Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    //TODO("Not yet implemented")
                }

            }).check()
    }

    override fun displayOrderDetails(position: Int) {
            val inflter = LayoutInflater.from(this)
            val v = inflter.inflate(R.layout.dialog_order_receive,null)
            val addDialog = AlertDialog.Builder(this)

            addDialog.setView(v)
                .setTitle("Order Details")
                .setIcon(R.drawable.order)
                .setNegativeButton("Close"){
                        dialog,_->
                    dialog.dismiss()
                }
                .show()
            orderDetailsList = arrayListOf()
            var recyclerView = v.findViewById<RecyclerView>(R.id.ordDetailsRecycler)
            var adapter = OrderReceiveDetailsAdapter(this,orderDetailsList)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

            val itemRemindNote = v.findViewById<TextView>(R.id.txt_remindNote)
            val itemSubPrice = v.findViewById<TextView>(R.id.txt_subtotal)
            val itemTotalPrice = v.findViewById<TextView>(R.id.txt_totalprice)
            val itemDiscountPrice = v.findViewById<TextView>(R.id.txt_discount)

            val list = arrayListOf<DataSnapshot>()
            databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load order details
                if(snapshot.exists() && orderList.size !=0){
                    orderDetailsList.clear() //clear previous data before load a new one
                    val curOrder = orderList[position]
                    for(userSnapshot in snapshot.children)
                    {
                        list.add(userSnapshot)

                        for(snap in userSnapshot.child("orderItem").children){

                            for(snap1 in snap.child("orderFoodItem").children){
                                if(snap.key == curOrder.orderId.toString()){
                                    val foodName = snap1.child("orderFoodName").getValue(String::class.java)
                                    val foodPrice = snap1.child("orderFoodPrice").getValue(String::class.java)
                                    val qty = snap1.child("orderFoodQty").getValue(String::class.java)
                                    val foodSubPrice = snap.child("orderSubPrice").getValue(String::class.java)
                                    val foodTotalPrice = snap.child("orderTotalPrice").getValue(String::class.java)
                                    val foodDiscountPrice = snap.child("orderDiscountPrice").getValue(String::class.java)
                                    val foodRemindNote = snap.child("orderNote").getValue(String::class.java)

                                    itemRemindNote.text = "Remind Note: " + foodRemindNote
                                    itemSubPrice.text = "Subtotal: RM" + foodSubPrice
                                    itemTotalPrice.text = "Total Price: RM" + foodTotalPrice
                                    itemDiscountPrice.text = "Discount: RM" + foodDiscountPrice
                                    itemRemindNote.paint?.isUnderlineText = true
                                    val OrderDetailsData = OrderReceiveDetailsData(foodName,foodPrice,qty,foodSubPrice,foodTotalPrice,foodDiscountPrice)
                                    orderDetailsList.add(OrderDetailsData)
                                }
                            }


                        }
                    }
//                    val keyClicked = list[position].key!!
//
//                    for(snap in snapshot?.child(keyClicked)?.child("orderItem").children){
//
//                        for(snap1 in snap.child("orderFoodItem").children){
//                            val foodName = snap1.child("orderFoodName").getValue(String::class.java)
//                            val foodPrice = snap1.child("orderFoodPrice").getValue(String::class.java)
//                            val qty = snap1.child("orderFoodQty").getValue(String::class.java)
//
//                            val foodSubPrice = snap.child("orderSubPrice").getValue(String::class.java)
//                            val foodTotalPrice = snap.child("orderTotalPrice").getValue(String::class.java)
//                            val foodDiscountPrice = snap.child("orderDiscountPrice").getValue(String::class.java)
//
//
//                            //might have bug in future, when display subtotal,total,discount
//                            itemSubPrice.text = "Subtotal: " + foodSubPrice
//                            itemTotalPrice.text = "Total Price: " + foodTotalPrice
//                            itemDiscountPrice.text = "Discount:   0.0" + foodDiscountPrice
//
//                            val OrderDetailsData = OrderReceiveDetailsData(foodName,foodPrice,qty,foodSubPrice,foodTotalPrice,foodDiscountPrice)
//                            orderDetailsList.add(OrderDetailsData)
//                        }
//
//                    }


                    recyclerView.adapter = OrderReceiveDetailsAdapter(this@VendorOrderReceiveActivity,orderDetailsList)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })


    }
}