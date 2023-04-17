package com.example.obsmini.dependencyInjection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.obsmini.ble.BleManager
import com.example.obsmini.ble.BleManagerImpl
import com.example.obsmini.dataStore.MyDataStore
import com.example.obsmini.dataStore.MyDataStoreImpl
import com.example.obsmini.location.LocationClient
import com.example.obsmini.location.LocationClientImpl
import com.example.obsmini.fileManager.FileRepository
import com.example.obsmini.fileManager.FileRepositoryImpl
import com.example.obsmini.fileManager.FileUploadApi
import com.example.obsmini.fileManager.FileWriter
import com.example.obsmini.fileManager.FileWriterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ------------------------------ DataStore ------------------------------
    @Provides
    @Singleton
    fun provideMyDataStore(@ApplicationContext context: Context): MyDataStore {
        return MyDataStoreImpl(context)
    }


    // ------------------------------ BLE ------------------------------
    @Provides
    @Singleton
    fun provideBleAdapter(@ApplicationContext context: Context): BluetoothAdapter {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    fun provideBleManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ): BleManager {
        return BleManagerImpl(bluetoothAdapter, context)
    }

    // ------------------------------ Location ------------------------------
    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context): LocationClient {
        return LocationClientImpl(context)
    }


    // ------------------------------ FileManager ------------------------------
    @Provides
    @Singleton
    fun provideFileApi(): FileUploadApi {
        return Retrofit.Builder()
            .baseUrl("https://placeholder.com") // as placeholder url
            .build()
            .create(FileUploadApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFileRepository(@ApplicationContext context: Context, fileApi: FileUploadApi): FileRepository {
        return FileRepositoryImpl(context, fileApi)
    }

    @Provides
    @Singleton
    fun provideFileWriter(@ApplicationContext context: Context): FileWriter {
        return FileWriterImpl(context)
    }
//    --- does not work ---
//    @Provides
//    @Singleton
//    fun provideMyViewModel(
//        myDataStore: MyDataStore,
//        bleManager: BleManager,
//        locationClient: LocationClient,
//        repository: FileRepository,
//        fileWriter: FileWriter
//    ) : MyViewModelInterface {
//        return MyViewModel(myDataStore, bleManager, locationClient, repository, fileWriter)
//    }
}