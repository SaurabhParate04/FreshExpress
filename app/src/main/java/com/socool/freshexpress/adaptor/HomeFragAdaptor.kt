package com.socool.freshexpress.adaptor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.socool.freshexpress.R
import com.socool.freshexpress.activity.RestaurantMenuActivity
import com.socool.freshexpress.database.RestaurantFavouritesEntities
import com.socool.freshexpress.fragment.HomeFragment
import com.socool.freshexpress.model.Restaurants
import com.squareup.picasso.Picasso

class HomeFragAdapter(
    val context: Context,
    private val itemList: ArrayList<Restaurants>,
    private val favList: ArrayList<RestaurantFavouritesEntities>
) : RecyclerView.Adapter<HomeFragAdapter.HomeFragViewHolder>() {

    class HomeFragViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtNameSingleRowHome)
        val txtCost: TextView = view.findViewById(R.id.txtCostSingleRowHome)
        val txtRating: TextView = view.findViewById(R.id.txtRatingSingleRowHome)
        val imgRestaurant: ImageView = view.findViewById(R.id.imgSingleRowHome)
        val favouriteRestaurantOn: ImageButton = view.findViewById(R.id.imgBtnFavSingleRowHomeOn)
        val favouriteRestaurantOff: ImageButton = view.findViewById(R.id.imgBtnFavSingleRowHomeOff)
        val singleRowRecyclerView: RelativeLayout = view.findViewById(R.id.rowSingleRowHome)
        val sharedPreferences: SharedPreferences =
            view.context.getSharedPreferences("Restaurant Preferences", Context.MODE_PRIVATE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFragViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_recycler_home_fragment, parent, false)

        return HomeFragViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: HomeFragViewHolder, position: Int) {

        val userId = holder.sharedPreferences.getString("user_id", "101")

        val restaurants = itemList[position]
        holder.txtRestaurantName.text = restaurants.name
        holder.txtRating.text = restaurants.rating
        holder.txtCost.text = restaurants.cost_for_one
        Picasso.get().load(restaurants.image_url).error(R.drawable.app_logo_transparent)
            .into(holder.imgRestaurant)

        holder.singleRowRecyclerView.setOnClickListener {
            val resMenuIntent = Intent(context, RestaurantMenuActivity::class.java)
            resMenuIntent.putExtra("res_id", restaurants.id)
            resMenuIntent.putExtra("name", restaurants.name)
            context.startActivity(resMenuIntent)
        }
        val resFavEntity = favList[position]

        if (HomeFragment.DBAsyncTaskFav(context, resFavEntity, 1, userId).execute().get()) {
            holder.favouriteRestaurantOn.visibility = View.VISIBLE
            holder.favouriteRestaurantOff.visibility = View.GONE

        } else {
            holder.favouriteRestaurantOff.visibility = View.VISIBLE
            holder.favouriteRestaurantOn.visibility = View.GONE
        }

        holder.favouriteRestaurantOn.setOnClickListener {
            val async = HomeFragment.DBAsyncTaskFav(context, resFavEntity, 3, userId).execute()
            val result = async.get()

            if (result) {
                Toast.makeText(
                    context, resFavEntity.resName + " has been removed from Favourites",
                    Toast.LENGTH_SHORT
                ).show()
                holder.favouriteRestaurantOn.visibility = View.GONE
                holder.favouriteRestaurantOff.visibility = View.VISIBLE
            } else {
                Toast.makeText(context, "Some Error Occurred!!", Toast.LENGTH_SHORT).show()
            }
        }

        holder.favouriteRestaurantOff.setOnClickListener {
            val async = HomeFragment.DBAsyncTaskFav(context, resFavEntity, 2, userId).execute()
            val result = async.get()

            if (result) {
                Toast.makeText(
                    context, resFavEntity.resName + " has been added to Favourites",
                    Toast.LENGTH_SHORT
                ).show()
                holder.favouriteRestaurantOff.visibility = View.GONE
                holder.favouriteRestaurantOn.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    context, "Some Unexpected Error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
