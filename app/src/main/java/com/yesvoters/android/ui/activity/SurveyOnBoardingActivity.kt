package com.yesvoters.android.ui.activity

import android.content.Intent
import android.os.Bundle
import com.yesvoters.android.databinding.ActivitySurveyOnBoardingBinding
import com.yesvoters.android.services.AudioRecorderService
import com.yesvoters.android.ui.base.BaseActivity
import com.yesvoters.android.ui.fragment.BeginSurveyFragment
import com.yesvoters.android.ui.model.response.SurveyAssignment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SurveyOnBoardingActivity : BaseActivity() {

    lateinit var binding: ActivitySurveyOnBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySurveyOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val surveyAssignment = intent.getParcelableExtra<SurveyAssignment>("clicked_survey")
        val fragment = BeginSurveyFragment()
        val bundle = Bundle()
        bundle.putParcelable("clicked_survey", surveyAssignment)
        fragment.arguments = bundle

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
                .commit()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()

        try {
            stopService(Intent(this, AudioRecorderService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        finish()
    }
}
