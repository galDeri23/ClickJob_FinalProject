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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()
    private var notificationsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

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
        //SeedUsers.seedAll()
        //SeedData.seedAll()
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

        // Setup notifications badge
        setupNotificationsBadge(bottomNav)

        // Observe global worker/employer mode and switch bottom nav color accordingly
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

    // Listen to unread notifications and show/hide badge on notifications icon
    private fun setupNotificationsBadge(bottomNav: BottomNavigationView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        notificationsListener = FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { documents, _ ->
                val unreadCount = documents?.size() ?: 0
                if (unreadCount > 0) {
                    val badge = bottomNav.getOrCreateBadge(R.id.notificationsFragment)
                    badge.isVisible = true
                    badge.number = unreadCount
                    badge.backgroundColor = ContextCompat.getColor(this, R.color.brand_pink)
                } else {
                    bottomNav.removeBadge(R.id.notificationsFragment)
                }
            }
    }

    fun hideBottomNav() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = android.view.View.GONE
    }

    fun showBottomNav() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = android.view.View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationsListener?.remove()
    }
}