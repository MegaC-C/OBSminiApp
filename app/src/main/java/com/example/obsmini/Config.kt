package com.example.obsmini

import android.Manifest
import android.os.Build

const val DEVICE_NAME = "Nordic_LBS"
const val OBS_SERVICE_UIID = "00001523-1212-efde-1523-785feabcd123"
const val OBS_CHARACTERISTICS_UUID = "00001524-1212-efde-1523-785feabcd123"
const val CCCD_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
const val FIRMWARE_VERSION = "v0.17"
const val MAX_DISTANCE_cm = 300
const val NUMBER_OF_MEASUREMENTS_PER_SIDE = 50
const val PAYLOAD_IN_BYTE = (NUMBER_OF_MEASUREMENTS_PER_SIDE * 4) + 2 // 2byte for each left and 2byte for each right + 2byte for battery
const val BLE_SCAN_PERIOD_ms = 60_000L


object VersionPermissions {
    val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
}