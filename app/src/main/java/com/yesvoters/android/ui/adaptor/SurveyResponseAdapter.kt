package com.yesvoters.android.ui.adaptor

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.yesvoters.android.R
import com.yesvoters.android.databinding.ItemSurveyResponseBinding
import com.yesvoters.android.player.AudioPlayerManager
import com.yesvoters.android.ui.model.response.MySurveyResponseData
import com.yesvoters.android.utils.DateUtil

class SurveyResponseAdapter(
    private val activity: Activity,
    private val list: List<MySurveyResponseData>
) : RecyclerView.Adapter<SurveyResponseAdapter.SurveyResponseViewHolder>() {

    private val playerManager = AudioPlayerManager(activity)
    private var currentlyPlayingPosition: Int? = null
    private var previousAudioControlsLayout: LinearLayout? = null


    inner class SurveyResponseViewHolder(val binding: ItemSurveyResponseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyResponseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSurveyResponseBinding.inflate(inflater, parent, false)
        return SurveyResponseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SurveyResponseViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvSurveyName.text = item.survey.title
            tvName.text = item.name
            tvDateTime.text = "${DateUtil.getFormattedDate(item.created_at)} ${DateUtil.getFormattedTime(item.created_at)}"
            tvAddress.text = item.address
            tvRecordDuration.text = "${item.survey.recording_time} min(s)"
            txtCurrentTime.text = "00:00"
            txtTotalDuration.text = "00:00"
            btnPlayPause.setImageResource(R.drawable.exo_icon_pause) // Set default
            hideControllers(audioControlsLayout, true)
            ivMic.setOnClickListener {
                hideControllers(holder.binding.audioControlsLayout, false)
                previousAudioControlsLayout?.let {
                    hideControllers(it, true)
                }
                val audioUrl = item.audio_file ?: return@setOnClickListener

                // If clicking same item again, toggle pause/play
                if (currentlyPlayingPosition == position && playerManager.isPlaying()) {
                    playerManager.stop()
                    hideControllers(holder.binding.audioControlsLayout, true)
                    currentlyPlayingPosition = null
                    return@setOnClickListener
                }

                // Stop previous audio
                playerManager.stop()

                // Play current audio
                hideControllers(holder.binding.audioControlsLayout, false)
                playerManager.playAudio(audioUrl) { current, total ->
                    txtCurrentTime.text = current
                    txtTotalDuration.text = total
                }

                // Update currently playing position
                currentlyPlayingPosition = position
                previousAudioControlsLayout = audioControlsLayout
                btnPlayPause.setImageResource(R.drawable.exo_icon_pause)

            }
            btnPlayPause.setOnClickListener {
                if (currentlyPlayingPosition == position) {
                    if (playerManager.isPlaying()) {
                        playerManager.pause()
                        btnPlayPause.setImageResource(R.drawable.exo_icon_play)
                    } else {
                        playerManager.resume { current, total ->
                            txtCurrentTime.text = current
                            txtTotalDuration.text = total
                        }
                        btnPlayPause.setImageResource(R.drawable.exo_icon_pause)
                    }
                }
            }

        }
    }
    private fun hideControllers(audioLayout: LinearLayout, isHide: Boolean) {
        audioLayout.visibility = if (isHide) View.INVISIBLE else View.VISIBLE
    }


    override fun getItemCount(): Int = list.size

    override fun onViewRecycled(holder: SurveyResponseViewHolder) {
        super.onViewRecycled(holder)
        if (holder.adapterPosition == currentlyPlayingPosition) {
            playerManager.stop()
            currentlyPlayingPosition = null
        }
    }

    fun releasePlayer() {
        playerManager.release()
    }
}
