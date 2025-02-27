package com.kasimtmc.bluetoothserialchat.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.kasimtmc.bluetoothserialchat.Constants.REQUIRED_PERMISSIONS
import com.kasimtmc.bluetoothserialchat.GlobalStates.deviceDetails
import com.kasimtmc.bluetoothserialchat.GlobalStates.dynamicColors
import com.kasimtmc.bluetoothserialchat.GlobalStates.isConnected
import com.kasimtmc.bluetoothserialchat.GlobalStates.messages
import com.kasimtmc.bluetoothserialchat.GlobalStates.selectedDevice
import com.kasimtmc.bluetoothserialchat.Message
import com.kasimtmc.bluetoothserialchat.R
import com.kasimtmc.bluetoothserialchat.ScreenDensity
import com.kasimtmc.bluetoothserialchat.services.ChatService
import kotlinx.coroutines.launch

class Chat(
    private val modifier: Modifier,
    private val navController: NavController,
    private val context: Context,
    private val chatService: ChatService
) {

    private var outgoing= ""

    @Composable
    fun Screen() {
        val dynamicColor= dynamicColors(context)
        val density= ScreenDensity(context)
        val chatScope= rememberCoroutineScope()
        val deviceName by remember { mutableStateOf(if (selectedDevice?.name == null) selectedDevice?.address.toString() else selectedDevice?.name.toString()) }
        var detailText by remember { mutableStateOf("Eşleştirilmiş") }
        val listState= rememberLazyListState()
        var chatOutgoing: String by remember { mutableStateOf("") }
        val view= LocalView.current
        var isImeVisible by remember { mutableStateOf(false) }
        BackHandler {
            if (!isImeVisible) {
                navController.popBackStack()
            }
        }
        LaunchedEffect(view) {

            val listener= OnApplyWindowInsetsListener { _, i ->
                isImeVisible = WindowInsetsCompat.toWindowInsetsCompat(i.toWindowInsets()!!).isVisible(WindowInsetsCompat.Type.ime())
                i
            }
            ViewCompat.setOnApplyWindowInsetsListener(view, listener)
        }
        Scaffold { innerPadding ->
            Column(
                modifier.fillMaxSize().background(dynamicColor.background).padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top )
            {
                Spacer(modifier.height(if (isImeVisible) density.vDp(5.0) else density.vDp(8.0)))
                Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                    if (REQUIRED_PERMISSIONS.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }) {
                        DeviceText(deviceName, dynamicColor)
                        Spacer(modifier.height(density.vDp(2.0)))
                        detailText= deviceDetails.value
                        DeviceText(detailText, dynamicColor)
                        Spacer(modifier.height(density.vDp(2.0)))
                        FloatingActionButton(
                            onClick = {
                                if (isConnected.value) {
                                    chatScope.launch { chatService.disconnect() }
                                    messages.clear()
                                } else {
                                    chatScope.launch { chatService.connect() }
                                }
                            },
                            modifier = modifier.size(density.vDp(6.0)),
                            shape = RoundedCornerShape(density.vDp(2.0)),
                            containerColor = if (isConnected.value) Color.Green else Color.Red,
                            contentColor = Color.White) {
                            Icon(
                                ImageBitmap.imageResource( if (isConnected.value) R.drawable.serial_connection_ic else R.drawable.serial_no_connection_ic),
                                contentDescription = "connect to device",
                                modifier.size(density.vDp(3.2)),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier.fillMaxHeight(if (isImeVisible) {
                            if (density.vDp(100.0) <= 1000.dp) 0.2f else 0.12f
                        } else {
                            0.08f
                        }))
                        LazyColumn(
                            state = listState,
                            reverseLayout = false,
                            modifier = modifier
                                .fillMaxWidth().
                                fillMaxHeight(if (isImeVisible) 0.83f else 0.85f)
                        ) {
                            items(messages) { message ->
                                ChatBubble(message, density)
                            }
                        }
                        if (messages.isNotEmpty()) {
                            LaunchedEffect(messages.size) {
                                listState.animateScrollToItem(messages.lastIndex)
                            }
                        }
                    } // perm check
                }
            }
            Spacer(modifier.height(density.vDp(1.6)))
            Column(modifier.fillMaxSize().padding(innerPadding), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Bottom) {
                Row(modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom) {
                    //
                    TextField(
                        modifier = modifier
                            .fillMaxWidth(0.80f)
                            .border(
                                border = BorderStroke(
                                    4.dp,
                                    color = if (isConnected.value) dynamicColor.inversePrimary else Color.DarkGray
                                ), shape = AbsoluteRoundedCornerShape(12.dp)
                            ),
                        enabled = isConnected.value,
                        value = chatOutgoing,
                        onValueChange = { chatOutgoing = it },
                        label = { Text("message") },
                        colors = TextFieldColors(
                            cursorColor = dynamicColor.onSecondaryContainer,
                            focusedTextColor = dynamicColor.onSecondaryContainer,
                            unfocusedTextColor = dynamicColor.onSecondary,
                            disabledTextColor = dynamicColor.inversePrimary,
                            errorTextColor = Color.Red,
                            focusedContainerColor = dynamicColor.secondaryContainer,
                            unfocusedContainerColor = dynamicColor.secondary,
                            disabledContainerColor = Color.Gray,
                            errorContainerColor = dynamicColor.inversePrimary,
                            errorCursorColor = Color.Cyan,
                            textSelectionColors = TextSelectionColors(
                                dynamicColor.tertiary,
                                dynamicColor.inversePrimary
                            ),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Red,
                            focusedLeadingIconColor = Color.Transparent,
                            unfocusedLeadingIconColor = Color.Transparent,
                            disabledLeadingIconColor = Color.Transparent,
                            errorLeadingIconColor = Color.Transparent,
                            focusedTrailingIconColor = Color.Transparent,
                            unfocusedTrailingIconColor = Color.Transparent,
                            disabledTrailingIconColor = Color.Transparent,
                            errorTrailingIconColor = Color.Transparent,
                            focusedLabelColor = Color.Transparent,
                            unfocusedLabelColor = dynamicColor.onSecondary,
                            disabledLabelColor = Color.Gray,
                            errorLabelColor = Color.Red,
                            focusedPlaceholderColor = Color.Transparent,
                            unfocusedPlaceholderColor = Color.Transparent,
                            disabledPlaceholderColor = Color.Transparent,
                            errorPlaceholderColor = Color.Transparent,
                            focusedSupportingTextColor = Color.Transparent,
                            unfocusedSupportingTextColor = Color.Transparent,
                            disabledSupportingTextColor = Color.Transparent,
                            errorSupportingTextColor = Color.Red,
                            focusedPrefixColor = Color.Transparent,
                            unfocusedPrefixColor = Color.Transparent,
                            disabledPrefixColor = Color.Transparent,
                            errorPrefixColor = Color.Transparent,
                            focusedSuffixColor = Color.Transparent,
                            unfocusedSuffixColor = Color.Transparent,
                            disabledSuffixColor = Color.Transparent,
                            errorSuffixColor = Color.Transparent,
                        ),
                        shape = AbsoluteRoundedCornerShape(16.dp),
                        maxLines = 2
                    )
                    //
                    Spacer(modifier.width(density.hDp(3.0)))
                    FloatingActionButton(
                        modifier = modifier.align(Alignment.CenterVertically),
                        onClick = {
                            if (chatOutgoing.isNotEmpty()) {
                                outgoing= chatOutgoing
                                chatService.sendData(outgoing)
                                chatOutgoing = ""
                            }
                        },
                        containerColor = if (isConnected.value) dynamicColor.primaryContainer else dynamicColor.tertiaryContainer
                    ) {
                        //
                        Icon(
                            ImageBitmap.imageResource(R.drawable.send_message_ic),
                            contentDescription = "search device",
                            modifier
                                .heightIn(22.dp, 36.dp)
                                .widthIn(22.dp, 36.dp),
                            tint = if (isConnected.value) dynamicColor.onPrimaryContainer else dynamicColor.onTertiaryContainer
                        )
                    } //send button
                } //end of bottom side
                Spacer(modifier.height(density.vDp(2.0)))
            }
        }

    }

    @Composable
    fun DeviceText(text: String, color: ColorScheme) {
        Text(text = text, color = color.onBackground, modifier = modifier)
    }

    @Composable
    fun ChatBubble(message: Message, density: ScreenDensity) {
        val dynamicColor= dynamicColors(context)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (message.isIncoming) 8.dp else 24.dp,
                    top = 4.dp,
                    end = if (message.isIncoming) 24.dp else 8.dp,
                    bottom = 4.dp
                ),
            horizontalArrangement = if (message.isIncoming) Arrangement.Start else Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (message.isIncoming) dynamicColor.tertiaryContainer else dynamicColor.primaryContainer,
                        shape = AbsoluteRoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
                    .widthIn(max = density.hDp(75.0))
            ) {
                Text(
                    text = message.text,
                    color = if (message.isIncoming) dynamicColor.onTertiaryContainer else dynamicColor.onPrimaryContainer,
                    fontSize = 16.sp,
                    textAlign = if (message.isIncoming) TextAlign.Start else TextAlign.End
                )
            }
        }
    }

}