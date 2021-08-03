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
import com.squareup.picasso.Picasso

class FavouritesAdaptor(
    val context: Context,
    private val resList: List<RestaurantFavouritesEntities>
) : RecyclerView.Adapter<FavouritesAdaptor.FavouriteViewHolder>() {

    class FavouriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_recycler_home_fragment, parent, false)

        return FavouriteViewHolder(view)
    }

    override fun getItemCount(): Int {

        return resList.size
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {

        val userId = holder.sharedPreferences.getString("user_id", "101")
        val res = resList[position]
        holder.txtRestaurantName.text = res.resName
        holder.txtRating.text = res.resRating
        holder.txtCost.text = res.resCost
        Picasso.get().load(res.resImg).error(R.drawable.app_logo_coloured)
            .into(holder.imgRestaurant)

        holder.singleRowRecyclerView.setOnClickListener {
            val resMenuIntent = Intent(context, RestaurantMenuActivity::class.java)
            resMenuIntent.putExtra("res_id", res.res_id.toString())
            resMenuIntent.putExtra("name", res.resName)
            context.startActivity(resMenuIntent)
        }

        holder.favouriteRestaurantOn.setOnClickListener {
            val async = HomeFragment.DBAsyncTaskFav(context, res, 3, userId).execute()
            val result = async.get()

            if (result) {
                Toast.makeText(
                    context, res.resName + " has been removed from Favourites",
                    Toast.LENGTH_SHORT
                ).show()
                holder.favouriteRestaurantOn.visibility = View.GONE
                holder.favouriteRestaurantOff.visibility = View.VISIBLE
                holder.singleRowRecyclerView.visibility = View.GONE

            } else {
                Toast.makeText(context, "Some Error Occurred!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}