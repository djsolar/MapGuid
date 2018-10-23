package com.twinflag.mapguid

import android.content.Context
import android.view.View
import android.widget.Toast

fun Context.dp2px(dp: Int): Float {
    return resources.displayMetrics.density * dp
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun View.dp2px(dp: Float): Float {
    return context.resources.displayMetrics.density * dp
}