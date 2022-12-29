package com.example.fypcanteensystem.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.dataModels.OrderListData

class OrderListAdapter(private val orderList : ArrayList<OrderListData>, private val context: Context, private val listener: OrderListAdapter.onItemClickListener) : RecyclerView.Adapter<OrderListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.order_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderListAdapter.MyViewHolder, position: Int) {
        val currentOrder = orderList[position]
        holder.orderId.text = currentOrder.orderId
        holder.vendorName.text = currentOrder.vendorName
        holder.orderTotalQty.text = currentOrder.orderTotalQty + " items"
        holder.orderNote.text = currentOrder.orderNote
        holder.orderStatus.text = currentOrder.orderStatus
        holder.orderDateTime.text = currentOrder.orderDateTime
        holder.paymentMethod.text = currentOrder.paymentMethod
        holder.orderTotalPrice.text = "RM " + currentOrder.orderTotalPrice

        if(currentOrder.orderRating != "" && currentOrder.orderRating != "Waiting Rate"){
            //holder.btnRatingVendor.isClickable = false
            holder.btnRatingVendor.isEnabled = false
            when (currentOrder.orderRating) {
                "Skipped" -> holder.btnRatingVendor.text = "Skipped"
                "Cancelled" -> holder.btnRatingVendor.text = "Cancelled"
                else -> holder.btnRatingVendor.text = "Rated: ${currentOrder.orderRating} stars"
            }

        }

        holder.tvClickToViewOrderItems.setOnClickListener {
            if(position != RecyclerView.NO_POSITION){
                listener.itemClick(position)
            }
        }

        holder.btnRatingVendor.setOnClickListener{
            if(currentOrder.orderStatus == "Completed"){
                if(position != RecyclerView.NO_POSITION){
                    listener.itemClickRating(position)
                }
            }
            else {
                listener.itemClickRating(-1)
            }
        }

    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    //inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val orderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val vendorName: TextView = itemView.findViewById(R.id.tvOrderVendorName)
        val orderTotalQty: TextView = itemView.findViewById(R.id.tvOrderTotalFoodQty)
        val orderNote: TextView = itemView.findViewById(R.id.tvOrderNote)
        val orderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val orderDateTime: TextView = itemView.findViewById(R.id.tvOrderDateTime)
        val paymentMethod: TextView = itemView.findViewById(R.id.tvOrderPaymentMethod)
        val orderTotalPrice: TextView = itemView.findViewById(R.id.tvOrderTotalPrice)
        val tvClickToViewOrderItems: TextView = itemView.findViewById(R.id.tvClickToViewOrderItems)
        val btnRatingVendor: Button = itemView.findViewById(R.id.btnRatingVendor)

        /*
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition

            if(position != RecyclerView.NO_POSITION){
                listener.itemClick(position)
            }
        }

         */
    }

    interface onItemClickListener {
        fun itemClick(position:Int)
        fun itemClickRating(position:Int)
    }





}