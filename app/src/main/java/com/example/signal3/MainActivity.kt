package com.example.signal3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.signal3.ui.theme.Signal3Theme

class MainActivity : ComponentActivity() {
    private val signCaptureViewModel by lazy { SignCaptureViewModel() }
    
    // Required permissions for BLE
    private val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all required permissions are granted
        val allGranted = permissions.entries.all { it.value }
        
        if (allGranted) {
            // Initialize BLE when permissions are granted
            signCaptureViewModel.initializeBleManager(this)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request BLE permissions
        checkAndRequestBlePermissions()
        
        // Initialize BLE connection manager if permissions are granted
        if (arePermissionsGranted()) {
            signCaptureViewModel.initializeBleManager(this)
        }
        
        setContent {
            Signal3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(signCaptureViewModel)
                }
            }
        }
    }
    
    private fun checkAndRequestBlePermissions() {
        if (!arePermissionsGranted()) {
            requestPermissionLauncher.launch(blePermissions)
        }
    }
    
    private fun arePermissionsGranted(): Boolean {
        return blePermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun MainContent(providedSignCaptureViewModel: SignCaptureViewModel? = null) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("signal_prefs", Context.MODE_PRIVATE) }
    val textToSpeechManager = remember { TextToSpeechManager(context) }
    val geminiViewModel: GeminiAiViewModel = viewModel()
    val signCaptureViewModel = providedSignCaptureViewModel ?: viewModel()
    var showIntro by remember { mutableStateOf(true) }
    
    // Initialize GeminiAI with saved API key
    LaunchedEffect(Unit) {
        val savedApiKey = sharedPrefs.getString("gemini_api_key", "") ?: ""
        geminiViewModel.initializeWithApiKey(savedApiKey)
        
        // Initialize BLE manager if not already initialized
        signCaptureViewModel.initializeBleManager(context)
    }
    
    DisposableEffect(key1 = Unit) {
        onDispose {
            textToSpeechManager.shutdown()
        }
    }
    
    if (showIntro) {
        IntroScreen(onFinished = { showIntro = false })
    } else {
        SignalApp(
            textToSpeechManager = textToSpeechManager,
            geminiViewModel = geminiViewModel,
            signCaptureViewModel = signCaptureViewModel
        )
    }
}

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", "Home", { Icon(Icons.Default.Home, contentDescription = "Home") })
    object Sensors : Screen("sensors", "Sensors", { Icon(Icons.Default.Sensors, contentDescription = "Sensors") })
    object Profile : Screen("profile", "Profile", { Icon(Icons.Default.Person, contentDescription = "Profile") })
    object Settings : Screen("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
}

@Composable
fun SignalApp(textToSpeechManager: TextToSpeechManager, geminiViewModel: GeminiAiViewModel, signCaptureViewModel: SignCaptureViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Sensors, Screen.Profile, Screen.Settings)
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon() },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    geminiViewModel = geminiViewModel,
                    textToSpeechManager = textToSpeechManager,
                    signCaptureViewModel = signCaptureViewModel
                )
            }
            composable(Screen.Sensors.route) {
                SensorVisualizationScreen(
                    signCaptureViewModel = signCaptureViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(geminiViewModel = geminiViewModel)
            }
        }
    }
}