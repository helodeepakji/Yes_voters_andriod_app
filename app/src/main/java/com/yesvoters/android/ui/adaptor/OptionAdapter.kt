package com.yesvoters.android.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yesvoters.android.R

class OptionAdapter(
    private val options: List<String>,
    private var selectedIndex: Int = -1,
    private val onOptionSelected: (String) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

    inner class OptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text = view.findViewById<TextView>(android.R.id.text1)

        fun bind(option: String, position: Int) {
            text.text = option
            text.setTextColor(
                if (selectedIndex == position)
                    ContextCompat.getColor(text.context, R.color.app_blue_color)
                else
                    ContextCompat.getColor(text.context, R.color.app_black_color)
            )

            text.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                if (selectedIndex == position) R.drawable.ic_check else 0,
                0
            )

            itemView.setOnClickListener {
                val previousIndex = selectedIndex
                selectedIndex = position
                notifyItemChanged(previousIndex)
                notifyItemChanged(position)
                onOptionSelected(option)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.option_list_item, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position], position)
    }

    override fun getItemCount(): Int {
        return options.size
    }
}
