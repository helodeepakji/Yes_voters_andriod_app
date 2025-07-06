package com.yesvoters.android.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.yesvoters.android.R
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.ActivityProfileBinding
import com.yesvoters.android.network.internet.InternetUtil
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.ui.base.BaseActivity
import com.yesvoters.android.ui.model.request.UpdateProfileRequest
import com.yesvoters.android.ui.model.response.LogoutResponse
import com.yesvoters.android.ui.model.response.UpdateProfileResponse
import com.yesvoters.android.ui.model.response.User
import com.yesvoters.android.ui.model.response.UserResponse
import com.yesvoters.android.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }

    lateinit var uri: Uri

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            this.uri = uri
            binding.imageProfile.setImageURI(uri)
            viewModel.callProfileApi(uriToFile(this, uri))
        } else {
            showToast(getString(R.string.no_image_selected))
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("upload_", ".png", context.cacheDir)
        tempFile.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
        return tempFile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val modifiedText = "Change Your Password %icon%" // you can use resource string here
        val span = SpannableString(modifiedText)
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.right_arrow_icon, null)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val image = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        val startIndex = modifiedText.indexOf("%icon%")

        span.setSpan(image, startIndex, startIndex + 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.tvChangePassword.setText(span)
        callUserDataApi()

        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnLogout.setOnClickListener {
            confirmLogout(
                onConfirm = { logoutUser() },
                onCancel = { showToast(getString(R.string.logout_cancelled)) }
            )
        }


        binding.tvChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.tvAddProfile.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.imageProfile.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.ibEditFullName.setOnClickListener {
            toggleEditState(binding.etFullNameValue, binding.ibEditFullName, "fullName")
        }

        binding.ibEditPhone.setOnClickListener {
            toggleEditState(binding.etPhoneValue, binding.ibEditPhone, "phone")
        }

        binding.ibEditEmail.setOnClickListener {
            toggleEditState(binding.etEmailValue, binding.ibEditEmail, "email")
        }

        binding.ibEditGender.setOnClickListener {
            toggleEditState(binding.etGenderValue, binding.ibEditGender, "gender")
        }

        binding.ibEditBio.setOnClickListener {
            toggleEditState(binding.etBioValue, binding.ibEditBio, "bio")
        }

    }

    private var currentlyEditingField: EditText? = null
    private var currentlyEditingButton: ImageButton? = null

    private fun toggleEditState(
        editText: EditText,
        editButton: ImageButton,
        key: String
    ) {
        val imm =
            editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val isEditing = editText.isFocusable && editText.isFocusableInTouchMode

        if (isEditing) {
            // Exit editing mode
            editText.apply {
                isFocusable = false
                isFocusableInTouchMode = false
                isClickable = false
                isCursorVisible = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
            editButton.setImageResource(R.drawable.edit_icon)
            currentlyEditingField = null
            currentlyEditingButton = null
            updateDataToServer()
        } else {
            // Disable previously editing field and reset icon
            currentlyEditingField?.apply {
                isFocusable = false
                isFocusableInTouchMode = false
                isClickable = false
                isCursorVisible = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
            currentlyEditingButton?.setImageResource(R.drawable.edit_icon)

            // Enable current editText
            editText.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                isClickable = true
                isCursorVisible = true
                setBackgroundResource(R.drawable.rounded_edittext)
                requestFocus()
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
            editButton.setImageResource(R.drawable.right_arrow_icon)

            // Save current as editing
            currentlyEditingField = editText
            currentlyEditingButton = editButton
        }
    }

    private fun callUserDataApi() {
        if (InternetUtil.isInternetAvailable(this)) {
            viewModel.callUserApi()
        } else {
            showToast(getString(R.string.please_check_your_internet_connection_try_again))
        }
    }

    private fun logoutUser() {
        if (InternetUtil.isInternetAvailable(this)) {
            viewModel.callLogoutApi()
        } else {
            showToast(getString(R.string.please_check_your_internet_connection_try_again))
        }
    }

    override fun setObservers() {
        viewModel.getUserResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleUserDataResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleUserDataError(response)
                }
            }
        }

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

        viewModel.getProfileResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleProfileResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleProfileError(response)
                }
            }
        }
        viewModel.getProfileImageResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleProfileResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleProfileError(response)
                }
            }
        }
    }

    private var originalUserData: User? = null

    private fun handleProfileResponse(response: UpdateProfileResponse?) {
        if (response?.success == true) {
            UserPreferences.saveUserData(response.user)
            originalUserData = response.user
        }
    }

    private fun handleUserDataError(response: UserResponse?) {
        showToast(getString(R.string.something_went_wrong_fetching_profile))
    }

    private fun handleProfileError(response: UpdateProfileResponse?) {
        showToast(response?.message ?: getString(R.string.profile_update_failed))
    }


    private fun handleLogoutResponse(response: LogoutResponse?) {
        if (response?.success == true) {
            UserPreferences.logoutUser()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } else {
            handleLogoutError(response)
        }
    }

    private fun handleLogoutError(response: LogoutResponse?) {
        response?.message?.let { showToast(it) }
    }

    private fun handleUserDataResponse(response: UserResponse?) {
        if (response?.success == true) {
            val user = response.user
            binding.etFullNameValue.setText(user.name)
            binding.etEmailValue.setText(user.email)
            binding.etPhoneValue.setText(user.phone)
            binding.etGenderValue.setText(user.gender)
            binding.etBioValue.setText(user.bio)

            val profileUrl = "http://213.210.37.77/storage/${user.profile}"
            Glide.with(this).load(profileUrl).into(binding.imageProfile)
        } else {
            handleUserDataError(response)
        }
    }


    private fun updateDataToServer() {
        viewModel.callProfileApi(getUpdateProfileRequest())
    }

    private fun getUpdateProfileRequest(): UpdateProfileRequest {
        return UpdateProfileRequest(
            name = binding.etFullNameValue.text.toString().takeIf { it.isNotBlank() },
            email = binding.etEmailValue.text.toString().takeIf { it.isNotBlank() },
            phone = binding.etPhoneValue.text.toString().takeIf { it.isNotBlank() },
            gender = binding.etGenderValue.text.toString().takeIf { it.isNotBlank() },
            bio = binding.etBioValue.text.toString().takeIf { it.isNotBlank() },
        )
    }

}
