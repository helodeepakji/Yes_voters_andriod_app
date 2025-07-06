package com.yesvoters.android.ui.model

import android.widget.EditText
import android.widget.ImageButton

data class EditableField(
    val editText: EditText,
    val editButton: ImageButton,
    val key: String
)
