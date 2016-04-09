package com.sandstormweb.droneone.helpers.sensorhelpers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerHelper
{
    private Context context;
    private int sensorDelay;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private OnSensorEvent onSensorEvent;

//    statics
    public static final int SENSOR_DELAY_FASTEST = SensorManager.SENSOR_DELAY_FASTEST;
    public static final int SENSOR_DELAY_GAME = SensorManager.SENSOR_DELAY_GAME;
    public static final int SENSOR_DELAY_NORMAL = SensorManager.SENSOR_DELAY_NORMAL;
    public static final int SENSOR_DELAY_UI = SensorManager.SENSOR_DELAY_UI;

//    interfaces
    public interface OnSensorEvent
    {
        public void onSensorEvent(SensorEvent sensorEvent);
    }

    public AccelerometerHelper(Context context, int sensorDelay)
    {
        try{
            this.context = context;
            this.sensorDelay = sensorDelay;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start()
    {
        try{
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            this.sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if(onSensorEvent != null){
                        onSensorEvent.onSensorEvent(event);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            sensorManager.registerListener(sensorEventListener, accelerometer, this.sensorDelay);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop()
    {
        try {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.unregisterListener(sensorEventListener, accelerometer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
