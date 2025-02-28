package com.kasimtmc.bluetoothserialchat.ui

import android.content.Context
import android.content.SharedPreferences
import android.widget.EditText
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.kasimtmc.bluetoothserialchat.GlobalStates.dynamicColors
import com.kasimtmc.bluetoothserialchat.GlobalStates.isButton
import com.kasimtmc.bluetoothserialchat.GlobalStates.settingSwitches
import com.kasimtmc.bluetoothserialchat.GlobalStates.settingSwitchesPref
import com.kasimtmc.bluetoothserialchat.ScreenDensity

class AppSettings(
    private val modifier: Modifier,
    private val navController: NavController,
    private val context: Context,
    private val launchPref: SharedPreferences
) {

    @Composable
    fun Screen() {
        val dynamicColor= dynamicColors(context)
        val density= ScreenDensity(context)
        val view= LocalView.current
        var isImeVisible by remember { mutableStateOf(false) }
        BackHandler {
            if (!isImeVisible) {
                navController.popBackStack()
            }
        }
        LaunchedEffect(view) {

            val listener= OnApplyWindowInsetsListener { _, i ->
                isImeVisible = WindowInsetsCompat.toWindowInsetsCompat(i.toWindowInsets()!!).isVisible(
                    WindowInsetsCompat.Type.ime())
                i
            }
            ViewCompat.setOnApplyWindowInsetsListener(view, listener)
        }
        Scaffold(modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier.fillMaxSize().background(dynamicColor.background).padding(innerPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier.height(density.vDp(6.0)))
                Row(horizontalArrangement = Arrangement.Center) {
                    Text("Ayarlar", modifier.fillMaxWidth(), color = dynamicColor.onBackground, textAlign = TextAlign.Center)
                }
                Spacer(modifier.height(density.vDp(1.5)))
                HorizontalDivider(modifier.fillMaxWidth(0.75f).align(Alignment.CenterHorizontally), color = dynamicColor.onPrimaryContainer, thickness = 2.dp)
                Spacer(modifier.height(density.vDp(3.0)))
                settingSwitches.forEach { item ->
                    var setValues by remember { mutableStateOf(launchPref.getBoolean(settingSwitchesPref[settingSwitches.indexOf(item)], false)) }
                    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(item, modifier.padding(start = 24.dp), color = dynamicColor.onBackground, textAlign = TextAlign.Center)
                        if (isButton[settingSwitches.indexOf(item)]) {
                            Switch(
                                modifier = modifier.scale(0.7f).padding(end = 24.dp),
                                checked = setValues,
                                enabled = true,
                                onCheckedChange = {
                                    setValues = it
                                    launchPref.edit().putBoolean(settingSwitchesPref[settingSwitches.indexOf(item)], setValues).apply()
                                },
                                colors = SwitchColors(uncheckedIconColor = dynamicColor.onSecondaryContainer,
                                    uncheckedThumbColor = dynamicColor.onSecondary,
                                    uncheckedTrackColor = dynamicColor.secondary,
                                    uncheckedBorderColor = Color.LightGray,
                                    checkedIconColor = dynamicColor.onPrimaryContainer,
                                    checkedThumbColor = dynamicColor.onPrimary,
                                    checkedTrackColor = dynamicColor.primary,
                                    checkedBorderColor = Color.Gray,
                                    disabledCheckedIconColor = Color.White,
                                    disabledCheckedThumbColor = Color.LightGray,
                                    disabledCheckedTrackColor = Color.DarkGray,
                                    disabledCheckedBorderColor = Color.Black,
                                    disabledUncheckedIconColor = Color.White,
                                    disabledUncheckedThumbColor = dynamicColor.inversePrimary,
                                    disabledUncheckedTrackColor = Color.DarkGray,
                                    disabledUncheckedBorderColor = Color.Magenta)
                            )
                        } else {

                        }

                    }
                }
            }
            //
        }
    }
}