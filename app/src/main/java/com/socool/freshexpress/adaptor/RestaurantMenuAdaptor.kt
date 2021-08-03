package com.socool.freshexpress.adaptor

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.socool.freshexpress.R
import com.socool.freshexpress.model.RestaurantMenu

class RestaurantMenuAdaptor(
    val context: Context,
    private val ItemList: ArrayList<RestaurantMenu>,
    private val listener: MenuItemClickListener
) : RecyclerView.Adapter<RestaurantMenuAdaptor.RestaurantMenuViewHolder>() {
    class RestaurantMenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItemName: TextView = view.findViewById(R.id.txtSingleRowRestaurantMenuName)
        val txtPrice: TextView = view.findViewById(R.id.txtSingleRowRestaurantMenuPrice)
        val txtItemNo: TextView = view.findViewById(R.id.txtSingleRowRestaurantMenuItemNo)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddRestaurantMenu)
        val qtyLayout: RelativeLayout = view.findViewById(R.id.layoutQty)
        val btnQtyPlus: Button = view.findViewById(R.id.btnPlusRestaurantMenu)
        val btnQtyMinus: Button = view.findViewById(R.id.btnMinusRestaurantMenu)
        val txtQty: TextView = view.findViewById(R.id.txtQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_recycler_restaurant_menu, parent, false)
        return RestaurantMenuViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ItemList.size
    }

    interface MenuItemClickListener {
        fun addItemToCart(menuItem: RestaurantMenu)
        fun removeItemFromCart(menuItem: RestaurantMenu)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RestaurantMenuViewHolder, position: Int) {
        val foodItem = ItemList[position]
        holder.txtItemName.text = foodItem.name
        holder.txtPrice.text = "Rs. " + foodItem.price.toString()
        holder.txtItemNo.text = (position + 1).toString()
        holder.btnAddToCart.setOnClickListener {
            holder.btnAddToCart.visibility = View.GONE
            holder.qtyLayout.visibility = View.VISIBLE
            holder.txtQty.text = "1"
            listener.addItemToCart(foodItem)
        }
        holder.btnQtyPlus.setOnClickListener {
            holder.txtQty.text = (holder.txtQty.text.toString().toInt() + 1).toString()
            listener.addItemToCart(foodItem)
        }
        holder.btnQtyMinus.setOnClickListener {
            holder.txtQty.text = (holder.txtQty.text.toString().toInt() - 1).toString()
            if (holder.txtQty.text.toString() == "0") {
                holder.qtyLayout.visibility = View.GONE
                holder.btnAddToCart.visibility = View.VISIBLE
            }
            listener.removeItemFromCart(foodItem)
        }
    }
}