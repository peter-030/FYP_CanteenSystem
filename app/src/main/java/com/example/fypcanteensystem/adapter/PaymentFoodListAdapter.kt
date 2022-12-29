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
import com.example.fypcanteensystem.dataModels.CartFoodListData

class PaymentFoodListAdapter(private val paymentFoodList : ArrayList<CartFoodListData>, private val context: Context) : RecyclerView.Adapter<PaymentFoodListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.payment_food_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PaymentFoodListAdapter.MyViewHolder, position: Int) {
        val currentPaymentFood = paymentFoodList[position]
        holder.paymentFoodName.text = currentPaymentFood.cartFoodName
        holder.paymentFoodPrice.text = "RM " + currentPaymentFood.cartFoodPrice
        holder.paymentFoodQty.text = currentPaymentFood.cartFoodQty + " x"

        if(currentPaymentFood.cartFoodImage.equals(null)){
            Glide.with(context).load(R.drawable.no_image_available).into(holder.paymentFoodImage)
        }
        else{
            Glide.with(context).load(currentPaymentFood.cartFoodImage).into(holder.paymentFoodImage)
        }
    }

    override fun getItemCount(): Int {
        return paymentFoodList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentFoodName: TextView = itemView.findViewById(R.id.tvPaymentFoodName)
        val paymentFoodPrice: TextView = itemView.findViewById(R.id.tvPaymentFoodSublPrice)
        val paymentFoodQty: TextView = itemView.findViewById(R.id.tvPaymentFoodQty)
        val paymentFoodImage: ImageView = itemView.findViewById(R.id.imgPaymentFood)

    }
}