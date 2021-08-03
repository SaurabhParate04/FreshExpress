package com.socool.freshexpress.activity

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.socool.freshexpress.R
import com.socool.freshexpress.adaptor.CartAdapter
import com.socool.freshexpress.database.OrderDatabase
import com.socool.freshexpress.database.OrderEntity
import com.socool.freshexpress.model.RestaurantMenu
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var txtResName: TextView
    private lateinit var progressLayoutCart: RelativeLayout
    private lateinit var btnPlaceOrder: Button
    private lateinit var checkBox: CheckBox
    private lateinit var btnPayOnline: Button
    private lateinit var payOnlineLayout: RelativeLayout
    private lateinit var txtAddressPayOnline: TextView
    private lateinit var txtTotalPayOnline: TextView
    private lateinit var etPasswordPayOnline: EditText
    private lateinit var btnPlaceOrderPayOnline: Button
    private lateinit var checkboxLayout: RelativeLayout
    private lateinit var etRequest: EditText
    lateinit var sharedPreferences: SharedPreferences
    private var orderList = ArrayList<RestaurantMenu>()
    private var resId: Int = 0
    private var resName: String = ""
    lateinit var notificationBuilder: Notification.Builder
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    val channelId = "com.socool.freshexpress.activity"
    val description = "Test Notification"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        init()
        setupToolbar()
        setUpCartList()
        placeOrder()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        progressLayoutCart = findViewById(R.id.ProgressLayoutCart)
        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "") as String
        txtResName = findViewById(R.id.txtListBelowCart)
        txtResName.text = "Ordering from: $resName"
        payOnlineLayout = findViewById(R.id.payOnline)
        payOnlineLayout.visibility = View.GONE
        checkBox = findViewById(R.id.CheckboxCart)
        btnPayOnline = findViewById(R.id.btnPayOnline)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        txtAddressPayOnline = findViewById(R.id.txtAddressPayOnline)
        txtTotalPayOnline = findViewById(R.id.txtTotalPayOnline)
        etPasswordPayOnline = findViewById(R.id.etPasswordDialogPay)
        checkboxLayout = findViewById(R.id.RelativeLayoutCheckBox)
        btnPlaceOrderPayOnline = findViewById(R.id.btnPlaceOrderPayOnline)
        etRequest = findViewById(R.id.etRequest)
        btnPlaceOrderPayOnline.visibility = View.INVISIBLE
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbarCart)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpCartList() {
        recyclerViewCart = findViewById(R.id.RecyclerViewCart)
        val dbList = GetItemsFromDBAsync(applicationContext).execute().get()

        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItem, Array<RestaurantMenu>::class.java).asList()
            )
        }
        progressLayoutCart.visibility = View.GONE
        cartAdapter = CartAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity)
        recyclerViewCart.layoutManager = mLayoutManager
        recyclerViewCart.itemAnimator = DefaultItemAnimator()
        recyclerViewCart.adapter = cartAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun placeOrder() {

        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].price
        }
        val total = "Place Order COD (Rs. $sum)"
        btnPlaceOrder.text = total

        btnPlaceOrder.setOnClickListener {
            progressLayoutCart.visibility = View.VISIBLE
            recyclerViewCart.visibility = View.INVISIBLE
            requestToServer()
        }
        checkBox.setOnClickListener {
            if (checkBox.isChecked) {
                btnPlaceOrder.visibility = View.INVISIBLE
                btnPayOnline.visibility = View.VISIBLE
                btnPayOnline.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@CartActivity,
                        R.anim.slide_up
                    )
                )
            } else {
                btnPayOnline.visibility = View.INVISIBLE
                btnPlaceOrder.visibility = View.VISIBLE
                btnPlaceOrder.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@CartActivity,
                        R.anim.slide_up
                    )
                )
            }
        }
        btnPayOnline.setOnClickListener {
            payOnlineLayout.visibility = View.VISIBLE
            payOnlineLayout.startAnimation(
                AnimationUtils.loadAnimation(
                    this@CartActivity,
                    R.anim.slide_up
                )
            )
            var totalPrice = 0
            val address = sharedPreferences.getString("address", "My home")
            for (i in 0 until orderList.size) {
                totalPrice += orderList[i].price
            }
            txtTotalPayOnline.text = "Payable amount: Rs.$totalPrice"
            txtAddressPayOnline.text = "Address: $address"
            it.visibility = View.INVISIBLE
            btnPlaceOrderPayOnline.visibility = View.VISIBLE
        }

        btnPlaceOrderPayOnline.setOnClickListener {

            val validPassword = sharedPreferences.getString("password", "Password")
            if (etPasswordPayOnline.text.toString() == validPassword) {
                payOnlineLayout.visibility = View.INVISIBLE
                recyclerViewCart.visibility = View.INVISIBLE
                progressLayoutCart.visibility = View.VISIBLE
                checkboxLayout.visibility = View.GONE
                etRequest.visibility = View.GONE
                requestToServer()
                Toast.makeText(this@CartActivity, "Payment Successful!!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@CartActivity, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestToServer() {
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"

        val jsonParams = JSONObject()
        jsonParams.put(
            "user_id",
            this@CartActivity.getSharedPreferences(
                getString(R.string.preference_file_name),
                Context.MODE_PRIVATE
            ).getString(
                "user_id",
                null
            ) as String
        )
        jsonParams.put("restaurant_id", resId.toString())
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].price
        }
        jsonParams.put("total_cost", sum.toString())
        val foodArray = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].id)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {
                        this@CartActivity.deleteDatabase("res-db")

                        notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("mode", "from_outside")

                        val contentView = RemoteViews(packageName, R.layout.notification_layout)
                        contentView.setTextViewText(R.id.txtTitleNotification, "FreshExpress")
                        contentView.setTextViewText(
                            R.id.txtContentNotification,
                            "Order placed successfully! Click here to see your order history"
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationChannel =
                                NotificationChannel(
                                    channelId,
                                    description,
                                    NotificationManager.IMPORTANCE_HIGH
                                )
                            notificationChannel.enableLights(true)
                            notificationChannel.lightColor = Color.BLUE
                            notificationChannel.enableVibration(false)
                            notificationManager.createNotificationChannel(notificationChannel)

                            notificationBuilder =
                                Notification.Builder(this, channelId).setContent(contentView)
                                    .setSmallIcon(R.drawable.app_logo_coloured).setLargeIcon(
                                        BitmapFactory.decodeResource(
                                            this.resources,
                                            R.drawable.app_logo_coloured
                                        )
                                    ).setContentIntent(
                                        PendingIntent.getActivity(
                                            this,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                    ).setStyle(Notification.BigTextStyle().bigText("This is supposed to be a really really long text This is supposed to be a really really long text This is supposed to be a really really long text"))
                        } else {
                            notificationBuilder = Notification.Builder(this).setContent(contentView)
                                .setSmallIcon(R.drawable.app_logo_coloured).setLargeIcon(
                                    BitmapFactory.decodeResource(
                                        this.resources,
                                        R.drawable.app_logo_coloured
                                    )
                                ).setContentIntent(
                                    PendingIntent.getActivity(
                                        this,
                                        0,
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                )
                        }
                        notificationManager.notify(1234, notificationBuilder.build())

                        val dialog = Dialog(
                            this@CartActivity,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        )
                        dialog.setContentView(R.layout.order_placed_dialog)
                        dialog.show()
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                        btnOk.setOnClickListener {
                            dialog.dismiss()
                            progressLayoutCart.visibility = View.VISIBLE
                            recyclerViewCart.visibility = View.GONE
                            ActivityCompat.finishAffinity(this@CartActivity)
                            startActivity(Intent(this@CartActivity, MainActivity::class.java))
                        }
                    } else {
                        recyclerViewCart.visibility = View.VISIBLE
                        Toast.makeText(this@CartActivity, "Some Error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    recyclerViewCart.visibility = View.VISIBLE
                }
            }, Response.ErrorListener {
                recyclerViewCart.visibility = View.VISIBLE
                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "7ead034d44439a"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }

    class GetItemsFromDBAsync(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        private val db = Room.databaseBuilder(context, OrderDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDAO().getAllOrders()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (payOnlineLayout.visibility == View.VISIBLE) {
            payOnlineLayout.visibility = View.GONE
            payOnlineLayout.startAnimation(
                AnimationUtils.loadAnimation(
                    this@CartActivity,
                    R.anim.slide_down
                )
            )
            btnPlaceOrderPayOnline.visibility = View.INVISIBLE
            btnPayOnline.visibility = View.VISIBLE
        } else {
            this@CartActivity.deleteDatabase("res-db")
            super.onBackPressed()
        }
    }
}
