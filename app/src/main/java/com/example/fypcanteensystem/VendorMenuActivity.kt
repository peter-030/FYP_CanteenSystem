package com.example.fypcanteensystem

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.databinding.ActivityVendorMenuBinding
import com.example.fypcanteensystem.model.FoodItemData
import com.example.fypcanteensystem.view.FoodItemAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.graphics.convertTo
import androidx.core.view.get
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.seconds

class VendorMenuActivity : AppCompatActivity(),FoodItemAdapter.onItemClickListener {
    private lateinit var binding : ActivityVendorMenuBinding
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv : RecyclerView
    private lateinit var foodItemList: ArrayList<FoodItemData>
    private lateinit var foodItemAdapter: FoodItemAdapter
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference? = null
    private var database : FirebaseDatabase? = null

    private lateinit var ImageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Food Item Menu")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")

        /**set List*/
        foodItemList = ArrayList()
        /**set find Id*/
        addsBtn = binding.addingBtn
        recv = binding.mRecycler
        /**set Adapter*/
        foodItemAdapter = FoodItemAdapter(this,foodItemList,this)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = foodItemAdapter


        loadAllItem()


        /**set Dialog*/
        addsBtn.setOnClickListener {


            addInfo()

        }

        val actionbar = supportActionBar
        //actionbar!!.title = "My Cart"
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadAllItem() {
        val user = auth.currentUser
        val userReference = databaseReference?.child(user?.uid!!)?.child("foodItem")


        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load food item name, price
                if(snapshot.exists()){
                    foodItemList.clear() //clear previous data before load a new one
                    for(userSnapshot in snapshot.children)
                    {
                        val item = userSnapshot.getValue(FoodItemData::class.java)
                        foodItemList.add(item!!)
                    }
                    recv.adapter = FoodItemAdapter(this@VendorMenuActivity,foodItemList,this@VendorMenuActivity)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }


    private fun addInfo() {
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.add_item,null)
        /**set view*/
        val itemName = v.findViewById<EditText>(R.id.itemName)
        val nameContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.itemNameCon)
        val priceContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.itemPriceCon)
        val descriptionContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.itemDescriptionCon)

        val itemPrice = v.findViewById<EditText>(R.id.itemPrice)
        val itemDes = v.findViewById<EditText>(R.id.itemDescription)
        val itemStatusShelf = v.findViewById<RadioButton>(R.id.itemShelf)
        val itemStatusUnshelve = v.findViewById<RadioButton>(R.id.itemUnshelve)
        val btnUploadPic = v.findViewById<Button>(R.id.btnUploadPic)

        btnUploadPic.setOnClickListener(){
            selectUserImg()
        }

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val currentTime = Date()
        val filename = formatter.format(currentTime)
        val storageReference = FirebaseStorage.getInstance().getReference("foodItemImages/${filename}.png")

        //firebase things
        val key = database!!.getReference("vendorProfile").push().key //to get random id for each food item
        val currentUser = auth.currentUser
        val currentUserDb = databaseReference?.child((currentUser?.uid!!))?.child("foodItem")?.child(key!!)



        val addDialog = AlertDialog.Builder(this)

        addDialog.setView(v)
        .setPositiveButton("Ok"){
                dialog,_->


        }
        .setNegativeButton("Cancel"){
                dialog,_->
            dialog.dismiss()
            Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show()

        }
        .create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                    //do validation here and after that update data in firebase
                    val names = itemName.text.toString()
                    val price = itemPrice.text.toString()
                    val description = itemDes.text.toString()
                    val statusShelf = itemStatusShelf.text.toString()
                    val statusUnshelve = itemStatusUnshelve.text.toString()

                    if(TextUtils.isEmpty(itemName.text.toString())){
                        nameContainer.setError("*Required!")
                        return@setOnClickListener
                    }
                    else{
                        nameContainer.error = null
                    }

                    if(TextUtils.isEmpty(itemPrice.text.toString())){
                        priceContainer.setError("*Required!")
                        return@setOnClickListener
                    }
                    else if(!itemPrice.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                        priceContainer.setError("*Invalid Price format!")
                        return@setOnClickListener
                    }
                    else{
                        priceContainer.error = null
                    }

                    if(TextUtils.isEmpty(itemDes.text.toString())){
                        descriptionContainer.setError("*Required!")
                        return@setOnClickListener
                    }
                    else{
                        descriptionContainer.error = null
                    }

                    //got error if no input image, click update btn
                    if(::ImageUri.isInitialized)
                    {
                        storageReference.putFile(ImageUri)
                            .addOnSuccessListener {
                                val result = it.metadata!!.reference!!.downloadUrl;
                                result.addOnSuccessListener {

                                    val imageLink = it.toString()
                                    currentUserDb?.child("ImageUri")?.setValue(imageLink)

                                }
                            }
                    }
                    else{

                    }


                    ImageUri = Uri.EMPTY //clear image data after upload a picture

                    if(itemStatusShelf.isChecked)
                    {
                        currentUserDb?.setValue(FoodItemData(names,price,description,statusShelf,null))
                        foodItemAdapter.notifyDataSetChanged()
                    }
                    else if(itemStatusUnshelve.isChecked)
                    {
                        currentUserDb?.setValue(FoodItemData(names,price,description,statusUnshelve,null))
                        foodItemAdapter.notifyDataSetChanged()
                    }

                    dismiss()
                    Toast.makeText(this@VendorMenuActivity, "Food Item Register Successful", Toast.LENGTH_SHORT).show()

                }
            }
        }

        .show()
    }

    override fun editInfo(position : Int){

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")

        val user = auth.currentUser
        val userReference = databaseReference?.child(user?.uid!!)?.child("foodItem")

        val v = LayoutInflater.from(this).inflate(R.layout.add_item,null)
        val name = v.findViewById<EditText>(R.id.itemName)
        val nameContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.itemNameCon)
        val priceContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.itemPriceCon)
        val descriptionContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.itemDescriptionCon)

        val price = v.findViewById<EditText>(R.id.itemPrice)
        val description = v.findViewById<EditText>(R.id.itemDescription)
        val itemStatusShelf = v.findViewById<RadioButton>(R.id.itemShelf)
        val itemStatusUnshelve = v.findViewById<RadioButton>(R.id.itemUnshelve)
        val imgView = v.findViewById<ImageView>(R.id.itemPic)

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val currentTime = Date()
        val filename = formatter.format(currentTime)
        val storageReference = FirebaseStorage.getInstance().getReference("foodItemImages/${filename}.png")

        val btnUploadPic = v.findViewById<Button>(R.id.btnUploadPic)
        btnUploadPic.setOnClickListener(){
            selectUserImg()
        }
        var image: Bitmap? = null
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val list = arrayListOf<DataSnapshot>()
        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(userSnapshot in snapshot.children)
                {
                    list.add(userSnapshot)
                    name.setText(userSnapshot.child("itemName").value.toString())
                    price.setText(userSnapshot.child("itemPrice").value.toString())
                    description.setText(userSnapshot.child("itemDescription").value.toString())

                    if(position == list.size-1) //exit loop when get all the position value in edit item page
                    {
                        break
                    }
                }

                //show radio button
                val keyClicked = list[position].key!!
                if(snapshot?.child(keyClicked)?.child("itemStatus").value.toString() == "shelf")
                {
                    itemStatusShelf!!.isChecked=true
                }
                if(snapshot?.child(keyClicked)?.child("itemStatus").value.toString() == "unshelve")
                {
                    itemStatusUnshelve!!.isChecked=true
                }
                //show image
                executor.execute {

                    val imgURL = snapshot?.child(keyClicked)?.child("ImageUri").value.toString()

                    // get the image and post it in the ImageView
                    try {
                        val `in` = java.net.URL(imgURL).openStream()
                        image = BitmapFactory.decodeStream(`in`)
                        handler.post {
                            imgView.setImageBitmap(image)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
        AlertDialog.Builder(this)
                            .setView(v)
                            .setPositiveButton("Ok"){
                                    dialog,_->

                            }
                            .setNegativeButton("Cancel"){
                                    dialog,_->
                                ImageUri = Uri.EMPTY
                                dialog.dismiss()

                            }
                            .create().apply {
                                setOnShowListener {
                                 getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                                     //do validation here and after that update data in firebase
                                     if(TextUtils.isEmpty(name.text.toString())){
                                         nameContainer.setError("*Required!")
                                         return@setOnClickListener
                                     }
                                     else{
                                         nameContainer.error = null
                                     }

                                     if(TextUtils.isEmpty(price.text.toString())){
                                         priceContainer.setError("*Required!")
                                         return@setOnClickListener
                                     }
                                     else if(!price.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                                         priceContainer.setError("*Invalid Price format!")
                                         return@setOnClickListener
                                     }
                                     else{
                                         priceContainer.error = null
                                     }

                                     if(TextUtils.isEmpty(description.text.toString())){
                                         descriptionContainer.setError("*Required!")
                                         return@setOnClickListener
                                     }
                                     else{
                                         descriptionContainer.error = null
                                     }

                                         if(position!=-1)
                                         {
                                             val keyClicked = list[position].key!!
                                             userReference?.child(keyClicked)?.child("itemName")?.setValue(name.text.toString())
                                             userReference?.child(keyClicked)?.child("itemPrice")?.setValue(price.text.toString())
                                             userReference?.child(keyClicked)?.child("itemDescription")?.setValue(description.text.toString())
                                             if(itemStatusShelf.isChecked)
                                             {
                                                 userReference?.child(keyClicked)?.child("itemStatus")?.setValue(itemStatusShelf.text.toString())
                                             }
                                             if(itemStatusUnshelve.isChecked)
                                             {
                                                 userReference?.child(keyClicked)?.child("itemStatus")?.setValue(itemStatusUnshelve.text.toString())
                                             }


                                             if(::ImageUri.isInitialized)
                                             {
                                                 storageReference.putFile(ImageUri)
                                                     .addOnSuccessListener {
                                                         val result = it.metadata!!.reference!!.downloadUrl;
                                                         result.addOnSuccessListener {

                                                             val imageLink = it.toString()
                                                             userReference?.child(keyClicked)?.child("ImageUri")?.setValue(imageLink)

                                                         }
                                                     }
                                             }

                                             ImageUri = Uri.EMPTY //clear image data after upload a picture

                                             foodItemAdapter.notifyDataSetChanged()
                                         }

                                         dismiss()
                                         Toast.makeText(this@VendorMenuActivity,"Food Item Information is Updated",Toast.LENGTH_SHORT).show()

                }
            }
        }
                            .show()

    }

    override fun deleteInfo(position : Int){
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")

        val user = auth.currentUser
        val userReference = databaseReference?.child(user?.uid!!)?.child("foodItem")


        val list1 = arrayListOf<DataSnapshot>()
            userReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        list1.add(snapshot)
                        if(position == list1.size-1) //exit loop when get all the position value in edit item page
                        {
                            break
                        }
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //TODO("Not yet implemented")
                }
            })

        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setIcon(R.drawable.ic_warning)
            .setMessage("Are you sure to delete this Item?")
            .setPositiveButton("Yes"){
                    dialog,_->



                if(position!=-1)
                {
                    foodItemList.clear() //to clear last deleted item bug
                    val keyClicked = list1[position].key!!
                    userReference?.child(keyClicked)?.removeValue()
                    recv.adapter?.notifyDataSetChanged()

                }

                //foodItemAdapter.notifyDataSetChanged()
                Toast.makeText(this,"The Item has been deleted",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No"){
                    dialog,_->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun selectUserImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//            https://firebasestorage.googleapis.com/v0/b/fyp-canteensystem.appspot.com/o/foodItemImages%2F2022_12_04_09_22_38.png?alt=media&token=1dabbc34-c64a-452e-9ad9-6c942889c87e

        val v = LayoutInflater.from(this).inflate(R.layout.add_item,null)
        val imgView = v.findViewById<ImageView>(R.id.itemPic)

        if(requestCode == 100 && resultCode == RESULT_OK)
        {
            ImageUri = data?.data!!
            imgView.setImageURI(ImageUri)

//            var uriString = ImageUri.toString()
//            Picasso.get().load(ImageUri).resize(150,0).centerCrop().into(imgView)
//            data?.data?.let {
//                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
//                imgView.setImageBitmap(bitmap)
//            }

        }

    }

}