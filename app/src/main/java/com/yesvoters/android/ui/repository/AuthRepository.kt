package com.yesvoters.android.ui.repository

import com.yesvoters.android.network.remote.ApiService
import com.yesvoters.android.ui.model.request.ChangePasswordRequest
import com.yesvoters.android.ui.model.request.LoginRequest
import com.yesvoters.android.ui.model.request.UpdateProfileRequest
import com.yesvoters.android.ui.model.response.ChangePasswordResponse
import com.yesvoters.android.ui.model.response.LoginResponse
import com.yesvoters.android.ui.model.response.LogoutResponse
import com.yesvoters.android.ui.model.response.UpdateProfileResponse
import com.yesvoters.android.ui.model.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(private val apiService: ApiService) {


    suspend fun loginApi(request: LoginRequest): Response<LoginResponse> {
        return apiService.callLoginApi(request)
    }

    suspend fun userApi(): Response<UserResponse> {
        return apiService.calUserApi()
    }

    suspend fun logoutApi(): Response<LogoutResponse> {
        return apiService.calLogoutApi()
    }

    suspend fun changePasswordApi(request: ChangePasswordRequest): Response<ChangePasswordResponse> {
        return apiService.callChangePasswordApi(request)
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Response<UpdateProfileResponse> {
        return apiService.callProfileApi(request)
    }

    suspend fun updateProfile(profile: MultipartBody.Part): Response<UpdateProfileResponse> {
        return apiService.callProfileApi(profile)
    }
}
