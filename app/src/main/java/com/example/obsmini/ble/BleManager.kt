package com.example.obsmini.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.obsmini.BLE_SCAN_PERIOD_ms
import com.example.obsmini.CCCD_DESCRIPTOR_UUID
import com.example.obsmini.DEVICE_NAME
import com.example.obsmini.OBS_CHARACTERISTICS_UUID
import com.example.obsmini.OBS_SERVICE_UIID

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


interface BleManager {
    val data: MutableSharedFlow<Resource<BleData>>
    fun startScanning()
    fun closeConnection()
}

@SuppressLint("MissingPermission")
class BleManagerImpl @Inject constructor(
    bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : BleManager {

    override val data: MutableSharedFlow<Resource<BleData>> = MutableSharedFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var bleGatt: BluetoothGatt? = null

    // -------------------- BLE Scanner --------------------
    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanFilter = ScanFilter.Builder()
        .setDeviceName(DEVICE_NAME)
        .build()

    private var scanning = false



    override fun startScanning() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            Handler(Looper.getMainLooper()).postDelayed({
                if (scanning) {
                    scanning = false
                    bleScanner.stopScan(scanCallback)
                    coroutineScope.launch { data.emit(Resource.Error("Failed to find target BLE device")) }
                }
            }, BLE_SCAN_PERIOD_ms)
            scanning = true
            bleScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
            coroutineScope.launch { data.emit(Resource.Loading(message = "Scanning for BLE device...")) }
        } else {
            scanning = false
            bleScanner.stopScan(scanCallback)
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            coroutineScope.launch { data.emit(Resource.Loading(message = "Connecting to BLE device...")) }
            if (scanning) {
                result.device.connectGatt(context, false, gattCallback)
                scanning = false
                bleScanner.stopScan(this)
            }
        }
    }

    // -------------------- BLE termination --------------------
    override fun closeConnection() {
        val characteristic = findCharacteristics(OBS_SERVICE_UIID, OBS_CHARACTERISTICS_UUID)
        if(characteristic != null){
            disconnectCharacteristic(characteristic)
        }
        bleGatt?.disconnect()
        // connection will be closed in callback handler after successful disconnect
    }

    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(bleGatt?.setCharacteristicNotification(characteristic,false) == false) {
                return
            }
            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

    private fun closeGatt() {
        bleGatt?.let { gatt ->
            gatt.close()
            bleGatt = null
        }
    }

    // -------------------- enable BLE GATT connection & notifications --------------------
    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // status == BluetoothGatt.GATT_SUCCESS is only true when connection change was called from android device
            // it is false when the BLE connection was closed by the other end, can help in reestablishing a lost connection
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if(newState == BluetoothProfile.STATE_CONNECTED) {
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services..."))
                    }
                    gatt.discoverServices()
                    this@BleManagerImpl.bleGatt = gatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    coroutineScope.launch {
                        data.emit(Resource.Success(BleData(byteArrayOf(), ConnectionState.Disconnected)))
                    }
                    closeGatt()
                }
            } else {
                closeGatt()
                startScanning()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            coroutineScope.launch { data.emit(Resource.Loading(message = "OBSmini discovered...")) }
            gatt.requestMtu(517) // request maximum MTU
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            coroutineScope.launch { data.emit(Resource.Loading(message = "MTU adjusted to $mtu")) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.setPreferredPhy(
                    BluetoothDevice.PHY_LE_1M_MASK,
                    BluetoothDevice.PHY_LE_1M_MASK,
                    BluetoothDevice.PHY_OPTION_NO_PREFERRED
                )
            } else enableNotification()
        }

        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                coroutineScope.launch { data.emit(Resource.Loading(message = "PHY adjusted to $rxPhy")) }
                enableNotification()
            }
        }


        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                super.onCharacteristicChanged(gatt, characteristic, value)
                if (characteristic.uuid == UUID.fromString(OBS_CHARACTERISTICS_UUID)) {
                    val bleData = BleData(value, ConnectionState.Connected)
                    coroutineScope.launch { data.emit(Resource.Success(bleData)) }
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                super.onCharacteristicChanged(gatt, characteristic)
                if (characteristic.uuid == UUID.fromString(OBS_CHARACTERISTICS_UUID)) {
                    val bleData = BleData(characteristic.value, ConnectionState.Connected)
                    coroutineScope.launch { data.emit(Resource.Success(bleData)) }
                }
            }
        }
    }

    private fun findCharacteristics(serviceUUID: String, characteristicsUUID:String) : BluetoothGattCharacteristic? {
        return bleGatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }

    private fun enableNotification() {
        val characteristic = findCharacteristics(OBS_SERVICE_UIID, OBS_CHARACTERISTICS_UUID)
        if (characteristic == null) {
            coroutineScope.launch { data.emit(Resource.Error("Could not find OBS BLE service")) }
            return
        }
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if (bleGatt?.setCharacteristicNotification(characteristic, true) == false) {
                return
            } else writeDescription(cccdDescriptor, payload)
        }
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        bleGatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, payload)
            } else {
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            }
        } ?: coroutineScope.launch { data.emit(Resource.Error("Not connected to a BLE device!")) }
    }
}