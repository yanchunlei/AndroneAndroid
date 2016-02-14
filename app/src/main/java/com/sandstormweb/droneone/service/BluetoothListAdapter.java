package com.sandstormweb.droneone.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sandstormweb.droneone.R;

public class BluetoothListAdapter extends BaseAdapter
{
    private String[] data;
    private Context context;

    public BluetoothListAdapter(Context context, String[] data){
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try{
            LayoutInflater inflater = LayoutInflater.from(context);

            convertView = inflater.inflate(R.layout.list_item, parent, false);

            ((TextView)convertView.findViewById(R.id.list_item_text)).setText(data[position]);

            return convertView;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
