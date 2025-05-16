package com.example.signal3

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BleConnectionManager(private val context: Context) {

    // BLE UUIDs that match the ESP32 code
    companion object {
        private const val TAG = "BleConnectionManager"
        
        // Service and characteristic UUIDs - match with ESP32 code
        private val SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        private val CHARACTERISTIC_TX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E") 
        
        // Descriptor UUID for enabling notifications
        private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        
        // Device name to look for - match with ESP32 code
        private const val DEVICE_NAME = "FakeGloveBLE"
        
        // Scan timeout
        private const val SCAN_PERIOD = 10000L // 10 seconds
    }
    
    // Interfaces for callback listeners
    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionFailed()
    }
    
    interface DataListener {
        fun onDataReceived(letter: Char, sensorData: Map<String, Any>?)
    }
    
    // Callback variables
    private var connectionListener: ConnectionListener? = null
    private var dataListener: DataListener? = null
    
    // Bluetooth variables
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    
    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private var isConnected = false
    
    private val handler = Handler(Looper.getMainLooper())
    
    // Set callback listeners
    fun setConnectionListener(listener: ConnectionListener) {
        connectionListener = listener
    }
    
    fun setDataListener(listener: DataListener) {
        dataListener = listener
    }
    
    // Start scanning for BLE devices
    fun startScan() {
        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }
        
        if (isScanning) return
        
        // Setup scan filters to look for our specific device
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build(),
            ScanFilter.Builder()
                .setDeviceName(DEVICE_NAME)
                .build()
        )
        
        // Scan settings
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        // Start scan with timeout
        bleScanner.startScan(filters, settings, scanCallback)
        isScanning = true
        
        handler.postDelayed({
            stopScan()
        }, SCAN_PERIOD)
        
        Log.d(TAG, "Started BLE scan")
    }
    
    // Stop scanning
    fun stopScan() {
        if (isScanning) {
            bleScanner.stopScan(scanCallback)
            isScanning = false
            Log.d(TAG, "Stopped BLE scan")
        }
    }
    
    // Connect to a specific BLE device
    fun connect(device: android.bluetooth.BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Log.d(TAG, "Connecting to device: ${device.name}")
    }
    
    // Disconnect from the current device
    fun disconnect() {
        bluetoothGatt?.apply {
            disconnect()
            close()
        }
        bluetoothGatt = null
        isConnected = false
    }
    
    // Scan callback implementation
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            Log.d(TAG, "Found device: ${device.name}, address: ${device.address}")
            
            if (device.name == DEVICE_NAME || result.scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true) {
                stopScan()
                connect(device)
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    }
    
    // GATT callback for handling connection events and data
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server")
                    isConnected = true
                    handler.post {
                        connectionListener?.onConnected()
                    }
                    // Discover services after connecting
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from GATT server")
                    isConnected = false
                    handler.post {
                        connectionListener?.onDisconnected()
                    }
                }
            } else {
                Log.w(TAG, "Connection state change with status: $status")
                isConnected = false
                handler.post {
                    connectionListener?.onConnectionFailed()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                // Find our specific service and characteristic
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val txCharacteristic = service.getCharacteristic(CHARACTERISTIC_TX_UUID)
                    if (txCharacteristic != null) {
                        // Enable notifications for the TX characteristic
                        enableNotifications(gatt, txCharacteristic)
                    } else {
                        Log.e(TAG, "TX characteristic not found")
                    }
                } else {
                    Log.e(TAG, "Service not found")
                }
            } else {
                Log.w(TAG, "Service discovery failed with status: $status")
            }
        }

        @Suppress("DEPRECATION")
        private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            // Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true)
            
            // Enable remote notifications
            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // API 33+ method
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    // Older APIs
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
                Log.d(TAG, "Notifications enabled for TX characteristic")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleCharacteristicChanged(characteristic, value)
        }

        // For older Android versions
        @Deprecated("Deprecated in API level 33")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleCharacteristicChanged(characteristic, characteristic.value)
        }
        
        private fun handleCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            if (characteristic.uuid == CHARACTERISTIC_TX_UUID) {
                val letterStr = String(value)
                val letter = if (letterStr.isNotEmpty()) letterStr[0] else ' '
                
                Log.d(TAG, "Received notification: $letterStr")
                
                handler.post {
                    dataListener?.onDataReceived(letter, null)
                }
            }
        }
    }
    
    fun isConnected(): Boolean {
        return isConnected
    }
} 