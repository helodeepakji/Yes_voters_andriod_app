package com.yesvoters.android.network.remote

import com.yesvoters.android.ui.model.request.ChangePasswordRequest
import com.yesvoters.android.ui.model.request.LoginRequest
import com.yesvoters.android.ui.model.request.UpdateProfileRequest
import com.yesvoters.android.ui.model.response.*
import com.yesvoters.android.utils.ApiConstant
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST(ApiConstant.LOGIN)
    suspend fun callLoginApi(@Body request: LoginRequest): Response<LoginResponse>

    @GET(ApiConstant.USER)
    suspend fun calUserApi(): Response<UserResponse>

    @POST(ApiConstant.LOGOUT)
    suspend fun calLogoutApi(): Response<LogoutResponse>

    @POST(ApiConstant.CHANGE_PASSWORD)
    suspend fun callChangePasswordApi(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @POST(ApiConstant.PROFILE)
    suspend fun callProfileApi(
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @Multipart
    @POST(ApiConstant.PROFILE)
    suspend fun callProfileApi(
        @Part profile: MultipartBody.Part
    ): Response<UpdateProfileResponse>

    @GET("survey-search/{query}")
    suspend fun callSurveySearchApi(
        @Path("query") query: String
    ): Response<SurveySearchResponse>

    @GET("survey-details/{surveyId}")
    suspend fun callSurveyDetailsApi(
        @Path("surveyId") surveyId: Int
    ): Response<SurveyDetailsResponse>

    @GET(ApiConstant.MY_SURVEY)
    suspend fun callMySurveyApi(): Response<SurveySearchResponse>

    @GET("survey-question/{surveyId}")
    suspend fun callSurveyQuestionApi( @Path("surveyId") surveyId: Int): Response<SurveyQuestionResponse>

    @Multipart
    @POST(ApiConstant.STORE_RESPONSE)
    suspend fun callStoreResponseApi(
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part audioFile: MultipartBody.Part
    ): Response<SurveyStoreResponse>

    @GET(ApiConstant.MY_RESPONSE)
    suspend fun callMyResponse(): Response<MyResponseApiResponse>

    @GET("other-details")
    suspend fun callOtherDetails(): Response<SurveySummaryResponse>
}
