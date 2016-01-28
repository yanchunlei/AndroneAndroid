package com.sandstormweb.droneone.ui;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.sandstormweb.droneone.R;

public class MainActivity extends AppCompatActivity
{
    private Joystick heightRotate, move;
    private Button autoMove;
    private ImageView bluetooth;
    private TextView angles;
    private boolean isFirstAngle = true;
    private double initRoll, initLoop;

    private double test;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                if (isFirstAngle) {
                    initRoll = Math.toDegrees(Math.atan((event.values[1]) / (event.values[2])));
                    initLoop = -1 * Math.toDegrees(Math.atan((event.values[0]) / (event.values[2])));
                    isFirstAngle = false;
                } else {
                    double loopAngle = -1 * Math.toDegrees(Math.atan((event.values[0]) / (event.values[2]))) - initLoop;
                    double rollAngle = Math.toDegrees(Math.atan((event.values[1]) / (event.values[2]))) - initRoll;
                    sendCommands(loopAngle, 0, rollAngle, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            initializeComponents();

            initializeAutoMove();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            heightRotate = (Joystick)findViewById(R.id.main_heightRotate);
            move = (Joystick)findViewById(R.id.main_move);
            angles = (TextView)findViewById(R.id.main_angle);
            autoMove = (Button)findViewById(R.id.main_autoMove);
            bluetooth = (ImageView)findViewById(R.id.main_bluetooth);

            bluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToBluetoothSettings();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeAutoMove()
    {
        try{
            autoMove.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            enableAutoMove(true);
                            break;
                        case MotionEvent.ACTION_UP:
                            enableAutoMove(false);
                            break;
                    }
                    return false;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void enableAutoMove(boolean autoMove)
    {
        try{
            move.setEnabled(!autoMove);

            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);

            Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if(autoMove) {
                sm.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }else{
                isFirstAngle = true;
                sm.unregisterListener(sensorEventListener, accelerometer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendCommands(double loopAngle, double yawAngle, double rollAngle, double height)
    {
        try{
            angles.setText("loop : " + Double.toString(loopAngle) + "\n" + "rollAngle : " + Double.toString(rollAngle));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private double angleShift()
    {
        try{
            return -1;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private void goToBluetoothSettings()
    {
        try{
            Intent i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
