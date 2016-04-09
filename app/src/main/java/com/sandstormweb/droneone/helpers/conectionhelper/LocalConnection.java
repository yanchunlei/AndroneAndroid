package com.sandstormweb.droneone.helpers.conectionhelper;

import android.content.Context;

import com.sandstormweb.droneone.helpers.wifihotspothelper.HotspotManager;

import java.net.Socket;

public class LocalConnection extends Connection
{
    public LocalConnection(Context context, int port, String username, String password)
    {
        super(HotspotManager.getCurrentConnectedHotspotIp(context), port, username, password);

        System.out.println("server : "+super.getServer());
    }

    @Override
    protected boolean login(Socket socket, String username, String password) {
        return true;
    }
}
