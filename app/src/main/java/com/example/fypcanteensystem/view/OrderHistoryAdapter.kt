package com.example.fypcanteensystem.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.model.OrderHistoryData
import com.example.fypcanteensystem.model.OrderReceiveDetailsData

class OrderHistoryAdapter (val c: Context, var orderHistoryList: ArrayList<OrderHistoryData>):
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    inner class OrderHistoryViewHolder(val v: View):RecyclerView.ViewHolder(v) {
        var orderNo: TextView
        var dateTime: TextView
        var cusName: TextView
        var orderStatus: TextView
        var noOfItem: TextView
        var paymentMethod: TextView
        var totalPrice: TextView

        init {
            orderNo = v.findViewById<TextView>(R.id.orderNumber)
            dateTime = v.findViewById<TextView>(R.id.dateTime)
            cusName = v.findViewById<TextView>(R.id.cusName)
            orderStatus = v.findViewById<TextView>(R.id.orderStatus)
            noOfItem = v.findViewById<TextView>(R.id.noOfItem)
            paymentMethod = v.findViewById<TextView>(R.id.paymentMethod)
            totalPrice = v.findViewById<TextView>(R.id.total)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryAdapter.OrderHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.order_history_items,parent,false)
        return OrderHistoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: OrderHistoryAdapter.OrderHistoryViewHolder, position: Int) {
        val newList = orderHistoryList[position]
        holder.orderNo.text = newList.orderId
        holder.dateTime.text = "Date: " + newList.orderDate
        holder.cusName.text = "Name: " + newList.cusName
        holder.orderStatus.text = "Order Status: " + newList.orderStatus
        holder.noOfItem.text = "No of Items: " + newList.noOfItems
        holder.paymentMethod.text = "(" + newList.paymentMethod + ")"
        holder.totalPrice.text = "Total Price: RM" + newList.totalPrice


        holder.orderNo.paint?.isUnderlineText = true
        if(newList.orderStatus.toString() == "Cancelled"){
            holder.orderStatus.setTextColor(Color.RED)
        }


    }

    override fun getItemCount(): Int {
        return  orderHistoryList.size
    }
}