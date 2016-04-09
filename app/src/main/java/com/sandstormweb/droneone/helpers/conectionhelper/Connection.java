package com.sandstormweb.droneone.helpers.conectionhelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Stack;

public abstract class Connection extends Thread
{
//    variables
    private Socket socket;
    private String server, username, password;
    private int port;
    private Stack<String> data;
    private OnConnectionEstablished onConnectionEstablished;
    private OnDataReceived onDataReceived;
    private OnLoginFailed onLoginFailed;
    private OnOperationFailed onOperationFailed;


//    interfaces
    public interface OnConnectionEstablished {
        public void onConnectionEstablished(Socket socket);
    }
    public interface OnDataReceived{
        public void onDataReceived(String data);
    }
    public interface OnLoginFailed{
        public void onLoginFailed(String message);
    }
    public interface OnOperationFailed{
        public void onOperationFailed(String message);
    }

//    static variables
    public static final String TYPE_DRONE = "TYPE_DRONE";
    public static final String TYPE_CONTROLLER = "TYPE_CONTROLLER";

//    static messages ----------------------------------------------------------------------------------
//    REQUEST ==> requests to server
    public static final String REQUEST_DRONE_LIST = "REQUEST_DRONES_LIST";
    public static final String REQUEST_INTRODUCTION = "REQUEST_INTRODUCTION";    //REQUEST_INTRODUCTION&username=[username]&password=[password]&type=[type]&recipientPassword=[recipientPassword]
    public static final String REQUEST_SEND_DATA = "REQUEST_SEND_DATA";   //REQUEST_SEND_DATA&recipientId=[recipientId]&recipientPassword=[recipientPassword]&data=[data]
    public static final String REQUEST_SET_RECIPIENT = "REQUEST_SET_RECIPIENT";
    public static final String RESULT_SET_RECIPIENT_SUCCESS = "RESULT_SET_RECIPIENT_SUCCESS";
    public static final String REQUEST_DATA_RECIPIENT = "REQUEST_DATA_RECIPIENT";
    public static final String REQUEST_DIRECT_TO_RECIPIENT = "REQUEST_DIRECT_TO_RECIPIENT";
//    L2_REQUEST ==> layer 2 request, requests to drone
    public static final String L2_REQUEST_IMAGE = "L2_REQUEST_IMAGE";
    public static final String L2_REQUEST_GPS = "L2_REQUEST_GPS";
    public static final String L2_REQUEST_HEIGHT = "L2_REQUEST_HEIGHT";
//    L2_COMMAND ==> layer 2 command, commands to drone
    public static final String L2_COMMAND_ANGLE_X = "L2_COMMAND_ANGLE_X";
    public static final String L2_COMMAND_ANGLE_Y = "L2_COMMAND_ANGLE_Y";
    public static final String L2_COMMAND_TPWM = "L2_COMMAND_TPWM";
//    DATA ==> data indicator
    public static final String DATA_DRONE_LIST = "DATA_DRONE_LIST";
    public static final String DATA_DATA = "DATA_DATA";
//    DATATYPE ==> data type indicator
    public static final String DATA_TYPE_IMAGE = "DATA_TYPE_IMAGE";
//    COMMAND ==> commands (requests) from server
    public static final String COMMAND_INTRODUCTION = "COMMAND_INTRODUCTION";
//    RESULT ==> result of operations both from server or drone or for local usages
    public static final String RESULT_LOGIN_SUCCESSFUL = "RESULT_LOGIN_SUCCESSFUL";
    public static final String RESULT_LOGIN_FAILED = "RESULT_LOGIN_FAILED";
    public static final String RESULT_ERROR = "RESULT_ERROR";
    public static final String RESULT_EMPTY = "RESULT_EMPTY";
    public static final String RESULT_SUCCESS = "RESULT_SUCCESS";
    public static final String RESULT_SET_RECIPIENT_FAILED = "RESULT_SET_RECIPIENT_FAILED";
//    -----------------------------------------------------------------------------------------------------


//    classes
    private class Receiver extends Thread{
    private Socket socket;

    public Receiver(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        try{
            while(socket.isConnected()){
                String read = ConnectionStatics.readFromInputStream(socket.getInputStream());

                onDataReceived.onDataReceived(read);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

    public Connection(String server, int port, String username, String password)
    {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.data = new Stack();

        try {
            this.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try{
            socket = new Socket(server, port);

            boolean access = false;

            if(socket.isConnected()) access = login(socket, this.username, this.password);
            if(access){
//                start receiver
                Receiver receiver = new Receiver(socket);
                receiver.start();

//                start transmitter
                startMessagingLoop(this.data);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected abstract boolean login(Socket socket, String username, String password);

    private void startMessagingLoop(Stack<String> data)
    {
        try {
            while (socket.isConnected()) {
                if (data.size() != 0) {
                    ConnectionStatics.writeToOutputStream(socket.getOutputStream(), data.get(0));

//                    test remove at release
                    System.out.println("whole data : sent this ==>"+data.get(0));

                    data.removeElementAt(0);
                } else {
                    Thread.sleep(5);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected Socket getSocket()
    {
        return this.socket;
    }

    protected InputStream getInputStream()
    {
        try{
            if(socket.isConnected()) return this.socket.getInputStream();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected OutputStream getOutputStream()
    {
        try{
            if(socket.isConnected()) return this.socket.getOutputStream();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage(String message)
    {
        try {
            this.data.push(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void addDataStack(String message)
    {
        try {
            this.data.push(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close()
    {
        try{
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void dispatchOnConnectionEstablished(Socket socket) throws Exception
    {
        try {
            if(this.onConnectionEstablished != null) this.onConnectionEstablished.onConnectionEstablished(socket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void dispatchOnDataReceived(String data)
    {
        try {
            if(this.onDataReceived != null) this.onDataReceived.onDataReceived(data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void dispatchOnLoginFailed(String message) throws Exception
    {
        if (this.onLoginFailed != null) this.onLoginFailed.onLoginFailed(message);

        throw new Exception("LOGIN FAILED ==> "+message);
    }

    protected void dispatchOnOperationFailed(String message)  throws Exception
    {
        if(this.onOperationFailed != null) this.onOperationFailed.onOperationFailed(message);

        throw new Exception("OPERATION FAILED ==> "+message);
    }

    public void removeOnConnectionEstablished()
    {
        this.onConnectionEstablished = null;
    }

    public void removeOnDataReceived()
    {
        this.onDataReceived = null;
    }

    public void removeOnLoginFailed()
    {
        this.onLoginFailed = null;
    }

    public void removeOnOperationFailed()
    {
        this.onOperationFailed = null;
    }

    public void setOnConnectionEstablished(OnConnectionEstablished onConnectionEstablished) {
        this.onConnectionEstablished = onConnectionEstablished;
    }

    public void setOnDataReceived(OnDataReceived onDataReceived) {
        this.onDataReceived = onDataReceived;
    }

    public void setOnLoginFailed(OnLoginFailed onLoginFailed) {
        this.onLoginFailed = onLoginFailed;
    }

    public void setOnOperationFailed(OnOperationFailed onOperationFailed) {
        this.onOperationFailed = onOperationFailed;
    }

    public boolean isConnectionAlive()
    {
        try{
            return this.socket.isConnected();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public String getServer() {
        return server;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public OnConnectionEstablished getOnConnectionEstablished() {
        return onConnectionEstablished;
    }

    public OnDataReceived getOnDataReceived() {
        return onDataReceived;
    }

    public OnLoginFailed getOnLoginFailed() {
        return onLoginFailed;
    }

    public OnOperationFailed getOnOperationFailed() {
        return onOperationFailed;
    }
}
