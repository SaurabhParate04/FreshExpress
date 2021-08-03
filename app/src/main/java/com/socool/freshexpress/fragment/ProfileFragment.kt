package com.socool.freshexpress.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.socool.freshexpress.R

class ProfileFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var name: TextView
    private lateinit var mobNo: TextView
    private lateinit var email: TextView
    private lateinit var address: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        name = view.findViewById(R.id.txtNameProfile)
        mobNo = view.findViewById(R.id.txtMobileNumberProfile)
        email = view.findViewById(R.id.txtEmailProfile)
        address = view.findViewById(R.id.txtAddressProfile)
        sharedPreferences = (activity as Context).getSharedPreferences(
            getString(R.string.preference_file_name),
            Context.MODE_PRIVATE
        )

        name.text = "Name: " + sharedPreferences.getString("name", "Your Name")
        mobNo.text =
            "Mobile Number: " + sharedPreferences.getString("mobile_number", "Your Mobile Number")
        email.text = "Email: " + sharedPreferences.getString("email", "Your Email")
        address.text = "Address: " + sharedPreferences.getString("address", "Your Address")

        return view
    }

}
