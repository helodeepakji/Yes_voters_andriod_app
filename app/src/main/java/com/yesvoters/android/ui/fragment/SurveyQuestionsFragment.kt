package com.yesvoters.android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesvoters.android.database.AppSharedPreferences
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.FragmentSurveyQuestionsBinding
import com.yesvoters.android.extentions.expandFully
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.services.AudioRecorderService
import com.yesvoters.android.ui.adaptor.QuestionAdapter
import com.yesvoters.android.ui.base.BaseFragment
import com.yesvoters.android.ui.model.response.SurveyAssignment
import com.yesvoters.android.ui.model.response.SurveyData
import com.yesvoters.android.ui.model.response.SurveyQuestionResponse
import com.yesvoters.android.ui.model.response.SurveyStoreResponse
import com.yesvoters.android.ui.viewModel.SurveyViewModel
import com.yesvoters.android.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class SurveyQuestionsFragment : BaseFragment() {

    private var _binding: FragmentSurveyQuestionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy { ViewModelProvider(this)[SurveyViewModel::class.java] }
    private var adapter: QuestionAdapter? = null

    private var name: String = ""
    private var fatherName: String = ""
    private var area: String = ""
    private var block: String = ""
    private var city: String = ""
    private var state: String = ""
    private var postalCode: String = ""
    private var surveyAssignment: SurveyAssignment? = null
    private var surveyData: SurveyData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyQuestionsBinding.inflate(inflater, container, false)

        binding.btnNext.setOnClickListener {
            requireContext().stopService(Intent(requireContext(), AudioRecorderService::class.java))
            waitForAudioFileAndSubmit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surveyAssignment = arguments?.getParcelable(AppConstants.ARG_CLICKED_SURVEY)
        surveyData = arguments?.getParcelable(AppConstants.ARG_SURVEY_DETAIL_RESPONSE)
        name = arguments?.getString(AppConstants.ARG_NAME) ?: ""
        fatherName = arguments?.getString(AppConstants.ARG_FATHER_NAME) ?: ""
        area = arguments?.getString(AppConstants.ARG_AREA) ?: ""
        block = arguments?.getString(AppConstants.ARG_BLOCK) ?: ""
        city = arguments?.getString(AppConstants.ARG_CITY) ?: ""
        state = arguments?.getString(AppConstants.ARG_STATE) ?: ""
        postalCode = arguments?.getString(AppConstants.ARG_POSTAL_CODE) ?: ""

        surveyData?.let { getQuestionsList(it.id) }

    }

    private fun getQuestionsList(id:Int) {
        viewModel.callSurveyQuestionApi(id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun waitForAudioFileAndSubmit() {
        CoroutineScope(Dispatchers.IO).launch {
            var file: File? = null
            var stable = false
            var previousSize = 0L

            repeat(20) { // check for up to 2 seconds
                val f = AudioRecorderService.getRecordedFilePath(requireContext())
                if (f != null && f.exists() && f.canRead()) {
                    val currentSize = f.length()
                    if (currentSize > 0 && currentSize == previousSize) {
                        file = f
                        stable = true
                        return@repeat
                    }
                    previousSize = currentSize
                }
                delay(100)
            }

            withContext(Dispatchers.Main) {
                if (!stable || file == null) {
                    Toast.makeText(requireContext(), "Audio file not ready", Toast.LENGTH_SHORT)
                        .show()
                    return@withContext
                }

                val extension = file!!.extension.lowercase()
                if (extension !in listOf("mp3", "wav", "ogg", "m4a")) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid audio format: .$extension",
                        Toast.LENGTH_LONG
                    ).show()
                    return@withContext
                }

                val answers = adapter?.getAnswers() ?: emptyList()
                submitStoreResponse(file!!, answers)
            }
        }
    }

    private fun submitStoreResponse(audioFile: File, surveyQAList: List<Pair<String, String>>) {
        val audioPart = MultipartBody.Part.createFormData(
            "audio_file", audioFile.name,
            audioFile.asRequestBody(getMimeType(audioFile).toMediaTypeOrNull())
        )
        val map = buildSurveyRequestMap(
            surveyAssignment?.survey_id.toString(),
            name,
            fatherName,
            AppSharedPreferences.getLatitude(),
            AppSharedPreferences.getLongitude(),
            area,
            state,
            city,
            block,
            postalCode,
            surveyQAList
        )

        viewModel.callStoreResponseApi(map, audioPart)
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            else -> "application/octet-stream" // fallback
        }
    }


    private fun buildSurveyRequestMap(
        surveyId: String,
        name: String,
        fatherName: String,
        lat: String,
        lng: String,
        address: String,
        state: String,
        city: String,
        block: String,
        pincode: String,
        surveyQAList: List<Pair<String, String>>
    ): Map<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()
        fun String.toRB(): RequestBody =
            RequestBody.create("text/plain".toMediaTypeOrNull(), this)

        map["survey_id"] = surveyId.toRB()
        map["name"] = name.toRB()
        map["father_name"] = fatherName.toRB()
        map["location[lat]"] = lat.toRB()
        map["location[lng]"] = lng.toRB()
        map["address"] = address.toRB()
        map["state"] = state.toRB()
        map["city"] = city.toRB()
        map["block"] = block.toRB()
        map["pincode"] = pincode.toRB()

        surveyQAList.forEachIndexed { index, (question, answer) ->
            map["survay[$index][question]"] = question.toRB()
            map["survay[$index][answer]"] = answer.toRB()
        }

        return map
    }

    override fun setObservers() {
        viewModel.getSurveyQuestionResponse().observe(viewLifecycleOwner) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    handleQuestionResponse(response)
                    progressBarHideShow(false)
                }

                Status.ERROR -> {
                    handleErrorResponse(response)
                    progressBarHideShow(false)
                }
            }
        }

        viewModel.getStoreResponseResponse().observe(viewLifecycleOwner) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> progressBarHideShow(true)
                Status.SUCCESS -> {
                    handleStoreResponse(response)
                    progressBarHideShow(false)
                }

                Status.ERROR -> {
                    handleStoreResponse(response)
                    progressBarHideShow(false)
                }
            }
        }
    }

    private fun handleStoreResponse(response: SurveyStoreResponse?) {
        if (response != null) {
            if (response.success) {
                val file = AudioRecorderService.getRecordedFilePath(requireContext())
                file?.delete()
                surveySubmitted()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000) // wait for 2 seconds
                    requireActivity().finish()
                }
            }
        }
    }

    private fun handleErrorResponse(response: SurveyQuestionResponse?) {
        Toast.makeText(requireContext(), "Failed to load questions", Toast.LENGTH_SHORT).show()
    }

    private fun handleQuestionResponse(response: SurveyQuestionResponse?) {
        if (response?.success == true && response.data.isNotEmpty()) {
            adapter = QuestionAdapter(response.data)
            binding.recyclerView.isNestedScrollingEnabled = false
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
            binding.recyclerView.expandFully()
        } else {
            Toast.makeText(requireContext(), "No questions available", Toast.LENGTH_SHORT).show()
        }

    }


}
