package com.example.cowboybluetoothdemo

import android.bluetooth.BluetoothDevice

/**
 * Simple class to package the bluetooth device along with the RSSI value
 */
class SortableDevice(val device: BluetoothDevice, var signalStrength: Int, val connectable: Boolean = true) {

    /**
     * Method used to ensure a proper clone can be created for updating the signalStrength on refreshing the data
     */
    fun copy(): SortableDevice {
        return SortableDevice(device, signalStrength, connectable)
    }
}
