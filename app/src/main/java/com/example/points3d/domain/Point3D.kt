package com.example.points3d.domain

data class Point3D(
    val modelNo: String,
    val pointNo: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val state: PointState
)