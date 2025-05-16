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
    // Simulated sensor data with always dynamic values
    var flexValues by remember { mutableStateOf(List(5) { 0.5f }) }
    var gyroValues by remember { mutableStateOf(Triple(12.5f, 8.3f, 5.7f)) } 
    var accelValues by remember { mutableStateOf(Triple(0.7f, 0.4f, 9.8f)) }
    
    // Access shared capture state
    val isCapturing = signCaptureViewModel.isCapturing.value
    val currentLetter = signCaptureViewModel.currentLetter.value
    val detectedLetters = signCaptureViewModel.detectedLetters.value
    val isDeviceConnected = signCaptureViewModel.isDeviceConnected.value
    
    // Debug logs for gyro values
    val lastGyroValues = remember { mutableStateOf(gyroValues) }
    DisposableEffect(gyroValues) {
        if (gyroValues != lastGyroValues.value) {
            println("GyroValues changed: ${gyroValues.first}, ${gyroValues.second}, ${gyroValues.third}")
            lastGyroValues.value = gyroValues
        }
        onDispose { }
    }
    
    // Continuous sensor data simulation that always runs regardless of capture state
    LaunchedEffect(Unit) {
        var time = System.currentTimeMillis() / 1000f
        
        while (true) {
            time = System.currentTimeMillis() / 1000f
            
            // Always update flex values with dynamic changes when not capturing
            if (!isCapturing || currentLetter.isEmpty()) {
                // Generate realistic-looking sensor data with natural drift and micro-movements
                flexValues = List(5) { i ->
                    val baseValue = sin((time + i * 0.5f) * 1.5f) * 0.4f + 0.5f
                    val randomJitter = (Random.nextFloat() - 0.5f) * 0.08f
                    val microJitter = sin(System.currentTimeMillis() / 50f + i * 10) * 0.02f
                    
                    // Combine base value with jitter components
                    val value = baseValue + randomJitter + microJitter
                    value.coerceIn(0.1f, 0.9f)
                }
            } else {
                // Use flex values from the signCaptureViewModel when capturing
                flexValues = signCaptureViewModel.currentFlexValues.value
            }
            
            // ALWAYS update gyro with dynamic values
            gyroValues = Triple(
                5.5f * sin(time * 0.3f) + 2.8f * sin(time * 5f) + (Random.nextFloat() - 0.5f) * 1.5f,
                4.2f * sin(time * 0.2f + 1f) + 1.6f * sin(time * 4.5f) + (Random.nextFloat() - 0.5f) * 1.2f,
                3.0f * sin(time * 0.1f + 2f) + 1.5f * sin(time * 4f) + (Random.nextFloat() - 0.5f) * 1.4f
            )
            
            // ALWAYS update accelerometer with dynamic values
            accelValues = Triple(
                0.2f + sin(time * 2f) * 0.3f + (Random.nextFloat() - 0.5f) * 0.4f,
                0.1f + sin(time * 1.7f) * 0.2f + (Random.nextFloat() - 0.5f) * 0.5f,
                9.8f + sin(time * 3f) * 0.15f + (Random.nextFloat() - 0.5f) * 0.3f
            )
            
            delay(20)  // Update at 50Hz for more frequent updates and visible shakiness
        }
    }
    
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isCapturing) "Capturing Sign: $currentLetter" else "Captured Signs: $detectedLetters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Start capture button
                        Button(
                            onClick = { signCaptureViewModel.startCapturing() },
                            enabled = !isCapturing && isDeviceConnected,
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
                            Text("Start Capture")
                        }
                    }
                }
            }
            
            // Flex Sensors Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Flex Sensors",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (isCapturing && currentLetter.isNotEmpty()) {
                            Text(
                                text = "Showing values for: $currentLetter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    flexValues.forEachIndexed { index, value ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Finger ${index + 1}",
                                modifier = Modifier.width(60.dp)
                            )
                            
                            LinearProgressIndicator(
                                progress = value,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(16.dp) // Increased height
                                    .clip(RoundedCornerShape(8.dp)),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                color = if (isCapturing) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    // Dynamic color based on value for better visibility
                                    Color(
                                        red = 0.3f + value * 0.5f,
                                        green = 0.2f + (1f - value) * 0.5f,
                                        blue = 0.7f,
                                        alpha = 0.8f
                                    )
                                },
                                strokeCap = StrokeCap.Round
                            )
                            
                            // Value display with more prominence
                            Text(
                                text = "${(value * 1023).toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(50.dp)
                                    .padding(start = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            // Gyroscope and Accelerometer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MPU9050 Module",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gyroscope header with active indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Gyroscope",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Animated indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        
                        // Display raw values for debugging
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "(${gyroValues.first.toInt()}, ${gyroValues.second.toInt()}, ${gyroValues.third.toInt()})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // New enhanced display for gyroscope values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EnhancedSensorValue("X", gyroValues.first, "°/s", MaterialTheme.colorScheme.primary)
                        EnhancedSensorValue("Y", gyroValues.second, "°/s", MaterialTheme.colorScheme.secondary)
                        EnhancedSensorValue("Z", gyroValues.third, "°/s", MaterialTheme.colorScheme.tertiary)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Accelerometer header with active indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Accelerometer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Animated indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        
                        // Display raw values for debugging
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "(${accelValues.first.toInt()}, ${accelValues.second.toInt()}, ${accelValues.third.toInt()})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // New enhanced display for accelerometer values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EnhancedSensorValue("X", accelValues.first, "m/s²", MaterialTheme.colorScheme.primary)
                        EnhancedSensorValue("Y", accelValues.second, "m/s²", MaterialTheme.colorScheme.secondary)
                        EnhancedSensorValue("Z", accelValues.third, "m/s²", MaterialTheme.colorScheme.tertiary)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 3D visualization
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        HandVisualization(flexValues, gyroValues)
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
fun HandVisualization(flexValues: List<Float>, gyroValues: Triple<Float, Float, Float>) {
    Canvas(modifier = Modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val handLength = size.width * 0.4f
        
        // Use gyroscope values to slightly tilt the whole hand (scale down the values)
        val handTiltX = gyroValues.first / 100f
        val handTiltY = gyroValues.second / 100f
        
        // Draw palm with slight displacement based on gyro values
        drawCircle(
            color = Color.Gray.copy(alpha = 0.5f),
            radius = size.width * 0.15f,
            center = center.copy(
                x = center.x + handTiltX * size.width,
                y = center.y + handTiltY * size.height
            )
        )
        
        // Draw fingers with more realistic movement
        for (i in flexValues.indices) {
            // Add slight angle variations based on gyro for natural hand position
            val angle = -Math.PI / 2 + (i - 2) * Math.PI / 8 + handTiltX * 0.3 + (if (i % 2 == 0) handTiltY * 0.2 else -handTiltY * 0.2)
            val fingerLength = handLength * (1f - flexValues[i] * 0.7f)
            
            // Add micro-tremors to finger positions
            val tremor = sin(System.currentTimeMillis() / (80f + i * 20)) * 2f
            
            val fingerEnd = Offset(
                center.x + (fingerLength * kotlin.math.cos(angle)).toFloat() + handTiltX * size.width + tremor,
                center.y + (fingerLength * kotlin.math.sin(angle)).toFloat() + handTiltY * size.height + tremor * 0.7f
            )
            
            val palmCenter = center.copy(
                x = center.x + handTiltX * size.width,
                y = center.y + handTiltY * size.height
            )
            
            drawLine(
                color = Color.Red,
                start = palmCenter,
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
                    palmCenter.x + (fingerLength * 0.5f * kotlin.math.cos(angle)).toFloat() + jointTremor,
                    palmCenter.y + (fingerLength * 0.5f * kotlin.math.sin(angle)).toFloat() + jointTremor * 0.7f
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