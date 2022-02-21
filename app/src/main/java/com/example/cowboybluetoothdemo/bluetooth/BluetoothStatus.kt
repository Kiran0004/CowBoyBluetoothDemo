package com.example.cowboybluetoothdemo.bluetooth

/**
 * Simple enum to be used to track the connection status in BluetoothViewModel. Can also be used through databinding in the XML files
 */
enum class BluetoothStatus {
    Unconnected,
    Connecting,
    Connected,
    Disconnecting
}
