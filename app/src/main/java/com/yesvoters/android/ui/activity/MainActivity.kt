package com.yesvoters.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.yesvoters.android.BuildConfig
import com.yesvoters.android.R
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.ActivityMainBinding
import com.yesvoters.android.network.internet.InternetUtil
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.ui.base.BaseActivity
import com.yesvoters.android.ui.fragment.HomeFragment
import com.yesvoters.android.ui.model.response.LogoutResponse
import com.yesvoters.android.ui.viewModel.AuthViewModel
import com.yesvoters.android.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavHeader()
        Glide.with(this)
            .load(AppConstants.LOGO_URL)
            .into(binding.ivLogo)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()

        binding.iconDrawerToggle.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.END)

            binding.drawerLayout.postDelayed({
                when (menuItem.itemId) {
                    R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                    R.id.nav_surveys -> startActivity(Intent(this, MySurveyResponsesActivity::class.java))
                    R.id.nav_log_out -> confirmLogout(
                        onConfirm = { logoutUser() },
                        onCancel = { showToast(getString(R.string.logout_cancelled)) }
                    )
                }
            }, 200) // delay for smooth closing animation
            true
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        setNavHeader()
    }

    override fun setObservers() {
        viewModel.getLogoutResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleLogoutResponse(response)
                }
                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleLogoutError(response)
                }
            }
        }
    }

    private fun handleLogoutResponse(response: LogoutResponse?) {
        if (response?.success == true) {
            UserPreferences.logoutUser()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } else {
            handleLogoutError(response)
        }
    }

    private fun handleLogoutError(response: LogoutResponse?) {
        showToast(response?.message ?: getString(R.string.logout_failed))
    }

    private fun logoutUser() {
        if (InternetUtil.isInternetAvailable(this)) {
            viewModel.callLogoutApi()
        } else {
            showToast(getString(R.string.please_check_your_internet_connection_try_again))
        }
    }

    private fun setNavHeader() {
        val header = binding.navView.getHeaderView(0) ?: return

        val ivProfile = header.findViewById<ShapeableImageView>(R.id.imageProfile)
        val tvUserName = header.findViewById<TextView>(R.id.tvUserName)

        tvUserName.text = UserPreferences.getFullName() ?: getString(R.string.guest_user)
        val profilePath = UserPreferences.getProfile()
        val profileUrl = if (profilePath.isNotEmpty()) {
            "http://213.210.37.77/storage/$profilePath"
        } else {
            null
        }
        Glide.with(this)
            .load(profileUrl ?: R.drawable.default_image_vertical)
            .into(ivProfile)

    }


}
