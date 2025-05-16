package com.example.signal3

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorVisualizationScreen(signCaptureViewModel: SignCaptureViewModel) {
    // Access shared state
    val isCapturing = signCaptureViewModel.isCapturing.value
    val currentLetter = signCaptureViewModel.currentLetter.value
    val detectedLetters = signCaptureViewModel.detectedLetters.value
    val isDeviceConnected = signCaptureViewModel.isDeviceConnected.value
    
    // Use the flex values directly from SignCaptureViewModel for real-time updates
    val flexValues = signCaptureViewModel.currentFlexValues.value
    val gyroValues = signCaptureViewModel.gyroValues.value
    val accelValues = signCaptureViewModel.accelValues.value
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Sensors", color = Color.White)
                        if (isCapturing && currentLetter.isNotEmpty()) {
                            Text(
                                "Currently Capturing: $currentLetter",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
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
            // Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDeviceConnected) 
                        Color(0xFFE8F5E9) // Light green when connected
                    else 
                        Color(0xFFFFEBEE) // Light red when disconnected
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Connection status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDeviceConnected) Color(0xFF4CAF50) 
                                else Color(0xFFF44336)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (isDeviceConnected) 
                            "Device Connected" 
                        else 
                            "Device Disconnected - Connect in Home tab",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Capture status card if active
            if (isCapturing || detectedLetters.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCapturing) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isCapturing) "Capturing Signs..." else "Capture Complete",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (detectedLetters.isNotEmpty()) {
                            Text(
                                text = "Detected: $detectedLetters",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (isCapturing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { signCaptureViewModel.stopCapturing() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Stop Capture")
                            }
                        } else if (detectedLetters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { signCaptureViewModel.startCapturing() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Start New Capture")
                            }
                        }
                    }
                }
            }
            
            // Live Sensor Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Live Sensor Data",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    if (!isDeviceConnected) {
                        Text(
                            text = "Connect device to see live sensor data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Flex Sensors Visualization
                    Text(
                        text = "Flex Sensors",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display flex sensors as vertical bars with values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        flexValues.forEachIndexed { index, value ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Dynamic color based on sensor value
                                val flexColor = when {
                                    value < 0.3f -> Color(0xFF4CAF50) // Green for low flex
                                    value < 0.7f -> Color(0xFFFFA000) // Amber for medium flex
                                    else -> Color(0xFFF44336) // Red for high flex
                                }
                                
                                // Flex value as text
                                Text(
                                    text = String.format("%.2f", value),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Vertical bar
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    // Fill based on flex value
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .height((value * 100).dp)
                                            .background(flexColor)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Finger label
                                Text(
                                    text = when (index) {
                                        0 -> "Thumb"
                                        1 -> "Index"
                                        2 -> "Middle"
                                        3 -> "Ring"
                                        else -> "Pinky"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // IMU Sensor Data - Accelerometer and Gyroscope
                    Text(
                        text = "IMU Sensors",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Accelerometer
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Accelerometer (g)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "X: ${String.format("%.2f", accelValues.first)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            LinearProgressIndicator(
                                progress = ((accelValues.first + 3f) / 6f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF2196F3),
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Y: ${String.format("%.2f", accelValues.second)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            LinearProgressIndicator(
                                progress = ((accelValues.second + 3f) / 6f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF4CAF50),
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Z: ${String.format("%.2f", accelValues.third)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            LinearProgressIndicator(
                                progress = (accelValues.third / 20f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFF9800),
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Gyroscope
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Gyroscope (°/s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "X: ${String.format("%.2f", gyroValues.first)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            LinearProgressIndicator(
                                progress = ((gyroValues.first + 20f) / 40f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF44336),
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Y: ${String.format("%.2f", gyroValues.second)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            LinearProgressIndicator(
                                progress = ((gyroValues.second + 20f) / 40f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF9C27B0),
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Z: ${String.format("%.2f", gyroValues.third)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            LinearProgressIndicator(
                                progress = ((gyroValues.third + 20f) / 40f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF009688),
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
            
            // Hand Visualization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Hand Visualization",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Hand drawing canvas
                    HandVisualization(
                        flexValues = flexValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                    
                    if (currentLetter.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Current Gesture: $currentLetter",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Capture Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Capture Controls",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Connect/Disconnect Button
                        Button(
                            onClick = { signCaptureViewModel.toggleDeviceConnection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeviceConnected) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                contentColor = if (isDeviceConnected) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        ) {
                            Text(if (isDeviceConnected) "Disconnect" else "Connect")
                        }
                        
                        // Start/Stop Capture Button
                        Button(
                            onClick = {
                                if (isCapturing) {
                                    signCaptureViewModel.stopCapturing()
                                } else {
                                    signCaptureViewModel.startCapturing()
                                }
                            },
                            enabled = isDeviceConnected,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCapturing) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                },
                                contentColor = if (isCapturing) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(if (isCapturing) "Stop Capture" else "Start Capture")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSensorValue(axis: String, value: Float, unit: String, accentColor: Color) {
    // Animate background color based on value changes
    val animatedColor = remember(value) { 
        if (value > 0) accentColor.copy(alpha = (value.coerceIn(0f, 20f) / 20f).coerceIn(0.05f, 0.3f))
        else accentColor.copy(alpha = 0.05f)
    }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = animatedColor
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp + (value.coerceIn(0f, 10f) / 5f).dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Axis label
            Text(
                text = axis,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            
            // Value with prominent display
            Text(
                text = "%.1f".format(value),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f + (kotlin.math.abs(value) % 5) / 15f
                )
            )
            
            // Unit
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SensorValueDisplay(axis: String, value: Float, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = axis,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "%.1f".format(value),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun HandVisualization(flexValues: List<Float>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val handLength = size.width * 0.4f
        
        // Draw palm with slight displacement based on gyro values
        drawCircle(
            color = Color.Gray.copy(alpha = 0.5f),
            radius = size.width * 0.15f,
            center = center
        )
        
        // Draw fingers with more realistic movement
        for (i in flexValues.indices) {
            // Add slight angle variations based on gyro for natural hand position
            val angle = -Math.PI / 2 + (i - 2) * Math.PI / 8
            val fingerLength = handLength * (1f - flexValues[i] * 0.7f)
            
            // Add micro-tremors to finger positions
            val tremor = sin(System.currentTimeMillis() / (80f + i * 20)) * 2f
            
            val fingerEnd = Offset(
                center.x + (fingerLength * kotlin.math.cos(angle)).toFloat() + tremor,
                center.y + (fingerLength * kotlin.math.sin(angle)).toFloat() + tremor * 0.7f
            )
            
            drawLine(
                color = Color.Red,
                start = center,
                end = fingerEnd,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
            
            // Draw finger joints with micro-movements
            val jointTremor = sin(System.currentTimeMillis() / (90f + i * 30) + i * 1000) * 1.5f
            drawCircle(
                color = Color.Red.copy(alpha = 0.7f),
                radius = 6f,
                center = Offset(
                    center.x + (fingerLength * 0.5f * kotlin.math.cos(angle)).toFloat() + jointTremor,
                    center.y + (fingerLength * 0.5f * kotlin.math.sin(angle)).toFloat() + jointTremor * 0.7f
                )
            )
            
            drawCircle(
                color = Color.Red.copy(alpha = 0.7f),
                radius = 8f,
                center = fingerEnd
            )
        }
    }
} 