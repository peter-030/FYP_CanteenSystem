package com.example.fypcanteensystem.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.dataModels.VendorsListData
import com.google.firebase.database.*

import com.bumptech.glide.Glide


class VendorsListAdapter(private val vendorList : ArrayList<VendorsListData>, private val context: Context, private val listener:onItemClickListener) : RecyclerView.Adapter<VendorsListAdapter.MyViewHolder>() {
    //, private val listener: onItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vendors_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentVendor = vendorList[position]
        holder.shopName.text = currentVendor.merchantName
        holder.phoneNo.text = currentVendor.phoneNumber

        //get image
        /*
        Glide.with(context).load(currentVendor.vendorImg)
            .into(holder.imgVendor)
         */
        if(currentVendor.vendorImg.equals(null)){
            Glide.with(context).load(R.drawable.blank_profile).into(holder.imgVendor)
        }
        else{
            Glide.with(context).load(currentVendor.vendorImg).into(holder.imgVendor)
        }

    }

    override fun getItemCount(): Int {
        return vendorList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val shopName: TextView = itemView.findViewById(R.id.tvFoodNameMenu)
        val phoneNo: TextView = itemView.findViewById(R.id.tvFoodPriceMenu)
        val imgVendor: ImageView = itemView.findViewById(R.id.imgFoodMenu)

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition

            if(position != RecyclerView.NO_POSITION){
                listener.itemClick(position)
            }
        }
    }

    interface onItemClickListener {
        fun itemClick(position:Int)
    }

}