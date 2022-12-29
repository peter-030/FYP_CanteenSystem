package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import com.example.fypcanteensystem.databinding.ActivityVendorStockReportBinding
import com.example.fypcanteensystem.model.FoodItemData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class VendorStockReportActivity : AppCompatActivity() {
    private lateinit var binding : ActivityVendorStockReportBinding
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference?=null
    private var database : FirebaseDatabase?= null
    private lateinit var genericMap : MutableMap<String, Int>
    private lateinit var tv2:TextView
    private lateinit var tv4:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorStockReportBinding.inflate(layoutInflater)
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

        var totalFoodPrice : Double = 0.0
        var totalFoodQty : Int = 0
        var totalPrice : Double = 0.0
        val user = auth.uid


        var foodNo : Int? = 0
        val list = arrayListOf<String>()
        databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemClassMap = mutableMapOf<String, FoodItemData>()
                var dateMap = mutableMapOf<String,String>()
                //load order details
                if(snapshot.exists()){


                    for(userSnapshot in snapshot.children)
                    {


                        for(snap in userSnapshot.child("orderItem").children){

                            for(snap1 in snap.child("orderFoodItem").children){

                                if(snap.child("orderStatus").getValue(String::class.java) == "Completed" && snap.child("orderVendorId").value.toString() == user!!.toString()){
                                    binding.reportTitle.text=snap.child("orderVendorName").getValue(String::class.java) + " - Sales Report(Quantity)"
                                    val foodId = snap1.child("orderFoodId").getValue(String::class.java)!!
                                    val foodPrice = snap1.child("orderFoodPrice").value
                                    val foodQty = snap1.child("orderFoodQty").getValue(String::class.java)!!

                                    if(!genericMap.containsKey(foodId)) {
                                        var newItem = FoodItemData(snap1.child("orderFoodName").getValue(String::class.java),"","","","")

                                        genericMap.put( foodId, foodQty.toInt())
                                        dateMap.put(foodId,snap.child("orderDateTime").getValue(String::class.java)!!.split(" ")[0])
                                        itemClassMap.put(foodId,  newItem)


                                    }else{
                                        var oldQuantity = genericMap.getValue(foodId)

                                        genericMap.put( foodId, oldQuantity+foodQty.toInt())
                                    }


                                }

                            }


                        }
                    }
                    println("testing:" + genericMap)
                    var pend :Int = 1
                    for(key in genericMap.keys) {

                        val qty = genericMap.get(key)
                        //totalFoodQty = totalFoodQty + qty!!
                        tv2 = TextView(applicationContext)

                        tv4 = TextView(applicationContext)


                        tv2.setText(itemClassMap.get(key)?.itemName)

                        tv4.setText(qty.toString())

                        /* Create a new row to be added. */
                        val tr = TableRow(applicationContext)

                        /* Create a textview to be the row-content. */
                        val tv1 = TextView(applicationContext)

                        tv1.setText(pend.toString())
                        pend +=1
                        tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                        tr.addView(tv1)
                        //val tv2 = TextView(applicationContext)
                        //tv2.setText(" Chicken Rice ")
                        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                        tr.addView(tv2)





                        tv4.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
                        tr.addView(tv4)




                        tv4.gravity = Gravity.CENTER
                        tv1.setPadding(20)
                        tv4.setPadding(20)

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

                        tv4.setLayoutParams(
                            TableRow.LayoutParams(
                                0,
                                TableRow.LayoutParams.WRAP_CONTENT,0.4f
                            )
                        )

                        /* Add row to TableLayout. */
                        //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                        binding.stockTable.addView(
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

}