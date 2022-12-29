package com.example.fypcanteensystem

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.databinding.ActivityVendorPromoCodeBinding
import com.example.fypcanteensystem.model.FoodItemData
import com.example.fypcanteensystem.model.PromoCodeData
import com.example.fypcanteensystem.view.FoodItemAdapter
import com.example.fypcanteensystem.view.PromoCodeAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VendorPromoCodeActivity : AppCompatActivity(),PromoCodeAdapter.onItemClickListener {
    private lateinit var binding : ActivityVendorPromoCodeBinding
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv : RecyclerView
    private lateinit var promoCodeList: ArrayList<PromoCodeData>
    private lateinit var promoCodeAdapter: PromoCodeAdapter
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference? =null
    private var database : FirebaseDatabase? =null

    private lateinit var codeList: ArrayList<String>

    var formatDate = SimpleDateFormat("dd-MM-yyyy", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorPromoCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Promo Code List")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")

        /**set List*/
        promoCodeList = ArrayList()
        /**set find Id*/
        addsBtn = binding.addingBtn
        recv = binding.mRecycler
        /**set Adapter*/
        promoCodeAdapter = PromoCodeAdapter(this,promoCodeList,this)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = promoCodeAdapter

        //for check promocode exist purpose
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.add_promocode,null)
        val codeName = v.findViewById<EditText>(R.id.codeName)
        val codeNameContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeNameCon)
        val currentUser = auth.currentUser
        val userReference = databaseReference?.child((currentUser?.uid!!))?.child("promoCode")

        codeList = arrayListOf<String>()
        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                codeList.clear()
                for(userSnapshot in snapshot.children)
                {
                    codeList.add(userSnapshot.child("codeName").value.toString())



                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

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

    private fun loadAllItem(){
        val user = auth.currentUser
        val userReference = databaseReference?.child(user?.uid!!)?.child("promoCode")


        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load food item name, price
                if(snapshot.exists()){
                    promoCodeList.clear() //clear previous data before load a new one
                    for(userSnapshot in snapshot.children)
                    {
                        val item = userSnapshot.getValue(PromoCodeData::class.java)
                        promoCodeList.add(item!!)
                    }
                    recv.adapter = PromoCodeAdapter(this@VendorPromoCodeActivity,promoCodeList,this@VendorPromoCodeActivity)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }


    private fun addInfo(){
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.add_promocode,null)
        /**set view*/
        val codeName = v.findViewById<EditText>(R.id.codeName)
        val codeNameContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeNameCon)
        val codeDiscountPriceContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeDiscountPriceCon)
        val codeMinSpendContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeMinSpendCon)
        val codeQuantityContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeQuantityCon)

        val codeDiscountPrice = v.findViewById<EditText>(R.id.codeDiscountPrice)
        val codeMinSpend = v.findViewById<EditText>(R.id.codeMinSpend)
        val codeQuantity = v.findViewById<EditText>(R.id.codeQuantity)
        val codeStatusShelf = v.findViewById<RadioButton>(R.id.codeShelf)
        val codeStatusUnshelve = v.findViewById<RadioButton>(R.id.codeUnshelve)
        val btnStartTime = v.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = v.findViewById<Button>(R.id.btnEndTime)
        val dateHeader = v.findViewById<TextView>(R.id.dateHeader)

        btnStartTime.setOnClickListener(View.OnClickListener {
            val getDate = Calendar.getInstance()
            val datepicker = DatePickerDialog(this,android.R.style.Theme_Holo_Dialog_MinWidth, DatePickerDialog.OnDateSetListener
            { datePicker, i, i2, i3 ->

                val selectDate = Calendar.getInstance()
                selectDate.set(Calendar.YEAR,i)
                selectDate.set(Calendar.MONTH,i2)
                selectDate.set(Calendar.DAY_OF_MONTH,i3)
                val date = formatDate.format(selectDate.time)
                btnStartTime.text = date

            }, getDate.get(Calendar.YEAR),getDate.get(Calendar.MONTH),getDate.get(Calendar.DAY_OF_MONTH))

//            if(datepicker!=null){
//                datepicker.updateDate(1999,2,2)
//            }

            datepicker.show()
        })

        btnEndTime.setOnClickListener(View.OnClickListener {
            val getDate = Calendar.getInstance()
            val datepicker = DatePickerDialog(this,android.R.style.Theme_Holo_Dialog_MinWidth, DatePickerDialog.OnDateSetListener
            { datePicker, i, i2, i3 ->

                val selectDate = Calendar.getInstance()
                selectDate.set(Calendar.YEAR,i)
                selectDate.set(Calendar.MONTH,i2)
                selectDate.set(Calendar.DAY_OF_MONTH,i3)
                val date = formatDate.format(selectDate.time)
                btnEndTime.text = date

            }, getDate.get(Calendar.YEAR),getDate.get(Calendar.MONTH),getDate.get(Calendar.DAY_OF_MONTH))
            datepicker.show()
        })



        //firebase things
        val key = database!!.getReference("vendorProfile").push().key //to get random id for each food item
        val currentUser = auth.currentUser
        val currentUserDb = databaseReference?.child((currentUser?.uid!!))?.child("promoCode")?.child(key!!)

        val addDialog = AlertDialog.Builder(this)

        addDialog.setView(v)
            .setPositiveButton("Ok"){
                    dialog,_->


            }
            .setNegativeButton("Cancel"){
                    dialog,_->
                dialog.dismiss()
                Toast.makeText(this,"Cancel", Toast.LENGTH_SHORT).show()

            }
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                        //do validation here and after that update data in firebase
                        val name = codeName.text.toString()
                        val discountPrice = codeDiscountPrice.text.toString()
                        val minSpend = codeMinSpend.text.toString()
                        val quantity = codeQuantity.text.toString()
                        val statusShelf = codeStatusShelf.text.toString()
                        val statusUnshelve = codeStatusUnshelve.text.toString()
                        val startTime = btnStartTime.text.toString()
                        val endTime = btnEndTime.text.toString()



                        if(TextUtils.isEmpty(codeName.text.toString())){
                            codeNameContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if(codeName.text.toString().count() < 5 || codeName.text.toString().count() > 12){
                            codeNameContainer.setError("*Character length must be within 5 to 12")
                            return@setOnClickListener
                        }
                        else{
                            codeNameContainer.error = null
                        }
                        for(codeLists in codeList){
                            codeLists.toString()
                            if(codeName.text.toString() == codeLists ){
                                codeNameContainer.setError("*This PromoCode has been used")
                                return@setOnClickListener
                            }

                        }



                        if(TextUtils.isEmpty(codeDiscountPrice.text.toString())){
                            codeDiscountPriceContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if(!codeDiscountPrice.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                            codeDiscountPriceContainer.setError("*Invalid Price format!")
                            return@setOnClickListener
                        }
                        else{
                            codeDiscountPriceContainer.error = null
                        }

                        if(TextUtils.isEmpty(codeMinSpend.text.toString())){
                            codeMinSpendContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if(!codeMinSpend.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                            codeMinSpendContainer.setError("*Invalid Price format!")
                            return@setOnClickListener
                        }
                        else{
                            codeMinSpendContainer.error = null
                        }

                        if(TextUtils.isEmpty(codeQuantity.text.toString())){
                            codeQuantityContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else{
                            codeQuantityContainer.error = null
                        }

                        if(btnStartTime.text.toString() == "Start Time"){
                            dateHeader.setError("*Required!")
                            Toast.makeText(this@VendorPromoCodeActivity, "Please select a Start Date", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        else{
                            dateHeader.error = null
                        }

                        if(btnEndTime.text.toString() == "End Time"){
                            dateHeader.setError("*Required!")
                            Toast.makeText(this@VendorPromoCodeActivity, "Please select a End Date", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        else{
                            dateHeader.error = null
                        }

                        if(btnStartTime.text.toString() > btnEndTime.text.toString()){
                            dateHeader.setError("*Invalid!")
                            Toast.makeText(this@VendorPromoCodeActivity, "Invalid Date Selected", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        else{
                            dateHeader.error = null
                        }

                        if(codeDiscountPrice.text.toString().toDouble() > codeMinSpend.text.toString().toDouble()){
                            codeDiscountPrice.setError("*Invalid!")
                            Toast.makeText(this@VendorPromoCodeActivity, "Discount cannot greater than Min Spend", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        else{
                            codeDiscountPrice.error = null
                        }

                        //add to firebase
                        if(codeStatusShelf.isChecked)
                        {
                            currentUserDb?.setValue(PromoCodeData(name,discountPrice,minSpend,quantity
                                ,statusShelf,startTime,endTime,null))
                            promoCodeAdapter.notifyDataSetChanged()
                        }
                        else if(codeStatusUnshelve.isChecked)
                        {
                            currentUserDb?.setValue(PromoCodeData(name,discountPrice,minSpend,quantity
                                ,statusUnshelve,startTime,endTime,null))
                            promoCodeAdapter.notifyDataSetChanged()
                        }

                        dismiss()
                        Toast.makeText(this@VendorPromoCodeActivity, "Promo Code Register Successful", Toast.LENGTH_SHORT).show()

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
        val userReference = databaseReference?.child(user?.uid!!)?.child("promoCode")

        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.add_promocode,null)
        /**set view*/
        val codeName = v.findViewById<EditText>(R.id.codeName)
        val codeNameContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeNameCon)
        val codeDiscountPriceContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeDiscountPriceCon)
        val codeMinSpendContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeMinSpendCon)
        val codeQuantityContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.codeQuantityCon)

        val codeDiscountPrice = v.findViewById<EditText>(R.id.codeDiscountPrice)
        val codeMinSpend = v.findViewById<EditText>(R.id.codeMinSpend)
        val codeQuantity = v.findViewById<EditText>(R.id.codeQuantity)
        val codeStatusShelf = v.findViewById<RadioButton>(R.id.codeShelf)
        val codeStatusUnshelve = v.findViewById<RadioButton>(R.id.codeUnshelve)
        val btnStartTime = v.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = v.findViewById<Button>(R.id.btnEndTime)

        val list = arrayListOf<DataSnapshot>()
        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(userSnapshot in snapshot.children)
                {
                    list.add(userSnapshot)
                    codeName.setText(userSnapshot.child("codeName").value.toString())
                    codeDiscountPrice.setText(userSnapshot.child("codeDiscountPrice").value.toString())
                    codeMinSpend.setText(userSnapshot.child("codeMinSpend").value.toString())
                    codeQuantity.setText(userSnapshot.child("codeQuantity").value.toString())
                    btnStartTime.setText(userSnapshot.child("codeStartDate").value.toString())
                    btnEndTime.setText(userSnapshot.child("codeEndDate").value.toString())

                    if(position == list.size-1) //exit loop when get all the position value in edit item page
                    {
                        break
                    }
                }

                //set code name and quantity to uneditable
                codeName.inputType = InputType.TYPE_NULL
                codeName.apply { setTypeface(typeface, Typeface.BOLD) }
                codeQuantity.inputType = InputType.TYPE_NULL
                codeQuantity.apply { setTypeface(typeface, Typeface.BOLD) }

                //show radio button
                val keyClicked = list[position].key!!
                if(snapshot?.child(keyClicked)?.child("codeStatus").value.toString() == "shelf")
                {
                    codeStatusShelf!!.isChecked=true
                }
                if(snapshot?.child(keyClicked)?.child("codeStatus").value.toString() == "unshelve")
                {
                    codeStatusUnshelve!!.isChecked=true
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

                dialog.dismiss()

            }
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                        //do validation here and after that update data in firebase
                        if(TextUtils.isEmpty(codeDiscountPrice.text.toString())){
                            codeDiscountPriceContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if(!codeDiscountPrice.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                            codeDiscountPriceContainer.setError("*Invalid Price format!")
                            return@setOnClickListener
                        }
                        else{
                            codeDiscountPriceContainer.error = null
                        }

                        if(TextUtils.isEmpty(codeMinSpend.text.toString())){
                            codeMinSpendContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if(!codeMinSpend.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                            codeMinSpendContainer.setError("*Invalid Price format!")
                            return@setOnClickListener
                        }
                        else{
                            codeMinSpendContainer.error = null
                        }


                        if(codeDiscountPrice.text.toString().toDouble() > codeMinSpend.text.toString().toDouble()){
                            codeDiscountPrice.setError("*Invalid!")
                            Toast.makeText(this@VendorPromoCodeActivity, "Discount cannot greater than Min Spend", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        else{
                            codeDiscountPrice.error = null
                        }


                        if(position!=-1)
                        {
                            val keyClicked = list[position].key!!
                            //userReference?.child(keyClicked)?.child("codeName")?.setValue(codeName.text.toString())
                            userReference?.child(keyClicked)?.child("codeDiscountPrice")?.setValue(codeDiscountPrice.text.toString())
                            userReference?.child(keyClicked)?.child("codeMinSpend")?.setValue(codeMinSpend.text.toString())

                            if(codeStatusShelf.isChecked)
                            {
                                userReference?.child(keyClicked)?.child("codeStatus")?.setValue(codeStatusShelf.text.toString())
                            }
                            if(codeStatusUnshelve.isChecked)
                            {
                                userReference?.child(keyClicked)?.child("codeStatus")?.setValue(codeStatusUnshelve.text.toString())
                            }


                            promoCodeAdapter.notifyDataSetChanged()
                        }

                        dismiss()
                        Toast.makeText(this@VendorPromoCodeActivity,"Promo Code Information is Updated",Toast.LENGTH_SHORT).show()

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
        val userReference = databaseReference?.child(user?.uid!!)?.child("promoCode")

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
                    promoCodeList.clear() //to clear last deleted item bug
                    val keyClicked = list1[position].key!!
                    userReference?.child(keyClicked)?.removeValue()
                    recv.adapter?.notifyDataSetChanged()

                }

                //foodItemAdapter.notifyDataSetChanged()
                Toast.makeText(this,"The Promo Code has been deleted",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No"){
                    dialog,_->
                dialog.dismiss()
            }
            .create()
            .show()
    }

}