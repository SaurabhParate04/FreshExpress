package com.socool.freshexpress.model

data class RestaurantMenu(
    val id: Int,
    val name: String,
    val price: Int,
    val restaurant_id: Int
)