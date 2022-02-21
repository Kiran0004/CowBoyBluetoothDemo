package com.example.cowboybluetoothdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cowboybluetoothdemo.databinding.MainActivityBinding
import com.example.cowboybluetoothdemo.device_details.DeviceDetailsFragment
import com.example.cowboybluetoothdemo.scan_devices.ScanDevicesFragment

class MainActivity : AppCompatActivity() {

    /**
     * Because there are no shared UI elements, this activity does not need to alter the UI
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        val navHost = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                if (fragment is DeviceDetailsFragment) {
                    // When returning to the scan results, the connected device should be disconnected
                    fragment.disconnect()
                }
                super.onBackPressed()
            }
        }
    }
}