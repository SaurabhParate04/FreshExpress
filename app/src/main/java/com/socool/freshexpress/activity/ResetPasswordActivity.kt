package com.socool.freshexpress.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.socool.freshexpress.R
import com.socool.freshexpress.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etOTP: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSubmit: Button
    lateinit var toolbar: Toolbar
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        init()
        requestToServer()
    }

    private fun init() {
        toolbar = findViewById(R.id.ToolbarResetPassword)
        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmNewPassword)
        btnSubmit = findViewById(R.id.btnSubmit)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Reset Password"
    }

    private fun requestToServer() {
        val queue = Volley.newRequestQueue(this@ResetPasswordActivity)
        val url = "http://13.235.250.119/v2/reset_password/fetch_result"

        btnSubmit.setOnClickListener {
            if (ConnectionManager().checkConnection(this@ResetPasswordActivity)) {

                when {
                    etOTP.text.toString().length != 4 -> {
                        etOTP.error = "Invalid OTP"
                    }
                    etNewPassword.text.toString().length < 4 -> {
                        etNewPassword.error = "Password must be at least 4 characters long"
                    }
                    etConfirmPassword.text.toString() != etNewPassword.text.toString() -> {
                        etConfirmPassword.error = "Passwords do not match"
                    }
                    else -> {
                        val params = JSONObject()
                        params.put(
                            "mobile_number",
                            sharedPreferences.getString("mobile_number", "My Number")
                        )
                        params.put("password", etNewPassword.text.toString())
                        params.put("otp", etOTP.text.toString())

                        val jsonRequest =
                            object :
                                JsonObjectRequest(Method.POST, url, params, Response.Listener {
                                    try {
                                        val data = it.getJSONObject("data")
                                        val success = data.getBoolean("success")

                                        if (success) {
                                            val response = data.getString("successMessage")

                                            Toast.makeText(
                                                this@ResetPasswordActivity,
                                                response,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            sharedPreferences.edit().clear().apply()
                                            startActivity(
                                                Intent(
                                                    this@ResetPasswordActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@ResetPasswordActivity,
                                                "Some unexpected error occurred!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: JSONException) {
                                        Toast.makeText(
                                            this@ResetPasswordActivity,
                                            "JSON Exception Occurred!!!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, Response.ErrorListener {
                                    Toast.makeText(
                                        this@ResetPasswordActivity,
                                        "Volley error occurred!!!",
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
                    }
                }
            } else {
                val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Failed")
                dialog.setPositiveButton("Open Settings") { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") { _, _ ->
                    ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                }
                dialog.create()
                dialog.show()
            }
        }
    }
}
