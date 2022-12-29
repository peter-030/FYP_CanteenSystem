package com.example.fypcanteensystem.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.dataModels.OrderFoodListData
import com.google.firebase.database.ValueEventListener

class OrderFoodListAdapter(private val orderFoodList: ArrayList<OrderFoodListData>, private val context: Context) : RecyclerView.Adapter<OrderFoodListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.order_food_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderFoodListAdapter.MyViewHolder, position: Int) {
        val currentOrderFood = orderFoodList[position]
        holder.orderFoodName.text = currentOrderFood.orderFoodName
        holder.orderFoodQty.text = currentOrderFood.orderFoodQty
        holder.orderFoodPrice.text = "RM " + currentOrderFood.orderFoodPrice

        if(currentOrderFood.orderFoodImage.equals(null)){
            Glide.with(context).load(R.drawable.no_image_available).into(holder.orderFoodImage)
        }
        else{
            Glide.with(context).load(currentOrderFood.orderFoodImage).into(holder.orderFoodImage)
        }
    }

    override fun getItemCount(): Int {
        return orderFoodList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val orderFoodName: TextView = itemView.findViewById(R.id.tvOrderFoodName)
        val orderFoodQty: TextView = itemView.findViewById(R.id.tvOrderFoodQty)
        val orderFoodPrice: TextView = itemView.findViewById(R.id.tvOrderFoodPrice)
        val orderFoodImage: ImageView = itemView.findViewById(R.id.imgOrderFood)


    }
}