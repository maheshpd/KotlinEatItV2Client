package com.createsapp.kotlineatitv2client.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.CategoryModel

class MyCategoriesAdapter(
    internal var context: Context,
    internal var categoriesList: List<CategoryModel>
) :
    RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var category_name: TextView? = null
        var category_image: ImageView? = null

        init {
            category_name = itemView.findViewById(R.id.category_name) as TextView
            category_image = itemView.findViewById(R.id.category_image) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_category_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (categoriesList.size == 1)
            Common.DEFAULT_COLUM_COUNT
        else {
            if (categoriesList.size % 2 == 0)
                Common.DEFAULT_COLUM_COUNT
            else
                if (position > 1 && position == categoriesList.size - 1) Common.FULL_WIDTH_COLUMN else Common.DEFAULT_COLUM_COUNT
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(categoriesList.get(position).image).into(holder.category_image!!)
        holder.category_name!!.setText(categoriesList.get(position).name)
    }
}