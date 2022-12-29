package com.example.fypcanteensystem.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.model.OrderReceiveDetailsData
import com.squareup.picasso.Picasso

class OrderReceiveDetailsAdapter (val c: Context, var orderDetailsList: ArrayList<OrderReceiveDetailsData>):
    RecyclerView.Adapter<OrderReceiveDetailsAdapter.OrderDetailsViewHolder>(){

    inner class OrderDetailsViewHolder(val v: View):RecyclerView.ViewHolder(v) {
        var itemName: TextView
        var itemPrice: TextView
        var itemQty: TextView
//        var itemSubPrice: TextView
//        var itemTotalPrice: TextView
//        var itemDiscountPrice: TextView

        init {
            itemName = v.findViewById<TextView>(R.id.txt_itemName)
            itemPrice = v.findViewById<TextView>(R.id.txt_itemPrice)
            itemQty = v.findViewById<TextView>(R.id.txt_itemQty)
//            itemSubPrice = v.findViewById<TextView>(R.id.txt_subtotal)
//            itemTotalPrice = v.findViewById<TextView>(R.id.txt_totalprice)
//            itemDiscountPrice = v.findViewById<TextView>(R.id.txt_discount)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderReceiveDetailsAdapter.OrderDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.order_details_items,parent,false)
        return OrderDetailsViewHolder(v)
    }

    override fun onBindViewHolder(holder: OrderReceiveDetailsAdapter.OrderDetailsViewHolder, position: Int) {
        val newList = orderDetailsList[position]
        holder.itemName.text = newList.itemName
        holder.itemPrice.text = "Price: " + newList.itemPrice
        holder.itemQty.text = "Quantity: " + newList.itemQty

        //unable to display subtotal all that, that put under rv one
//        val inflter = LayoutInflater.from(c)
//        val v = inflter.inflate(R.layout.dialog_order_receive,null)
//
//        val itemSubPrice = v.findViewById<TextView>(R.id.txt_subtotal)
//
//        itemSubPrice.text = "Subtotal6767" + newList.itemSubPrice
        //holder.itemTotalPrice.text = "Total Price" + newList.itemTotalPrice
        //holder.itemDiscountPrice.text = "Discount" + newList.itemDiscountPrice

        holder.itemName.paint?.isUnderlineText = true

    }

    override fun getItemCount(): Int {
        return  orderDetailsList.size
    }
}