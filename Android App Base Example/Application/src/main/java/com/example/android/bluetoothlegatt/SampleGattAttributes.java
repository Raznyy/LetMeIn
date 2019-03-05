package com.example.android.bluetoothlegatt;

import android.util.Log;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String ACTION_USER_CHECK_SERVICE = "0000ffe9-0000-1000-8000-00805f9b34fb";
    public static String ACTION_READ_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String WRITE_CHARACTERISTICS = "a922bc74-81dc-444a-8f5f-fbe1a4ec685c";
    public static String PIN_CHECK = "c0d86de0-f0e1-4935-af1a-daff2b3911d8";

    static {
        // Sample Services.
        //attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        //attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(ACTION_USER_CHECK_SERVICE, ACTION_READ_CHARACTERISTIC_CONFIG);
        attributes.put(ACTION_USER_CHECK_SERVICE, WRITE_CHARACTERISTICS);
        attributes.put(ACTION_USER_CHECK_SERVICE, PIN_CHECK);
        attributes.put(ACTION_READ_CHARACTERISTIC_CONFIG, "Read data");
        attributes.put(WRITE_CHARACTERISTICS, "Write data");
        attributes.put(PIN_CHECK, "Check PIN");
    }

    public static String lookup(String uuid, String defaultName)
    {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
