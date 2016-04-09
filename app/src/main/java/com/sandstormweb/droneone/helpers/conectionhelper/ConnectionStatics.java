package com.sandstormweb.droneone.helpers.conectionhelper;

import com.sandstormweb.droneone.datamodels.Drone;
import com.sandstormweb.droneone.datamodels.KeyValueTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class ConnectionStatics
{
//    socket tools
    public static void writeToOutputStream(OutputStream outputStream, String message)
    {
    try{
        System.out.println("hamid data : "+message.getBytes().length);

        byte[] data = message.getBytes();
        outputStream.write(data.length);
        outputStream.write(data.length>>8);
        outputStream.write(data.length>>16);
        outputStream.write(data.length>>24);
        outputStream.write(data);
    }catch (Exception e){
        e.printStackTrace();
    }
}

    public static String readFromInputStream(InputStream inputStream)
    {
        try{
            int read = inputStream.read();
            read |= inputStream.read()<<8;
            read |= inputStream.read()<<16;
            read |= inputStream.read()<<24;

//            test
            System.out.println("whole data : read in read ==> "+read);

            byte[] buffer = new byte[read];
            for (int i = 0; i < read; i++) {
                buffer[i] = (byte) inputStream.read();
            }

            return new String(buffer, 0, read);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

//    message tools
    public static String executeMessages(String message, Socket socket, KeyValueTable data)
    {
        try{
            System.out.println("message received : "+message);

            String[] parts = message.split("&");

            switch(parts[0])
            {
                case Connection.DATA_DRONE_LIST :
                    return executeDataDroneList(parts);
                case Connection.COMMAND_INTRODUCTION :
                    return executeCommandIntroduction(socket, data);
                case Connection.RESULT_LOGIN_SUCCESSFUL :
                case Connection.RESULT_LOGIN_FAILED :
                    return executeLoginResult(parts);
                default:
                    return Connection.RESULT_EMPTY;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String executeDataDroneList(String[] parts)
    {
        return parts[1];
    }

    private static String executeCommandIntroduction(Socket socket, KeyValueTable data)
    {
        try{
            writeToOutputStream(socket.getOutputStream(), Connection.REQUEST_INTRODUCTION+"&username="+data.getValue("username")+"&type="+data.getValue("type")+"&password="+data.getValue("password")+"&recipientPassword="+data.getValue("recipientPassword"));

            return Connection.RESULT_SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
            return Connection.RESULT_ERROR;
        }
    }

    private static String executeLoginResult(String[] parts)
    {
        return parts[0];
    }

    public static Drone[] convertDroneListResponseToDroneArray(String droneListResponse)
    {
        try{
            droneListResponse = droneListResponse.substring(Connection.DATA_DRONE_LIST.length()+1, droneListResponse.length());

//            remove later
            System.out.println("data : "+droneListResponse);

            JSONObject jsonObject = new JSONObject(droneListResponse);
            JSONArray jsonArray = jsonObject.getJSONArray("DRONES");

            Drone[] drones = new Drone[jsonArray.length()];
            for(int i = 0; i < drones.length; i++)
            {
                JSONObject tmp = (JSONObject)jsonArray.get(i);
                Drone temp = new Drone(tmp.getString("username"), tmp.getLong("id"));

                drones[i] = temp;
            }

            return drones;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String produceRandomRecipientPassword()
    {
        try{
            String password = "";
            Random random = new Random();

            for(int i = 0; i < 8; i++)
            {
                password += random.nextInt(74)+48;
            }

            return password;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
