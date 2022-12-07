package com.example.fypcanteensystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class CustomerOrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_order)

        this.setTitle("My Order")
    }
}