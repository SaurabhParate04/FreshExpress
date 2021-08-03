package com.socool.freshexpress.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RestaurantFavouritesDAO {

    @Insert
    fun insertRestaurant(restaurantFavourites: RestaurantFavouritesEntities)

    @Delete
    fun deleteRestaurant(restaurantFavourites: RestaurantFavouritesEntities)

    @Query("SELECT * FROM restaurants_favourites")
    fun getAllRestaurants(): List<RestaurantFavouritesEntities>

    @Query("SELECT * FROM restaurants_favourites WHERE res_id = :resId ")
    fun getRestaurantById(resId: String): RestaurantFavouritesEntities
}