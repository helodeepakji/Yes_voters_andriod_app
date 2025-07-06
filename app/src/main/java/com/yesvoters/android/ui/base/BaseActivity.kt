package com.yesvoters.android.ui.base

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.yesvoters.android.R
import com.yesvoters.android.utils.DialogUtil

open class BaseActivity : AppCompatActivity(), BaseInterface, LifecycleOwner {

    private val progressDialog by lazy { DialogUtil.progressBarHideShow(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor()
        initView()
        setClickListeners()
        setObservers()
    }

    override fun initView() {}

    override fun setClickListeners() {}

    override fun setObservers() {}

    override fun progressBarHideShow(isVisible: Boolean) {
        if (isVisible) {
            if (!progressDialog.isShowing) progressDialog.show()
        } else {
            if (progressDialog.isShowing) progressDialog.dismiss()
        }
    }

    private fun setStatusBarColor() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context, R.color.app_status_bar_color)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun setStatusBarColor(color: Int) {
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = ContextCompat.getColor(this@BaseActivity, color)
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun confirmLogout(
        onConfirm: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        DialogUtil.showLogoutConfirmationDialog(
            context = this,
            message = getString(R.string.logout_confirmation_message),
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }

}
