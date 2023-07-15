package com.bennohan.mynotes.helper

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bennohan.mynotes.R


class ColorAdapter (context: Context, private val data: List<String>) :
    ArrayAdapter<String>(context, 0, data) {

    private val colorList = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_note, parent, false
        )

        val colorIndex = position % colorList.size
        val color = colorList[colorIndex]

        view.setBackgroundColor(color)

        return view
    }
}