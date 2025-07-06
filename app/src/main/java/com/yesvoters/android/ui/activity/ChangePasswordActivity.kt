package com.yesvoters.android.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.yesvoters.android.R
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.ActivityChangePasswordBinding
import com.yesvoters.android.network.internet.InternetUtil
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.ui.base.BaseActivity
import com.yesvoters.android.ui.model.request.ChangePasswordRequest
import com.yesvoters.android.ui.model.response.ChangePasswordResponse
import com.yesvoters.android.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibBack.setOnClickListener {
            finish()
        }

        binding.btnChangePassword.setOnClickListener {
            val oldPassword = binding.etOldPassword.text.toString().trim()
            val newPassword = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConformPassword.text.toString().trim()

            when {
                oldPassword.isEmpty() -> {
                    showToast("Please enter old password")
                    binding.etOldPassword.requestFocus()
                }

                newPassword.isEmpty() -> {
                    showToast("Please enter new password")
                    binding.etPassword.requestFocus()
                }

                newPassword.length < 6 -> {
                    showToast("Password must be at least 6 characters")
                    binding.etPassword.requestFocus()
                }

                confirmPassword.isEmpty() -> {
                    showToast("Please confirm your password")
                    binding.etConformPassword.requestFocus()
                }

                newPassword != confirmPassword -> {
                    showToast("Passwords do not match")
                    binding.etConformPassword.requestFocus()
                }

                else -> {
                    changePasswordApi()
                }
            }
        }

    }

    private fun changePasswordApi() {
        val token = UserPreferences.getUserToken()
        if (token.isEmpty()) {
            showToast("Session expired. Please login again.")
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        if (InternetUtil.isInternetAvailable(this)) {
            viewModel.callChangePasswordApi(getChangePasswordRequest())
        } else {
            showToast(getString(R.string.please_check_your_internet_connection_try_again))
        }
    }


    override fun setObservers() {
        viewModel.getChangePasswordResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> {
                    progressBarHideShow(true)
                }

                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleChangePasswordResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleChangePasswordError(response)
                }
            }
        }

    }

    private fun getChangePasswordRequest(): ChangePasswordRequest {
        return ChangePasswordRequest().apply {
            old_password = binding.etOldPassword.text.toString()
            password = binding.etPassword.text.toString()
            password_confirmation = binding.etConformPassword.text.toString()
        }
    }

    private fun handleChangePasswordResponse(response: ChangePasswordResponse?) {
        if (response?.success == true) {
            UserPreferences.logoutUser()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            handleChangePasswordError(response)
        }
    }

    private fun handleChangePasswordError(response: ChangePasswordResponse?) {
        showToast(response?.message ?: getString(R.string.something_went_wrong))
    }


}
