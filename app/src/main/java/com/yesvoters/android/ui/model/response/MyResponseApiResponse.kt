package com.yesvoters.android.ui.model.response

data class MyResponseApiResponse(
    val success: Boolean,
    val message: String,
    val data: List<MySurveyResponseData>
)
data class MySurveyResponseData(
    val id: Int,
    val survey_id: Int,
    val user_id: Int,
    val location: String, // JSON string: can be parsed separately if needed
    val audio_file: String,
    val verification: String,
    val verified_by: Any?, // null or maybe Int/String in future
    val created_at: String,
    val updated_at: String,
    val name: String,
    val father_name: String,
    val address: String,
    val state: String,
    val city: String,
    val block: String,
    val pincode: String,
    val survey: SurveyInfo,
    val answers: List<MySurveyAnswer>
)

data class SurveyInfo(
    val id: Int,
    val title: String,
    val description: String,
    val address: String,
    val recording_time: Int,
    val is_active: Int,
    val created_at: String,
    val updated_at: String
)

data class MySurveyAnswer(
    val id: Int,
    val survey_response_id: Int,
    val question: String,
    val answer: String,
    val ai_answer: String,
    val created_at: String,
    val updated_at: String
)

data class LatLngModel(
    val lat: String,
    val lng: String
)
