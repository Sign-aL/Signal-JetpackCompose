package com.example.signal3

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
     * Connect to the Signal device
     */
    fun connectDevice() {
        isDeviceConnected.value = true
    }
    
    /**
     * Disconnect from the Signal device
     */
    fun disconnectDevice() {
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
        
        // Start the capture process in the ViewModel
        startCaptureProcess()
    }
    
    private fun startCaptureProcess() {
        // Cancel any existing job first
        captureJob?.cancel()
        
        captureJob = viewModelScope.launch {
            while (isCapturing.value && captureIndex.value < lettersToCapture.size) {
                // Show capturing animation
                showPulse.value = true
                delay(300)
                showPulse.value = false
                delay(500)
                
                // Update with next letter
                updateWithNextLetter()
                
                // Simulating device delay
                delay(700)
            }
            
            // If we've reached the end, stop capturing
            if (captureIndex.value >= lettersToCapture.size) {
                isCapturing.value = false
            }
        }
    }
    
    fun updateWithNextLetter() {
        if (captureIndex.value < lettersToCapture.size) {
            // Capture the current letter
            val letter = lettersToCapture[captureIndex.value]
            currentLetter.value = letter.toString()
            detectedLetters.value += currentLetter.value
            
            // Update flex values for this letter with added noise for realism
            baseLetterFlexValues[letter]?.let { baseValues ->
                // Add random noise to make sensors look realistic
                currentFlexValues.value = baseValues.map { baseValue ->
                    // Add random noise between -0.05 and +0.05 to make readings look shaky
                    val noise = Random.nextFloat() * 0.1f - 0.05f
                    (baseValue + noise).coerceIn(0.1f, 0.9f)
                }
            }
            
            // Start a coroutine to continuously add slight variations to the sensor values
            viewModelScope.launch {
                while (isCapturing.value && captureIndex.value < lettersToCapture.size) {
                    val baseValues = baseLetterFlexValues[letter] ?: continue
                    
                    // Update with noise every 50ms for natural sensor drift
                    currentFlexValues.value = baseValues.mapIndexed { index, baseValue ->
                        // Add microfluctuations to simulate real sensor readings
                        val noise = Random.nextFloat() * 0.08f - 0.04f
                        val currentValue = currentFlexValues.value[index]
                        // Apply a blend of the current value and the new noisy value for smooth transitions
                        val blendedValue = currentValue * 0.7f + (baseValue + noise) * 0.3f
                        blendedValue.coerceIn(0.1f, 0.9f)
                    }
                    
                    delay(50) // Update at 20Hz for realistic sensor sampling
                }
            }
            
            // Move to next letter
            captureIndex.value++
            
            // If we've reached the end, stop capturing
            if (captureIndex.value >= lettersToCapture.size) {
                isCapturing.value = false
            }
        }
    }
    
    fun stopCapturing() {
        captureJob?.cancel()
        isCapturing.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        captureJob?.cancel()
    }
} 