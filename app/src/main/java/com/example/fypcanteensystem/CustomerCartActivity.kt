package com.example.fypcanteensystem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.adapter.CartFoodListAdapter
import com.example.fypcanteensystem.adapter.FoodMenuListAdapter
import com.example.fypcanteensystem.dataModels.CartFoodListData
import com.example.fypcanteensystem.dataModels.FoodMenuListData
import com.example.fypcanteensystem.databinding.ActivityCustomerCartBinding
import com.example.fypcanteensystem.databinding.ActivityCustomerMenuDisplayBinding
import com.example.fypcanteensystem.databinding.CartEditPopupBinding
import com.example.fypcanteensystem.databinding.CartListViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList
import java.util.concurrent.Executors

class CustomerCartActivity : AppCompatActivity(), CartFoodListAdapter.onItemClickListener {
    private lateinit var binding: ActivityCustomerCartBinding
    private lateinit var bindingEditCart: CartEditPopupBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference : DatabaseReference? =null
    private var database : FirebaseDatabase? =null
    private lateinit var cartFoodListArray: ArrayList<CartFoodListData>
    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("customerProfile")

        this.setTitle("My Cart")

        binding.rvCartFoodList.layoutManager = LinearLayoutManager(this)
        binding.rvCartFoodList.setHasFixedSize(true)
        loadCartFoodData()

    }

    private fun loadCartFoodData() {
        val userId = auth.currentUser?.uid!!
        val cartReference = databaseReference?.child(userId)?.child("cartItem")

        cartReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    cartFoodListArray = arrayListOf()
                    for (foodCartListSnapshot in snapshot.children){
                        //val cartId = foodCartListSnapshot.key
                        val cartFoodVendorId = foodCartListSnapshot.child("cartFoodVendorId").getValue(String::class.java)
                        val cartFoodId = foodCartListSnapshot.child("cartFoodId").getValue(String::class.java)
                        val cartFoodImage = foodCartListSnapshot.child("ImageUri").getValue(String::class.java)
                        val cartFoodName = foodCartListSnapshot.child("cartFoodName").getValue(String::class.java)
                        val cartFoodPrice = foodCartListSnapshot.child("cartFoodPrice").getValue(String::class.java)
                        val cartFoodQty = foodCartListSnapshot.child("cartFoodQty").getValue(String::class.java)

                        //val cartFoodList = CartFoodListData(cartId, cartFoodVendorId, cartFoodId, cartFoodImage, cartFoodName, cartFoodPrice, cartFoodQty)
                        val cartFoodList = CartFoodListData(cartFoodVendorId, cartFoodId, cartFoodImage, cartFoodName, cartFoodPrice, cartFoodQty)
                        cartFoodListArray.add(cartFoodList!!)
                    }
                    binding.rvCartFoodList.adapter = CartFoodListAdapter(cartFoodListArray, context, context)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    override fun itemClick(position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        bindingEditCart = CartEditPopupBinding.inflate(layoutInflater)
        dialogBuilder.setView(bindingEditCart.root)
        val dialog = dialogBuilder.create()
        dialog.show()

        bindingEditCart.cvBtnCartMinusPopup.setOnClickListener(){
            print("-")
        }

        bindingEditCart.cvBtnCartPlusPopup.setOnClickListener(){
            print("+")
        }
    }



}