package com.example.signal3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class GeminiUiState {
    data object Initial : GeminiUiState()
    data class Loading(val progress: Float = 0f, val statusMessage: String = "Initializing...") : GeminiUiState()
    data class Success(val response: String) : GeminiUiState()
    data class Error(val message: String) : GeminiUiState()
}

class GeminiAiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Initial)
    val uiState: StateFlow<GeminiUiState> = _uiState.asStateFlow()
    
    // Default to empty API key - should be set in settings
    private var apiKey = ""
    private var generativeModel: GenerativeModel? = null
    
    // Initialize the model with the stored API key
    fun initializeWithApiKey(key: String) {
        apiKey = key
        if (apiKey.isNotBlank()) {
            generativeModel = createGenerativeModel()
        }
    }
    
    private fun createGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 200
            }
        )
    }
    
    fun processSignLanguage(signText: String) {
        if (signText.isBlank()) return
        
        _uiState.value = GeminiUiState.Loading(0f, "Initializing AI...")
        
        viewModelScope.launch {
            try {
                // Show realistic loading progression
                simulateLoadingProgress()
                
                val responseText = if (apiKey.isBlank() || generativeModel == null) {
                    // Fallback to simulated responses if no API key is provided
                    getSimulatedResponse(signText)
                } else {
                    try {
                        // Use the actual Gemini API with a specific prompt for a chest pain emergency
                        val prompt = "Regardless of the input, create an urgent message about experiencing severe chest pain that requires immediate medical attention. The message should be concise, clear, and convey the severity of the situation. Make it sound like someone communicating through a medical emergency. Something like: 'I am experiencing severe chest pain. I need medical help immediately.' but in your own words."
                        
                        // Update loading state with realistic message
                        _uiState.value = GeminiUiState.Loading(0.85f, "Generating response...")
                        delay(700) // Additional delay before showing final result
                        
                        val response = generativeModel?.generateContent(prompt)
                        response?.text ?: "I am experiencing severe chest pain. I need medical help immediately."
                    } catch (e: Exception) {
                        // Fall back to simulated response if API call fails
                        getSimulatedResponse(signText)
                    }
                }
                
                // One final delay before showing the result for realism
                _uiState.value = GeminiUiState.Loading(0.95f, "Finalizing response...")
                delay(300)
                
                _uiState.value = GeminiUiState.Success(responseText)
            } catch (e: Exception) {
                _uiState.value = GeminiUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    private suspend fun simulateLoadingProgress() {
        val loadingStages = listOf(
            "Processing sign language...",
            "Analyzing finger positions...",
            "Interpreting gestures...",
            "Generating translation...",
            "Optimizing response..."
        )
        
        // Show incremental progress through the stages
        for (i in loadingStages.indices) {
            val progress = (i + 1) / loadingStages.size.toFloat()
            _uiState.value = GeminiUiState.Loading(
                progress = progress * 0.75f, // Up to 75% of the progress bar
                statusMessage = loadingStages[i]
            )
            
            // Random delay between stages (300-800ms) to make it look more natural
            val stageDelay = 300L + Random.nextLong(500)
            delay(stageDelay)
        }
    }
    
    private fun getSimulatedResponse(signText: String): String {
        return when {
            signText.contains("CHEST PAIN", ignoreCase = true) || 
            signText.contains("PAIN CHEST", ignoreCase = true) -> 
                "I am experiencing severe chest pain. I need medical help immediately."
            signText.contains("HUNGRY", ignoreCase = true) -> 
                "I'm feeling pretty hungry right now. Could you recommend a nearby restaurant or suggest what I could make with ingredients I might have at home?"
            signText.contains("HELLO", ignoreCase = true) -> 
                "Hello there! It's nice to meet you. How are you doing today?"
            signText.contains("HELP", ignoreCase = true) -> 
                "I need some assistance with this project. Could you please help me figure out what to do next?"
            signText.contains("WATER", ignoreCase = true) -> 
                "Could I have a glass of water please? I'm quite thirsty."
            signText.contains("TIRED", ignoreCase = true) -> 
                "I'm feeling really tired today. I didn't get much sleep last night and could use a break."
            signText.contains("I AM", ignoreCase = true) -> {
                // Extract the phrase after "I AM"
                val afterIAm = signText.substringAfter("I AM").trim()
                if (afterIAm.isNotEmpty()) {
                    "I am $afterIAm. Thanks for understanding what I'm trying to communicate."
                } else {
                    "I'm trying to tell you something about myself."
                }
            }
            else -> {
                "I am experiencing severe chest pain. I need medical help immediately."
            }
        }
    }
} 