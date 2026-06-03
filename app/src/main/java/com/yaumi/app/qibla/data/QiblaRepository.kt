package com.yaumi.app.qibla.data

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.yaumi.app.core.location.DeviceLocationResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

data class QiblaData(
    val lat: Double,
    val lon: Double,
    val locationName: String,
    val qiblaBearing: Double,
    val distanceKm: Double
)

data class CompassReading(
    val headingDegrees: Double,
    val accuracy: Int
)

class QiblaRepository(private val appContext: Context) {
    private val locationResolver = DeviceLocationResolver(appContext)

    @SuppressLint("MissingPermission")
    suspend fun getQiblaData(): QiblaData = withContext(Dispatchers.IO) {
        val resolved = locationResolver.resolve(defaultLat = -6.2088, defaultLon = 106.8456)
        val bearing = QiblaMath.bearingToKaaba(resolved.lat, resolved.lon)
        val distance = QiblaMath.distanceToKaabaKm(resolved.lat, resolved.lon)
        QiblaData(
            lat = resolved.lat,
            lon = resolved.lon,
            locationName = resolved.cityName,
            qiblaBearing = bearing,
            distanceKm = distance
        )
    }

    /**
     * Continuous heading stream from rotation-vector sensor (or accelerometer +
     * magnetic fallback). Emits on every sensor change so the compass UI
     * tracks device motion smoothly.
     */
    fun headingFlow(): Flow<CompassReading> = callbackFlow {
        val manager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVector = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetic = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationVector == null && (accel == null || magnetic == null)) {
            trySend(CompassReading(0.0, SensorManager.SENSOR_STATUS_UNRELIABLE))
            close()
            return@callbackFlow
        }

        var accelValues: FloatArray? = null
        var magneticValues: FloatArray? = null
        var lastAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val deg = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
                        trySend(CompassReading(deg, lastAccuracy))
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelValues = event.values.clone()
                        emitFromAccelMag(accelValues, magneticValues, lastAccuracy)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        magneticValues = event.values.clone()
                        emitFromAccelMag(accelValues, magneticValues, lastAccuracy)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                lastAccuracy = accuracy
            }

            private fun emitFromAccelMag(
                a: FloatArray?,
                m: FloatArray?,
                accuracy: Int
            ) {
                if (a == null || m == null) return
                val rotationMatrix = FloatArray(9)
                val ok = SensorManager.getRotationMatrix(rotationMatrix, FloatArray(9), a, m)
                if (!ok) return
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val deg = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
                trySend(CompassReading(deg, accuracy))
            }
        }

        if (rotationVector != null) {
            manager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_GAME)
        } else {
            manager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
            manager.registerListener(listener, magnetic, SensorManager.SENSOR_DELAY_GAME)
        }

        awaitClose { manager.unregisterListener(listener) }
    }.flowOn(Dispatchers.Main.immediate)
}
