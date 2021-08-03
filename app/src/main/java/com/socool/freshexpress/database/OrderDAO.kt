package com.socool.freshexpress.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderDAO {

    @Insert
    fun insertOrder(restaurantMenu: OrderEntity)

    @Delete
    fun deleteOrder(restaurantMenu: OrderEntity)

    @Query("SELECT * FROM restaurant_order")
    fun getAllOrders(): List<OrderEntity>

}