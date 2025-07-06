package com.yesvoters.android.ui.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.yesvoters.android.utils.DialogUtil


open class BaseFragment : Fragment(), BaseInterface {

    private val progressDialog by lazy { context?.let { DialogUtil.progressBarHideShow(it) } }

    private val TAG = BaseFragment::class.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setClickListeners()
        setObservers()
    }

    override fun initView() {}

    override fun setClickListeners() {}

    override fun progressBarHideShow(isVisible: Boolean) {
        if (activity == null || !isAdded || requireActivity().isFinishing) return

        when (isVisible) {
            true -> {
                if (progressDialog?.isShowing == false) {
                    try {
                        progressDialog?.show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing progressDialog: ${e.localizedMessage}")
                    }
                }
            }

            false -> {
                try {
                    progressDialog?.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error dismissing progressDialog: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun setObservers() {}

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(
            TAG, "onAttach: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        Log.e(
            TAG, "onStart: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.e(
            TAG, "onResume: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    override fun onPause() {
        super.onPause()
        Log.e(
            TAG, "onPause: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    override fun onStop() {
        super.onStop()
        Log.e(
            TAG, "onStop: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    override fun onDestroyView() {
        progressDialog?.dismiss()
        super.onDestroyView()
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e(
            TAG,
            "onDestroy: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(
            TAG, "onDetach: ${parentFragmentManager.fragments.firstOrNull()?.javaClass?.simpleName}"
        )
    }

    fun surveySubmitted() {
        context?.let {
            DialogUtil.showSurveySubmittedDialog(
                context = it
            )
        }
    }

}