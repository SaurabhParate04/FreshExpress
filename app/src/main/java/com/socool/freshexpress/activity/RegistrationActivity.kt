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

class RegistrationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMobNo: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        init()
        setUpToolBar()
        requestToServer()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
        finish()
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun init() {
        etName = findViewById(R.id.etRegisterName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etMobNo = findViewById(R.id.etRegisterMobileNo)
        etAddress = findViewById(R.id.etRegisterDeliveryAddress)
        etPassword = findViewById(R.id.etRegisterPassword)
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        toolbar = findViewById(R.id.ToolbarSignUp)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Sign Up"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun requestToServer() {
        val queue = Volley.newRequestQueue(this@RegistrationActivity)
        val url = "http://13.235.250.119/v2/register/fetch_result"

        btnRegister.setOnClickListener {
            if (ConnectionManager().checkConnection(this@RegistrationActivity)) {

                if (etName.text.toString().length < 3) {
                    etName.error = "Name must be at least 3 characters long"

                } else if (!isEmailValid(etEmail.text.toString())) {
                    etEmail.error = "Enter a valid email address"

                } else if (etMobNo.text.toString().length != 10) {
                    etMobNo.error = "Mobile Number must be of 10 digits"

                } else if (etAddress.text.toString().isEmpty()) {
                    etAddress.error = "This field cannot be empty"

                } else if (etPassword.text.toString().length < 4) {
                    etPassword.error = "Password must be at least 4 characters long"

                } else if (etConfirmPassword.text.toString() != etPassword.text.toString()) {
                    etConfirmPassword.error = "Passwords do not match"

                } else {
                    val info = JSONObject()
                    info.put("name", etName.text.toString())
                    info.put("mobile_number", etMobNo.text.toString())
                    info.put("password", etPassword.text.toString())
                    info.put("address", etAddress.text.toString())
                    info.put("email", etEmail.text.toString())

                    val jsonRequest =
                        object : JsonObjectRequest(Method.POST, url, info, Response.Listener {
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

                                    startActivity(
                                        Intent(
                                            this@RegistrationActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    val error = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@RegistrationActivity,
                                        error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    this@RegistrationActivity,
                                    "Json Exception Occurred!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, Response.ErrorListener {
                            Toast.makeText(
                                this@RegistrationActivity,
                                "Volley Error Occurred!!!",
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
                val dialog = AlertDialog.Builder(this@RegistrationActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet connection not found")
                dialog.setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    finish()
                }
                dialog.setNegativeButton("Exit") { _, _ ->
                    ActivityCompat.finishAffinity(this@RegistrationActivity)
                }
                dialog.create()
                dialog.show()
            }
        }
    }
}
