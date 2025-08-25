package com.example.points3d.scene

import android.util.Log
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode

private const val TAG = "SceneHelpers"

/** Load a GLB from assets/models and add it to the SceneView (synchronous create). */
fun addModelFromAssets(
    sceneView: SceneView,
    assetPath: String,                           // e.g. "models/sample_model.glb"
    position: Position = Position(0f, 0f, -0.5f),
    rotation: Rotation = Rotation(0f, 0f, 0f),
    scale: Scale = Scale(1f),
    onLoaded: ((ModelNode) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    try {
        // Create a ModelInstance directly from the asset path
        val modelInstance = sceneView.modelLoader.createModelInstance(assetPath)
        val node = ModelNode(modelInstance).apply {
            this.position = position
            this.rotation = rotation
            this.scale = scale
        }
        sceneView.addChildNode(node)
        onLoaded?.invoke(node)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load model: $assetPath", e)
        onError?.invoke(e)
    }
}

/**
 * Add a point by loading a tiny pre-colored sphere GLB and scaling it.
 *
 * @param sphereAssetPath one of:
 *  - "models/point_idle.glb"
 *  - "models/point_complete.glb"
 *  - "models/point_error.glb"
 */
fun addPointSphereFromAsset(
    sceneView: SceneView,
    sphereAssetPath: String,
    position: Position,
    radiusMeters: Float = 0.01f,
    onLoaded: ((ModelNode) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    try {
        val modelInstance = sceneView.modelLoader.createModelInstance(sphereAssetPath)
        val node = ModelNode(modelInstance).apply {
            this.position = position
            this.scale = Scale(radiusMeters)
        }
        sceneView.addChildNode(node)
        onLoaded?.invoke(node)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load sphere: $sphereAssetPath", e)
        onError?.invoke(e)
    }
}
