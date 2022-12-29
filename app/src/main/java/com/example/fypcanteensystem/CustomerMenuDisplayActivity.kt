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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.adapter.FoodMenuListAdapter
import com.example.fypcanteensystem.dataModels.CartFoodListData
import com.example.fypcanteensystem.dataModels.FoodMenuListData
import com.example.fypcanteensystem.dataModels.VendorIdData
import com.example.fypcanteensystem.databinding.ActivityCustomerMenuDisplayBinding
import com.example.fypcanteensystem.databinding.AddToCartPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.Executors
import java.util.*

class CustomerMenuDisplayActivity : AppCompatActivity(), FoodMenuListAdapter.onItemClickListener {

    private lateinit var binding: ActivityCustomerMenuDisplayBinding
    private lateinit var bindingAddToCart: AddToCartPopupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var databaseFoodReference : DatabaseReference
    private lateinit var databaseCustReference : DatabaseReference
    private lateinit var cartOrderReference: DatabaseReference
    private lateinit var foodReference: DatabaseReference
    private lateinit var cartReference: DatabaseReference
    private lateinit var vendorReference: DatabaseReference
    private lateinit var wishlistReference: DatabaseReference
    private lateinit var wishlistPushReference: DatabaseReference

    private lateinit var database : FirebaseDatabase
    private lateinit var foodMenuListArray: ArrayList<FoodMenuListData>
    private lateinit var foodCartListArray: ArrayList<CartFoodListData>
    private lateinit var cartVendorArray: ArrayList<VendorIdData>
    private var context = this
    private lateinit var selectedVendorId: String
    private lateinit var selectedFoodId: String
    private var selectedFoodQty = 1
    private lateinit var cartOrderListener: ValueEventListener
    private lateinit var foodListener: ValueEventListener
    private lateinit var wishlistListener: ValueEventListener
    private lateinit var wishlistPushListener: ValueEventListener

    private lateinit var vendorListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMenuDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar!!.title = "Menu"
        actionbar.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")
        databaseCustReference = database?.reference!!.child("customerProfile")

        cartVendorArray = arrayListOf<VendorIdData>()

        selectedVendorId = intent.getStringExtra("vendorId")!!
        binding.rvFoodMenuList.layoutManager = LinearLayoutManager(this)
        binding.rvFoodMenuList.setHasFixedSize(true)
        //val foodReference = databaseReference?.child(user?.uid!!)?.child("foodItem")
        loadVendorMenuData(selectedVendorId!!)

        var userId = auth.currentUser?.uid!!

        databaseFoodReference = database?.reference!!.child("vendorProfile")
        databaseCustReference = database?.reference!!.child("customerProfile")

        cartOrderReference= databaseCustReference?.child(userId)?.child("cartItem")
        var cartVendorId : String? = null

        cartOrderListener = cartOrderReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                        cartVendorArray = arrayListOf()
                        for (foodCartListSnapshot in snapshot.children) {
                            cartVendorId = foodCartListSnapshot.child("cartVendorId")
                                .getValue(String::class.java)
                        }
                        val vendId = VendorIdData(cartVendorId)
                        cartVendorArray.add(vendId)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        binding.cvMenuWishlist.setOnClickListener(){
            actionWishlist(selectedVendorId!!)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun actionWishlist(vendorId: String) {
        val userId = auth.currentUser?.uid!!
        wishlistReference = databaseCustReference?.child(userId)?.child("wishlist")
        wishlistPushReference = databaseCustReference?.child(userId)?.child("wishlist")?.push()

        if(binding.imgMenuWishlist.drawable == null) {
            wishlistPushReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    wishlistPushReference?.child("wishlistVendorId").setValue(vendorId)
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }
            })

            binding.imgMenuWishlist.setImageResource(R.drawable.wishlist)
            Toast.makeText(this, "Wishlist Added", Toast.LENGTH_LONG).show()
        }
        else {
            wishlistReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (wishlistVendorSnapshot in snapshot.children) {
                            val wishListId = wishlistVendorSnapshot.key
                            val wishVendorId = wishlistVendorSnapshot.child("wishlistVendorId").getValue(String::class.java)

                            if(vendorId == wishVendorId){
                                wishlistReference?.child(wishListId!!).removeValue()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")

                }
            })

            binding.imgMenuWishlist.setImageDrawable(null)
            Toast.makeText(this, "Wishlist Removed", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadVendorMenuData(vendorId: String) {
        val userId = auth.currentUser?.uid!!
        wishlistReference = databaseCustReference?.child(userId)?.child("wishlist")

        wishlistReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (wishlistVendorSnapshot in snapshot.children) {
                        val wishListId = wishlistVendorSnapshot.key
                        val wishVendorId = wishlistVendorSnapshot.child("wishlistVendorId").getValue(String::class.java)

                        if(vendorId == wishVendorId){
                            binding.imgMenuWishlist.setImageResource(R.drawable.wishlist)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")

            }
        })


        vendorReference = databaseReference?.child(vendorId)

        vendorListener = vendorReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var image: Bitmap? = null
                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    val imgURL = snapshot.child("ImageUri").value.toString()

                    //get the image and post it in the ImageView
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

                val rentalCode = snapshot.child("Rental Code").value.toString().split("-")
                val shopLocation = rentalCode[0]
                val shopCode = rentalCode[1]

                binding.tvCanteenLocation.setText(shopLocation.uppercase(Locale.getDefault()) + " Canteen \n" + shopLocation[0].uppercase(Locale.getDefault()) + "-$shopCode")

                binding.tvMerchantNameMenu.setText(snapshot.child("Merchant Name").value.toString())
                binding.tvPhoneNoMenu.setText(snapshot.child("Phone Number").value.toString())

                val vendorRate = snapshot.child("Rate Average").value.toString().toFloatOrNull()
                if(vendorRate != null)
                    binding.vendorAvgRateMenu.rating = vendorRate
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        foodReference = databaseReference?.child(vendorId)?.child("foodItem")
        foodListener = foodReference?.addValueEventListener(object : ValueEventListener {
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
                //TODO("Not yet implemented")
            }
        })
    }
    override fun itemClick(position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        bindingAddToCart = AddToCartPopupBinding.inflate(layoutInflater)
        dialogBuilder.setView(bindingAddToCart.root)
        val dialog = dialogBuilder.create()
        dialog.show()

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
        selectedFoodId = selectedFood.foodId!!
        bindingAddToCart.tvSelectedFoodQuantity.text = selectedFoodQty.toString()
        bindingAddToCart.tvSelectedFoodName.text = selectedFood.foodName
        bindingAddToCart.tvSelectedFoodPrice.text = "RM " + selectedFood.foodPrice

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
            //checkCartVendor()
            checkVendor()
            dialog.cancel()
        }

    }

    private fun checkVendor(){
        var userId = auth.currentUser?.uid!!
        var vendorId = selectedVendorId

        if(cartVendorArray.size == 0){
            addToCart()
        }
        else if(cartVendorArray.size == 1) {
            for(checkId in cartVendorArray){
                if(checkId.vendorId == vendorId){
                    addToCart()
                } else {

                    Toast.makeText(this, "", Toast.LENGTH_LONG).show()
                    AlertDialog.Builder(this)
                        .setTitle("Cart existed in different shop")
                        .setMessage("Do you want to REMOVE cart and ADD this instead?")
                        .setPositiveButton("Yes"){
                                _, _->

                            databaseCustReference?.child(userId)?.child("cartItem")?.removeValue()
                            cartVendorArray.clear()
                            addToCart()

                        }
                        .setNegativeButton("No"){
                                _, _->
                        }
                        .create()
                        .show()


                }
            }
        }

    }

    private fun addToCart(){
        var vendorId = selectedVendorId


            var userId = auth.currentUser?.uid!!
            var foodId = selectedFoodId
            var foodImage: String?
            var foodName: String?
            var foodPrice: String?
            var foodQty: String? = selectedFoodQty.toString()

            val key = database!!.getReference("customerProfile").push().key

            cartReference = databaseCustReference?.child((userId))?.child("cartItem")?.child(key!!)
            foodReference = databaseFoodReference?.child(vendorId.toString())?.child("foodItem")
                    ?.child(foodId.toString())
            foodReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        foodImage = snapshot.child("ImageUri").getValue(String::class.java)
                        foodName = snapshot.child("itemName").getValue(String::class.java)
                        foodPrice = snapshot.child("itemPrice").getValue(String::class.java)

                        cartReference?.child("ImageUri")?.setValue(foodImage)
                        cartReference?.child("cartVendorId")?.setValue(vendorId)
                        cartReference?.child("cartFoodId")?.setValue(foodId)
                        cartReference?.child("cartFoodName")?.setValue(foodName)
                        cartReference?.child("cartFoodPrice")?.setValue(foodPrice)
                        cartReference?.child("cartFoodQty")?.setValue(foodQty)
                        cartReference?.child("cartId")?.setValue(key)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }
            })
            Toast.makeText(this, "Food Added", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cartOrderListener?.let { cartOrderReference!!.removeEventListener(it) }
        //foodListener?.let { foodReference!!.removeEventListener(it) }
        //wishlistListener?.let { wishlistReference!!.removeEventListener(it) }
        //wishlistPushListener?.let { wishlistPushReference!!.removeEventListener(it) }
        vendorListener?.let { vendorReference!!.removeEventListener(it) }
    }
}