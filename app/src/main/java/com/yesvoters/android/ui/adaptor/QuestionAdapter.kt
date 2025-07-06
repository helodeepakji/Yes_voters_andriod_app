package com.yesvoters.android.ui.adaptor

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.yesvoters.android.R
import com.yesvoters.android.extentions.expandFully
import com.yesvoters.android.ui.model.response.SurveyQuestion

class QuestionAdapter(
    private val questions: List<SurveyQuestion>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val answersMap = mutableMapOf<String, String>()

    fun getAnswers(): List<Pair<String, String>> {
        return answersMap.toList()
    }

    private fun saveAnswer(question: String, answer: String) {
        answersMap[question] = answer
    }

    override fun getItemViewType(position: Int): Int {
        return when (questions[position].type) {
            "radio" -> 0
            "checkbox" -> 1
            "select" -> 2
            "text" -> 3
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> RadioViewHolder(inflater.inflate(R.layout.item_radio_question, parent, false))
            1 -> CheckboxViewHolder(
                inflater.inflate(
                    R.layout.item_checkbox_question,
                    parent,
                    false
                )
            )

            2 -> SelectViewHolder(inflater.inflate(R.layout.item_select_question, parent, false))
            3 -> TextInputViewHolder(inflater.inflate(R.layout.item_text_question, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = questions.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = questions[position]
        val callback = this::saveAnswer

        when (holder) {
            is RadioViewHolder -> holder.bind(question, position, callback)
            is CheckboxViewHolder -> holder.bind(question, position, callback)
            is SelectViewHolder -> holder.bind(question, position, callback)
            is TextInputViewHolder -> holder.bind(question, position, callback)
        }
    }

    class RadioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var selectedIndex = -1

        fun bind(question: SurveyQuestion, position: Int, onAnswerChanged: (String, String) -> Unit) {
            val questionText = itemView.findViewById<TextView>(R.id.questionText)
            val flexboxLayout = itemView.findViewById<FlexboxLayout>(R.id.radioGroup)

            val num = position + 1
            questionText.text = "$num. ${question.question}"
            flexboxLayout.removeAllViews()

            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(itemView.context).apply {
                    text = option
                    id = View.generateViewId()
                    layoutParams = FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 16, 8)
                    }
                    buttonTintList = ContextCompat.getColorStateList(context, R.color.radio_dot_color)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    isChecked = selectedIndex == index
                }

                radioButton.setOnClickListener {
                    selectedIndex = index
                    notifySelection(flexboxLayout, index)
                    onAnswerChanged(question.question, option)
                }

                flexboxLayout.addView(radioButton)
            }
        }

        private fun notifySelection(parent: ViewGroup, selectedIdx: Int) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child is RadioButton) {
                    child.isChecked = i == selectedIdx
                }
            }
        }
    }

    class CheckboxViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(question: SurveyQuestion, position: Int, onAnswerChanged: (String, String) -> Unit) {
            val questionText = itemView.findViewById<TextView>(R.id.questionText)
            val container = itemView.findViewById<ViewGroup>(R.id.checkboxContainer)
            val num = position + 1
            questionText.text = "$num. ${question.question}"

            val selectedOptions = mutableSetOf<String>()
            container.removeAllViews()
            question.options.forEach { option ->
                val checkBox = CheckBox(itemView.context).apply {
                    text = option
                    textSize = 14f
                    val padding = 8
                    setPadding(padding, padding, padding, padding)
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 16, 0)
                    }
                }
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedOptions.add(option) else selectedOptions.remove(option)
                    onAnswerChanged(question.question, selectedOptions.joinToString(", "))
                }
                container.addView(checkBox)
            }
        }
    }

    class SelectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var selectedAnswer: String? = null
        private var isExpanded = false

        fun bind(question: SurveyQuestion, position: Int, onAnswerChanged: (String, String) -> Unit) {
            val questionText = itemView.findViewById<TextView>(R.id.questionText)
            val optionRecyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewOption)

            val num = position + 1
            questionText.text = "$num. ${question.question}"
            optionRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)

            val adapter = OptionAdapter(question.options) { selected ->
                selectedAnswer = selected
                onAnswerChanged(question.question, selected)
            }
            optionRecyclerView.adapter = adapter

            optionRecyclerView.visibility = View.VISIBLE
            isExpanded = true
            setArrowDrawable(questionText, R.drawable.down_arrow)

            questionText.setOnClickListener {
                isExpanded = !isExpanded
                optionRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
                val arrowRes = if (isExpanded) R.drawable.ic_down_arrow else R.drawable.ic_arrow_up
                setArrowDrawable(questionText, arrowRes)
                (itemView.parent as? RecyclerView)?.post {
                    (itemView.parent as RecyclerView).expandFully()
                }
            }
        }

        private fun setArrowDrawable(textView: TextView, drawableResId: Int) {
            val drawable = ContextCompat.getDrawable(itemView.context, drawableResId)
            val sizeInDp = 25
            val sizeInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                sizeInDp.toFloat(),
                itemView.resources.displayMetrics
            ).toInt()
            drawable?.setBounds(0, 0, sizeInPx, sizeInPx)
            textView.setCompoundDrawablesRelative(null, null, drawable, null)
            textView.compoundDrawablePadding = 8
        }
    }

    class TextInputViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(question: SurveyQuestion, position: Int, onAnswerChanged: (String, String) -> Unit) {
            val questionText = itemView.findViewById<TextView>(R.id.questionText)
            val answerInput = itemView.findViewById<EditText>(R.id.answerInput)

            val num = position + 1
            questionText.text = "$num. ${question.question}"

            answerInput.addTextChangedListener {
                val answer = it.toString()
                onAnswerChanged(question.question, answer)
            }
        }
    }
}
