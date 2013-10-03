package com.phinmadvader.andcpp;

import android.os.Environment;

/**
 * Created by invader on 8/11/13.
 */
public class Constants {
    public static String dcDirectory = Environment.getExternalStorageDirectory()+"/DC++";
    public static String dcConfDirectory = ".andcpp";
    public static String PREFS_NAME = "ANDCPPSETTINGS";
    public static String SETTINGS_IP_KEY = "dcipaddr";
    public static String SETTINGS_NICK_KEY = "dcnick";
    public static int MAX_DOWNLOAD_Q = 128;
    public static int DOWNLOAD_TIMEOUT_MILLIS = 10000;
    public static int DOWNLOAD_UPDATE_INTERVAL_MILLIS = 100; // Updates all download activities every 200ms
    public static int POLLING_CLASS_TIME_IN_MILLIS = 1000; // Interval frequency for poll updates
}
