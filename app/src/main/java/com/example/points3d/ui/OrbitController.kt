package com.example.points3d.ui

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import kotlin.math.*

class OrbitPanController(
    private val sceneView: SceneView,
    private var target: Position = Position(0f, 0f, 0f),
    startAzimuthDeg: Float = 0f,
    startElevationDeg: Float = 20f,
    startRadius: Float = 1.8f,
    private val onTapSelect: ((MotionEvent) -> Unit)? = null // delegate selection to Activity
) {
    enum class Mode { ORBIT, PAN, SELECT }

    //    var mode: Mode = Mode.ORBIT
    var mode: Mode = Mode.ORBIT
        set(value) {
            field = value
            if (value == Mode.SELECT) dragging = false
        }
    private var azimuth = startAzimuthDeg
    private var elevation = startElevationDeg
    private var radius = startRadius

    // Tuning
    var rotateSpeed = 0.25f       // deg per pixel
    var panSpeed = 0.0018f        // meters per pixel (scale with radius below)
    var zoomResponse = 0.6f       // 0.4 = smoother, 0.8 = snappier
    var minRadius = 0.1f
    var maxRadius = 200f
    var minElevation = -85f
    var maxElevation = 85f

    // state
    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false
    private var movedPx = 0f
    private val tapSlopPx = 12f

    private val scaleDetector = ScaleGestureDetector(
        sceneView.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val sf = detector.scaleFactor.coerceIn(0.01f, 100f)
                val damp = sf.toDouble().pow(zoomResponse.toDouble()).toFloat()
                radius = (radius / damp).coerceIn(minRadius, maxRadius)
                updateCamera()
                return true
            }
        }
    )

    private val tapDetector = GestureDetector(
        sceneView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // we’ll only treat as a tap if user didn’t drag a lot
                if (movedPx <= tapSlopPx) onTapSelect?.invoke(e)
                movedPx = 0f
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                // reset view
                azimuth = 0f
                elevation = 20f
                radius = startRadius.coerceIn(minRadius, maxRadius)
                updateCamera()
                return true
            }
        }
    )

    fun attach() {
        sceneView.setOnTouchListener { _, ev ->
            // In SELECT mode, don’t intercept; let SceneView’s onTouchEvent handle taps/hit tests
            if (mode == Mode.SELECT) return@setOnTouchListener false
            var consumed = false
            consumed = scaleDetector.onTouchEvent(ev) || consumed
            consumed = tapDetector.onTouchEvent(ev) || consumed

            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragging = true
                    movedPx = 0f
                    lastX = ev.x
                    lastY = ev.y
                    consumed = true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.x - lastX
                    val dy = ev.y - lastY
                    movedPx += hypot(dx, dy)

                    if (dragging && !scaleDetector.isInProgress) {
//                        val dx = ev.x - lastX
//                        val dy = ev.y - lastY
                        when (mode) {
                            Mode.ORBIT -> {
                                azimuth -= dx * rotateSpeed
                                elevation += dy * rotateSpeed
                                elevation = elevation.coerceIn(minElevation, maxElevation)
                                updateCamera()
                                consumed = true
                            }

                            Mode.PAN -> {
                                // improved FOV‑based panning
                                val (right, up) = cameraBasis()
                                val k = panSpeed * radius

                                val moveRight = -dx * k
                                val moveUp = -dy * k
                                target = Position(
                                    target.x + right.x * moveRight + up.x * moveUp,
                                    target.y + right.y * moveRight + up.y * moveUp,
                                    target.z + right.z * moveRight + up.z * moveUp
                                )
                                updateCamera()
                                consumed = true
                            }

                            Mode.SELECT -> {
                                // nothing to do here – SELECT mode leaves camera untouched
                                // (touches are handled elsewhere / passed through)
                                consumed = false
                            }
                        }
                        lastX = ev.x
                        lastY = ev.y
                        updateCamera()
                        consumed = true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragging = false
                    consumed = true
                }
            }
            consumed
        }
        updateCamera()
    }

    fun setTarget(newTarget: Position) {
        target = newTarget
        updateCamera()
    }

//    fun setMode(newMode: Mode) {
//        mode = newMode
//    }

    private fun updateCamera() {
        val a = Math.toRadians(azimuth.toDouble())
        val e = Math.toRadians(elevation.toDouble())

        val x = (radius * cos(e) * sin(a)).toFloat()
        val y = (radius * sin(e)).toFloat()
        val z = (radius * cos(e) * cos(a)).toFloat()

        val cam = sceneView.cameraNode
        cam.position = Position(target.x + x, target.y + y, target.z + z)

        try {
            cam.lookAt(target)
        } catch (_: Throwable) {
            cam.rotation = Rotation(elevation, -azimuth, 0f)
        }
    }

    /** Camera basis vectors given azimuth/elevation. */
    private fun cameraBasis(): Pair<Position, Position> {
        val a = Math.toRadians(azimuth.toDouble())
        val e = Math.toRadians(elevation.toDouble())

        // forward (from camera to target)
        val fx = (-cos(e) * sin(a)).toFloat()
        val fy = (-sin(e)).toFloat()
        val fz = (-cos(e) * cos(a)).toFloat()

        // world up
        val ux = 0f
        val uy = 1f
        val uz = 0f

        // right = normalize(cross(forward, up))
        var rx = fy * uz - fz * uy
        var ry = fz * ux - fx * uz
        var rz = fx * uy - fy * ux
        val rlen = sqrt(rx * rx + ry * ry + rz * rz).coerceAtLeast(1e-6f)
        rx /= rlen; ry /= rlen; rz /= rlen

        // real up = cross(right, forward)
        val upx = ry * fz - rz * fy
        val upy = rz * fx - rx * fz
        val upz = rx * fy - ry * fx

        return Position(rx, ry, rz) to Position(upx, upy, upz)
    }
}
