package com.example.fypcanteensystem


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.adapter.FoodMenuListAdapter
import com.example.fypcanteensystem.adapter.VendorsListAdapter
import com.example.fypcanteensystem.dataModels.CartFoodListData
import com.example.fypcanteensystem.dataModels.FoodMenuListData
import com.example.fypcanteensystem.dataModels.VendorsListData
import com.example.fypcanteensystem.databinding.ActivityCustomerMenuDisplayBinding
import com.example.fypcanteensystem.databinding.AddToCartPopupBinding
import com.example.fypcanteensystem.databinding.CartEditPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.Executors
import java.util.*

class CustomerMenuDisplayActivity : AppCompatActivity(), FoodMenuListAdapter.onItemClickListener {

    private lateinit var binding: ActivityCustomerMenuDisplayBinding
    private lateinit var bindingAddToCart: AddToCartPopupBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference : DatabaseReference? =null
    private var databaseFoodReference : DatabaseReference? =null
    private var databaseCustReference : DatabaseReference? =null
    private var database : FirebaseDatabase? =null
    private lateinit var foodMenuListArray: ArrayList<FoodMenuListData>
    private lateinit var foodCartListArray: ArrayList<CartFoodListData>
    private var context = this
    private var selectedVendorId: String? = null
    private var selectedFoodId: String? = null
    private var selectedFoodQty = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMenuDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Menu")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")
        databaseCustReference = database?.reference!!.child("customerProfile")

        selectedVendorId = intent.getStringExtra("vendorId")!!
        binding.rvFoodMenuList.layoutManager = LinearLayoutManager(this)
        binding.rvFoodMenuList.setHasFixedSize(true)
        //val foodReference = databaseReference?.child(user?.uid!!)?.child("foodItem")
        loadVendorMenuData(selectedVendorId!!)



    }

    private fun loadVendorMenuData(vendorId: String) {
        val vendorReference = databaseReference?.child(vendorId)

        vendorReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var image: Bitmap? = null
                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    val imgURL = snapshot.child("ImageUri").value.toString()

                    // get the image and post it in the ImageView
                    try {
                        val `in` = java.net.URL(imgURL).openStream()
                        image = BitmapFactory.decodeStream(`in`)
                        handler.post {
                            binding.imgVendorMenu.setImageBitmap(image)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                binding.tvMerchantNameMenu.setText(snapshot.child("Merchant Name").value.toString())
                binding.tvPhoneNoMenu.setText(snapshot.child("Phone Number").value.toString())

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val foodReference = databaseReference?.child(vendorId)?.child("foodItem")
        foodReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    foodMenuListArray = arrayListOf()
                    for (foodMenuListSnapshot in snapshot.children){
                        val foodId = foodMenuListSnapshot.key
                        val foodName = foodMenuListSnapshot.child("itemName").getValue(String::class.java)
                        val foodPrice = foodMenuListSnapshot.child("itemPrice").getValue(String::class.java)
                        val foodStatus = foodMenuListSnapshot.child("itemStatus").getValue(String::class.java)
                        val foodDescription = foodMenuListSnapshot.child("itemDescription").getValue(String::class.java)
                        val foodImage = foodMenuListSnapshot.child("ImageUri").getValue(String::class.java)

                        val foodMenu = FoodMenuListData(foodId, foodName, foodPrice, foodStatus, foodDescription, foodImage)
                        if(foodStatus.equals("shelf"))
                            foodMenuListArray.add(foodMenu!!)
                    }
                    binding.rvFoodMenuList.adapter = FoodMenuListAdapter(foodMenuListArray, context, context)
                }

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    override fun itemClick(position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        bindingAddToCart = AddToCartPopupBinding.inflate(layoutInflater)
        dialogBuilder.setView(bindingAddToCart.root)
        val dialog = dialogBuilder.create()
        dialog.show()

        bindingAddToCart.cvBtnMinus.setOnClickListener(){
            if( selectedFoodQty > 1){
                selectedFoodQty -= 1
                bindingAddToCart.tvSelectedFoodQuantity.text = selectedFoodQty.toString()
            }
            else {
                selectedFoodQty -= 1
                dialog.cancel()
            }
        }
        bindingAddToCart.cvBtnPlus.setOnClickListener(){
            selectedFoodQty += 1
            bindingAddToCart.tvSelectedFoodQuantity.text = selectedFoodQty.toString()
        }

        bindingAddToCart.btnAddFoodToCart.setOnClickListener(){
            addFoodToCart()
            dialog.cancel()
        }

        val selectedFood = foodMenuListArray[position]
        selectedFoodQty = 1

        var image: Bitmap? = null
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val imgURL = selectedFood.foodImage

            // get the image and post it in the ImageView
            try {
                val `in` = java.net.URL(imgURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
                handler.post {
                    bindingAddToCart.imgSelectedFood.setImageBitmap(image)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        selectedFoodId = selectedFood.foodId
        bindingAddToCart.tvSelectedFoodQuantity.text = selectedFoodQty.toString()
        bindingAddToCart.tvSelectedFoodName.text = selectedFood.foodName
        bindingAddToCart.tvSelectedFoodPrice.text = "RM " + selectedFood.foodPrice

    }

    private fun addFoodToCart() {
        if(auth.currentUser != null)
        {
            var userId = auth.currentUser?.uid!!
            var vendorId = selectedVendorId
            var foodId = selectedFoodId
            var foodImage : String?
            var foodName : String?
            var foodPrice : String?
            var foodQty : String?  = selectedFoodQty.toString()

            databaseFoodReference = database?.reference!!.child("vendorProfile")

            val key = database!!.getReference("customerProfile").push().key

            val currentUserDb = databaseCustReference?.child((userId))?.child("cartItem")?.child(key!!)
            val foodReference = databaseFoodReference?.child(vendorId.toString())?.child("foodItem")?.child(foodId.toString())
            foodReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        foodImage = snapshot.child("ImageUri").getValue(String::class.java)
                        foodName = snapshot.child("itemName").getValue(String::class.java)
                        foodPrice = snapshot.child("itemPrice").getValue(String::class.java)

                        //currentUserDb?.setValue(CartFoodListData(vendorId, foodId, foodImage, foodName, foodPrice, foodQty))
                        currentUserDb?.child("ImageUri")?.setValue(foodImage)
                        currentUserDb?.child("cartVendorId")?.setValue(vendorId)
                        currentUserDb?.child("cartFoodId")?.setValue(foodId)
                        currentUserDb?.child("cartFoodName")?.setValue(foodName)
                        currentUserDb?.child("cartFoodPrice")?.setValue(foodPrice)
                        currentUserDb?.child("cartFoodQty")?.setValue(foodQty)

                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            Toast.makeText(this, "Food Added", Toast.LENGTH_LONG).show()
        }
        else
            Toast.makeText(this, "Food id Error", Toast.LENGTH_LONG).show()
    }

}