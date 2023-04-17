package com.example.obsmini.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MyViewModel
) {
    val focusManager = LocalFocusManager.current

    val isDarkThemeState by viewModel.isDarkThemeFlow.collectAsStateWithLifecycle()
    val isLeftRight by viewModel.isLeftRight.collectAsStateWithLifecycle()
    val portalUrlState by viewModel.portalUrlFlow.collectAsStateWithLifecycle()
    val portalTokenState by viewModel.portalTokenFlow.collectAsStateWithLifecycle()
    val leftHandlebarDistanceState by viewModel.leftHandlebarFlow.collectAsStateWithLifecycle()
    val rightHandlebarDistanceState by viewModel.rightHandlebarFlow.collectAsStateWithLifecycle()

    // necessary to avoid buggy behaviour when giving input to OutlineTextField. Find better Solution?
    var portalUrl by remember { mutableStateOf(portalUrlState) }
    var portalToken by remember { mutableStateOf(portalTokenState) }
    var leftHandlebarDistance by remember { mutableStateOf(leftHandlebarDistanceState) }
    var rightHandlebarDistance by remember { mutableStateOf(rightHandlebarDistanceState) }

    Row() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                modifier = Modifier.padding(12.dp),
                onClick = { navController.navigate("main") }
            ) {
                Icon(Icons.Rounded.ArrowBack, null)
            }
            IconButton(
                modifier = Modifier.padding(12.dp),
                onClick = { viewModel.changeTheme() }
            ) {
                if (isDarkThemeState) {
                    Icon(Icons.Outlined.LightMode, null)
                } else {
                    Icon(Icons.Rounded.DarkMode, null)
                }
            }
            IconButton(
                modifier = Modifier.padding(top = 12.dp),
                onClick = { viewModel.changeLeftRight() }
            ) {
                Icon(Icons.Rounded.Cached, null)
            }
            if (isLeftRight) {
                Text(text = "L/R")
            } else {
                Text(text = "R/L")
            }
        }
        Column() {
            OutlinedTextField(
                label = { Text(text = "portal url") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                value = portalUrl,
                onValueChange = {
                    portalUrl = it
                    viewModel.setPortalUrl(portalUrl)
                }
            )
            OutlinedTextField(
                label = { Text(text = "portal token") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                value = portalToken,
                onValueChange = {
                    portalToken = it
                    viewModel.setPortalToken(portalToken)
                }
            )
            OutlinedTextField(
                label = { Text(text = "left handlebar distance") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                value = leftHandlebarDistance.toString(),
                onValueChange = {
                    leftHandlebarDistance = it.toIntOrNull() ?: 0
                    viewModel.setLeftHandlebar(leftHandlebarDistance)
                }
            )
            OutlinedTextField(
                label = { Text(text = "right handlebar distance") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),

                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                value = rightHandlebarDistance.toString(),
                onValueChange = {
                    rightHandlebarDistance = it.toIntOrNull() ?: 0
                    viewModel.setRightHandlebar(rightHandlebarDistance)
                }
            )
        }
    }
}