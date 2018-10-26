package com.fatihsevban.instakotlinapp.Utils

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class GridViewItem(context: Context, attributeSet: AttributeSet): ImageView(context, attributeSet) {

    // displays an image view like a square.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}