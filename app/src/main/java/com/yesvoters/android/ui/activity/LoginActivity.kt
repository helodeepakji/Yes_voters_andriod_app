package com.yesvoters.android.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.yesvoters.android.R
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.ActivityLoginBinding
import com.yesvoters.android.network.internet.InternetUtil
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.ui.base.BaseActivity
import com.yesvoters.android.ui.model.request.LoginRequest
import com.yesvoters.android.ui.model.response.LoginResponse
import com.yesvoters.android.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }
    private var rememberMe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        setupTermsCheckbox()
        setupListeners()
    }

    private fun callLoginApi() {
        if (InternetUtil.isInternetAvailable(this)) {
            if (checkValidation()) {
                viewModel.callLoginApi(getLoginRequest())
            }
        } else {
            showToast(getString(R.string.please_check_your_internet_connection_try_again))
        }
    }

    private fun checkValidation(): Boolean {
        val emailValid = checkNull(binding.etEmail)
        val pwdValid = checkNull(binding.etPassword)
        if (!(emailValid)) {
            showToast(getString(R.string.please_enter_the_email))
            return false
        }
        if (!pwdValid) {
            showToast(getString(R.string.please_enter_the_password))
            return false
        }
        return true
    }

    override fun setObservers() {
        viewModel.getLoginResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> {
                    progressBarHideShow(true)
                }

                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleSignInResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleSignInError(response)
                }
            }
        }
    }

    private fun handleSignInResponse(response: LoginResponse?) {
        if (response == null) {
            showToast(getString(R.string.unexpected_error))
            return
        }
            if (response.success) {
            UserPreferences.saveUserData(response.user)
            UserPreferences.saveUserToken(response.token)
            checkLocationPermission()
        } else {
            handleSignInError(response)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LocationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun handleSignInError(response: LoginResponse?) {
        showToast(response?.message ?: getString(R.string.something_went_wrong))
    }

    private fun getLoginRequest(): LoginRequest {
        return LoginRequest().apply {
            email_or_phone = binding.etEmail.text.trim().toString()
            password = binding.etPassword.text.toString()
        }
    }

    private fun setupBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.trim()?.toString() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            rememberMe = binding.checkboxRemember.isChecked
            val agreedToTerms = binding.checkboxTerms.isChecked

            if (!agreedToTerms) {
                showToast("Please accept Terms and Privacy Policy")
                return@setOnClickListener
            }
            callLoginApi()
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
        }

        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-In clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkNull(editText: EditText): Boolean {
        return editText.text?.trim()?.isNotEmpty() == true
    }

    private fun setupTermsCheckbox() {
        val fullText =
            " By proceeding, you acknowledge that you have read and agree to our Terms and Privacy Policy."
        val spannable = SpannableString(fullText)

        val termsStart = fullText.indexOf("Terms")
        val termsEnd = fullText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "Terms clicked!", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.checkboxTerms.text = spannable
        binding.checkboxTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.checkboxTerms.highlightColor = Color.TRANSPARENT
    }
}
