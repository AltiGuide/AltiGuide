package com.example.altiguide_mobile.util

import com.example.altiguide_mobile.R

fun getMountainDrawable(name: String): Int {
    val key = name.lowercase().removePrefix("gunung ").trim()
    return when (key) {
        "merbabu"  -> R.drawable.merbabu
        "andong"   -> R.drawable.andong
        "lawu"     -> R.drawable.lawu
        "prau"     -> R.drawable.prau
        "sindoro"  -> R.drawable.sindoro
        "slamet"   -> R.drawable.slamet
        "sumbing"  -> R.drawable.sumbing
        "ungaran"  -> R.drawable.ungaran
        else       -> R.drawable.startjourney_img
    }
}

fun formatNumber(n: Int): String {
    return String.format("%,d", n).replace(',', '.')
}
