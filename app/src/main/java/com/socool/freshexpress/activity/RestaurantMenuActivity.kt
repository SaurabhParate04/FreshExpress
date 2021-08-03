package com.socool.freshexpress.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.socool.freshexpress.R
import com.socool.freshexpress.adaptor.RestaurantMenuAdaptor
import com.socool.freshexpress.database.OrderDatabase
import com.socool.freshexpress.database.OrderEntity
import com.socool.freshexpress.model.RestaurantMenu
import com.socool.freshexpress.util.ConnectionManager
import org.json.JSONException

class RestaurantMenuActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var recyclerView: RecyclerView
    lateinit var progressLayout: RelativeLayout
    lateinit var recyclerAdaptor: RestaurantMenuAdaptor
    lateinit var btnProceedToCart: Button
    private lateinit var progressBar: ProgressBar
    private var resId: Int? = 0
    private var resName: String? = "Restaurant Menu"
    var restaurantMenuList = ArrayList<RestaurantMenu>()
    var orderedFoodList = ArrayList<RestaurantMenu>()
    val layoutManager = LinearLayoutManager(this@RestaurantMenuActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        init()

        setUpToolBar()

        btnProceedToCart.setOnClickListener {
            proceedToCart()
        }

        getInfo()

        requestToServer()
    }

    private fun proceedToCart() {

        val foodItem = Gson().toJson(orderedFoodList)
        val async =
            DBAsyncTaskCart(this@RestaurantMenuActivity, resId.toString(), foodItem, 1).execute()
        val result = async.get()

        if (result) {
            val data = Bundle()
            data.putInt("resId", resId as Int)
            data.putString("resName", resName)
            val intent = Intent(this@RestaurantMenuActivity, CartActivity::class.java)
            intent.putExtra("data", data)
            startActivity(intent)

        } else {
            Toast.makeText(this@RestaurantMenuActivity, "Some unexpected error", Toast.LENGTH_SHORT)
                .show()
        }
    }

    class DBAsyncTaskCart(
        val context: Context,
        private val resId: String,
        private val foodItem: String,
        private val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {

        private val db = Room.databaseBuilder(context, OrderDatabase::class.java, "res-db")
            .build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDAO().insertOrder(OrderEntity(resId.toInt(), foodItem))
                    db.close()
                    return true
                }
                2 -> {
                    db.orderDAO().deleteOrder(OrderEntity(resId.toInt(), foodItem))
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (orderedFoodList.isNotEmpty()) {
            val dialog = AlertDialog.Builder(this@RestaurantMenuActivity)
            dialog.setTitle("Confirmation")
            dialog.setMessage("Going back will reset cart items. Do you still want to proceed")
            dialog.setPositiveButton("Yes") { _, _ ->
                super.onBackPressed()
            }
            dialog.setNegativeButton("No") { _, _ ->
                dialog.create().dismiss()
            }
            dialog.create()
            dialog.show()
        } else {
            super.onBackPressed()
        }
    }

    private fun init() {
        toolbar = findViewById(R.id.toolbarRestaurantMenu)
        recyclerView = findViewById(R.id.RecyclerViewRestaurantMenu)
        progressLayout = findViewById(R.id.ProgressLayoutRestaurantMenu)
        progressBar = findViewById(R.id.ProgressBarRestaurantMenu)
        btnProceedToCart = findViewById(R.id.btnProceedToCart)

        progressLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        btnProceedToCart.visibility = View.GONE
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = resName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getInfo() {
        if (intent != null) {
            resId = intent?.getStringExtra("res_id")?.toInt()
            resName = intent.getStringExtra("name")

        } else {
            finish()
            Toast.makeText(
                this@RestaurantMenuActivity,
                "Some Unexpected error occurred!",
                Toast.LENGTH_LONG
            ).show()
        }

        if (resId == 0) {
            Toast.makeText(
                this@RestaurantMenuActivity,
                "Some Unexpected error occurred!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestToServer() {
        val queue = Volley.newRequestQueue(this@RestaurantMenuActivity)

        val url = "http://13.235.250.119/v2/restaurants/fetch_result/" + resId.toString()

        if (ConnectionManager().checkConnection(this@RestaurantMenuActivity)) {

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener {

                    try {
                        progressLayout.visibility = View.GONE
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")

                        if (success) {
                            val response = data.getJSONArray("data")

                            for (i in 0 until response.length()) {
                                val restaurantMenu = response.getJSONObject(i)

                                val restaurantMenuObject = RestaurantMenu(
                                    restaurantMenu.getString("id").toInt(),
                                    restaurantMenu.getString("name"),
                                    restaurantMenu.getString("cost_for_one").toInt(),
                                    restaurantMenu.getString("restaurant_id").toInt()
                                )
                                restaurantMenuList.add(restaurantMenuObject)
                                recyclerAdaptor = RestaurantMenuAdaptor(
                                    this@RestaurantMenuActivity,
                                    restaurantMenuList,
                                    object : RestaurantMenuAdaptor.MenuItemClickListener {
                                        override fun addItemToCart(menuItem: RestaurantMenu) {
                                            orderedFoodList.add(menuItem)
                                            if (orderedFoodList.size > 0) {
                                                btnProceedToCart.visibility = View.VISIBLE
                                                if (orderedFoodList.size == 1) {
                                                    btnProceedToCart.startAnimation(
                                                        AnimationUtils.loadAnimation(
                                                            this@RestaurantMenuActivity,
                                                            R.anim.slide_up
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        override fun removeItemFromCart(menuItem: RestaurantMenu) {
                                            orderedFoodList.remove(menuItem)
                                            if (orderedFoodList.isEmpty()) {
                                                btnProceedToCart.visibility = View.GONE
                                                btnProceedToCart.startAnimation(
                                                    AnimationUtils.loadAnimation(
                                                        this@RestaurantMenuActivity,
                                                        R.anim.slide_down
                                                    )
                                                )
                                            }

                                        }
                                    })
                                recyclerView.adapter = recyclerAdaptor
                                recyclerView.layoutManager = layoutManager
                                recyclerView.setItemViewCacheSize(15)
                            }
                        } else {
                            Toast.makeText(
                                this@RestaurantMenuActivity,
                                "Some unexpected error occurred!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@RestaurantMenuActivity,
                            "JSON Exception Occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }, Response.ErrorListener {
                    Toast.makeText(
                        this@RestaurantMenuActivity,
                        "Some unexpected Volley Error occurred!!!",
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
            val dialog = AlertDialog.Builder(this@RestaurantMenuActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Failed")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@RestaurantMenuActivity)
            }
            dialog.create()
            dialog.show()
        }
    }
}