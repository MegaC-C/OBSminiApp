package com.example.obsmini.fileManager

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.IOException
import javax.inject.Inject


interface FileWriter {
    val directory: File
    fun writeTrack(text: String, fileName: String, headerMetadata: String)
    fun deleteTrack(fileName: String)
    fun listSavedTracks(): List<String>
}


class FileWriterImpl @Inject constructor(private val context: Context) : FileWriter {

    private val dirName = "savedTracks"
    override val directory: File = context.getDir(dirName, Context.MODE_PRIVATE)

    override fun writeTrack(text: String, fileName: String, headerMetadata: String) {
        val file = File(directory, "$fileName.csv")
        if (!listSavedTracks().contains("$fileName.csv")) {
            try {
                file.writeText(headerMetadata)
                Toast.makeText(context, "File created!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Unable to create File!", Toast.LENGTH_SHORT).show()
            }
        }
        try {
            file.appendText(text)
        } catch (e: IOException) {
            Toast.makeText(context, "Unable to write to File!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun deleteTrack(fileName: String) {
        if (listSavedTracks().contains(fileName)) {
            try {
                File(directory.path, fileName).delete()
                Toast.makeText(context, "File deleted!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "An Error Occurred", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "File nonexistent", Toast.LENGTH_SHORT).show()
        }
    }

    override fun listSavedTracks(): List<String> {
        val files = directory.list()
        return files?.toList() ?: emptyList()
    }
}