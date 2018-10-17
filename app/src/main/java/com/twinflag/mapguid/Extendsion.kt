package com.twinflag.mapguid

import android.content.Context

fun Context.dp2px(dp: Int): Float {
    return resources.displayMetrics.density * dp
}