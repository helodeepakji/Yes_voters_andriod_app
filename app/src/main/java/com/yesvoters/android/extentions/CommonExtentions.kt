package com.yesvoters.android.extentions

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.expandFully() {
    val adapter = adapter ?: return
    val layoutManager = layoutManager ?: return

    post {
        var totalHeight = 0
        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += holder.itemView.measuredHeight
        }
        layoutParams.height = totalHeight+350
        requestLayout()
    }
}




