@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.obsmini

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.obsmini.ble.BleManager
import com.example.obsmini.ble.MyBroadcastReceiver
import com.example.obsmini.dataStore.MyDataStore
import com.example.obsmini.screens.MyNavigation
import com.example.obsmini.ui.theme.OBSminiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class ObsMiniApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var myDataStore: MyDataStore

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    @Inject
    lateinit var bleManager: BleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        ContextCompat.registerReceiver(
            this,
            myBroadcastReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setContent {
            val isDarkTheme by myDataStore.getTheme().collectAsState(initial = true)
            OBSminiTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    permissionLauncher.launch(VersionPermissions.permissions)
                    val permissionsState = rememberMultiplePermissionsState(VersionPermissions.permissions.toList())

                    if (permissionsState.allPermissionsGranted) {
                        enableBleDialog()
                        enableLocationDialog()
                        MyNavigation()
                    } else {
                        MissingPermissionsScreen()
                    }

                }
            }
        }
    }
    // Note: You must call registerForActivityResult() before the fragment or activity is created,
    // but you can't launch the ActivityResultLauncher until the fragment or activity's Lifecycle
    // has reached CREATED. (https://developer.android.com/training/basics/intents/result)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Not needed */ }

    private var isBleDialogCurrentlyShown = false
    private val enableBleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isBleDialogCurrentlyShown = false
        if(result.resultCode != Activity.RESULT_OK){
            enableBleDialog()
        }
    }

    private var isLocationDialogCurrentlyShown = false
    private val enableLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLocationDialogCurrentlyShown = false
        if(result.resultCode != Activity.RESULT_OK){
            enableLocationDialog()
        }
    }

    private fun enableBleDialog() {
        if(!bluetoothAdapter.isEnabled && !isBleDialogCurrentlyShown) {
            isBleDialogCurrentlyShown = true
            bleManager.closeConnection()
            enableBleLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun enableLocationDialog() {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled && !isNetworkEnabled && !isLocationDialogCurrentlyShown) {
            isLocationDialogCurrentlyShown = true
            enableLocationLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    // needed to observe Ble & Location On/Off state
    private val myBroadcastReceiver = MyBroadcastReceiver ({enableBleDialog()}, {enableLocationDialog()})

    // if you register a receiver in onCreate(Bundle), you should unregister it in onDestroy()
    override fun onDestroy() {
        this.unregisterReceiver(myBroadcastReceiver)
        super.onDestroy()
    }
}

@Composable
fun MissingPermissionsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Please go to app settings and give permissions")
    }
}