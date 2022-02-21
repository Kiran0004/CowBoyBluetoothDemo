package com.example.cowboybluetoothdemo.bluetooth

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.cowboybluetoothdemo.SortableDevice

/**
 * This ViewModel requires the application to access certain Bluetooth features. Thus, this is passed in through the VM Factory
 */
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    private val _status = MutableLiveData(BluetoothStatus.Unconnected)
    val status
        get() = _status

    private val _foundDevices = MutableLiveData<ArrayList<SortableDevice>>(ArrayList())
    val foundDevices
        get() = _foundDevices

    val deviceList = ArrayList<SortableDevice>()
    var firstScan = true

    private val _connectedDevice = MutableLiveData<SortableDevice?>(null)
    val connectedDevice
        get() = _connectedDevice

    private var candidateDevice: SortableDevice? = null

    private val _servicesAndCharacteristics =
        MutableLiveData<HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>>()
    val servicesAndCharacteristics
        get() = _servicesAndCharacteristics

    private val bluetoothManager: BluetoothManager =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null

    private val mainHandler = Handler(
        Looper.getMainLooper()
    )

    fun startScanningContinuously() {
        startScan()

        mainHandler.removeCallbacks(restartScan)
        mainHandler.post(restartScan)
    }

    /**
     * Can start or restart an ongoing scan
     */
    fun startScan() {
        bleScanner?.stopScan(scanCallBack)

        val builder = ScanSettings.Builder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder
                // All these settings can be adjusted based on the use case. Because none were specified in the requirements, I've used the most aggressive (and standard) settings
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        }
        val scanSettings = builder
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0L)
            .build()
        bleScanner.startScan(null, scanSettings, scanCallBack)
    }

    private val restartScan = object : Runnable {
        override fun run() {
            updateDeviceList()

            startScan()
            // The first time we perform the scan we want results fairly quickly. Afterwards we can increase the delay to increase the chance to find a desired device
            val delay = if (firstScan) {
                firstScan = false
                2000L
            } else {
                10000L
            }
            mainHandler.postDelayed(this, delay)
        }
    }

    private fun stopScanning() {
        firstScan = true
        bleScanner?.stopScan(scanCallBack)
        mainHandler.removeCallbacks(restartScan)
    }

    /**
     * Method called after every scan period.
     * Can be used to sort the devices, and to clear out all devices that can no longer be found
     */
    fun updateDeviceList() {
        deviceList.sortByDescending { it.signalStrength }

        // The deviceList must be copied, otherwise changing the RSSI values updates them in realtime, which is not (necessarily) what we want
        foundDevices.value = deviceList.map { it.copy() } as ArrayList<SortableDevice>
    }

    fun connect(it: SortableDevice) {
        candidateDevice = it

        stopScanning()

        _status.value = BluetoothStatus.Connecting
        bluetoothGatt = it.device.connectGatt(getApplication(), true, connectionCallBack)
    }

    fun disconnect() {
        _status.value = BluetoothStatus.Disconnecting
        bluetoothGatt?.disconnect()
    }

    private val scanCallBack: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            processResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (scanResult in results) {
                processResult(scanResult)
            }
        }

        /**
         * Process the newly found devices based on the following principles
         * 1. A new device should be added if the list does not contain it yet
         * 2. The signal strength of a device should be updated if it was already in the list
         */
        private fun processResult(result: ScanResult) {
            // Go through the list of devices. If a device matches the scan result, update the signal strength and return
            deviceList.forEach {
                if (it.device.address == result.device.address) {
                    it.signalStrength = result.rssi
                    return
                }
            }
            // If no device has been found, add it instead.
            addDevice(deviceList, result)

            // Currently, new devices are only shown once a scan cycle is completed. Instead, the line below can be uncommented to display a newly found device immediately
            // updateDeviceList()
        }

        /**
         * This is an alternative solution, where the devices are updated in real time.
         * It sorts the devices after every update, and thus #updateDeviceList should no longer be called.
         *
         * This is not the most optimal solution.
         * An alternative could be considered where you insert new items into the list based on the signal strength.
         * E.g. If item #4 has a signal strength of -50 and the new item has a strength of -45, the new item should be inserted as the #4 item instead, shifting the other items accordingly
         *
         * However, this approach seems risky as there are often multiple devices with the same signal strength, which would mean that a device could be added as a duplicate.
         * Additionally, if a device's signal strength increase, e.g. from -50 to -40, it would insert a duplicate at the right position, but the old item would still remain.
         * This would require finishing the loop to remove that item, which would reduce the performance regardless.
         */
        private fun processResultRealTime(result: ScanResult) {
            val deviceList = foundDevices.value
            deviceList?.let { list ->
                // Go through the list of devices. If a device matches the scan result, update the signal strength and return
                list.forEach {
                    if (it.device.address == result.device.address) {
                        it.signalStrength = result.rssi
                        list.sortByDescending { device -> device.signalStrength }
                        return
                    }
                }
                // If no device has been found, add it instead. Then sort the list.
                addDevice(deviceList, result)
                list.sortByDescending { it.signalStrength }
            }
            foundDevices.value = deviceList
        }

        /**
         * Helper function because the default min sdk does not support "connectable"
         */
        private fun addDevice(deviceList: ArrayList<SortableDevice>, result: ScanResult) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceList.add(SortableDevice(result.device, result.rssi, result.isConnectable))
            } else {
                deviceList.add(SortableDevice(result.device, result.rssi))
            }
        }
    }

    private val connectionCallBack: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt?.discoverServices()
                _status.postValue(BluetoothStatus.Connected)

                candidateDevice?.let {
                    _connectedDevice.postValue(it)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _status.postValue(BluetoothStatus.Unconnected)
                _connectedDevice.postValue(null)
            }
        }

        /**
         * Once services are discovered we add them to a hashmap that represents the service -> characteristic relation
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val res = HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>()

                bluetoothGatt?.services?.forEach { service ->
                    val characteristicList = ArrayList<BluetoothGattCharacteristic>()
                    service.characteristics.forEach { characteristic ->
                        characteristicList.add(characteristic)
                    }
                    res[service] = characteristicList
                }
                _servicesAndCharacteristics.postValue(res)
            }
        }
    }
}