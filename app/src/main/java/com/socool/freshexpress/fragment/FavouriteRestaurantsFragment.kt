package com.socool.freshexpress.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.socool.freshexpress.R
import com.socool.freshexpress.adaptor.FavouritesAdaptor
import com.socool.freshexpress.database.RestaurantFavouritesDatabase
import com.socool.freshexpress.database.RestaurantFavouritesEntities

class FavouriteRestaurantsFragment : Fragment() {

    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerFavFragAdaptor: FavouritesAdaptor
    private lateinit var backgroundFavFrag: RelativeLayout
    private lateinit var bgImgFavFrag: ImageView
    private lateinit var bgTxtFavFrag: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_restaurants, container, false)
        val recyclerFavFrag: RecyclerView = view.findViewById(R.id.RecyclerViewFavFrag)
        val layoutManager = LinearLayoutManager(activity)

        progressLayout = view.findViewById(R.id.ProgressLayoutFavFrag)
        progressBar = view.findViewById(R.id.ProgressBarFavFrag)
        backgroundFavFrag = view.findViewById(R.id.FavFragBackgroundLayout)
        bgImgFavFrag = view.findViewById(R.id.imgFavFrag)
        bgTxtFavFrag = view.findViewById(R.id.txtFavFrag)
        sharedPreferences = view.context.getSharedPreferences("Restaurant Preferences", Context.MODE_PRIVATE)

        val userId = sharedPreferences.getString("user_id","101")
        val favResList = RetrieveFavourites(activity as Context, userId).execute().get()

        backgroundFavFrag.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        if (activity != null) {
            progressLayout.visibility = View.GONE
            if (favResList.isEmpty()){
                backgroundFavFrag.visibility = View.VISIBLE
            }
            recyclerFavFragAdaptor = FavouritesAdaptor(activity as Context, favResList)
            recyclerFavFrag.adapter = recyclerFavFragAdaptor
            recyclerFavFrag.layoutManager = layoutManager
        }
        return view
    }

    class RetrieveFavourites(val context: Context, private val userId: String?) :
        AsyncTask<Void, Void, List<RestaurantFavouritesEntities>>() {
        override fun doInBackground(vararg params: Void?): List<RestaurantFavouritesEntities> {
            val db = Room.databaseBuilder(
                context,
                RestaurantFavouritesDatabase::class.java,
                "res-fav-db$userId"
            ).build()
            return db.restaurantFavouritesDAO().getAllRestaurants()
        }
    }
}
