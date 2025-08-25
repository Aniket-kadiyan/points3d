package com.example.points3d.scene

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
//import com.google.ar.sceneform.rendering.RenderableSource
import com.google.ar.sceneform.rendering.ShapeFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import com.example.points3d.R

private const val TAG = "SceneHelpers"

/**
 * Load a GLB from res/raw and add it to the scene at the given position.
 * Uses the simple ModelRenderable.builder().setSource(context, uri) path (no RenderableSource).
 */
suspend fun addModelFromRaw(
    context: Context,
    sceneView: SceneView,
    @RawRes rawResId: Int,
    position: Vector3 = Vector3(0f, 0f, -0.5f)
): Node {
    val model = loadModelRenderableFromRaw(context, rawResId)
    val node = Node().apply {
        renderable = model
        localPosition = position
    }
    sceneView.scene.addChild(node)
    Log.d(TAG, "Model added at $position")
    return node
}

/** Create a small colored sphere renderable. */
suspend fun createColoredSphere(
    context: Context,
    radius: Float,
    colorInt: Int
): Renderable = suspendCancellableCoroutine { cont ->
    val sceneformColor = Color(colorInt)
    MaterialFactory.makeOpaqueWithColor(
        context, sceneformColor
    ).thenAccept { material ->
        val center = Vector3(0f, 0f, 0f)
        val sphere = ShapeFactory.makeSphere(radius, center, material)
        cont.resume(sphere)
    }.exceptionally { ex ->
        cont.resumeWithException(ex)
        null
    }
}

/**
 * Creates a small colored sphere and adds it to the scene at the given position.
 * colorInt should be an ARGB android.graphics.Color int (e.g., Color.RED).
 */
suspend fun addColoredSpherePoint(
    context: Context,
    sceneView: SceneView,
    position: Vector3,
    radiusMeters: Float = 0.01f,
    colorInt: Int,
    onTap: ((Node) -> Unit)? = null
): Node {
    val sphere = createColoredSphereRenderable(context, radiusMeters, colorInt)
    val node = Node().apply {
        renderable = sphere
        localPosition = position
        setOnTapListener { _, _ -> onTap?.invoke(this) }
    }
    sceneView.scene.addChild(node)
    Log.d(TAG, "Sphere added at $position (r=$radiusMeters)")
    return node
}


/** Load a GLB model from res/raw via RenderableSource. */
//suspend fun loadGlbFromRaw(
//    context: Context,
//    rawResId: Int
//): ModelRenderable = suspendCancellableCoroutine { cont ->
////    val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
////    ModelRenderable.builder()
////        .setSource(context, uri)
////        .build()
////        .thenAccept { renderable -> cont.resume(renderable) }
////        .exceptionally { ex ->
////            cont.resumeWithException(ex)
////            null
////        }
//    val entryName = context.resources.getResourceEntryName(R.raw.sample_model)
//    val uri = "android.resource://${context.packageName}/raw/$entryName".toUri()
//
//    ModelRenderable.builder()
//        .setSource(this, uri)   // no RenderableSource
//        .build()
//        .thenAccept { renderable -> cont.resume(renderable) }
//        .exceptionally { ex ->
//            cont.resumeWithException(ex)
//            null
//        }
//
////    ModelRenderable.builder()
////        .setSource(context, source)
////        .setRegistryId(uri)
////        .build()
////        .thenAccept { renderable -> cont.resume(renderable) }
////        .exceptionally { ex ->
////            cont.resumeWithException(ex)
////            null
////        }
//
//}

/* ------------ Internals (suspend helpers) ------------ */

/** Loads a GLB from res/raw using the simple builder API. */
private suspend fun loadModelRenderableFromRaw(
    context: Context,
    @RawRes rawResId: Int
): ModelRenderable = suspendCancellableCoroutine { cont ->
    // Build android.resource://<pkg>/raw/<entryName>
    val entryName = context.resources.getResourceEntryName(rawResId)
    val uri = Uri.parse("android.resource://${context.packageName}/raw/$entryName")

    ModelRenderable.builder()
        .setSource(context, uri) // simple overload, works for GLB in raw/
        .build()
        .thenAccept { renderable ->
            cont.resume(renderable)
        }
        .exceptionally { ex ->
            Log.e(TAG, "Model load failed (raw=$entryName): ${ex.message}", ex)
            cont.resumeWithException(ex)
            null
        }
}

/** Creates a Sceneform Renderable sphere with a solid color. */
private suspend fun createColoredSphereRenderable(
    context: Context,
    radiusMeters: Float,
    colorInt: Int
): Renderable = suspendCancellableCoroutine { cont ->
    // Use Sceneform's Color, constructed from Android ARGB int
    val sfColor = Color(colorInt)

    MaterialFactory.makeOpaqueWithColor(context, sfColor)
        .thenAccept { material ->
            val center = Vector3(0f, 0f, 0f)
            val sphere = ShapeFactory.makeSphere(radiusMeters, center, material)
            cont.resume(sphere)
        }
        .exceptionally { ex ->
            Log.e(TAG, "Sphere material failed: ${ex.message}", ex)
            cont.resumeWithException(ex)
            null
        }
}

/** Helper to add a Renderable at a given position with a tap listener. */
fun addRenderableNode(
    sceneView: SceneView,
    renderable: Renderable,
    position: Vector3,
    onTap: ((Node) -> Unit)? = null
): Node {
    val node = Node().apply {
        this.renderable = renderable
        localPosition = position
        setOnTapListener { _, _ -> onTap?.invoke(this) }
    }
    sceneView.scene.addChild(node)
    return node
}