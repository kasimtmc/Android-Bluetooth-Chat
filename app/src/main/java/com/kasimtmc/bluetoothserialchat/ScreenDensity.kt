package com.kasimtmc.bluetoothserialchat

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

class ScreenDensity(context: Context) {
    private val metrics= WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(context)
    private val width= metrics.bounds.width()
    private val height= metrics.bounds.height()
    private val density= context.resources.displayMetrics.density.toInt()
    private val vDp= height/density
    private val hDp= width/density

    fun vDp(percent: Double) : Dp {
        return ((vDp/100) * percent).dp
    }

    fun hDp(percent: Double) : Dp {
        return ((hDp/100) * percent).dp
    }

}