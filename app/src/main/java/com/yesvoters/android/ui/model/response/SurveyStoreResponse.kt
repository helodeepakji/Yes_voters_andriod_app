package com.yesvoters.android.ui.model.response

class SurveyStoreResponse(
    val success: Boolean,
    val message: String,
    val data: SurveyResponseData?
)

data class SurveyResponseData(
    val survey_id: String,
    val name: String,
    val location: String, // You can parse this JSON string separately if needed
    val audio_file: String,
    val father_name: String,
    val address: String,
    val state: String,
    val city: String,
    val block: String,
    val pincode: String,
    val user_id: Int,
    val created_at: String,
    val updated_at: String,
    val id: Int,
    val answers: List<SurveyAnswer>
)

data class SurveyAnswer(
    val id: Int,
    val survey_response_id: Int,
    val question: String,
    val answer: String,
    val ai_answer: String,
    val created_at: String,
    val updated_at: String
)