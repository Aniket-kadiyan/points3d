package com.example.points3d.ui

//import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.points3d.R
import com.example.points3d.databinding.ActivityMainBinding
import com.example.points3d.domain.Point3D
import com.example.points3d.domain.PointState
import com.example.points3d.scene.addColoredSpherePoint
import com.example.points3d.scene.addModelFromRaw
import com.example.points3d.scene.addRenderableNode
import com.example.points3d.scene.createColoredSphere
import com.google.ar.sceneform.math.Vector3
import kotlinx.coroutines.launch
import android.graphics.Color as AColor

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load reference model (GLB in res/raw)
        val ctx = this@MainActivity
        lifecycleScope.launch {
            try {
//                val model = loadGlbFromRaw(ctx, R.raw.sample_model)
//                addRenderableNode(
//                    binding.sceneView,
//                    model,
//                    position = Vector3(0f, 0f, -0.5f)
//                )
                addModelFromRaw(
                    context = ctx,
                    sceneView = binding.sceneView,
                    rawResId = R.raw.sample_model,
                    position = Vector3(0f, 0f, -0.5f)
                )
                binding.infoText.text = "Model loaded ✓"
            } catch (e: Exception) {
                Log.e("ModelLoad", "Failed: ${e.message}", e)
                binding.infoText.text = "Model load failed: ${e.message}"
            }
        }

        // Observe and render points
        vm.samplePoints.observe(this) { points ->
//            renderPoints(points)
            lifecycleScope.launch {
                for (p in points) {
                    addColoredSpherePoint(
                        context = ctx,
                        sceneView = binding.sceneView,
                        position = Vector3(p.x, p.y, p.z),
                        radiusMeters = 0.01f,
                        colorInt = stateToColor(p.state)
                    ) { showInfo(p) }
                }
            }
        }

    }

    private fun stateToColor(state: PointState): Int = when (state) {
        PointState.IDLE -> AColor.GRAY
        PointState.COMPLETE -> AColor.GREEN
        PointState.ERROR -> AColor.RED
    }

    private fun renderPoints(points: List<Point3D>) {
        lifecycleScope.launch {
            for (p in points) {
                val sphere = createColoredSphere(
                    this@MainActivity,
                    radius = 0.01f,
                    colorInt = stateToColor(p.state)
                )
                addRenderableNode(
                    binding.sceneView,
                    sphere,
                    position = Vector3(p.x, p.y, p.z)
                ) { showInfo(p) }
            }
        }
    }

    private fun showInfo(p: Point3D) {
        binding.infoText.text = "Model: ${p.modelNo} | Point: ${p.pointNo} | State: ${p.state}"
    }
}