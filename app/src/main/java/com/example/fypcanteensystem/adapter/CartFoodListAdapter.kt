package com.example.fypcanteensystem.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.dataModels.CartFoodListData

class CartFoodListAdapter(private val cartFoodList : ArrayList<CartFoodListData>, private val context: Context, private val listener: onItemClickListener) : RecyclerView.Adapter<CartFoodListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cart_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartFoodListAdapter.MyViewHolder, position: Int) {
        val currentCartFood = cartFoodList[position]
        holder.cartFoodName.text = currentCartFood.cartFoodName
        holder.cartFoodPrice.text = "RM " + currentCartFood.cartFoodPrice
        holder.cartFoodQty.text = currentCartFood.cartFoodQty

        if(currentCartFood.cartFoodImage.equals(null)){
            Glide.with(context).load(R.drawable.no_image_available).into(holder.cartFoodImage)
        }
        else{
            Glide.with(context).load(currentCartFood.cartFoodImage).into(holder.cartFoodImage)
        }
    }

    override fun getItemCount(): Int {
        return cartFoodList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val cartFoodName: TextView = itemView.findViewById(R.id.tvCartFoodName)
        val cartFoodPrice: TextView = itemView.findViewById(R.id.tvCartFoodPrice)
        val cartFoodQty: TextView = itemView.findViewById(R.id.tvCartFoodQty)
        val cartFoodImage: ImageView = itemView.findViewById(R.id.imgCartFood)

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