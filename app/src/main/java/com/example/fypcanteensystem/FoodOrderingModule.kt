package com.example.fypcanteensystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.fypcanteensystem.databinding.ActivityMainBinding

class FoodOrderingModule : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_ordering_module)

        val iBtnProfile = findViewById<ImageButton>(R.id.iBtnProfile)
        iBtnProfile.setOnClickListener {
            val intent = Intent(this, CustomerProfile::class.java)
            startActivity(intent)
        }

        val iBtnCart = findViewById<ImageButton>(R.id.iBtnCart)
        iBtnCart.setOnClickListener {
            val intent = Intent(this, CustomerCart::class.java)
            startActivity(intent)
        }

        val iBtnMyOrder = findViewById<ImageButton>(R.id.iBtnMyOrder)
        iBtnMyOrder.setOnClickListener {
            val intent = Intent(this, CustomerOrder::class.java)
            startActivity(intent)
        }

        val iBtnWishlist = findViewById<ImageButton>(R.id.iBtnWishlist)
        iBtnWishlist.setOnClickListener {
            val intent = Intent(this, CustomerWishlist::class.java)
            startActivity(intent)

            replaceFragment(VendorListFragment())
        }

        val iBtnSetting = findViewById<ImageButton>(R.id.iBtnSetting)
        iBtnSetting.setOnClickListener {
            val intent = Intent(this, CustomerSetting::class.java)
            startActivity(intent)

            replaceFragment(VendorMenuFragment())
            }
        }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentCustomerHomepage, fragment)
        fragmentTransaction.commit()
    }
}
