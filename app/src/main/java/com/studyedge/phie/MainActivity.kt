package com.studyedge.phie

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.studyedge.phie.databinding.ActivityMainBinding
import com.studyedge.phie.ui.dialogs.ExitDialog
import com.studyedge.phie.ui.dialogs.SupportDialog
import com.studyedge.phie.ui.dialogs.UpdateDialog
import com.studyedge.phie.ui.fragment.CourseListFragment
import com.studyedge.phie.ui.fragment.FavoritesFragment
import com.studyedge.phie.ui.viewmodel.MainViewModel
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import android.view.Menu
import android.view.MenuInflater
import com.google.android.material.button.MaterialButton
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val viewModel: MainViewModel by viewModels()
    private var telegramUrl: String? = null
    private var supportEmail: String? = null
    private var supportWebsite: String? = null
    private var maintenanceView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupFavorites()
        setupObservers()
        setupBackNavigation()
        setupFragmentManager()
        setupClickListeners()
        
        // Show dashboard fragment by default
        if (savedInstanceState == null) {
            showDashboard()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupNavigationDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupFragmentManager() {
        supportFragmentManager.addOnBackStackChangedListener {
            val hasBackStack = supportFragmentManager.backStackEntryCount > 0
            binding.mainContent.visibility = if (hasBackStack) View.GONE else View.VISIBLE
            binding.fragmentContainer.visibility = if (hasBackStack) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.exploreCoursesButton.setOnClickListener {
            navigateToCourseList()
        }
        
        binding.pwStartLearningButton.setOnClickListener {
            navigateToCourseList()
        }
        
        binding.ntStartLearningButton.setOnClickListener {
            // Show a toast message for NT courses
            Toast.makeText(this, "NT Courses coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCourseList() {
        binding.mainContent.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, CourseListFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToFavorites() {
        binding.mainContent.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FavoritesFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    private fun setupFavorites() {
        binding.favoritesCard.setOnClickListener {
            navigateToFavorites()
        }
    }

    private fun setupObservers() {
        viewModel.appSettings.observe(this) { response ->
            response?.settings?.let { settings ->
                // Check for maintenance mode first
                if (settings.maintenance_mode) {
                    showMaintenanceMode(settings.support.telegram)
                    return@let
                }
                
                // Update app name in toolbar
                binding.welcomeText.text = settings.app_name

                // Update navigation header
                val headerView = binding.navigationView.getHeaderView(0)
                headerView?.let { header ->
                    // Update logo
                    Glide.with(this)
                        .load(settings.app_logo)
                        .placeholder(R.drawable.ic_logo)
                        .error(R.drawable.ic_logo)
                        .into(header.findViewById(R.id.navHeaderLogo))

                    // Update title and subtitle
                    header.findViewById<TextView>(R.id.navHeaderTitle).text = settings.app_name
                    header.findViewById<TextView>(R.id.navHeaderSubtitle).text = settings.description
                }

                // Store support information
                telegramUrl = settings.support.telegram
                supportEmail = settings.support.email
                supportWebsite = settings.support.website

                // Check for updates
                val latestVersion = settings.versions.maxByOrNull { it.version }?.version
                val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
                
                // Only show dialog if not on latest version
                if (latestVersion != null && latestVersion != currentVersion) {
                    UpdateDialog(
                        context = this,
                        forceUpdate = settings.force_update,
                        versions = settings.versions
                    ).show()
                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    supportFragmentManager.backStackEntryCount > 0 -> {
                        supportFragmentManager.popBackStack()
                        if (supportFragmentManager.backStackEntryCount == 1) {
                            binding.mainContent.visibility = View.VISIBLE
                            binding.fragmentContainer.visibility = View.GONE
                        }
                    }
                    else -> {
                        ExitDialog(this@MainActivity) {
                            finish()
                        }.show()
                    }
                }
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // Show the main dashboard content
                showDashboard()
            }
            R.id.nav_courses -> {
                // Navigate to CourseListFragment
                navigateToCourseList()
            }
            R.id.nav_favorites -> {
                // Navigate to FavoritesFragment
                navigateToFavorites()
            }
            R.id.nav_physics -> {
                Toast.makeText(this, "Physics", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_toppers -> {
                Toast.makeText(this, "Toppers", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_kaksha -> {
                Toast.makeText(this, "Kaksha", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_telegram -> {
                telegramUrl?.let { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            R.id.nav_stream -> {
                Toast.makeText(this, "Stream", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_support -> {
                showSupportDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showSupportDialog() {
        val dialog = SupportDialog(
            context = this,
            email = supportEmail ?: "",
            website = supportWebsite ?: "",
            telegram = telegramUrl ?: ""
        )
        dialog.show()
    }

    private fun showDashboard() {
        // Clear the back stack to ensure we're at the root
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        
        // Show the main content
        binding.mainContent.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        
        // Update the welcome text
        binding.welcomeText.text = getString(R.string.hello_baccho)
    }

    private fun showMaintenanceMode(telegramUrl: String?) {
        // Hide all normal content
        binding.mainContent.visibility = View.GONE
        binding.fragmentContainer.visibility = View.GONE
        
        // Inflate maintenance layout if not already inflated
        if (maintenanceView == null) {
            maintenanceView = layoutInflater.inflate(R.layout.layout_maintenance, binding.drawerLayout, false)
            binding.drawerLayout.addView(maintenanceView)
            
            // Set up Telegram button
            maintenanceView?.findViewById<MaterialButton>(R.id.telegramButton)?.setOnClickListener {
                telegramUrl?.let { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        } else {
            maintenanceView?.visibility = View.VISIBLE
        }
        
        // Disable drawer navigation
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun hideMaintenanceMode() {
        maintenanceView?.visibility = View.GONE
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}