package com.yesvoters.android.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yesvoters.android.R
import com.yesvoters.android.database.AppSharedPreferences
import com.yesvoters.android.database.UserPreferences
import com.yesvoters.android.databinding.FragmentBeginSurveyBinding
import com.yesvoters.android.network.remote.Status
import com.yesvoters.android.services.AudioRecorderService
import com.yesvoters.android.ui.base.BaseFragment
import com.yesvoters.android.ui.model.response.SurveyAssignment
import com.yesvoters.android.ui.model.response.SurveyData
import com.yesvoters.android.ui.model.response.SurveyDetailsResponse
import com.yesvoters.android.ui.viewModel.SurveyViewModel
import com.yesvoters.android.utils.DateUtil
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class BeginSurveyFragment : BaseFragment() {

    private var _binding: FragmentBeginSurveyBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy { ViewModelProvider(this)[SurveyViewModel::class.java] }

    private var surveyAssignment: SurveyAssignment? = null
    private var surveyData: SurveyData? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeginSurveyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = Color.parseColor("#F5F5F5")
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        initLocationClient()
        getLastKnownLocation()

        surveyAssignment = arguments?.getParcelable("clicked_survey")

        binding.header.tvHeading.text = getString(com.yesvoters.android.R.string.begin_survey_text)
        binding.header.ibBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnStartSurvey.setOnClickListener {
            if (!binding.checkboxConfirmLocation.isChecked) {
                showToast("Please accept Confirmation for location")
                return@setOnClickListener
            }

            if (hasMicPermission()) {
                startSurvey()
            } else {
                requestMicPermission()
            }
        }

        surveyAssignment?.let { getSurveyDetail(it.survey_id) }

        setObservers()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSurvey()
        } else {
            showToast("Mic permission is required to proceed")
        }
    }
    private fun startSurvey() {
        startAudioService()

        val detailFragment = SurveyDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("clicked_survey", surveyAssignment)
            putParcelable("survey_detail_response", surveyData)
        }
        detailFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(
                (requireActivity() as com.yesvoters.android.ui.activity.SurveyOnBoardingActivity).binding.fragmentContainer.id,
                detailFragment
            )
            .addToBackStack(null)
            .commit()
    }


    private fun startAudioService() {
        val intent = Intent(requireContext(), AudioRecorderService::class.java)
        intent.putExtra("record_duration_minutes", surveyAssignment?.survey?.recording_time) // for 8 minutes
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun getSurveyDetail(id: Int) {
        viewModel.callSurveyDetailsApi( id)
    }

    override fun setObservers() {
        viewModel.getSurveyDetailResponse().observe(viewLifecycleOwner) { res ->
            val response = res.data
            when (res.status) {
                Status.LOADING -> {
                    progressBarHideShow(true)
                }

                Status.SUCCESS -> {
                    handleDetailResponse(response)
                    progressBarHideShow(false)
                }

                Status.ERROR -> {
                    handleErrorResponse(response)
                    progressBarHideShow(false)
                }
            }
        }
    }

    private fun handleErrorResponse(response: SurveyDetailsResponse?) {
        showToast(response?.message ?: "Failed to load survey details")
    }

    private fun handleDetailResponse(response: SurveyDetailsResponse?) {
        if (response?.success != true) return

        surveyData = response.data ?: return

        val createdAt = surveyAssignment?.created_at
        val date = createdAt?.let { DateUtil.getFormattedDate(it) } ?: ""
        val time = createdAt?.let { DateUtil.getFormattedTime(it) } ?: ""
        val dateTime = "$date $time"

        with(binding) {
            tvSurveyorName.text = surveyData!!.title
            tvAssignerName.text = surveyAssignment?.assigner?.name.orEmpty()
            tvSurveyorId.text = surveyData!!.id.toString()
            val text =
                "<font color=#212121>Description: </font> <font color=#3F51B5>${surveyData!!.description}</font>"
            tvDescription.text = (Html.fromHtml(text))
            tvAssignDateTime.text = dateTime
            tvTime.text = DateUtil.getFormattedTime(surveyData!!.created_at)
        }
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    AppSharedPreferences.setLatitude(latitude.toString())
                    AppSharedPreferences.setLongitude(longitude.toString())
                    getAddressFromLocation(latitude, longitude)
                } else {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Location fetch failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address> =
                geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                binding.tvLocation.text = address.locality
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_address_found), Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), getString(R.string.address_retrieval_failed), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicPermission() {
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1001)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
