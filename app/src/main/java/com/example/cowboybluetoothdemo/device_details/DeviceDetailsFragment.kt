package com.example.cowboybluetoothdemo.device_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cowboybluetoothdemo.bluetooth.BluetoothViewModel
import com.example.cowboybluetoothdemo.bluetooth.BluetoothViewModelFactory
import com.example.cowboybluetoothdemo.databinding.FragmentDeviceDetailsBinding

class DeviceDetailsFragment : Fragment() {

    private lateinit var viewModel: BluetoothViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDeviceDetailsBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val factory = BluetoothViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory).get(BluetoothViewModel::class.java)

        binding.model = viewModel

        val deviceInfoAdapter = DeviceInfoAdapter()
        binding.deviceDetailsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceInfoAdapter
        }
        viewModel.servicesAndCharacteristics.observe(viewLifecycleOwner, {
            it?.let {
                // Once the details have been retrieved, add them to the adapter
                deviceInfoAdapter.addMap(it)
                deviceInfoAdapter.notifyDataSetChanged()
            }
        })
        return binding.root
    }

    /**
     * Called by the MainActivity in onBackPressed to disconnect the robot upon navigation
     */
    fun disconnect() {
        viewModel.disconnect()
    }
}