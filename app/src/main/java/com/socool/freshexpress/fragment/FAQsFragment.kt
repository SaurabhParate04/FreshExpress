package com.socool.freshexpress.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.socool.freshexpress.R

class FAQsFragment : Fragment() {

    private lateinit var btnContactPhone: Button
    private lateinit var btnContactEmail: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faq_s, container, false)

        btnContactPhone = view.findViewById(R.id.btnContactUsPhone)
        btnContactEmail = view.findViewById(R.id.btnContactUsEmail)

        btnContactPhone.setOnClickListener {
            val intentPhone = Intent(Intent.ACTION_DIAL)
            intentPhone.data = Uri.parse("tel:07232243308")
            startActivity(intentPhone)
        }

        btnContactEmail.setOnClickListener {
            val intentEmail =
                Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "customercare@freshexpress.com"))
            startActivity(intentEmail)
        }
        return view
    }

}
