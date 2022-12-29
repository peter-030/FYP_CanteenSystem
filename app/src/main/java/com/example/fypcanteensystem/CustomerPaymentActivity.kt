package com.example.fypcanteensystem

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.split
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fypcanteensystem.adapter.CartFoodListAdapter
import com.example.fypcanteensystem.adapter.PaymentFoodListAdapter
import com.example.fypcanteensystem.dataModels.CartFoodListData
import com.example.fypcanteensystem.dataModels.PromoCodeData
import com.example.fypcanteensystem.dataModels.PromoCodeValidateListData
import com.example.fypcanteensystem.databinding.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Double.valueOf
import java.sql.Date.valueOf
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CustomerPaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerPaymentBinding
    private lateinit var bindingPromoCode: EnterPromoCodePopupBinding
    private lateinit var bindingEWalletValidation: EwalletValidationPopupBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseCustReference: DatabaseReference
    private lateinit var databaseVendReference: DatabaseReference
    private lateinit var paymentFoodReference: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private lateinit var addOrderReference: DatabaseReference
    private lateinit var cartItemReference: DatabaseReference
    private lateinit var promoCodeReference: DatabaseReference
    private lateinit var vendorReference: DatabaseReference
    private lateinit var vendEWReference: DatabaseReference
    private lateinit var custEWReference: DatabaseReference

    private lateinit var paymentFoodListener: ValueEventListener
    private lateinit var addOrderListener: ValueEventListener
    private lateinit var cartItemListener: ValueEventListener
    private lateinit var promoCodeListener: ValueEventListener
    private lateinit var vendorListener: ValueEventListener
    private lateinit var vendEWListener: ValueEventListener
    private lateinit var custEWListener: ValueEventListener

    private lateinit var database: FirebaseDatabase
    private lateinit var paymentFoodListArray: ArrayList<CartFoodListData>
    private lateinit var promoCodeArray: ArrayList<PromoCodeData>
    private lateinit var promoValidateArray: ArrayList<PromoCodeValidateListData>

    private var context = this
    private var vendorId: String? = null
    private var paymentTotalPrice: Double = 0.0
    private var discountAmount: Double = 0.0
    private var totalPriceAfterPromo: Double = 0.0
    private var totalQty: Int = 0
    private var merchantName: String? = null
    private var paymentMethod: String? = null
    private var promoCodeRedeemStatus: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseCustReference = database?.reference!!.child("customerProfile")

        val actionbar = supportActionBar
        actionbar!!.title = "Payment"
        actionbar.setDisplayHomeAsUpEnabled(true)

        binding.rvPaymentOrderItems.layoutManager = LinearLayoutManager(this)
        binding.rvPaymentOrderItems.setHasFixedSize(true)
        loadPaymentFoodData()

        paymentMethod = null
        binding.rgPaymentMethod.setOnCheckedChangeListener { group, checkId ->
            if (checkId == R.id.rbEWallet)
                paymentMethod = "E-wallet"
            if (checkId == R.id.rbPayAtCounter)
                paymentMethod = "PayAtCounter"

        }

        promoValidateArray = arrayListOf()
        promoCodeArray = arrayListOf()


        binding.btnEnterPromoCode.setOnClickListener() {
            promoCodeReference = databaseVendReference?.child(vendorId!!)?.child("promoCode")
            promoCodeReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    promoValidateArray = arrayListOf()
                    if (snapshot.exists()) {
                        for (promoCodeListSnapshot in snapshot.children) {
                            val promoCodeId = promoCodeListSnapshot.key
                            val promoCodeName =
                                promoCodeListSnapshot.child("codeName")
                                    .getValue(String::class.java)
                            val promoCodeStatus = promoCodeListSnapshot.child("codeStatus")
                                .getValue(String::class.java)
                            val promoCodeStartDate =
                                promoCodeListSnapshot.child("codeStartDate")
                                    .getValue(String::class.java)
                            val promoCodeEndDate = promoCodeListSnapshot.child("codeEndDate")
                                .getValue(String::class.java)
                            val promoCodeQuantity = promoCodeListSnapshot.child("codeQuantity")
                                .getValue(String::class.java)
                            val promoCodeMinSpend = promoCodeListSnapshot.child("codeMinSpend")
                                .getValue(String::class.java)
                            val promoCodeDiscountPrice =
                                promoCodeListSnapshot.child("codeDiscountPrice")
                                    .getValue(String::class.java)
                            promoValidateArray.add(
                                PromoCodeValidateListData(
                                    promoCodeId,
                                    promoCodeName,
                                    promoCodeStatus,
                                    promoCodeStartDate,
                                    promoCodeEndDate,
                                    promoCodeQuantity,
                                    promoCodeMinSpend,
                                    promoCodeDiscountPrice
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }
            })
            enterPromoCode()
        }

        binding.btnMakeOrder.setOnClickListener() {
            if (binding.rgPaymentMethod.checkedRadioButtonId == -1) {
                AlertDialog.Builder(this)
                    .setMessage("Please select ONE payment method")
                    .create()
                    .show()
            } else {

                if (binding.rbEWallet.isChecked) {
                    paymentMethod = "PayByE-Wallet"
                    checkEWalletPassword()

                } else if (binding.rbPayAtCounter.isChecked) {
                    paymentMethod = "PayAtCounter"
                    sendOrderToVendor()
                }

            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun loadPaymentFoodData() {

        vendorId = null
        paymentTotalPrice = 0.0
        discountAmount = 0.0
        totalPriceAfterPromo = 0.0
        totalQty = 0
        merchantName = null

        var subPrice: Double
        var totalPrice: Double = 0.0


        val userId = auth.currentUser?.uid!!
        paymentFoodReference = databaseCustReference?.child(userId)?.child("cartItem")
        paymentFoodListener =
            paymentFoodReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        paymentFoodListArray = arrayListOf()
                        for (foodCartListSnapshot in snapshot.children) {
                            val cartId = foodCartListSnapshot.key
                            val cartFoodVendorId = foodCartListSnapshot.child("cartVendorId")
                                .getValue(String::class.java)
                            val cartFoodId = foodCartListSnapshot.child("cartFoodId")
                                .getValue(String::class.java)
                            val cartFoodImage =
                                foodCartListSnapshot.child("ImageUri").getValue(String::class.java)
                            val cartFoodName = foodCartListSnapshot.child("cartFoodName")
                                .getValue(String::class.java)
                            val cartFoodPrice = foodCartListSnapshot.child("cartFoodPrice")
                                .getValue(String::class.java)
                            val cartFoodQty = foodCartListSnapshot.child("cartFoodQty")
                                .getValue(String::class.java)

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

                            vendorId = cartFoodVendorId

                            paymentFoodListArray.add(cartFoodList!!)
                        }
                        paymentTotalPrice = totalPrice
                        totalPriceAfterPromo = totalPrice

                        binding.tvPaymentOrderItems.setText("Subtotal ($totalQty items):")
                        binding.tvPaymentSubtotal.setText("RM ${totalPrice.format(2)}")

                        binding.tvOrderSummarySubtotal.setText("Order Subtotal ($totalQty items)")
                        binding.tvOrderSummarySubtotalCost.setText("RM ${totalPrice.format(2)}")

                        binding.tvOrderSummaryTotalCost.setText("RM ${totalPriceAfterPromo.format(2)}")

                        binding.tvPaymentSmallTotalCost.setText("RM ${totalPriceAfterPromo.format(2)}")

                        binding.rvPaymentOrderItems.adapter =
                            PaymentFoodListAdapter(paymentFoodListArray, context)
                    }

                    if (vendorId != null) {
                        databaseVendReference = database?.reference!!.child("vendorProfile")
                        vendorReference = databaseVendReference?.child(vendorId.toString())
                        vendorReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {

                                        merchantName =
                                            snapshot.child("Merchant Name")
                                                .getValue(String::class.java)
                                        binding.tvPaymentVendorName.setText(merchantName)

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    //TODO("Not yet implemented")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }
            })


    }


    private fun enterPromoCode() {
        bindingPromoCode = EnterPromoCodePopupBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(bindingPromoCode.root)
        val dialog = dialogBuilder.create()
        dialog.show()

        bindingPromoCode.etPromoCode.text = null
        promoCodeRedeemStatus = null

        bindingPromoCode.btnEnterPromoCodeConfirm.setOnClickListener() {
            checkPromoCodeValidate(bindingPromoCode.etPromoCode.text.toString())
            dialog.cancel()
        }
        bindingPromoCode.btnEnterPromoCodeCancel.setOnClickListener() {
            dialog.cancel()
        }
    }


    private fun checkPromoCodeValidate(promoCode: String) {
        //val formatter = SimpleDateFormat("yyyy, MM, dd")
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val date = Date()
        val cDate = formatter.format(date)


        discountAmount = 0.0
        promoCodeArray.clear()

        if (vendorId != null) {
            if (paymentTotalPrice != null && paymentTotalPrice > 0) {

                for (pCode in promoValidateArray) {

                    val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDate.parse(cDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    } else {
                        TODO("VERSION.SDK_INT < O")
                    }
                    val startDate =
                        LocalDate.parse(
                            pCode.promoCodeStartDate,
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        )
                    val endDate =
                        LocalDate.parse(
                            pCode.promoCodeEndDate,
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        )

                    if (promoCode == pCode.promoCodeName) {
                        if (pCode.promoCodeStatus.equals("shelf") && currentDate.isAfter(startDate)
                            && currentDate.isBefore(endDate) && pCode.promoCodeQuantity?.toInt()!! > 0
                        ) {
                            if (paymentTotalPrice > pCode.promoCodeMinSpend!!.toDouble()) {
                                discountAmount = pCode.promoCodeDiscountPrice!!.toDouble()
                                totalPriceAfterPromo = paymentTotalPrice - discountAmount!!

                                /*
                                binding.btnEnterPromoCode.setText(
                                    "Discount: RM ${
                                        discountAmount!!.format(
                                            2
                                        )
                                    }"
                                )

                                 */
                                binding.tvOrderSummaryPromoCodeCost.setText(
                                    "(RM ${
                                        discountAmount!!.format(
                                            2
                                        )
                                    })"
                                )
                                binding.tvOrderSummaryTotalCost.setText(
                                    "RM ${
                                        totalPriceAfterPromo.format(
                                            2
                                        )
                                    }"
                                )
                                binding.tvPaymentSmallTotalCost.setText(
                                    "RM ${
                                        totalPriceAfterPromo.format(
                                            2
                                        )
                                    }"
                                )
                                //promoCodeRedeemStatus = "Redeem Successfully"

                                //promoCodeReference?.child(promoCodeId!!).child("codeQuantity").setValue((promoCodeQuantity.toInt() - 1).toString())
                                promoCodeArray.add(
                                    PromoCodeData(
                                        pCode.promoCodeId,
                                        pCode.promoCodeQuantity
                                    )
                                )
                                Toast.makeText(this,"Promo Code Redeem Successfully",Toast.LENGTH_SHORT).show()

                                break
                            } else {
                                //promoCodeRedeemStatus == "Minimum spent is RM ${promoCodeMinSpend.format(2)}"
                                /*
                                    binding.btnEnterPromoCode.setText(
                                    "Minimum spent is RM ${pCode.promoCodeMinSpend!!.format(2)}."
                                )
                                 */
                                Toast.makeText(this,"Minimum spent is RM ${pCode.promoCodeMinSpend!!.format(2)}.\nPlease Try Again",Toast.LENGTH_SHORT).show()

                                break
                            }
                        } else {
                            //promoCodeRedeemStatus == "Promo Code is not available"
                            //binding.btnEnterPromoCode.setText("Promo Code is not available.")
                            Toast.makeText(this,"Promo Code is not available.\nPlease Try Again",Toast.LENGTH_SHORT).show()
                            break
                        }
                    } else {
                        //promoCodeRedeemStatus == "Invalid Promo Code"
                        //binding.btnEnterPromoCode.setText("Invalid Promo Code.")
                        Toast.makeText(this,"Invalid Promo Code.\nPlease Try Again",Toast.LENGTH_SHORT).show()
                    }


                }
            }
        }
    }

    private fun sendOrderToVendor() {
        //if (binding.rgPaymentMethod.checkedRadioButtonId == 0) {


        var userId = auth.currentUser?.uid!!
/*
            if(paymentMethod == "E-wallet"){
                custEWReference = databaseCustReference?.child(userId)
                custEWListener = custEWReference?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshotC: DataSnapshot) {
                        if (snapshotC.exists()) {
                            var custEW = snapshotC.child("E-wallet Balance")
                                .getValue(String::class.java)!!.toDouble()
                            custEW -= totalPriceAfterPromo
                            custEWReference.child("E-wallet Balance").setValue(custEW.toString())

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })

                vendEWReference = databaseVendReference?.child(vendorId!!)
                vendEWListener = vendEWReference?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshotV: DataSnapshot) {
                        if (snapshotV.exists()) {
                            var vendEW = snapshotV.child("E-wallet Balance")
                                .getValue(String::class.java)!!.toDouble()
                            vendEW += totalPriceAfterPromo
                            vendEWReference.child("E-wallet Balance").setValue(vendEW.toString())

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })
            }

 */

        val formatterDate = SimpleDateFormat("dd-MM-yyyy")
        val formatterTime = SimpleDateFormat("hh:mm:ss")
        val formatterDateTime = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")

        val date = Date()
        val currentDate = formatterDate.format(date)
        val currentTime = formatterTime.format(date)
        val currentDateTime = formatterDateTime.format(date)


        databaseCustReference = database?.reference!!.child("customerProfile")

        val key = database!!.getReference("customerProfile").child("orderItem").push().key
        var orderNote = binding.etOrderNote.text.toString()

        addOrderReference = databaseCustReference?.child(userId)?.child("orderItem")?.child(key!!)
        addOrderListener = addOrderReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addOrderReference?.child("orderVendorId")?.setValue(vendorId)
                    addOrderReference?.child("orderVendorName")?.setValue(merchantName)
                    addOrderReference?.child("orderStatus")?.setValue("Pending")

                    addOrderReference?.child("orderNote")?.setValue(orderNote)
                    //orderReference?.child("orderDate")?.setValue(currentDate)
                    //orderReference?.child("orderTime")?.setValue(currentTime)
                    addOrderReference?.child("orderDateTime")?.setValue(currentDateTime)
                    addOrderReference?.child("orderSubPrice")
                        ?.setValue(paymentTotalPrice.format(2))
                    addOrderReference?.child("orderDiscountPrice")
                        ?.setValue(discountAmount.format(2))
                    addOrderReference?.child("orderTotalPrice")
                        ?.setValue(totalPriceAfterPromo.format(2))
                    addOrderReference?.child("orderTotalQty")
                        ?.setValue(totalQty.toString())

                    addOrderReference?.child("orderRating")?.setValue("")
                    addOrderReference?.child("orderPaymentMethod")?.setValue(paymentMethod)

                    if (paymentMethod == "PayByE-Wallet") {

                        custEWReference = databaseCustReference?.child(userId)
                        custEWReference?.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshotC: DataSnapshot) {
                                if (snapshotC.exists()) {
                                    var custEW = snapshotC.child("E-wallet Balance")
                                        .getValue(String::class.java)!!.toDouble()
                                    //custEW -= totalPriceAfterPromo
                                    var custEWBalance = custEW - totalPriceAfterPromo
                                    custEWReference.child("E-wallet Balance")
                                        .setValue(custEWBalance.format(2).toString())

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                //TODO("Not yet implemented")
                            }
                        })

                        vendEWReference = databaseVendReference?.child(vendorId!!)
                        vendEWReference?.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshotV: DataSnapshot) {
                                if (snapshotV.exists()) {
                                    var vendEW = snapshotV.child("E-wallet Balance")
                                        .getValue(String::class.java)!!.toDouble()
                                    //vendEW += totalPriceAfterPromo
                                    var vendEWBalance = vendEW + totalPriceAfterPromo
                                    vendEWReference.child("E-wallet Balance")
                                        .setValue(vendEWBalance.format(2).toString())

                                    if (promoCodeArray.size > 0) {
                                        vendEWReference.child("promoCode")
                                            .child(promoCodeArray[0].promoCodeId!!)
                                            .child("codeQuantity")
                                            .setValue((promoCodeArray[0].promoCodeQty!!.toInt() - 1).toString())
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                //TODO("Not yet implemented")
                            }
                        })
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        cartItemReference = databaseCustReference?.child(userId)?.child("cartItem")
        cartItemReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot2: DataSnapshot) {
                if (snapshot2.exists()) {
                    for (foodCartListSnapshot in snapshot2.children) {

                        val keyOrder =
                            database!!.getReference("customerProfile").child("orderItem")
                                .push().key
                        //val orderItemReference = databaseCustReference?.child(userId)?.child("orderItem")?.child("orderFoodItem")?.child(keyOrder!!)

                        val cartFoodId = foodCartListSnapshot.child("cartFoodId")
                            .getValue(String::class.java)
                        val cartFoodName = foodCartListSnapshot.child("cartFoodName")
                            .getValue(String::class.java)
                        val cartFoodPrice = foodCartListSnapshot.child("cartFoodPrice")
                            .getValue(String::class.java)
                        val cartFoodQty = foodCartListSnapshot.child("cartFoodQty")
                            .getValue(String::class.java)
                        val cartFoodImage =
                            foodCartListSnapshot.child("ImageUri").getValue(String::class.java)

                        addOrderReference?.child("orderFoodItem")?.child(keyOrder!!)
                            ?.child("orderFoodId")?.setValue(cartFoodId)
                        addOrderReference?.child("orderFoodItem")?.child(keyOrder!!)
                            ?.child("orderFoodName")?.setValue(cartFoodName)
                        addOrderReference?.child("orderFoodItem")?.child(keyOrder!!)
                            ?.child("orderFoodPrice")?.setValue(cartFoodPrice)
                        addOrderReference?.child("orderFoodItem")?.child(keyOrder!!)
                            ?.child("orderFoodQty")?.setValue(cartFoodQty)
                        addOrderReference?.child("orderFoodItem")?.child(keyOrder!!)
                            ?.child("ImageUri")?.setValue(cartFoodImage)

                    }
                    //remove cart
                    databaseCustReference?.child(userId)?.child("cartItem")?.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })

        Toast.makeText(this, "Order Successfully", Toast.LENGTH_LONG).show()
        val intent = Intent(this, CustomerOrderActivity::class.java)
        finish()
        startActivity(intent)

    }

    private fun checkEWalletPassword() {
        //below is to check password and add e-wallet things to firebase
        val dialogBuilder = AlertDialog.Builder(this)
        bindingEWalletValidation = EwalletValidationPopupBinding.inflate(layoutInflater)
        dialogBuilder.setView(bindingEWalletValidation.root)
        val dialog = dialogBuilder.create()
        dialog.show()
        bindingEWalletValidation.btnEWalletValidateSubmit.setOnClickListener() {

            var currentUser = auth.currentUser
            val credential = EmailAuthProvider.getCredential(
                currentUser?.email!!,
                bindingEWalletValidation.etEWalletPassword.text.toString()
            )

            currentUser?.reauthenticate(credential)
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        sendOrderToVendor()
                        Toast.makeText(this, "Pay Successful", Toast.LENGTH_LONG).show()

                    }
                    else
                        Toast.makeText(this, "Incorrect Password", Toast.LENGTH_LONG).show()
                }

        }

        bindingEWalletValidation.btnEWalletValidateCancel.setOnClickListener() {
            Toast.makeText(this,"E-wallet Cancelled",Toast.LENGTH_SHORT).show()
            dialog.cancel()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        paymentFoodListener?.let { paymentFoodReference!!.removeEventListener(it) }
        //addOrderListener?.let { addOrderReference!!.removeEventListener(it)}
        //cartItemListener?.let { cartItemReference!!.removeEventListener(it)}
        //promoCodeListener?.let { promoCodeReference!!.removeEventListener(it)}
        //vendorListener?.let { vendorReference!!.removeEventListener(it) }
        //custEWListener?.let { custEWReference!!.removeEventListener(it)}
        //vendEWListener?.let { vendEWReference!!.removeEventListener(it)}

    }

}
