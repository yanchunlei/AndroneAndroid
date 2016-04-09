package com.sandstormweb.droneone.datamodels;

import java.util.ArrayList;

public class KeyValueTable
{
    private ArrayList<String[]> data;

    public KeyValueTable()
    {
        this.data = new ArrayList();
    }

    public String getValue(String key)
    {
        try{
            for(int i = 0; i < data.size(); i++)
            {
                if(data.get(i)[0].equals(key)) return this.data.get(i)[1];
            }

            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public int getValueInt(String key)
    {
        try{
            for(int i = 0; i < data.size(); i++)
            {
                if(data.get(i)[0].equals(key)) return Integer.parseInt(this.data.get(i)[1]);
            }

            return -1;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public long getValueLong(String key)
    {
        try{
            for(int i = 0; i < data.size(); i++)
            {
                if(data.get(i)[0].equals(key)) return Long.parseLong(this.data.get(i)[1]);
            }

            return -1;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public void setValue(String key, String value)
    {
        try{
            String[] temp = new String[2];
            temp[0] = key;
            temp[1] = value;

            this.data.add(temp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setValue(String key, int value)
    {
        try{
            String[] temp = new String[2];
            temp[0] = key;
            temp[1] = Integer.toString(value);

            this.data.add(temp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setValue(String key, long value)
    {
        try{
            String[] temp = new String[2];
            temp[0] = key;
            temp[1] = Long.toString(value);

            this.data.add(temp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void remove(int index)
    {
        try{
            this.data.remove(index);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
