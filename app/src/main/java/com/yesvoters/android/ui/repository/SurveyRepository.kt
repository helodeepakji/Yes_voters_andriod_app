package com.yesvoters.android.ui.repository

import android.app.Application
import com.yesvoters.android.BuildConfig
import com.yesvoters.android.network.remote.ApiService
import com.yesvoters.android.network.remote.NetworkModule
import com.yesvoters.android.ui.model.response.MyResponseApiResponse
import com.yesvoters.android.ui.model.response.SurveyDetailsResponse
import com.yesvoters.android.ui.model.response.SurveyQuestionResponse
import com.yesvoters.android.ui.model.response.SurveySearchResponse
import com.yesvoters.android.ui.model.response.SurveyStoreResponse
import com.yesvoters.android.ui.model.response.SurveySummaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class SurveyRepository @Inject constructor(
    private val apiService: ApiService
) {


    suspend fun surveySearchApi(query: String): Response<SurveySearchResponse> {
        return apiService.callSurveySearchApi(query)
    }

    suspend fun surveyDetailsApi(id: Int): Response<SurveyDetailsResponse> {
        return apiService.callSurveyDetailsApi(surveyId = id)
    }

    suspend fun mySurveyApi(): Response<SurveySearchResponse> {
        return apiService.callMySurveyApi()
    }

    suspend fun surveyQuestionApi(id:Int): Response<SurveyQuestionResponse> {
        return apiService.callSurveyQuestionApi(id)
    }

    suspend fun storeResponseApi(
        partMap: Map<String, RequestBody>,
        audioFile: MultipartBody.Part
    ): Response<SurveyStoreResponse> {
        return apiService.callStoreResponseApi(partMap = partMap, audioFile = audioFile)
    }

    suspend fun myResponse(): Response<MyResponseApiResponse> {
        return apiService.callMyResponse()
    }

    suspend fun otherDetails(): Response<SurveySummaryResponse> {
        return apiService.callOtherDetails()
    }
}
