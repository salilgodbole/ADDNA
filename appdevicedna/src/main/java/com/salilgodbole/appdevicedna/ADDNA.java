package com.salilgodbole.appdevicedna;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.WebView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by salil on 31/3/16.
 */
public class ADDNA {

    private static ADDNA instance = null;
    private Context mContext = null;

    public static ADDNA getInstance(Context context) {
        if (instance == null) {
            synchronized (ADDNA.class) {
                if (instance == null) {
                    instance = new ADDNA(context);
                }
            }
        }

        return instance;
    }

    private ADDNA(Context context) {
        mContext = context;
    }

    /**
     * Get the current app bundle.
     *
     * @return app bundle
     */
    public synchronized String getAppBundle() {
        return mContext.getPackageName();
    }

    /**
     * Get the current app name
     *
     * @return app name
     */
    public synchronized String getAppName() {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(getAppBundle(), 0);
        } catch (final PackageManager.NameNotFoundException ignored) {
        }

        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    /**
     * Get current app version.
     *
     * @return app version
     */
    public synchronized String getAppVersion() {
        try {
            return mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "unknown";
    }

    public synchronized long getAppInstalledDate() {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getAppBundle(), PackageManager.GET_ACTIVITIES);
            return packageInfo.firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    /**
     * It checks whether the app with the given packageName is installed or not.
     *
     * @param packageName - Bundle identifier for the app.
     * @return true if installed else false.
     */
    public synchronized boolean isAppInstalled(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public synchronized String getDeviceMake() {
        return Build.MANUFACTURER;
    }

    public synchronized String getDeviceModel() {
        return Build.MODEL;
    }

    public synchronized String getDeviceOSVersion() {
        return Build.VERSION.RELEASE;
    }

    public synchronized String getDeviceOS() {
        return "Android";
    }

    public synchronized String getUserAgent() {
        WebView webView = new WebView(mContext);
        return webView.getSettings().getUserAgentString();
    }

    /**
     * Get current system time. Pass date format to get the time in desired format.
     * Default format is dd-MM-yyyy hh:mm:ss aa i.e. 14-04-2016 12:36:27 PM
     *
     * @param format
     * @return
     */
    public synchronized String getCurrentTime(String format) {
        String datetime;
        Calendar c = Calendar.getInstance();
        if (TextUtils.isEmpty(format)) {
            format = "dd-MM-yyyy hh:mm:ss aa";
        }

        SimpleDateFormat dateformat = new SimpleDateFormat(format, Locale.getDefault());
        datetime = dateformat.format(c.getTime());

        return datetime;
    }

    /**
     * Get unique device id for the particular device. This will be same for every app on that device.
     *
     * @return unique device id.
     */
    public synchronized String getDeviceId() {
        return generateDeviceId();
    }

    /**
     * Get IP Address of the device.
     *
     * @return
     */
    public synchronized String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4)
                            return sAddr;
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    private synchronized String generateDeviceId() {

        // 1 compute DEVICE ID
        String m_szDevIDShort = getDeviceIdPseudo();
        // 2 android ID
        String m_szAndroidID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        // 3 SUM THE IDs
        String m_szLongID = m_szDevIDShort + m_szAndroidID;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte p_md5Data[] = new byte[0];
        if (m != null) {
            m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
            p_md5Data = m.digest();
        }
        String m_szUniqueID = "";
        for (byte aP_md5Data : p_md5Data) {
            int b = (0xFF & aP_md5Data);
            // if it is a single digit, make sure it have 0 in front (proper
            // padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
            // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();

        return m_szUniqueID;

    }

    private synchronized String getDeviceIdPseudo() {
        String tstr = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            // we make this look like a valid IMEI
            tstr += "35" + Build.SERIAL;
            tstr += (Build.FINGERPRINT.length() % 10)
                    + (Build.BOARD.length() % 10)
                    + (Build.DISPLAY.length() % 10)
                    + (Build.ID.length() % 10)
                    + (Build.SERIAL.length() % 10)
                    + (Build.HOST.length() % 10)
                    + (Build.HARDWARE.length() % 10)
                    + (Build.MODEL.length() % 10)
                    + (Build.PRODUCT.length() % 10)
                    + (Build.MANUFACTURER.length() % 10)
                    + (Build.BRAND.length() % 10)
                    + (Build.CPU_ABI.length() % 10)
                    + (Build.DEVICE.length() % 10);
        }
        return tstr;
    }

}
