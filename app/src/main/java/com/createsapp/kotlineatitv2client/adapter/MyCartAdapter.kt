package com.createsapp.kotlineatitv2client.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.database.CartDataSource
import com.createsapp.kotlineatitv2client.database.CartDatabase
import com.createsapp.kotlineatitv2client.database.CartItem
import com.createsapp.kotlineatitv2client.database.LocalCartDataSource
import com.createsapp.kotlineatitv2client.eventbus.UpdateItemInCart
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus


class MyCartAdapter(internal var context: Context, internal var cartItems: List<CartItem>) :
    RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder>() {

    internal lateinit var compositeDisposable: CompositeDisposable
    internal lateinit var cartDataSource: CartDataSource


    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartViewHolder {
        return MyCartViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_cart_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: MyCartViewHolder, position: Int) {
        Glide.with(context).load(cartItems[position].foodImage).into(holder.img_cart)
        holder.txt_food_name.text = StringBuilder(cartItems[position].foodName!!)
//        holder.text_food_price.text = StringBuilder(cartItems[position].foodPrice?.plus(cartItems[position].foodExtraPrice))
        holder.text_food_price.text = StringBuilder("").append(
            cartItems[position].foodPrice?.plus(
                cartItems[position].foodExtraPrice
            )
        )
        holder.number_button.number = cartItems[position].foodQuantity.toString()


        //Event
        holder.number_button.setOnValueChangeListener { view, oldValue, newValue ->
            cartItems[position].foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(cartItems[position]))
        }
    }


    class MyCartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var img_cart: ImageView
        lateinit var txt_food_name: TextView
        lateinit var text_food_price: TextView
        lateinit var number_button: ElegantNumberButton

        init {
            img_cart = itemView.findViewById(R.id.img_cart) as ImageView
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            text_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            number_button = itemView.findViewById(R.id.number_button) as ElegantNumberButton
        }
    }


}