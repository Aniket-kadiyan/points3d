package com.example.points3d.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PointEntity::class, ModelEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pointDao(): PointDao
    abstract fun modelDao(): ModelDao
}