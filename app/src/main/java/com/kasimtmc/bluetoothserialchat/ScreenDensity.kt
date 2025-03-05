package com.kasimtmc.bluetoothserialchat

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowMetricsCalculator

class ScreenDensity(private val context: Context) {
    private val metrics= WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(context)
    private val hDp= metrics.bounds.width()/context.resources.displayMetrics.density
    private val vDp= metrics.bounds.height()/context.resources.displayMetrics.density

    @Stable
    fun compactScreen() : MutableState<Boolean> {
        val windowSizeClass = WindowSizeClass.compute(hDp, vDp)
        return mutableStateOf(windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT || windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT)
    }

    fun vDp(percent: Double) : Dp {
        return ( vDp.div(100).times(percent) ).dp
    }

    fun hDp(percent: Double) : Dp {
        return ( hDp.div(100).times(percent) ).dp
    }

    fun aDp(percent: Double) : Dp {
        return hDp(percent).plus(vDp(percent)).div(2)
    }

}