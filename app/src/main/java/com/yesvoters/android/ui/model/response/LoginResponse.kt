package com.yesvoters.android.ui.model.response

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: User
)
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val bio: String?,
    val profile: String?,
    val gender: String,
    val phone: String,
    val role_id: Int,
    val email_verified_at: String?,
    val created_at: String,
    val updated_at: String,
    val team_id: Int,
    val is_team_leader: Int
)

