package com.bennohan.mynotes.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.database.OpenNavigation
import com.bennohan.mynotes.database.user.User
import com.bennohan.mynotes.database.user.UserDao
import com.bennohan.mynotes.databinding.ActivityNavigationBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.bennohan.mynotes.ui.biometric.SettingActivity
import com.bennohan.mynotes.ui.categoryNote.CategoryActivity
import com.bennohan.mynotes.ui.favouriteNote.FavouriteActivity
import com.bennohan.mynotes.ui.login.LoginActivity
import com.bennohan.mynotes.ui.profile.ProfileFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crocodic.core.base.activity.NoViewModelActivity
import com.crocodic.core.extension.openActivity
import com.crocodic.core.helper.ImagePreviewHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class NavigationActivity : NoViewModelActivity<ActivityNavigationBinding>(R.layout.activity_navigation) {

    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()

    @Inject
    lateinit var userDao : UserDao

    private  var user : User? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private val fragmentManager = supportFragmentManager
    private val fragment = fragmentManager.findFragmentById(R.id.home_fragment) as? HomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawerLayout = binding.drawerLayout
        navView = binding.navigationView


        binding.btnAddNote.setOnClickListener {
            openActivity<NoteActivity>()
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userDao.getUser().collect{
                        user = it
                        sideMenu()
                    }
                }
            }
        }

        replaceFragment(homeFragment)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> {
                    replaceFragment(homeFragment)
                    true
                }
                R.id.navProfile -> {
                    replaceFragment(profileFragment)
                    true
                }
                // Add more cases for each menu item
                else -> false
            }
        }


    }

    @Suppress("DEPRECATION")
     fun sideMenu() {

        val inflater = LayoutInflater.from(this)
        val headerView = inflater.inflate(R.layout.nav_header, navView, false)
        val ivProfile = headerView.findViewById<ImageView>(R.id.ivProfile)
        val tvUsername = headerView.findViewById<TextView>(R.id.tvUsername)

        if (headerView == null) {
            // inflate a new header view and add it to the navigation view
            val headersView = inflater.inflate(R.layout.nav_header, navView, false)
            navView.addHeaderView(headersView)

        } else {
            //If Header is not null it remove old header
            navView.removeHeaderView(navView.getHeaderView(0))
            navView.addHeaderView(headerView)
        }

        ivProfile.setOnClickListener {
            ImagePreviewHelper(this).show(ivProfile, user?.photo)
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userDao.getUser().collect{
                        tvUsername.text = it.name

                        Glide.with(this@NavigationActivity)
                            .load(it.photo)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfile)

                    }
                }
            }
        }


        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //Do nothing
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

                // Set the status bar color to the drawer open color when the drawer is opened
//                window.statusBarColor =
//                    ContextCompat.getColor(this@HomeActivity, R.color.my_hint_color)
//                window.decorView.systemUiVisibility = 0
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)

//                window.statusBarColor =
//                    ContextCompat.getColor(this@HomeActivity, R.color.main_background_color)
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Do nothing
            }


        })
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_favourite -> {
                    // Handle Settings item selection
                    drawerLayout.closeDrawer(GravityCompat.START)
                    // Implement your logic for Settings item here
                    openActivity<FavouriteActivity>()
                    true
                }
                R.id.nav_category -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    openActivity<CategoryActivity>()
                    true
                }
                R.id.nav_setting -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    openActivity<SettingActivity>()
                    true
                }
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    logout()
                    true
                }
                else -> false
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
     fun condition( openNavigation: OpenNavigation ){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Handle ActionBarDrawerToggle click event
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.constraint, fragment)
            .commit()
    }

    private fun logout() {

        val builder = AlertDialog.Builder(this@NavigationActivity)
        builder.setTitle("Logout")
        builder.setMessage(R.string.logout_message)
            .setCancelable(false)
            .setPositiveButton("Logout") { _, _ ->
                // Delete selected note from database
                fragment?.logout()
                openActivity<LoginActivity>()
                finishAffinity()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()

        // Set the color of the positive button text
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // Show the dialog
        dialog.show()

    }

}