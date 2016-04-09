package com.sandstormweb.droneone.helpers.conectionhelper;

import java.net.Socket;

public class ServerConnection extends Connection {
//    variables
    private String type, recipientPassword;
    private Recipient recipient;

//    classes
    public class Recipient{
        private String recipientPassword;
        private long recipientId;

        public Recipient(long recipientId, String recipientPassword)
        {
            this.recipientPassword = recipientPassword;
            this.recipientId = recipientId;
        }

        public String getRecipientPassword() {
            return recipientPassword;
        }

        public long getRecipientId() {
            return recipientId;
        }
    }

    public ServerConnection(String type, String username, String password, String server, int port, String recipientPassword) {
        super(server, port, username, password);
        this.type = type;
        this.recipientPassword = recipientPassword;
    }

    @Override
    public boolean login(Socket socket, String username, String password) {
        try {
            System.out.println("socket : " + super.isConnectionAlive());

                System.out.println("socket : start reading..."+super.getInputStream() == null);
                String read = ConnectionStatics.readFromInputStream(super.getInputStream());
                System.out.println("socket : "+read);
            if (read.equals(Connection.COMMAND_INTRODUCTION)) {
                ConnectionStatics.writeToOutputStream(super.getOutputStream(), Connection.REQUEST_INTRODUCTION + "&username=" + username + "&password=" + password + "&recipientPassword=" + this.recipientPassword + "&type=" + this.type);

                String answer;
                if (!(answer = ConnectionStatics.readFromInputStream(super.getInputStream())).equals(Connection.RESULT_LOGIN_SUCCESSFUL)) {
                    dispatchOnLoginFailed("server at " + super.getServer() + " says ==> " + answer);
                    return false;
                }else{
                    dispatchOnConnectionEstablished(socket);
                    return true;
                }
            } else {
                dispatchOnOperationFailed("server at " + super.getServer() + " is not asking for INTRODUCTION");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void sendData(String data)
    {
        try {
            if(this.recipient != null) {
//                String temp = Connection.REQUEST_SEND_DATA + "&data=" + data + "&recipientId=" + Long.toString(this.recipient.getRecipientId()) + "&recipientPassword=" + this.recipient.getRecipientPassword();
                String temp = Connection.DATA_DATA+ "&" + data;

                super.addDataStack(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void sendListRequest()
    {
        try {
            String temp = Connection.REQUEST_DRONE_LIST;
            super.addDataStack(temp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public synchronized void setRecipient(long recipientId, String recipientPassword) {
        try {
            this.recipient = new Recipient(recipientId, recipientPassword);

            super.addDataStack(Connection.REQUEST_SET_RECIPIENT+"&recipientId="+recipientId+"&recipientPassword="+recipientPassword);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void requestDirectConnection()
    {
        try{
            super.addDataStack(Connection.REQUEST_DIRECT_TO_RECIPIENT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
