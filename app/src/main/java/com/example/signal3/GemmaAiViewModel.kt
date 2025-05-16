package com.example.signal3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// UI state for Gemma responses
sealed class GemmaUiState {
    object Initial : GemmaUiState()
    object Loading : GemmaUiState()
    data class Success(val response: String) : GemmaUiState()
    data class Error(val message: String) : GemmaUiState()
}

/**
 * ViewModel for interacting with Gemma AI for local, health-related processing
 * This is a simulated version that would ultimately use a local Gemma model
 */
class GemmaAiViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<GemmaUiState>(GemmaUiState.Initial)
    val uiState: StateFlow<GemmaUiState> = _uiState
    
    /**
     * Generate an emergency summary based on health profile and detected condition
     */
    fun generateEmergencySummary(
        profile: MedicalProfile,
        detectedCondition: String
    ) {
        _uiState.value = GemmaUiState.Loading
        
        viewModelScope.launch {
            try {
                // In a real implementation, this would use the Gemma API or a local model
                // For now, we'll simulate the response
                val response = simulateGemmaResponse(profile, detectedCondition)
                
                // Add a small delay to simulate processing
                TimeUnit.MILLISECONDS.sleep(1000)
                
                _uiState.value = GemmaUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = GemmaUiState.Error("Failed to generate emergency summary: ${e.message}")
            }
        }
    }
    
    /**
     * Generate a health info summary based on health profile
     */
    fun summarizeHealthProfile(profile: MedicalProfile) {
        _uiState.value = GemmaUiState.Loading
        
        viewModelScope.launch {
            try {
                // In a real implementation, this would use the Gemma API or a local model
                // For now, we'll simulate the response
                val response = simulateHealthSummary(profile)
                
                // Add a small delay to simulate processing
                TimeUnit.MILLISECONDS.sleep(800)
                
                _uiState.value = GemmaUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = GemmaUiState.Error("Failed to summarize health profile: ${e.message}")
            }
        }
    }
    
    /**
     * Simulate Gemma response for emergency situations
     * In a real implementation, this would use the Gemma API
     */
    private fun simulateGemmaResponse(profile: MedicalProfile, detectedCondition: String): String {
        // Create emergency response based on profile and condition
        val emergencyInfo = StringBuilder()
        
        // Add patient name
        if (profile.fullName.isNotEmpty()) {
            emergencyInfo.append("PATIENT: ${profile.fullName}\n")
        }
        
        // Add relevant condition info
        when {
            detectedCondition.contains("pain", ignoreCase = true) -> {
                emergencyInfo.append("EMERGENCY ALERT: Patient reporting pain\n")
                // Add allergies if available
                if (profile.allergies.isNotEmpty()) {
                    emergencyInfo.append("⚠️ ALLERGIES: ${profile.allergies}\n")
                }
                
                // Add medications if available
                if (profile.medications.isNotEmpty()) {
                    emergencyInfo.append("MEDICATIONS: ${profile.medications}\n")
                }
            }
            
            detectedCondition.contains("breath", ignoreCase = true) || 
            detectedCondition.contains("breathing", ignoreCase = true) -> {
                emergencyInfo.append("EMERGENCY ALERT: Patient reporting breathing difficulty\n")
                // Check if asthma is mentioned in conditions
                if (profile.medicalConditions.contains("asthma", ignoreCase = true)) {
                    emergencyInfo.append("⚠️ PATIENT HAS ASTHMA - Check for inhaler\n")
                }
                
                // Add any relevant medications
                if (profile.medications.isNotEmpty()) {
                    emergencyInfo.append("MEDICATIONS: ${profile.medications}\n")
                }
            }
            
            detectedCondition.contains("dizzy", ignoreCase = true) || 
            detectedCondition.contains("faint", ignoreCase = true) -> {
                emergencyInfo.append("EMERGENCY ALERT: Patient reporting dizziness/faintness\n")
                // Add blood type if available
                if (profile.bloodType.isNotEmpty()) {
                    emergencyInfo.append("BLOOD TYPE: ${profile.bloodType}\n")
                }
                
                // Add diabetes check
                if (profile.medicalConditions.contains("diabetes", ignoreCase = true)) {
                    emergencyInfo.append("⚠️ PATIENT HAS DIABETES - Check blood sugar\n")
                }
            }
            
            else -> {
                emergencyInfo.append("EMERGENCY ALERT: General medical assistance needed\n")
                // Include important medical conditions
                if (profile.medicalConditions.isNotEmpty()) {
                    emergencyInfo.append("CONDITIONS: ${profile.medicalConditions}\n")
                }
                
                // Add allergies if available
                if (profile.allergies.isNotEmpty()) {
                    emergencyInfo.append("⚠️ ALLERGIES: ${profile.allergies}\n")
                }
            }
        }
        
        // Always add emergency contact if available
        if (profile.emergencyContact.isNotEmpty() && profile.emergencyPhone.isNotEmpty()) {
            emergencyInfo.append("\nEMERGENCY CONTACT: ${profile.emergencyContact} - ${profile.emergencyPhone}")
        }
        
        return emergencyInfo.toString()
    }
    
    /**
     * Simulate health summary based on profile
     * In a real implementation, this would use the Gemma API
     */
    private fun simulateHealthSummary(profile: MedicalProfile): String {
        val summary = StringBuilder("Health Profile Summary:\n\n")
        
        // Add name if available
        if (profile.fullName.isNotEmpty()) {
            summary.append("Name: ${profile.fullName}\n")
        }
        
        // Extract age from date of birth
        if (profile.dateOfBirth.isNotEmpty()) {
            summary.append("DOB: ${profile.dateOfBirth}\n")
        }
        
        // Highlight critical information
        var hasCriticalInfo = false
        
        // Check for allergies
        if (profile.allergies.isNotEmpty()) {
            summary.append("\n⚠️ ALLERGIES: ${profile.allergies}\n")
            hasCriticalInfo = true
        }
        
        // Check for medical conditions
        if (profile.medicalConditions.isNotEmpty()) {
            summary.append("\nMedical Conditions: ${profile.medicalConditions}\n")
            
            // Check for critical conditions
            val criticalConditions = listOf("diabetes", "asthma", "seizure", "heart", "cardiac")
            for (condition in criticalConditions) {
                if (profile.medicalConditions.contains(condition, ignoreCase = true)) {
                    summary.append("⚠️ CRITICAL: Patient has $condition condition\n")
                    hasCriticalInfo = true
                }
            }
        }
        
        // Add medications
        if (profile.medications.isNotEmpty()) {
            summary.append("\nCurrent Medications: ${profile.medications}\n")
        }
        
        // Add blood type if available
        if (profile.bloodType.isNotEmpty()) {
            summary.append("\nBlood Type: ${profile.bloodType}\n")
        }
        
        // Add emergency contact
        if (profile.emergencyContact.isNotEmpty() && profile.emergencyPhone.isNotEmpty()) {
            summary.append("\nEmergency Contact: ${profile.emergencyContact} (${profile.emergencyPhone})\n")
        }
        
        // Add a general notice if we have critical information
        if (hasCriticalInfo) {
            summary.insert(0, "⚠️ ATTENTION: This profile contains critical medical information.\n\n")
        }
        
        return summary.toString()
    }
} 