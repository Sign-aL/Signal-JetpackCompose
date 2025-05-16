package com.example.signal3

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val PREF_GEMINI_API_KEY = "gemini_api_key"
private const val PREF_SPEECH_OUTPUT = "speech_output"
private const val PREF_VIBRATION_FEEDBACK = "vibration_feedback"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(geminiViewModel: GeminiAiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("signal_prefs", Context.MODE_PRIVATE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var apiKey by remember { mutableStateOf(sharedPrefs.getString(PREF_GEMINI_API_KEY, "") ?: "") }
    var sampleRate by remember { mutableStateOf(100f) }
    var useSpeechOutput by remember { mutableStateOf(sharedPrefs.getBoolean(PREF_SPEECH_OUTPUT, true)) }
    var useVibrationFeedback by remember { mutableStateOf(sharedPrefs.getBoolean(PREF_VIBRATION_FEEDBACK, true)) }
    
    // Initialize Gemini with stored API key on first load
    LaunchedEffect(Unit) {
        geminiViewModel.initializeWithApiKey(apiKey)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gemini API Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gemini AI Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "Get your API key from Google AI Studio"
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Enter your Gemini API key to enable AI-powered sign language translation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                // Save API key to preferences
                                sharedPrefs.edit().putString(PREF_GEMINI_API_KEY, apiKey).apply()
                                // Initialize the Gemini model with the new key
                                geminiViewModel.initializeWithApiKey(apiKey)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("API Key saved")
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                            }
                        }
                    )
                    
                    if (apiKey.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Without an API key, Signal will use simulated responses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Sensor Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sensor Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Sampling Rate: ${sampleRate.toInt()} Hz",
                         style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = sampleRate,
                        onValueChange = { sampleRate = it },
                        valueRange = 10f..200f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    SettingsSwitchRow(
                        title = "Calibration Mode",
                        description = "Show raw sensor values and calibration tools",
                        checked = false,
                        onCheckedChange = { /* No change in demo */ }
                    )
                }
            }
            
            // Accessibility Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accessibility Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingsSwitchRow(
                        title = "Speech Output",
                        description = "Read translations aloud using text-to-speech",
                        checked = useSpeechOutput,
                        onCheckedChange = { 
                            useSpeechOutput = it 
                            // Save to preferences
                            sharedPrefs.edit().putBoolean(PREF_SPEECH_OUTPUT, it).apply()
                        }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SettingsSwitchRow(
                        title = "Vibration Feedback",
                        description = "Vibrate when signs are recognized",
                        checked = useVibrationFeedback,
                        onCheckedChange = { 
                            useVibrationFeedback = it 
                            // Save to preferences
                            sharedPrefs.edit().putBoolean(PREF_VIBRATION_FEEDBACK, it).apply()
                        }
                    )
                }
            }
            
            // About section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Signal v1.0",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "One-handed sign language translation device powered by AI",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Signal uses 5 flex sensors and an MPU9050 module to detect hand gestures, " +
                                "which are then interpreted using Vertex AI Gemini to provide spoken translations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
} 