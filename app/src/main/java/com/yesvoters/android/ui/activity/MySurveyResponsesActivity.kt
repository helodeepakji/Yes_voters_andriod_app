package com.yesvoters.android.ui.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesvoters.android.databinding.ActivityMySurveyResponsesBinding
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.ui.adaptor.SurveyResponseAdapter
import com.yesvoters.android.ui.base.BaseActivity
import com.yesvoters.android.ui.model.response.MyResponseApiResponse
import com.yesvoters.android.ui.viewModel.SurveyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySurveyResponsesActivity : BaseActivity() {

    private lateinit var binding: ActivityMySurveyResponsesBinding
    private var adapter: SurveyResponseAdapter? = null


    private val viewModel by lazy { ViewModelProvider(this)[SurveyViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySurveyResponsesBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.header.tvHeading.text = "My Survey Responses"

        getMySurveyResponse()

        binding.header.ibBack.setOnClickListener {
            finish()
        }

    }

    private fun getMySurveyResponse() {
        viewModel.callMyResponse()
    }

    override fun setObservers() {
        viewModel.getMyResponseResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleError(response)
                }
            }
        }
    }

    private fun handleError(response: MyResponseApiResponse?) {
        showToast(response?.message ?: "Something went wrong")
    }

    private fun handleResponse(response: MyResponseApiResponse?) {
        if (response?.success == true && response.data.isNotEmpty()) {
            adapter = SurveyResponseAdapter(this, response.data)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        } else {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        adapter?.releasePlayer()
    }

}