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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject

// Constants for SharedPreferences keys
private const val PREF_MEDICAL_PROFILE = "medical_profile"

// Data model for medical profile
data class MedicalProfile(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val medications: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val medicalConditions: String = "",
    val additionalNotes: String = "",
    val insuranceInfo: String = ""
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("fullName", fullName)
        json.put("dateOfBirth", dateOfBirth)
        json.put("bloodType", bloodType)
        json.put("allergies", allergies)
        json.put("medications", medications)
        json.put("emergencyContact", emergencyContact)
        json.put("emergencyPhone", emergencyPhone)
        json.put("medicalConditions", medicalConditions)
        json.put("additionalNotes", additionalNotes)
        json.put("insuranceInfo", insuranceInfo)
        return json.toString()
    }
    
    companion object {
        fun fromJson(jsonString: String): MedicalProfile {
            if (jsonString.isEmpty()) return MedicalProfile()
            
            val json = JSONObject(jsonString)
            return MedicalProfile(
                fullName = json.optString("fullName", ""),
                dateOfBirth = json.optString("dateOfBirth", ""),
                bloodType = json.optString("bloodType", ""),
                allergies = json.optString("allergies", ""),
                medications = json.optString("medications", ""),
                emergencyContact = json.optString("emergencyContact", ""),
                emergencyPhone = json.optString("emergencyPhone", ""),
                medicalConditions = json.optString("medicalConditions", ""),
                additionalNotes = json.optString("additionalNotes", ""),
                insuranceInfo = json.optString("insuranceInfo", "")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("signal_prefs", Context.MODE_PRIVATE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Load saved profile data
    val savedProfileJson = sharedPrefs.getString(PREF_MEDICAL_PROFILE, "") ?: ""
    var profile by remember { mutableStateOf(MedicalProfile.fromJson(savedProfileJson)) }
    
    // UI state variables
    var fullName by remember { mutableStateOf(profile.fullName) }
    var dateOfBirth by remember { mutableStateOf(profile.dateOfBirth) }
    var bloodType by remember { mutableStateOf(profile.bloodType) }
    var allergies by remember { mutableStateOf(profile.allergies) }
    var medications by remember { mutableStateOf(profile.medications) }
    var emergencyContact by remember { mutableStateOf(profile.emergencyContact) }
    var emergencyPhone by remember { mutableStateOf(profile.emergencyPhone) }
    var medicalConditions by remember { mutableStateOf(profile.medicalConditions) }
    var additionalNotes by remember { mutableStateOf(profile.additionalNotes) }
    var insuranceInfo by remember { mutableStateOf(profile.insuranceInfo) }
    
    // Update UI with saved profile when component loads
    LaunchedEffect(savedProfileJson) {
        if (savedProfileJson.isNotEmpty()) {
            val loadedProfile = MedicalProfile.fromJson(savedProfileJson)
            fullName = loadedProfile.fullName
            dateOfBirth = loadedProfile.dateOfBirth
            bloodType = loadedProfile.bloodType
            allergies = loadedProfile.allergies
            medications = loadedProfile.medications
            emergencyContact = loadedProfile.emergencyContact
            emergencyPhone = loadedProfile.emergencyPhone
            medicalConditions = loadedProfile.medicalConditions
            additionalNotes = loadedProfile.additionalNotes
            insuranceInfo = loadedProfile.insuranceInfo
        }
    }
    
    // Function to save all profile data
    fun saveProfile() {
        val updatedProfile = MedicalProfile(
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            bloodType = bloodType,
            allergies = allergies,
            medications = medications,
            emergencyContact = emergencyContact,
            emergencyPhone = emergencyPhone,
            medicalConditions = medicalConditions,
            additionalNotes = additionalNotes,
            insuranceInfo = insuranceInfo
        )
        
        // Save to SharedPreferences
        sharedPrefs.edit().putString(PREF_MEDICAL_PROFILE, updatedProfile.toJson()).apply()
        
        // Show confirmation to user
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Profile saved")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
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
            // Introduction card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Health Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your health information will be available for emergency situations when communication is difficult.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This information stays on your device and is only shown when you need it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Personal Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("Date of Birth (MM/DD/YYYY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        label = { Text("Blood Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Medical Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Health Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Allergies") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = medications,
                        onValueChange = { medications = it },
                        label = { Text("Current Medications") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = medicalConditions,
                        onValueChange = { medicalConditions = it },
                        label = { Text("Health Conditions") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
            
            // Emergency Contact
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emergency Contact",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Emergency Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Additional Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Additional Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = insuranceInfo,
                        onValueChange = { insuranceInfo = it },
                        label = { Text("Insurance Information") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        label = { Text("Additional Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
            
            // Save button
            Button(
                onClick = { saveProfile() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("SAVE PROFILE")
            }
            
            // Spacer at the bottom for better scrolling
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
} 