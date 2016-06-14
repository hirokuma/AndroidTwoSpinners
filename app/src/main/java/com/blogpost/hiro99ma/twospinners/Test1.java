package com.blogpost.hiro99ma.twospinners;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.blogpost.hiro99ma.ble.BleAdapterService;
import com.blogpost.hiro99ma.ble.Utility;

/**
 * Created on 2016/06/01.
 */
public class Test1 implements ITestForm {
    private static final Test1 mInstance = new Test1();
    public static Test1 getInstance() { return mInstance; }
    private Test1() {}

    private static final String MAIN_CATEGORY_NAME = "main1";

    private static final Command[] SUB_CATEGORY = new Command[]{
            //sub1-1
            new Command() {
                @Override
                public String name() {
                    return "sub1-1";
                }

                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-1");

                    byte[] bytes = new byte[3];
                    MessageHandler mMessageHandler = new MessageHandler(service, bytes);
                    service.setActivityHandler(mMessageHandler);

                    byte[] wrt = new byte[] { 0x12, 0x34, 0x56 };
                    service.writeCharacteristic(BleAdapterService.READ_WRITE_SERVICE_SERVICE_UUID, BleAdapterService.WRITE_CHARACTERISTIC_UUID, wrt);
                    Log.d(MAIN_CATEGORY_NAME, "end Test1");
                }
            },
            //sub1-2
            new Command() {
                @Override
                public String name() {
                    return "sub1-2";
                }

                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-2");

                    service.readCharacteristic(BleAdapterService.READ_WRITE_SERVICE_SERVICE_UUID, BleAdapterService.READ_CHARACTERISTIC_UUID);
                }
            },
            //sub1-3
            new Command() {
                @Override
                public String name() {
                    return "sub1-3";
                }

                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-3");
                }
            },
    };

    private static class MessageHandler extends Handler {
        private BleAdapterService mService;
        private byte[] mBytes;

        public MessageHandler(BleAdapterService service, byte[] bytes) {
            super(Looper.getMainLooper());
            mService = service;
            mBytes = bytes;
        }

        @Override
        public void handleMessage(Message msg) {
            final String TAG = "service handler";

            Bundle bundle;
            String service_uuid;
            String characteristic_uuid;
            String descriptor_uuid;
            byte[] bytes;

            switch (msg.what) {
                case BleAdapterService.GATT_CONNECTED:
                    Log.d(TAG, "GATT_CONNECTED");
                    //UIを有効にするなどの処理(接続ボタンがあれば無効にする)
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    Log.d(TAG, "GATT_DISCONNECT");
                    //UIを無効にするなどの処理(接続ボタンがあれば有効にする)
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    Log.d(TAG, "GATT_SERVICES_DISCOVERED");
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(TAG, "GATT_CHARACTERISTIC_READ: " + characteristic_uuid + ":" + service_uuid);
                    bytes = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    System.arraycopy(bytes, 0, mBytes, 0, bytes.length);
                    Log.d(TAG, "  Value=" + Utility.byteArrayAsHexString(bytes));
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(TAG, "GATT_CHARACTERISTIC_WRITTEN: " + characteristic_uuid + ":" + service_uuid);
                    bytes = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    System.arraycopy(bytes, 0, mBytes, 0, bytes.length);
                    Log.d(TAG, "  Value=" + Utility.byteArrayAsHexString(bytes));
                    mService.JoinCountDown();
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    Log.d(TAG, "GATT_DESCRIPTOR_WRITTEN: " + descriptor_uuid + "(" + characteristic_uuid + ":" + service_uuid + ")");
                    break;
                case BleAdapterService.NOTIFICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(TAG, "NOTIFICATION_RECEIVED: " + characteristic_uuid + ":" + service_uuid);
                    bytes = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    System.arraycopy(bytes, 0, mBytes, 0, bytes.length);
                    Log.d(TAG, "  Value=" + Utility.byteArrayAsHexString(bytes));
                    break;
                case BleAdapterService.GATT_REMOTE_RSSI:
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    Log.d(TAG, "MESSAGE: " + text);
                    break;
                case BleAdapterService.ERROR:
                    bundle = msg.getData();
                    String error = bundle.getString(BleAdapterService.PARCEL_ERROR);
                    Log.d(TAG, "ERROR: " + error);
            }
        }
    }

    @Override
    public String getCategoryName() {
        return MAIN_CATEGORY_NAME;
    }

    @Override
    public Command[] getSubCategory() {
        return SUB_CATEGORY;
    }
}
