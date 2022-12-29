package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fypcanteensystem.databinding.ActivityVendorSalesReportBinding
import com.google.firebase.auth.FirebaseAuth
import android.R
import android.content.ContentValues.TAG
import android.text.format.DateUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button

import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import com.example.fypcanteensystem.model.FoodItemData
import com.example.fypcanteensystem.model.OrderReceiveDetailsData
import com.example.fypcanteensystem.view.OrderReceiveDetailsAdapter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class VendorSalesReportActivity : AppCompatActivity() {
    private lateinit var binding : ActivityVendorSalesReportBinding
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference?=null
    private var database : FirebaseDatabase?= null
    private lateinit var calendar: Calendar
    private lateinit var simpleDateFormat : SimpleDateFormat
    private lateinit var tv2:TextView
    private lateinit var tv3:TextView
    private lateinit var tv4:TextView
    private lateinit var tv5:TextView

    private lateinit var genericMap : MutableMap<String, Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorSalesReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Sales Report")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("customerProfile")
        genericMap = mutableMapOf<String,Int>()
        addTableChild()

        val actionbar = supportActionBar
        //actionbar!!.title = "My Cart"
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addTableChild(){
        var date : String
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        date = simpleDateFormat.format(calendar.time)

        var totalFoodPrice : Double = 0.0
        var totalFoodQty : Int = 0
        var totalPrice : Double = 0.0
        val user = auth.uid


        var foodNo : Int? = 0
        val list = arrayListOf<String>()
        databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemClassMap = mutableMapOf<String,FoodItemData>()
                var dateMap = mutableMapOf<String,String>()
                //load order details
                if(snapshot.exists()){


                    for(userSnapshot in snapshot.children)
                    {


                        for(snap in userSnapshot.child("orderItem").children){

                            for(snap1 in snap.child("orderFoodItem").children){

                                if(snap.child("orderStatus").getValue(String::class.java) == "Completed" && snap.child("orderVendorId").value.toString() == user!!.toString()){
                                    binding.reportTitle.text=snap.child("orderVendorName").getValue(String::class.java) + " - Sales Report"
                                    val foodId = snap1.child("orderFoodId").getValue(String::class.java)!!
                                    val foodPrice = snap1.child("orderFoodPrice").value
                                    val foodQty = snap1.child("orderFoodQty").getValue(String::class.java)!!
//                                    val genericMap: Map<String, Int> = mapOf<String,Int>(
//                                        foodId to foodQty.toInt(),
//                                    )
//                                    genericMap = mutableMapOf<String,Int>(
//                                        //foodId to foodQty.toInt()
//                                    )
                                    if(!genericMap.containsKey(foodId)) {
                                        var newItem = FoodItemData(snap1.child("orderFoodName").getValue(String::class.java),snap1.child("orderFoodPrice").getValue(String::class.java),"","","")

                                        genericMap.put( foodId, foodQty.toInt())
                                        dateMap.put(foodId,snap.child("orderDateTime").getValue(String::class.java)!!.split(" ")[0])
                                        itemClassMap.put(foodId,  newItem)


                                    }else{
                                        var oldQuantity = genericMap.getValue(foodId)

                                        genericMap.put( foodId, oldQuantity+foodQty.toInt())
                                    }
//                                    totalFoodQty += genericMap.get(foodId)!!
//                                    genericMap.put(foodId, totalFoodQty)

//                                    else{
//                                        for(key in genericMap.keys) {
//                                            val qty1 = genericMap.get(key)
//                                            println("test:"+qty1)
//                                            println("Element $key: ${genericMap.get(key)}")
//                                            totalFoodQty += qty1!!
//                                            println("testing : "+ totalFoodQty)
//                                        }
//
//                                    }

                                }

                            }


                        }
                    }
                    println("testing:" + genericMap)
                    for(key in genericMap.keys) {


//                                        val mergedValue = (genericMap.toList() + genericMap.toList())
//                                            .groupBy({ it.first }, { it.second })
//                                            .map { (key, values) -> key to values.sum() }
//                                            .toMap()
//                                        val peopleToSumOfYears: Map<String, Int> =
//                                            genericMap.mapValues { (key, values) -> values}
                        //val sum = genericMap.values.sum()
                        //println("Element at key position $key: ${genericMap.get(key)}")

                            val qty = genericMap.get(key)
                            //totalFoodQty = totalFoodQty + qty!!
                            tv2 = TextView(applicationContext)
                            tv3 = TextView(applicationContext)
                            tv4 = TextView(applicationContext)
                            tv5 = TextView(applicationContext)

                            tv2.setText(itemClassMap.get(key)?.itemName)
                            tv3.setText(itemClassMap.get(key)?.itemPrice)
                            tv4.setText(qty.toString())
                            totalPrice = itemClassMap.get(key)?.itemPrice!!.toDouble() * qty!!.toDouble()
                            tv5.setText(totalPrice.toString())
                            /* Create a new row to be added. */
                            val tr = TableRow(applicationContext)

                            /* Create a textview to be the row-content. */
                            val tv1 = TextView(applicationContext)
                            val date = dateMap.get(key)
                            tv1.setText(date)
                            tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                            tr.addView(tv1)
                            //val tv2 = TextView(applicationContext)
                            //tv2.setText(" Chicken Rice ")
                            tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                            tr.addView(tv2)


                            tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                            tr.addView(tv3)


                            tv4.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                            tr.addView(tv4)


                            tv5.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                            tr.addView(tv5)
                            tv3.gravity = Gravity.CENTER
                            tv4.gravity = Gravity.CENTER
                            tv5.gravity = Gravity.CENTER
                            tv3.setPadding(20)

                            tv1.setLayoutParams(
                                TableRow.LayoutParams(
                                    0,
                                    TableRow.LayoutParams.WRAP_CONTENT,0.4f
                                )
                            )
                            tv2.setLayoutParams(
                                TableRow.LayoutParams(
                                    0,
                                    TableRow.LayoutParams.WRAP_CONTENT,0.4f
                                )
                            )
                            tv3.setLayoutParams(
                                TableRow.LayoutParams(
                                    0,
                                    TableRow.LayoutParams.WRAP_CONTENT,0.4f
                                )
                            )
                            tv4.setLayoutParams(
                                TableRow.LayoutParams(
                                    0,
                                    TableRow.LayoutParams.WRAP_CONTENT,0.4f
                                )
                            )
                            tv5.setLayoutParams(
                                TableRow.LayoutParams(
                                    0,
                                    TableRow.LayoutParams.WRAP_CONTENT,0.4f
                                )
                            )
                            /* Add row to TableLayout. */
                            //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                            binding.salesTable.addView(
                                tr,
                                TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.FILL_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT
                                )
                            )
                        }


                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })



    }




    private fun Calendar.isToday() : Boolean {
        val today = Calendar.getInstance()
        return today[Calendar.YEAR] == get(Calendar.YEAR) && today[Calendar.DAY_OF_YEAR] == get(Calendar.DAY_OF_YEAR)
    }
}