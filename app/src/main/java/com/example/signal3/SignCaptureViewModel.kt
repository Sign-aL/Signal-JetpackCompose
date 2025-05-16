package com.example.signal3

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SignCaptureViewModel : ViewModel() {
    // Detected letters
    val detectedLetters = mutableStateOf("")
    val currentLetter = mutableStateOf("")
    val captureIndex = mutableStateOf(0)
    val isCapturing = mutableStateOf(false)
    val showPulse = mutableStateOf(false)
    
    // Device connection status - shared across all screens
    val isDeviceConnected = mutableStateOf(false)
    
    // BLE manager reference
    private var bleManager: BleConnectionManager? = null
    
    // Sensor values from BLE device
    val flexValues = mutableStateOf(List(5) { 0.5f })
    val accelValues = mutableStateOf(Triple(0f, 0f, 0f))
    val gyroValues = mutableStateOf(Triple(0f, 0f, 0f))
    
    // Letters to capture one by one, without spaces
    val lettersToCapture = "CHESTPAIN".toCharArray()
    
    // Sensor readings mapped to letters for display
    private val baseLetterFlexValues = mapOf(
        'C' to listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.1f),
        'H' to listOf(0.1f, 0.1f, 0.8f, 0.8f, 0.1f),
        'E' to listOf(0.8f, 0.8f, 0.8f, 0.8f, 0.1f),
        'S' to listOf(0.1f, 0.8f, 0.8f, 0.8f, 0.1f),
        'T' to listOf(0.8f, 0.1f, 0.1f, 0.1f, 0.8f),
        'P' to listOf(0.1f, 0.1f, 0.8f, 0.8f, 0.8f),
        'A' to listOf(0.1f, 0.8f, 0.8f, 0.8f, 0.8f),
        'I' to listOf(0.9f, 0.9f, 0.9f, 0.9f, 0.2f),
        'N' to listOf(0.1f, 0.1f, 0.1f, 0.8f, 0.8f),
        'M' to listOf(0.8f, 0.8f, 0.8f, 0.1f, 0.1f),
        'U' to listOf(0.1f, 0.1f, 0.8f, 0.1f, 0.1f),
        'G' to listOf(0.8f, 0.1f, 0.1f, 0.1f, 0.8f),
        'R' to listOf(0.1f, 0.8f, 0.1f, 0.8f, 0.8f),
        'Y' to listOf(0.1f, 0.8f, 0.8f, 0.8f, 0.1f)
    )
    
    // Current flex values for the active letter with added noise for realism
    val currentFlexValues = mutableStateOf(List(5) { 0.5f })
    
    // Track capture job so we can cancel it when needed
    private var captureJob: Job? = null
    
    /**
     * Initialize BLE Manager
     */
    fun initializeBleManager(context: Context) {
        if (bleManager == null) {
            bleManager = BleConnectionManager(context).apply {
                // Set connection listener
                setConnectionListener(object : BleConnectionManager.ConnectionListener {
                    override fun onConnected() {
                        isDeviceConnected.value = true
                    }
                    
                    override fun onDisconnected() {
                        isDeviceConnected.value = false
                        if (isCapturing.value) {
                            stopCapturing()
                        }
                    }
                    
                    override fun onConnectionFailed() {
                        isDeviceConnected.value = false
                        if (isCapturing.value) {
                            stopCapturing()
                        }
                    }
                })
                
                // Set data listener to receive letters
                setDataListener(object : BleConnectionManager.DataListener {
                    override fun onDataReceived(letter: Char, sensorData: Map<String, Any>?) {
                        // Add letter to detected letters
                        if (letter.isLetter() || letter == ' ' || letter == '\'') {
                            currentLetter.value = letter.toString()
                            detectedLetters.value += currentLetter.value
                            
                            // Pulse animation
                            viewModelScope.launch {
                                showPulse.value = true
                                delay(300)
                                showPulse.value = false
                            }
                            
                            // If we have sensor data, update the values
                            sensorData?.let { data ->
                                // Parse flex values if available
                                (data["flex"] as? List<Int>)?.let { flexList ->
                                    // Convert raw flex values (0-4095) to normalized values (0.0-1.0)
                                    currentFlexValues.value = flexList.map { it / 4095f }
                                }
                                
                                // Parse accelerometer values if available
                                (data["accel"] as? List<Float>)?.let { accelList ->
                                    if (accelList.size >= 3) {
                                        accelValues.value = Triple(accelList[0], accelList[1], accelList[2])
                                    }
                                }
                                
                                // Parse gyroscope values if available
                                (data["gyro"] as? List<Float>)?.let { gyroList ->
                                    if (gyroList.size >= 3) {
                                        gyroValues.value = Triple(gyroList[0], gyroList[1], gyroList[2])
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }
    
    /**
     * Connect to the Signal device
     */
    fun connectDevice() {
        bleManager?.let {
            if (!it.isConnected()) {
                it.startScan()
            }
            isDeviceConnected.value = bleManager?.isConnected() ?: false
        }
    }
    
    /**
     * Disconnect from the Signal device
     */
    fun disconnectDevice() {
        bleManager?.disconnect()
        isDeviceConnected.value = false
        // If capturing, stop it when disconnecting
        if (isCapturing.value) {
            stopCapturing()
        }
    }
    
    /**
     * Toggle the device connection state
     */
    fun toggleDeviceConnection() {
        if (isDeviceConnected.value) {
            disconnectDevice()
        } else {
            connectDevice()
        }
    }
    
    fun startCapturing() {
        // Only allow capturing if device is connected
        if (!isDeviceConnected.value) return
        
        detectedLetters.value = ""
        currentLetter.value = ""
        captureIndex.value = 0
        isCapturing.value = true
        
        // When using real BLE, we don't need to simulate letters
        // The BLE callback will handle incoming letters
        // We'll just keep the capturing state active
        
        // Start a job to simulate dynamic sensor values while waiting for real data
        startSensorSimulation()
    }
    
    // Simulates sensor values when no real data is present
    private fun startSensorSimulation() {
        captureJob?.cancel()
        
        captureJob = viewModelScope.launch {
            var time = System.currentTimeMillis() / 1000f
            
            while (isCapturing.value) {
                time = System.currentTimeMillis() / 1000f
                
                // If we don't have a current letter from BLE yet, simulate dynamic values
                if (currentLetter.value.isEmpty()) {
                    // Generate sensor data with natural motion
                    currentFlexValues.value = List(5) { i ->
                        val baseValue = 0.5f + 0.4f * sin((time + i * 0.5f) * 0.8f)
                        val randomJitter = (Random.nextFloat() - 0.5f) * 0.05f
                        (baseValue + randomJitter).coerceIn(0.1f, 0.9f)
                    }
                    
                    // Update gyro with dynamic values
                    gyroValues.value = Triple(
                        4.0f * sin(time * 0.3f) + (Random.nextFloat() - 0.5f) * 1.0f,
                        3.0f * sin(time * 0.2f + 1f) + (Random.nextFloat() - 0.5f) * 1.0f,
                        2.0f * sin(time * 0.1f + 2f) + (Random.nextFloat() - 0.5f) * 1.0f
                    )
                    
                    // Update accelerometer with dynamic values
                    accelValues.value = Triple(
                        0.2f + sin(time * 0.5f) * 0.3f + (Random.nextFloat() - 0.5f) * 0.2f,
                        0.1f + sin(time * 0.4f) * 0.2f + (Random.nextFloat() - 0.5f) * 0.2f,
                        9.8f + sin(time * 0.3f) * 0.1f + (Random.nextFloat() - 0.5f) * 0.1f
                    )
                } else {
                    // If we have a letter, add slight noise to the sensor values for realism
                    currentFlexValues.value = currentFlexValues.value.map { value ->
                        val noise = (Random.nextFloat() - 0.5f) * 0.02f
                        (value + noise).coerceIn(0.1f, 0.9f)
                    }
                }
                
                delay(50) // Update at 20Hz for realistic sensor sampling
            }
        }
    }
    
    // Legacy method kept for compatibility but no longer generates fake letters
    fun updateWithNextLetter() {
        // This method is kept for compatibility with existing UI components
        // but when using real BLE data, we don't need to simulate letters
    }
    
    fun stopCapturing() {
        captureJob?.cancel()
        isCapturing.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        captureJob?.cancel()
        bleManager?.disconnect()
    }
} 