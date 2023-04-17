package com.example.obsmini.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController


@Composable
fun SavedTrackScreen(
    navController: NavController,
    viewModel : MyViewModel
) {
    val savedTracks by viewModel.savedTracks.collectAsStateWithLifecycle()

    Row() {
        IconButton(
            modifier = Modifier.padding(12.dp),
            onClick = { navController.navigate(Screen.Main.route) }
        ) {
            Icon(Icons.Rounded.ArrowBack, null)
        }
        LazyColumn() {
            items(savedTracks.size) { index ->
                ElevatedCard(modifier = Modifier.padding(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.uploadTrack(savedTracks[index]) }) {
                            Icon(Icons.Rounded.Upload, null)
                        }
                        Text(text = savedTracks[index])
                        IconButton(onClick = { viewModel.deleteTrack(savedTracks[index]) }) {
                            Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}