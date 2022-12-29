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
import com.example.fypcanteensystem.model.EwalletData
import com.example.fypcanteensystem.model.FoodItemData
import com.squareup.picasso.Picasso

class EwalletAdapter(val c: Context, val eWalletList:ArrayList<EwalletData>, val listener: EwalletAdapter.onItemClickListener):
    RecyclerView.Adapter<EwalletAdapter.EwalletViewHolder>() {

    inner class EwalletViewHolder(val v: View):RecyclerView.ViewHolder(v) {
        var studId: TextView
        var rechargeAmount: TextView
        var studPhoneNo: TextView
        var rechargeDate: TextView

        init {
            studId = v.findViewById<TextView>(R.id.txt_studentID)
            rechargeAmount = v.findViewById<TextView>(R.id.txt_rechargeAmount)
            studPhoneNo = v.findViewById<TextView>(R.id.txt_studPhoneNumber)
            rechargeDate = v.findViewById<TextView>(R.id.txt_rechargeDate)

        }
    }

    interface onItemClickListener{

        //fun editInfo(position: Int)
        //fun deleteInfo(position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EwalletAdapter.EwalletViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_ewallet_recharge,parent,false)
        return EwalletViewHolder(v)
    }

    override fun onBindViewHolder(holder: EwalletAdapter.EwalletViewHolder, position: Int) {
        val newList = eWalletList[position]
        holder.studId.text = newList.studId
        holder.rechargeAmount.text = "RM"+ newList.rechargeAmount
        holder.studPhoneNo.text = newList.studPhoneNo
        holder.rechargeDate.text = newList.rechargeDate

    }

    override fun getItemCount(): Int {
        return  eWalletList.size
    }

}