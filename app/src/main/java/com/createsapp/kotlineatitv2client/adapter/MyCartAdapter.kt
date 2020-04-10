package com.createsapp.kotlineatitv2client.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.database.CartItem

class MyCartAdapter(internal var context: Context, internal var foodList: List<CartItem>) :
    RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MyCartViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


    class MyCartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var img_cart: ImageView

        init {
            img_cart = itemView.findViewById(R.id.img_cart)
        }
    }


}