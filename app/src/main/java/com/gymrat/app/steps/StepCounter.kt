package com.gymrat.app.steps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounter(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var listener: SensorEventListener? = null

    fun start(
        onSensorAvailability: (available: Boolean) -> Unit,
        onStepsSinceBoot: (steps: Long) -> Unit
    ): Boolean {
        val stepSensor = sensor ?: run {
            onSensorAvailability(false)
            return false
        }

        onSensorAvailability(true)

        val l = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val value = event.values.firstOrNull() ?: return
                onStepsSinceBoot(value.toLong())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        listener = l
        return sensorManager.registerListener(l, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        val l = listener ?: return
        sensorManager.unregisterListener(l)
        listener = null
    }
}

