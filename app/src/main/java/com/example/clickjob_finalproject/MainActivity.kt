package com.example.clickjob_finalproject

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // The app background is light, so status bar icons (battery/wifi/clock)
        // need to be dark to stay visible - otherwise they default to light/white
        // and nearly disappear against a light background, like in your screenshot.
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // On Android 15+ (targetSdk 35+) the system enforces edge-to-edge and the line
        // above stops having effect, so we add the system bars' insets as padding
        // ourselves. This keeps things correct on every API level.
        //
        // Top/left/right go on the whole screen (so content clears the status bar).
        // Bottom goes on the bottom nav bar ITSELF, not on main_root - that way the
        // nav bar's own background fills the gesture-bar area instead of leaving a
        // stray strip of the screen's background color showing below it.
        val rootView = findViewById<android.view.View>(R.id.main_root)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            bottomNav.setPadding(
                bottomNav.paddingLeft,
                bottomNav.paddingTop,
                bottomNav.paddingRight,
                systemBars.bottom
            )
            insets
        }

        // Connect bottom navigation to nav controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.myJobsFragment -> {
                    navController.navigate(R.id.myJobsFragment)
                    true
                }
                R.id.scanFragment -> {
                    navController.navigate(R.id.scanFragment)
                    true
                }
                R.id.notificationsFragment -> {
                    navController.navigate(R.id.notificationsFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }
        // Observe global worker/employer mode and switch bottom nav color accordingly.
        // Pink = worker mode, teal = employer mode.
        appViewModel.isWorkerMode.observe(this) { isWorker ->
            val colorList = if (isWorker) {
                ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
            } else {
                ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color_teal)
            }
            bottomNav.itemIconTintList = colorList
            bottomNav.itemTextColor = colorList
        }
    }
    fun hideBottomNav() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = android.view.View.GONE
    }

    fun showBottomNav() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = android.view.View.VISIBLE
    }


}