package com.sandstormweb.droneone.ui;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.sandstormweb.droneone.R;

public class MainActivity extends AppCompatActivity
{
    private Joystick heightRotate, move;
    private Button autoMove;
    private TextView angles;
    private boolean isFirstAngle = true;
    private double initRoll, initLoop;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                if (isFirstAngle) {
                    initRoll = Math.toDegrees(Math.atan((event.values[2]) / (event.values[1])));
                    initLoop = Math.toDegrees(Math.atan((event.values[2]) / (event.values[0])));
                    isFirstAngle = false;
                } else {
                    double loopAngle = Math.toDegrees(Math.atan((event.values[2]) / (event.values[0]))) - initLoop;
                    double rollAngle = -1*Math.toDegrees(Math.atan((event.values[2]) / (event.values[1]))) - initRoll;
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
                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN :
                            enableAutoMove(true);
                            break;
                        case MotionEvent.ACTION_UP :
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
}
