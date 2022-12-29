package com.example.fypcanteensystem.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fypcanteensystem.R
import com.example.fypcanteensystem.model.FoodItemData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.childEvents
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class FoodItemAdapter(val c: Context, val userList:ArrayList<FoodItemData>,val listener: FoodItemAdapter.onItemClickListener):
    RecyclerView.Adapter<FoodItemAdapter.UserViewHolder>() {

    private lateinit var auth : FirebaseAuth
    private var databaseReference : DatabaseReference? = null
    private var database : FirebaseDatabase? = null
    private lateinit var ImageUri : Uri

    inner class UserViewHolder(val v: View):RecyclerView.ViewHolder(v) {
        var name: TextView
        var price: TextView
        var status: TextView
        var itemPic: ImageView
        var mMenus: ImageView

        init {
            name = v.findViewById<TextView>(R.id.itemName)
            price = v.findViewById<TextView>(R.id.itemPrice)
            status = v.findViewById<TextView>(R.id.itemStatus)
            itemPic = v.findViewById(R.id.imageItem)
            mMenus = v.findViewById(R.id.mMenus)

        }

        public fun popupMenus(v:View, position: Int) {


            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            databaseReference = database?.reference!!.child("vendorProfile")

            val user = auth.currentUser
            val userReference = databaseReference?.child(user?.uid!!)?.child("foodItem")



            //val position = userList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        listener.editInfo(position)
//                        val v = LayoutInflater.from(c).inflate(R.layout.add_item,null)
//                        val name = v.findViewById<EditText>(R.id.itemName)
//                        val price = v.findViewById<EditText>(R.id.itemPrice)
//                        val description = v.findViewById<EditText>(R.id.itemDescription)
//                        val itemStatusShelf = v.findViewById<RadioButton>(R.id.itemShelf)
//                        val itemStatusUnshelve = v.findViewById<RadioButton>(R.id.itemUnshelve)
//
//                        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
//                        val currentTime = Date()
//                        val filename = formatter.format(currentTime)
//                        val storageReference = FirebaseStorage.getInstance().getReference("foodItemImages/${filename}.png")
//
//                        val btnUploadPic = v.findViewById<Button>(R.id.btnUploadPic)
//                        btnUploadPic.setOnClickListener(){
//                            val intent = Intent()
//                            intent.type = "image/*"
//                            intent.action = Intent.ACTION_GET_CONTENT
//
//                            c.startActivity(intent)
//
//                        }
//                        userReference?.addValueEventListener(object : ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                for(userSnapshot in snapshot.children)
//                                {
//
//                                    name.setText(userSnapshot.child("itemName").value.toString())
//                                    price.setText(userSnapshot.child("itemPrice").value.toString())
//                                    description.setText(userSnapshot.child("itemDescription").value.toString())
//                                    if(userSnapshot.child("itemStatus").value.toString() == "shelf")
//                                    {
//                                        itemStatusShelf!!.isChecked=true
//                                    }
//                                    if(userSnapshot.child("itemStatus").value.toString() == "unshelve")
//                                    {
//                                        itemStatusUnshelve!!.isChecked=true
//                                    }
//                                    if(position.itemName == name.text.toString() &&
//                                        position.itemPrice == price.text.toString())
//                                    {
//                                        break
//                                    }
//                                }
//
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                TODO("Not yet implemented")
//                            }
//                        })
//
//                        AlertDialog.Builder(c)
//                            .setView(v)
//                            .setPositiveButton("Ok"){
//                                    dialog,_->
//                                //position.itemName = name.text.toString()
//                                //position.itemPrice = number.text.toString()
//
//                                val list = arrayListOf<DataSnapshot>()
//                                userReference?.addValueEventListener(object : ValueEventListener {
//                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                        for (snapshot in dataSnapshot.children) {
//                                            list.add(snapshot)
//
//                                        }
//                                        if(adapterPosition!=-1)
//                                        {
//                                            val keyClicked = list[adapterPosition].key!!
//                                            userReference.child(keyClicked).child("itemName").setValue(name.text.toString())
//                                            userReference.child(keyClicked).child("itemPrice").setValue(price.text.toString())
//                                            userReference.child(keyClicked).child("itemDescription").setValue(description.text.toString())
//                                            if(itemStatusShelf.isChecked)
//                                            {
//                                                userReference.child(keyClicked).child("itemStatus").setValue(itemStatusShelf.text.toString())
//                                            }
//                                            if(itemStatusUnshelve.isChecked)
//                                            {
//                                                userReference.child(keyClicked).child("itemStatus").setValue(itemStatusUnshelve.text.toString())
//                                            }
//
//                                            //can show gallery, but unable to upload to firebase
//                                            if(::ImageUri.isInitialized)
//                                            {
//                                                storageReference.putFile(ImageUri)
//                                                    .addOnSuccessListener {
//                                                        val result = it.metadata!!.reference!!.downloadUrl;
//                                                        result.addOnSuccessListener {
//
//                                                            val imageLink = it.toString()
//                                                            userReference.child(keyClicked).child("ImageUri").setValue(imageLink)
//
//                                                        }
//                                                    }
//                                            }
//
//                                            notifyDataSetChanged()
//                                        }
//
//                                    }
//
//                                    override fun onCancelled(databaseError: DatabaseError) {
//                                        TODO("Not yet implemented")
//                                    }
//                                })
//
//                                notifyDataSetChanged()
//                                Toast.makeText(c,"Food Item Information is Updated",Toast.LENGTH_SHORT).show()
//                                dialog.dismiss()
//
//                            }
//                            .setNegativeButton("Cancel"){
//                                    dialog,_->
//                                dialog.dismiss()
//
//                            }
//                            .create()
//                            .show()

                        true
                    }
                    R.id.delete->{
                        listener.deleteInfo(position)
//                        if(userList.size == 1 )
//                        {
//                            userList.removeAt(position) //remove last one deleted error!
//                            notifyItemRemoved(position)
//                            notifyDataSetChanged()
//                        }

                        /**set delete*/
//                        val list1 = arrayListOf<DataSnapshot>()
//                        userReference?.addValueEventListener(object : ValueEventListener {
//                            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                for (snapshot in dataSnapshot.children) {
//                                    list1.add(snapshot)
//
//                                }
//                            }
//
//                            override fun onCancelled(databaseError: DatabaseError) {
//                                TODO("Not yet implemented")
//                            }
//                        })
//
//                        AlertDialog.Builder(c)
//                            .setTitle("Delete")
//                            .setIcon(R.drawable.ic_warning)
//                            .setMessage("Are you sure to delete this Item")
//                            .setPositiveButton("Yes"){
//                                    dialog,_->
//
//                                if(adapterPosition!=-1)
//                                {
//
//                                    val keyClicked = list1[adapterPosition].key!!
//                                    userReference?.child(keyClicked)?.removeValue()
//                                    //userReference.child(keyClicked).child("itemPrice").removeValue()
//                                    userList.removeAt(adapterPosition) //remove last one deleted error!
//                                    notifyItemRemoved(adapterPosition)
//                                    notifyDataSetChanged()
//                                }
//
//                                notifyDataSetChanged()
//                                Toast.makeText(c,"Deleted this Item",Toast.LENGTH_SHORT).show()
//                                dialog.dismiss()
//                            }
//                            .setNegativeButton("No"){
//                                    dialog,_->
//                                dialog.dismiss()
//                            }
//                            .create()
//                            .show()

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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v  = inflater.inflate(R.layout.list_item,parent,false)
            return UserViewHolder(v)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val newList = userList[position]
            holder.name.text = newList.itemName
            holder.price.text = newList.itemPrice
            holder.status.text = newList.itemStatus
            if(holder.status.text == "unshelve")
            {
                holder.status.setTextColor(Color.RED)
            }

            if(newList.ImageUri!!.isNotEmpty())
            {
                Picasso.get().load(newList.ImageUri).resize(150,0).centerCrop().into(holder.itemPic)
            }
            holder.mMenus.setOnClickListener { holder.popupMenus(it,position) }
        }

        override fun getItemCount(): Int {
            return  userList.size
        }
}