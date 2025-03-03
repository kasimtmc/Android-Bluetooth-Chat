package com.kasimtmc.bluetoothserialchat.ui

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.kasimtmc.bluetoothserialchat.GlobalStates.dynamicColors
import com.kasimtmc.bluetoothserialchat.GlobalStates.isButton
import com.kasimtmc.bluetoothserialchat.GlobalStates.settingSwitches
import com.kasimtmc.bluetoothserialchat.GlobalStates.settingSwitchesPref
import com.kasimtmc.bluetoothserialchat.R
import com.kasimtmc.bluetoothserialchat.ScreenDensity

class AppSettings(
    private val modifier: Modifier,
    private val navController: NavController,
    private val context: Context,
    private val setsPref: SharedPreferences
) {
    //

    @Composable
    fun Screen() {
        val dynamicColor= dynamicColors(context)
        val density= ScreenDensity(context)
        val view= LocalView.current
        var isImeVisible by remember { mutableStateOf(false) }
        val imeController= LocalSoftwareKeyboardController.current
        //
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
                    Text(
                        context.getString(R.string.settings),
                        modifier.fillMaxWidth(),
                        color = dynamicColor.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier.height(density.vDp(1.5)))
                HorizontalDivider(
                    modifier.fillMaxWidth(0.75f).align(Alignment.CenterHorizontally),
                    color = dynamicColor.onPrimaryContainer,
                    thickness = 2.dp
                )
                Spacer(modifier.height(density.vDp(5.0)))
                settingSwitches.forEach { item ->
                    Row(
                        modifier
                            .fillMaxWidth()
                            .padding(
                                start = density.hDp(6.0),
                                end = if (isButton[settingSwitches.indexOf(item)]) density.hDp(6.0) else density.hDp(3.0),
                                top = if (isButton[settingSwitches.indexOf(item)]) density.vDp(2.0) else density.vDp(0.0),
                                bottom = if (isButton[settingSwitches.indexOf(item)]) density.vDp(4.0) else density.vDp(2.0)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item,
                            modifier.align(Alignment.CenterVertically),
                            color = dynamicColor.onBackground,
                            textAlign = TextAlign.Start
                        )
                        if (isButton[settingSwitches.indexOf(item)]) {
                            var setValues by remember { mutableStateOf(setsPref.getBoolean(settingSwitchesPref[settingSwitches.indexOf(item)],false) ) }
                            Switch(
                                modifier = modifier
                                    .height(10.dp)
                                    .align(Alignment.CenterVertically),
                                checked = setValues,
                                enabled = true,
                                onCheckedChange = {
                                    setValues = it
                                    setsPref.edit().putBoolean(settingSwitchesPref[settingSwitches.indexOf(item)], setValues).apply()
                                },
                                colors = SwitchColors(
                                    uncheckedIconColor = dynamicColor.onSecondaryContainer,
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
                                    disabledUncheckedBorderColor = Color.Magenta
                                )
                            )
                        } else {
                            var setText by remember { mutableIntStateOf(setsPref.getInt(settingSwitchesPref[settingSwitches.indexOf(item)], 20 )) }
                            OutlinedTextField(
                                enabled = true,
                                shape = AbsoluteRoundedCornerShape(8.dp),
                                value = setText.toString(),
                                onValueChange = {
                                    setText = it.toInt()
                                },
                                modifier = modifier
                                    .align(Alignment.CenterVertically)
                                    .width(90.dp)
                                    .scale(0.7f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    capitalization = KeyboardCapitalization.None,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    setsPref.edit().putInt(settingSwitchesPref[settingSwitches.indexOf(item)], setText).apply()
                                    imeController?.hide()
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    cursorColor = dynamicColor.onSurface,
                                    focusedTextColor = dynamicColor.onSurface,
                                    unfocusedTextColor = dynamicColor.onSurface,
                                    disabledTextColor = dynamicColor.inversePrimary,
                                    errorTextColor = Color.Red,
                                    focusedContainerColor = dynamicColor.surfaceContainerHigh,
                                    unfocusedContainerColor = dynamicColor.surfaceContainerLow,
                                    disabledContainerColor = Color.Gray,
                                    errorContainerColor = dynamicColor.inversePrimary,
                                    focusedBorderColor = dynamicColor.inversePrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    disabledBorderColor = Color.DarkGray,
                                    errorBorderColor = Color.Red,
                                    selectionColors = TextSelectionColors(
                                        dynamicColor.tertiary,
                                        dynamicColor.inversePrimary
                                    ),
                                    errorCursorColor = Color.Cyan
                                ),
                                textStyle= TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    textDecoration = TextDecoration.None,
                                    fontWeight = FontWeight.W600,
                                    lineHeight = 22.sp
                                ),

                            )
                        }

                    }
                }
            }
        }
    }
}