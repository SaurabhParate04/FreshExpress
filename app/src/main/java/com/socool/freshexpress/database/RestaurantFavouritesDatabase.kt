package com.socool.freshexpress.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RestaurantFavouritesEntities::class], version = 1)
abstract class RestaurantFavouritesDatabase : RoomDatabase() {

    abstract fun restaurantFavouritesDAO(): RestaurantFavouritesDAO
}