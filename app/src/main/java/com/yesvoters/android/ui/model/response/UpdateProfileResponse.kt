package com.yesvoters.android.ui.model.response

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val user: User
)