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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.socool.freshexpress.R
import com.socool.freshexpress.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnNext: Button
    lateinit var etMobNo: EditText
    lateinit var toolbar: Toolbar
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        init()
        setUpToolBar()
        requestToServer()
    }

    private fun init() {
        etMobNo = findViewById(R.id.etMobileNoForgot)
        etEmail = findViewById(R.id.etEmailForgot)
        btnNext = findViewById(R.id.btnNext)
        toolbar = findViewById(R.id.ToolbarForgotPassword)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Forgot Password"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun requestToServer() {
        val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
        val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

        btnNext.setOnClickListener {
            if (ConnectionManager().checkConnection(this@ForgotPasswordActivity)) {

                if (etMobNo.text.toString().length != 10) {
                    etMobNo.error = "Mobile number must be 10 digits long"

                } else if (!isEmailValid(etEmail.text.toString())) {
                    etEmail.error = "Please enter a valid email address"

                } else {
                    val params = JSONObject()
                    params.put("mobile_number", etMobNo.text.toString())
                    params.put("email", etEmail.text.toString())

                    val jsonRequest = object : JsonObjectRequest(
                        Method.POST,
                        url,
                        params,
                        com.android.volley.Response.Listener {
                            try {
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")

                                if (success) {
                                    val firstTry = data.getBoolean("first_try")
                                    if (firstTry) {
                                        val intent = Intent(
                                            this@ForgotPasswordActivity,
                                            ResetPasswordActivity::class.java
                                        )
                                        sharedPreferences.edit()
                                            .putString("mobile_number", etMobNo.text.toString())
                                            .apply()
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@ForgotPasswordActivity,
                                            "You can change your password ONLY once!!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    val error = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@ForgotPasswordActivity,
                                        error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    "JSON Exception Occurred!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        com.android.volley.Response.ErrorListener {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Volley Exception Occurred!!!",
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
            } else {
                val dialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Failed")
                dialog.setPositiveButton("Open Settings") { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") { _, _ ->
                    ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                }
                dialog.create()
                dialog.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
        finish()
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
