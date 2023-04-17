package com.example.obsmini.ble

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager


class MyBroadcastReceiver(
    private val enableBleDialog: () -> Unit,
    private val enableLocationDialog: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            enableBleDialog()
        }
        if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            enableLocationDialog()
        }
    }
}