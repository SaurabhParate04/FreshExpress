package com.socool.freshexpress.adaptor

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.socool.freshexpress.R
import com.socool.freshexpress.model.OrderHistory
import com.socool.freshexpress.model.RestaurantMenu

class OrderHistoryFragAdaptor(val context: Context, private val itemList: ArrayList<OrderHistory>) :
    RecyclerView.Adapter<OrderHistoryFragAdaptor.OrderHistoryFragViewHolder>() {

    class OrderHistoryFragViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantNameOrderHistory)
        val txtOrderDate: TextView = view.findViewById(R.id.txtOrderDate)
        val txtOrderTime: TextView = view.findViewById(R.id.txtOrderTime)
        val recyclerInSingleRow: RecyclerView =
            view.findViewById(R.id.RecyclerViewSingleRowOrderHistory)
        val txtTotalPrice: TextView = view.findViewById(R.id.txtRestaurantTotalCostOrderHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryFragViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_recycler_order_history, parent, false)
        return OrderHistoryFragViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderHistoryFragViewHolder, position: Int) {
        val item = itemList[position]
        holder.txtRestaurantName.text = "Restaurant: " + item.resName
        holder.txtTotalPrice.text = "Total " + item.totalCost
        holder.txtOrderDate.text = "Date: " + item.orderTime.split(" ")[0]
        holder.txtOrderTime.text = "Time: " + item.orderTime.split(" ")[1]

        val foodItemsList = ArrayList<RestaurantMenu>()
        for (i in 0 until item.foodItem.length()) {
            val menuItem = item.foodItem.getJSONObject(i)
            foodItemsList.add(
                RestaurantMenu(
                    menuItem.getString("food_item_id").toInt(),
                    menuItem.getString("name"),
                    menuItem.getString("cost").toInt(),
                    100
                )
            )
        }
        val cartItemAdapter = CartAdapter(foodItemsList, context)
        val layoutManager = LinearLayoutManager(context)
        holder.recyclerInSingleRow.layoutManager = layoutManager
        holder.recyclerInSingleRow.itemAnimator = DefaultItemAnimator()
        holder.recyclerInSingleRow.adapter = cartItemAdapter
    }
}