package com.example.fypcanteensystem.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.dataModels.VendorListData

import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList


class VendorsListAdapter(
    private val vendorList: ArrayList<VendorListData>,
    private val context: Context,
    private val listener: onItemClickListener
) : RecyclerView.Adapter<VendorsListAdapter.MyViewHolder>() {
    //, private val listener: onItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.vendors_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentVendor = vendorList[position]
        var rentalCode = currentVendor.rentalCode
        if (rentalCode != null) {
            val rentalCodeSplit = rentalCode.toString().split("-")
            val shopLocation = rentalCodeSplit[0]
            val shopCode = rentalCodeSplit[1]
            rentalCode =
                shopLocation.uppercase(Locale.getDefault()) + " Canteen \n" + shopLocation[0].uppercase(
                    Locale.getDefault()
                ) + "-$shopCode"
        }

        holder.shopName.text = currentVendor.merchantName
        holder.phoneNo.text = currentVendor.phoneNumber
        holder.rentalCode.text = rentalCode

        val rate = currentVendor.vendorAvgRate?.toFloatOrNull()
        if (rate != null) {
            holder.vendorAvgRate.rating = rate
        }

        //get image
        /*
        Glide.with(context).load(currentVendor.vendorImg)
            .into(holder.imgVendor)
         */
        if (currentVendor.vendorImg.equals(null)) {
            Glide.with(context).load(R.drawable.blank_profile).into(holder.imgVendor)
        } else {
            Glide.with(context).load(currentVendor.vendorImg).into(holder.imgVendor)
        }

    }

    override fun getItemCount(): Int {
        return vendorList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val shopName: TextView = itemView.findViewById(R.id.tvFoodNameMenu)
        val phoneNo: TextView = itemView.findViewById(R.id.tvFoodPriceMenu)
        val imgVendor: ImageView = itemView.findViewById(R.id.imgFoodMenu)
        val rentalCode: TextView = itemView.findViewById(R.id.tvShopLocation)
        val vendorAvgRate: RatingBar = itemView.findViewById(R.id.vendorAvgRate)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition

            if (position != RecyclerView.NO_POSITION) {
                listener.itemClick(position)
            }
        }
    }

    interface onItemClickListener {
        fun itemClick(position: Int)
    }

}