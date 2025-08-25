package com.example.points3d

import android.app.Application
import androidx.room.Room
import com.example.points3d.data.local.AppDatabase

class Points3DApp : Application() {
    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "points3d.db"
        ).fallbackToDestructiveMigration().build()
    }
}