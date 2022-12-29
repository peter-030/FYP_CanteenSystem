package com.example.fypcanteensystem

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fypcanteensystem.databinding.ActivityVendorReportBinding

class VendorReportActivity : AppCompatActivity() {
    private lateinit var binding : ActivityVendorReportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setTitle("Report")

        binding.tvSalesReport.setOnClickListener(){
            startActivity(Intent(this@VendorReportActivity,VendorSalesReportActivity::class.java))
            //finish()
        }
        binding.tvStockReport.setOnClickListener(){
            startActivity(Intent(this@VendorReportActivity,VendorStockReportActivity::class.java))
            //finish()
        }

        val actionbar = supportActionBar
        //actionbar!!.title = "My Cart"
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}