package com.example.skannedai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalText: String,
    val mode: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)