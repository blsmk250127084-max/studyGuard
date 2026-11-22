package com.example.studyguard.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.sqrt

class SensorManagerHelper(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    /**
     * Flow emitting true when proximity sensor detects an object close to the screen.
     */
    val proximityFlow: Flow<Boolean> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }

        val proximitySensor = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (proximitySensor == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
                    val distance = event.values[0]
                    val maxRange = proximitySensor.maximumRange
                    // Proximity triggers if distance < maxRange (and < 5.0 cm for continuous sensors) or distance == 0.0
                    val isClose = (distance < maxRange && distance < 5.0f) || (distance == 0.0f) || (maxRange <= 1.0f && distance < 1.0f)
                    trySend(isClose)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        manager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST)
        awaitClose {
            try {
                manager.unregisterListener(listener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Flow emitting true when linear acceleration sensor detects movement/lifting of the phone.
     */
    val motionFlow: Flow<Boolean> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }

        val linearAccelSensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if (linearAccelSensor == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val isMoving = magnitude > 1.2f
                    trySend(isMoving)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        manager.registerListener(listener, linearAccelSensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose {
            try {
                manager.unregisterListener(listener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Flow emitting true when accelerometer detects phone is tilted or lifted away from lying flat on table.
     */
    val positionFlow: Flow<Boolean> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }

        val accelSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelSensor == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    // If phone is flat face up on table, z ~ 9.8, x ~ 0, y ~ 0.
                    // If tilted or held up: |x| > 3.0 or |y| > 3.0 or z < 7.5
                    val isTiltedOrHeld = abs(x) > 3.0f || abs(y) > 3.0f || z < 7.5f
                    trySend(isTiltedOrHeld)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        manager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose {
            try {
                manager.unregisterListener(listener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
