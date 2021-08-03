package com.socool.freshexpress.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_order")
data class OrderEntity(
    @PrimaryKey val item_id: Int,
    @ColumnInfo(name = "food_item") val foodItem: String
)