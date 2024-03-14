package com.feihu.cp.adb;

public interface AdbBase64 {
    String encodeToString(byte[] data);

    byte[] decode(byte[] data);
}
