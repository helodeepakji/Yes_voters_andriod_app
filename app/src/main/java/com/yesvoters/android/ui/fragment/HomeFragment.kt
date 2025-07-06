package com.yesvoters.android.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesvoters.android.R
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.FragmentHomeBinding
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.ui.activity.MySurveyResponsesActivity
import com.yesvoters.android.ui.activity.SurveyOnBoardingActivity
import com.yesvoters.android.ui.adaptor.MySurveyListAdapter
import com.yesvoters.android.ui.adaptor.SearchSurveyAdapter
import com.yesvoters.android.ui.base.BaseFragment
import com.yesvoters.android.ui.model.response.SurveyAssignment
import com.yesvoters.android.ui.model.response.SurveySearchResponse
import com.yesvoters.android.ui.model.response.SurveySummaryResponse
import com.yesvoters.android.ui.viewModel.SurveyViewModel
import com.yesvoters.android.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy { ViewModelProvider(this)[SurveyViewModel::class.java] }
    private lateinit var adapter: SearchSurveyAdapter
    private lateinit var mySurveyAdapter: MySurveyListAdapter
    private var surveyList = listOf<SurveyAssignment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView = binding.searchView
        observeKeyboardVisibility()

        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        val searchEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        val searchIcon =
            searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        val searchPlate =
            searchView.findViewById<LinearLayout>(androidx.appcompat.R.id.search_plate)

        searchIcon.setImageDrawable(null)

        searchEditText.compoundDrawablePadding = 12

        searchEditText.setBackgroundColor(Color.TRANSPARENT)
        searchEditText.setHintTextColor(Color.GRAY)
        searchEditText.setTextColor(Color.BLACK)

        searchPlate.setBackgroundColor(Color.TRANSPARENT)

        adapter = SearchSurveyAdapter(surveyList) { clickedSurvey ->
            val intent = Intent(requireContext(), SurveyOnBoardingActivity::class.java)
            intent.putExtra(AppConstants.ARG_CLICKED_SURVEY, clickedSurvey)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        getMySurveyList()
        callOtherDetails()
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.recyclerView.visibility = View.GONE
            }
        }

        binding.cvTotalSurvey.setOnClickListener {
            val intent = Intent(requireContext(), MySurveyResponsesActivity::class.java)
            startActivity(intent)
        }

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty()
                if (query.isBlank()) {
                    binding.recyclerView.visibility = View.GONE
                    adapter.updateList(emptyList())
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    search(query)
                }
                return true
            }
        })
    }

    private fun search(query: String) {
        viewModel.callSurveySearchApi(query)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeKeyboardVisibility() {
        val rootView = requireActivity().window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // If keyboard is hidden
            if (keypadHeight < screenHeight * 0.15) {
                binding.recyclerView.visibility = View.GONE
            }
        }
    }

    override fun setObservers() {
        viewModel.getSurveySearchResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> {
                    progressBarHideShow(true)
                }

                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleSearchResponse(response)
                }
                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleSearchInError(res.data)
                }
            }
        }

        viewModel.getMySurveyResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> {
                    progressBarHideShow(true)
                }

                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleMySurveyListResponse(response)
                }

                Status.ERROR -> {
                    progressBarHideShow(false)
                }
            }
        }

        viewModel.getOtherDetailsResponse().observe(this) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> {
                    progressBarHideShow(true)
                }

                Status.SUCCESS -> {
                    progressBarHideShow(false)
                    handleOtherDetailResponse(response)
                }
                Status.ERROR -> {
                    progressBarHideShow(false)
                    handleOtherDetailError(res.data)
                }
            }
        }

    }

    private fun handleOtherDetailResponse(response: SurveySummaryResponse?) {
        response?.takeIf { it.success }?.let {
            binding.tvTotalSurvey.text = "${it.data.today_response} Response"
            binding.tvSurveySubmitted.text = "${it.data.total_survey_complete} Surveys Submitted"
            binding.tvSurveyorPending.text = "${it.data.pending_survey_complete} Surveys Left"
        }
    }


    private fun handleOtherDetailError(data: SurveySummaryResponse?) {


    }

    private fun handleMySurveyListResponse(response: SurveySearchResponse?) {
        val mySurveyList = response?.data ?: emptyList()

        surveyList = mySurveyList

        mySurveyAdapter = MySurveyListAdapter(mySurveyList) { clickedSurvey ->
            val intent = Intent(requireContext(), SurveyOnBoardingActivity::class.java)
            intent.putExtra("clicked_survey", clickedSurvey)
            startActivity(intent)
        }

        binding.rvMySurveyList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mySurveyAdapter
            visibility = View.VISIBLE
        }
    }


    private fun handleSearchResponse(response: SurveySearchResponse?) {
        response?.data?.let {
            surveyList = it
            adapter.updateList(surveyList)
            binding.recyclerView.scrollToPosition(0)
        }
    }

    private fun handleSearchInError(response: SurveySearchResponse?) {
        surveyList = emptyList()
        adapter.updateList(surveyList)
        showToast(getString(R.string.something_went_wrong),)
    }

    private fun getMySurveyList() {
        viewModel.callMySurveyApi()
    }
    private fun callOtherDetails() {
        viewModel.callOtherDetails()
    }
}
