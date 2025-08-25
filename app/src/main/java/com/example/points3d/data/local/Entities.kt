package com.example.points3d.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point")
data class PointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelNo: String,
    val pointNo: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val state: String
)

@Entity(tableName = "model")
data class ModelEntity(
    @PrimaryKey val modelNo: String,
    val modelUrl: String
)