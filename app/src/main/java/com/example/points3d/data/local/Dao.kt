package com.example.points3d.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(points: List<PointEntity>)

    @Query("SELECT * FROM point WHERE modelNo = :modelNo")
    suspend fun pointsForModel(modelNo: String): List<PointEntity>
}

@Dao
interface ModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: ModelEntity)

    @Query("SELECT * FROM model WHERE modelNo = :modelNo")
    suspend fun get(modelNo: String): ModelEntity?
}