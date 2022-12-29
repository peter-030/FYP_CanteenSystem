package com.example.fypcanteensystem.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.model.EwalletHistoryData
import com.example.fypcanteensystem.model.OrderHistoryData

class EwalletHistoryAdapter (val c: Context, var eWalletHistoryList: ArrayList<EwalletHistoryData>):
    RecyclerView.Adapter<EwalletHistoryAdapter.EwalletHistoryViewHolder>(){

    inner class EwalletHistoryViewHolder(val v: View):RecyclerView.ViewHolder(v) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EwalletHistoryAdapter.EwalletHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.order_history_items,parent,false)
        return EwalletHistoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: EwalletHistoryAdapter.EwalletHistoryViewHolder, position: Int) {
        val newList = eWalletHistoryList[position]
        holder.orderNo.text = newList.orderId
        holder.dateTime.text = "Date: " + newList.orderDate
        holder.cusName.text = "Sender Name: " + newList.cusName
        holder.orderStatus.text = "Transaction Status: " + newList.orderStatus
        holder.noOfItem.text = "No of Bought Items: " + newList.noOfItems
        holder.paymentMethod.text = "(" + newList.paymentMethod + ")"
        holder.totalPrice.text = "Total Transaction: RM" + newList.totalPrice


        holder.orderNo.paint?.isUnderlineText = true
        if(newList.orderStatus.toString() == "Cancelled"){
            holder.orderStatus.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int {
        return  eWalletHistoryList.size
    }
}