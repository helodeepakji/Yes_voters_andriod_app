package com.yesvoters.android.ui.model.response

data class SurveyQuestionResponse(
    val success: Boolean,
    val data: List<SurveyQuestion>,
    val message: String
)

data class SurveyQuestion(
    val id: Int,
    val survey_id: Int,
    val question: String,
    val type: String, // "radio", "select", "text", "checkbox"
    val options: List<String>,
    val created_at: String,
    val updated_at: String
)
