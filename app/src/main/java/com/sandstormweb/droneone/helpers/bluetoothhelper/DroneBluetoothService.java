package com.sandstormweb.droneone.helpers.bluetoothhelper;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.sandstormweb.droneone.R;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

public class DroneBluetoothService
{
//    BLUETOOTH STUFF
    private BluetoothAdapter bluetoothAdapter;
    private final UUID THE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket bluetoothSocket;
    private onDataReceiveListener onDataReceiveListener;

//    OTHER VARIABLES
    private final String TAG = "myData";
    private Context context;
    private Dialog dialog;
    private Stack<String> datas;

//    DRONE PROTOCOL VARIABLES
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
    private class Receiver extends Thread{
        private InputStream inputStream;
        private onDataReceiveListener onDataReceiveListener;

        public Receiver(InputStream inputStream, onDataReceiveListener onDataReceiveListener)
        {
            this.inputStream = inputStream;
            this.onDataReceiveListener = onDataReceiveListener;
        }

        @Override
        public void run() {
            try{
                while(bluetoothSocket.isConnected()) {
                    String data = readFromBluetoothWithStopSign(inputStream);
                    if (data != null) {
                        if (onDataReceiveListener != null)
                            onDataReceiveListener.onDataReceive(data);
                    } else {
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private Receiver receiver;
    private class Transmitter extends Thread{
        private OutputStream outputStream;

        public Transmitter(OutputStream outputStream)
        {
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try{
                while(bluetoothSocket.isConnected()) {
                    if(datas.size() == 0){
                        Thread.sleep(5);
                    }else{
                        writeToBluetoothWithStopSign(outputStream, datas.get(0));
                        datas.removeElementAt(0);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private Transmitter transmitter;

    /**
     *
     * @param context must be activity.this
     */
    public DroneBluetoothService(Context context)
    {
        this.context = context;
        this.datas = new Stack<>();
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
                receiver = new Receiver(bluetoothSocket.getInputStream(), onDataReceiveListener);
                receiver.start();
                transmitter = new Transmitter(bluetoothSocket.getOutputStream());
                transmitter.start();
                Toast.makeText(context, "connection established", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "problem connecting to bluetooth device", Toast.LENGTH_LONG).show();
        }
    }

    private void disconnect()
    {
        try{
            bluetoothSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setOnReceiveListener(onDataReceiveListener onReceiveListener)
    {
        try {
            this.onDataReceiveListener = onReceiveListener;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendRouterPwmCommand(String router, int value)
    {
        try{
            String command;

            if(value > 0){
                command = COMMAND_INCREASE_PWM + DIVIDER + router + DIVIDER + Integer.toString(value);
            }else{
                command = COMMAND_DECREASE_PWM + DIVIDER + router + DIVIDER + Integer.toString(-1*value);
            }

            writeToBluetooth(bluetoothSocket.getOutputStream(), command);
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

    private static void writeToBluetooth(OutputStream outputStream, String data)
    {
        try {
            byte[] temp = data.getBytes();
            if (temp.length <= Byte.MAX_VALUE) {
                outputStream.write(temp.length);
                outputStream.write(temp);
            } else {
                throw new Exception("data length is bigger than possible length");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String readFromBluetooth(InputStream inputStream)
    {
        try{
            int length = inputStream.read();
            if(length != -1) {
                byte[] buffer = new byte[length];
                for (int i = 0; i < buffer.length; i++) {
                    inputStream.read(buffer, 0, length);
                }

                return new String(buffer);
            }

            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static void writeToBluetoothWithStopSign(OutputStream outputStream, String data)
    {
        try {
            outputStream.write((data+"#").getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String readFromBluetoothWithStopSign(InputStream inputStream)
    {
        try{
            int read = 0; byte[] buffer = new byte[1024]; int i = 0;
            while((read = inputStream.read()) != '#')
            {
                buffer[i] = (byte)read;
            }

            return new String(buffer);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void sendData(String data)
    {
        try{
            this.datas.add(data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
