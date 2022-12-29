package com.example.fypcanteensystem.view

import android.app.AlertDialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.VendorOrderReceiveActivity
import com.example.fypcanteensystem.model.OrderReceiveData
import com.squareup.picasso.Picasso

class OrderReceiveAdapter (val c: Context, var orderList: ArrayList<OrderReceiveData>, val listener: OrderReceiveAdapter.onItemClickListener) :
    RecyclerView.Adapter<OrderReceiveAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val v: View):RecyclerView.ViewHolder(v) {
        var orderNo: TextView
        var dateTime: TextView
        var cusName: TextView
        var orderStatus: TextView
        var noOfItem: TextView
        var cusPic: ImageView
        var orderMenus: ImageView
        var paymentMethod: TextView

        init {
            orderNo = v.findViewById<TextView>(R.id.txt_orderNumber)
            dateTime = v.findViewById<TextView>(R.id.txt_dateTime)
            cusName = v.findViewById<TextView>(R.id.txt_cusName)
            orderStatus = v.findViewById<TextView>(R.id.txt_orderStatus)
            noOfItem = v.findViewById<TextView>(R.id.txt_noOfItem)
            cusPic = v.findViewById(R.id.img_cusPic)
            orderMenus = v.findViewById(R.id.orderMenus)
            paymentMethod = v.findViewById<TextView>(R.id.txt_paymentMethod)
        }

        public fun popupMenus(v: View, position: Int) {


            //val position = userList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_order_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.updateOrder->{
                        listener.updateOrder(position)


                        true
                    }
                    R.id.callCus->{
                        listener.callCus(position)


                        true
                    }

                    else-> true
                }

            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }


    }

    interface onItemClickListener{

        fun updateOrder(position: Int)
        fun displayInfo(position: Int)
        fun callCus(position: Int)
        //fun deleteInfo(position: Int)
        fun displayOrderDetails(position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.order_receive_item,parent,false)
        return OrderViewHolder(v)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val newList = orderList[position]
        holder.orderNo.text = newList.orderId
        holder.dateTime.text = "Date: " + newList.orderDate
        holder.cusName.text = "Name: " + newList.cusName
        holder.orderStatus.text = "Order Status: " + newList.orderStatus
        holder.noOfItem.text = "No of Items: " + newList.noOfItems
        holder.paymentMethod.text = "(" + newList.paymentMethod + ")"

        holder.orderNo.paint?.isUnderlineText = true

        if(holder.cusPic.toString().isNotEmpty())
        {
            Picasso.get().load(newList.ImageUri).resize(150,0).centerCrop().into(holder.cusPic)
        }
        if(holder.cusPic.drawable == null){
            holder.cusPic.setImageResource(R.drawable.profile)
        }

        holder.itemView.setOnClickListener(){

            listener.displayOrderDetails(position)

        }

        holder.orderMenus.setOnClickListener { holder.popupMenus(it,position) }
    }

    override fun getItemCount(): Int {
        return  orderList.size
    }
}