package com.yesvoters.android.ui.model.response

data class SurveySummaryResponse(
    val success: Boolean,
    val data: SurveyDataSummary,
    val message: String
)

data class SurveyDataSummary(
    val total_survey_complete: Int,
    val pending_survey_complete: Int,
    val today_response: Int
)
