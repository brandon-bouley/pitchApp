package com.example.pitchapp.ui.components
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt


class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {
    private var shakeThresholdGravity = 1.2f
    private var lastShakeTime = 0L
    private val shakeSlopTimeMs = 500

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gForce = sqrt((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
        if (gForce > shakeThresholdGravity) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime > shakeSlopTimeMs) {
                lastShakeTime = currentTime
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
