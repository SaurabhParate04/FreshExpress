package com.socool.freshexpress.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.socool.freshexpress.R
import com.socool.freshexpress.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var etMobNo: EditText
    private lateinit var etPassword: EditText
    private lateinit var txtSignUp: TextView
    private lateinit var txtForgot: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }

        etMobNo = findViewById(R.id.etMobileNo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogIn)
        txtSignUp = findViewById(R.id.txtSignUp)
        txtForgot = findViewById(R.id.txtForgotPassword)

        txtSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
            finish()
        }

        txtForgot.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }

        btnLogin.setOnClickListener {

            if (etMobNo.text.toString().length < 10) {
                etMobNo.error = "Mobile Number must be 10 digits long"

            } else {
                val queue = Volley.newRequestQueue(this@LoginActivity)
                val url = "http://13.235.250.119/v2/login/fetch_result"
                val mobNo = etMobNo.text.toString()
                val password = etPassword.text.toString()
                val jsonParams = JSONObject()

                jsonParams.put("mobile_number", mobNo)
                jsonParams.put("password", password)

                if (ConnectionManager().checkConnection(this@LoginActivity)) {

                    val jsonRequest =
                        object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                            try {
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")

                                if (success) {
                                    val response = data.getJSONObject("data")
                                    sharedPreferences.edit()
                                        .putString("user_id", response.getString("user_id")).apply()
                                    sharedPreferences.edit()
                                        .putString("name", response.getString("name")).apply()
                                    sharedPreferences.edit()
                                        .putString("email", response.getString("email")).apply()
                                    sharedPreferences.edit()
                                        .putString(
                                            "mobile_number",
                                            response.getString("mobile_number")
                                        )
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("address", response.getString("address")).apply()
                                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                    sharedPreferences.edit().putString("password", password).apply()
                                    sharedPreferences.edit().putBoolean("firstLogin", true).apply()

                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                } else {
                                    val error = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@LoginActivity,
                                        error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "JSON Exception Occurred!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }, Response.ErrorListener {
                            Toast.makeText(
                                this@LoginActivity,
                                "Volley Exception Occurred!!",
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
                    queue.add(jsonRequest)
                } else {
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection Failed")
                    dialog.setPositiveButton("Open Settings") { _, _ ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        finish()
                    }
                    dialog.setNegativeButton("Exit") { _, _ ->
                        ActivityCompat.finishAffinity(this@LoginActivity)
                    }
                    dialog.create()
                    dialog.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this@LoginActivity)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
