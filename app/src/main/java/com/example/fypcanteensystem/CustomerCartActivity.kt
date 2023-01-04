package com.example.fypcanteensystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.adapter.CartFoodListAdapter
import com.example.fypcanteensystem.dataModels.CartFoodListData
import com.example.fypcanteensystem.databinding.ActivityCustomerCartBinding
import com.example.fypcanteensystem.databinding.CartEditPopupBinding
import com.example.fypcanteensystem.databinding.CartListViewBinding
import com.example.fypcanteensystem.functionClass.SwipeToRemove
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList
import java.util.concurrent.Executors
import android.R.string.no
import androidx.appcompat.app.ActionBar
import androidx.core.text.isDigitsOnly


class CustomerCartActivity : AppCompatActivity(), CartFoodListAdapter.onItemClickListener {
    private lateinit var binding: ActivityCustomerCartBinding
    private lateinit var bindingCart: CartListViewBinding
    private lateinit var bindingEditCart: CartEditPopupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var cartReference: DatabaseReference
    private lateinit var cartEditReference: DatabaseReference
    private lateinit var cartDeleteReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var cartFoodListArray: ArrayList<CartFoodListData>
    private var context = this
    private lateinit var cartFoodListener: ValueEventListener
    private lateinit var cartDeleteListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference!!.child("customerProfile")

        val actionbar = supportActionBar
        actionbar!!.title = "My Cart"
        actionbar.setDisplayHomeAsUpEnabled(true)

        binding.rvCartFoodList.layoutManager = LinearLayoutManager(this)
        binding.rvCartFoodList.setHasFixedSize(true)
        loadCartFoodData()

        //swipe cart
        val swipeToRemoveCart = object : SwipeToRemove() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                removeCartDialog(position)
            }

        }
        val itemTouchHelper = ItemTouchHelper(swipeToRemoveCart)
        itemTouchHelper.attachToRecyclerView(binding.rvCartFoodList)


        binding.btnCheckOut.setOnClickListener() {
            if (binding.tvCartQty.text.toString() == "0 piece") {
                Toast.makeText(this, "Cart cannot be empty.", Toast.LENGTH_SHORT).show()

            } else {
                val intent = Intent(this, CustomerPaymentActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun loadCartFoodData() {


        val userId = auth.currentUser?.uid!!
        cartReference = databaseReference.child(userId).child("cartItem")

        cartFoodListener = cartReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    cartFoodListArray = arrayListOf()
                    var subPrice: Double = 0.0
                    var totalPrice: Double = 0.0
                    var totalQty: Int = 0

                    for (foodCartListSnapshot in snapshot.children) {
                        val cartId = foodCartListSnapshot.key
                        val cartFoodVendorId =
                            foodCartListSnapshot.child("cartVendorId").getValue(String::class.java)
                        val cartFoodId =
                            foodCartListSnapshot.child("cartFoodId").getValue(String::class.java)
                        val cartFoodImage =
                            foodCartListSnapshot.child("ImageUri").getValue(String::class.java)
                        val cartFoodName =
                            foodCartListSnapshot.child("cartFoodName").getValue(String::class.java)
                        val cartFoodPrice =
                            foodCartListSnapshot.child("cartFoodPrice").getValue(String::class.java)
                        val cartFoodQty =
                            foodCartListSnapshot.child("cartFoodQty").getValue(String::class.java)

                        val cartFoodList = CartFoodListData(
                            cartId,
                            cartFoodVendorId,
                            cartFoodId,
                            cartFoodImage,
                            cartFoodName,
                            cartFoodPrice,
                            cartFoodQty
                        )
                        subPrice = cartFoodPrice!!.toDouble() * cartFoodQty!!.toInt()
                        totalPrice += subPrice
                        totalQty += cartFoodQty!!.toInt()

                        cartFoodListArray.add(cartFoodList!!)
                    }
                    binding.tvCartTotalPrice.setText("RM ${totalPrice.format(2)}")
                    binding.tvCartQty.setText("$totalQty piece(s)")
                    binding.btnCheckOut.setText("CHECK OUT (" + cartFoodListArray.size + ")")

                    binding.rvCartFoodList.adapter =
                        CartFoodListAdapter(cartFoodListArray, context, context)

                } else {
                    binding.tvCartTotalPrice.setText("RM 0.00")
                    binding.tvCartQty.setText("0 piece(s)")
                    binding.btnCheckOut.setText("CHECK OUT (0)")
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

        val selectedCartFood = cartFoodListArray[position]
        var cartFoodQty = selectedCartFood.cartFoodQty?.toInt()

        var image: Bitmap? = null
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val imgURL = selectedCartFood.cartFoodImage

            // get the image and post it in the ImageView
            try {
                val `in` = java.net.URL(imgURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
                handler.post {
                    bindingEditCart.imgCartFoodPopup.setImageBitmap(image)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val selectedCartFoodId = selectedCartFood.cartId
        bindingEditCart.tvCartFoodNamePopup.text = selectedCartFood.cartFoodName
        bindingEditCart.tvCartFoodPricePopup.text = "RM " + selectedCartFood.cartFoodPrice
        bindingEditCart.tvCartFoodQtyPopup.text = cartFoodQty.toString()


        bindingEditCart.cvBtnCartMinusPopup.setOnClickListener() {
            if (cartFoodQty != null) {
                if (cartFoodQty > 1) {
                    cartFoodQty -= 1
                    bindingEditCart.tvCartFoodQtyPopup.text = cartFoodQty.toString()
                } else {
                    removeCartDialog(position)

                    //cartFoodQty -= 1
                    dialog.cancel()
                }
            }
        }

        bindingEditCart.cvBtnCartPlusPopup.setOnClickListener() {
            if (cartFoodQty != null) {
                cartFoodQty += 1
                bindingEditCart.tvCartFoodQtyPopup.text = cartFoodQty.toString()
            }
        }

        bindingEditCart.btnCartEditCancelPopup.setOnClickListener() {
            dialog.cancel()
        }

        bindingEditCart.btnCartEditConfirmPopup.setOnClickListener() {
            val userId = auth.currentUser?.uid!!

            cartEditReference =
                databaseReference.child(userId).child("cartItem").child(selectedCartFood.cartId!!)
            cartEditReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        cartEditReference.child("cartFoodQty").setValue(cartFoodQty.toString())

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            Toast.makeText(this, "Cart Quantity is changed", Toast.LENGTH_SHORT).show()
            dialog.cancel()
        }

    }

    private fun removeCartDialog(position: Int) {
        val userId = auth.currentUser?.uid!!
        cartDeleteReference = databaseReference?.child(userId)?.child("cartItem")

        val selectedCartFood = cartFoodListArray[position]
        val selectedCartId = selectedCartFood.cartId

        AlertDialog.Builder(this)
            .setTitle("Do you want to REMOVE this food?")
            .setPositiveButton("Yes") { dialog, _ ->

                if (selectedCartId != null) {
                    cartFoodListArray.clear()
                    cartDeleteReference?.child(selectedCartId)?.removeValue()
                    binding.rvCartFoodList.adapter?.notifyDataSetChanged()
                    Toast.makeText(this, "Food Removed.", Toast.LENGTH_SHORT).show()
                    loadCartFoodData()
                }
                dialog.cancel()

            }
            .setNegativeButton("No") { dialog, _ ->
                loadCartFoodData()
                dialog.cancel()
            }
            .create()
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        cartFoodListener?.let { cartReference!!.removeEventListener(it) }
        //cartDeleteListener?.let { cartDeleteReference!!.removeEventListener(it) }
    }

}


