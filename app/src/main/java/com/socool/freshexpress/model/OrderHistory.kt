package com.socool.freshexpress.model

import org.json.JSONArray

data class OrderHistory(
    val orderId: String,
    val resName: String,
    val totalCost: String,
    val orderTime: String,
    val foodItem: JSONArray
)