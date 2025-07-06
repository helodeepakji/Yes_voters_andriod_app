package com.yesvoters.android.ui.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesvoters.android.databinding.ItemSearchSurveyRowBinding
import com.yesvoters.android.ui.model.response.SurveyAssignment

class SearchSurveyAdapter(
    private var surveyList: List<SurveyAssignment>,
    private val onItemClick: (SurveyAssignment) -> Unit
) : RecyclerView.Adapter<SearchSurveyAdapter.SurveyViewHolder>() {

    inner class SurveyViewHolder(val binding: ItemSearchSurveyRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val binding =
            ItemSearchSurveyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurveyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val item = surveyList[position]
        holder.binding.tvSurveyTitle.text = item.survey.title

        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = surveyList.size

    fun updateList(filtered: List<SurveyAssignment>) {
        surveyList = filtered
        notifyDataSetChanged()
    }
}
