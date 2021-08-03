package com.socool.freshexpress.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.socool.freshexpress.R
import com.socool.freshexpress.model.RestaurantMenu

class CartAdapter(private val cartList: ArrayList<RestaurantMenu>, val context: Context) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_row_recycler_cart, parent, false)
        return CartViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartObject = cartList[position]
        holder.itemName.text = cartObject.name
        val cost = "Rs. ${cartObject.price}"
        holder.itemCost.text = cost
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.txtSingleRowCartName)
        val itemCost: TextView = view.findViewById(R.id.txtSingleRowCartPrice)
    }
}