package com.sandstormweb.droneone.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;
import com.sandstormweb.droneone.R;
import com.sandstormweb.droneone.helpers.camerahelper.CameraHelper;
import com.sandstormweb.droneone.helpers.conectionhelper.Connection;
import com.sandstormweb.droneone.helpers.conectionhelper.LocalConnection;
import com.sandstormweb.droneone.helpers.conectionhelper.LocalServer;
import com.sandstormweb.droneone.helpers.conectionhelper.ServerConnection;
import com.sandstormweb.droneone.helpers.conectionhelper.ConnectionStatics;
import com.sandstormweb.droneone.helpers.bluetoothhelper.DroneBluetoothService;
import com.sandstormweb.droneone.statics.DBHelper;
import com.sandstormweb.droneone.datamodels.Drone;
import com.sandstormweb.droneone.helpers.wifihotspothelper.HotspotManager;

import java.net.Socket;

public class Controller extends AppCompatActivity
{
    private Joystick heightRotate, move;
    private Button autoMove;
    private ImageView droneConnect;
    private TextView angles, connectionStatus;
    private boolean isFirstAngle = true;
    private double initRoll, initLoop;
    private double roll, loop;
    private double[] gravity = {0, 0, 0};
    private int fiveToGravity = 0;
    private ServerConnection serverConnection;
    private LocalServer localServer;
    private LocalConnection localConnection;
    private ImageView imageview;
    private SurfaceView surface;
    private CameraHelper cameraHelper;
    private boolean imageRequest;

    private final double COEFFICIENT = 0.8;

    private DroneBluetoothService bluetoothService;

//    GARBAGE
    private double test;
    private DelayedCommandRepeater delayedCommandRepeater;

//    MASSIVE OBJECTS
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
    private class DelayedCommandRepeater extends Thread{
        private DroneBluetoothService droneBluetoothService;
        private String command;
        private int delay;
        private boolean running = true;

        public DroneBluetoothService getDroneBluetoothService() {
            return droneBluetoothService;
        }

        public void setDroneBluetoothService(DroneBluetoothService droneBluetoothService) {
            this.droneBluetoothService = droneBluetoothService;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public DelayedCommandRepeater(DroneBluetoothService droneBluetoothService, int delay, String command)
        {
            this.droneBluetoothService = droneBluetoothService;
            this.delay = delay;
            this.command = command;
        }

        public void terminate()
        {
            this.running = false;
        }

        public boolean isRunning()
        {
            return running;
        }

        @Override
        public void run() {
            try{
                while (running){
                    if(droneBluetoothService.isConnected()){
                        droneBluetoothService.sendData(command);

                        System.out.println("sent : "+command);
                    }

                    Thread.sleep(delay);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.controller_activity);
//            JUST for test, delete on release
//            DBHelper dbHelper = new DBHelper(getApplicationContext());
//            dbHelper.insertOrUpdateKeyValue(DBHelper.TABLE_INFO, new KeyValue("username", "hamid"));
//            dbHelper.insertOrUpdateKeyValue(DBHelper.TABLE_INFO, new KeyValue("password", "123456"));
//            dbHelper.insertOrUpdateKeyValue(DBHelper.TABLE_INFO, new KeyValue("server", "5.39.101.9"));
//            dbHelper.insertOrUpdateKeyValue(DBHelper.TABLE_INFO, new KeyValue("port", "18428"));
//            dbHelper.close();
            initializeComponents();

            initializeBluetooth();

            initializeAutoMove();

//            initializeMagnetometer();

            initializeImageView();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeComponents()
    {
        try{
            heightRotate = (Joystick)findViewById(R.id.controller_heightRotate);
            move = (Joystick)findViewById(R.id.controller_move);
            angles = (TextView)findViewById(R.id.controller_angle);
            autoMove = (Button)findViewById(R.id.controller_autoMove);
            droneConnect = (ImageView)findViewById(R.id.controller_bluetooth);
            connectionStatus = (TextView)findViewById(R.id.controller_connection_status);
            imageview = (ImageView)findViewById(R.id.controller_imageview);
            surface = (SurfaceView)findViewById(R.id.controller_surfaceview);

            heightRotate.setJoystickListener(new JoystickListener() {
                @Override
                public void onDown() {

                }

                @Override
                public void onDrag(float v, float v1) {
                    if (bluetoothService.isConnected()) bluetoothService.sendData("1"+Integer.toString((int)(v1 * 255)));
                }

                @Override
                public void onUp() {
                }
            });

            droneConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT == 18) {
                        initializeLocalConnection();
                    } else {
                        startBluetoothConnection();
                    }
                }
            });

            connectionStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT == 18) {
//                        initializeLocalConnection();
                    }else{
                        initializeLocalServerConnection();
                    }
                }
            });

            autoMove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initializeLocalServerConnection();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeBluetooth()
    {
        try{
            bluetoothService = new DroneBluetoothService(Controller.this);
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

            Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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

//            double v1 = Math.sqrt(Math.pow(loopAngle,2)+Math.pow(rollAngle, 2))/60;
//            if (loopAngle > 0 && rollAngle > 0) {
//                if (bluetoothService.isConnected()) {
//                    bluetoothService.writeString("1" + Integer.toString((int) (v1 * 255)) + "#");
//                    angles.setText("1" + Integer.toString((int) (v1 * 255)) + "#");
//                }
//            } else if (loopAngle > 0 && rollAngle < 0) {
//                if (bluetoothService.isConnected()) {
//                    bluetoothService.writeString("2" + Integer.toString((int) (v1 * 255)) + "#");
//                    angles.setText("2" + Integer.toString((int) (v1 * 255)) + "#");
//                }
//            } else if (loopAngle < 0 && rollAngle > 0) {
//                if (bluetoothService.isConnected()) {
//                    bluetoothService.writeString("4" + Integer.toString((int) (v1 * 255)) + "#");
//                    angles.setText("4" + Integer.toString((int) (v1 * 255)) + "#");
//                }
//            } else if (loopAngle < 0 && rollAngle < 0) {
//                if (bluetoothService.isConnected()) {
//                    bluetoothService.writeString("3" + Integer.toString((int) (v1 * 255)) + "#");
//                    angles.setText("3" + Integer.toString((int) (v1 * 255)) + "#");
//                }
//            }
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

    private void initializeMagnetometer()
    {
        try{
            SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

            Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    angles.setText(event.values[0] + "\n" + event.values[1] + "\n" + event.values[2]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeServerConnection()
    {
        try{
            new CameraHelper(surface, new CameraHelper.OnCameraInitialized() {
                @Override
                public void OnCameraInitialized(CameraHelper cameraHelper) {
                    System.out.println("camera initialized...");
                }
            });

            DBHelper dbHelper = new DBHelper(getApplicationContext());

            serverConnection = new ServerConnection(ServerConnection.TYPE_DRONE, dbHelper.getKeyValue(DBHelper.TABLE_INFO, "username").getValue(), dbHelper.getKeyValue(DBHelper.TABLE_INFO, "password").getValue(), dbHelper.getKeyValue(DBHelper.TABLE_INFO, "server").getValue(), Integer.parseInt(dbHelper.getKeyValue(DBHelper.TABLE_INFO, "port").getValue()), "134679");
            serverConnection.setOnConnectionEstablished(new Connection.OnConnectionEstablished() {
                @Override
                public void onConnectionEstablished(Socket socket) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectionStatus.setText("connected");
                            serverConnection.sendListRequest();
                            sendThemImage(serverConnection);
                        }
                    });
                }
            });
            serverConnection.setOnDataReceived(new Connection.OnDataReceived() {
                @Override
                public void onDataReceived(final String data) {
                    try {
                        System.out.println("whole data : "+data);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectionStatus.setText(data.split("&")[0]);
                            }
                        });
                        switch (data.split("&")[0]) {
                            case Connection.DATA_DRONE_LIST:
                                Drone[] drone = ConnectionStatics.convertDroneListResponseToDroneArray(data);
                                if(Build.VERSION.SDK_INT > 19) {
                                    serverConnection.setRecipient(drone[0].getId(), "134679");
                                }else{
                                    serverConnection.setRecipient(drone[1].getId(), "134679");
                                }
                                break;
                            case Connection.RESULT_SET_RECIPIENT_SUCCESS :
                                serverConnection.requestDirectConnection();
                            case Connection.DATA_DATA:
                                if (data.split("&")[1].equals("REQUEST_IMAGE")) {
                                    imageRequest = true;
                                } else {
                                    //                                    request new image
                                    serverConnection.sendData("REQUEST_IMAGE");

//                                    System.out.println("whole data : " + data.getBytes().length);
                                    byte[] temp = Base64.decode(data.split("&")[1].replace('{','='), 0);
                                    final Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
//                                    System.out.println("whole data : the bitmap status => " + (bitmap == null));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            imageview.setImageBitmap(bitmap);
                                        }
                                    });
                                }
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("whole data : ");
                    }
                }
            });
            serverConnection.setRecipient(1223456, "1215646");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeLocalServerConnection()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        HotspotManager.setHotspotEnabled(getApplicationContext(), true);

                        while (!HotspotManager.isHotspotEnable(getApplicationContext())) Thread.sleep(200);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectionStatus.setText("loc_ser ok");
                            }
                        });

                        HotspotManager.setHotspotSSID(getApplicationContext(), "hamid hotspot androne test");
                        HotspotManager.enableWPA2Auth(getApplicationContext(), "12345678");

                        localServer = new LocalServer(LocalServer.DEFAULT_PORT, new LocalServer.OnReceive() {
                            @Override
                            public void onReceive(String data) {
                                System.out.println("hamid data = "+data);
                                if(data.equals(Connection.L2_REQUEST_IMAGE)){
                                    imageRequest = true;
                                }
                            }
                        });
                        localServer.start();
                        sendThemImageLocal();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeLocalConnection()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(HotspotManager.isHotspotEnable(getApplicationContext())) {
                            localConnection = new LocalConnection(getApplicationContext(), LocalServer.DEFAULT_PORT, null, null);

                            while(!localConnection.isConnectionAlive()) Thread.sleep(200);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connectionStatus.setText("conn ok");
                                }
                            });

                            localConnection.setOnDataReceived(new Connection.OnDataReceived() {
                                @Override
                                public void onDataReceived(String data) {
                                    processReceivedData(data);
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void processReceivedData(String data)
    {
        try{
            String parts[] = data.split("&");
            switch (parts[0])
            {
                case Connection.DATA_DATA :
                    switch(parts[1])
                    {

                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    for test
    private void initializeImageView()
    {
        try{
            cameraHelper = new CameraHelper(this.surface, new CameraHelper.OnCameraInitialized() {
                @Override
                public void OnCameraInitialized(final CameraHelper cameraHelper) {
//                    Thread t = new Thread(new Runnable() {
//                        byte[] data;
//
//                        @Override
//                        public void run() {
//                            try {
//                                while (true) {
//                                    System.out.println("system is running");
//
//                                    if((data = cameraHelper.getPreview()) != null){
//                                        System.out.println("record : "+data.length);
//                                        final Bitmap bitmap = CameraHelper.getBitmapFromJpegByteArray(data);
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                imageview.setImageBitmap(bitmap);
//                                            }
//                                        });
//                                    }
//
//                                    Thread.sleep(5);
//                                }
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    t.start();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendThemImage(final ServerConnection serverConnection)
    {
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            while (!imageRequest) Thread.sleep(5);

                            imageRequest = false;

                            final byte[] preview = cameraHelper.getPreview();

                            if (preview != null) {
//
                                String prev = Base64.encodeToString(preview, 0);
//
                                prev = prev.replace('=', '{');

                                serverConnection.sendData(prev);

//                                System.out.println("record : data sent" + prev.getBytes().length);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendThemImageLocal()
    {
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            while (!imageRequest) Thread.sleep(5);

                            imageRequest = false;

                            final byte[] preview = cameraHelper.getPreview();

                            if (preview != null) {
                                String prev = Base64.encodeToString(preview, Base64.NO_WRAP);

                                localServer.sendData(prev);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
