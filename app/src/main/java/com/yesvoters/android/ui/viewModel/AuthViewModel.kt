package com.yesvoters.android.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yesvoters.android.network.remote.Resource
import com.yesvoters.android.ui.model.request.ChangePasswordRequest
import com.yesvoters.android.ui.model.request.LoginRequest
import com.yesvoters.android.ui.model.request.UpdateProfileRequest
import com.yesvoters.android.ui.model.response.ChangePasswordResponse
import com.yesvoters.android.ui.model.response.LoginResponse
import com.yesvoters.android.ui.model.response.LogoutResponse
import com.yesvoters.android.ui.model.response.UpdateProfileResponse
import com.yesvoters.android.ui.model.response.UserResponse
import com.yesvoters.android.ui.repository.AuthRepository
import com.yesvoters.android.utils.ApiConstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val loginLiveData = MutableLiveData<Resource<LoginResponse>>()
    fun getLoginResponse(): LiveData<Resource<LoginResponse>> = loginLiveData

    private val userLiveData = MutableLiveData<Resource<UserResponse>>()
    fun getUserResponse(): LiveData<Resource<UserResponse>> = userLiveData

    private val logoutLiveData = MutableLiveData<Resource<LogoutResponse>>()
    fun getLogoutResponse(): LiveData<Resource<LogoutResponse>> = logoutLiveData

    private val changePasswordLiveData = MutableLiveData<Resource<ChangePasswordResponse>>()
    fun getChangePasswordResponse(): LiveData<Resource<ChangePasswordResponse>> = changePasswordLiveData

    private val profileLiveData = MutableLiveData<Resource<UpdateProfileResponse>>()
    fun getProfileResponse(): LiveData<Resource<UpdateProfileResponse>> = profileLiveData

    private val profileImageLiveData = MutableLiveData<Resource<UpdateProfileResponse>>()
    fun getProfileImageResponse(): LiveData<Resource<UpdateProfileResponse>> = profileImageLiveData

    // --- API Call Functions ---

    fun callLoginApi(request: LoginRequest) =
        launchJob(loginLiveData) { authRepository.loginApi(request) }

    fun callUserApi() =
        launchJob(userLiveData) { authRepository.userApi() }

    fun callLogoutApi() =
        launchJob(logoutLiveData) { authRepository.logoutApi() }

    fun callChangePasswordApi(request: ChangePasswordRequest) =
        launchJob(changePasswordLiveData) { authRepository.changePasswordApi(request) }

    fun callProfileApi(request: UpdateProfileRequest) =
        launchJob(profileLiveData) { authRepository.updateProfile(request) }

    fun callProfileApi(file: File) {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("profile", file.name, requestFile)

        launchJob(profileImageLiveData) {
            authRepository.updateProfile(imagePart)
        }
    }

    // --- Shared Coroutine Launcher ---

    private fun <T> launchJob(
        liveData: MutableLiveData<Resource<T>>,
        apiCall: suspend () -> retrofit2.Response<T>
    ) {
        liveData.value = Resource.loading(null, 0)
        viewModelScope.launch(Dispatchers.IO) {
            val response = apiCall()
            withContext(Dispatchers.Main) {
                liveData.value = if (response.code() == ApiConstant.CODE_200) {
                    Resource.success(response.body(), response.code())
                } else {
                    Resource.error(response.message(), null, response.code())
                }
            }
        }
    }
}
