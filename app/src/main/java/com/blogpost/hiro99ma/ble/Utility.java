package com.blogpost.hiro99ma.ble;
import android.util.Log;

public class Utility {

    private static final String BLUETOOTH_SIG_UUID_BASE = "0000XXXX-0000-1000-8000-00805f9b34fb";
    private static final String HEX_CHARS="01234567890ABCDEF";
    private static final String TAG = "BDS:Utility";

    public static String normaliseUUID(String uuid) {
        String normalised_128_bit_uuid = uuid;
        if (uuid.length() == 4) {
            normalised_128_bit_uuid = BLUETOOTH_SIG_UUID_BASE.replace("XXXX",uuid);
        }
        if (uuid.length() == 32) {
            normalised_128_bit_uuid = uuid.substring(0,8) + "-"
                    + uuid.substring( 8,12) + "-"
                    + uuid.substring(12,16) + "-"
                    + uuid.substring(16,20) + "-"
                    + uuid.substring(20,32);
        }
        return normalised_128_bit_uuid;
    }

    public static String extractCharacteristicUuidFromTag(String tag) {
        String uuid="";
        String [] parts = tag.split("_");
        if (parts.length == 4) {
            uuid = parts[3];
        }
        return uuid;
    }

    public static String extractServiceUuidFromTag(String tag) {
        String uuid="";
        String [] parts = tag.split("_");
        if (parts.length == 4) {
            uuid = parts[2];
        }
        return uuid;
    }

    public static byte[] getByteArrayFromHexString(String hex_string) {
     String hex = hex_string.replace(" ", "");
     hex = hex.toUpperCase();

        byte[] bytes = new byte[hex.length() / 2];
        int i = 0;
        int j = 0;
        while (i < hex.length()) {
            String h1 = hex.substring(i, i + 1);
            String h2 = hex.substring(i + 1, i + 2);
            try {
                int b = (Integer.valueOf(h1, 16) * 16) + (Integer.valueOf(h2, 16));
                bytes[j++] = (byte) b;
                i = i + 2;
            } catch (NumberFormatException e) {
                Log.w(TAG, "NFE handling " + h1 + h2 + " with i=" + i);
                throw e;
            }
        }
        return bytes;
    }


    public static String byteArrayAsHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder hex = new StringBuilder();
        for (byte bt : bytes) {
            if ((bt >= 0) && (bt < 16)) {
                hex.append("0");
            }
            hex.append(Integer.toString(bt & 0xff, 16).toUpperCase());
        }
        return hex.toString();
    }

    public static boolean isValidHex(String hex_string) {
        String hex = hex_string.replace(" ","");
        hex = hex.toUpperCase();
        int len = hex.length();
        int remainder = len % 2;
        if (remainder != 0) {
            Log.w(TAG, "isValidHex: not even number of chars");
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!HEX_CHARS.contains(hex.substring(i, i + 1))) {
            	Log.w(TAG, "isValidHex: not HEX chars");
                return false;
            }
        }
        return true;
    }

}