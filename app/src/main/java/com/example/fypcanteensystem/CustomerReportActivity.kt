package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.adapter.OrderFoodListAdapter
import com.example.fypcanteensystem.adapter.OrderListAdapter
import com.example.fypcanteensystem.databinding.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.github.mikephil.charting.animation.Easing

import com.github.mikephil.charting.formatter.PercentFormatter

import com.github.mikephil.charting.data.PieData

import android.graphics.Color
import com.example.fypcanteensystem.dataModels.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry

import com.github.mikephil.charting.data.PieDataSet

import com.github.mikephil.charting.utils.ColorTemplate
import android.R.attr.data
import android.widget.Toast
import java.util.*


class CustomerReportActivity : AppCompatActivity(), OrderListAdapter.onItemClickListener  {
    private lateinit var binding: ActivityCustomerReportBinding
    private lateinit var bindingPurchasedHistory: ActivityCustomerOrderBinding
    private lateinit var bindingPurchasedHistoryFoodList: OrderFoodListPopupBinding
    private lateinit var bindingReportRating: OrderRatingPopupBinding
    private lateinit var bindingMonthlyPiechartReport: MonthlyPiechartReportBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseVendReference: DatabaseReference
    private lateinit var databaseCustReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var historyReference: DatabaseReference
    private lateinit var historyFoodReference: DatabaseReference
    private lateinit var historyRatingVendReference: DatabaseReference
    private lateinit var historyRatingCustReference: DatabaseReference
    private lateinit var pieChartReference: DatabaseReference

    private lateinit var orderHistoryListArray: ArrayList<OrderListData>
    private lateinit var orderFoodHistoryListArray: ArrayList<OrderFoodListData>
    //private lateinit var vendorAmtListArray: ArrayList<VendorIdData>
    private lateinit var pieChartListArray: ArrayList<PieChartListData>
    private var context = this

    private lateinit var historyListener: ValueEventListener
    private lateinit var historyFoodListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar!!.title = "My Report"
        actionbar.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseCustReference = database?.reference!!.child("customerProfile")
        databaseVendReference = database?.reference!!.child("vendorProfile")

        var userId = auth.currentUser?.uid!!
        //vendorAmtListArray = arrayListOf<VendorIdData>()
        pieChartListArray = arrayListOf<PieChartListData>()


        pieChartReference = FirebaseDatabase.getInstance().getReference("customerProfile").child(userId).child("orderItem")
        pieChartReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    pieChartListArray = arrayListOf()
                    for (vendAmttListSnapshot in snapshot.children){
                        val vendorName = vendAmttListSnapshot.child("orderVendorName").getValue(String::class.java)
                        val status = vendAmttListSnapshot.child("orderStatus").getValue(String::class.java)
                        val qty = vendAmttListSnapshot.child("orderTotalQty").getValue(String::class.java)

                        if(status == "Completed"){
                            var duplicate = false
                            for(isDuplicate in pieChartListArray) {
                                if(vendorName == isDuplicate.vendorName) {
                                    duplicate = true
                                    var orgAmt = isDuplicate.orderAmt!!.toInt()
                                    var totalAmt = orgAmt + qty!!.toInt()
                                    isDuplicate.orderAmt = totalAmt.toString()

                                }
                            }
                            if(!duplicate) {
                                val pieData = PieChartListData(vendorName, qty)
                                pieChartListArray.add(pieData)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        binding.btnReportPurchasedHistory.setOnClickListener(){
            bindingPurchasedHistory = ActivityCustomerOrderBinding.inflate(layoutInflater)
            setContentView(bindingPurchasedHistory.root)

            bindingPurchasedHistory.rvOrderList.layoutManager = LinearLayoutManager(this)
            bindingPurchasedHistory.rvOrderList.setHasFixedSize(true)

            orderHistoryListArray = arrayListOf<OrderListData>()
            orderFoodHistoryListArray = arrayListOf<OrderFoodListData>()

            loadHistorylistData()
        }

        binding.btnReportMontlyPurchased.setOnClickListener(){
            bindingMonthlyPiechartReport = MonthlyPiechartReportBinding.inflate(layoutInflater)
            setContentView(bindingMonthlyPiechartReport.root)


            loadMonthlyPiechartReport()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun loadMonthlyPiechartReport(){

        /*
        //xValues
        val xValues = ArrayList<String>()
        xValues.add("A")
        xValues.add("B")
        xValues.add("C")

        //xValues
        val yValues = ArrayList<Float>()
        yValues.add(1f)
        yValues.add(2f)
        yValues.add(3f)

        //colors
        val colors = ArrayList<Int>()
        colors.add(Color.BLUE)
        colors.add(Color.RED)
        colors.add(Color.GREEN)

        */
        val rnd = Random()
        val xValues = ArrayList<String>()
        val yValues = ArrayList<Float>()
        val colors = ArrayList<Int>()

        for(pieData in pieChartListArray)
        {
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

            xValues.add(pieData.vendorName!!)
            yValues.add(pieData.orderAmt!!.toFloat())
            colors.add(color)
        }

        //yValues
        val pieChartEntry = ArrayList<Entry>()

        for((i, item) in yValues.withIndex()){
            pieChartEntry.add(Entry(item, i))
        }


        val pieDataSet = PieDataSet(pieChartEntry, "")
        pieDataSet.colors = colors
        pieDataSet.sliceSpace = 5f


        val data = PieData(xValues, pieDataSet)
        data.setValueTextSize(15f)
        bindingMonthlyPiechartReport.pieChart.data = data

        bindingMonthlyPiechartReport.pieChart.holeRadius = 0f
        bindingMonthlyPiechartReport.pieChart.setDescription("Vendors List")
        bindingMonthlyPiechartReport.pieChart.setDescriptionTextSize(30f)
        bindingMonthlyPiechartReport.pieChart.animateY(1000)

        val legend : Legend = bindingMonthlyPiechartReport.pieChart.legend
        legend.position = Legend.LegendPosition.BELOW_CHART_RIGHT
        //legend.form = Legend.LegendForm.
        legend.textColor = resources.getColor(R.color.black)
        legend.textSize = 15f
        legend.formSize = 13f
    }


    private fun loadHistorylistData() {
        val userId = auth.currentUser?.uid!!
        historyReference = databaseCustReference.child(userId)?.child("orderItem")
        historyListener = historyReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    orderHistoryListArray = arrayListOf()
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

                        if(orderStatus == "Completed" || orderStatus == "Cancelled") {
                            val historyList = OrderListData(
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
                            orderHistoryListArray.add(historyList)
                        }
                    }
                    bindingPurchasedHistory.rvOrderList.adapter = OrderListAdapter(orderHistoryListArray, context, context)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

    }

    override fun itemClick(position: Int) {

        val dialogBuilder = AlertDialog.Builder(this)
        bindingPurchasedHistoryFoodList = OrderFoodListPopupBinding.inflate(layoutInflater)
        dialogBuilder.setView(bindingPurchasedHistoryFoodList.root)
        val dialog = dialogBuilder.create()
        dialog.show()

        bindingPurchasedHistoryFoodList.rvOrderFoodItemList.layoutManager = LinearLayoutManager(this)
        bindingPurchasedHistoryFoodList.rvOrderFoodItemList.setHasFixedSize(true)

        val selectedOrder = orderHistoryListArray[position]
        val orderId = selectedOrder.orderId.toString()

        val userId = auth.currentUser?.uid!!
        historyFoodReference =
            databaseCustReference.child(userId)?.child("orderItem")?.child(orderId)
                ?.child("orderFoodItem")
        //orderFoodReference = database2?.reference!!.child("customerProfile").child(userId)?.child("orderItem")?.child(orderId)?.child("orderFoodItem")

        historyFoodListener =
            historyFoodReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        orderFoodHistoryListArray = arrayListOf()
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
                            orderFoodHistoryListArray.add(orderFoodList)
                        }
                        bindingPurchasedHistoryFoodList.rvOrderFoodItemList.adapter =
                            OrderFoodListAdapter(orderFoodHistoryListArray, context)
                        bindingPurchasedHistoryFoodList.tvOrderIdPopUp.text = "Order Id: $orderId"
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
            bindingReportRating = OrderRatingPopupBinding.inflate(layoutInflater)
            dialogBuilder.setView(bindingReportRating.root)
            val dialog = dialogBuilder.create()
            dialog.show()

            val userId = auth.currentUser?.uid!!
            val orderId = orderHistoryListArray[position].orderId
            val vendorId = orderHistoryListArray[position].vendorId
            var totalRate: String? = null
            var totalRateNo: String? = null

            bindingReportRating.btnRatingSubmit.setOnClickListener() {
                val rating = bindingReportRating.ratingBar.rating.toString()

                historyRatingCustReference =
                    databaseCustReference.child(userId)?.child("orderItem")?.child(orderId!!)
                historyRatingCustReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            historyRatingCustReference?.child("orderRating")?.setValue(rating)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })

                historyRatingVendReference = databaseVendReference.child(vendorId!!)
                historyRatingVendReference?.addListenerForSingleValueEvent(object : ValueEventListener {
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
                            historyRatingVendReference?.child("Total Rate")?.setValue(totalRate.toString())
                            historyRatingVendReference?.child("Total Rate Number")
                                ?.setValue(totalRateNo.toString())

                            var averageRate = totalRate!!.toDouble() / totalRateNo!!.toDouble()
                            historyRatingVendReference?.child("Rate Average")
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

            bindingReportRating.btnRatingSkip.setOnClickListener() {
                historyRatingCustReference = databaseCustReference.child(userId)?.child("orderItem")?.child(orderId!!)
                historyRatingCustReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            historyRatingCustReference?.child("orderRating")?.setValue("Skipped")
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
}