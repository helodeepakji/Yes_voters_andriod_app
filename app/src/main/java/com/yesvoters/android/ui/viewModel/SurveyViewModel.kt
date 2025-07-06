package com.yesvoters.android.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yesvoters.android.network.remote.Resource
import com.yesvoters.android.ui.model.response.MyResponseApiResponse
import com.yesvoters.android.ui.model.response.SurveyDetailsResponse
import com.yesvoters.android.ui.model.response.SurveyQuestionResponse
import com.yesvoters.android.ui.model.response.SurveySearchResponse
import com.yesvoters.android.ui.model.response.SurveyStoreResponse
import com.yesvoters.android.ui.model.response.SurveySummaryResponse
import com.yesvoters.android.ui.repository.SurveyRepository
import com.yesvoters.android.utils.ApiConstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import retrofit2.Response

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val surveyRepository: SurveyRepository
) : ViewModel() {

    private val surveySearchLiveData = MutableLiveData<Resource<SurveySearchResponse>>()
    fun getSurveySearchResponse(): LiveData<Resource<SurveySearchResponse>> = surveySearchLiveData

    private val surveyDetailLiveData = MutableLiveData<Resource<SurveyDetailsResponse>>()
    fun getSurveyDetailResponse(): LiveData<Resource<SurveyDetailsResponse>> = surveyDetailLiveData

    private val mySurveyLiveData = MutableLiveData<Resource<SurveySearchResponse>>()
    fun getMySurveyResponse(): LiveData<Resource<SurveySearchResponse>> = mySurveyLiveData

    private val surveyQuestionLiveData = MutableLiveData<Resource<SurveyQuestionResponse>>()
    fun getSurveyQuestionResponse(): LiveData<Resource<SurveyQuestionResponse>> = surveyQuestionLiveData

    private val storeResponseLiveData = MutableLiveData<Resource<SurveyStoreResponse>>()
    fun getStoreResponseResponse(): LiveData<Resource<SurveyStoreResponse>> = storeResponseLiveData

    private val myResponseLiveData = MutableLiveData<Resource<MyResponseApiResponse>>()
    fun getMyResponseResponse(): LiveData<Resource<MyResponseApiResponse>> = myResponseLiveData

    private val otherDetailsLiveData = MutableLiveData<Resource<SurveySummaryResponse>>()
    fun getOtherDetailsResponse(): LiveData<Resource<SurveySummaryResponse>> = otherDetailsLiveData

    private fun <T> launchJob(
        liveData: MutableLiveData<Resource<T>>,
        apiCall: suspend () -> Response<T>
    ) {
        liveData.value = Resource.loading(null, 0)
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) { apiCall() }
            liveData.value = if (response.code() == ApiConstant.CODE_200) {
                Resource.success(response.body(), response.code())
            } else {
                Resource.error(response.message(), null, response.code())
            }
        }
    }

    fun callSurveySearchApi(query: String) =
        launchJob(surveySearchLiveData) { surveyRepository.surveySearchApi(query) }

    fun callSurveyDetailsApi(id: Int) =
        launchJob(surveyDetailLiveData) { surveyRepository.surveyDetailsApi(id) }

    fun callMySurveyApi() =
        launchJob(mySurveyLiveData) { surveyRepository.mySurveyApi() }

    fun callSurveyQuestionApi(id:Int) =
        launchJob(surveyQuestionLiveData) { surveyRepository.surveyQuestionApi(id) }

    fun callStoreResponseApi(map: Map<String, RequestBody>, audioFile: MultipartBody.Part) =
        launchJob(storeResponseLiveData) { surveyRepository.storeResponseApi(map, audioFile) }

    fun callMyResponse() =
        launchJob(myResponseLiveData) { surveyRepository.myResponse() }

    fun callOtherDetails() =
        launchJob(otherDetailsLiveData) { surveyRepository.otherDetails() }
}
