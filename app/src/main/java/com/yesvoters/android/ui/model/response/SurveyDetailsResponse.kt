package com.yesvoters.android.ui.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class SurveyDetailsResponse(
    val success: Boolean,
    val data: SurveyData,
    val message: String
)

@Parcelize
data class SurveyData(
    val id: Int,
    val title: String,
    val description: String,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val questions: List<Question>
): Parcelable

@Parcelize
data class Question(
    val id: Int,
    val survey_id: Int,
    val question: String,
    val type: String,
    val options: String?, // Can be null for text type
    val created_at: String,
    val updated_at: String
): Parcelable
