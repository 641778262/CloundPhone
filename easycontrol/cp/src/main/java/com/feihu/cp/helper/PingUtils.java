package com.feihu.cp.helper;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PingUtils {

    private static final int TIME_INTERVAL = 2;

    private Process process;
    private Thread checkPingThread;

    public interface OnGetTimeListener {
        void onGetTimeMs(int ms);
    }

    public void checkPings(String address, OnGetTimeListener listener) {

        if (checkPingThread == null) {
            checkPingThread = new Thread(() -> {
                try {
                    String url = PublicTools.getIpAndPort(address).first;
                    if (TextUtils.isEmpty(url)) {
                        return;
                    }
                    String command = "/system/bin/ping -i " + TIME_INTERVAL + " " + url;
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
    public static int getTimeMs(String line) {
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

    public static void getBestIp(String addresses) {
        final String[] strs = addresses.split(";");
        new Thread(() -> {
            try {
                Log.d("aaa", "start");
                for (int i = 0; i < strs.length; i++) {
                    String url = strs[i].trim();
//                    String url = PublicTools.getIpAndPort(address).first;
//                    if (TextUtils.isEmpty(url)) {
//                        return;
//                    }
                    String command = "/system/bin/ping -i " + TIME_INTERVAL + " " + url;
                    Process process = Runtime.getRuntime().exec(command);       //执行ping指令
                    InputStream is = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    int count = 0;
                    int times = 0;
                    while ((line = reader.readLine()) != null && count < 6) {
                        count++;
                        times = times + getTimeMs(line);
                    }
                    Log.d("aaa", "address==" + url + ";times ==" + times);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void getBestIp2(String addresses) {
        final String[] strs = addresses.split(";");
        Map<String,Integer> map = new HashMap<>();
        Log.d("aaa", "start");
        CountDownLatch countDownLatch = new CountDownLatch(strs.length);
        for (int i = 0; i < strs.length; i++) {
            final int index = i;
           Thread thread =  new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = null;
                    BufferedReader reader = null;
                    try {
                        String url = strs[index].trim();
                        String command = "/system/bin/ping -i " + 1 + " " + url;
                        Process process = Runtime.getRuntime().exec(command);       //执行ping指令
                        is = process.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(is));
                        String line;
                        int count = 0;
                        int times = 0;
                        while ((line = reader.readLine()) != null && count < 3) {
                            count++;
                            times = times + getTimeMs(line);
                            Log.d("aaa","url="+url+";times="+getTimeMs(line));
                        }
                        is.close();
                        reader.close();
                        Log.d("aaa", "address==" + url + ";times ==" + times);
                        map.put(url,times);
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if(is != null) {
                                is.close();
                            }
                            if(reader != null) {
                                reader.close();
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
           thread.start();
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String ip = null;
        int min = 0;
        for(Map.Entry<String,Integer> entry: map.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            if(value < min || min == 0) {
                min = value;
                ip = key;
            }
        }
        Log.d("aaa", "map="+map);
        Log.d("aaa", "key="+ip);
    }
}

