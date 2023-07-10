package com.example.obsmini.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery1Bar
import androidx.compose.material.icons.rounded.Battery2Bar
import androidx.compose.material.icons.rounded.Battery3Bar
import androidx.compose.material.icons.rounded.Battery4Bar
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.Battery6Bar
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsmini.MAX_DISTANCE_cm
import com.example.obsmini.ble.ConnectionState


@Composable
fun MainScreen(
    navController: NavController, viewModel: MyViewModel
) {
    // Display always on as long as connected to BLE device
    val window = LocalContext.current as Activity

    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val message by viewModel.bleMessage.collectAsStateWithLifecycle()

    Row {
        ControlButtons(navController, viewModel, window)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (connectionState) {
                ConnectionState.Disconnected -> {
                    window.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.BluetoothDisabled, null)
                        Text(text = "Disconnected")
                    }
                }

                ConnectionState.CurrentlyInitializing -> {
                    window.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(text = message.toString())
                    }
                }

                ConnectionState.Failed -> {
                    window.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Error, null)
                        Text(text = message.toString())
                    }
                }

                ConnectionState.Connected -> Data(viewModel)
            }
        }
    }
}

@Composable
fun ControlButtons(
    navController: NavController, viewModel: MyViewModel, window: Activity
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    var openDialog by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(modifier = Modifier.padding(12.dp),
            onClick = { navController.navigate(Screen.Settings.route) }) {
            Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (connectionState == ConnectionState.Connected) {
            FloatingActionButton(onClick = { viewModel.stopTrack() }) {
                Icon(Icons.Rounded.Stop, null, modifier = Modifier.size(32.dp))
            }
        } else {
            FloatingActionButton(onClick = {
                if (connectionState != ConnectionState.CurrentlyInitializing) {
                    viewModel.startNewTrack()
                }
            }) {
                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(32.dp))
            }
        }
        FloatingActionButton(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp), onClick = {
            if (connectionState == ConnectionState.Connected) {
                openDialog = true
            } else if (connectionState != ConnectionState.CurrentlyInitializing) {
                navController.navigate(Screen.SavedTracks.route)
            }
        }) {
            Icon(Icons.Rounded.Upload, null, modifier = Modifier.size(32.dp))
        }
    }

    if (openDialog) {
        AlertDialog(onDismissRequest = { openDialog = false },
            title = { Text(text = "You are still tracking.") },
            text = { Text(text = "If you continue your current track will be stopped and saved.") },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    viewModel.stopTrack()
                    window.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    navController.navigate(Screen.SavedTracks.route)
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text("Cancel")
                }
            })
    }
}

@Composable
fun Data(viewModel: MyViewModel) {
    Column(modifier = Modifier
        .clickable {
            viewModel.toggleTheme()
            viewModel.overtaking()
        }
        .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)) {
        Row(modifier = Modifier.weight(5f)) {
            MinLeftDistance(Modifier.weight(3f), viewModel)
            Spacer(Modifier.size(8.dp))
            LocationBatterySpeed(Modifier.weight(2f), viewModel)
        }
        Spacer(Modifier.size(8.dp))
        DynamicDistances(Modifier.weight(3f), viewModel)
    }
}

@Composable
fun MinLeftDistance(
    modifier: Modifier = Modifier, viewModel: MyViewModel
) {
    val smallestLeftDistanceInLastThreeSeconds by viewModel.smallestLeftDistanceInLastThreeSeconds.collectAsStateWithLifecycle()
    val infinitySymbol = "\u221E"
    Card(modifier = modifier.fillMaxSize()) {
        AutoResizedText(
            text = if (smallestLeftDistanceInLastThreeSeconds < MAX_DISTANCE_cm) smallestLeftDistanceInLastThreeSeconds.toString()
            else infinitySymbol,
            defaultFontSize = 300.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
        )
    }
}

@Composable
fun LocationBatterySpeed(
    modifier: Modifier = Modifier, viewModel: MyViewModel
) {
    val latitude by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude by viewModel.longitude.collectAsStateWithLifecycle()
    val altitude by viewModel.altitude.collectAsStateWithLifecycle()
    val speed by viewModel.speed.collectAsStateWithLifecycle()
    val batteryVoltageV by viewModel.batteryVoltageV.collectAsStateWithLifecycle()

    Card(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "$latitude °N")
                        Text(text = "$longitude °E")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Rounded.Language, null)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$altitude m")
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Rounded.Height, null)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$batteryVoltageV V")
                    Spacer(modifier = Modifier.width(16.dp))
                    when (batteryVoltageV.toFloat()) {
                        in 4.1f..4.2f -> Icon(Icons.Rounded.BatteryFull, null)
                        in 3.9f..4.1f -> Icon(Icons.Rounded.Battery6Bar, null)
                        in 3.7f..3.9f -> Icon(Icons.Rounded.Battery5Bar, null)
                        in 3.6f..3.7f -> Icon(Icons.Rounded.Battery4Bar, null)
                        in 3.5f..3.6f -> Icon(Icons.Rounded.Battery3Bar, null)
                        in 3.4f..3.5f -> Icon(Icons.Rounded.Battery2Bar, null)
                        in 3.3f..3.4f -> Icon(
                            Icons.Rounded.Battery1Bar, null, tint = MaterialTheme.colorScheme.error
                        )

                        else -> Icon(
                            Icons.Rounded.BatteryAlert, null, tint = MaterialTheme.colorScheme.error
                        )
                    }

                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$speed ", style = MaterialTheme.typography.displaySmall)
                    Text(text = "km/h")
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Rounded.Speed, null)
                }
            }
        }
    }
}

@Composable
fun DynamicDistances(
    modifier: Modifier = Modifier, viewModel: MyViewModel
) {
    val minLeftDistanceCM by viewModel.minLeftDistanceCmNew.collectAsStateWithLifecycle()
    val minRightDistanceCM by viewModel.minRightDistanceCmNew.collectAsStateWithLifecycle()
    val infinitySymbol = "\u221E"
    Card(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(
                modifier = modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = 1f - (minLeftDistanceCM.toFloat() / MAX_DISTANCE_cm.toFloat()),
                    color = MaterialTheme.colorScheme.background,
                    trackColor = ProgressIndicatorDefaults.linearColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 16.dp, end = 8.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(40))
                )
                Text(
                    text = if (minLeftDistanceCM < MAX_DISTANCE_cm) minLeftDistanceCM.toString() else infinitySymbol,
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Column(
                modifier = modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = minRightDistanceCM.toFloat() / MAX_DISTANCE_cm.toFloat(),
                    trackColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, top = 16.dp, end = 16.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(40))
                )
                Text(
                    text = if (minRightDistanceCM < MAX_DISTANCE_cm) minRightDistanceCM.toString() else infinitySymbol,
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}


@Composable
fun AutoResizedText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = style.color,
    defaultFontSize: TextUnit = style.fontSize
) {
    var resizedFontSize by remember {
        mutableStateOf(defaultFontSize)
    }
    var shouldDraw by remember {
        mutableStateOf(false)
    }
    Text(text = text,
        color = color,
        style = LocalTextStyle.current.merge(
            TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false      // necessary to center the text
                )
            )
        ),
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        textAlign = TextAlign.Center,
        softWrap = false,
        fontSize = resizedFontSize,
        onTextLayout = { result ->
            if (result.didOverflowHeight or result.didOverflowWidth) {
                if (style.fontSize.isUnspecified) {
                    resizedFontSize = defaultFontSize
                }
                resizedFontSize *= 0.95

            } else {
                shouldDraw = true
            }
        })
}