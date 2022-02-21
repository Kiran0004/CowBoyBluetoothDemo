package com.example.cowboybluetoothdemo.bluetooth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Boilerplate code to allow the view model creator to pass the application into the viewModel
 */
class BluetoothViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            return BluetoothViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}