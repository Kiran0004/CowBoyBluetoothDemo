package com.example.cowboybluetoothdemo.scan_devices

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cowboybluetoothdemo.SortableDevice
import com.example.cowboybluetoothdemo.databinding.DeviceItemBinding

class BluetoothDeviceAdapter(
    private val itemClickListener: DeviceClickListener,
    private var devices: MutableList<SortableDevice> = mutableListOf()
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], itemClickListener)
    }

    override fun getItemCount() = devices.size

    fun setDeviceList(devices: List<SortableDevice>) {
        this.devices = devices.toMutableList()
        notifyDataSetChanged()
    }

    class DeviceViewHolder(private val binding: DeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): DeviceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DeviceItemBinding.inflate(layoutInflater, parent, false)

                return DeviceViewHolder(binding)
            }
        }

        fun bind(device: SortableDevice, clickListener: DeviceClickListener) {
            binding.device = device
            binding.clickListener = clickListener
        }

    }

    class DeviceClickListener(val clickListener: (device: SortableDevice) -> Unit) {
        fun onClick(device: SortableDevice) = clickListener(device)
    }
}
