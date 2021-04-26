package es.deusto.mcu.librarianapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public class NetUtils {

    private static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * It checks if the device has connection to the Internet.
     * @param context is the object to get the connectivity manager.
     * @return whether the device has connection (true) or not (false).
     */
    public static boolean isWiFiConnected(Context context) {
        ConnectivityManager connMgr = getConnectivityManager(context);
        if (connMgr != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities netCapabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
                return (netCapabilities != null &&
                        netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            } else {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return (networkInfo != null && networkInfo.isConnected());
            }
        }
        return false;
    }

    /**
     * It checks if the device has connection to the Internet.
     * @param context is the object to get the connectivity manager.
     * @return whether the device has connection (true) or not (false).
     */
    public static boolean isAnyConnected(Context context) {
        ConnectivityManager connMgr = getConnectivityManager(context);
        if (connMgr != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities netCapabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
                return (netCapabilities != null &&
                        netCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
            } else {
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                return (networkInfo != null && networkInfo.isConnected());
            }
        }
        return false;
    }

}
