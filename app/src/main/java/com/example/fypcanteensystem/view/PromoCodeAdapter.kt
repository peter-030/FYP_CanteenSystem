package com.example.fypcanteensystem.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.model.PromoCodeData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class PromoCodeAdapter(val c: Context, val codeList:ArrayList<PromoCodeData>, val listener: PromoCodeAdapter.onItemClickListener):
    RecyclerView.Adapter<PromoCodeAdapter.CodeViewHolder>() {



    inner class CodeViewHolder(val v: View):RecyclerView.ViewHolder(v) {
        var name: TextView
        var discountPrice: TextView
        var minSpend: TextView
        var quantity: TextView
        var status: TextView
        var validDate: TextView
        var mMenus: ImageView

        init {
            name = v.findViewById<TextView>(R.id.codeName)
            discountPrice = v.findViewById<TextView>(R.id.codeDiscountPrice)
            minSpend = v.findViewById<TextView>(R.id.codeMinSpend)
            quantity = v.findViewById<TextView>(R.id.codeQuantity)
            status = v.findViewById<TextView>(R.id.codeStatus)
            validDate = v.findViewById<TextView>(R.id.codeValidDate)

            mMenus = v.findViewById(R.id.mMenusCode)

        }

        public fun popupMenus(v: View, position: Int) {


            //val position = userList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        listener.editInfo(position)


                        true
                    }
                    R.id.delete->{
                        listener.deleteInfo(position)


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

        fun editInfo(position: Int)
        fun deleteInfo(position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_promocode,parent,false)
        return CodeViewHolder(v)
    }

    override fun onBindViewHolder(holder: PromoCodeAdapter.CodeViewHolder, position: Int) {
        val newList = codeList[position]
        holder.name.text = newList.codeName
        holder.discountPrice.text = newList.codeDiscountPrice
        holder.minSpend.text = newList.codeMinSpend
        holder.quantity.text = newList.codeQuantity
        holder.status.text = newList.codeStatus
        if(holder.status.text == "unshelve")
        {
            holder.status.setTextColor(Color.RED)
        }

        holder.name.paint?.isUnderlineText = true
        holder.validDate.text = newList.codeStartDate +" to " + newList.codeEndDate

        holder.mMenus.setOnClickListener { holder.popupMenus(it,position) }
    }

    override fun getItemCount(): Int {
        return  codeList.size
    }

}