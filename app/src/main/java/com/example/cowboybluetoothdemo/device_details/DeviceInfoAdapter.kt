package com.example.cowboybluetoothdemo.device_details

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cowboybluetoothdemo.databinding.CharacteristicItemBinding
import com.example.cowboybluetoothdemo.databinding.ServiceItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceInfoAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_VIEW_TYPE_SERVICE = 0
    private val ITEM_VIEW_TYPE_CHARACTERISTIC = 1

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var itemList = ArrayList<DataItem>()

    fun addMap(map: HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>) {
        adapterScope.launch {
            val resList = ArrayList<DataItem>()
            if (map.values.isNotEmpty()) {
                map.keys.forEach { service ->
                    // Each service is added as a section header of sorts
                    resList.add(DataItem.Service(service))

                    map[service]?.let { list ->
                        resList.addAll(
                            list.map { characteristic ->
                                // Each characteristic is added as a list item underneath the corresponding service header
                                DataItem.Characteristic(characteristic)
                            }
                        )
                    }
                }
            }
            itemList = resList
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_SERVICE -> ServiceViewHolder.from(parent)
            ITEM_VIEW_TYPE_CHARACTERISTIC -> CharacteristicViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ServiceViewHolder -> {
                val item = itemList[position] as DataItem.Service
                holder.bind(item.service)
            }
            is CharacteristicViewHolder -> {
                val item = itemList[position] as DataItem.Characteristic
                holder.bind(item.characteristic)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is DataItem.Service -> ITEM_VIEW_TYPE_SERVICE
            is DataItem.Characteristic -> ITEM_VIEW_TYPE_CHARACTERISTIC
        }
    }

    class ServiceViewHolder private constructor(val binding: ServiceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BluetoothGattService) {
            binding.service = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ServiceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ServiceItemBinding.inflate(layoutInflater, parent, false)

                return ServiceViewHolder(binding)
            }
        }
    }

    class CharacteristicViewHolder private constructor(val binding: CharacteristicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BluetoothGattCharacteristic) {
            binding.characteristic = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): CharacteristicViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CharacteristicItemBinding.inflate(layoutInflater, parent, false)

                return CharacteristicViewHolder(binding)
            }
        }
    }

    /**
     * The DataItem superclass is used to encapsulate the data of each item (service or characteristic)
     * and use their individual implementations accordingly in onCreateViewHolder and onBindViewHolder
     */
    sealed class DataItem {
        data class Service(val service: BluetoothGattService) : DataItem() {
            override val id = service.instanceId
        }

        data class Characteristic(val characteristic: BluetoothGattCharacteristic) : DataItem() {
            override val id: Int = characteristic.instanceId
        }

        abstract val id: Int
    }

    override fun getItemCount() = itemList.size
}