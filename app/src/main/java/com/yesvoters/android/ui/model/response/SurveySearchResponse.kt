package com.yesvoters.android.ui.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class SurveySearchResponse(
    val success: Boolean,
    val data: List<SurveyAssignment>,
    val message: String
)

@Parcelize
data class SurveyAssignment(
    val id: Int,
    val user_id: Int,
    val survey_id: Int,
    val assigned_by: Int,
    val created_at: String,
    val updated_at: String,
    val assigner: Assigner,
    val survey: Survey
) : Parcelable

@Parcelize
data class Assigner(
    val id: Int,
    val name: String,
    val email: String,
    val bio: String?,
    val profile: String?,
    val gender: String?,
    val phone: String?,
    val role_id: Int,
    val email_verified_at: String?,
    val created_at: String,
    val updated_at: String,
    val team_id: Int?,
    val is_team_leader: Int
) : Parcelable

@Parcelize
data class Survey(
    val id: Int,
    val title: String,
    val description: String,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val address: String,
    val recording_time: Int?,
    val my_response_count: Int
) : Parcelable
