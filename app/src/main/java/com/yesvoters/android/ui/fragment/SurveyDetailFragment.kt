package com.yesvoters.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yesvoters.android.R
import com.yesvoters.android.databinding.FragmentSurveyDetailBinding
import com.yesvoters.android.ui.base.BaseFragment
import com.yesvoters.android.ui.model.response.SurveyAssignment
import com.yesvoters.android.ui.model.response.SurveyData
import com.yesvoters.android.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SurveyDetailFragment : BaseFragment() {

    private var _binding: FragmentSurveyDetailBinding? = null
    private val binding get() = _binding!!
    private var surveyAssignment: SurveyAssignment? = null
    private var surveyData: SurveyData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyDetailBinding.inflate(inflater, container, false)


        surveyAssignment = arguments?.getParcelable(AppConstants.ARG_CLICKED_SURVEY)
        surveyData = arguments?.getParcelable(AppConstants.ARG_SURVEY_DETAIL_RESPONSE)

//        binding.etYourName.setText("Dummy Name")
//        binding.etYourFatherName.setText("Dummy Father")
//        binding.etArea.setText("Dummy Area")
//        binding.etBlock.setText("Block A")
//        binding.etCity.setText("Dummy City")
//        binding.etState.setText("Dummy State")
//        binding.etPostalCode.setText("123456")


        binding.btnNext.setOnClickListener {
            if (validateFields()) {
                val bundle = Bundle().apply {
                    putParcelable(AppConstants.ARG_CLICKED_SURVEY, surveyAssignment)
                    putParcelable(AppConstants.ARG_SURVEY_DETAIL_RESPONSE, surveyData)
                    putString(AppConstants.ARG_NAME, binding.etYourName.text.toString().trim())
                    putString(AppConstants.ARG_FATHER_NAME, binding.etYourFatherName.text.toString().trim())
                    putString(AppConstants.ARG_AREA, binding.etArea.text.toString().trim())
                    putString(AppConstants.ARG_BLOCK, binding.etBlock.text.toString().trim())
                    putString(AppConstants.ARG_CITY, binding.etCity.text.toString().trim())
                    putString(AppConstants.ARG_STATE, binding.etState.text.toString().trim())
                    putString(AppConstants.ARG_POSTAL_CODE, binding.etPostalCode.text.toString().trim())
                }

                val fragment = SurveyQuestionsFragment()
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(
                        (requireActivity() as com.yesvoters.android.ui.activity.SurveyOnBoardingActivity).binding.fragmentContainer.id,
                        fragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }


        binding.header.ibBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    private fun validateFields(): Boolean {
        val name = binding.etYourName.text.toString().trim()
        val fatherName = binding.etYourFatherName.text.toString().trim()
        val area = binding.etArea.text.toString().trim()
        val block = binding.etBlock.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val state = binding.etState.text.toString().trim()
        val postalCode = binding.etPostalCode.text.toString().trim()

        return when {
            name.isEmpty() -> {
                showToast(getString(R.string.enter_your_name))
                false
            }

            fatherName.isEmpty() -> {
                showToast(getString(R.string.enter_father_name))
                false
            }

            area.isEmpty() -> {
                showToast(getString(R.string.enter_area))
                false
            }

            block.isEmpty() -> {
                showToast(getString(R.string.enter_block))
                false
            }

            city.isEmpty() -> {
                showToast(getString(R.string.enter_city))
                false
            }

            state.isEmpty() -> {
                showToast(getString(R.string.enter_state))
                false
            }

            postalCode.isEmpty() -> {
                showToast(getString(R.string.enter_postal_code))
                false
            }

            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
