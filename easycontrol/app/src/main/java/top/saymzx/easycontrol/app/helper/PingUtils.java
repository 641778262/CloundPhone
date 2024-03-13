package top.saymzx.easycontrol.app.helper;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PingUtils {

    private static final int TIME_INTERVAL = 2;

    private Process process;
    private Thread checkPingThread;

    public interface OnGetTimeListener {
        void onGetTimeMs(int ms);
    }

    public void checkPings(String address, OnGetTimeListener listener) {
        String url = null;
        try {
            url = PublicTools.getIpAndPort(address).first;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        final String phoneUrl = url;
        if (checkPingThread == null) {
            checkPingThread = new Thread(() -> {
                try {
                    String command = "/system/bin/ping -i " + TIME_INTERVAL + " " + phoneUrl;
                    process = Runtime.getRuntime().exec(command);       //执行ping指令
                    InputStream is = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        listener.onGetTimeMs(getTimeMs(line));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            checkPingThread.start();
        }
    }

    /**
     * 64 bytes from 58.252.217.14: icmp_seq=68 ttl=57 time=89.5 ms
     */
    private int getTimeMs(String line) {
        if (TextUtils.isEmpty(line)) {
            return 0;
        }
        String str = "time=";
        if (!line.contains(str)) {
            return 0;
        }
        int index = line.indexOf(str);
        if (index < 0) {
            return 0;
        }
        String timeStr = line.substring(index + str.length());
        if (timeStr.contains("ms")) {
            String timeFloat = timeStr.substring(0, timeStr.indexOf("ms")).trim();
            try {
                return Math.round(Float.parseFloat(timeFloat));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return 0;
    }

    public void destroy() {
        try {
            if (checkPingThread != null) {
                checkPingThread.interrupt();
                checkPingThread = null;
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
