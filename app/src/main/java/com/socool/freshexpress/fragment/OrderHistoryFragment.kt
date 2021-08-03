package com.socool.freshexpress.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.socool.freshexpress.R
import com.socool.freshexpress.adaptor.OrderHistoryFragAdaptor
import com.socool.freshexpress.model.OrderHistory
import com.socool.freshexpress.util.ConnectionManager
import org.json.JSONException

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerOrderHistory: RecyclerView
    private lateinit var progressBar: ProgressBar
    lateinit var recyclerOrderHistoryAdapter: OrderHistoryFragAdaptor
    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressLayout: RelativeLayout
    var orderHistoryList = ArrayList<OrderHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        recyclerOrderHistory = view.findViewById(R.id.RecyclerViewOrderHistory)
        progressLayout = view.findViewById(R.id.ProgressLayoutOrderHistoryFrag)
        progressBar = view.findViewById(R.id.ProgressBarOrderHistoryFrag)
        sharedPreferences = (activity as Context).getSharedPreferences(
            getString(R.string.preference_file_name),
            Context.MODE_PRIVATE
        )
        progressLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        requestToServer()

        return view
    }

    private fun requestToServer(){
        if (ConnectionManager().checkConnection(activity as Context)) {

            val userId = sharedPreferences.getString("user_id", "userId")
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"

            val jsonRequest = object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {
                        progressLayout.visibility = View.GONE
                        val response = data.getJSONArray("data")

                        for (i in 0 until response.length()) {
                            val order = response.getJSONObject(i)

                            val orderHistory = OrderHistory(
                                order.getString("order_id"),
                                order.getString("restaurant_name"),
                                order.getString("total_cost"),
                                order.getString("order_placed_at"),
                                order.getJSONArray("food_items")
                            )
                            orderHistoryList.add(orderHistory)
                            recyclerOrderHistoryAdapter =
                                OrderHistoryFragAdaptor(activity as Context, orderHistoryList)
                            recyclerOrderHistory.adapter = recyclerOrderHistoryAdapter
                            recyclerOrderHistory.layoutManager =
                                LinearLayoutManager(activity as Context)
                        }
                    } else {
                        Toast.makeText(
                            activity as Context,
                            "Some Unexpected Error Occurred!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(
                        activity as Context,
                        "Json Exception Occurred!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(activity as Context, "Volley Error Occurred!!", Toast.LENGTH_SHORT)
                    .show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "7ead034d44439a"
                    return headers
                }
            }
            queue.add(jsonRequest)
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
