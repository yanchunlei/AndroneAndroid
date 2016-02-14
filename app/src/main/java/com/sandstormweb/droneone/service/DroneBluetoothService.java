package com.sandstormweb.droneone.service;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.sandstormweb.droneone.R;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DroneBluetoothService
{
//    BLUETOOTH STUFF
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothProfile bluetoothProfile;
    private final UUID THE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private InputStream inputStream;
    private OutputStream outputStream;
    private BluetoothSocket bluetoothSocket;

//    OTHER VARIABLES
    private final String TAG = "myData";
    private Context context;
    private Dialog dialog;

//    DRONE PROTOCOL VARIABLES
    private final String END_SIGN = "#";
    private final String ROUTER_FRONT_LEFT = "rfl";
    private final String ROUTER_FRONT_RIGHT = "rfr";
    private final String ROUTER_REAR_LEFT = "rrl";
    private final String ROUTER_REAR_RIGHT = "rrr";
    private final String DIVIDER = "_";
    private final String COMMAND_GET_HEIGHT = "cgh";
    private final String COMMAND_INCREASE_PWM = "cip";
    private final String COMMAND_DECREASE_PWM = "cdp";

//    LISTENERS
    public interface onDataReceiveListener{
        public void onDataReceive(String data);
    }
    private class Waiter extends Thread{
        private InputStream inputStream;
        private onDataReceiveListener onDataReceiveListener;
        private boolean goOn = true;

        public Waiter(InputStream inputStream, onDataReceiveListener onDataReceiveListener)
        {
            this.inputStream = inputStream;
            this.onDataReceiveListener = onDataReceiveListener;
        }

        @Override
        public void run() {
            try{
                while(goOn)
                {
                    int read = 0; byte[] data = new byte[1000];
                    while((read = inputStream.read(data)) != -1){
                        onDataReceiveListener.onDataReceive(new String(data, 0, read));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void terminateThread()
        {
            try{
                this.onDataReceiveListener = null;
                this.goOn = false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private Waiter waiter;

    /**
     *
     * @param context must be activity.this
     */
    public DroneBluetoothService(Context context)
    {
        this.context = context;
    }

    public boolean initializeBluetooth()
    {
        try{
//            GET BLUETOTH ADAPTER
            if(bluetoothAdapter == null) bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter == null){
                Log.d(TAG, "this device does not support bluetooth");
                return false;
            }

//            MAKE ADAPTER ENABLE
            if(!bluetoothAdapter.isEnabled()){
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                return false;
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void connectToDrone()
    {
        try{
            Set<BluetoothDevice> connectedDevices = bluetoothAdapter.getBondedDevices();

//            INITIALIZE DIALOG
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bluetooth_connect_dialog);

//            INITIALIZE_DIALOG COMPONENTS
            ListView listView = (ListView)dialog.findViewById(R.id.bluetooth_dialog_list);
            Button pair = (Button)dialog.findViewById(R.id.bluetooth_pair);

            Iterator<BluetoothDevice> interator = connectedDevices.iterator();
            String[] names = new String[connectedDevices.size()];
            for(int i = 0; interator.hasNext(); i++){
                names[i] = interator.next().getName();
            }
            BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(context, names);
            listView.setAdapter(bluetoothListAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    CONNECT HERE
                    connectToDevice(position);
                    dialog.dismiss();
                }
            });

            pair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        goToBluetoothSettings();
                        dialog.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void goToBluetoothSettings()
    {
        try{
            Intent i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void connectToDevice(int index)
    {
        try{
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            Iterator<BluetoothDevice> iterator = devices.iterator();

            for(int i = 0; i < index && iterator.hasNext(); i++){
                iterator.next();
            }

            BluetoothDevice bluetoothDevice = iterator.next();

//            TOASTING NAME
            Toast.makeText(context, "connecting to "+bluetoothDevice.getName()+"...wait", Toast.LENGTH_LONG).show();

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(THE_UUID);
            bluetoothSocket.connect();


            Thread.sleep(1000);

            if(!bluetoothSocket.isConnected()){
                Toast.makeText(context, "selected device is not available", Toast.LENGTH_LONG).show();
            }else {
                this.inputStream = bluetoothSocket.getInputStream();
                this.outputStream = bluetoothSocket.getOutputStream();
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "problem connecting to bluetooth device", Toast.LENGTH_LONG).show();
        }
    }

    private void disconnect()
    {
        try{
            this.inputStream = null;
            this.outputStream = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeString(String data)
    {
        try{
            outputStream.write(data.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String readString()
    {
        try{
            byte[] data = new byte[1000];

            int length = inputStream.read(data);

            return new String(data, 0, length);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void setOnReceiveListener(onDataReceiveListener onReceiveListener)
    {
        try {
            waiter = new Waiter(this.inputStream, onReceiveListener);
            waiter.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removeOnReceiveListener()
    {
        try{
            waiter.terminateThread();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendRouterPwmCommand(String router, int value)
    {
        try{
            String command;

            if(value > 0){
                command = COMMAND_INCREASE_PWM + DIVIDER + router + DIVIDER + Integer.toString(value) + DIVIDER + END_SIGN;
            }else{
                command = COMMAND_DECREASE_PWM + DIVIDER + router + DIVIDER + Integer.toString(-1*value) + DIVIDER + END_SIGN;
            }

            writeString(command);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isConnected()
    {
        try{
            return bluetoothSocket.isConnected();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
