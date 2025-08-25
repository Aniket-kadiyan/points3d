package com.example.points3d.ui


import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.points3d.databinding.ActivityMainBinding
import com.example.points3d.domain.Point3D
import com.example.points3d.domain.PointState
import com.example.points3d.scene.addModelFromAssets

import com.example.points3d.scene.addPointSphereFromAsset
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import android.view.MotionEvent
import io.github.sceneview.collision.HitResult


class MainActivity : ComponentActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var orbit: OrbitPanController
    private val vm: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


// Load reference model from assets/models/
        lifecycleScope.launch {
            addModelFromAssets(
                sceneView = binding.sceneView,
                assetPath = "models/sample_model.glb"
            )

        }


// Render points when data arrives
        vm.samplePoints.observe(this) { points ->
            lifecycleScope.launch {
                for (p in points) {
                    addPointSphereFromAsset(
                        sceneView = binding.sceneView,
                        sphereAssetPath = stateToSpherePath(p.state),
                        position = Position(p.x, p.y, p.z),
                        radiusMeters = 0.01f
                    )
                }
            }
        }


// Tap detection (global listener)
        binding.sceneView.onTouchEvent = { e: MotionEvent, hit: HitResult? ->
            val node = hit?.node
            if (node is ModelNode) {
                binding.infoText.text = "Tapped node at ${node.position}"
                true
            } else {
                false
            }
        }


    }


    private fun stateToColor(state: PointState): Int = when (state) {
        PointState.IDLE -> AColor.GRAY
        PointState.COMPLETE -> AColor.GREEN
        PointState.ERROR -> AColor.RED
    }

    fun stateToSpherePath(state: PointState): String = when (state) {
        PointState.IDLE -> "models/point_idle.glb"
        PointState.COMPLETE -> "models/point_complete.glb"
        PointState.ERROR -> "models/point_error.glb"
    }

    private fun showInfo(p: Point3D) {
        binding.infoText.text = "Model: ${p.modelNo} | Point: ${p.pointNo} | State: ${p.state}"
    }
}