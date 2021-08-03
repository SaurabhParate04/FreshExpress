package com.socool.freshexpress.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.socool.freshexpress.R
import com.socool.freshexpress.adaptor.ViewPagerAdapter
import com.socool.freshexpress.fragment.*
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.android.synthetic.main.drawer_header.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var frameLayout: FrameLayout
    private lateinit var navigationView: NavigationView
    private lateinit var viewPager: ViewPager
    private lateinit var tabsLayout: TabLayout
    private lateinit var parentLayout: RelativeLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var sharedPreferences: SharedPreferences
    lateinit var btnReady: Button
    lateinit var txtSkip: TextView
    private var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        intro()
        setupToolbar()
        openHome()
        setProfile()
        setUpDrawer()
        openedFromOrderPlacedNotification()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Restaurants"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val imgSort = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_sort)
        toolbar.overflowIcon = imgSort
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.FrameLayoutMain, HomeFragment())
            .commit()
        supportActionBar?.title = "All Restaurants"
        drawerLayout.closeDrawers()
        previousMenuItem?.isChecked = false
        navigationView.menu.getItem(0).isChecked = true
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawers()
            }
            supportFragmentManager.findFragmentById(R.id.FrameLayoutMain) !is HomeFragment -> {
                openHome()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun setProfile() {
        val name = navigationView.getHeaderView(0)
        val mobNo = navigationView.getHeaderView(0)

        name.txtNameDrawerHeader.text = sharedPreferences.getString("name", "Your Name")
        mobNo.txtMobileNoDrawerHeader.text = sharedPreferences.getString(
            "mobile_number",
            "Your Number"
        )
    }

    private fun init() {
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.ToolbarMain)
        coordinatorLayout = findViewById(R.id.CoordinatorMain)
        frameLayout = findViewById(R.id.FrameLayoutMain)
        navigationView = findViewById(R.id.NavigationViewMain)
        viewPager = findViewById(R.id.viewPager)
        tabsLayout = findViewById(R.id.tabs)
        btnReady = findViewById(R.id.btnReady)
        txtSkip = findViewById(R.id.txtSkip)
        parentLayout = findViewById(R.id.parentMain)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
    }

    private fun intro() {
        val firstLogin = sharedPreferences.getBoolean("firstLogin", false)
        if (firstLogin) {
            val fragmentList = ArrayList<Fragment>()
            fragmentList.add(IntroScreen1Fragment())
            fragmentList.add(IntroScreen2Fragment())
            fragmentList.add(IntroScreen3Fragment())

            drawerLayout.visibility = View.GONE
            tabsLayout.visibility = View.VISIBLE
            btnReady.visibility = View.INVISIBLE
            txtSkip.visibility = View.VISIBLE

            val adapter = ViewPagerAdapter(supportFragmentManager, fragmentList)
            viewPager.adapter = adapter
            val wormDotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
            wormDotsIndicator.setViewPager(viewPager)
            tabsLayout.setupWithViewPager(viewPager, true)
            sharedPreferences.edit().putBoolean("firstLogin", false).apply()


            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    // Do Nothing
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    // Do Nothing
                }

                override fun onPageSelected(position: Int) {
                    if (position == 2) {
                        btnReady.visibility = View.VISIBLE
                        txtSkip.visibility = View.GONE
                        btnReady.startAnimation(
                            AnimationUtils.loadAnimation(
                                this@MainActivity,
                                R.anim.slide_up
                            )
                        )
                    } else {
                        btnReady.visibility = View.INVISIBLE
                        txtSkip.visibility = View.VISIBLE
                    }
                }
            })
            btnReady.setOnClickListener {
                recreate()
                frameLayout.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@MainActivity,
                        R.anim.slide_up
                    )
                )
            }
            txtSkip.setOnClickListener {
                viewPager.setCurrentItem(2, true)
            }
        } else {
            tabsLayout.visibility = View.GONE
            drawerLayout.visibility = View.VISIBLE
            btnReady.visibility = View.GONE
            txtSkip.visibility = View.GONE
        }
    }

    private fun setUpDrawer() {
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            when (it.itemId) {

                R.id.HomeMenu -> {
                    openHome()
                }

                R.id.ProfileMenu -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.FrameLayoutMain, ProfileFragment())
                        .commit()
                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }

                R.id.FavouritesMenu -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.FrameLayoutMain, FavouriteRestaurantsFragment())
                        .commit()
                    supportActionBar?.title = "Favourite Restaurants"
                    drawerLayout.closeDrawers()
                }

                R.id.OrderHistoryMenu -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.FrameLayoutMain, OrderHistoryFragment())
                        .commit()
                    supportActionBar?.title = "Order History"
                    drawerLayout.closeDrawers()
                }

                R.id.FAQsMenu -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.FrameLayoutMain, FAQsFragment())
                        .commit()
                    supportActionBar?.title = "FAQs"
                    drawerLayout.closeDrawers()
                }

                R.id.LogOutMenu -> {

                    drawerLayout.closeDrawers()
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Confirmation")
                    dialog.setMessage("Are you sure you want to Exit?")
                    dialog.setPositiveButton("Yes") { _, _ ->
                        sharedPreferences.edit().clear().apply()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                    dialog.setNegativeButton("No") { _, _ ->
                        dialog.create().dismiss()
                        it.isChecked = false
                        navigationView.menu.getItem(0).isChecked = true
                    }
                    dialog.setCancelable(false)
                    dialog.create()
                    dialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun openedFromOrderPlacedNotification() {

        if (intent != null) {
            val mode = intent.getStringExtra("mode")

            if (mode != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.FrameLayoutMain, OrderHistoryFragment())
                    .commit()
                supportActionBar?.title = "Order History"
            }
        }
    }
}