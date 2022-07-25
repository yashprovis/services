package com.service.app.network;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.telephony.TelephonyManager;
import android.util.Log;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NetworkUtils {

    private NetworkUtils() {
        throw new AssertionError();
    }

    public static NetworkType getNetworkType(Context context) {

        Integer infoType = getInfoType(context).orElse(null);
        if (infoType == null)
            return NetworkType.NONE;

        switch (infoType) {
            case ConnectivityManager.TYPE_VPN:

                return NetworkType.VPN;
            case ConnectivityManager.TYPE_ETHERNET:

                return NetworkType.ETHERNET;
            case ConnectivityManager.TYPE_WIFI:

                return NetworkType.WIFI;
            case ConnectivityManager.TYPE_BLUETOOTH:
                return NetworkType.BLUETOOTH;
            case ConnectivityManager.TYPE_MOBILE:
                return getMobileNetworkType(context);
            default:
                return NetworkType.OTHER;
        }
    }

    public static boolean isConnected(Context context) {
        return getInfoType(context).isPresent();
    }

    public static boolean isWifiConnection(Context context) {

        return getInfoType(context).filter((e) -> e == ConnectivityManager.TYPE_WIFI).isPresent();
    }

    public static boolean isMobileConnection(Context context) {
        return getInfoType(context).filter((e) -> e == ConnectivityManager.TYPE_MOBILE).isPresent();
    }

    public static boolean isConnectionFast(Context context) {
        final List<NetworkType> fastNetworkTypes = Arrays.asList(
                NetworkType.WIFI,
                NetworkType.ETHERNET,
                NetworkType.VPN,
                NetworkType.NETWORK_MOBILE_3_G,
                NetworkType.NETWORK_MOBILE_4_G,
                NetworkType.NETWORK_MOBILE_5_G
        );

        return fastNetworkTypes.contains(getNetworkType(context));
    }

    public static Optional<String> getWifiSSID(Context context) {
        if (isWifiConnection(context)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            return Optional.ofNullable(info.getSSID());
        }
        return Optional.empty();
    }

    private static NetworkType getMobileNetworkType(Context context) {
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
//                PackageManager.PERMISSION_GRANTED) {
////            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) !=
////                    PackageManager.PERMISSION_GRANTED) {
//
//            return NetworkType.NETWORK_MOBILE_UNKNOWN;
//        }
        ConnectivityManager mgr = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE));
          int info=  mgr.getActiveNetworkInfo().getSubtype();
        Log.e("got the result","got the current data" );
        switch (info) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            
                return NetworkType.NETWORK_MOBILE_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
              
                return NetworkType.NETWORK_MOBILE_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
             
                return NetworkType.NETWORK_MOBILE_4_G;
            case TelephonyManager.NETWORK_TYPE_NR:
             
                return NetworkType.NETWORK_MOBILE_5_G;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
              
                return NetworkType.NETWORK_MOBILE_UNKNOWN;
        }
    }

    private static NetworkInfo getInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
    }

    private static Optional<Integer> getInfoType(Context context) {
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return Optional.empty();
        }
        return Optional.of(info.getType());
    }

    public enum NetworkType {
        NONE("No internet"),
        OTHER("Unknown internet connection type"),
        ETHERNET("Ethernet"),
        WIFI("WiFi"),
        BLUETOOTH("Bluetooth"),
        VPN("VPN"),
        NETWORK_MOBILE_UNKNOWN("Unknown mobile network"),
        NETWORK_MOBILE_2_G("Mobile - 2G"),
        NETWORK_MOBILE_3_G("Mobile - 3G"),
        NETWORK_MOBILE_4_G("Mobile - 4G"),
        NETWORK_MOBILE_5_G("Mobile - 5G");

        private String name;

        NetworkType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
