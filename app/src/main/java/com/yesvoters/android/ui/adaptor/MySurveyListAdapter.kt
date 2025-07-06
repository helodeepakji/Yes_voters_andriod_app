package com.yesvoters.android.ui.adaptor

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yesvoters.android.R
import com.yesvoters.android.databinding.ItemSurveyCardBinding
import com.yesvoters.android.ui.model.response.SurveyAssignment

class MySurveyListAdapter(
    private var surveyList: List<SurveyAssignment>,
    private val onItemClick: (SurveyAssignment) -> Unit
) : RecyclerView.Adapter<MySurveyListAdapter.SurveyViewHolder>() {

    inner class SurveyViewHolder(val binding: ItemSurveyCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val binding =
            ItemSurveyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurveyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val item = surveyList[position]
        val background = holder.binding.tvStatus.background as? GradientDrawable

        if (item.survey.is_active == 1) {
            background?.setColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.active_color
                )
            )
        } else {
            background?.setColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.pending_color
                )
            )
        }

        holder.binding.tvSurveyName.text = item.survey.title
        holder.binding.tvAddress.text = item.survey.address
        holder.binding.tvRecordDuration.text = "${item.survey.recording_time} min"
        holder.binding.tvDescription.text = item.survey.description
        holder.binding.tvRespondent.text = item.survey.my_response_count.toString()
        holder.binding.btnStartSurvey.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = surveyList.size

}
