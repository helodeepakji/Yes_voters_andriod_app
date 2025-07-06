package com.yesvoters.android.ui.model.request

data class ChangePasswordRequest(
    var old_password: String="",
    var password: String="",
    var password_confirmation: String=""
)
