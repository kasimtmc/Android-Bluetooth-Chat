package com.kasimtmc.bluetoothserialchat

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowMetricsCalculator
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kasimtmc.bluetoothserialchat.Constants.REQUEST_CODE_PERMISSIONS
import com.kasimtmc.bluetoothserialchat.Constants.REQUIRED_PERMISSIONS
import com.kasimtmc.bluetoothserialchat.Constants.listOfPerms
import com.kasimtmc.bluetoothserialchat.GlobalStates.discoveredDevices
import com.kasimtmc.bluetoothserialchat.GlobalStates.dynamicColors
import com.kasimtmc.bluetoothserialchat.GlobalStates.hasRemote
import com.kasimtmc.bluetoothserialchat.GlobalStates.isChat
import com.kasimtmc.bluetoothserialchat.GlobalStates.isConnected
import com.kasimtmc.bluetoothserialchat.GlobalStates.isPaired
import com.kasimtmc.bluetoothserialchat.GlobalStates.messages
import com.kasimtmc.bluetoothserialchat.GlobalStates.remoteDevice
import com.kasimtmc.bluetoothserialchat.GlobalStates.selectedDevice
import com.kasimtmc.bluetoothserialchat.services.ChatService
import com.kasimtmc.bluetoothserialchat.ui.AppSettings
import com.kasimtmc.bluetoothserialchat.ui.Chat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.BreakIterator
import java.text.StringCharacterIterator

class MainActivity : ComponentActivity(), ChatService.ServiceListener, ChatService.MessageListener {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var chatService: ChatService
    private lateinit var permAlertDialog: AlertDialog.Builder
    private lateinit var permAlertDialog2: AlertDialog.Builder
    private var isFistLaunch: Boolean= true
    private lateinit var isFirstLaunchPref: SharedPreferences
    private lateinit var setsPref: SharedPreferences
    private val filter= IntentFilter(BluetoothDevice.ACTION_FOUND)

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.bt_on), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.bt_on_req_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    private val discoverableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.discoverable_req_cancelled), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.discoverable), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = if (compactScreen()) SCREEN_ORIENTATION_PORTRAIT else SCREEN_ORIENTATION_FULL_USER
        //
        bluetoothManager= getSystemService(BluetoothManager::class.java)
        bluetoothAdapter= bluetoothManager.adapter
        //
        isFirstLaunchPref= this.getSharedPreferences("launchState", MODE_PRIVATE)
        setsPref= this.getSharedPreferences("settingsState", MODE_PRIVATE)

        permAlertDialog= AlertDialog.Builder(this)
        permAlertDialog.setTitle(getString(R.string.perm_title))
        permAlertDialog.setMessage(getString(R.string.perm_text))
        permAlertDialog.setPositiveButton(getString(R.string.yes)){_, _ ->
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            isFirstLaunchPref.edit().putBoolean("isFirstLaunch", false).apply()
        }
        permAlertDialog.setNegativeButton(getString(R.string.no)) {_, _ ->
            isFirstLaunchPref.edit().putBoolean("isFirstLaunch", false).apply()
        }
        permAlertDialog2= AlertDialog.Builder(this)
        permAlertDialog2.setMessage(getString(R.string.give_perm))
        permAlertDialog2.setPositiveButton(getString(R.string.ok)){_, _ ->
            val permIntent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val permUri= Uri.fromParts("package", packageName, null)
            permIntent.data = permUri
            startActivity(permIntent)
        }

        isFistLaunch= isFirstLaunchPref.getBoolean("isFirstLaunch", true)


        //
        registerReceiver(receiver, filter)
        chatService= ChatService(this, bluetoothAdapter , this, this)
        setContent {
            AppNavigation()
            val permissionsState by remember { mutableStateOf(REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) }
            if (!permissionsState) {
                if (isFistLaunch) permAlertDialog.show() else permAlertDialog2.show()
            } else {
                turnOnBt()
                bluetoothAdapter.cancelDiscovery()
            }
        }
        //
    }

    private val receiver= object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice?=
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    if (!discoveredDevices.contains(device) && REQUIRED_PERMISSIONS.all {
                            ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                        }) {
                        if (setsPref.getBoolean("nameless", false)) {
                            discoveredDevices.add(device)
                        } else {
                            if (device!!.name != null) discoveredDevices.add(device)
                        }
                    }
                }
            }
        }
    }

    //navigation
    @Composable
    private fun AppNavigation() {
        FullScreenEffect(this@MainActivity)
        val navController= rememberNavController()
        NavHost(navController = navController,
            startDestination = "home",
            enterTransition = {
                scaleIn(
                    initialScale = 0f,
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessHigh
                    ))+
                        slideInVertically(initialOffsetY = {1000},
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessHigh
                            ))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0f,
                    transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessHigh
                    ))+
                        slideOutVertically(targetOffsetY = {1000},
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessHigh
                            ))+
                        fadeOut(animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessHigh
                        ))
            },
            popEnterTransition = {
                scaleIn(
                    initialScale = 0f,
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessHigh
                    ))+
                        slideInVertically(initialOffsetY = {-1000},
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessHigh
                            ))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0f,
                    transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessHigh
                    ))+
                        slideOutVertically(targetOffsetY = {-1000},
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessHigh
                            ))+
                        fadeOut(animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessHigh
                        ))
            },
            modifier = Modifier.background(
                color = dynamicColors(this@MainActivity).background
            )
        ) {
            composable("home") { MainScreen(modifier = Modifier, navController, this@MainActivity) }
            composable("chat") { ChatScreen(modifier = Modifier, navController, this@MainActivity, chatService) }
            composable("sets") { SettingsScreen(modifier = Modifier, navController, this@MainActivity, setsPref) }
        }
    }

    @Composable
    private fun MainScreen(modifier: Modifier, navController: NavController, context: Context) {
        val dynamicColor= dynamicColors(context)
        val density= ScreenDensity(context)
        val mainScope= rememberCoroutineScope()
        var isDiscovering by remember { mutableStateOf(if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) bluetoothAdapter.isDiscovering else false) }
        //lottie anim
        val lottieComposition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(
                R.raw.discovering
            )
        )
        //
        val drawerState= rememberDrawerState(initialValue = DrawerValue.Closed, confirmStateChange = {true})
        ModalNavigationDrawer(
            scrimColor = Color.Transparent,
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = dynamicColor.primaryContainer.copy(alpha = 0.9f),
                    modifier = modifier.fillMaxWidth(0.50f)
                ){
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        //
                        Spacer(modifier.height(density.vDp(5.0)))
                        Text(text = stringResource(R.string.devicesAround),
                            modifier= modifier.align(Alignment.CenterHorizontally),
                            color = dynamicColor.onPrimaryContainer)
                        Spacer(modifier.height(density.vDp(0.5)))
                        HorizontalDivider(color = dynamicColor.onPrimaryContainer, thickness = 2.dp)
                        Spacer(modifier.height(density.vDp(0.5)))
                        if (discoveredDevices.isNotEmpty() && REQUIRED_PERMISSIONS.all {
                                ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                            } ) {
                            discoveredDevices.forEach { device ->
                                NavigationDrawerItem(
                                    label = { Text(
                                        text = if (device?.name == null) device?.address.toString() else device?.name.toString(),
                                        color = dynamicColor.onPrimaryContainer) },
                                    icon = {
                                        Icon(
                                            ImageBitmap.imageResource(
                                                if (bluetoothAdapter.bondedDevices.contains(device)) R.drawable.bond_bonded
                                                else R.drawable.bond_none),
                                            contentDescription = "current device ${if (device?.name == null) device?.address.toString() else device?.name.toString()}",
                                            modifier.size(density.vDp(2.0)),
                                            tint = if (remoteDevice.value != null) {
                                                if (remoteDevice.value == device) Color.Green else dynamicColor.onPrimaryContainer
                                            } else {
                                                dynamicColor.onPrimaryContainer
                                            }
                                        )
                                    },
                                    selected = false,
                                    onClick =
                                    {
                                        selectedDevice= device
                                        isPaired.value= bluetoothAdapter.bondedDevices.contains(device)
                                        if (isPaired.value) {
                                            mainScope.launch {
                                                drawerState.close()
                                                navController.navigate("chat")
                                            }
                                        } else {
                                            pairDevice(device!!)
                                        }

                                    }
                                )
                                HorizontalDivider(
                                    modifier.widthIn(density.hDp(15.0), density.hDp(30.0)),
                                    color = dynamicColor.onPrimaryContainer,
                                    thickness = 1.dp)
                            }
                        }
                        //
                    }
                    Column(
                        modifier = modifier
                            .fillMaxHeight()
                            .padding(start = density.hDp(2.6)),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.Start
                    ) {
                        //
                        Row(modifier, horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stringResource(R.string.requestsAccepted),
                                color = dynamicColor.onPrimaryContainer,
                                modifier = modifier.padding(start = density.vDp(2.0)))
                            SwitchButton(
                                modifier = modifier,
                                size = 0.8f,
                                enabled = allPermissionsGranted(),
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
                        }
                        Spacer(modifier.height(density.vDp(1.0)))
                        HorizontalDivider(color = dynamicColor.onPrimaryContainer, thickness = 2.dp)
                        NavigationDrawerItem(
                            label = { Text(
                                text = stringResource(R.string.settings),
                                color = dynamicColor.onPrimaryContainer) },
                            icon = {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "app settings",
                                    modifier.size(density.vDp(2.0)),
                                    tint = dynamicColor.onPrimaryContainer
                                )
                            },
                            selected = false,
                            onClick =
                            {
                                mainScope.launch {
                                    drawerState.close()
                                    navController.navigate("sets")
                                }
                            }
                        )
                        Spacer(modifier.height(density.vDp(3.2)))
                    }
                }
            },
            gesturesEnabled = true
        ) {
            Scaffold(modifier = modifier.fillMaxSize(), containerColor = dynamicColor.background) { innerPadding ->
                Column(modifier
                    .fillMaxSize()
                    .padding(innerPadding), verticalArrangement = Arrangement.Top) {
                    Spacer(modifier.height(density.vDp(8.0)))
                    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                        Column(modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                            val discoveringStateText= remember { mutableStateOf("") }
                            discoveringStateText.value= if (isDiscovering) getString(R.string.searching) else getString(R.string.searching_stp)
                            DiscoveryTextAnimation(discoveringStateText.value, modifier, dynamicColor.onBackground)
                            Spacer(modifier.height(density.vDp(2.0)))
                            //
                            LottieAnimation(
                                composition = lottieComposition,
                                iterations = LottieConstants.IterateForever,
                                isPlaying = isDiscovering,
                                modifier = modifier.fillMaxSize(0.35f),
                                alignment = Alignment.Center,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier.height(density.vDp(2.0)))
                            val filledSize: Dp by animateDpAsState(
                                targetValue = if (!isDiscovering) density.aDp(8.0) else density.aDp(4.0),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                            FilledIconButton (
                                onClick = {
                                    bluetoothAdapter.startDiscovery()
                                    object : CountDownTimer(setsPref.getInt("searchTime", 10).toLong()*1000, 1) {
                                        override fun onTick(millisUntilFinished: Long) {
                                            isDiscovering= if (REQUIRED_PERMISSIONS.all {
                                                    ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                                                }) bluetoothAdapter.isDiscovering else false
                                        }
                                        override fun onFinish() {
                                            if (REQUIRED_PERMISSIONS.all {
                                                    ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                                                }) bluetoothAdapter.cancelDiscovery()
                                            isDiscovering= false
                                        }
                                    }.start()
                                },
                                modifier = modifier.size(filledSize),
                                shape = RoundedCornerShape(if (!isDiscovering) density.aDp(3.0) else density.aDp(1.5)),
                                enabled = (allPermissionsGranted() && !isDiscovering),
                                colors = IconButtonColors(
                                    containerColor = dynamicColor.tertiaryContainer,
                                    contentColor = dynamicColor.onTertiaryContainer,
                                    disabledContainerColor = Color.Gray,
                                    disabledContentColor = Color.LightGray)
                            ){
                                Icon(
                                    ImageBitmap.imageResource(R.drawable.search_ic),
                                    contentDescription = "search device",
                                    modifier.size(if (!isDiscovering) density.aDp(5.0) else density.aDp(2.5)),
                                    tint = dynamicColor.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
        //
    }

    @Composable
    private fun ChatScreen(modifier: Modifier, navController: NavController, context: Context, service: ChatService) {
        Chat(modifier, navController, context, service).Screen()
    }

    @Composable
    private fun SettingsScreen(modifier: Modifier, navController: NavController, context: Context, sharedPreferences: SharedPreferences) {
        AppSettings(modifier, navController, context, sharedPreferences).Screen()
    }

    @Composable
    private fun FullScreenEffect(activity: ComponentActivity) {
        val view = LocalView.current
        LaunchedEffect(view) {
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars()) // tuşlar ve statusbar'ı gizle
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE // alttan ya da üstten çekince göster
        }
    }

    @Composable
    private fun SwitchButton(
        modifier: Modifier,
        size: Float,
        enabled: Boolean= true,
        colors: SwitchColors = SwitchDefaults.colors() )
    {
        val switchScope= rememberCoroutineScope()
        Switch(
            modifier = modifier.scale(size),
            checked = isChat.value,
            enabled = enabled,
            onCheckedChange = {
                isChat.value = it
                switchScope.launch {
                    if (bluetoothAdapter.isEnabled) {
                        if (isChat.value) {
                            chatService.startServer()
                        } else {
                            chatService.stopServer()
                            //discoveredDevices.clear()
                            remoteDevice.value= null
                        }
                    }
                }
                if (isChat.value) {
                    setDiscoverable()
                }
            },
            colors = colors
        )
    }

    @Composable
    private fun DiscoveryTextAnimation(text: String, modifier: Modifier, color: Color) {
        val breakIterator = remember(text) { BreakIterator.getCharacterInstance() }
        var substringText by remember { mutableStateOf("") }
        //
        LaunchedEffect(text) {
            //
            delay(200L)
            breakIterator.text = StringCharacterIterator(text)

            var nextIndex = breakIterator.next()
            while (nextIndex != BreakIterator.DONE) {
                substringText = text.subSequence(0, nextIndex).toString()
                nextIndex = breakIterator.next()
                delay(20L)
            }
        }
        Text(text = substringText, modifier = modifier, color = color)
    }

    private fun setDiscoverable() {
        if (bluetoothAdapter.isEnabled && !setsPref.getBoolean("sepServer", false) ) {
            val discoverableIntent= Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, setsPref.getInt("discoTime", 150))
            }
            discoverableLauncher.launch(discoverableIntent)
        }
    }

    private fun compactScreen() : Boolean {
        val metrics = WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(this)
        val width = metrics.bounds.width()
        val height = metrics.bounds.height()
        val density = resources.displayMetrics.density
        val windowSizeClass = WindowSizeClass.compute(width/density, height/density)

        return windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT ||
                windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
    }

    private fun turnOnBt() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }

    private fun pairDevice(btDevice: BluetoothDevice) {
        val method= btDevice::class.java.getMethod("createBond")
        val pairDialog= AlertDialog.Builder(this)
        pairDialog.setMessage(getString(R.string.want_to_pair))
        pairDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
            method.invoke(btDevice)
        }
        pairDialog.setNegativeButton(getString(R.string.no)) { _, _ ->
            Toast.makeText(this, getString(R.string.pairing_cancelled), Toast.LENGTH_SHORT).show()
        }
        pairDialog.show()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun allPermissionsGranted(): Boolean = rememberMultiplePermissionsState(listOfPerms).allPermissionsGranted

    override fun onConnectionStateChanged(state: Boolean) {
        isConnected.value= state
    }

    override fun onSecureConnection(device: BluetoothDevice?) {
        if (device != null && !discoveredDevices.contains(device)) {
            remoteDevice.value= device
            hasRemote.value= true
            discoveredDevices.add(device)
        } else {
            remoteDevice.value= null
            //discoveredDevices.clear()
            hasRemote.value= false
        }
        if (device != null && discoveredDevices.contains(device)){
            val deviceIndex= discoveredDevices.indexOf(device)
            discoveredDevices.removeAt(deviceIndex)
            discoveredDevices.add(device)
        }
    }

    override fun onMessageReceived(message: String) {
        messages.add(Message(message, true))
    }

    override fun onMessageSent(outgoing: String) {
        messages.add(Message(outgoing, false))
    }

}