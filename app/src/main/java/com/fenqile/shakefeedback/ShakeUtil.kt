package com.fenqile.shakefeedback

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Gravity
import android.widget.FrameLayout
import kotlin.math.abs

class ShakeUtil(private val activity: Activity) {
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null


    fun registerSensorManager(listener:  ()->Unit) {
        //获取 SensorManager 负责管理传感器
        if (mSensorEventListener == null) {
            mSensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val sensorType = event.sensor.type
                    val values = event.values
                    if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                        if (abs(values[0]) > 17 || abs(values[1]) > 17 || abs(values[2]) > 17) {
                            listener.invoke()
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }
        }
        mSensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if (mSensorManager != null) {
            //获取加速度传感器
            val mAccelerometerSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (mAccelerometerSensor != null) {
                mSensorManager!!.registerListener(mSensorEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun unregisterSensorManager() {
        if (mSensorManager != null) {
            mSensorManager!!.unregisterListener(mSensorEventListener)
        }
    }

}