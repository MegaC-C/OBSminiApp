package com.example.obsmini.ble


data class BleData(
    val bleBytes: ByteArray,
    val connectionState: ConnectionState
)

sealed interface ConnectionState{
    object Connected: ConnectionState
    object Disconnected: ConnectionState
    object CurrentlyInitializing: ConnectionState
    object Failed: ConnectionState
}

sealed class Resource<out T: Any>{
    data class Success<out T: Any> (val data:T): Resource<T>()
    data class Error(val errorMessage:String): Resource<Nothing>()
    data class Loading<out T: Any>(val data:T? = null, val message:String? = null): Resource<T>()
}