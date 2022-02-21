package com.example.cowboybluetoothdemo.scan_devices

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cowboybluetoothdemo.R
import com.example.cowboybluetoothdemo.bluetooth.BluetoothViewModel
import com.example.cowboybluetoothdemo.bluetooth.BluetoothViewModelFactory
import com.example.cowboybluetoothdemo.databinding.FragmentScanDevicesBinding

class ScanDevicesFragment : Fragment() {
    private lateinit var viewModel: BluetoothViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Super quick implementation of the permissions required for bluetooth operations
        // If the permission was already granted, it will still trigger the onRequestPermissionResult and start the scan
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        requestPermissions(permissions,1)
        val binding = FragmentScanDevicesBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val factory = BluetoothViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory).get(BluetoothViewModel::class.java)

        binding.model = viewModel

        val bluetoothDeviceAdapter =
            BluetoothDeviceAdapter(BluetoothDeviceAdapter.DeviceClickListener {
                // This adapter passes a click listener to each list item, which allows us to process the click here: Connect, and navigate
                viewModel.connect(it)
                findNavController().navigate(R.id.connect)
            })

        binding.devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bluetoothDeviceAdapter
        }

        viewModel.foundDevices.observe(viewLifecycleOwner, {
            it?.let {
                bluetoothDeviceAdapter.setDeviceList(it)
            }
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                viewModel.startScanningContinuously()
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}