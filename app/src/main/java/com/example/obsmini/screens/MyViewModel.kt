package com.example.obsmini.screens

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obsmini.FIRMWARE_VERSION
import com.example.obsmini.MAX_DISTANCE_cm
import com.example.obsmini.NUMBER_OF_MEASUREMENTS_PER_SIDE
import com.example.obsmini.PAYLOAD_IN_BYTE
import com.example.obsmini.ble.BleManager
import com.example.obsmini.ble.ConnectionState
import com.example.obsmini.ble.Resource
import com.example.obsmini.location.LocationClient
import com.example.obsmini.dataStore.MyDataStore
import com.example.obsmini.fileManager.FileRepository
import com.example.obsmini.fileManager.FileWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.lang.System.currentTimeMillis
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.inject.Inject

// ----- this interface does not work using Hilt. perhaps other Solution? -----
// ----- properties and methods are only for overview, no (return)type mentioned -----
//interface MyViewModelInterface {
//
// ##### Properties #####
// ----- DataStore -----
//val portalUrlFlow
//val portalTokenFlow
//val leftHandlebarFlow
//val rightHandlebarFlow
//val isDarkThemeFlow
//val isLeftRight
//
// ----- Location -----
//val latitude
//val longitude
//val altitude
//val course
//val speed
//val hdop
//
// ----- FileLogic -----
//val savedTracks
//
// ----- BLE -----
//val bleBytes
//val bleMessage
//val connectionState
//
// ----- BleDataLogic -----
//val batteryVoltageV
//val minLeftDistanceCmNew
//val minRightDistanceCmNew
//val smallestLeftDistanceInLastThreeSeconds
//
//
// ###### Methods ######
// ----- DataStore -----
//fun setPortalUrl(portalUrl: String)
//fun setPortalToken(portalToken: String)
//fun setLeftHandlebar(rightHandlebar: Int)
//fun setRightHandlebar(rightHandlebar: Int)
//fun changeLeftRight()
//fun changeTheme()
//fun toggleTheme()
//
// ----- FileLogic -----
//fun startNewTrack()
//fun stopTrack()
//
// ----- FileLogic -----
//fun uploadTrack(trackName: String)
//fun deleteTrack(fileName: String)
//
// ----- BleDataLogic -----
//fun overtaking()
//}


@HiltViewModel
class MyViewModel @Inject constructor(
    private val myDataStore: MyDataStore,
    private val bleManager: BleManager,
    private val locationClient: LocationClient,
    private val repository: FileRepository,
    private val fileWriter: FileWriter
) : ViewModel() {

    // ############################## Properties #############################
    // ------------------------------ DataStore ------------------------------
    private val _portalUrlFlow = MutableStateFlow("")
    val portalUrlFlow = _portalUrlFlow.asStateFlow()

    private val _portalTokenFlow = MutableStateFlow("")
    val portalTokenFlow = _portalTokenFlow.asStateFlow()

    private val _leftHandlebarFlow = MutableStateFlow(0)
    val leftHandlebarFlow = _leftHandlebarFlow.asStateFlow()

    private val _rightHandlebarFlow = MutableStateFlow(0)
    val rightHandlebarFlow = _rightHandlebarFlow.asStateFlow()

    private val _isDarkThemeFlow = MutableStateFlow(true)
    val isDarkThemeFlow = _isDarkThemeFlow.asStateFlow()

    private val _isLeftRight = MutableStateFlow(true)
    val isLeftRight = _isLeftRight.asStateFlow()

    // ------------------------------ Location ------------------------------
    private val _latitude = MutableStateFlow("")
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow("")
    val longitude = _longitude.asStateFlow()

    private val _altitude = MutableStateFlow("")
    val altitude = _altitude.asStateFlow()

    private val _course = MutableStateFlow("")
    val course = _course.asStateFlow()

    private val _speed = MutableStateFlow("")
    val speed = _speed.asStateFlow()

    private val _hdop = MutableStateFlow("")
    val hdop = _hdop.asStateFlow()

    // ------------------------------ BLE ------------------------------
    private val _bleBytes = MutableStateFlow(byteArrayOf())
    val bleBytes = _bleBytes.asStateFlow()

    private val _bleMessage = MutableStateFlow<String?>(null)
    val bleMessage = _bleMessage.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    // ------------------------------ FileLogic ------------------------------
    private val _savedTracks = MutableStateFlow(listOf(""))
    val savedTracks = _savedTracks.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    // ------------------------------ BleDataLogic ------------------------------
    private val _batteryVoltageV = MutableStateFlow("")
    val batteryVoltageV = _batteryVoltageV.asStateFlow()

    private val _minLeftDistanceCmNew = MutableStateFlow(MAX_DISTANCE_cm)
    val minLeftDistanceCmNew = _minLeftDistanceCmNew.asStateFlow()

    private val _minRightDistanceCmNew = MutableStateFlow(MAX_DISTANCE_cm)
    val minRightDistanceCmNew = _minRightDistanceCmNew.asStateFlow()

    private val _smallestLeftDistanceInLastThreeSeconds = MutableStateFlow(MAX_DISTANCE_cm)
    val smallestLeftDistanceInLastThreeSeconds = _smallestLeftDistanceInLastThreeSeconds.asStateFlow()

    private var leftDistancesList = mutableListOf<Int>()
    private var rightDistancesList = mutableListOf<Int>()
    private var smallestLeftDistanceInLastThreeSecondsList = mutableListOf(MAX_DISTANCE_cm, MAX_DISTANCE_cm, MAX_DISTANCE_cm)
    private var minLeftDistanceCmMiddle = MAX_DISTANCE_cm
    private var minLeftDistanceCmOld = MAX_DISTANCE_cm
    private var minLeftIndexNew = 0
    private var minLeftIndexMiddle = 0
    private var minLeftIndexOld = 0
    private var minRightIndex = 0
    private var oneTrackRowNew = mutableListOf<String>()
    private var oneTrackRowMiddle = mutableListOf<String>()
    private var oneTrackRowOld = mutableListOf<String>()

    private var startMillis = 0L
    private var lastDeltaMillis = 0L
    private var trackName = ""
    private val COMMENT = ""
    private val SATELLITES = 5
    private var confirmed = 0
    private val MARKED = ""
    private val INVALID = 0
    private val INSIDE_PRIVACY_AREA = 0
    private val FACTOR = 58

    // ############################### Methods ###############################
    // ------------------------------ DataStore ------------------------------
    fun setPortalUrl(portalUrl: String) { viewModelScope.launch { myDataStore.setPortalUrl(portalUrl) } }
    fun setPortalToken(portalToken: String) { viewModelScope.launch { myDataStore.setPortalToken(portalToken) } }
    fun setLeftHandlebar(rightHandlebar: Int) { viewModelScope.launch { myDataStore.setLeftHandlebar(rightHandlebar) } }
    fun setRightHandlebar(rightHandlebar: Int) { viewModelScope.launch { myDataStore.setRightHandlebar(rightHandlebar) } }
    fun changeLeftRight() { viewModelScope.launch { myDataStore.changeLeftRight() } }
    fun changeTheme() { viewModelScope.launch { myDataStore.changeTheme() } }

    fun toggleTheme() {
        viewModelScope.launch {
            myDataStore.changeTheme()
            delay(500)
            myDataStore.changeTheme()
        }
    }

    private fun subscribeToDataStore() {
        viewModelScope.launch { myDataStore.getTheme().collect() { _isDarkThemeFlow.value = it } }
        viewModelScope.launch { myDataStore.getLeftRight().collect() { _isLeftRight.value = it } }
        viewModelScope.launch { myDataStore.getRightHandlebarFlow().collect() { _rightHandlebarFlow.value = it } }
        viewModelScope.launch { myDataStore.getLeftHandlebarFlow().collect() { _leftHandlebarFlow.value = it } }
        viewModelScope.launch { myDataStore.getPortalTokenFlow().collect() { _portalTokenFlow.value = it } }
        viewModelScope.launch { myDataStore.getPortalUrlFlow().collect() { _portalUrlFlow.value = it } }
    }

    // ------------------------------ Location ------------------------------
    private fun startLocationTracking(interval: Long) {
        locationClient
            .getLocationUpdates(interval)
            .onEach { location ->
                _latitude.value = String.format("%.7f", location.latitude).replace(",", ".")
                _longitude.value = String.format("%.7f", location.longitude).replace(",", ".")
                _altitude.value = String.format("%.2f", location.altitude).replace(",", ".")
                _course.value = String.format("%.5f", location.bearing).replace(",", ".")
                _speed.value = String.format("%.1f", location.speed).replace(",", ".")
                _hdop.value = String.format("%.2f", location.accuracy/3).replace(",", ".") // div by 3 to get aprox. HDOP
            }
            .launchIn(viewModelScope)
    }

    // ------------------------------ BLE ------------------------------
    private fun subscribeToBleChanges() {
        viewModelScope.launch {
            bleManager.data.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _connectionState.value = result.data.connectionState
                        _bleBytes.value = result.data.bleBytes
                        if (bleBytes.value.isNotEmpty()) {
                            writeOneTrackRow(bleBytes.value)
                        }
                    }
                    is Resource.Loading -> {
                        _bleMessage.value = result.message
                        _connectionState.value = ConnectionState.CurrentlyInitializing
                    }
                    is Resource.Error -> {
                        _bleMessage.value = result.errorMessage
                        _connectionState.value = ConnectionState.Failed
                    }
                }
            }
        }
    }

    fun startNewTrack() {
        _bleMessage.value = null
        bleManager.startScanning()
        startLocationTracking(1000)
        oneTrackRowNew.clear()
        oneTrackRowMiddle.clear()
        oneTrackRowOld.clear()
        trackName = SimpleDateFormat("yyyy MM dd HH mm ss").format(Date())
        startMillis = currentTimeMillis()
    }

    fun stopTrack() {
        bleManager.closeConnection()
    }
    override fun onCleared() {
        super.onCleared()
        bleManager.closeConnection()
    }

    // ------------------------------ FileLogic ------------------------------
    fun uploadTrack(trackName: String) {
        _isUploading.value = true
        val track = File(fileWriter.directory, trackName)
        viewModelScope.launch {
            if (repository.uploadTrack(
                    url = myDataStore.getPortalUrlFlow().first() + "/api/tracks",
                    token = "OBSUserId " + myDataStore.getPortalTokenFlow().first(),
                    file = track
                )
            ) {
                deleteTrack(trackName) // only when upload was successful
                _isUploading.value = false
            } else _isUploading.value = false
        }
    }

    fun deleteTrack(fileName: String) {
        fileWriter.deleteTrack(fileName)
        updateSavedTracks()
    }

    private fun writeToTrack(text: String, fileName: String) {
        var timeNLeftNRightN = ""
        for (i in 0 until NUMBER_OF_MEASUREMENTS_PER_SIDE) {
            timeNLeftNRightN += "Tms${i+1};Lus${i+1};Rus${i+1};"
        }
        val header = "Date;Time;Millis;Comment;Latitude;Longitude;Altitude;Course;Speed;HDOP;Satellites;" +
                "BatteryLevel;Left;Right;Confirmed;Marked;Invalid;InsidePrivacyArea;Factor;Measurements;" +
                timeNLeftNRightN

        val headerMetadata = "OBSDataFormat=2&" +
                "OBSFirmwareVersion=$FIRMWARE_VERSION&" +
                "DataPerMeasurement=3&" +
                "MaximumMeasurementsPerLine=$NUMBER_OF_MEASUREMENTS_PER_SIDE&" +
                "OffsetLeft=${leftHandlebarFlow.value}&" +
                "OffsetRight=${rightHandlebarFlow.value}&" +
                "NumberOfDefinedPrivacyAreas=0&" +
                "PrivacyLevelApplied=NoPrivacy&" +
                "MaximumValidFlightTimeMicroseconds=18560&" +
                "DistanceSensorsUsed=HC-SR04/JSN-SR04T&" +
                "BluetoothEnabled=1&" +
                "PresetId=default&" +
                "TimeZone=GPS&" +
                "TrackId=${UUID.randomUUID()}\n" +
                header.dropLast(1) + "\n"       // drop excess semicolon at header end

        fileWriter.writeTrack(text, fileName, headerMetadata)
        updateSavedTracks()
    }

    private fun updateSavedTracks() {
        _savedTracks.value = fileWriter.listSavedTracks().sortedByDescending { it }
    }

    // ------------------------------ BleDataLogic ------------------------------
    // all BLE data is send in one ByteArray, fist it must be decoded to Ints and then the battery and distance data must be separated
    private fun decodeBleByteList(byteList: ByteArray): Boolean {
        if (byteList.size != PAYLOAD_IN_BYTE) return false

        // decode received BLE ByteArray to mutableListOf<Int>
        val buffer = ByteBuffer.wrap(byteList)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val decodedValues = mutableListOf<Int>()
        while (buffer.hasRemaining()) {
            val encodedValue = buffer.short.toInt()
            decodedValues.add(encodedValue)
        }

        // extract battery voltage and separate remaining list of alternating left right distances to one left and one right distance list
        val batteryVoltagemV = decodedValues.removeFirst()
        _batteryVoltageV.value = String.format("%.2f", batteryVoltagemV.toFloat() / 1000f).replace(",", ".")
        val leftList = mutableListOf<Int>()
        val rightList = mutableListOf<Int>()
        for (i in decodedValues.indices) {
            if (i % 2 == 0) {
                leftList.add(decodedValues[i])
            } else {
                rightList.add(decodedValues[i])
            }
        }
        if (isLeftRight.value) {
            leftDistancesList = leftList
            rightDistancesList = rightList
        } else {
            leftDistancesList = rightList
            rightDistancesList = leftList
        }

        minLeftIndexOld = minLeftIndexMiddle
        minLeftIndexMiddle = minLeftIndexNew
        minLeftDistanceCmOld = minLeftDistanceCmMiddle
        minLeftDistanceCmMiddle = minLeftDistanceCmNew.value

        // find the smallest distance and corresponding measurement
        _minLeftDistanceCmNew.value = leftDistancesList.min() / FACTOR
        minLeftIndexNew = leftDistancesList.indexOf(minLeftDistanceCmNew.value) + 1 // in myTrack.csv the index n starts at 1
        _minRightDistanceCmNew.value = rightDistancesList.min() / FACTOR
        minRightIndex = rightDistancesList.indexOf(minRightDistanceCmNew.value) + 1

        smallestLeftDistanceInLastThreeSecondsList.removeLast()
        smallestLeftDistanceInLastThreeSecondsList.add(0, minLeftDistanceCmNew.value)
        _smallestLeftDistanceInLastThreeSeconds.value = smallestLeftDistanceInLastThreeSecondsList.min()

        return true
    }

    // the last two dataRows are discarded when BLE is stopped, probably it`s not necessary to fix it?
    private fun writeOneTrackRow(bleByteList: ByteArray) {
        if (!decodeBleByteList(bleByteList)) return
        val date = SimpleDateFormat("dd.MM.yyyy").format(Date())
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        val deltaMillis = currentTimeMillis() - startMillis
        val measurements = leftDistancesList.size
        val timeBetweenMeasurements = (deltaMillis - lastDeltaMillis) / measurements
        lastDeltaMillis = deltaMillis

        if (oneTrackRowOld.isNotEmpty()) {
            writeToTrack(oneTrackRowOld.joinToString(separator = "", prefix = "", postfix = ""), trackName)
        }

        oneTrackRowOld = oneTrackRowMiddle
        oneTrackRowMiddle = oneTrackRowNew
        oneTrackRowNew = mutableListOf(
            "$date;",
            "$time;",
            "$deltaMillis;",
            "$COMMENT;",
            "${latitude.value};",
            "${longitude.value};",
            "${altitude.value};",
            "${course.value};",
            "${speed.value};",
            "${hdop.value};",
            "$SATELLITES;",
            "${batteryVoltageV.value};",
            "${minLeftDistanceCmNew.value};",
            "${minRightDistanceCmNew.value};",
            "$confirmed;",
            "$MARKED;",
            "$INVALID;",
            "$INSIDE_PRIVACY_AREA;",
            "$FACTOR;",
            "$measurements;"
        )

        for (i in 0 until measurements) {
            if (i < measurements - 1 ){
                oneTrackRowNew.addAll(listOf(
                    "${timeBetweenMeasurements * (i+1)};",
                    "${leftDistancesList[i]};",
                    "${rightDistancesList[i]};"
                ))
            } else {
                oneTrackRowNew.addAll(listOf(
                    "${timeBetweenMeasurements * (i+1)};",
                    "${leftDistancesList[i]};",
                    "${rightDistancesList[i]}\n"
                ))
            }
        }
    }

    // 3 rows are cached to find the smallest left distance of the last 3 seconds when an overtaking event occurs
    fun overtaking() {
        val smallest = minOf(minLeftDistanceCmNew.value, minLeftDistanceCmMiddle, minLeftDistanceCmOld)

        when (smallest) {
            minLeftDistanceCmNew.value -> oneTrackRowNew[14] = "$minLeftIndexNew;"
            minLeftDistanceCmMiddle -> oneTrackRowMiddle[14] = "$minLeftIndexMiddle;"
            minLeftDistanceCmOld -> oneTrackRowOld[14] = "$minLeftIndexOld;"
        }
    }


    init {
        updateSavedTracks()
        subscribeToDataStore()
        subscribeToBleChanges()
    }
}