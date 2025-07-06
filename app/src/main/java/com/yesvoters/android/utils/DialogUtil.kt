package com.yesvoters.android.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.yesvoters.android.R


object DialogUtil {

    var dialog: AlertDialog? = null
    fun progressBarHideShow(context: Context): AlertDialog {
        // Create a custom layout for the progress dialog
        val progressDialogView = LayoutInflater.from(context)
            .inflate(R.layout.progress_dialog_layout, null)

        // Create an AlertDialog with the custom layout
        val dialogBuilder =
            AlertDialog.Builder(context).setView(progressDialogView).setCancelable(false)

        // Create and show the AlertDialog
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        return dialog
    }

    fun getMyDialog(): AlertDialog? {
        return dialog
    }

    fun showLogoutConfirmationDialog(
        context: Context,
        message: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_logout_confirmation, null)
        val dialogBuilder = AlertDialog.Builder(context).setView(view).setCancelable(false)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnYes = view.findViewById<Button>(R.id.btnLogout)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message
        btnYes.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
            onCancel()
        }
        dialog.show()
    }

    fun showSurveySubmittedDialog(
        context: Context,
        ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_survey_submitted, null)

        val dialogBuilder = AlertDialog.Builder(context).setView(view).setCancelable(false)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

}







