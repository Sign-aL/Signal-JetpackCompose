# Signal

## Overview
The Signal App is a sign language interpretation system that uses a glove with sensors to detect American Sign Language (ASL) finger spelling. This demo app showcases what the final product would look like once the hardware is ready.

## Features
- **ASL Finger Spelling Detection**: Simulates the detection of one-handed ASL finger spelling using fake sensor data
- **Sensor Visualization**: Real-time visualization of simulated flex sensors and gyroscope data
- **Gemini AI Integration**: Demonstrates integration with Google's Vertex AI Gemini for interpreting sign language
- **Modern UI**: Clean, intuitive interface built with Material Design 3 and Jetpack Compose

## Technology Stack
- **Android Development**: Kotlin + Jetpack Compose
- **AI Integration**: Google Vertex AI Gemini API + Gemma
- **UI Framework**: Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)

## Hardware Concept
The actual hardware would consist of:
- 5 flex sensors (one for each finger)
- GY-91 MPU9050 module for orientation detection
- Microcontroller for data processing
- Bluetooth module for communication with the smartphone

## Demo Instructions
1. Launch the app
2. On the home screen, tap "Simulate Sign Detection" to see a demonstration of ASL finger spelling detection
3. Use the bottom right buttons to navigate to the Sensor Visualization screen or Settings
4. In the Sensor Visualization screen, you can see simulated readings from the flex sensors and gyroscope
5. In the Settings screen, you can configure app settings and (in a real implementation) provide your Gemini API key

## Google Solution Challenge
This project is being developed as part of the Google Solution Challenge, focusing on accessibility technology to help bridge communication gaps for people who use sign language.

## Future Development
- Integration with actual hardware when available
- Expanded sign language vocabulary beyond finger spelling
- Real-time translation to and from sign language
- Community features for sharing and improving sign language detection

## Credits
Developed by [Team Signal] 
