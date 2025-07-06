package com.yesvoters.android.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel

open class BaseViewModel(
    private val application: Application
) : AndroidViewModel(application)