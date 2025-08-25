package com.example.points3d.data.remote

data class StatusRequest(
    val model_no: String,
    val point_no: String,
    val value: Double
)

data class StatusResponse(
    val state: String // "idle" | "complete" | "error"
)

data class ModelBundle(
    val model_no: String,
    val model_url: String,
    val points: List<ModelPoint>
)

data class ModelPoint(
    val point_no: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val state: String
)