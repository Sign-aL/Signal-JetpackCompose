package com.example.signal3

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private const val PREF_SPEECH_OUTPUT = "speech_output"
private const val PREF_MEDICAL_PROFILE = "medical_profile"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    geminiViewModel: GeminiAiViewModel,
    textToSpeechManager: TextToSpeechManager,
    signCaptureViewModel: SignCaptureViewModel
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("signal_prefs", Context.MODE_PRIVATE) }
    val speechEnabled = remember { sharedPrefs.getBoolean(PREF_SPEECH_OUTPUT, true) }
    
    // Device connection status now comes from the ViewModel
    val isDeviceConnected = signCaptureViewModel.isDeviceConnected.value
    
    // Load medical profile info
    val savedProfileJson = remember { sharedPrefs.getString(PREF_MEDICAL_PROFILE, "") ?: "" }
    val medicalProfile = remember { if (savedProfileJson.isNotEmpty()) MedicalProfile.fromJson(savedProfileJson) else null }
    
    // Check if we should display medical info
    var showMedicalInfo by remember { mutableStateOf(false) }
    
    // Access shared sign capture state
    val isCapturing = signCaptureViewModel.isCapturing.value
    val currentLetter = signCaptureViewModel.currentLetter.value
    val detectedLetters = signCaptureViewModel.detectedLetters.value
    val showPulse = signCaptureViewModel.showPulse.value
    
    val geminiUiState by geminiViewModel.uiState.collectAsState()
    
    // Auto-speak when a successful response is received - if enabled
    LaunchedEffect(geminiUiState, detectedLetters) {
        // Process completed signs with Gemini when capture is complete
        if (!isCapturing && detectedLetters.isNotEmpty() && 
            detectedLetters.length == signCaptureViewModel.lettersToCapture.size &&
            geminiUiState !is GeminiUiState.Loading && geminiUiState !is GeminiUiState.Success) {
            geminiViewModel.processSignLanguage(detectedLetters)
        }
        
        // Handle text-to-speech
        if (geminiUiState is GeminiUiState.Success && speechEnabled) {
            val response = (geminiUiState as GeminiUiState.Success).response
            textToSpeechManager.speak(response)
            
            // Check if we should show medical info (if keywords are detected)
            val medicalKeywords = listOf(
                "doctor", "emergency", "hospital", "medical", "medicine", "pain", 
                "sick", "help", "allergy", "allergic", "health", "condition", "hurt"
            )
            showMedicalInfo = medicalKeywords.any { keyword -> 
                response.contains(keyword, ignoreCase = true) 
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Signal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "One-handed sign language translation powered by AI",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDeviceConnected) 
                        Color(0xFFE8F5E9) // Light green when connected
                    else 
                        Color(0xFFFFEBEE) // Light red when disconnected
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon based on connection status
                    Icon(
                        imageVector = if (isDeviceConnected) 
                            Icons.Default.Bluetooth 
                        else 
                            Icons.Default.BluetoothDisabled,
                        contentDescription = "Connection Status",
                        tint = if (isDeviceConnected) 
                            Color(0xFF4CAF50) // Green
                        else 
                            Color(0xFFF44336), // Red
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Status text
                    Column {
                        Text(
                            text = if (isDeviceConnected) "Device Connected" else "Device Disconnected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDeviceConnected) 
                                "Signal glove is ready to translate" 
                            else 
                                "Connect to the Signal glove to begin",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Connect/Disconnect button
                    Button(
                        onClick = { signCaptureViewModel.toggleDeviceConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeviceConnected) 
                                Color(0xFFF44336) // Red for disconnect
                            else 
                                Color(0xFF4CAF50) // Green for connect
                        )
                    ) {
                        Text(
                            text = if (isDeviceConnected) "Disconnect" else "Connect",
                            color = Color.White
                        )
                    }
                }
            }
            
            // Sign Detection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCapturing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gesture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Detected Signs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Current letter being captured indicator
                        if (isCapturing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Letter indicator
                                if (currentLetter.isNotEmpty()) {
                                    Text(
                                        text = "Capturing: $currentLetter",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                
                                // Pulsing detection indicator
                                AnimatedVisibility(
                                    visible = showPulse,
                                    enter = fadeIn(animationSpec = tween(150)),
                                    exit = fadeOut(animationSpec = tween(150))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Detecting signs...")
                            }
                        } else if (detectedLetters.isNotEmpty()) {
                            Text(
                                text = detectedLetters,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Begin signing to detect ASL finger spelling",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Capture button
                    Button(
                        onClick = { 
                            if (isDeviceConnected) {
                                signCaptureViewModel.startCapturing() 
                            }
                        },
                        enabled = !isCapturing && isDeviceConnected,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver, 
                            contentDescription = "Capture", 
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capture Signs")
                    }
                }
            }
            
            // Divider with text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f))
                
                Text(
                    text = "   AI Translation   ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Divider(modifier = Modifier.weight(1f))
            }
            
            // Gemini AI Translation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (geminiUiState is GeminiUiState.Success) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (geminiUiState is GeminiUiState.Success) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gemini AI Translation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (geminiUiState is GeminiUiState.Success) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (geminiUiState is GeminiUiState.Success) {
                            IconButton(
                                onClick = {
                                    val response = (geminiUiState as GeminiUiState.Success).response
                                    textToSpeechManager.speak(response)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = if (geminiUiState is GeminiUiState.Success) {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (geminiUiState) {
                            is GeminiUiState.Initial -> {
                                if (detectedLetters.isEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lightbulb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = "Capture signs to get AI translation",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Ready to translate",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            is GeminiUiState.Loading -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val loadingState = geminiUiState as GeminiUiState.Loading
                                    
                                    // Progress indicator with actual progress
                                    androidx.compose.material3.LinearProgressIndicator(
                                        progress = { loadingState.progress },
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    
                                    // Status message
                                    Text(
                                        loadingState.statusMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Percentage display
                                    Text(
                                        "${(loadingState.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            is GeminiUiState.Success -> {
                                Text(
                                    text = (geminiUiState as GeminiUiState.Success).response,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            is GeminiUiState.Error -> {
                                Text(
                                    text = (geminiUiState as GeminiUiState.Error).message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            // Medical Profile Information Card (only shown when medical keywords are detected)
            AnimatedVisibility(
                visible = showMedicalInfo && medicalProfile != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                // Only render if we have a medical profile
                if (medicalProfile != null) {
                    // Divider with text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "   Health Information   ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Divider(modifier = Modifier.weight(1f))
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Important Health Information",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                IconButton(
                                    onClick = { /* Share medical profile */ }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Health Info",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Patient info
                            if (medicalProfile.fullName.isNotEmpty()) {
                                SectionRow("Patient", medicalProfile.fullName)
                            }
                            
                            if (medicalProfile.dateOfBirth.isNotEmpty()) {
                                SectionRow("Date of Birth", medicalProfile.dateOfBirth)
                            }
                            
                            if (medicalProfile.bloodType.isNotEmpty()) {
                                SectionRow("Blood Type", medicalProfile.bloodType)
                            }
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                            )
                            
                            // Medical conditions
                            if (medicalProfile.medicalConditions.isNotEmpty()) {
                                SectionRow("Medical Conditions", medicalProfile.medicalConditions)
                            }
                            
                            if (medicalProfile.allergies.isNotEmpty()) {
                                SectionRow("Allergies", medicalProfile.allergies)
                            }
                            
                            if (medicalProfile.medications.isNotEmpty()) {
                                SectionRow("Medications", medicalProfile.medications)
                            }
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                            )
                            
                            // Emergency contact
                            if (medicalProfile.emergencyContact.isNotEmpty()) {
                                SectionRow("Emergency Contact", medicalProfile.emergencyContact)
                            }
                            
                            if (medicalProfile.emergencyPhone.isNotEmpty()) {
                                SectionRow("Emergency Phone", medicalProfile.emergencyPhone)
                            }
                            
                            if (medicalProfile.additionalNotes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Additional Notes",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = medicalProfile.additionalNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.width(120.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
    }
} 