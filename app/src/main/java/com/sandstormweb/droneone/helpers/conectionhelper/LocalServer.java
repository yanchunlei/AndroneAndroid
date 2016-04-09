package com.sandstormweb.droneone.helpers.conectionhelper;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

public class LocalServer extends Thread
{
    private int port;
    private Socket socket;
    private boolean alive = true, intrrupted;
    private OnReceive onReceive;
    private Stack<String> datas;

//    statics
    public static final int DEFAULT_PORT = 18428;

//    interfaces
    public interface OnReceive
    {
        public void onReceive(String data);
    }

//    classes
    public class Receiver extends Thread{

    private Socket socket;
    private OnReceive onReceive;

    public Receiver(Socket socket, OnReceive onReceive)
    {
        this.socket = socket;
        this.onReceive = onReceive;
    }

    @Override
        public void run() {
            super.run();
            try{
                System.out.println("hamid data = receiver started");
                while (alive && !intrrupted) {
                    String data = ConnectionStatics.readFromInputStream(socket.getInputStream());

                    if(data == null){
                        intrrupted = true;
                        return;
                    }

                    onReceive.onReceive(data);
                }
            }catch (Exception e){
                e.printStackTrace();
                intrrupted = true;
            }
        }
    }

    public LocalServer(int port, OnReceive onReceive)
    {
        this.port = port;
        this.datas = new Stack<>();
        this.onReceive = onReceive;
    }

    public LocalServer(OnReceive onReceive) {
        this.port = DEFAULT_PORT;
        this.datas = new Stack<>();
        this.onReceive = onReceive;
    }

    @Override
    public void run() {
        super.run();
        try{
            while(alive) {
                intrrupted = false;

                ServerSocket serverSocket = new ServerSocket(port);

                socket = serverSocket.accept();
                serverSocket.close();

                System.out.println("message : local server has a new connection");

                new Receiver(socket, onReceive).start();

                startSendingData(socket);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendData(String data)
    {
        try {
            this.datas.add(data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startSendingData(Socket socket)
    {
        try{
            while(alive && !intrrupted) {
                if (datas.size() == 0) {
                    Thread.sleep(5);
                } else {
                    ConnectionStatics.writeToOutputStream(socket.getOutputStream(), datas.get(0));
                    datas.removeElementAt(0);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            intrrupted = true;
        }
    }
}
