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
import com.example.fypcanteensystem.adapter.FoodMenuListAdapter.MyViewHolder
import com.example.fypcanteensystem.dataModels.FoodMenuListData

class FoodMenuListAdapter(private val foodMenuList : ArrayList<FoodMenuListData>, private val context: Context, private val listener: onItemClickListener) : RecyclerView.Adapter<FoodMenuListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.menu_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentFood = foodMenuList[position]
        holder.orderId.text = currentFood.foodName
        holder.foodPrice.text = "RM " + currentFood.foodPrice
        holder.foodDesp.text = currentFood.foodDescription

        if(currentFood.foodImage.equals(null)){
            Glide.with(context).load(R.drawable.no_image_available).into(holder.foodImage)
        }
        else{
            Glide.with(context).load(currentFood.foodImage).into(holder.foodImage)
        }
    }

    override fun getItemCount(): Int {
        return foodMenuList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val orderId: TextView = itemView.findViewById(R.id.tvFoodNameMenu)
        val foodPrice: TextView = itemView.findViewById(R.id.tvFoodPriceMenu)
        val foodDesp: TextView = itemView.findViewById(R.id.tvFoodDespMenu)
        val foodImage: ImageView = itemView.findViewById(R.id.imgFoodMenu)


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