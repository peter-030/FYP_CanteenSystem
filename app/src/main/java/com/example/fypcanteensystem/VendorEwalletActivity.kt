package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.databinding.ActivityVendorEwalletBinding
import com.example.fypcanteensystem.model.*
import com.example.fypcanteensystem.view.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VendorEwalletActivity : AppCompatActivity(),EwalletAdapter.onItemClickListener {

    private lateinit var binding : ActivityVendorEwalletBinding
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv : RecyclerView
    private lateinit var eWalletList: ArrayList<EwalletData>
    private lateinit var eWalletAdapter: EwalletAdapter
    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference?=null
    private var database : FirebaseDatabase?=null

    private lateinit var calendar: Calendar
    private lateinit var simpleDateFormat : SimpleDateFormat

    private lateinit var studIdList: ArrayList<String>
    private lateinit var getCurrentBalance: String
    private lateinit var getStudId: String
    private lateinit var getStudBalance: String

    private lateinit var eWalletHistoryList: ArrayList<EwalletHistoryData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorEwalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("E-wallet Recharge")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("vendorProfile")

        /**set List*/
        eWalletList = ArrayList()
        /**set find Id*/
        addsBtn = binding.addingBtn
        recv = binding.walletRecycler
        /**set Adapter*/
        eWalletAdapter = EwalletAdapter(this,eWalletList,this)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = eWalletAdapter

        val cReference = database?.reference!!.child("customerProfile")

        studIdList = arrayListOf<String>()
        cReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studIdList.clear()
                for(userSnapshot in snapshot.children)
                {
                    studIdList.add(userSnapshot.child("Student Id").value.toString())



                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        //to check vendor e-wallet balance purpose
        val user = auth.currentUser
        val bReference = databaseReference?.child(user?.uid!!)?.child("E-wallet Balance")
        bReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                getCurrentBalance = snapshot.value.toString()

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        eWalletHistoryList = arrayListOf()

        loadAllItem()

        /**set Dialog*/
        addsBtn.setOnClickListener {


            addInfo()

        }

        binding.btnEWalletHistory.setOnClickListener(){
            showEwalletHistory()
        }

        val actionbar = supportActionBar
        //actionbar!!.title = "My Cart"
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showEwalletHistory(){
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.dialog_ewallet_history,null)
        val addDialog = AlertDialog.Builder(this)
        val user = auth.uid

        val cuReference = database?.reference!!.child("customerProfile")
        val historyCount = v.findViewById<TextView>(R.id.txt_eWalletHistory_count)

        addDialog.setView(v)

            .setNegativeButton("Close"){
                    dialog,_->
                dialog.dismiss()
            }
            .show()

        var recyclerView = v.findViewById<RecyclerView>(R.id.eWalletHistoryRecycler)
        var adapter = EwalletHistoryAdapter(this,eWalletHistoryList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cuReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load order details
                if(snapshot.exists()){
                    eWalletHistoryList.clear() //clear previous data before load a new one
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
                            if((snap.child("orderVendorId").value.toString() == user!!.toString() && snap.child("orderStatus").getValue(String::class.java) == "Completed"
                                && snap.child("orderPaymentMethod").getValue(String::class.java) == "PayByE-Wallet") || snap.child("orderVendorId").value.toString() == user!!.toString()
                                && snap.child("orderStatus").getValue(String::class.java) == "Cancelled" && snap.child("orderPaymentMethod").getValue(String::class.java) == "PayByE-Wallet"  ){
                                val HistoryData = EwalletHistoryData(orderID,orderDateTime,name,orderStatus,noOfItems,null,orderPaymentMethod, null,orderTotalPrice)
                                eWalletHistoryList.add(HistoryData)
                                //break
                            }
                        }


                    }
                    historyCount.setText(StringBuilder("Transaction History (").append(eWalletHistoryList.size).append(")"))
                    recyclerView.adapter = EwalletHistoryAdapter(this@VendorEwalletActivity,eWalletHistoryList)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

    }

    private fun loadAllItem() {
        val user = auth.currentUser
        val userReference = databaseReference?.child(user?.uid!!)?.child("E-wallet")
        val balanceReference = databaseReference?.child(user?.uid!!)

        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load stud Id, amount recharge
                if(snapshot.exists()){
                    eWalletList.clear() //clear previous data before load a new one
                    for(userSnapshot in snapshot.children)
                    {
                        val item = userSnapshot.getValue(EwalletData::class.java)
                        eWalletList.add(item!!)
                    }
                    binding.txtRechargeCount.setText(StringBuilder("Record (").append(eWalletList.size).append(")"))
                    recv.adapter = EwalletAdapter(this@VendorEwalletActivity,eWalletList,this@VendorEwalletActivity)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        balanceReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //load stud Id, amount recharge
                if(snapshot.exists()){
                    binding.txtBalance.setText("Balance: RM" + snapshot.child("E-wallet Balance").getValue().toString())
                }

            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }

    private fun addInfo(){
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.recharge_ewallet,null)

        val studIDContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.studIdCon)
        val rechargeAmountContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.rechargeAmountCon)
        val checkPasswordContainer = v.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.checkPasswordContainer)

        val studID = v.findViewById<EditText>(R.id.studId)
        val rechargeAmount = v.findViewById<EditText>(R.id.rechargeAmount)
        val checkPassword = v.findViewById<EditText>(R.id.checkPassword)

        //setup datetime
        var date : String
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy, HH:mm")
        date = simpleDateFormat.format(calendar.time)

        //firebase things
        val key = database!!.getReference("vendorProfile").push().key //to get random id for each food item
        val currentUser = auth.currentUser
        val currentUserDb = databaseReference?.child((currentUser?.uid!!))?.child("E-wallet")?.child(key!!)
        val findBalanceDb = databaseReference?.child((currentUser?.uid!!))?.child("E-wallet Balance")

        val customerReference = database?.reference!!.child("customerProfile")

        val addDialog = AlertDialog.Builder(this)

        addDialog.setView(v)
            .setPositiveButton("Submit"){
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
                        val studId = studID.text.toString()
                        val amount = rechargeAmount.text.toString()
                        val password = checkPassword.text.toString()


                        //validate student Id
                        if(TextUtils.isEmpty(studID.text.toString())){
                            studIDContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if (studID.text.toString() in studIdList) {
                            studIDContainer.error = null
                        }
                        else{
                            studIDContainer.setError("Inserted Student ID doesn't exist in the system")
                            return@setOnClickListener
                        }

                        //validate recharge amount
                        if(TextUtils.isEmpty(rechargeAmount.text.toString())){
                            rechargeAmountContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else if(!rechargeAmount.text.toString().matches("^\\d{1,8}(\\.\\d{1,2})?\$".toRegex())){
                            rechargeAmountContainer.setError("*Invalid format!")
                            return@setOnClickListener
                        }
                        else if(rechargeAmount.text.toString().toDouble() > getCurrentBalance.toDouble()){
                            rechargeAmountContainer.setError("*Exceeded your current balance!")
                            return@setOnClickListener
                        }
                        else{
                            rechargeAmountContainer.error = null
                        }

                        //validate password
                        if(TextUtils.isEmpty(checkPassword.text.toString())){
                            checkPasswordContainer.setError("*Required!")
                            return@setOnClickListener
                        }
                        else{
                            checkPasswordContainer.error = null
                        }

                        //below is to check password and add e-wallet things to firebase
                        if(currentUser != null){
                            val credential = EmailAuthProvider
                                .getCredential(currentUser.email!!, checkPassword.text.toString())

                            currentUser?.reauthenticate(credential)
                                ?.addOnCompleteListener {
                                    if(it.isSuccessful){
                                        //Toast.makeText(this@VendorEwalletActivity, "Authentication Success", Toast.LENGTH_SHORT).show()
                                        //set addListenerForSingle to avoid onDataChange loop!
                                        customerReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {

                                                //load order details
                                                if(snapshot.exists()){

                                                    for(userSnapshot in snapshot.children)
                                                    {

                                                        if(userSnapshot.child("Student Id").getValue(String::class.java) == studId)
                                                        {
                                                            val getKey = userSnapshot.key
                                                            val getVendorID = databaseReference?.child((currentUser?.uid!!))?.key
                                                            getStudId = userSnapshot.key!!
                                                            getStudBalance = userSnapshot.child("E-wallet Balance").getValue(String::class.java)!!

                                                            customerReference?.child(getKey!!).child("E-wallet").child(key!!)
                                                                .setValue(EwalletData(null,amount,null,date,getVendorID))


                                                            val studPhoneNo = userSnapshot.child("Phone Number").getValue(String::class.java)
                                                            currentUserDb?.setValue(EwalletData(studId,amount,studPhoneNo,date,null))
                                                            recv.adapter?.notifyDataSetChanged()
                                                            break
                                                        }

                                                    }


                                                    //minus vendor balance here after recharge for students and add up student balance
                                                    val getAmount = amount.toDouble()
                                                    val finalBalance = getCurrentBalance.toDouble().minus(getAmount).toString()
                                                    findBalanceDb?.setValue(finalBalance)

                                                    val studFinalBalance = getStudBalance.toDouble() + amount.toDouble()

                                                    customerReference?.child(getStudId).child("E-wallet Balance")
                                                        .setValue(studFinalBalance.toString())
                                                }

                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                //TODO("Not yet implemented")
                                            }
                                        })
                                        dismiss()
                                        Toast.makeText(this@VendorEwalletActivity, "E-wallet Recharge Successful", Toast.LENGTH_SHORT).show()

                                    }
                                    else{
                                        //if password incorrect then dialog will not dismiss and will not quit dialog until enter correct password
                                        Toast.makeText(this@VendorEwalletActivity, "Authentication Failed", Toast.LENGTH_SHORT).show()
                                        checkPasswordContainer.setError("*Incorrect Password!")
                                    }
                                }

                        }

                    }
                }
            }

            .show()
    }
}