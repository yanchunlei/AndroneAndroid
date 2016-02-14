package com.sandstormweb.droneone.ui;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;
import com.sandstormweb.droneone.R;
import com.sandstormweb.droneone.service.DroneBluetoothService;

public class MainActivity extends AppCompatActivity
{
    private Joystick heightRotate, move;
    private Button autoMove;
    private ImageView bluetooth;
    private TextView angles;
    private boolean isFirstAngle = true;
    private double initRoll, initLoop;
    private double roll, loop;
    private double[] gravity = {0, 0, 0};
    private int fiveToGravity = 0;

    private final double COEFFICIENT = 0.8;

    private DroneBluetoothService bluetoothService;

    private double test;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                updateGravity(event.values[0], event.values[1], event.values[2]);
                updateRollAndLoopLPS();
                showAnglesAverage(15);
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

            initializeBluetooth();

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
                    startBluetoothConnection();
                }
            });
            heightRotate.setJoystickListener(new JoystickListener() {
                @Override
                public void onDown() {

                }

                @Override
                public void onDrag(float v, float v1) {
                    if (v > 0 && v < 90) {
                        if (bluetoothService.isConnected()) {
                            bluetoothService.writeString("1" + Integer.toString((int) (v1 * 255)) + "#");
                            angles.setText("1" + Integer.toString((int) (v1 * 255)) + "#");
                        }
                    } else if (v > 90 && v < 180) {
                        if (bluetoothService.isConnected()) {
                            bluetoothService.writeString("2" + Integer.toString((int) (v1 * 255)) + "#");
                            angles.setText("2" + Integer.toString((int) (v1 * 255)) + "#");
                        }
                    } else if (v < 0 && v > -90) {
                        if (bluetoothService.isConnected()) {
                            bluetoothService.writeString("4" + Integer.toString((int) (v1 * 255)) + "#");
                            angles.setText("4" + Integer.toString((int) (v1 * 255)) + "#");
                        }
                    } else if (v < -90 && v > -180) {
                        if (bluetoothService.isConnected()) {
                            bluetoothService.writeString("3" + Integer.toString((int) (v1 * 255)) + "#");
                            angles.setText("3" + Integer.toString((int) (v1 * 255)) + "#");
                        }
                    }
                }

                @Override
                public void onUp() {
                    try {
                        if (bluetoothService.isConnected()) bluetoothService.writeString("a");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeBluetooth()
    {
        try{
            bluetoothService = new DroneBluetoothService(MainActivity.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startBluetoothConnection()
    {
        try{
            if(bluetoothService.initializeBluetooth()) {
                bluetoothService.connectToDrone();
            }
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
                sm.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            }else{
                isFirstAngle = true;
                fiveToGravity = 0;
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

    private void updateRollAndLoop(SensorEvent event){
        try{
            if (isFirstAngle) {
                initRoll = Math.toDegrees(Math.atan((event.values[1]) / (event.values[2])));
                initLoop = -1 * Math.toDegrees(Math.atan((event.values[0]) / (event.values[2])));
                isFirstAngle = false;
            } else {
                loop = -1 * Math.toDegrees(Math.atan((event.values[0]) / (event.values[2]))) - initLoop;
                roll = Math.toDegrees(Math.atan((event.values[1]) / (event.values[2]))) - initRoll;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private double tmpLoop, tmpRoll, counter;
    private void showAnglesAverage(int countToShow){
        try{
            if(counter == countToShow-1) {
                sendCommands(tmpLoop/16, 0, tmpRoll/countToShow, 0);
                counter = 0;
                tmpRoll = 0;
                tmpLoop = 0;
            }else{
                tmpLoop += this.loop;
                tmpRoll += this.roll;
                counter++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateGravity(double gx, double gy, double gz)
    {
        try{
            gravity[0] = COEFFICIENT * gravity[0] + (1-COEFFICIENT) * gx;
            gravity[1] = COEFFICIENT * gravity[1] + (1-COEFFICIENT) * gy;
            gravity[2] = COEFFICIENT * gravity[2] + (1-COEFFICIENT) * gz;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateRollAndLoopLPS()
    {
        try{
            try{
                if (fiveToGravity < 5) {
                    initRoll = Math.toDegrees(Math.atan((gravity[1]) / (gravity[2])));
                    initLoop = -1 * Math.toDegrees(Math.atan((gravity[0]) / (gravity[2])));
                    fiveToGravity++;
                } else {
                    loop = -1 * Math.toDegrees(Math.atan((gravity[0]) / (gravity[2]))) - initLoop;
                    roll = Math.toDegrees(Math.atan((gravity[1]) / (gravity[2]))) - initRoll;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showAngle()
    {
        try{
            sendCommands(this.loop, 0, this.roll, 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
