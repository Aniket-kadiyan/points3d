package com.example.points3d.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points3d.domain.Point3D
import com.example.points3d.domain.PointState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _samplePoints = MutableLiveData<List<Point3D>>()
    val samplePoints: LiveData<List<Point3D>> = _samplePoints

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _samplePoints.postValue(
                listOf(
                    Point3D("M123", "P1", 0.0f, 0.0f, -0.2f, PointState.IDLE),
                    Point3D("M123", "P2", 0.1f, 0.05f, -0.25f, PointState.COMPLETE),
                    Point3D("M123", "P3", -0.1f, 0.02f, -0.3f, PointState.ERROR)
                )
            )
        }
    }
}