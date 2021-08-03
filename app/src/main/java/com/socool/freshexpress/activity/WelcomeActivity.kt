package com.socool.freshexpress.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.socool.freshexpress.R

class WelcomeActivity : AppCompatActivity() {

    lateinit var imgLogo: ImageView
    lateinit var imgName: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        imgLogo = findViewById(R.id.imgWelcome)
        imgName = findViewById(R.id.imgWelcomeName)

        imgLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.welcome_logo))
        imgName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.welcome_name))

        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1200)
    }

    override fun onBackPressed() {
        //Do Nothing
    }
}
