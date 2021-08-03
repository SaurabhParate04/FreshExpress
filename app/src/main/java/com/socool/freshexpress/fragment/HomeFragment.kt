package com.socool.freshexpress.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.socool.freshexpress.R
import com.socool.freshexpress.adaptor.HomeFragAdapter
import com.socool.freshexpress.database.RestaurantFavouritesDatabase
import com.socool.freshexpress.database.RestaurantFavouritesEntities
import com.socool.freshexpress.model.Restaurants
import com.socool.freshexpress.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var progressLayout: RelativeLayout
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomeFragAdapter
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar
    var restaurantFavEntitiesList = arrayListOf<RestaurantFavouritesEntities>()
    var restaurantList = arrayListOf<Restaurants>()
    var displayList = arrayListOf<Restaurants>()

    private var nameComparator = Comparator<Restaurants> { res1, res2 ->
        res1.name.compareTo(res2.name, true)
    }

    private var costComparator = Comparator<Restaurants> { res1, res2 ->
        if (res1.cost_for_one.compareTo(res2.cost_for_one, true) == 0) {
            res1.name.compareTo(res2.name, true)

        } else {
            res1.cost_for_one.compareTo(res2.cost_for_one, true)
        }
    }

    private var ratingComparator = Comparator<Restaurants> { res1, res2 ->
        if (res1.rating.compareTo(res2.rating, true) == 0) {
            res1.name.compareTo(res2.name, true)

        } else {
            res1.rating.compareTo(res2.rating, true)
        }
    }

    private var nameComparatorFav = Comparator<RestaurantFavouritesEntities> { res1, res2 ->
        res1.resName.compareTo(res2.resName, true)
    }

    private var costComparatorFav = Comparator<RestaurantFavouritesEntities> { res1, res2 ->
        if (res1.resCost.compareTo(res2.resCost, true) == 0) {
            res1.resName.compareTo(res2.resName, true)

        } else {
            res1.resCost.compareTo(res2.resCost, true)
        }
    }

    private var ratingComparatorFav = Comparator<RestaurantFavouritesEntities> { res1, res2 ->
        if (res1.resRating.compareTo(res2.resRating, true) == 0) {
            res1.resName.compareTo(res2.resName, true)

        } else {
            res1.resRating.compareTo(res2.resRating, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)

        recyclerView = view.findViewById(R.id.RecyclerViewHomeFrag)
        progressLayout = view.findViewById(R.id.ProgressLayoutHomeFrag)
        progressBar = view.findViewById(R.id.ProgressBarHomeFrag)
        layoutManager = LinearLayoutManager(activity)
        sharedPreferences = (activity as Context).getSharedPreferences(
            getString(R.string.preference_file_name),
            Context.MODE_PRIVATE
        )
        progressLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        requestToServer()

        return view
    }

    class DBAsyncTaskFav(
        val context: Context,
        private val restaurantFavouritesEntity: RestaurantFavouritesEntities,
        private val mode: Int,
        userId: String?
    ) : AsyncTask<Void, Void, Boolean>() {

        private val db = Room.databaseBuilder(
            context, RestaurantFavouritesDatabase::class.java,
            "res-fav-db$userId"
        )
            .build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    val restaurantFavourites: RestaurantFavouritesEntities? =
                        db.restaurantFavouritesDAO()
                            .getRestaurantById(restaurantFavouritesEntity.res_id.toString())
                    db.close()
                    return restaurantFavourites != null
                }
                2 -> {
                    db.restaurantFavouritesDAO().insertRestaurant(restaurantFavouritesEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantFavouritesDAO().deleteRestaurant(restaurantFavouritesEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)

        val searchItem = menu.findItem(R.id.search_bar_home_frag)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            val editTextSearchView =
                searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            editTextSearchView.hint = "Search Restaurants..."

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty()) {
                        displayList.clear()
                        val search = newText.toLowerCase(Locale.getDefault())

                        restaurantList.forEach {
                            if (it.name.toLowerCase(Locale.getDefault()).contains(search)) {
                                displayList.add(it)
                            }
                        }
                        recyclerAdapter.notifyDataSetChanged()

                    } else {
                        displayList.clear()
                        displayList.addAll(restaurantList)
                        recyclerAdapter.notifyDataSetChanged()
                    }
                    return true
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_by_name_az -> {
                Collections.sort(displayList, nameComparator)
                Collections.sort(restaurantFavEntitiesList, nameComparatorFav)
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_by_name_za -> {
                Collections.sort(displayList, nameComparator)
                Collections.sort(restaurantFavEntitiesList, nameComparatorFav)
                displayList.reverse()
                restaurantFavEntitiesList.reverse()
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_by_cost_ascending -> {
                Collections.sort(displayList, costComparator)
                Collections.sort(restaurantFavEntitiesList, costComparatorFav)
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_by_cost_descending -> {
                Collections.sort(displayList, costComparator)
                Collections.sort(restaurantFavEntitiesList, costComparatorFav)
                displayList.reverse()
                restaurantFavEntitiesList.reverse()
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_by_rating_ascending -> {
                Collections.sort(displayList, ratingComparator)
                Collections.sort(restaurantFavEntitiesList, ratingComparatorFav)
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_by_rating_descending -> {
                Collections.sort(displayList, ratingComparator)
                Collections.sort(restaurantFavEntitiesList, ratingComparatorFav)
                displayList.reverse()
                restaurantFavEntitiesList.reverse()
                recyclerAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestToServer() {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnection(activity as Context)) {

            val jsonObjectRequest = object :
                JsonObjectRequest(Method.GET, url, null, com.android.volley.Response.Listener {
                    try {
                        progressLayout.visibility = View.GONE
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {

                            val response = data.getJSONArray("data")

                            for (i in 0 until response.length()) {
                                val restaurantJsonObject = response.getJSONObject(i)

                                val restaurantObject = Restaurants(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one") + "/person",
                                    restaurantJsonObject.getString("image_url")
                                )

                                val restaurantFavouritesEntity = RestaurantFavouritesEntities(
                                    restaurantObject.id.toInt(),
                                    restaurantObject.name,
                                    restaurantObject.cost_for_one,
                                    restaurantObject.rating,
                                    restaurantObject.image_url
                                )
                                restaurantFavEntitiesList.add(restaurantFavouritesEntity)

                                if (activity != null) {
                                    restaurantList.add(restaurantObject)
                                    displayList.add(restaurantObject)
                                    recyclerAdapter =
                                        HomeFragAdapter(
                                            activity as Context,
                                            displayList,
                                            restaurantFavEntitiesList
                                        )
                                    recyclerView.adapter = recyclerAdapter
                                    recyclerView.layoutManager = layoutManager
                                }
                            }
                        } else {
                            Toast.makeText(
                                activity,
                                "Some Unexpected Error Occurred!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity,
                            "Some Unexpected JSON Exception Occurred!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }, com.android.volley.Response.ErrorListener {
                    Toast.makeText(
                        activity,
                        "Some Unexpected Volley Error Occurred!!",
                        Toast.LENGTH_SHORT
                    ).show()

                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "7ead034d44439a"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)

        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Failed")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
    }
}
