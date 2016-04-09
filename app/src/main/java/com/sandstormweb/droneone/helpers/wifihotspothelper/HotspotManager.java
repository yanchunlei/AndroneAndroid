package com.sandstormweb.droneone.helpers.wifihotspothelper;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;

public class HotspotManager
{
    public static boolean isHotspotEnable(Context context)
    {
        try{
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean)method.invoke(wifiManager);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setHotspotEnabled(Context context, boolean enable)
    {
        try{
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration wifiConfiguration = null;

            if(isHotspotEnable(context)){
                wifiManager.setWifiEnabled(false);
            }

            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, wifiConfiguration, enable);
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setHotspotSSID(Context context, String name)
    {
        try{
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            Method getHotspotConfig = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfiguration = (WifiConfiguration)getHotspotConfig.invoke(wifiManager);

            wifiConfiguration.SSID = name;

            Method setHotspotConfig = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setHotspotConfig.invoke(wifiManager, wifiConfiguration);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean enableWPA2Auth(Context context, String password)
    {
        try{
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            Method getHotspotConfig = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfiguration = (WifiConfiguration)getHotspotConfig.invoke(wifiManager);

            wifiConfiguration.preSharedKey = password;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

//            wifiConfiguration.wepKeys[0] = password;
//            wifiConfiguration.wepTxKeyIndex = 0;

            Method setHotspotConfig = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setHotspotConfig.invoke(wifiManager, wifiConfiguration);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static String getCurrentConnectedHotspotIp(Context context)
    {
        try{
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifiInfo.getIpAddress();

            int ip = wifiManager.getDhcpInfo().serverAddress;
            byte[] ipBytes = BigInteger.valueOf(ip).toByteArray();
            return InetAddress.getByAddress(ipBytes).getHostAddress();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
