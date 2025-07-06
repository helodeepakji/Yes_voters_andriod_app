package com.yesvoters.android.ui.model.request

data class UpdateProfileRequest(
    var name: String? = null,
    var email: String? = null,
    var bio: String? = null,
    var gender: String? = null,
    var phone: String? = null,
)
